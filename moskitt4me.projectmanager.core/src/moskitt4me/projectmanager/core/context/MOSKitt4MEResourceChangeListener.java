package moskitt4me.projectmanager.core.context;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;

/*
* A listener that reacts to Resource Change events in the Resource Explorer view. In MOSKitt4ME,
* when the selected project changes, the process instance that is running in the Activiti Engine
* must be updated (since every project has a different process instance associated to it).
*
* @author Mario Cervera
*/
public class MOSKitt4MEResourceChangeListener implements
		IResourceChangeListener {
	
	public void resourceChanged(IResourceChangeEvent event) {
		
		IResourceDelta delta = event.getDelta();
		
		List<String> initialPath = new ArrayList<String>();
		List<String> finalPath = new ArrayList<String>();
		
		IResourceDelta[] deltas = delta.getAffectedChildren();
		
		updatePaths(initialPath, finalPath, deltas);
		
		String oldPath = "";
		String newPath = "";
		
		for(String s : initialPath) {
			oldPath += s;
		}
		for(String s : finalPath) {
			newPath += s;
		}
		
		if(!oldPath.equals(newPath) && 
				!newPath.contains(Context.selectedProject.getName() + "/.method/")) { // Rename ...
			
			Context.updatePath(oldPath, newPath);
		}
	}

	private void updatePaths(List<String> initialPath, List<String> finalPath, IResourceDelta[] deltas) {
		
		if(deltas.length == 2) {
			
			if(deltas[0].getMovedFromPath() != null) {
				initialPath.clear();
				initialPath.add(deltas[0].getMovedFromPath().toString());
			}
			else if(deltas[1].getMovedFromPath() != null) {
				initialPath.clear();
				initialPath.add(deltas[1].getMovedFromPath().toString());
			}
			
			if(deltas[0].getMovedToPath() != null) {
				finalPath.clear();
				finalPath.add(deltas[0].getMovedToPath().toString());
			}
			else if(deltas[1].getMovedToPath() != null) {
				finalPath.clear();
				finalPath.add(deltas[1].getMovedToPath().toString());
			}
			
		}
		else if(deltas.length == 1) {
			initialPath.add("/" + deltas[0].getResource().getName().toString());
			finalPath.add("/" + deltas[0].getResource().getName().toString());
		}
		
		if(deltas.length > 0) {
			IResourceDelta[] deltas2 = deltas[0].getAffectedChildren();
			updatePaths(initialPath, finalPath, deltas2);
		}
	}
}
