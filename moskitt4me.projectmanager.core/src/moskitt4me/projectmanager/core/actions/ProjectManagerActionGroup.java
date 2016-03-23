package moskitt4me.projectmanager.core.actions;

import moskitt4me.projectmanager.core.Activator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.plugin.AbstractUIPlugin;


/*
 * This class defines the actions that appear in the Action Bar of all the views
 * of the Project Manager Component.
 *
 * @author Mario Cervera
 */
public class ProjectManagerActionGroup extends ActionGroup {

	private final TreeViewer treeViewer;
	
	//Actions
	
	private CollapseAllAction collapseAllAction;
	
	public ProjectManagerActionGroup(TreeViewer viewer) {
		
		super();
		
		this.treeViewer = viewer;
		
		makeActions();
	}
	
	protected final ImageDescriptor getImageDescriptor(String relativePath) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, relativePath);
	}
	
	private void makeActions() {
		
		//Collapse all action
		
		collapseAllAction = new CollapseAllAction(treeViewer);
		ImageDescriptor collapseAllIcon = getImageDescriptor("icons/full/elcl16/collapseall.gif"); //$NON-NLS-1$
		collapseAllAction.setImageDescriptor(collapseAllIcon);
		collapseAllAction.setToolTipText("Collapse all");
	}
	
	@Override
	public void fillActionBars(IActionBars actionBars) {
		
		actionBars.getToolBarManager().add(collapseAllAction);
	}
}
