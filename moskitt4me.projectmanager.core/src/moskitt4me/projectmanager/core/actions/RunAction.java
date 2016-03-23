package moskitt4me.projectmanager.core.actions;

import java.util.ArrayList;
import java.util.List;

import moskitt4me.projectmanager.core.context.Context;
import moskitt4me.projectmanager.core.providers.ActivityCP;
import moskitt4me.projectmanager.core.providers.CapabilityPatternCP;
import moskitt4me.projectmanager.core.providers.TaskDescriptorCP;
import moskitt4me.projectmanager.core.views.ProcessView;
import moskitt4me.projectmanager.methodspecification.MethodElements;
import moskitt4me.projectmanager.processsupport.Engine;
import moskitt4me.projectmanager.processsupport.util.EngineUtil;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.epf.uma.Activity;
import org.eclipse.epf.uma.BreakdownElement;
import org.eclipse.epf.uma.CapabilityPattern;
import org.eclipse.epf.uma.TaskDescriptor;
import org.eclipse.epf.uma.VariabilityElement;
import org.eclipse.epf.uma.VariabilityType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

/*
* The Run Action (which is shown as a button in the Action Bar of the Process view) allows the
* user to set a Task or an Activity as executed. When a Task or Activity is set as executed, the
* Project Manager Component notifies the Activiti Engine, which takes the process instance to its
* next state.
*
* @author Mario Cervera
*/
public class RunAction extends Action implements IAction {

	public RunAction() {

		setEnabled(false);
		setId("RunAction");
	}
	
	@Override
	public void run() {

		// Get the Process View object

		IViewPart viewPart = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().findView(
						ProcessView.ProcessViewId);

		if (viewPart instanceof ProcessView) {

			ProcessView processView = (ProcessView) viewPart;

			if (processView.getViewer().getSelection() instanceof StructuredSelection) {
				
				// Get the selected elements
				
				StructuredSelection selection = (StructuredSelection) processView
						.getViewer().getSelection();
				
				Object[] selectedObjects = selection.toArray();
				
				for(int i = 0; i < selectedObjects.length; i++) {
					if (selectedObjects[i] instanceof TaskDescriptor) { //A task is selected ...
					
						TaskDescriptor td = (TaskDescriptor) selectedObjects[i];
						List<String> cpIds = null;
						if(td instanceof TaskDescriptorCP) {
							TaskDescriptorCP tdcp = (TaskDescriptorCP) td;
							cpIds = tdcp.getCpGuids();
						}
						executeTask(td, cpIds);
					}
					else if(selectedObjects[i] instanceof CapabilityPattern) { // A Capability Pattern is selected ...
						CapabilityPattern cp = (CapabilityPattern) selectedObjects[i];
						List<String> cpIds = null;
						if(cp instanceof CapabilityPatternCP) {
							CapabilityPatternCP cpcp = (CapabilityPatternCP) cp;
							cpIds = cpcp.getCpGuids();
						}
						executeActivity(cp, cpIds);
					}
					else if(selectedObjects[i] instanceof Activity) { // An Activity is selected ...
						
						Activity actv = (Activity) selectedObjects[i];
						List<String> cpIds = null;
						if(actv instanceof ActivityCP) {
							ActivityCP actvcp = (ActivityCP) actv;
							cpIds = actvcp.getCpGuids();
						}
						executeActivity(actv, cpIds);
					}
				}
				
				if(selectedObjects.length > 0) {
					EngineUtil.addSeparator(Context.selectedProject);
				}
				
				// Enable Undo action and refresh the Process view
				
				processView.enableUndoButton();
				processView.refreshViewer();
				
				// Disable Run Action
				
				setEnabled(false);
				
				RunRepeatableAction runRepeatableAction = getRunRepeatableAction();
				if(runRepeatableAction != null) {
					runRepeatableAction.setEnabled(false);
				}
			}
		}
	}
	
