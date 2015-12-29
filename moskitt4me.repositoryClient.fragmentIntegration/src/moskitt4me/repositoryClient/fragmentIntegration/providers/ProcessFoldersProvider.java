package moskitt4me.repositoryClient.fragmentIntegration.providers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.epf.uma.ContentPackage;
import org.eclipse.epf.uma.MethodPackage;
import org.eclipse.epf.uma.MethodPlugin;
import org.eclipse.epf.uma.ProcessComponent;
import org.eclipse.epf.uma.ProcessPackage;

/**Use: Integration of conceptual fragments of process type. 
 * On second page of the wizard, selection of the folder.
 */
public class ProcessFoldersProvider extends AdapterFactoryContentProvider {
	
	public ProcessFoldersProvider(AdapterFactory adapterFactory) {
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
		if (element instanceof MethodPlugin || element instanceof ProcessPackage) {
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
					for(MethodPackage mp2 : mp.getChildPackages()){
						if(mp2.getName().equals("CapabilityPatterns") && mp2 instanceof ProcessPackage) 
							result.add(mp2);
					}
				}
			}
		}
		if(parentElement instanceof ProcessPackage){
			ProcessPackage packg = (ProcessPackage) parentElement;
			for(MethodPackage m : packg.getChildPackages()){
				if(!(m instanceof ProcessComponent)) result.add(m);
			}
		}
		return result.toArray();
	}
}