package moskitt4me.projectmanager.core.pages;

import moskitt4me.projectmanager.core.Activator;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class MOSKitt4MEPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		
		/*
		 * "ExternalLibraryPath" stores the path of a Method Library that contains
		 * delivery processes that can be used by the PMC without being necessarily contained
		 * in the plugin es.cv.gvcase.gvm.configuration
		 */
		addField(new FileFieldEditor("ExternalLibraryPath", "External Library Path:",
				getFieldEditorParent()));
	}

}
