package moskitt4me.repositoryClient.core.views;

import java.util.ArrayList;
import java.util.List;

import moskitt4me.repositoryClient.core.Activator;
import moskitt4me.repositoryClient.core.actions.CreateTechnicalFragmentAction;
import moskitt4me.repositoryClient.core.actions.CreateConceptualFragmentAction;
import moskitt4me.repositoryClient.core.actions.DefineExternalToolAction;
import moskitt4me.repositoryClient.core.actions.DefineInternalToolAction;
import moskitt4me.repositoryClient.core.actions.DiscardLocationAction;
import moskitt4me.repositoryClient.core.actions.IntegrateConceptualFragmentAction;
import moskitt4me.repositoryClient.core.actions.IntegrateFragmentAction;
import moskitt4me.repositoryClient.core.actions.IntegrateTechnicalFragmentAction;
import moskitt4me.repositoryClient.core.actions.RefreshAction;
import moskitt4me.repositoryClient.core.actions.RepositoriesViewActionGroup;
import moskitt4me.repositoryClient.core.providers.MethodFragmentItemProvider;
import moskitt4me.repositoryClient.core.providers.RepositoriesContentProvider;
import moskitt4me.repositoryClient.core.providers.RepositoriesLabelProvider;
import moskitt4me.repositoryClient.core.util.RepositoryClientUtil;
import moskitt4me.repositoryClient.core.util.RepositoryLocation;

import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.epf.library.LibraryService;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/*
* This class implements the Repositories view, which represents the Repository Client of MOSKitt4ME.
*
* @author Mario Cervera
*/
public class RepositoriesView extends CommonNavigator implements IMenuListener {

	public static final String RepositoriesViewId = "repositoryClient.views.repositoriesView";
	
	private static List<RepositoryLocation> locations = new ArrayList<RepositoryLocation>();

	public List<RepositoryLocation> getLocations() {
		return locations;
	}
	
	@Override
	public void createPartControl(Composite parent) {
		
		super.createPartControl(parent);
		
		if(getCommonViewer() != null) {

			getCommonViewer().setContentProvider(
					new RepositoriesContentProvider(new ResourceItemProviderAdapterFactory()));
			
			getCommonViewer().setInput(locations);
			
			getCommonViewer().setLabelProvider(new RepositoriesLabelProvider());
			
			getCommonViewer().setSorter(null);
			
			getCommonViewer().addSelectionChangedListener(new SelectionChangedListener());
			
			createContextMenuForViewer(getCommonViewer());
		}
	}
	
	@Override
	public void init(IViewSite aSite, IMemento aMemento)
			throws PartInitException {
		
		super.init(aSite, aMemento);
		
		/*
		* Memento design pattern. Applied to store Repository Locations when MOSKitt4ME is closed
		* and restore them when MOSKitt4ME is opened.
		*/
		
		if(aMemento != null && locations.size() == 0) {
			int i = 0;
			while(aMemento.getString("host" + i) != null) {
				RepositoryLocation rp = new RepositoryLocation();
				rp.setHost(aMemento.getString("host" + i));
				rp.setRepositoryPath(aMemento.getString("repositorypath" + i));
				rp.setUser(aMemento.getString("user" + i));
				rp.setPassword(aMemento.getString("password" + i));
				rp.setType(aMemento.getString("type" + i));
				locations.add(rp);
				i++;
			}
		}
	}
	
	@Override
	protected CommonViewer createCommonViewerObject(Composite aParent) {
		
		//No multiple selection allowed
		
		return new CommonViewer(getViewSite().getId(), aParent,
				SWT.H_SCROLL | SWT.V_SCROLL);
	}
	
	private class SelectionChangedListener implements ISelectionChangedListener {
			
		public void selectionChanged(SelectionChangedEvent event) {
			
			Object selected = null;
			if(event.getSelection() instanceof StructuredSelection) {
				selected = ((StructuredSelection)event.getSelection()).getFirstElement();
			}
			
			enableIntegrateFragmentButtons(selected);
		}
	}
	
	@Override
	protected ActionGroup createCommonActionGroup() {
		
		return new RepositoriesViewActionGroup(this.getCommonViewer());
	}
	
	public void refreshViewer() {
		CommonViewer viewer = getCommonViewer();
		if (viewer != null && viewer.getTree().isDisposed() == false) {
			viewer.refresh();
		}
	}
	
	/*
	* Manages the enablement of two buttons of the Actions Bar: Integrate Conceptual Fragment and
	* Integrate Technical Fragment.
	*/
	private void enableIntegrateFragmentButtons(Object selection) {
		
		IntegrateFragmentAction c_action = getIntegrateConceptualFragmentAction();
		IntegrateFragmentAction t_action = getIntegrateTechnicalFragmentAction();

		if(c_action != null && t_action != null) {
			if(selection instanceof MethodFragmentItemProvider && isLibraryOpened()) {
				MethodFragmentItemProvider fragment = (MethodFragmentItemProvider) selection;
				
				if(RepositoryClientUtil.isTechnicalFragment(fragment.getType())) {
					c_action.setEnabled(false);
					t_action.setEnabled(true);
				}
				else {
					c_action.setEnabled(true);
					t_action.setEnabled(false);
				}
			}
			else {			
				c_action.setEnabled(false);
				t_action.setEnabled(false);
			}
		}
	}
	
