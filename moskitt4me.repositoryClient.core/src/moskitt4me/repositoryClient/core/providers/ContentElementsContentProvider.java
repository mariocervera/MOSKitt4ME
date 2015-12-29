package moskitt4me.repositoryClient.core.providers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.epf.uma.ContentElement;
import org.eclipse.epf.uma.ContentPackage;
import org.eclipse.epf.uma.MethodPackage;
import org.eclipse.epf.uma.MethodPlugin;
import org.eclipse.epf.uma.Task;
import org.eclipse.epf.uma.WorkProduct;

public class ContentElementsContentProvider extends AdapterFactoryContentProvider {
	
	public ContentElementsContentProvider(AdapterFactory adapterFactory) {
		
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
		
		if (element instanceof MethodPlugin || element instanceof MethodPackage
				|| element instanceof TasksItemProvider
				|| element instanceof WorkProductsItemProvider) {
			
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
			
			result.add(new TasksItemProvider(null, cpackage));
			result.add(new WorkProductsItemProvider(null, cpackage));
		}
		else if(parentElement instanceof TasksItemProvider) {
			TasksItemProvider tip = (TasksItemProvider) parentElement;
			
			if(tip.getAssociatedPackage() instanceof ContentPackage) {
				ContentPackage cp = (ContentPackage) tip.getAssociatedPackage();
				
				for(ContentElement element : cp.getContentElements()) {
					if(element instanceof Task) {
						result.add(element);
					}
				}
			}
		}
		else if(parentElement instanceof WorkProductsItemProvider) {
			WorkProductsItemProvider wpip = (WorkProductsItemProvider) parentElement;
			
			if(wpip.getAssociatedPackage() instanceof ContentPackage) {
				ContentPackage cp = (ContentPackage) wpip.getAssociatedPackage();
				
				for(ContentElement element : cp.getContentElements()) {
					if(element instanceof WorkProduct) {
						result.add(element);
					}
				}
			}
		}
		
		return result.toArray();
	}

}
