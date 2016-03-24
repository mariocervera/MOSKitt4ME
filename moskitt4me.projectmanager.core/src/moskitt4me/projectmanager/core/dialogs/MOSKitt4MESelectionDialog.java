package moskitt4me.projectmanager.core.dialogs;

import java.util.Iterator;
import java.util.List;

import moskitt4me.projectmanager.core.actions.RoleSelectionAction;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.dialogs.SelectionDialog;

/*
 * This class contains the common functionality of different selection dialogs of
 * the Project Manager Component.
 *
 * @author Mario Cervera
 */
public class MOSKitt4MESelectionDialog extends SelectionDialog {

	/**
	 * The default width of a dialog
	 */
	private int DEFAULT_DIALOG_WIDTH = 400;

	/**
	 * The default height of a dialog
	 */
	private int DEFAULT_DIALOG_HEIGHT = 300;

	/**
	 * The minimum width of a dialog
	 */
	private int MIN_DIALOG_WIDTH = 300;

	/**
	 * The minimum height of a dialog
	 */
	private int MIN_DIALOG_HEIGHT = 300;

	private ILabelProvider labelProvider;
	
	private ITreeContentProvider treeContentProvider;
	
	private ViewerFilter filter;
	
	private Tree tree;
	private TreeViewer treeViewer;

	private Object[] objects;
	
	private int treeStyle;
	
	/*
	 * The types for which the OK button is enabled
	 */
	private List<EClass> enablementTypes;
	
	public MOSKitt4MESelectionDialog(Shell parentShell, String title,
			String message, Object[] objects, int treeStyle) {
		
		super(parentShell);
		
		setTitle(title);
		setMessage(message);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		
		this.objects = objects;
		
		this.treeStyle = treeStyle;
	}
	
	protected void configureShell(Shell shell) {
		shell.setMinimumSize(MIN_DIALOG_WIDTH, MIN_DIALOG_HEIGHT);

		super.configureShell(shell);
	}

	protected Control createDialogArea(Composite parent) {
		
		Composite dialogComposite = (Composite) super.createDialogArea(parent);

		GridLayout dialogLayout = new GridLayout();
		dialogLayout.marginWidth = 10;
		dialogLayout.marginHeight = 10;
		GridData dialogLayoutData = new GridData(GridData.FILL_BOTH);
		dialogLayoutData.widthHint = DEFAULT_DIALOG_WIDTH;
		dialogLayoutData.heightHint = DEFAULT_DIALOG_HEIGHT;
		dialogComposite.setLayout(dialogLayout);
		dialogComposite.setLayoutData(dialogLayoutData);

		tree = new Tree(dialogComposite, treeStyle);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		treeViewer = new TreeViewer(tree);
		if(filter != null) {
			treeViewer.addFilter(filter);
		}
		treeViewer.setLabelProvider(this.labelProvider);
		treeViewer.setContentProvider(this.treeContentProvider);
		treeViewer.setSorter(null);
		treeViewer.setInput(this.objects);
		
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@SuppressWarnings("unchecked")
			public void selectionChanged(SelectionChangedEvent event) {

				if (event.getSelection() instanceof StructuredSelection) {
					
					StructuredSelection sel = (StructuredSelection) event.getSelection();
					
					if (sel.size() <= 0) {
						getOkButton().setEnabled(false);
					}
					else if (sel.size() > 1 && containsNullRole(sel)) {
						getOkButton().setEnabled(false);
					} 
					else if (getEnablementTypes() == null
							|| getEnablementTypes().size() == 0) {

						getOkButton().setEnabled(true);
					}
					else {
						Iterator it = sel.iterator();

						// Look for an object which is not instance of any enablement type
						boolean found = false;

						while (it.hasNext() && !found) {

							Object obj = it.next();

							// Is "obj" instance of one of the enablement types?

							boolean found2 = false;
							int i = 0;
							List<EClass> types = getEnablementTypes();
							while (i < types.size() && !found2) {
								EClass type = types.get(i);
								if (type.isInstance(obj)) {
									found2 = true;
								}
								i++;
							}

							if (!found2) {
								// If "obj" is not instance of any enablement type,
								// the object we were looking for has been found
								found = true;
							}
						}

						if (found) {
							getOkButton().setEnabled(false);
						} else {
							getOkButton().setEnabled(true);
						}
					}
				}
			}
		});
		
		return dialogComposite;
	}
	
	public void setLabelProvider(ILabelProvider provider) {
		this.labelProvider = provider;
	}
	
	public void setContentProvider(ITreeContentProvider contentProvider) {
		this.treeContentProvider = contentProvider;
	}
	
	public void setFilter(ViewerFilter filter) {
		this.filter = filter;
	}
	
	public void setEnablementTypes(List<EClass> types) {
		
		this.enablementTypes = types;
	}
	
	public List<EClass> getEnablementTypes() {
		
		return this.enablementTypes;
	}
	
	@SuppressWarnings("unchecked")
	public boolean containsNullRole(StructuredSelection sel) {
		
		Iterator it = sel.iterator();
		
		while(it.hasNext()) {
			Object obj = it.next();
			
			if(obj instanceof RoleSelectionAction.NullRole) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	protected void okPressed() {
	
		if(treeViewer.getSelection() instanceof StructuredSelection) {
			StructuredSelection selection = (StructuredSelection) treeViewer.getSelection();
			setResult(selection.toList());
		}
		
		super.okPressed();
	}
}
