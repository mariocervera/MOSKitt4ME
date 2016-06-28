package moskitt4me.projectmanager.core.actions;

import org.eclipse.jface.viewers.TreeViewer;

/**
 * This class defines the actions that appear in the action bar of the Guides view
 * and are not common to all the other views in the project manager
 *
 * @author Mario Cervera
 */
public class GuidesViewActionGroup extends ProjectManagerActionGroup {

	public GuidesViewActionGroup(TreeViewer viewer) {
		
		super(viewer);
	}

}
