package moskitt4me.projectmanager.core.providers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import moskitt4me.projectmanager.core.context.Context;
import moskitt4me.projectmanager.methodspecification.MethodElements;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.epf.uma.Domain;
import org.eclipse.epf.uma.WorkProduct;

/*
 * ContentProvider for the Product Explorer View.
 *
 * @author Mario Cervera
 */
public class ProductsContentProvider extends AdapterFactoryContentProvider {
	
	public ProductsContentProvider(AdapterFactory adapterFactory) {
		
		super(adapterFactory);
	}
	
	@Override
	public Object[] getElements(Object object) {
		
		if(object instanceof IProject) {
			Object[] elements = new Object[1];
			elements[0] = new ProductsItemProvider(adapterFactory);
			
			return elements;
		}
		
		return super.getElements(object);
	}
	
	@Override
	public boolean hasChildren(Object object) {
			
		if(object instanceof ProductsItemProvider ||
				object instanceof Domain ||
				object instanceof WorkProduct) {
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public Object[] getChildren(Object object) {
		
		try {
			
			List<Object> children = new ArrayList<Object>();
			
			if(object instanceof ProductsItemProvider) {
				
				children.addAll(MethodElements.domains);
				Domain uncategorizedDomain = new UncategorizedDomain();
				uncategorizedDomain.setName("Uncategorized");
				uncategorizedDomain.setPresentationName("Uncategorized");
				children.add(uncategorizedDomain);
			}
			else if(object instanceof Domain) {
				
				Domain domain = (Domain) object;
				
				if(domain instanceof UncategorizedDomain) {
					children.addAll(MethodElements.getUncategorizedWorkProducts());
				}
				else {
					children.addAll(domain.getSubdomains());
					children.addAll(domain.getWorkProducts());
				}
			}
			else if(object instanceof WorkProduct) {
				
				WorkProduct product = (WorkProduct) object;
				
				children.addAll(getFiles(product));
			}
			
			return children.toArray();
		}
		catch(Exception e) {
			return null;
		}
	}
	
	private List<IFile> getFiles(WorkProduct product) {
		
		List<IFile> files = new ArrayList<IFile>();
		
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
								IFile file = (IFile) res;
								files.add(file);
							}
						}
					}
				}
			}
			
			fr.close();

			return files;
			
		} catch (Exception e) {
			return files;
		}
	}
}
