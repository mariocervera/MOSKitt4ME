package moskitt4me.repositoryClient.fragmentIntegration.providers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.epf.uma.MethodPackage;
import org.eclipse.epf.uma.MethodPlugin;
import org.eclipse.epf.uma.ProcessComponent;
import org.eclipse.epf.uma.ProcessPackage;


/**Use: Integration of conceptual fragments of process type. 
 * On first page of the wizard, selection of the process element.
 */

public class ProcessElementsProvider extends AdapterFactoryContentProvider {
	
	public ProcessElementsProvider(AdapterFactory adapterFactory) {
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
				if(mp.getName().equals("DeliveryProcesses") && mp instanceof ProcessPackage) {
					result.addAll(mp.getChildPackages());
				}
			}
		}
		else if(parentElement instanceof ProcessComponent) {
			result.addAll(((ProcessComponent)parentElement).getChildPackages());	
		}
		else if(parentElement instanceof ProcessPackage) {
			result.addAll(((ProcessPackage) parentElement).getChildPackages());		
		}
		return result.toArray();
	}
}
