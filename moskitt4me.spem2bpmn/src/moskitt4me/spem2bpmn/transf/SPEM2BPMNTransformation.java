package moskitt4me.spem2bpmn.transf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import moskitt4me.spem2bpmn.util.SPEM2BPMNUtil;

import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.CallActivity;
import org.eclipse.bpmn2.Documentation;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.Property;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.UserTask;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.epf.uma.Activity;
import org.eclipse.epf.uma.CapabilityPattern;
import org.eclipse.epf.uma.MethodElement;
import org.eclipse.epf.uma.MethodPackage;
import org.eclipse.epf.uma.ProcessElement;
import org.eclipse.epf.uma.ProcessPackage;
import org.eclipse.epf.uma.TaskDescriptor;
import org.eclipse.epf.uma.VariabilityElement;
import org.eclipse.epf.uma.VariabilityType;
import org.eclipse.epf.uma.WorkBreakdownElement;
import org.eclipse.epf.uma.WorkOrder;

/**
 * This class implements a Model-to-Model transformation that obtains a BPMN 2.0 representation
 * (in terms of the Activiti Designer) of a SPEM 2.0 delivery process (defined by means of the
 * EPF Composer).
 *
 * @author Mario Cervera
 */
public class SPEM2BPMNTransformation {

	/*
	* Number of BPMN 2.0 tasks that have been generated
	*/
	private int userTaskCounter;
	
	/*
	* Number of BPMN 2.0 sequence flows that have been generated
	*/
	private int sequenceFlowCounter;
	
	/*
	* Number of BPMN 2.0 call activities that have been generated
	*/
	private int callActivityCounter;
	
	/*
	* The root of the BPMN 2.0 model
	*/
	private Activity rootActivity;
	
	/*
	* The folder where the output model will be stored
	*/
	private String outputFolder;
	
	private List<String> generatedCapabilityPatterns;
	
	public SPEM2BPMNTransformation(Activity rootActivity, String outputFolder,
			int userTaskCounter, int sequenceFlowCounter, int callActivityCounter,
			List<String> generatedCapabilityPatterns) {
		
		this.rootActivity = rootActivity;
		this.outputFolder = outputFolder;
		
		this.userTaskCounter = userTaskCounter;
		this.sequenceFlowCounter = sequenceFlowCounter;
		this.callActivityCounter = callActivityCounter;
		
		this.generatedCapabilityPatterns = generatedCapabilityPatterns;
	}
	
