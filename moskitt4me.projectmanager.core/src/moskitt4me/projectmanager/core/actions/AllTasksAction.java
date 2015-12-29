package moskitt4me.projectmanager.core.actions;

import moskitt4me.projectmanager.core.context.Context;
import moskitt4me.projectmanager.core.context.Context.ProcessVisualization;
import moskitt4me.projectmanager.core.views.ProcessView;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

public class AllTasksAction extends Action implements IAction {
	
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
