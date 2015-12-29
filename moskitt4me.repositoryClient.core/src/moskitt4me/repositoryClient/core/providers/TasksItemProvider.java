package moskitt4me.repositoryClient.core.providers;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;
import org.eclipse.epf.uma.MethodPackage;

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
