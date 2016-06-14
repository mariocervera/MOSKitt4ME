package moskitt4me.repositoryClient.core.util;

import java.util.List;

/**
 * A class that represents a Technical Fragment of type "Internal Tool". An internal tool is a
 * tool that is already installed in MOSKitt4ME (e.g., Eclipse frameworks such as GMF or EMF).
 * This type of technical fragments do not need to encapsulate the implementation of the tools;
 * only the identifiers of the Eclipse plug-ins.
 *
 * @author Mario Cervera
 */
public class InternalToolFragment extends TechnicalFragment {

	private String description; // A description of the tool
	private List<String> pluginIds; // References to the plug-ins that implement the internal tool
	
	/*
	 * Constructor
	 */
	public InternalToolFragment(String name, String origin, String objective,
			String input, String output, String description, List<String> pluginIds) {
		
		super(name, "Internal Tool", origin, objective, input, output);
		
		this.description = description;
		this.pluginIds = pluginIds;
		
	}
	
	// Getters and setters
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public void setPluginIds(List<String> pluginIds) {
		this.pluginIds = pluginIds;
	}

	public List<String> getPluginIds() {
		return pluginIds;
	}
	
}
