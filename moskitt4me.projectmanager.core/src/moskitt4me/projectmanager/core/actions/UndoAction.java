package moskitt4me.projectmanager.core.actions;

import java.io.File;
import java.util.List;
import java.util.Map;

import moskitt4me.projectmanager.core.context.Context;
import moskitt4me.projectmanager.core.views.ProcessView;
import moskitt4me.projectmanager.methodspecification.MethodElements;
import moskitt4me.projectmanager.processsupport.Engine;
import moskitt4me.projectmanager.processsupport.util.EngineUtil;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

/**
 * The Undo Action (which is shown as a button in the Action Bar of the Process view) allows the
 * user to go back in the process execution. When the Undo Action is invoked, the last tasks that
 * were set as "executed" are set as "non-executed"; thus, the process instance returns to its
 * previous state.
 *
 * @author Mario Cervera
 */
public class UndoAction extends Action implements IAction {
	
	/*
	 * Constructor
	 */
	public UndoAction() {
		
		// Initially, the Undo Action is disabled
		
		setEnabled(false);
	}
	
	@Override
	public void run() {

		try {
			IViewPart viewPart = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage().findView(
							ProcessView.ProcessViewId);

			if (viewPart instanceof ProcessView) {

				ProcessView processView = (ProcessView) viewPart;

				//Delete current process instance
				
				if(Context.processInstanceId != null &&
						!Context.processInstanceId.equals("")) {
					
					Engine.deleteProcessInstance(Context.processInstanceId,
							Context.selectedProject);
				}

				//Delete "startedPatterns.txt" file
				
				String dir = Context.selectedProject.getLocation().toString() + "/.method/";
				String startedPatternsFileName = "startedPatterns.txt";
				File f = new File(dir + startedPatternsFileName);
				if(f.exists()) f.delete();
				
				//Delete "patternInstances.txt" file
				
				String patternInstancesFileName = "patternInstances.txt";
				File f2 = new File(dir + patternInstancesFileName);
				if(f2.exists()) f2.delete();
				
				//Create a new process instance
				
				String newProcessInstanceId = Engine.createProcessInstance(MethodElements.processKey,
						Context.selectedProject);
				
				if(newProcessInstanceId != null && !newProcessInstanceId.equals("")) {
					Context.processInstanceId = newProcessInstanceId;
				}
				
				//Get the executed tasks
				
				List<Map<String, List<List<String>>>> executedTasks = EngineUtil
						.getExecutedTaskIds(Context.selectedProject);
				
				//Delete the "executedTasks.txt" file
				
				String executedTasksFileName = "executedTasks.txt";
				File f3 = new File(dir + executedTasksFileName);
				if(f3.exists()) f3.delete();
				

				//Execute the tasks. Do no execute the tasks that must be undone.
				
				for (int i = 0; i < executedTasks.size() - 1; i++) {
					Map<String, List<List<String>>> map = executedTasks.get(i);
					
					boolean executed = false;
					
					do {
						executed = false;
						for(String taskId : map.keySet()) {
							List<List<String>> capabilityPatterns = map.get(taskId);
							
							for(List<String> cpIds : capabilityPatterns) {
								String processInstanceId = "";
								
								if(cpIds.size() <= 0 ||
										(cpIds.size() == 1 && cpIds.get(0).equals("dp"))) {
									processInstanceId = Context.processInstanceId;
								}
								else {
									processInstanceId = Context.getProcessInstance(cpIds);
								}
								
								if (Engine.isExecutable(taskId, 
										processInstanceId, Context.selectedProject)) {
									
									Engine.executeTask(taskId, processInstanceId,
											Context.selectedProject);
									
									executed = true;
								}
							}
						}
					}
					while(executed);
					
					if(map.keySet().size() > 0) {
						EngineUtil.addSeparator(Context.selectedProject);
					}
				}

				//Refresh
				
				processView.enableUndoButton();
				processView.refreshViewer();
			}
		}
		catch (Exception e) {
			MessageDialog.openError(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					"Error", "An error occurred during undo action. \n" + e.getMessage());
		}
	}
}
