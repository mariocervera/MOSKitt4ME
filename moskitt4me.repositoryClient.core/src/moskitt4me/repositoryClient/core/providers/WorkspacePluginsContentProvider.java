package moskitt4me.repositoryClient.core.providers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * A content provider for the composite of the "Define External Tool Dialog".
 *
 * @author Mario Cervera
 */
public class WorkspacePluginsContentProvider extends ArrayContentProvider implements ITreeContentProvider {

	/*
	 * This method returns the root-level elements of the Tree viewer
	 */
	public Object[] getElements(Object inputElement) {
		
		if(inputElement instanceof List<?>) {
			List<IProject> projects = new ArrayList<IProject>();
			List<?> list = (List<?>) inputElement;
			for(Object obj : list) {
				if(obj instanceof IProject) {
					projects.add((IProject)obj);
				}
			}
			return getPluginProjects(projects).toArray();
		}
		
		return super.getElements(inputElement);
	}

	
	public Object getParent(Object element) {
		
		return null;
	}

	/*
	 * The elements of the Tree Viewers do not have children
	 */
	public boolean hasChildren(Object element) {
		
		return false;
	}
	
	/*
	 * The elements of the Tree Viewers do not have children
	 */
	public Object[] getChildren(Object parentElement) {
		
		return null;
	}
	
	/*
	* Gets the plug-in projects of the MOSKitt4ME workspace
	*/
	private List<IProject> getPluginProjects(List<IProject> projs) {
		
		List<IProject> pluginProjects = new ArrayList<IProject>();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		
		IProject[] projects = root.getProjects();
		
		for(IProject project : projects) {
			try {
				if(project.hasNature("org.eclipse.pde.PluginNature") 
						&& !hasErrors(project) && !projs.contains(project)) {
					pluginProjects.add(project);
				}
			}
			catch(CoreException e) {
				
			}
		}
		
		return pluginProjects;
	}
	
	/*
	* Calculates whether a Plug-in project of the workspace has compilation errors
	*/
	private boolean hasErrors(IProject project) {

		try {
			IMarker[] markers = project.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER,
					true, IResource.DEPTH_INFINITE);
			
			for (IMarker marker : markers) {
				Integer severityType = (Integer) marker.getAttribute(IMarker.SEVERITY);
				if (severityType.intValue() == IMarker.SEVERITY_ERROR) {
					return true;
				}
			}
			
			return false;
		} 
		catch (Exception e) {
			return true;
		}
	}
}