	private IntegrateConceptualFragmentAction getIntegrateConceptualFragmentAction() {
		
		for(IContributionItem item : this.getViewSite().getActionBars().getToolBarManager().getItems()) {
			if(item instanceof ActionContributionItem) {
				ActionContributionItem acitem = (ActionContributionItem) item;
				if(acitem.getAction() instanceof IntegrateConceptualFragmentAction) {
					return (IntegrateConceptualFragmentAction)acitem.getAction();
				}
			}
		}
		
		return null;
	}
	
	private IntegrateTechnicalFragmentAction getIntegrateTechnicalFragmentAction() {
		
		for(IContributionItem item : this.getViewSite().getActionBars().getToolBarManager().getItems()) {
			if(item instanceof ActionContributionItem) {
				ActionContributionItem acitem = (ActionContributionItem) item;
				if(acitem.getAction() instanceof IntegrateTechnicalFragmentAction) {
					return (IntegrateTechnicalFragmentAction)acitem.getAction();
				}
			}
		}
		
		return null;
	}
	
	private boolean isLibraryOpened() {
		
		return LibraryService.getInstance().getCurrentMethodLibrary() != null;
	}
	
	/*
	* Memento design pattern. Applied to store Repository Locations when MOSKitt4ME is closed
	* and restore them when MOSKitt4ME is opened.
	*/
	@Override
	public void saveState(IMemento aMemento) {
		
		super.saveState(aMemento);
		
		int size = locations.size();
		
		for(int i=0; i < size; i++) {
			RepositoryLocation rp = locations.get(i);
			aMemento.putString("host" + i, rp.getHost());
			aMemento.putString("repositorypath" + i, rp.getRepositoryPath());
			aMemento.putString("user" + i, rp.getUser());
			aMemento.putString("password" + i, rp.getPassword());
			aMemento.putString("type" + i, rp.getType());
		}
	}
	
	private void createContextMenuForViewer(CommonViewer viewer) {
		MenuManager contextMenu = new MenuManager("Popup");
		contextMenu.setRemoveAllWhenShown(true);
		contextMenu.addMenuListener(this);
		Menu menu = contextMenu.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(contextMenu, viewer);
	}
	
	/*
	* This method creates the contextual menu.
	*/
	public void menuAboutToShow(IMenuManager manager) {
		
		ISelection selection = this.getCommonViewer().getSelection();
		if(selection instanceof StructuredSelection) {
			StructuredSelection sel = (StructuredSelection) selection;
			Object obj = sel.getFirstElement();
			if(obj instanceof RepositoryLocation) {
				RepositoryLocation rl = (RepositoryLocation) obj;
			
				RefreshAction ra = new RefreshAction();
				ra.setText("Refresh");
				ImageDescriptor id = getImageDescriptor("icons/full/elcl16/refresh.gif");
				ra.setImageDescriptor(id);
				manager.add(ra);
				
				DiscardLocationAction dla = new DiscardLocationAction();
				dla.setText("Discard location");
				ImageDescriptor id2 = getImageDescriptor("icons/full/obj16/Delete.gif");
				dla.setImageDescriptor(id2);
				manager.add(dla);
				
				if(rl.getType().equals("Empty") || rl.getType().equals("Conceptual")) {
					
					manager.add(new Separator());
					
					CreateConceptualFragmentAction ccfa = new CreateConceptualFragmentAction();
					ccfa.setText("Create conceptual fragment");
					ImageDescriptor id3 = getImageDescriptor("icons/full/obj16/ConceptualFragment.gif");
					ccfa.setImageDescriptor(id3);
					manager.add(ccfa);
				}
				if(rl.getType().equals("Empty") || rl.getType().equals("Technical")) {
					
					manager.add(new Separator());
					
					CreateTechnicalFragmentAction ctfa = new CreateTechnicalFragmentAction();
					ctfa.setText("Create technical fragment");
					ImageDescriptor id4 = getImageDescriptor("icons/full/obj16/TechnicalFragment.gif");
					ctfa.setImageDescriptor(id4);
					manager.add(ctfa);
					
					DefineExternalToolAction deta = new DefineExternalToolAction();
					deta.setText("Define external tool");
					ImageDescriptor id5 = getImageDescriptor("icons/full/obj16/TechnicalFragment.gif");
					deta.setImageDescriptor(id5);
					manager.add(deta);
					
					DefineInternalToolAction dita = new DefineInternalToolAction();
					dita.setText("Define internal tool");
					ImageDescriptor id6 = getImageDescriptor("icons/full/obj16/TechnicalFragment.gif");
					dita.setImageDescriptor(id6);
					manager.add(dita);
				}
			}
		}
	}
	
	protected final ImageDescriptor getImageDescriptor(String relativePath) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, relativePath);
	}
}
