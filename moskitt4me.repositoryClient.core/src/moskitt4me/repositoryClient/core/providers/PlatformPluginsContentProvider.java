package moskitt4me.repositoryClient.core.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

/*
* A content provider for the composite of the "Define Internal Tool Dialog"
*
* @author Mario Cervera
*/
public class PlatformPluginsContentProvider extends ArrayContentProvider implements ITreeContentProvider {

	private List<String> pluginsToExclude;
	
	public PlatformPluginsContentProvider() {
		
		this.pluginsToExclude = new ArrayList<String>();
	}
	
	public void setPluginsToExclude(List<String> pluginsToExclude) {
		this.pluginsToExclude = pluginsToExclude;
	}
	
	public Object[] getElements(Object inputElement) {

		if (inputElement instanceof List<?>) {
			
			List<String> elements = new ArrayList<String>();
			List<?> list = (List<?>) inputElement;
			
			for (Object obj : list) {
				if (obj instanceof String) {
					String s = (String) obj;
					if(!this.pluginsToExclude.contains(s)) {
						elements.add(s);
					}
				}
			}
			Collections.sort(elements);
			
			return elements.toArray();
		}

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
