package moskitt4me.repositoryClient.core.providers;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.epf.uma.MethodPackage;

/**
 * The "Tasks" element that appears in the Tree Viewer of the "Content Element Selection Dialog"
 *
 * @author Mario Cervera
 */
public class TasksItemProvider extends ItemProviderAdapter {

	private MethodPackage associatedPackage;
	
	public TasksItemProvider(AdapterFactory adapterFactory,
			MethodPackage associatedPackage) {
		
		super(adapterFactory);
		
		this.associatedPackage = associatedPackage;
	}
	
	public MethodPackage getAssociatedPackage() {
		return associatedPackage;
	}
}
