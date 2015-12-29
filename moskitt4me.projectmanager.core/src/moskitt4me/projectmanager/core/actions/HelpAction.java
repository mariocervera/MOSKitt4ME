package moskitt4me.projectmanager.core.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

public class HelpAction extends Action implements IAction {

	public HelpAction() {
		
		super("Help");
	}

	public void run() {
		
		String message = "Colours indicate states of tasks and activities:\n\n" +
		"\t- Green: Executable.\n" + 
		"\t- Red: Non-executable.\n" +
		"\t- Blue: Executed.\n" +
		"\t- Grey: Optional.";
		
		MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				"Information", message);
	}
}
