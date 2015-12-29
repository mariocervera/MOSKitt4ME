package moskitt4me.repositoryClient.core.providers;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

public class AssetPluginsContentProvider extends ArrayContentProvider implements ITreeContentProvider {

	public Object[] getElements(Object inputElement) {
		
		return super.getElements(inputElement);
	}


	public Object getParent(Object element) {
		
		return null;
	}

	public boolean hasChildren(Object element) {
		
		return false;
	}

	public Object[] getChildren(Object parentElement) {
		
		return null;
	}
}
