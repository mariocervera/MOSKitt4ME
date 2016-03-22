package moskitt4me.repositoryClient.core.dialogs;

import moskitt4me.repositoryClient.core.composites.InternalToolPluginsComposite;
import moskitt4me.repositoryClient.core.util.InternalToolFragment;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/*
* A dialog that allows the user to define internal tools. An internal tool is a tool that is already
* installed in MOSKitt4ME (e.g., Eclipse frameworks such as GMF or EMF). This type of technical fragments
* do not need to encapsulate the implementation of the tools; only the identifiers of the Eclipse plug-ins.
*
* @author Mario Cervera
*/
public class DefineInternalToolDialog extends Dialog {

	//Fragment properties (name, origin, objective, etc.)

	private String name;
	private Text nameText;
	
	private String origin;
	private Text originText;
	
	private String objective;
	private Text objectiveText;
	
	private String input;
	private Text inputText;
	
	private String output;
	private Text outputText;
	
	private String description;
	private Text descriptionText;
	
	private InternalToolPluginsComposite pluginsComposite;
	
	//Ok and Cancel buttons
	
	protected Button okButton;
	protected Button cancelButton;
	
	//The fragment to be stored in the repository
	
	private InternalToolFragment result;
	
	public DefineInternalToolDialog(Shell parentShell) {
		
		super(parentShell);
		
		this.name = "";
		this.origin = "";
		this.objective = "";
		this.input = "";
		this.output = "";
		this.description = "";
	}

	public String getName() {
		return name;
	}
	
	public String getOrigin() {
		return origin;
	}
	
	public String getObjective() {
		return objective;
	}
	
	public String getInput() {
		return input;
	}
	
	public String getOutput() {
		return output;
	}
	
	public String getDescription() {
		return description;
	}
	
	public InternalToolFragment getResult() {
		return result;
	}
	
	public Button getOkButton() {
		return okButton;
	}
	
	protected void configureShell(Shell shell) {
        
		super.configureShell(shell);
        
        shell.setText("Define Internal Tool");
		
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
        
    	okButton= createButton(parent, IDialogConstants.OK_ID,
                IDialogConstants.OK_LABEL, true);
        
    	okButton.setEnabled(false);
    	
    	cancelButton = createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
		
	}
	
	protected Control createDialogArea(Composite parent) {

		Composite composite = (Composite) super.createDialogArea(parent);
	        GridLayout compositeLayout = new GridLayout(2, false);
	        composite.setLayout(compositeLayout);
	        
	        Label nameLabel = new Label(composite, SWT.NONE);
	        nameLabel.setText("Name:");
	        GridData gd = new GridData();
	        nameLabel.setLayoutData(gd);
	        
	        nameText = new Text(composite, SWT.BORDER);
	        GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
	        gd2.widthHint = 350;
	        nameText.setLayoutData(gd2);
        
	        Label originLabel = new Label(composite, SWT.NONE);
	        originLabel.setText("Origin:");
	        GridData gd3 = new GridData();
	        nameLabel.setLayoutData(gd3);
	        
	        originText = new Text(composite, SWT.BORDER);
	        GridData gd4 = new GridData(GridData.FILL_HORIZONTAL);
	        originText.setLayoutData(gd4);
	        
	        Label objectiveLabel = new Label(composite, SWT.NONE);
	        objectiveLabel.setText("Objective:");
	        GridData gd5 = new GridData();
	        objectiveLabel.setLayoutData(gd5);
	        
	        objectiveText = new Text(composite, SWT.BORDER);
	        GridData gd6 = new GridData(GridData.FILL_HORIZONTAL);
	        objectiveText.setLayoutData(gd6);
	        
	        Label inputLabel = new Label(composite, SWT.NONE);
	        inputLabel.setText("Input:");
	        GridData gd7 = new GridData();
	        inputLabel.setLayoutData(gd7);
        
	        inputText = new Text(composite, SWT.BORDER);
	        GridData gd8 = new GridData(GridData.FILL_HORIZONTAL);
	        inputText.setLayoutData(gd8);
	        
	        Label outputLabel = new Label(composite, SWT.NONE);
	        outputLabel.setText("Output:");
	        GridData gd9 = new GridData();
	        outputLabel.setLayoutData(gd9);
	        
	        outputText = new Text(composite, SWT.BORDER);
	        GridData gd10 = new GridData(GridData.FILL_HORIZONTAL);
	        outputText.setLayoutData(gd10);
	        
	        Label descriptionLabel = new Label(composite, SWT.NONE);
	        descriptionLabel.setText("Description:");
	        GridData gd11 = new GridData();
	        descriptionLabel.setLayoutData(gd11);
        
	        descriptionText = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
	        GridData gd12 = new GridData(GridData.FILL_BOTH);
	        gd12.heightHint = 100;
	        descriptionText.setLayoutData(gd12);
	        
	        pluginsComposite = new InternalToolPluginsComposite(composite, SWT.NONE, this);
	        GridData gd13 = new GridData(GridData.FILL_BOTH);
	        gd13.horizontalSpan = 2;
	        pluginsComposite.setLayoutData(gd13);
	        
	        hookListeners();
	        
	        return composite;
	}
	
	protected void hookListeners() {
		
		nameText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				
				name = nameText.getText();
				enableOkButton();
			}
		});
		
		originText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				
				origin = originText.getText();
				enableOkButton();
			}
		});
		
		objectiveText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				
				objective = objectiveText.getText();
				enableOkButton();
			}
		});
		
		inputText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				
				input = inputText.getText();
				enableOkButton();
			}
		});
		
		outputText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				
				output = outputText.getText();
				enableOkButton();
			}
		});

		descriptionText.addModifyListener(new ModifyListener() {
	
			public void modifyText(ModifyEvent e) {
				
				description = descriptionText.getText();
				enableOkButton();
			}
		});
	}

	public void enableOkButton() {
	
		if(!name.equals("") && !name.contains(" ") && !origin.equals("")
				&& !objective.equals("") && !input.equals("") &&
				!output.equals("") && !description.equals("") &&
				pluginsComposite.getFragmentPlugins().size() > 0) {
			
			okButton.setEnabled(true);
		}
		else {
			okButton.setEnabled(false);
		}
	}
	
	@Override
	protected void okPressed() {
		
		this.result = new InternalToolFragment(name, origin, objective, input,
				output, description, pluginsComposite.getFragmentPlugins());
		
		super.okPressed();
	}
}