	/*
	* This method sets a given task as executed by communicating with the Activiti Engine
	*/
	private boolean executeTask(TaskDescriptor td, List<String> cpIds) {
		
		if(Context.isPerformedByCurrentRole(td)) {
			String activitiTaskId = MethodElements.getActivitiTaskId(td);
			String processInstanceId = "";
			
			if(cpIds != null && cpIds.size() > 0) {
				processInstanceId = Context.getProcessInstance(cpIds);
			}
			else {
				processInstanceId = getProcessInstanceId(td);
			}
			
			if(Engine.isExecutable(activitiTaskId, processInstanceId, Context.selectedProject)) {
			
				Engine.executeTask(activitiTaskId, processInstanceId,
						Context.selectedProject);
				
				return true;
			}
		}
		
		return false;
	}
	
	/*
	* This method executes a given Activity. This execution involves setting as executed
	* all the nested tasks. For executing the nested activities, this method invokes itself
	* recursively.
	*/
	private boolean executeActivity(Activity actv, List<String> cpIds) {
		
		boolean taskExecuted = false;
		boolean result = false;
		List<String> cp_ids = new ArrayList<String>();
		if(cpIds == null) {
			cpIds = new ArrayList<String>();
		}
		cp_ids.addAll(cpIds);
		
		if(actv instanceof CapabilityPattern) {
			CapabilityPattern cp = (CapabilityPattern) actv;
			
			if(cp.getVariabilityType().equals(VariabilityType.EXTENDS)) {
				
				cp_ids.add(cp.getGuid());
				
				CapabilityPattern cp2 = MethodElements.getCapabilityPattern(
						getCapabilityPatternGUId(cp));
				
				actv = cp2;
			}
		}
		
		do {
 			taskExecuted = false;
			
			List<BreakdownElement> elements = actv.getBreakdownElements();
			int size = elements.size();
			
			for(int i = 0; i < size; i++) {
				BreakdownElement be = actv.getBreakdownElements().get(i);
				
				if(be instanceof TaskDescriptor) {
					TaskDescriptor td = (TaskDescriptor) be;
					
					boolean b = executeTask(td, cp_ids);
					
					if(!taskExecuted && b) {
						taskExecuted = true;
						result = true;
					}
				}
				else if(be instanceof Activity) {
					Activity actv2 = (Activity) be;
					
					boolean b = false;
					
					if(actv2 instanceof ActivityCP) {
						ActivityCP actvcp = (ActivityCP) actv2;
						b = executeActivity(actvcp, actvcp.getCpGuids());
					}
					else if(actv2 instanceof CapabilityPatternCP) {
						CapabilityPatternCP cpcp = (CapabilityPatternCP) actv2;
						b = executeActivity(cpcp, cpcp.getCpGuids());
					}
					else {
						b = executeActivity(actv2, cp_ids);
					}
					
					if(!taskExecuted && b) {
						taskExecuted = true;
						result = true;
					}
				}
			}
		}
		while(taskExecuted);
		
		return result;
	}
	
	private String getCapabilityPatternGUId(CapabilityPattern cp) {
		
		if(cp != null) {
			VariabilityElement ve = cp.getVariabilityBasedOnElement();
			if(ve instanceof CapabilityPattern) {
				CapabilityPattern cp2 = (CapabilityPattern) ve;
				if(cp2.eIsProxy()) {
					URI proxyURI = ((InternalEObject)cp2).eProxyURI();
					return proxyURI.host();
				}
			}
		}
		
		return "";
	}
	
	private String getProcessInstanceId(TaskDescriptor td) {
		
		if(td instanceof TaskDescriptorCP) {
			TaskDescriptorCP tdcp = (TaskDescriptorCP) td;
			return Context.getProcessInstance(tdcp.getCpGuids());
		}

		return Context.processInstanceId;
	}
	
	private RunRepeatableAction getRunRepeatableAction() {
		
		IViewPart viewPart = PlatformUI.getWorkbench()
		.getActiveWorkbenchWindow().getActivePage().findView(
				ProcessView.ProcessViewId);

		if (viewPart instanceof ProcessView) {
			ProcessView processView = (ProcessView) viewPart;
			
			for (IContributionItem item : processView.getViewSite()
					.getActionBars().getToolBarManager().getItems()) {
				
				if (item instanceof ActionContributionItem) {
					ActionContributionItem acitem = (ActionContributionItem) item;
					if (acitem.getAction() instanceof RunRepeatableAction) {
						return (RunRepeatableAction) acitem.getAction();
					}
				}
			}
		}
		
		return null;
	}
}
