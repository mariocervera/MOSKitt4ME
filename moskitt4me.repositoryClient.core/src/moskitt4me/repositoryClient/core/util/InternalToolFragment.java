package moskitt4me.repositoryClient.core.util;

import java.util.List;

public class InternalToolFragment extends TechnicalFragment {

	private String description;
	private List<String> pluginIds;
	
	public InternalToolFragment(String name, String origin, String objective,
			String input, String output, String description, List<String> pluginIds) {
		
		super(name, "Internal Tool", origin, objective, input, output);
		
		this.description = description;
		this.pluginIds = pluginIds;
		
	}
	
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
