package moskitt4me.repositoryClient.core.providers;

import java.io.IOException;

import moskitt4me.repositoryClient.core.Activator;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/*
* A label provider for the composite of the "Define Internal Tool Dialog"
*
* @author Mario Cervera
*/
public class PlatformPluginsLabelProvider extends LabelProvider {
	
	@Override
	public Image getImage(Object element) {
		
		try {
			
			String imagePath = FileLocator.toFileURL(Platform.getBundle(
			Activator.PLUGIN_ID).getResource("icons/full/obj16")).getPath();
			
			if(element instanceof String) {
				Image image = new Image(Display.getCurrent(), imagePath + "MethodPlugin.gif");
				return image;
			}
		}
		catch(IOException e) {
			return null;
		}
		
		return super.getImage(element);
	}
	
}

