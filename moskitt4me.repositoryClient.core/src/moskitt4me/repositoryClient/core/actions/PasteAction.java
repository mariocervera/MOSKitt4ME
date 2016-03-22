package moskitt4me.repositoryClient.core.actions;

import moskitt4me.repositoryClient.core.Activator;
import moskitt4me.repositoryClient.core.dialogs.CreateTechnicalFragmentDialog;
import moskitt4me.repositoryClient.core.util.TechnicalFragment;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/*
* A paste action that can be used within the Create Technical Fragment Dialog.
*
* @author Mario Cervera
*/
public class PasteAction extends Action {

	private CreateTechnicalFragmentDialog dialog;
	
	public PasteAction(CreateTechnicalFragmentDialog dialog) {
		
		this.dialog = dialog;
		
		setText("Paste");
		
		try {
			String imagePath = FileLocator.toFileURL(Platform.getBundle(
					Activator.PLUGIN_ID).getResource("icons/full/elcl16")).getPath();
			Image image = new Image(Display.getCurrent(), imagePath + "paste.gif");
			ImageDescriptor desc = ImageDescriptor.createFromImage(image);
			setImageDescriptor(desc);
		}
		catch(Exception e) {
			
		}
	}
	
	@Override
	public void run() {
		
		super.run();
		
		if(dialog != null && CreateTechnicalFragmentDialog.clipboard != null) {
			TechnicalFragment selectedTF = dialog.getSelectedTechnicalFragment();
			if(selectedTF != null) {
				TechnicalFragment clone = CreateTechnicalFragmentDialog.clipboard.duplicate();
				clone.setParent(selectedTF);
				selectedTF.getDependencies().add(clone);
				TechnicalFragment root = dialog.getRootFragment();
				dialog.resolveTechnicalFragment(root);
				dialog.getDependenciesTreeViewer().refresh();
				dialog.enableOkButton();
			}			
		}
	}
}
