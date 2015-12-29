/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package moskitt4me.projectmanager.core.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.handlers.CollapseAllHandler;

/**
 * This action delegate collapses all expanded elements in a Navigator view.
 */
public class CollapseAllAction extends Action implements IAction {

	private final TreeViewer treeViewer;

	/**
	 * Create the CollapseAll action.
	 * 
	 * @param aViewer
	 *            The viewer to be collapsed.
	 */
	public CollapseAllAction(TreeViewer aViewer) {
		super("Collapse all");
		setToolTipText("Collapse all");
		setActionDefinitionId(CollapseAllHandler.COMMAND_ID);
		treeViewer = aViewer;
	}

	public void run() {
		if (treeViewer != null) {
			treeViewer.collapseAll();
		}
	}
}
