package moskitt4me.repositoryClient.core.providers;

import moskitt4me.repositoryClient.core.util.RepositoryLocation;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;

/**
 * This class represents the method fragments that will be displayed in the Repositories views.
 *
 * @author Mario Cervera
 */
public class MethodFragmentItemProvider extends ItemProviderAdapter {

	private RepositoryLocation location;
	private String fileName;
	
	// Method fragment properties
	
	private String type;
	private String origin;
	private String objective;
	private String input;
	private String output;
	private String toolId;
	private String description; //Only for external and internal tools
	
	/*
	 * Constructor
	 */
	public MethodFragmentItemProvider(AdapterFactory adapterFactory,
			RepositoryLocation location, String fileName, String type,
			String origin, String objective, String input, 
			String output, String toolId, String description) {
		
		super(adapterFactory);
		
		this.location = location;
		this.fileName = fileName;
		
		this.type = type;
		this.origin = origin;
		this.objective = objective;
		this.input = input;
		this.output = output;
		this.toolId = toolId;
		this.description = description;
	}

	/*
	 * Constructor
	 */
	public MethodFragmentItemProvider(AdapterFactory adapterFactory,
			RepositoryLocation location, String fileName, String type,
			String origin, String objective) {
		
		this(adapterFactory, location, fileName, type, origin, objective, "", "", "" ,"");
	}

	// Getters
	
	public RepositoryLocation getLocation() {
		return location;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public String getType() {
		return type;
	}
	
	public String getOrigin() {
		return origin;
	}
	
	public String getObjective() {
		return objective;
	}
	
	public String getInput() {
		return input;
	}
	
	public String getOutput() {
		return output;
	}
	
	public String getToolId() {
		return toolId;
	}
	
	public String getDescription() {
		return description;
	}
}
