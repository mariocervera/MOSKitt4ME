package moskitt4me.repositoryClient.core.providers;

import moskitt4me.repositoryClient.core.util.RepositoryLocation;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;


public class MethodFragmentItemProvider extends ItemProviderAdapter {

	private RepositoryLocation location;
	private String fileName;
	
	private String type;
	private String origin;
	private String objective;
	private String input;
	private String output;
	private String toolId;
	private String description; //Only for external and internal tools
	
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

	public MethodFragmentItemProvider(AdapterFactory adapterFactory,
			RepositoryLocation location, String fileName, String type,
			String origin, String objective) {
		
		this(adapterFactory, location, fileName, type, origin, objective, "", "", "" ,"");
	}

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
