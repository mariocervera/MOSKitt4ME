package moskitt4me.repositoryClient.core.providers;

import moskitt4me.repositoryClient.core.util.RepositoryLocation;
import moskitt4me.repositoryClient.core.util.TechnicalFragment;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class DependenciesTreeContentProvider implements ITreeContentProvider {
	
	TechnicalFragment rootFragment;
	
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
	
	public Object getParent(Object element) {
		
		if(element instanceof TechnicalFragment) {
			TechnicalFragment tf = (TechnicalFragment) element;
			return tf.getParent();
		}
		
		return null;
	}
	
	public Object[] getElements(Object inputElement) {
		
		if(inputElement instanceof RepositoryLocation) {
			return new Object[]{rootFragment};
		}
		
		return null;
	}
	
	public boolean hasChildren(Object element) {

		if(element instanceof TechnicalFragment) {
			TechnicalFragment tf = (TechnicalFragment) element;
			return tf.getDependencies().size() > 0;
		}
		
		return false;
	}
	
	public Object[] getChildren(Object parentElement) {
		
		if(parentElement instanceof TechnicalFragment) {
			TechnicalFragment tf = (TechnicalFragment) parentElement;
			return tf.getDependencies().toArray();
		}
		
		return null;
	}	

}
