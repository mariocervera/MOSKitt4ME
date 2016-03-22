package moskitt4me.repositoryClient.core.actions;

import moskitt4me.repositoryClient.core.util.RepositoryLocation;
import moskitt4me.repositoryClient.core.views.RepositoriesView;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

/*
* This action allows the user to delete Repository Locations from the Repositories view.
*
* @author Mario Cervera
*/
public class DiscardLocationAction extends Action {
	
	@Override
	public void run() {
		
		super.run();
		
		boolean result = MessageDialog.openConfirm(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				"Discard location",
				"Are you sure you want to discard the selected repository location?");
		
		if (result) {
			
			IWorkbenchPart part = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage().getActivePart();
			
			if (part instanceof RepositoriesView) {
				RepositoriesView rv = (RepositoriesView) part;
				ISelection selection = rv.getCommonViewer().getSelection();
				if (selection instanceof StructuredSelection) {
					StructuredSelection sel = (StructuredSelection) selection;
					Object selectedobject = sel.getFirstElement();
					if (selectedobject instanceof RepositoryLocation) {
						RepositoryLocation rp = (RepositoryLocation) selectedobject;
						rv.getLocations().remove(rp);
						rv.refreshViewer();
					}
				}
			}
		}
	}

}
