package moskitt4me.projectmanager.core.actions;

import moskitt4me.projectmanager.core.context.Context;
import moskitt4me.projectmanager.core.context.Context.ProcessVisualization;
import moskitt4me.projectmanager.core.views.ProcessView;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

/**
 * An action that is shown as a toggle button in the Action Bar of the Process view. When the button
 * is activated, the Process view displays all the tasks of the process under execution. When the 
 * button is deactivated, it only displays the tasks that are executable in the current state of the
 * process instance.
 *
 * @author Mario Cervera
 */
public class AllTasksAction extends Action implements IAction {
	
	/*
	 * Constructor
	 */
	public AllTasksAction() {
		
		super("", Action.AS_CHECK_BOX);
	}
	
	@Override
	public void run() {
		
		if(this.isChecked()) {
			Context.currentProcessVisualization = ProcessVisualization.ALLTASKS;
		}
		else {
			Context.currentProcessVisualization = ProcessVisualization.NEXTTASKS;
		}
		
		ProcessView pv = getProcessView();
		if(pv != null) {
			pv.refreshViewer();
		}
	}
	
	private ProcessView getProcessView() {
		
		try {
			IViewPart viewPart = PlatformUI.getWorkbench().
			getActiveWorkbenchWindow().getActivePage().findView(ProcessView.ProcessViewId);
			
			if(viewPart instanceof ProcessView) {
				return (ProcessView) viewPart;
			}
		}
		catch(Exception e) {
			return null;
		}
		
		return null;
	}
}
