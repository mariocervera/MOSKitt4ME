package moskitt4me.projectmanager.core.views;

import moskitt4me.projectmanager.core.actions.ProductExplorerViewActionGroup;
import moskitt4me.projectmanager.core.actions.RoleSelectionAction.NullRole;
import moskitt4me.projectmanager.core.context.Context;
import moskitt4me.projectmanager.core.filters.DomainsFilter;
import moskitt4me.projectmanager.core.providers.ProductsContentProvider;
import moskitt4me.projectmanager.core.providers.ProductsLabelProvider;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.epf.uma.RoleDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.ide.IDE;

/**
 * The Product Explorer view shows a hierarchical representation of the method products that
 * have been created for the project that is selected in the Resource Explorer. This hierarchy is
 * based on the elements of type "Domain" and "Product", which are defined in the method model.
 * 
 * @author Mario Cervera
 */
public class ProductExplorerView extends ProjectManagerViewPart {

	public static final String ProductExplorerViewId = "moskitt4me.projectmanager.core.views.productExplorerView";
	
	@Override
	public void createPartControl(Composite parent) {
		
		super.createPartControl(parent);
		
		this.updateContentDescription();
	}
	
	/*
	 * Double-click events open the selected file
	 */
	@Override
	protected void handleDoubleClick(DoubleClickEvent anEvent) {
		
		super.handleDoubleClick(anEvent);
		
		if(anEvent.getSelection() instanceof StructuredSelection) {
			
			StructuredSelection selection = (StructuredSelection) anEvent.getSelection();
			
			// If the selected element is a file, open it
			
			if(selection.getFirstElement() instanceof IFile) {
				
				IFile file = (IFile) selection.getFirstElement();
				
				openFile(file);
			}
		}
	}
	
	/*
	 * This method opens the default editor that supports the edition of the given file
	 */
	private void openFile(IFile file) {
		 
		if (file.exists()) {
			
		    IFileStore fileStore = EFS.getLocalFileSystem().getStore(file.getLocationURI());
		    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		 
		    try {
		        IDE.openEditorOnFileStore(page, fileStore);
		    }
		    catch ( PartInitException e ) {
		    	
		    }
		}
	}
	
	// The following methods create the actions, content provider, label provider, filters,
	// and sorters of the Product Explorer view
	
	@Override
	protected ActionGroup createActionGroup() {
		
		return new ProductExplorerViewActionGroup(this.getViewer());
	}

	@Override
	protected IContentProvider createContentProvider() {

		return new ProductsContentProvider(
				new ResourceItemProviderAdapterFactory());
	}

	@Override
	protected IBaseLabelProvider createLabelProvider() {
		
		return new ProductsLabelProvider();
	}
	
	@Override
	protected ViewerFilter[] createFilters() {
		
		ViewerFilter[] filters = new ViewerFilter[1];
		filters[0] = new DomainsFilter();
		
		return filters;
	}

	@Override
	protected ViewerSorter createSorter() {
		
		return null;
	}
	
	/*
	 * This method updates the content description of the Product Explorer view. This description is
	 * shown as a textual label on the top part of the view and it informs the user about the roles
	 * that are currently selected (which establish a filter for the view)
	 */
	public void updateContentDescription() {
		
		String description = "Selected roles:";
		
		if (Context.currentRoles.size() == 0) {
			description += " <None>";
		}
		else if (Context.currentRoles.size() == 1
				&& Context.currentRoles.get(0) instanceof NullRole) {
			
			description += " <None>";
		}
		else {
			for(RoleDescriptor rd : Context.currentRoles) {
				description += " " + getName(rd) + ",";
			}
			
			description = description.substring(0, description.length() - 1);
		}
		
		this.setContentDescription(description);
	}
	
	private String getName(RoleDescriptor rd) {
		
		if(rd.getPresentationName() != null && !rd.getPresentationName().equals("")) {
			return rd.getPresentationName();
		}
		else return rd.getName();
	}
}
