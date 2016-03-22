package moskitt4me.repositoryClient.core.actions;

import moskitt4me.repositoryClient.core.util.RepositoryClientUtil;
import moskitt4me.repositoryClient.core.views.RepositoriesView;

import org.eclipse.jface.action.Action;

/*
* A refresh action for the Repositories view.
*
* @author Mario Cervera
*/
public class RefreshAction extends Action {
	
	@Override
	public void run() {
		
		super.run();
		
		RepositoriesView repositoriesView = RepositoryClientUtil.getRepositoriesView();
		
		if(repositoriesView != null) {
			repositoriesView.refreshViewer();
		}
	}

}
