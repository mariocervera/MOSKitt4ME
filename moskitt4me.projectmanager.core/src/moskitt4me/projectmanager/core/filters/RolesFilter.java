package moskitt4me.projectmanager.core.filters;

import java.util.List;

import moskitt4me.projectmanager.methodspecification.MethodElements;

import org.eclipse.epf.uma.Role;
import org.eclipse.epf.uma.RoleDescriptor;
import org.eclipse.epf.uma.RoleSet;
import org.eclipse.epf.uma.RoleSetGrouping;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class RolesFilter extends ViewerFilter {

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		
		if(element instanceof RoleSetGrouping) {
			RoleSetGrouping roleSetGrouping = (RoleSetGrouping) element;
			if(!containsRolesToShow(roleSetGrouping)) {
				return false;
			}
		}
		else if(element instanceof RoleSet) {
			RoleSet roleSet = (RoleSet) element;
			if(!containsRolesToShow(roleSet)) {
				return false;
			}
		}
		
		return true;
	}

	private boolean containsRolesToShow(RoleSetGrouping roleSetGrouping) {
		
		for(RoleSet roleSet : roleSetGrouping.getRoleSets()) {
			if(containsRolesToShow(roleSet)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean containsRolesToShow(RoleSet roleSet) {
		
		for(Role r : roleSet.getRoles()) {
			List<RoleDescriptor> descriptors = MethodElements.roleDescriptorMap.get(r);
			if(descriptors != null && descriptors.size() > 0) {
				return true;
			}
		}
		
		return false;
	}
}
