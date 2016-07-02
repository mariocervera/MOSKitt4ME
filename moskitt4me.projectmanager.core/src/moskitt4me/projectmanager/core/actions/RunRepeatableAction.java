package moskitt4me.projectmanager.core.actions;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import moskitt4me.projectmanager.core.context.Context;
import moskitt4me.projectmanager.core.providers.ActivityCP;
import moskitt4me.projectmanager.core.providers.CapabilityPatternCP;
import moskitt4me.projectmanager.core.providers.TaskDescriptorCP;
import moskitt4me.projectmanager.core.views.ProcessView;
import moskitt4me.projectmanager.methodspecification.MethodElements;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.epf.uma.Activity;
import org.eclipse.epf.uma.BreakdownElement;
import org.eclipse.epf.uma.CapabilityPattern;
import org.eclipse.epf.uma.TaskDescriptor;
import org.eclipse.epf.uma.VariabilityElement;
import org.eclipse.epf.uma.VariabilityType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

/**
 * The Run Repeatable Action (which is shown as a button in the Action Bar of the Process view) allows the
 * user to execute a Task that is defined as repetable in the method model. Unlike the Run Action, the task
 * will not be marked as "executed"; therefore, it will be possible to execute the task more than once.
 *
 * @author Mario Cervera
 */
public class RunRepeatableAction extends Action implements IAction {

	/*
	 * Constructor
	 */
	public RunRepeatableAction() {

		// The action is initially disabled
		// It is enabled when an executable (and repeatable) task is selected
		
		setEnabled(false);
		setId("RunRepeatableAction");
	}
	
	@Override
	public void run() {

		// Get the Process view
		
		IViewPart viewPart = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().findView(
						ProcessView.ProcessViewId);

		if (viewPart instanceof ProcessView) {

			ProcessView processView = (ProcessView) viewPart;

			// Get the selected elements
			
			if (processView.getViewer().getSelection() instanceof StructuredSelection) {
				
				StructuredSelection selection = (StructuredSelection) processView
						.getViewer().getSelection();
				
				Object[] selectedObjects = selection.toArray();
				
				// For each of the selected elements ... execute the element
				// and set the action as disabled
				
				for(int i = 0; i < selectedObjects.length; i++) {
					if (selectedObjects[i] instanceof TaskDescriptor) {
					
						TaskDescriptor td = (TaskDescriptor) selectedObjects[i];
						List<String> cpIds = null;
						if(td instanceof TaskDescriptorCP) {
							TaskDescriptorCP tdcp = (TaskDescriptorCP) td;
							cpIds = tdcp.getCpGuids();
						}
						executeRepeatableTask(td, cpIds);
					}
					else if(selectedObjects[i] instanceof CapabilityPattern) {
						CapabilityPattern cp = (CapabilityPattern) selectedObjects[i];
						List<String> cpIds = null;
						if(cp instanceof CapabilityPatternCP) {
							CapabilityPatternCP cpcp = (CapabilityPatternCP) cp;
							cpIds = cpcp.getCpGuids();
						}
						executeRepeatableActivity(cp, cpIds);
					}
					else if(selectedObjects[i] instanceof Activity) {
						
						Activity actv = (Activity) selectedObjects[i];
						List<String> cpIds = null;
						if(actv instanceof ActivityCP) {
							ActivityCP actvcp = (ActivityCP) actv;
							cpIds = actvcp.getCpGuids();
						}
						executeRepeatableActivity(actv, cpIds);
					}
				}
				
				setEnabled(false);
			}
		}
	}
	
	private void executeRepeatableTask(TaskDescriptor td, List<String> cpIds) {
		
		if(Context.isPerformedByCurrentRole(td)) {
			storeRestartedRepeatableTask(cpIds, td.getGuid());
		}
	}
	
	private void executeRepeatableActivity(Activity actv, List<String> cpIds) {
		
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
			
		List<BreakdownElement> elements = actv.getBreakdownElements();
		int size = elements.size();
		
		for(int i = 0; i < size; i++) {
			BreakdownElement be = actv.getBreakdownElements().get(i);
			
			if(be instanceof TaskDescriptor) {
				TaskDescriptor td = (TaskDescriptor) be;
				
				if(td.getIsRepeatable()) {
					executeRepeatableTask(td, cp_ids);
				}
			}
			else if(be instanceof Activity) {
				Activity actv2 = (Activity) be;
				
				if(actv2 instanceof ActivityCP) {
					ActivityCP actvcp = (ActivityCP) actv2;
					executeRepeatableActivity(actvcp, actvcp.getCpGuids());
				}
				else if(actv2 instanceof CapabilityPatternCP) {
					CapabilityPatternCP cpcp = (CapabilityPatternCP) actv2;
					executeRepeatableActivity(cpcp, cpcp.getCpGuids());
				}
				else {
					executeRepeatableActivity(actv2, cp_ids);
				}
			}
		}
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
	
	private void storeRestartedRepeatableTask(List<String> cpIds, String taskId) {
		
		String dir = Context.selectedProject.getLocation().toString() + "/.method/";
		String restartedTasksFileName = "restartedTasks.txt";
		
		if(cpIds == null) {
			cpIds = new ArrayList<String>();
			cpIds.add("dp");
		}
		
		try {
			FileWriter fstream = new FileWriter(dir + restartedTasksFileName, true);
			BufferedWriter out = new BufferedWriter(fstream);
			
			String ids = "";
			for(String id : cpIds) {
				ids += id + " ";
			}
			
			out.write(ids + taskId);
			out.newLine();
			
			out.close();
			
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
}
