package moskitt4me.repositoryClient.fragmentIntegration.util;

import java.util.ArrayList;
import java.util.List;

/**Auxiliary class that represent a Conceptual fragment
 * Use: is where the fragment reader saves the information temporaly
 * */

public class ConceptualFragment {

	private String name;
	private String type;
	private String origin;
	private String objective;
	private List<Object> elements;
	private boolean isResolved;
	private String errors;
	
	public ConceptualFragment() {
		this.isResolved = false;
		setElements(new ArrayList<Object>());
	}
	
	public ConceptualFragment(String name, String type, String origin, 
			String objective) {
		
		this();
		
		this.name = name;
		this.type = type;
		this.origin = origin;
		this.objective = objective;
		
	}
	
	public String getName() {
		return name;
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
	
	public boolean isResolved() {
		return isResolved;
	}
	
	public String getErrors() {
		return errors;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	
	public void setObjective(String objective) {
		this.objective = objective;
	}
	
	public void setResolved(boolean isResolved) {
		this.isResolved = isResolved;
	}
	
	public void setErrors(String errors) {
		this.errors = errors;
	}

	public void resolve() {
		
		boolean resolved = true;
		
		this.errors = "";
	
		if(name == null || name.equals("")) {
			resolved = false;
			errors += "- The name of the asset must be specified.\n";
		}
		if(type == null || type.equals("")) {
			resolved = false;
			errors += "- The type of the asset must be specified.\n";
		}
		if(origin == null || origin.equals("") && !isOthers()) {
			resolved = false;
			errors += "- The origin of the asset must be specified.\n";
		}
		if(objective == null || objective.equals("") && !isOthers()) {
			resolved = false;
			errors += "- The objective of the asset must be specified.\n";
		}
		if(errors != null && !errors.equals("")) {
			errors = "This asset has the following errors:\n\n" + errors;
		}
		this.isResolved = resolved;
	}
	
	private boolean isOthers() {
		return (this.type != null && this.type.equals("Others"));
	}
	
	public ConceptualFragment duplicate() {
		
		ConceptualFragment clone = new ConceptualFragment();
		clone.setName(this.getName());
		clone.setType(this.getType());
		clone.setOrigin(this.getOrigin());
		clone.setObjective(this.getObjective());
		return clone;
		
	}

	public void setElements(List<Object> elements) {
		this.elements = elements;
	}

	public List<Object> getElements() {
		return elements;
	}
}
