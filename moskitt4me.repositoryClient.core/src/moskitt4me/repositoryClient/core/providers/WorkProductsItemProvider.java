package moskitt4me.repositoryClient.core.providers;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.epf.uma.MethodPackage;

/**
 * The "Work Products" element that appears in the Tree Viewer of the "Content Element Selection Dialog"
 *
 * @author Mario Cervera
 */
public class WorkProductsItemProvider extends ItemProviderAdapter {

	private MethodPackage associatedPackage;
	
	public WorkProductsItemProvider(AdapterFactory adapterFactory,
			MethodPackage associatedPackage) {
		
		super(adapterFactory);
		
		this.associatedPackage = associatedPackage;
	}
	
	public MethodPackage getAssociatedPackage() {
		return associatedPackage;
	}
}
