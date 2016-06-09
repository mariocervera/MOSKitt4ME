package moskitt4me.repositoryClient.core.providers;

import java.io.IOException;

import moskitt4me.repositoryClient.core.Activator;
import moskitt4me.repositoryClient.core.util.RepositoryClientUtil;
import moskitt4me.repositoryClient.core.util.RepositoryLocation;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * Provides labels and icons for the graphical elements of the Repositories View.
 *
 * @author Mario Cervera
 */
public class RepositoriesLabelProvider extends LabelProvider {

	/*
	 * This method provides a label for the given element
	 */
	@Override
	public String getText(Object element) {
		
		if(element instanceof RepositoryLocation) {
			return getRepositoryLocationLabel(((RepositoryLocation)element));
		}
		else if(element instanceof MethodFragmentItemProvider) {
			return removeZipExtension(((MethodFragmentItemProvider) element).getFileName());
		}
		else if(element instanceof MethodFragmentPropertyItemProvider) {
			MethodFragmentPropertyItemProvider property = (MethodFragmentPropertyItemProvider)element;
			
			if(property.getPropertyName().equals("Descriptor") ||
					property.getPropertyName().equals("Interface")) {
				
				return property.getPropertyName();
			}
			else {
				return property.getPropertyName() + ": " + property.getPropertyValue();
			}
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
			
			if(element instanceof RepositoryLocation) {
				Image image = new Image(Display.getCurrent(), imagePath + "Repositories.gif");
				return image;
			}
			else if(element instanceof MethodFragmentItemProvider) {
				MethodFragmentItemProvider fragment = (MethodFragmentItemProvider) element;
				Image image = null;
				
				if(RepositoryClientUtil.isTechnicalFragment(fragment.getType())) {
					image = new Image(Display.getCurrent(), imagePath + "TechnicalFragment.gif");
				}
				else {
					image = new Image(Display.getCurrent(), imagePath + "ConceptualFragment.gif");
				}
				
				return image;
			}
			else if(element instanceof MethodFragmentPropertyItemProvider) {
				MethodFragmentPropertyItemProvider property = (MethodFragmentPropertyItemProvider) element;
				
				Image image = new Image(Display.getCurrent(), imagePath + property.getPropertyName() + ".gif");
				return image;
			}
		}
		catch(IOException e) {
			return null;
		}
		
		return super.getImage(element);
	}
	
	private String getRepositoryLocationLabel(RepositoryLocation location) {
		
		String host = location.getHost() == null? "" : location.getHost();
		String repositoryPath = location.getRepositoryPath() == null? "" : location.getRepositoryPath();
		String user = location.getUser() == null? "" : location.getUser();
		
		return user + "@" + host + ":" + repositoryPath;
	}
	
	private String removeZipExtension(String fileName) {
		
		return fileName.substring(0, fileName.length() - 4);
	}
}
