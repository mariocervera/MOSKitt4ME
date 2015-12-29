package moskitt4me.repositoryClient.core.util;

public class ExternalToolFragment extends TechnicalFragment {

	private String fileExtension;
	private String description;
	
	public ExternalToolFragment(String name, String origin, String objective,
			String input, String output, String fileExtension,
			String description) {
		
		super(name, "External Tool", origin, objective, input, output);
		
		this.fileExtension = fileExtension;
		this.description = description;
	}
	
	public String getFileExtension() {
		return fileExtension;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
}
