package moskitt4me.projectmanager.core.providers;

import java.io.IOException;

import moskitt4me.projectmanager.core.Activator;
import moskitt4me.projectmanager.core.context.Context;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.epf.uma.Domain;
import org.eclipse.epf.uma.WorkProduct;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/*
 * LabelProvider for the Product Explorer View
 */
public class ProductsLabelProvider extends LabelProvider {	
	
	@Override
	public String getText(Object element) {
		
		if (element instanceof ProductsItemProvider) {
			String text = "Products";
			
			if(Context.selectedProject != null &&
					Context.selectedProject.getName() != null &&
					!Context.selectedProject.getName().equals("")) {
				
				text += " of project " + Context.selectedProject.getName();
			}
			
			return text;
		}
		else if (element instanceof Domain) {
			Domain dom = (Domain) element;
			return dom.getPresentationName() != null ? dom.getPresentationName() : dom.getName();
		}
		else if(element instanceof WorkProduct) {
			WorkProduct wp = (WorkProduct) element;
			return wp.getPresentationName() != null ? wp.getPresentationName() : wp.getName();
		}
		else if(element instanceof IFile) {
			return ((IFile)element).getName();
		}
		
		return super.getText(element);
	}
	
	@Override
	public Image getImage(Object element) {
		
		try {
			
			String imagePath = FileLocator.toFileURL(Platform.getBundle(
			Activator.PLUGIN_ID).getResource("icons/full/obj16")).getPath();
			
			if (element instanceof ProductsItemProvider) {
				Image image = new Image(Display.getCurrent(), imagePath + "Products.gif");
				return image;
			}
			else if (element instanceof Domain) {
				Image image = new Image(Display.getCurrent(), imagePath + "Domain.gif");
				return image;
			}
			else if(element instanceof WorkProduct) {
				Image image = new Image(Display.getCurrent(), imagePath + "Product.gif");
				return image;
			}
			else if(element instanceof IFile) {
				WorkbenchLabelProvider wlp = new WorkbenchLabelProvider();
				return wlp.getImage(element);
			}
		}
		catch(IOException e) {
			return null;
		}
		
		return super.getImage(element);
	}
	
}
