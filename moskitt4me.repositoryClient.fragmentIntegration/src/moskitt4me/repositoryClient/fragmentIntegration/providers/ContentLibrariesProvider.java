package moskitt4me.repositoryClient.fragmentIntegration.providers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.epf.uma.ContentPackage;
import org.eclipse.epf.uma.MethodPackage;
import org.eclipse.epf.uma.MethodPlugin;

public class ContentLibrariesProvider extends AdapterFactoryContentProvider {
	
	/**Provide the content libraries
	 * Use: Integration of conceptual fragments of content type
	 * */
	
	public ContentLibrariesProvider(AdapterFactory adapterFactory) {
		super(adapterFactory);
	}
	
	public Object[] getElements(Object inputElement) {
		if(inputElement instanceof Object[]) {
			Object[] elements = (Object[]) inputElement;
			return elements;
		}
		return super.getElements(inputElement);
	}
	
	public boolean hasChildren(Object element) {
		if (element instanceof MethodPlugin || element instanceof MethodPackage) {
			return true;
		}
		return false;
	}
	
	public Object[] getChildren(Object parentElement) {
		List<Object> result = new ArrayList<Object>();
		if(parentElement instanceof  MethodPlugin) {
			MethodPlugin plugin = (MethodPlugin) parentElement;
			for(MethodPackage mp : plugin.getMethodPackages()) {
				if(mp.getName().equals("Content") && mp instanceof ContentPackage) {
					for(MethodPackage mp2 : mp.getChildPackages()) {
						if(mp2.getName().equals("CoreContent") && mp2 instanceof ContentPackage) {
							result.addAll(mp2.getChildPackages());
						}
					}
				}
			}
		}
		else if(parentElement instanceof ContentPackage) {
			ContentPackage cpackage = (ContentPackage) parentElement;
			result.addAll(cpackage.getChildPackages());
		}	
		
		return result.toArray();
	}

}
