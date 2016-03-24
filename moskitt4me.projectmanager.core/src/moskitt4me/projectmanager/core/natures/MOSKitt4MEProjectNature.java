package moskitt4me.projectmanager.core.natures;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * The Project Nature for the projects of type "MOSKitt4ME".
 * 
 * @author Mario Cervera
 */
public class MOSKitt4MEProjectNature implements IProjectNature {

	/** The project. */
    	
    	private IProject project;
	
	/*
	* (non-Javadoc)
	* @see org.eclipse.core.resources.IProjectNature#configure()
	*/
	public void configure() throws CoreException {

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject() {
		return this.project;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project) {
		this.project = project;
	}

}
