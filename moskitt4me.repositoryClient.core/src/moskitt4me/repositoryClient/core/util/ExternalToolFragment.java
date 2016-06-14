package moskitt4me.repositoryClient.core.util;

/**
 * A class that represents a Technical Fragment of type "External Tool". An external tool is a
 * tool that is not implemented as Eclipse plug-ins, and, therefore, cannot be integrated in the
 * generated CASE environment. This type of fragments only contain textual information about the
 * external tool.
 *
 * @author Mario Cervera
 */
public class ExternalToolFragment extends TechnicalFragment {

	private String fileExtension; //The type of files supported by the external tool
	private String description; // A description of the tool
	
	/*
	 * Constructor
	 */
	public ExternalToolFragment(String name, String origin, String objective,
			String input, String output, String fileExtension,
			String description) {
		
		super(name, "External Tool", origin, objective, input, output);
		
		this.fileExtension = fileExtension;
		this.description = description;
	}
	
	// Getters and setters
	
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
