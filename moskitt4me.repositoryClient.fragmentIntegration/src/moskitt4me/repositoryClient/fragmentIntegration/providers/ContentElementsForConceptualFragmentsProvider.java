package moskitt4me.repositoryClient.fragmentIntegration.providers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.epf.uma.ContentElement;
import org.eclipse.epf.uma.ContentPackage;
import org.eclipse.epf.uma.MethodPackage;
import org.eclipse.epf.uma.MethodPlugin;
import org.eclipse.epf.uma.Role;
import org.eclipse.epf.uma.Task;
import org.eclipse.epf.uma.WorkProduct;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

public class ContentElementsForConceptualFragmentsProvider extends ArrayContentProvider   implements ITreeContentProvider{

	/**Provide Tasks, Roles and WorkProducts in the plugin content libraries
	 * Use: Conceptual fragment creation (Task, Role, Work Product and Content element types)
	 * @param type 
	 * */
	private String type;
	private List<Object> elements;
	
	/**
	 * These maps store the item providers so that they are not created more than once. It fixes the refresh problem
	 */
	private static Map<MethodPackage, TasksItemProvider> tproviders = new HashMap<MethodPackage, TasksItemProvider>();
	private static Map<MethodPackage, RolesItemProvider> rproviders = new HashMap<MethodPackage, RolesItemProvider>();
	private static Map<MethodPackage, WorkProductsItemProvider> wpproviders = new HashMap<MethodPackage, WorkProductsItemProvider>();
	
	public ContentElementsForConceptualFragmentsProvider(String type, List<Object> elements) {
		super();
		this.type = type;
		this.setElements(elements);
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
				|| element instanceof WorkProductsItemProvider
				|| element instanceof RolesItemProvider) {
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
			
			if(type.equals("Content Element")){
				result.add(getRolesItemProvider(cpackage));
				result.add(getTasksItemProvider(cpackage));
				result.add(getWorkProductsItemProvider(cpackage));
			}
			else if(type.equals("Role")){
				result.add(getRolesItemProvider(cpackage));
			}
			else if(type.equals("Task")) {
				result.add(getTasksItemProvider(cpackage));
			}
			else if(type.equals("Work Product")){
				result.add(getWorkProductsItemProvider(cpackage));
			}
			
		}
		else if(parentElement instanceof TasksItemProvider) {
			TasksItemProvider tip = (TasksItemProvider) parentElement;
			if(tip.getAssociatedPackage() instanceof ContentPackage) {
				ContentPackage cp = (ContentPackage) tip.getAssociatedPackage();
				for(ContentElement element : cp.getContentElements()) {
					if(element instanceof Task && !this.elements.contains(element)) {
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
					if(element instanceof WorkProduct  && !this.elements.contains(element)) {
						result.add(element);
					}
				}
			}
		}
		else if(parentElement instanceof RolesItemProvider) {
			RolesItemProvider rip = (RolesItemProvider) parentElement;
			if(rip.getAssociatedPackage() instanceof ContentPackage) {
				ContentPackage cp = (ContentPackage) rip.getAssociatedPackage();
				for(ContentElement element : cp.getContentElements()) {
					if(element instanceof Role  && !this.elements.contains(element)) {
						result.add(element);
					}
				}
			}
		}
		return result.toArray();
	}

	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setElements(List<Object> elements) {
		this.elements = elements;
	}

	public List<Object> getElements() {
		return elements;
	}

	private TasksItemProvider getTasksItemProvider(MethodPackage associatedPackage) {
		if (this.tproviders.get(associatedPackage) == null) {
			TasksItemProvider tip = new TasksItemProvider(null, associatedPackage);
			this.tproviders.put(associatedPackage, tip);
		}
		
		return this.tproviders.get(associatedPackage);
	}
	
	private RolesItemProvider getRolesItemProvider(MethodPackage associatedPackage) {
		if (this.rproviders.get(associatedPackage) == null) {
			RolesItemProvider rip = new RolesItemProvider(null, associatedPackage);
			this.rproviders.put(associatedPackage, rip);
		}
		
		return this.rproviders.get(associatedPackage);
	}
	
	private WorkProductsItemProvider getWorkProductsItemProvider(MethodPackage associatedPackage) {
		if (this.wpproviders.get(associatedPackage) == null) {
			WorkProductsItemProvider wpip = new WorkProductsItemProvider(null, associatedPackage);
			this.wpproviders.put(associatedPackage, wpip);
		}
		
		return this.wpproviders.get(associatedPackage);
	}
}
