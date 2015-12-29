package moskitt4me.toolgenerator.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ToolGenerationDialog extends Dialog {
	
	protected Label destinationLabel;
	protected Text destinationText;
	protected Button destinationButton;
	
	protected Label productRootLabel;
	protected Text productRootText;
	
	protected Button okButton;
	protected Button cancelButton;
	
	private String destination;
	private String productRoot;
	
	public ToolGenerationDialog(Shell parent) {
		
		super(parent);
		
		this.destination = "";
		this.productRoot = "";
	}
	
	public String getDestination() {
		
		return destination;
	}
	
	public String getProductRoot() {
		
		return productRoot;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		
		getShell().setText("CASE tool generation");
		
		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout layout = (GridLayout) composite.getLayout();
		layout.numColumns = 2;
		
		destinationLabel = new Label(composite, SWT.NONE);
		destinationLabel.setText("Select a destination directory for the CASE tool:");
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		destinationLabel.setLayoutData(gridData);
		destinationText = new Text(composite, SWT.BORDER);
		GridData gridData2 = new GridData(GridData.FILL_HORIZONTAL);
		gridData2.widthHint = 250;
		destinationText.setLayoutData(gridData2);
		destinationButton = new Button(composite, SWT.PUSH);
		destinationButton.setText("Browse...");
		destinationButton.setLayoutData(new GridData());
		
		productRootLabel = new Label(composite, SWT.NONE);
		productRootLabel.setText("CASE tool name: ");
		GridData gridData3 = new GridData(GridData.FILL_HORIZONTAL);
		gridData3.horizontalSpan = 2;
		productRootLabel.setLayoutData(gridData3);
		productRootText = new Text(composite, SWT.BORDER);
		GridData gridData4 = new GridData(GridData.FILL_HORIZONTAL);
		gridData4.horizontalSpan = 2;
		productRootText.setLayoutData(gridData4);
		
		hookListeners();
		
		return composite;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
	
		okButton = createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, true);
		
		okButton.setEnabled(false);

		cancelButton = createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}
	
	protected void hookListeners() {
		
		destinationButton.addSelectionListener(new SelectionAdapter() {
			
			public void widgetSelected(SelectionEvent e) {
				
				DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SAVE);
				dialog.setText("Destination directory");
				dialog.setMessage("Select a destination directory for the CASE tool");
				String res = dialog.open();
				if (res != null) {
					destinationText.setText(res);
					destination = res;
					enableOkButton();
				}
			}
		});
		
		productRootText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				
				productRoot = productRootText.getText();
				
				enableOkButton();
			}
		});
	}
	
	private void enableOkButton() {
		
		if(destination != null && productRoot != null &&
				!destination.equals("") && !productRoot.equals("")) {
			
			okButton.setEnabled(true);
		}
		else {
			okButton.setEnabled(false);
		}
	}
}
