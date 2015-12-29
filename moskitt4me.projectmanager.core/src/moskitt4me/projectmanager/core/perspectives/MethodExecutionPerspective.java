package moskitt4me.projectmanager.core.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IViewRegistry;

public class MethodExecutionPerspective implements IPerspectiveFactory {

	public static final String ID = "moskitt4me.projectmanager.core.perspective";
	
	public void createInitialLayout(IPageLayout layout) {

		String editorArea = layout.getEditorArea();

		IFolderLayout left = layout.createFolder("left", IPageLayout.LEFT,
				0.2f, editorArea);
		
		String explorerViewId = "";
		if(existsView("es.cv.gvcase.ide.navigator.resourceView")) {
			explorerViewId = "es.cv.gvcase.ide.navigator.resourceView";
		}
		else {
			explorerViewId = "org.eclipse.ui.navigator.ProjectExplorer";
		}
		
		left.addView(explorerViewId);
		left.addPlaceholder(explorerViewId);
		
		IFolderLayout leftMiddle = layout.createFolder("leftBottom", IPageLayout.BOTTOM,
				0.5f, "left");
		leftMiddle.addView("moskitt4me.projectmanager.core.views.processView");
		leftMiddle.addPlaceholder("moskitt4me.projectmanager.core.views.processView");
		
		IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM,
				0.7f, editorArea);
		bottom.addView("moskitt4me.projectmanager.core.views.productExplorerView");
		bottom.addPlaceholder("moskitt4me.projectmanager.core.views.productExplorerView");
		
		IFolderLayout bottomRight = layout.createFolder("right", IPageLayout.RIGHT,
				0.5f, "right");
		bottomRight.addView("org.eclipse.help.ui.HelpView");
		bottomRight.addPlaceholder("org.eclipse.help.ui.HelpView");
	}
	

	private boolean existsView(String viewId) {

		IViewRegistry registry = PlatformUI.getWorkbench().getViewRegistry();

		if(registry != null && registry.find(viewId) != null) {
			return true;
		}

		return false;
	}
}
