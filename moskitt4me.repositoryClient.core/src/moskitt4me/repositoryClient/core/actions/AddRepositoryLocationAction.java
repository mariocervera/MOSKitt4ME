package moskitt4me.repositoryClient.core.actions;

import java.util.List;

import moskitt4me.repositoryClient.core.dialogs.AddRepositoryLocationDialog;
import moskitt4me.repositoryClient.core.util.RepositoryClientUtil;
import moskitt4me.repositoryClient.core.util.RepositoryLocation;
import moskitt4me.repositoryClient.core.views.RepositoriesView;

import org.apache.commons.net.ftp.FTPClient;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;

/*
* This action allows the user to add a new repository location to the Repositories view. A
* repository location is a graphical element that represents a FTP repository that contains
* reusable assets.
*
* To create a new repository location, the user needs to specify host, repository path, username,
* and password.
*/
public class AddRepositoryLocationAction extends Action implements IAction {

	@Override
	public void run() {
		
		RepositoriesView repositoriesView = RepositoryClientUtil.getRepositoriesView();
		
		if(repositoriesView != null) {
			
			AddRepositoryLocationDialog dialog = new AddRepositoryLocationDialog(
					repositoriesView.getSite().getShell());
			
			if(dialog.open() == Window.OK) {
				String host = dialog.getHost();
				String repositoryPath = dialog.getRepositoryPath();
				String user = dialog.getUser();
				String password = dialog.getPassword();
				
				if(existingLocation(repositoriesView, host, repositoryPath)) {
					MessageDialog.openError(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							"Connection failed",
							"The repository location already exists");
				}
				else {
					RepositoryLocation location = new RepositoryLocation(
							host, repositoryPath, user, password);
					
					RepositoryClientUtil.setInitialLocationType(location);
					
					FTPClient client = RepositoryClientUtil.connect(location, true);
					
					if(client != null) {
						RepositoryClientUtil.disconnect(client);
						repositoriesView.getLocations().add(location);
						repositoriesView.getCommonViewer().setInput(repositoriesView.getLocations());
					}
				}
			}
		}
	}
	
	private boolean existingLocation(RepositoriesView repositoriesView,
			String host, String repositoryPath) {
		
		List<RepositoryLocation> locations = repositoriesView.getLocations();
		
		for(RepositoryLocation location : locations) {
			if(location.getHost().equals(host) &&
					location.getRepositoryPath().equals(repositoryPath)) {
				
				return true;
			}
		}
		return false;
	}
}
