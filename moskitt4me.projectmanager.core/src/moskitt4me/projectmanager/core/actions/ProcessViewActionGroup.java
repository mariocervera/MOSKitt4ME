package moskitt4me.projectmanager.core.actions;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IActionBars;

/**
 * This class defines the actions that appear in the Action Bar of the Process view
 * and are not common to all the other views in the Project Manager Component.
 *
 * @author Mario Cervera
 */
public class ProcessViewActionGroup extends ProjectManagerActionGroup {

	// Actions
	
	private AllTasksAction allTasksAction;
	private RoleSelectionAction roleSelectionAction;
	private UndoAction undoAction;
	private RunRepeatableAction runRepeatableAction;
	private RunAction runAction;
	private HelpAction helpAction;
	
	/*
	 * Constructor
	 */
	public ProcessViewActionGroup(TreeViewer viewer) {
		
		super(viewer);
		
		makeActions();
	}

	private void makeActions() {
		
		//All Tasks Action
		
		allTasksAction = new AllTasksAction();
		ImageDescriptor imageDescriptor = getImageDescriptor("icons/full/obj16/Tasks.gif");
		allTasksAction.setImageDescriptor(imageDescriptor);
		allTasksAction.setToolTipText("All Tasks");
		
		//Role Selection Action
		
		roleSelectionAction = new RoleSelectionAction();
		ImageDescriptor imageDescriptor2 = getImageDescriptor("icons/full/obj16/Role.gif");
		roleSelectionAction.setImageDescriptor(imageDescriptor2);
		roleSelectionAction.setToolTipText("Role selection");
		
		//Undo Action
		
		undoAction = new UndoAction();
		ImageDescriptor imageDescriptor3 = getImageDescriptor("icons/full/elcl16/undo.gif");
		undoAction.setImageDescriptor(imageDescriptor3);
		undoAction.setToolTipText("Undo");
		
		//Run Repeatable Action
		
		runRepeatableAction = new RunRepeatableAction();
		ImageDescriptor imageDescriptor4 = getImageDescriptor("icons/full/elcl16/runRepeatable.gif");
		runRepeatableAction.setImageDescriptor(imageDescriptor4);
		runRepeatableAction.setToolTipText("Run Repeatable");
		
		//Run Action
		
		runAction = new RunAction();
		ImageDescriptor imageDescriptor5 = getImageDescriptor("icons/full/elcl16/run.gif");
		runAction.setImageDescriptor(imageDescriptor5);
		runAction.setToolTipText("Run");
		
		//Help Action
		
		helpAction = new HelpAction();
		ImageDescriptor imageDescriptor6 = getImageDescriptor("icons/full/elcl16/help.gif");
		helpAction.setImageDescriptor(imageDescriptor6);
		helpAction.setToolTipText("Help");
	}
	
	@Override
	public void fillActionBars(IActionBars actionBars) {
		
		super.fillActionBars(actionBars);
		
		actionBars.getToolBarManager().add(allTasksAction);
		actionBars.getToolBarManager().add(roleSelectionAction);
		actionBars.getToolBarManager().add(undoAction);
		actionBars.getToolBarManager().add(runRepeatableAction);
		actionBars.getToolBarManager().add(runAction);
		actionBars.getToolBarManager().add(helpAction);
	}
	
}
