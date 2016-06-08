package moskitt4me.repositoryClient.core.providers;

import java.io.IOException;

import moskitt4me.repositoryClient.core.Activator;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.epf.uma.MethodPackage;
import org.eclipse.epf.uma.MethodPlugin;
import org.eclipse.epf.uma.Task;
import org.eclipse.epf.uma.WorkProduct;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * Provides labels and icons for the elements of the "Content Element Selection Dialog".
 *
 * @author Mario Cervera
 */
public class ContentElementsLabelProvider extends LabelProvider {

	/*
	 * This method provides a label for the given element
	 */
	@Override
	public String getText(Object element) {
		
		if(element instanceof MethodPlugin) {
			return ((MethodPlugin) element).getName();
		}
		else if(element instanceof MethodPackage) {
			return ((MethodPackage) element).getName();
		}
		else if(element instanceof TasksItemProvider) {
			return "Tasks";
		}
		else if(element instanceof WorkProductsItemProvider) {
			return "Work Products";
		}
		else if(element instanceof Task) {
			return ((Task) element).getName();
		}
		else if(element instanceof WorkProduct) {
			return ((WorkProduct) element).getName();
		}
		
		return super.getText(element);
	}
	
	/*
	 * This method provides an icon for the given element
	 */
	@Override
	public Image getImage(Object element) {
		
		try {
			
			String imagePath = FileLocator.toFileURL(Platform.getBundle(
			Activator.PLUGIN_ID).getResource("icons/full/obj16")).getPath();
			
			if(element instanceof MethodPlugin) {
				Image image = new Image(Display.getCurrent(), imagePath + "MethodPlugin.gif");
				return image;
			}
			else if(element instanceof MethodPackage) {
				Image image = new Image(Display.getCurrent(), imagePath + "Package.gif");
				return image;
			}
			else if(element instanceof TasksItemProvider) {
				Image image = new Image(Display.getCurrent(), imagePath + "Tasks.gif");
				return image;
			}
			else if(element instanceof WorkProductsItemProvider) {
				Image image = new Image(Display.getCurrent(), imagePath + "WorkProducts.gif");
				return image;
			}
			else if(element instanceof Task) {
				Image image = new Image(Display.getCurrent(), imagePath + "Task.gif");
				return image;
			}
			else if(element instanceof WorkProduct) {
				Image image = new Image(Display.getCurrent(), imagePath + "WorkProduct.gif");
				return image;
			}
		}
		catch(IOException e) {
			return null;
		}
		
		return super.getImage(element);
	}
}
