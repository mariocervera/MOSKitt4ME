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
* An action that allows the user to copy technical fragments in the "CreateTechnicalFragmentDialog"
*/
public class CopyAction extends Action {

	private TechnicalFragment technicalFragment;
	
	public CopyAction(TechnicalFragment technicalFragment) {
		
		this.technicalFragment = technicalFragment;
		
		setText("Copy");
		
		try {
			String imagePath = FileLocator.toFileURL(Platform.getBundle(
					Activator.PLUGIN_ID).getResource("icons/full/elcl16")).getPath();
			Image image = new Image(Display.getCurrent(), imagePath + "copy.gif");
			ImageDescriptor desc = ImageDescriptor.createFromImage(image);
			setImageDescriptor(desc);
		}
		catch(Exception e) {
			
		}
	}
	
	@Override
	public void run() {
		
		super.run();
		
		CreateTechnicalFragmentDialog.clipboard = technicalFragment;
	}
}
