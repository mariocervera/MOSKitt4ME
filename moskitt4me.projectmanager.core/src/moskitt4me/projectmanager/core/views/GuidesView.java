package moskitt4me.projectmanager.core.views;

import moskitt4me.projectmanager.core.actions.GuidesViewActionGroup;
import moskitt4me.projectmanager.core.providers.GuidesContentProvider;
import moskitt4me.projectmanager.core.providers.GuidesLabelProvider;
import moskitt4me.projectmanager.methodspecification.MethodElements;

import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.epf.authoring.ui.views.ContentView;
import org.eclipse.epf.uma.Guidance;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionGroup;

/**
 * The Guides view provides MOSKitt4ME users with guidelines on the performance of the
 * method tasks. Specifically, the Guides view shows the elements of type "Guidance" that
 * are associated to the method task that is selected in the Process view. These elements
 * are obtained from the method model at runtime.
 * 
 * @author Mario Cervera
 */
public class GuidesView extends ProjectManagerViewPart {

	public static final String GuidesViewId = "moskitt4me.projectmanager.core.views.guidesView";
	
	@Override
	public void createPartControl(Composite parent) {
		
		super.createPartControl(parent);
		
		if(getViewer() != null) {
			
			getViewer().addSelectionChangedListener(new ISelectionChangedListener() {
				
				public void selectionChanged(SelectionChangedEvent event) {
					
					if(event.getSelection() instanceof StructuredSelection) {
						Object firstElement = ((StructuredSelection)event.getSelection()).getFirstElement();
						if(firstElement instanceof Guidance) {
							Guidance g = (Guidance) firstElement;
							ContentView cv = getContentView();
							if(cv != null) {
								MethodElements.resolveContentDescription(g);
								cv.displayHTMLContentPage(g);
							}
						}
					}
				}
			});
		}
	}
	
	@Override
	protected void handleDoubleClick(DoubleClickEvent anEvent) {
		
		super.handleDoubleClick(anEvent);
		
		if(anEvent.getSelection() instanceof StructuredSelection) {
			StructuredSelection selection = (StructuredSelection) anEvent.getSelection();
			if(selection.getFirstElement() instanceof Guidance) {
				Guidance g = (Guidance) selection.getFirstElement();
				ContentView cv = getContentView();
				if(cv == null) {
					displayContentView();
					cv = getContentView();
				}
				if(cv != null) {
					MethodElements.resolveContentDescription(g);
					cv.displayHTMLContentPage(g);
				}
			}
		}
	}
	
	private void displayContentView() {

		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();

		try {
			page.showView(ContentView.VIEW_ID);
			
		} catch (Exception e) {

		}
	}
	
	private ContentView getContentView() {
		
		IViewPart viewPart = PlatformUI.getWorkbench()
		.getActiveWorkbenchWindow().getActivePage().findView(
				ContentView.VIEW_ID);

		if (viewPart instanceof ContentView) {
			ContentView cv = (ContentView) viewPart;
			return cv;
		}
		
		return null;
	}
	
	@Override
	protected ActionGroup createActionGroup() {
		
		return new GuidesViewActionGroup(this.getViewer());
	}

	@Override
	protected IContentProvider createContentProvider() {

		return new GuidesContentProvider(
				new ResourceItemProviderAdapterFactory());
	}

	@Override
	protected ViewerFilter[] createFilters() {
		
		return new ViewerFilter[0];
	}

	@Override
	protected IBaseLabelProvider createLabelProvider() {
		
		return new GuidesLabelProvider();
	}

	@Override
	protected ViewerSorter createSorter() {
		
		return null;
	}
}
