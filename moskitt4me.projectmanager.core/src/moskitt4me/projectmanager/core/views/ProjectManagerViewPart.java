package moskitt4me.projectmanager.core.views;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.part.ViewPart;

/*
 * This class contains the common functionality of the different views of
 * the project manager
 */
public abstract class ProjectManagerViewPart extends ViewPart {

	private TreeViewer treeViewer;
	
	private ActionGroup actionGroup;
	
	@Override
	public void createPartControl(Composite parent) {
		
		treeViewer = new TreeViewer(parent, getTreeStyle());
		treeViewer.setContentProvider(createContentProvider());
		treeViewer.setFilters(createFilters());
		treeViewer.setLabelProvider(createLabelProvider());
		treeViewer.setSorter(createSorter());
		treeViewer.setInput(getInitialInput());
		
		initListeners();
		
		actionGroup = createActionGroup();
		if(actionGroup != null) {
			actionGroup.fillActionBars(getViewSite().getActionBars());
		}
		
		initContextMenu();
	}

	@Override
	public void setFocus() {
		if (treeViewer != null) {
			treeViewer.getTree().setFocus();
		}
		
	}
	
	protected void initListeners() {

		treeViewer.addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent event) {
				try {
					handleDoubleClick(event);
				} catch (RuntimeException re) {
					re.printStackTrace();
				}
			}
		});
	}
	
	protected Object getInitialInput() {
		return getSite().getPage().getInput();
	}
	
	protected void handleDoubleClick(DoubleClickEvent anEvent) {
		//Do nothing
	}
	
	public TreeViewer getViewer() {
		return treeViewer;
	}
	
	@Override
	public void dispose() {
		
		if(treeViewer != null && treeViewer.getTree() != null) {
			treeViewer.getTree().dispose();
		}
		if (actionGroup != null) {
			actionGroup.dispose();
		}
		
		super.dispose();
	}
	
	protected void initContextMenu() {
		MenuManager menuMgr = new MenuManager("projectmanager.views.productExplorerView.popupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {

			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		
		Menu menu = menuMgr.createContextMenu(treeViewer.getTree());
		treeViewer.getTree().setMenu(menu);
	}
	
	protected void fillContextMenu(IMenuManager menuMgr) {
		
		//Do nothing
	}
	
	public void refreshViewer() {
		
		if (treeViewer != null && !treeViewer.getTree().isDisposed()) {
			treeViewer.refresh();
		}
	}
	
	protected int getTreeStyle() {
		
		return SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL;
	}
	
	protected abstract ActionGroup createActionGroup();
	
	protected abstract IContentProvider createContentProvider();
	
	protected abstract ViewerFilter[] createFilters();
	
	protected abstract IBaseLabelProvider createLabelProvider();
	
	protected abstract ViewerSorter createSorter();
}
