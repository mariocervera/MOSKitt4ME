package moskitt4me.repositoryClient.core.providers;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;

/*
 * Asset Property Item Providers are Descriptor, Interface, 
 * Origin, Objective, Type, Situation and Intention
 */
public class MethodFragmentPropertyItemProvider extends ItemProviderAdapter {

	/*
	 * The associated MethodFragmentItemProvider needs to be stored to retrieve the repository 
	 * location and the file name when calculating the Descriptor and Interface children
	 */
	private MethodFragmentItemProvider methodFragment;
	
	private String propertyName;
	private String propertyValue;
	
	public MethodFragmentPropertyItemProvider(AdapterFactory adapterFactory, 
			MethodFragmentItemProvider methodFragment, 
			String propertyName, String propertyValue) {
		
		super(adapterFactory);
		
		this.methodFragment = methodFragment;
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
	}
	
	public void setMethodFragment(MethodFragmentItemProvider methodFragment) {
		this.methodFragment = methodFragment;
	}

	public MethodFragmentItemProvider getMethodFragment() {
		return this.methodFragment;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}

	public String getPropertyValue() {
		return propertyValue;
	}
}
