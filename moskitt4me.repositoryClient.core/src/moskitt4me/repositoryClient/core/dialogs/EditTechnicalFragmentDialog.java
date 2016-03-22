package moskitt4me.repositoryClient.core.dialogs;

import java.util.List;

import moskitt4me.repositoryClient.core.composites.AssetPluginsComposite;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/*
* A dialog to edit the properties of a technical fragment.
*
* @author Mario Cervera
*/
public class EditTechnicalFragmentDialog extends Dialog {
	
	// Fragment properties (name, type, origin, etc.)
	
	private String name;
	private Text nameText;
	
	private String type;
	private CCombo typeCombo;
	
	private String origin;
	private Text originText;
	
	private String objective;
	private Text objectiveText;
	
	private String input;
	private Text inputText;
	
	private String output;
	private Text outputText;
	
	private List<IProject> plugins;
	private AssetPluginsComposite assetPluginsComposite;
	
	public EditTechnicalFragmentDialog(Shell parentShell, String name, String type, 
			String origin, String objective, String input, String output, List<IProject> plugins) {
		
		super(parentShell);
		
		this.name = name;
		this.type = type;
		this.origin = origin;
		this.objective = objective;
		this.input = input;
		this.output = output;
		this.plugins = plugins;
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
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
	
	public List<IProject> getPlugins() {
		return plugins;
	}

	protected void configureShell(Shell shell) {
        
		super.configureShell(shell);
        
        shell.setText("Edit Technical Fragment");
    	}
	
	/*
	* Create graphical elements (Grid layout)
	*/
	protected Control createDialogArea(Composite parent) {

		Composite composite = (Composite) super.createDialogArea(parent);
	        GridLayout compositeLayout = new GridLayout(2, false);
	        composite.setLayout(compositeLayout);
	        
	        Label nameLabel = new Label(composite, SWT.NONE);
	        nameLabel.setText("Name:");
	        GridData gd = new GridData();
	        nameLabel.setLayoutData(gd);
        
	        nameText = new Text(composite, SWT.BORDER);
	        if(this.name != null) {
	        	nameText.setText(this.name);
	        }
	        GridData gd2 = new GridData(GridData.FILL_HORIZONTAL);
	        gd2.widthHint = 250;
	        nameText.setLayoutData(gd2);
	        
	        Label typeLabel = new Label(composite, SWT.NONE);
	        typeLabel.setText("Type:");
	        GridData gd3 = new GridData();
	        typeLabel.setLayoutData(gd3);
        
	        typeCombo = new CCombo(composite, SWT.BORDER);
	        typeCombo.setItems(new String[]{"Graphical Editor", "Meta-Model", 
	        		"Form-based Editor", "Model transformation", "Guidance", "Others"});
	        typeCombo.setVisibleItemCount(6);
	        if(type != null) {
	        	typeCombo.setText(type);
	        }
	        GridData gd4 = new GridData(GridData.FILL_HORIZONTAL);
	        gd4.widthHint = 250;
	        typeCombo.setLayoutData(gd4);
        
	        Label originLabel = new Label(composite, SWT.NONE);
	        originLabel.setText("Origin:");
	        GridData gd5 = new GridData();
	        nameLabel.setLayoutData(gd5);
	        
	        originText = new Text(composite, SWT.BORDER);
	        if(this.origin != null) {
	        	originText.setText(this.origin);
	        }
	        if(this.type != null && this.type.equals("Others")) {
	        	originText.setEnabled(false);
	        	originText.setText("");
	        }
	        GridData gd6 = new GridData(GridData.FILL_HORIZONTAL);
	        gd6.widthHint = 250;
	        originText.setLayoutData(gd6);
	        
	        Label objectiveLabel = new Label(composite, SWT.NONE);
	        objectiveLabel.setText("Objective:");
	        GridData gd7 = new GridData();
	        objectiveLabel.setLayoutData(gd7);
	        
	        objectiveText = new Text(composite, SWT.BORDER);
	        if(this.objective != null) {
	        	objectiveText.setText(this.objective);
	        }
	        if(this.type != null && this.type.equals("Others")) {
	        	objectiveText.setEnabled(false);
	        	objectiveText.setText("");
	        }
	        GridData gd8 = new GridData(GridData.FILL_HORIZONTAL);
	        gd8.widthHint = 250;
	        objectiveText.setLayoutData(gd8);
	        
	        Label inputLabel = new Label(composite, SWT.NONE);
	        inputLabel.setText("Input:");
	        GridData gd9 = new GridData();
	        inputLabel.setLayoutData(gd9);
        
	        inputText = new Text(composite, SWT.BORDER);
	        if(this.input != null) {
	        	inputText.setText(this.input);
	        }
	        if(this.type != null && this.type.equals("Others")) {
	        	inputText.setEnabled(false);
	        	inputText.setText("");
	        }
	        GridData gd10 = new GridData(GridData.FILL_HORIZONTAL);
	        gd10.widthHint = 250;
	        inputText.setLayoutData(gd10);
	        
	        Label outputLabel = new Label(composite, SWT.NONE);
	        outputLabel.setText("Output:");
	        GridData gd11 = new GridData();
	        outputLabel.setLayoutData(gd11);
        
	        outputText = new Text(composite, SWT.BORDER);
	        if(this.output != null) {
	        	outputText.setText(this.output);
	        }
	        if(this.type != null && this.type.equals("Others")) {
	        	outputText.setEnabled(false);
	        	outputText.setText("");
	        }
	        GridData gd12 = new GridData(GridData.FILL_HORIZONTAL);
	        gd12.widthHint = 250;
	        outputText.setLayoutData(gd12);
	        
	        assetPluginsComposite = new AssetPluginsComposite(composite, SWT.NONE, this.plugins);
	        GridData gd13 = new GridData(GridData.FILL_HORIZONTAL);
	        gd13.horizontalSpan = 2;
	        assetPluginsComposite.setLayoutData(gd13);
	        
	        hookListeners();
	        
	        return composite;
	}
	
	protected void hookListeners() {
		
		typeCombo.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				
				String text = typeCombo.getText();
				if(text.equals("Others")) {
					originText.setEnabled(false);
					objectiveText.setEnabled(false);
					inputText.setEnabled(false);
					outputText.setEnabled(false);
				}
				else {
					originText.setEnabled(true);
					objectiveText.setEnabled(true);
					inputText.setEnabled(true);
					outputText.setEnabled(true);
				}
			}
		});
	}
	
	@Override
	protected void okPressed() {
		
		this.name = nameText.getText();
		this.type = typeCombo.getText();
		this.origin = originText.getText();
		this.objective = objectiveText.getText();
		this.input = inputText.getText();
		this.output = outputText.getText();
		this.plugins = assetPluginsComposite.getPlugins();
		
		super.okPressed();
	}
}