	/*
	* This method generates the content of the BPMN 2.0 model (not the graphical representations)
	*/
	public int generateBPMNModelContent() throws Exception {
		
		ProcessPackage rootElement = SPEM2BPMNUtil.getProcessPackage(rootActivity);
		
		Resource outputResource = SPEM2BPMNUtil.createResource(outputFolder,
				rootActivity.getGuid() + ".activiti");
		
		Process p = generateRootProcess(rootActivity);
		outputResource.getContents().add(p);
		
		StartEvent se = generateStartEvent();
		outputResource.getContents().add(se);
		
		EndEvent ee = generateEndEvent();
		outputResource.getContents().add(ee);
		
		//Once the Process, the StartEvent and the EndEvent have been created, 
		//create the UserTasks and CallActivities
		
		for(ProcessElement elem: rootElement.getProcessElements()) {
			
			if(elem instanceof TaskDescriptor) {
				TaskDescriptor td = (TaskDescriptor) elem;
				UserTask ut = generateUserTask(td);
				
				outputResource.getContents().add(ut);
				
				//Create a SequenceFlow from the StartEvent to the UserTask if it has no predecessor
				
				if(!SPEM2BPMNUtil.hasPredecessor(td)) {
					SequenceFlow sf = generateSequenceFlow(se, ut);
					outputResource.getContents().add(sf);
				}
			}
		}
		
		for(MethodPackage mp : rootElement.getChildPackages()) {
			if(mp instanceof ProcessPackage) {
				ProcessPackage pp = (ProcessPackage) mp;
				Activity actv = SPEM2BPMNUtil.getActivity(pp);
				Activity actv_backup = null;
				
				boolean alreadyGenerated = false;
				String cpID = null;
				
				if(actv instanceof CapabilityPattern &&
						((CapabilityPattern)actv).getVariabilityType().equals(VariabilityType.EXTENDS)) {
					
					cpID = actv.getGuid();
					
					VariabilityElement ve = ((CapabilityPattern)actv).getVariabilityBasedOnElement();
					
					if(ve instanceof CapabilityPattern) {
						CapabilityPattern cp = (CapabilityPattern) ve;
						
						if(!generatedCapabilityPatterns.contains(cp.getGuid())) {
							generatedCapabilityPatterns.add(cp.getGuid());
						}
						else {
							alreadyGenerated = true;
						}
						
						actv_backup = actv;
						actv = cp;
					}
				}
				
				CallActivity ca = generateCallActivity(actv, cpID);
				
				outputResource.getContents().add(ca);
				
				//Create a SequenceFlow from the StartEvent to the CallActivity if it has no predecessor
				
				if(!SPEM2BPMNUtil.hasPredecessor(actv_backup != null ? actv_backup : actv)) {
					SequenceFlow sf = generateSequenceFlow(se, ca);
					outputResource.getContents().add(sf);
				}
				
				if(!alreadyGenerated) {
				
					//Recursively, invoke transformation for the nested elements
					
					SPEM2BPMNTransformation t = new SPEM2BPMNTransformation(
							actv, outputFolder, userTaskCounter, 1, 1, generatedCapabilityPatterns);

					userTaskCounter = t.generateBPMNModelContent();
				}
			}
		}
		
		//Once the UserTasks and CallActivities have been created, 
		//create the SequenceFlows that connect these elements
		
		for(ProcessElement elem: rootElement.getProcessElements()) {
			if(elem instanceof TaskDescriptor) {
				TaskDescriptor td = (TaskDescriptor) elem;
				
				List<SequenceFlow> sequenceFlows = generatePredecessorSequenceFlows(
						td, outputResource.getContents());
				
				outputResource.getContents().addAll(sequenceFlows);
			}
		}
		
		for(MethodPackage mp : rootElement.getChildPackages()) {
			if(mp instanceof ProcessPackage) {
				ProcessPackage pp = (ProcessPackage) mp;
				Activity actv = SPEM2BPMNUtil.getActivity(pp);
				
				List<SequenceFlow> sequenceFlows = generatePredecessorSequenceFlows(
						actv, outputResource.getContents());
				
				outputResource.getContents().addAll(sequenceFlows);
			}
		}
		
		//Finally, create the SequenceFlows that connect the last UserTasks 
		//and CallActivities with the EndEvent
		
		for(ProcessElement elem: rootElement.getProcessElements()) {
			if(elem instanceof TaskDescriptor) {
				TaskDescriptor td = (TaskDescriptor) elem;
				if(!SPEM2BPMNUtil.hasSuccessor(td, outputResource.getContents(), null)) {
					
					FlowNode source = SPEM2BPMNUtil.getFlowNode(td, outputResource.getContents(), null);
					if(source != null) {
						SequenceFlow sf = generateSequenceFlow(source, ee);
						outputResource.getContents().add(sf);
					}
				}
			}
		}
		
		for(MethodPackage mp : rootElement.getChildPackages()) {
			if(mp instanceof ProcessPackage) {
				ProcessPackage pp = (ProcessPackage) mp;
				Activity actv = SPEM2BPMNUtil.getActivity(pp);
				String cpId = null;
				
				if(actv instanceof CapabilityPattern &&
						((CapabilityPattern)actv).getVariabilityType().equals(VariabilityType.EXTENDS)) {
					
					CapabilityPattern cp = (CapabilityPattern) actv;
					VariabilityElement ve = cp.getVariabilityBasedOnElement();
					
					if(ve instanceof CapabilityPattern) {
						CapabilityPattern cp2 = (CapabilityPattern) ve;
						actv = cp2;
						cpId = cp.getGuid();
					}
				}
				
				if(!SPEM2BPMNUtil.hasSuccessor(actv, outputResource.getContents(), cpId)) {
					
					FlowNode source = SPEM2BPMNUtil.getFlowNode(actv, outputResource.getContents(), cpId);
					if(source != null) {
						SequenceFlow sf = generateSequenceFlow(source, ee);
						outputResource.getContents().add(sf);
					}
				}
			}
		}
		
		outputResource.save(Collections.EMPTY_MAP);
		
		return userTaskCounter;
	}
	
	// ------------------------------------------------------------------------------------------------
	// Private methods (generate the different BPMN 2.0 elements: start event, end event, tasks, etc.)
	// ------------------------------------------------------------------------------------------------
	
	private Process generateRootProcess(Activity activity) {
		
		Process p = Bpmn2Factory.eINSTANCE.createProcess();
		p.setId(activity.getGuid());
		p.setName(getName(activity));
		
		Documentation doc = Bpmn2Factory.eINSTANCE.createDocumentation();
		doc.setId("documentation_process");
		doc.setText("");
		doc.setTextFormat("text/plain");
		
		p.getDocumentation().add(doc);
		
		return p;
	}
	
