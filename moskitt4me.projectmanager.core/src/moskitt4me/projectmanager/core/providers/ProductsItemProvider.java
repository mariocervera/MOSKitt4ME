package moskitt4me.projectmanager.core.providers;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;

/**
 * The root element of the Product Explorer view
 * 
 * @author Mario Cervera
 */
public class ProductsItemProvider extends ItemProviderAdapter  {

	public ProductsItemProvider(AdapterFactory adapterFactory) {
		
		super(adapterFactory);
	}
	
}
