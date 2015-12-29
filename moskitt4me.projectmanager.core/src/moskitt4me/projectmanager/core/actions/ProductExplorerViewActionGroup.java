package moskitt4me.projectmanager.core.actions;

import org.eclipse.jface.viewers.TreeViewer;

/*
 * This class defines the actions that appear in the action bars of the Product Explorer
 * view and are not common to all the other views in the project manager
 */
public class ProductExplorerViewActionGroup extends ProjectManagerActionGroup {
	
	public ProductExplorerViewActionGroup(TreeViewer viewer) {
		
		super(viewer);
	}
	
}
