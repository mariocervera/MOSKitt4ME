package moskitt4me.repositoryClient.core.actions;

import moskitt4me.repositoryClient.core.Activator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class RepositoriesViewActionGroup extends ActionGroup {

	private final CommonViewer commonViewer;
	
	//Actions
	
	private IntegrateFragmentAction integrateConceptualFragmentAction;
	
	private IntegrateFragmentAction integrateTechnicalFragmentAction;
	
	private AddRepositoryLocationAction addRepositoryLocationAction;
	
	private SearchAction searchAction;
	
	private CollapseAllAction collapseAllAction;
	
	public RepositoriesViewActionGroup(CommonViewer viewer) {
		
		super();
		
		this.commonViewer = viewer;
		
		makeActions();
	}
	
	protected final ImageDescriptor getImageDescriptor(String relativePath) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, relativePath);
	}
	
	private void makeActions() {
		
		//Integrate Conceptual Fragment action
		
		integrateConceptualFragmentAction = new IntegrateConceptualFragmentAction();
		ImageDescriptor integrateConceptualFragmentIcon = getImageDescriptor("icons/full/elcl16/integrateconceptualfragment.gif");
		integrateConceptualFragmentAction.setImageDescriptor(integrateConceptualFragmentIcon);
		integrateConceptualFragmentAction.setToolTipText("Integrate Conceptual Fragment");
		
		//Integrate Technical Fragment action
		
		integrateTechnicalFragmentAction = new IntegrateTechnicalFragmentAction();
		ImageDescriptor integrateTechnicalFragmentIcon = getImageDescriptor("icons/full/elcl16/integratetechnicalfragment.gif");
		integrateTechnicalFragmentAction.setImageDescriptor(integrateTechnicalFragmentIcon);
		integrateTechnicalFragmentAction.setToolTipText("Integrate Technical Fragment");
		
		//Add Repository Location action
		
		addRepositoryLocationAction = new AddRepositoryLocationAction();
		ImageDescriptor addRepositoryLocationIcon = getImageDescriptor("icons/full/elcl16/addrepositorylocation.gif");
		addRepositoryLocationAction.setImageDescriptor(addRepositoryLocationIcon);
		addRepositoryLocationAction.setToolTipText("Add repository location");
		
		//Search action
		
		searchAction = new SearchAction();
		ImageDescriptor searchIcon = getImageDescriptor("icons/full/elcl16/search.gif");
		searchAction.setImageDescriptor(searchIcon);
		searchAction.setToolTipText("Search");
		
		//Collapse all action
		
		collapseAllAction = new CollapseAllAction(commonViewer);
		ImageDescriptor collapseAllIcon = getImageDescriptor("icons/full/elcl16/collapseall.gif");
		collapseAllAction.setImageDescriptor(collapseAllIcon);
		collapseAllAction.setToolTipText("Collapse all");
	}
	
	@Override
	public void fillActionBars(IActionBars actionBars) {
		
		actionBars.getToolBarManager().add(integrateConceptualFragmentAction);
		actionBars.getToolBarManager().add(integrateTechnicalFragmentAction);
		actionBars.getToolBarManager().add(addRepositoryLocationAction);
		actionBars.getToolBarManager().add(searchAction);
		actionBars.getToolBarManager().add(collapseAllAction);
	}
}
