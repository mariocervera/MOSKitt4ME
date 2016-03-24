package moskitt4me.projectmanager.core.filters;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

import moskitt4me.projectmanager.core.context.Context;
import moskitt4me.projectmanager.core.providers.UncategorizedDomain;
import moskitt4me.projectmanager.methodspecification.MethodElements;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.epf.uma.Domain;
import org.eclipse.epf.uma.RoleDescriptor;
import org.eclipse.epf.uma.TaskDescriptor;
import org.eclipse.epf.uma.WorkProduct;
import org.eclipse.epf.uma.WorkProductDescriptor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;


/**
 * This class filters the domains that don't have products to be shown
 * in the Product Explorer view.
 * 
 * @author Mario Cervera
 */
public class DomainsFilter extends ViewerFilter {

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		
		if(element instanceof Domain) {
			Domain domain = (Domain) element;
			
			if(!containsProductsToShow(domain)) {
				return false;
			}
		}
		else if(element instanceof WorkProduct) {
			WorkProduct product = (WorkProduct) element;
			
			if(!containsFiles(product) || !assignedToCurrentRoles(product)) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean containsProductsToShow(Domain domain) {
		
		try {
			List<WorkProduct> products = null;
			
			if(domain instanceof UncategorizedDomain) {
				products = MethodElements.getUncategorizedWorkProducts();
			}
			else {
				products = domain.getWorkProducts();
			}
			
			if(products != null && products.size() > 0) {
				for(WorkProduct product : products) {
					if(containsFiles(product) && assignedToCurrentRoles(product)) {
						return true;
					}
				}
			}
			
			if(!(domain instanceof UncategorizedDomain)) {
				for(Domain d : domain.getSubdomains()) {
					if(containsProductsToShow(d)) {
						return true;
					}
				}
			}
		}
		catch(Exception e) {
			return false;
		}
		
		return false;
	}
	

	private boolean assignedToCurrentRoles(WorkProduct product) {

		if (Context.currentRoles.size() == 0) {
			return true;
		}
		
		for(RoleDescriptor rd : Context.currentRoles) {
			for(TaskDescriptor td : MethodElements.taskDescriptors) {	
				if(MethodElements.performs(rd, td)) {
					for(WorkProductDescriptor wpd : td.getOutput()) {
						WorkProduct product2 = wpd.getWorkProduct();
						
						if(product2.eIsProxy()) {
							product2 = MethodElements.getWorkProduct(product2);
						}
						
						if(product2 != null && product.equals(product2)) {
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}
	
	private boolean containsFiles(WorkProduct product) {
		
		try {
			String projectLocation = Context.selectedProject.getLocation().toString();
			String methodPropertiesPath = projectLocation + "/.method/methodProperties.txt";

			FileReader fr = new FileReader(methodPropertiesPath);
			BufferedReader br = new BufferedReader(fr);
			
			String s;
			
			while ((s = br.readLine()) != null) {
				
				if(s.startsWith("Resource: ")) {
					int index = s.indexOf(" - WorkProduct GUId: ");
					
					if(index >= 0) {
						String location = s.substring(10, index);
						String guid = s.substring(index + 21, s.length());
						
						if(product.getGuid().equals(guid)) {
							
							IPath path = new Path(location.substring(projectLocation.length()));
							IResource res = Context.selectedProject.findMember(path);
							
							if(res instanceof IFile) {
								return true;
							}
						}
					}
				}
			}
			
			fr.close();

			return false;
			
		} catch (Exception e) {
			return false;
		}
	}
}
