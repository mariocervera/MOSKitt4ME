package moskitt4me.repositoryClient.fragmentIntegration.providers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.epf.uma.ContentPackage;
import org.eclipse.epf.uma.MethodPackage;
import org.eclipse.epf.uma.MethodPlugin;
import org.eclipse.epf.uma.ProcessComponent;
import org.eclipse.epf.uma.ProcessPackage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

public class ProcessElementsForConceptualFragmentsProvider extends ArrayContentProvider   implements ITreeContentProvider{

	/**Provide Patterns on the plugin
	 * Use: Conceptual fragment creation (Process type)
	 * @param elements 
	 * */
	List<Object> elements ;
	
	public ProcessElementsForConceptualFragmentsProvider(List<Object> elements ) {
		super();
		this.elements = elements;
	}
	
	
	public Object[] getElements(Object inputElement) {
		if(inputElement instanceof Object[]) {
			Object[] elements = (Object[]) inputElement;
			return elements;
		}
		return super.getElements(inputElement);
	}
	
	public boolean hasChildren(Object element) {
		
		if(element instanceof ProcessComponent) {
			return false;
		}
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
						if(mp2.getName().equals("CapabilityPatterns") && mp2 instanceof ProcessPackage) {
							for(MethodPackage mp3 : mp2.getChildPackages()) {
								if(!this.elements.contains(mp3)) {
									result.add(mp3);	
								}
							}
						}
					}
				}
			}
		}
		else if(parentElement instanceof  ProcessPackage) {
			ProcessPackage pack= (ProcessPackage) parentElement;
			for(MethodPackage childpackage : pack.getChildPackages()) {
				if(!this.elements.contains(childpackage)) {
					result.add(childpackage);	
				}
			}
		}
		
		return result.toArray();
	}

	public Object getParent(Object element) {
		return null;
	}

}
