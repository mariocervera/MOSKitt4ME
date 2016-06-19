package moskitt4me.repositoryClient.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;

/**
 * A class for technical fragments. 
 *
 * Subclasses: ExternalToolFragment and InternalToolFragment.
 *
 * @author Mario Cervera
 */
public class TechnicalFragment {

	// Technical fragment properties: name, type, origin, objective, input, and output
	
	private String name;
	private String type;
	private String origin;
	private String objective;
	private String input;
	private String output;
	
	// Eclipse plug-ins that are encapsulated in the technical fragment
	
	private List<IProject> plugins;
	
	// Does the fragment contain errors (e.g., empty name or unresolved dependencies) ?
	
	private boolean isResolved;
	private String errors;
	
	// Software dependencies
	
	private List<TechnicalFragment> dependencies = new ArrayList<TechnicalFragment>();
	private TechnicalFragment parent;
	
	// Has the fragment been imported from another repository
	
	boolean isImported;

	// Constructors
	
	public TechnicalFragment() {
		
		this.isResolved = false;
		this.plugins = new ArrayList<IProject>();
		this.isImported = false;
	}
	
	public TechnicalFragment(String name, String type, String origin, 
			String objective, String input, String output) {
		
		this();
		
		this.name = name;
		this.type = type;
		this.origin = origin;
		this.objective = objective;
		this.input = input;
		this.output = output;
	}
	
	// Getters and setters
	
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
	
	public String getInput() {
		return input;
	}
	
	public String getOutput() {
		return output;
	}
	
	public List<IProject> getPlugins() {
		return plugins;
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
	
	public void setInput(String input) {
		this.input = input;
	}
	
	public void setOutput(String output) {
		this.output = output;
	}
	
	public void setPlugins(List<IProject> plugins) {
		this.plugins = plugins;
	}
	
	public void setResolved(boolean isResolved) {
		this.isResolved = isResolved;
	}
	
	public void setErrors(String errors) {
		this.errors = errors;
	}
	
	public List<TechnicalFragment> getDependencies() {
		return dependencies;
	}
	
	public TechnicalFragment getParent() {
		return parent;
	}
	
	public void setParent(TechnicalFragment parent) {
		this.parent = parent;
	}
	
	public boolean isImported() {
		return isImported;
	}
	
	public void setImported(boolean isImported) {
		this.isImported = isImported;
	}
	
	/*
	 * Calculates whether the fragment contains errors. This is necessary in the "Create Technical Fragment Dialog".
	 * An error may be, for example, that the user has not specified the fragment name or a software dependency
	 * is unsatisfied.
	 */
	public void resolve() {
		
		boolean resolved = true;
		
		this.errors = "";
		
		Map<IProject, List<IProject>> unsatisfiedDependencies = getUnsatisfiedDependencies();
		
		if(name == null || name.equals("")) {
			resolved = false;
			errors += "- The name of the fragment must be specified.\n";
		}
		if(name != null && !name.equals("") && name.contains(" ")) {
			resolved = false;
			errors += "- The name of the fragment cannot contain blanks.\n";
		}
		if(type == null || type.equals("")) {
			resolved = false;
			errors += "- The type of the fragment must be specified.\n";
		}
		if(origin == null || origin.equals("") && !isOthers()) {
			resolved = false;
			errors += "- The origin of the fragment must be specified.\n";
		}
		if(objective == null || objective.equals("") && !isOthers()) {
			resolved = false;
			errors += "- The objective of the fragment must be specified.\n";
		}
		if(input == null || input.equals("") && !isOthers()) {
			resolved = false;
			errors += "- The input of the fragment must be specified.\n";
		}
		if(output == null || output.equals("") && !isOthers()) {
			resolved = false;
			errors += "- The output of the fragment must be specified.\n";
		}
		if(plugins == null || plugins.size() == 0) {
			resolved = false;
			errors += "- The fragment must not be empty.\n";
		}
		if(unsatisfiedDependencies.size() > 0) {
			resolved = false;
			Set<IProject> keys = unsatisfiedDependencies.keySet();
			for(IProject key : keys) {
				List<IProject> dependencies = unsatisfiedDependencies.get(key);
				for(IProject dependency : dependencies) {
					errors += "- Unsatisfied dependency: " + key.getName() + " --> " + dependency.getName() + "\n";
				}
			}
		}
		
		if(errors != null && !errors.equals("")) {
			errors = "This technical fragment has the following errors:\n\n" + errors;
		}
		
		this.isResolved = resolved;
	}
	
	private Map<IProject, List<IProject>> getUnsatisfiedDependencies() {
		
		// Project of the fragment - List of its unsatisfied dependencies 
		
		Map<IProject, List<IProject>> unsatisfiedDependencies = new HashMap<IProject, List<IProject>>();
		
		for(IProject project : plugins) {
			List<IProject> dependencies = DependenciesUtil.getDependencies(project);
			if(dependencies.size() > 0) {
				removeSatisfiedDependencies(dependencies, this);
				if(dependencies.size() > 0) {
					unsatisfiedDependencies.put(project, dependencies);
				}
			}
		}
		
		return unsatisfiedDependencies;
	}
	
	private void removeSatisfiedDependencies(List<IProject> dependencies, TechnicalFragment tf) {
		
		dependencies.removeAll(tf.getPlugins());
		
		if(dependencies.size() > 0) {
			List<TechnicalFragment> deps = tf.getDependencies();
			for(TechnicalFragment tf2 : deps) {
				removeSatisfiedDependencies(dependencies, tf2);
			}
		}
	}
	
	/*
	 * Checks whether the fragment is of type "Others" (i.e., it is not of one of the 
	 * types supported by MOSKitt4ME).
	 */
	private boolean isOthers() {
		
		return (this.type != null && this.type.equals("Others"));
	}
	
	/*
	 * Duplicates the fragment. Useful for the copy & paste functionality
	 */
	public TechnicalFragment duplicate() {
		
		TechnicalFragment clone = new TechnicalFragment();
		
		clone.setName(this.getName());
		clone.setType(this.getType());
		clone.setOrigin(this.getOrigin());
		clone.setObjective(this.getObjective());
		clone.setInput(this.getInput());
		clone.setOutput(this.getOutput());
		clone.setImported(this.isImported());
		
		for(IProject prj : this.plugins) {
			clone.getPlugins().add(prj);
		}
		
		for(TechnicalFragment dependency : this.dependencies) {
			TechnicalFragment cloneDep = dependency.duplicate();
			clone.getDependencies().add(cloneDep);
			cloneDep.setParent(clone);
		}
		
		return clone;
	}
}
