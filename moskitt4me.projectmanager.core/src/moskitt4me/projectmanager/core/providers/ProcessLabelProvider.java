package moskitt4me.projectmanager.core.providers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import moskitt4me.projectmanager.core.Activator;
import moskitt4me.projectmanager.core.context.Context;
import moskitt4me.projectmanager.core.context.Context.ProcessVisualization;
import moskitt4me.projectmanager.methodspecification.MethodElements;
import moskitt4me.projectmanager.processsupport.Engine;
import moskitt4me.projectmanager.processsupport.util.EngineUtil;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.epf.uma.Activity;
import org.eclipse.epf.uma.BreakdownElement;
import org.eclipse.epf.uma.CapabilityPattern;
import org.eclipse.epf.uma.DeliveryProcess;
import org.eclipse.epf.uma.MethodLibrary;
import org.eclipse.epf.uma.Phase;
import org.eclipse.epf.uma.TaskDescriptor;
import org.eclipse.epf.uma.VariabilityElement;
import org.eclipse.epf.uma.VariabilityType;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * A Label Provider for the Process view. Provides labels and iconcs for the graphical elements.
 * 
 * @author Mario Cervera
 */
public class ProcessLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		
		if(element instanceof MethodLibrary) {
			MethodLibrary ml = (MethodLibrary) element;
			return ml.getPresentationName() != null 
				&& !ml.getPresentationName().equals("") ? ml.getPresentationName() : ml.getName();
		}
		else if(element instanceof DeliveryProcess) {
			DeliveryProcess dp = (DeliveryProcess) element;
			return dp.getPresentationName() != null ? dp.getPresentationName() : dp.getName();
		}
		else if(element instanceof CapabilityPattern) {
			CapabilityPattern cp = (CapabilityPattern) element;
			
			return cp.getPresentationName() != null &&
				!cp.getPresentationName().equals("")? cp.getPresentationName() : cp.getName();
		}
		else if(element instanceof Activity) {
			Activity actv = (Activity) element;
			return actv.getPresentationName() != null ? actv.getPresentationName() : actv.getName();
		}
		else if(element instanceof TaskDescriptor) {
			TaskDescriptor td = (TaskDescriptor) element;
			return td.getPresentationName() != null ? td.getPresentationName() : td.getName();
		}
		
		return super.getText(element);
	}
	
	@Override
	public Image getImage(Object element) {
		
		try {
			
			String imagePath = FileLocator.toFileURL(Platform.getBundle(
			Activator.PLUGIN_ID).getResource("icons/full/obj16")).getPath();
			
			if(element instanceof MethodLibrary) {
				Image image = new Image(Display.getCurrent(), imagePath + "MethodLibrary.gif");
				return image;
			}
			else if (element instanceof DeliveryProcess) {
				Image image = new Image(Display.getCurrent(), imagePath + "DeliveryProcess.gif");
				return image;
			}
			else if(element instanceof Activity) {
				Image image = null;
				Activity actv = (Activity) element;
				
				List<String> cpIds = new ArrayList<String>();
				
				String colour = "";
				if(actv instanceof CapabilityPattern) {
					CapabilityPattern cp = (CapabilityPattern) actv;
					if(cp instanceof CapabilityPatternCP) {
						CapabilityPatternCP cpcp = (CapabilityPatternCP) cp;
						cpIds.addAll(cpcp.getCpGuids());
					}
					if(cp.getVariabilityType().equals(VariabilityType.EXTENDS)) {
						cpIds.add(cp.getGuid());
					}
					colour = getActivityColour(actv, cpIds);
				}
				else if (actv instanceof ActivityCP){
					ActivityCP actv2 = (ActivityCP) actv;
					colour = getActivityColour(actv, actv2.getCpGuids());
				}
				else {
					colour = getActivityColour(actv, null);
				}
				
				if(colour.equals("Red")) {
					if(actv instanceof Phase) {
						image = new Image(Display.getCurrent(), imagePath + "PhaseRed.gif");
					}
					else if(actv instanceof CapabilityPattern) {
						image = new Image(Display.getCurrent(), imagePath + "CapabilityPatternRed.gif");
					}
					else {
						image = new Image(Display.getCurrent(), imagePath + "ActivityRed.PNG");
					}
				}
				else if(colour.equals("Green")) {
					if(actv instanceof Phase) {
						image = new Image(Display.getCurrent(), imagePath + "PhaseGreen.gif");
					}
					else if(actv instanceof CapabilityPattern) {
						image = new Image(Display.getCurrent(), imagePath + "CapabilityPatternGreen.gif");
					}
					else {
						image = new Image(Display.getCurrent(), imagePath + "ActivityGreen.PNG");
					}
				}
				else if(colour.equals("Grey")) {
					if(actv instanceof Phase) {
						image = new Image(Display.getCurrent(), imagePath + "PhaseGrey.gif");
					}
					else if(actv instanceof CapabilityPattern) {
						image = new Image(Display.getCurrent(), imagePath + "CapabilityPatternGrey.gif");
					}
					else {
						image = new Image(Display.getCurrent(), imagePath + "ActivityGrey.PNG");
					}
				}
				else {
					if(actv instanceof Phase) {
						image = new Image(Display.getCurrent(), imagePath + "PhaseBlue.gif");
					}
					else if(actv instanceof CapabilityPattern) {
						image = new Image(Display.getCurrent(), imagePath + "CapabilityPatternBlue.gif");
					}
					else {
						image = new Image(Display.getCurrent(), imagePath + "ActivityBlue.PNG");
					}
				}
				
				return image;
			}
			else if(element instanceof TaskDescriptor) {
				Image image = null;
				TaskDescriptor td = (TaskDescriptor) element;
				
				String activitiTaskId = MethodElements.getActivitiTaskId(td);
				String processInstanceId = Context.processInstanceId;
				List<String> cpIds = new ArrayList<String>();
				if(td instanceof TaskDescriptorCP) {
					TaskDescriptorCP tdcp = (TaskDescriptorCP) td;
					cpIds.addAll(tdcp.getCpGuids());
					processInstanceId = Context.getProcessInstance(cpIds);
				}
				
				if (Engine.isExecutable(activitiTaskId,
						processInstanceId, Context.selectedProject)) {
					
					if(td.getIsOptional()) {
						image = new Image(Display.getCurrent(), imagePath + "TaskGrey.gif");
					}
					else {
						image = new Image(Display.getCurrent(), imagePath + "TaskGreen.gif");
					}
				}
				else if(EngineUtil.hasBeenExecuted(activitiTaskId, 
						cpIds, Context.selectedProject)) {
						
					image = new Image(Display.getCurrent(), imagePath + "TaskBlue.gif");
				}
				else  {
					image = new Image(Display.getCurrent(), imagePath + "TaskRed.gif");
				}
				
				return image;
			}
		}
		catch(IOException e) {
			return null;
		}
		
		return super.getImage(element);
	}
	
	private String getActivityColour(Activity actv, List<String> cpIds) {
		
		String colour = "";
		
		if(actv instanceof CapabilityPattern) {
			CapabilityPattern cp = (CapabilityPattern) actv;
			
			if(cp.getVariabilityType().equals(VariabilityType.EXTENDS)) {
				CapabilityPattern cp2 = MethodElements.getCapabilityPattern(getGUId(cp));
				if(cp2 != null) {
					actv = cp2;
				}
			}
		}

		List<BreakdownElement> elements = actv.getBreakdownElements();
		int size = elements.size();
		
		if(Context.currentProcessVisualization == ProcessVisualization.NEXTTASKS) {
			
			colour = "Grey";
			for (int i = 0 ; i < size; i++) {
				
				BreakdownElement be = elements.get(i);
				
				if(be instanceof TaskDescriptor) {
					TaskDescriptor td = (TaskDescriptor) be;
					String activitiTaskId = MethodElements.getActivitiTaskId(td);
					String processInstanceId = "";
					if(cpIds == null || cpIds.size() == 0) {
						processInstanceId = Context.processInstanceId;
					}
					else {
						processInstanceId = Context.getProcessInstance(cpIds);
					}
					
					if (Engine.isExecutable(activitiTaskId,
							processInstanceId, Context.selectedProject)) {
						
						if(!td.getIsOptional()) {
							return "Green";
						}
					}
				}
				else if(be instanceof Activity) {
					Activity actv2 = (Activity) be;
					
					String activityColour = "";
					List<String> guids = new ArrayList<String>();
					if(actv2 instanceof CapabilityPattern) {
						if(actv2 instanceof CapabilityPatternCP) {
							CapabilityPatternCP cpcp = (CapabilityPatternCP) actv2;
							guids.addAll(cpcp.getCpGuids());
						}
						else if(cpIds != null && cpIds.size() > 0) {
							guids.addAll(cpIds);
						}
						if(((CapabilityPattern)actv2).getVariabilityType().equals(VariabilityType.EXTENDS)) {
							guids.add(actv2.getGuid());
						}
					}
					else if(actv2 instanceof ActivityCP) {
						ActivityCP actvcp = (ActivityCP) actv2;
						guids.addAll(actvcp.getCpGuids());
					}
					else {
						if(cpIds == null) {
							cpIds = new ArrayList<String>();
						}
						guids.addAll(cpIds);	
					}
					activityColour = getActivityColour(actv2, guids);
					
					if(activityColour.equals("Green")) {
						return "Green";
					}
				}
			}
		}
		else {
			colour = "Red";

			for (int i = 0 ; i < size; i++) {
				
				BreakdownElement be = elements.get(i);
				
				if(be instanceof TaskDescriptor) {
					TaskDescriptor td = (TaskDescriptor) be;		
					String activitiTaskId = MethodElements.getActivitiTaskId(td);
					String processInstanceId = "";
					if(cpIds == null || cpIds.size() == 0) {
						processInstanceId = Context.processInstanceId;
					}
					else {
						processInstanceId = Context.getProcessInstance(cpIds);
					}
					
					if (Engine.isExecutable(activitiTaskId,
							processInstanceId, Context.selectedProject)) {
						
						if(!colour.equals("Green") && td.getIsOptional()) {
							colour = "Grey";
						}
						
						if(!td.getIsOptional()) {
							return "Green";
						}
					}
					else if (EngineUtil.hasBeenExecuted(activitiTaskId,
							cpIds, Context.selectedProject)) {
						
						if(colour.equals("Red")) {
							colour = "Blue";
						}
					}
				}
				else if(be instanceof Activity) {
					Activity actv2 = (Activity) be;
					
					String activityColour = "";
					List<String> guids = new ArrayList<String>();
					if(actv2 instanceof CapabilityPattern) {
						if(actv2 instanceof CapabilityPatternCP) {
							CapabilityPatternCP cpcp = (CapabilityPatternCP) actv2;
							guids.addAll(cpcp.getCpGuids());
						}
						else if(cpIds != null && cpIds.size() > 0) {
							guids.addAll(cpIds);
						}
						if(((CapabilityPattern)actv2).getVariabilityType().equals(VariabilityType.EXTENDS)) {
							guids.add(actv2.getGuid());
						}
					}
					else if(actv2 instanceof ActivityCP) {
						ActivityCP actvcp = (ActivityCP) actv2;
						guids.addAll(actvcp.getCpGuids());
					}
					else {
						if(cpIds == null) {
							cpIds = new ArrayList<String>();
						}
						guids.addAll(cpIds);
						
					}
					activityColour = getActivityColour(actv2, guids);
					
					if(activityColour.equals("Grey")) {
						if(!colour.equals("Green")) {
							colour = "Grey";
						}
					}
					else if(activityColour.equals("Blue")) {
						if(colour.equals("Red")) {
							colour = "Blue";
						}
					}
					else if(activityColour.equals("Green")) {
						return "Green";
					}
				}
			}
		}
		
		return colour;
	}
	
	private String getGUId(CapabilityPattern cp) {
		
		VariabilityElement ve = cp.getVariabilityBasedOnElement();
		if(ve instanceof CapabilityPattern) {
			CapabilityPattern cp2 = (CapabilityPattern) ve;
			if(cp2.eIsProxy()) {
				URI proxyURI = ((InternalEObject)cp2).eProxyURI();
				return proxyURI.host();
			}
		}
		
		return "";
	}
}
