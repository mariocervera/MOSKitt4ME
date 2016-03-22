package moskitt4me.repositoryClient.core.filters;

import moskitt4me.repositoryClient.core.providers.MethodFragmentItemProvider;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/*
* This filter is applied when search criteria are specified by means of the Search Dialog.
*
* @author Mario Cervera
*/
public class SearchFilter extends ViewerFilter {

	private String type;
	private String origin;
	private String objective;
	private String input;
	private String output;
    
    	public SearchFilter() {
		this("","","","","");
	}
    
    	public SearchFilter(String type, String origin, String objective, String input, String output) {
		this.type = type;
		this.origin = origin;
		this.objective = objective;
		this.input = input;
		this.output = output;
	}
   
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
	
		if(type.equals("") && origin.equals("") && objective.equals("") &&
				input.equals("") && output.equals("")) {
			
			return true;
		}
		
		if(element instanceof MethodFragmentItemProvider) {
			
			MethodFragmentItemProvider mf = (MethodFragmentItemProvider) element;
				
			if (!type.equals("") &&
					!mf.getType().toLowerCase().contains(type.toLowerCase())) {
				return false;
			}
			if(!origin.equals("") &&
					!mf.getOrigin().toLowerCase().contains(origin.toLowerCase())) {
				return false;
			}
			if(!objective.equals("") &&
					!mf.getObjective().toLowerCase().contains(objective.toLowerCase())) {
				return false;
			}
			if(!input.equals("") &&
					!mf.getInput().toLowerCase().contains(input.toLowerCase())) {
				return false;
			}
			if(!output.equals("") &&
					!mf.getOutput().toLowerCase().contains(output.toLowerCase())) {
				return false;
			}
			
			return true;
		}
		
		return true;
	}

}
