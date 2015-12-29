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
package moskitt4me.repositoryClient.core.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.handlers.CollapseAllHandler;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * This action delegate collapses all expanded elements in a Navigator view.
 */
public class CollapseAllAction extends Action implements IAction {

	private final CommonViewer commonViewer;

	/**
	 * Create the CollapseAll action.
	 * 
	 * @param aViewer
	 *            The viewer to be collapsed.
	 */
	public CollapseAllAction(CommonViewer aViewer) {
		super("Collapse all");
		setToolTipText("Collapse all");
		setActionDefinitionId(CollapseAllHandler.COMMAND_ID);
		commonViewer = aViewer;
	}

	public void run() {
		if (commonViewer != null) {
			commonViewer.collapseAll();
		}
	}
}
