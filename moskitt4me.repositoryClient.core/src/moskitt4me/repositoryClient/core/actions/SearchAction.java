package moskitt4me.repositoryClient.core.actions;

import moskitt4me.repositoryClient.core.dialogs.SearchDialog;
import moskitt4me.repositoryClient.core.filters.SearchFilter;
import moskitt4me.repositoryClient.core.util.RepositoryClientUtil;
import moskitt4me.repositoryClient.core.views.RepositoriesView;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;

/*
* This action allows the user to filter the content of the Repositories view according
* to a set of criteria.
*
* @author Mario Cervera
*/
public class SearchAction extends Action implements IAction {

	@Override
	public int getStyle() {
		return IAction.AS_CHECK_BOX;
	}
	
	@Override
	public void run() {

		RepositoriesView repositoriesView = RepositoryClientUtil.getRepositoriesView();
		
		if(repositoriesView != null) {
			
			SearchFilter filter = null;
			
			if (this.isChecked()) {
				SearchDialog dialog = new SearchDialog(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());

				if (dialog.open() == Window.OK) {
					String type = dialog.getType();
					String origin = dialog.getOrigin();
					String objective = dialog.getObjective();
					String input = dialog.getInput();
					String output = dialog.getOutput();
					
					filter = new SearchFilter(type, origin, objective, input, output);
				}
				else {
					this.setChecked(false);
				}
			}
			else {
				filter = new SearchFilter();
			}
			
			if(filter != null) {
				repositoriesView.getCommonViewer().setFilters(new ViewerFilter[]{filter});
			}
		}
	}
}
