package moskitt4me.repositoryClient.fragmentIntegration.providers;

import java.io.IOException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.epf.uma.CapabilityPattern;
import org.eclipse.epf.uma.DeliveryProcess;
import org.eclipse.epf.uma.MethodPackage;
import org.eclipse.epf.uma.MethodPlugin;
import org.eclipse.epf.uma.ProcessComponent;
import org.eclipse.epf.uma.ProcessElement;
import org.eclipse.epf.uma.ProcessPackage;
import org.eclipse.epf.uma.Role;
import org.eclipse.epf.uma.Task;
import org.eclipse.epf.uma.WorkProduct;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class ElementsLabelProvider extends LabelProvider {

	/**Provide the label and icon for each element on the tree of Creation dialog*/
	
	@Override
	public String getText(Object element) {
		
		if(element instanceof MethodPlugin) {
			return ((MethodPlugin) element).getName();
		}
		else if(element instanceof ProcessComponent) {
			return ((ProcessComponent) element).getName();
		}
		else if(element instanceof ProcessPackage) {
			return ((ProcessPackage) element).getName();
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
		else if(element instanceof RolesItemProvider) {
			return "Roles";
		}
		else if(element instanceof Task) {
			return ((Task) element).getName();
		}
		else if(element instanceof WorkProduct) {
			return ((WorkProduct) element).getName();
		}
		else if(element instanceof Role) {
			return ((Role) element).getName();
		}
		return super.getText(element);
	}
	
	@Override
	public Image getImage(Object element) {
		
		try {
			String imagePath = FileLocator.toFileURL(Platform.getBundle(
					"moskitt4me.repositoryClient.core").getResource("icons/full/obj16")).getPath();
			if(element instanceof MethodPlugin) {
				Image image = new Image(Display.getCurrent(), imagePath + "MethodPlugin.gif");
				return image;
			}else if(element instanceof ProcessComponent && ((ProcessComponent)element).getProcess() instanceof CapabilityPattern) {
				Image image = new Image(Display.getCurrent(), imagePath + "CapabilityPattern.gif");
				return image;
			}else if(element instanceof ProcessComponent && ((ProcessComponent)element).getProcess() instanceof DeliveryProcess) {
				Image image = new Image(Display.getCurrent(), imagePath + "DeliveryProcess.gif");
				return image;
			}
			else if(element instanceof ProcessPackage) {
					if(!((ProcessPackage) element).getProcessElements().isEmpty()){
						for(ProcessElement pe : ((ProcessPackage) element).getProcessElements()){
							if(pe instanceof CapabilityPattern){
								Image image = new Image(Display.getCurrent(), imagePath + "CapabilityPattern.gif");
								return image;
							}
						}
					Image image = new Image(Display.getCurrent(), imagePath + "activity16.gif");
					return image;
					}
				Image image = new Image(Display.getCurrent(), imagePath + "CapabilityPatterns.gif");
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
			else if(element instanceof RolesItemProvider) {
				Image image = new Image(Display.getCurrent(), imagePath + "RoleSetCategory.gif");
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
			else if(element instanceof Role) {
				Image image = new Image(Display.getCurrent(), imagePath + "role.gif");
				return image;
			}
		}
		catch(IOException e) {
			return null;
		}
		return super.getImage(element);
	}
}