	private StartEvent generateStartEvent() {
		
		StartEvent se = Bpmn2Factory.eINSTANCE.createStartEvent();
		se.setId("startevent1");
		se.setName("Start");
		
		return se;
	}
	
	private EndEvent generateEndEvent() {
		
		EndEvent ee = Bpmn2Factory.eINSTANCE.createEndEvent();
		ee.setId("endevent1");
		ee.setName("End");
		
		return ee;
	}
	
	private UserTask generateUserTask(TaskDescriptor td) {
		
		UserTask userTask = Bpmn2Factory.eINSTANCE.createUserTask();
		userTask.setId("usertask" + userTaskCounter);
		userTask.setName(getName(td));
		userTask.setAssignee("kermit");
		
		//Every UserTask must store the guid of the TaskDescriptor it corresponds to
		
		Property property = Bpmn2Factory.eINSTANCE.createProperty();
		property.setId("uma_element");
		property.setName(td.getGuid());
		
		userTask.getProperties().add(property);
		
		userTaskCounter++;
		
		return userTask;
	}
	
	private CallActivity generateCallActivity(Activity actv, String cpID) {
		
		String callActivityID = "";
		
		if(cpID == null) {
			callActivityID = "callactivity" + callActivityCounter;
		}
		else {
			callActivityID = "capabilityPattern" + cpID;
		}
		
		CallActivity ca = Bpmn2Factory.eINSTANCE.createCallActivity();
		ca.setId(callActivityID);
		ca.setName(getName(actv));
		ca.setCalledElement(actv.getGuid());
		
		callActivityCounter++;
		
		return ca;
	}
	
	private SequenceFlow generateSequenceFlow(FlowNode source, FlowNode target) {
		
		SequenceFlow sf = Bpmn2Factory.eINSTANCE.createSequenceFlow();
		sf.setId("flow" + sequenceFlowCounter);
		sf.setSourceRef(source);
		sf.setTargetRef(target);
		
		sequenceFlowCounter++;
		
		return sf;
	}
	
	private List<SequenceFlow> generatePredecessorSequenceFlows(
			WorkBreakdownElement wbe, EList<EObject> eobjects) {
		
		List<SequenceFlow> sequenceFlows = new ArrayList<SequenceFlow>();
		
		if(SPEM2BPMNUtil.hasPredecessor(wbe)) {
			for(WorkOrder wo : wbe.getLinkToPredecessor()) {
				WorkBreakdownElement pred = wo.getPred();
				WorkBreakdownElement predecessor = pred;
				WorkBreakdownElement element = wbe;
				if(pred != null) {
					String cpIds = null;
					String cpIdt = null;
					if(pred instanceof CapabilityPattern &&
							((CapabilityPattern)pred).getVariabilityType().equals(VariabilityType.EXTENDS)) {	
						CapabilityPattern cp = (CapabilityPattern) pred;
						cpIds = cp.getGuid();
						predecessor = getCapabilityPattern(cp);
					}
					if(wbe instanceof CapabilityPattern &&
							((CapabilityPattern)wbe).getVariabilityType().equals(VariabilityType.EXTENDS)) {
						CapabilityPattern cp = (CapabilityPattern) wbe;
						cpIdt = cp.getGuid();
						element = getCapabilityPattern(cp);
					}
					FlowNode source = SPEM2BPMNUtil.getFlowNode(predecessor, eobjects, cpIds);
					FlowNode target = SPEM2BPMNUtil.getFlowNode(element, eobjects, cpIdt);
					
					if(source != null && target != null) {
						SequenceFlow sf = generateSequenceFlow(source, target);
						sequenceFlows.add(sf);
					}
				}
			}
		}
		
		return sequenceFlows;
	}
	
	private String getName(MethodElement me) {
		
		if(me.getPresentationName() != null && !me.getPresentationName().equals("")) {
			return me.getPresentationName();
		}
		else if(me.getName() != null && !me.getName().equals("")) {
			return me.getName();
		}
		
		return me.getGuid();
	}
	
	private CapabilityPattern getCapabilityPattern(CapabilityPattern cp) {
		
		if(cp.getVariabilityType().equals(VariabilityType.EXTENDS)) {
			
			VariabilityElement ve = cp.getVariabilityBasedOnElement();
			
			if(ve instanceof CapabilityPattern) {
				CapabilityPattern cp2 = (CapabilityPattern) ve;
				return cp2;
			}
		}
		
		return cp;
	}
}
