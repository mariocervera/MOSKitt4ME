package moskitt4me.repositoryClient.core.providers;

import moskitt4me.repositoryClient.core.util.RepositoryLocation;
import moskitt4me.repositoryClient.core.util.TechnicalFragment;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Provides content for the Dependencies Tree of the Create Technical Fragment Dialog.
 *
 * @author Mario Cervera
 */
public class DependenciesTreeContentProvider implements ITreeContentProvider {
	
	// The root fragment of the tree. All of its descendants represent software dependencies
	
	TechnicalFragment rootFragment;
	
	/*
	 * Constructor
	 */
	public DependenciesTreeContentProvider() {
		
		rootFragment = new TechnicalFragment();
		rootFragment.setName("NewTechnicalFragment");
		rootFragment.resolve();
	}

	public TechnicalFragment getRootFragment() {
		return rootFragment;
	}
	
	public void dispose() {

	}
	
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	
	}
	
	/*
	 * Returns the parent of a given technical fragment
	 */
	public Object getParent(Object element) {
		
		if(element instanceof TechnicalFragment) {
			TechnicalFragment tf = (TechnicalFragment) element;
			return tf.getParent();
		}
		
		return null;
	}
	
	/*
	 * This method returns the root-level element of the Dependencies Tree. In this case, the
	 * root is the technical fragment that is stored in the variable "rootFragment"
	 */
	public Object[] getElements(Object inputElement) {
		
		if(inputElement instanceof RepositoryLocation) {
			return new Object[]{rootFragment};
		}
		
		return null;
	}
	
	/*
	* Calculates whether a given element has children
	*/
	public boolean hasChildren(Object element) {

		if(element instanceof TechnicalFragment) {
			TechnicalFragment tf = (TechnicalFragment) element;
			return tf.getDependencies().size() > 0;
		}
		
		return false;
	}
	
	/*
	* This method returns the children of a given element. The children of a technical fragment
	* are its software dependencies
	*/
	public Object[] getChildren(Object parentElement) {
		
		if(parentElement instanceof TechnicalFragment) {
			TechnicalFragment tf = (TechnicalFragment) parentElement;
			return tf.getDependencies().toArray();
		}
		
		return null;
	}	

}
