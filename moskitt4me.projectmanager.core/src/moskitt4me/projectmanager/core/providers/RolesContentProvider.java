package moskitt4me.projectmanager.core.providers;

import java.util.ArrayList;
import java.util.List;

import moskitt4me.projectmanager.core.actions.RoleSelectionAction;
import moskitt4me.projectmanager.methodspecification.MethodElements;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.epf.uma.Role;
import org.eclipse.epf.uma.RoleDescriptor;
import org.eclipse.epf.uma.RoleSet;
import org.eclipse.epf.uma.RoleSetGrouping;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * A Content Provider for the Role Selection Dialog
 * 
 * @author Mario Cervera
 */
public class RolesContentProvider extends ArrayContentProvider implements
		ITreeContentProvider {

	/*
	 * Returns the root elements: Role Set Groupings, Role Sets, and Roles
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		
		if(inputElement instanceof Object[]) {
			
			List<Object> rootElements = new ArrayList<Object>();
			
			rootElements.add(new RoleSelectionAction.NullRole());
			
			if(((Object[])inputElement).length > 0) {
				rootElements.addAll(MethodElements.roleSetGroupings);
				
				for(RoleSet rs : MethodElements.roleSets) {
					if(!isContainedInRoleSetGrouping(rs)) {
						rootElements.add(rs);
					}
				}
				
				for(RoleDescriptor r : filterRoles(MethodElements.roleDescriptors)) {
					if(!isContainedInRoleSet(r)) {
						rootElements.add(r);
					}
				}
			}
			
			return rootElements.toArray();
		}
		
		return super.getElements(inputElement);
	}
	
	/*
	 * This method returns true if the given element has children
	 */
	public boolean hasChildren(Object element) {
		
		if(element instanceof RoleDescriptor ||
				element instanceof RoleSelectionAction.NullRole) {
			
			return false;
		}
		else return true;
	}
	
	public Object getParent(Object element) {
		
		return null;
	}
	
	/*
	 * Returns the children of a given element
	 */
	public Object[] getChildren(Object parentElement) {
		
		if(parentElement instanceof RoleSetGrouping) {
			return ((RoleSetGrouping) parentElement).getRoleSets().toArray();
		}
		else if(parentElement instanceof RoleSet) {
			List<RoleDescriptor> children = new ArrayList<RoleDescriptor>();
			for(Role r : ((RoleSet) parentElement).getRoles()) {
				List<RoleDescriptor> descriptors = MethodElements.roleDescriptorMap.get(r);
				if(descriptors != null && descriptors.size() > 0) {
					children.add(descriptors.get(0));
				}
			}
			return children.toArray();
		}
		
		return null;
	}

	private boolean isContainedInRoleSetGrouping(RoleSet rs) {
		
		for(RoleSetGrouping rsg : MethodElements.roleSetGroupings) {
			if(rsg.getRoleSets().contains(rs)) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean isContainedInRoleSet(RoleDescriptor rd) {
		
		for(RoleSet rs : MethodElements.roleSets) {
			for(Role r : rs.getRoles()) {
				List<RoleDescriptor> descriptors = MethodElements.roleDescriptorMap.get(r);
				if(descriptors != null && descriptors.contains(rd)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private List<RoleDescriptor> filterRoles(List<RoleDescriptor> roles) {
		
		List<RoleDescriptor> result = new ArrayList<RoleDescriptor>();
		
		for(RoleDescriptor rd : roles) {
			
			boolean contained = false;
			
			int i = 0;
			while(i < result.size() && !contained) {
				RoleDescriptor rd2 = result.get(i);
				
				if(rd.getRole().eIsProxy() && rd2.getRole().eIsProxy()) {
					URI proxyURI = ((InternalEObject)rd.getRole()).eProxyURI();
					URI proxyURI2 = ((InternalEObject)rd2.getRole()).eProxyURI();
					
					if(proxyURI.fragment() != null && proxyURI2.fragment() != null &&
							proxyURI.fragment().equals(proxyURI2.fragment())) {
						
						contained = true;
					}
				}
				i++;
			}
			
			if(!contained) {
				result.add(rd);
			}
		}
		
		return result;
	}
}
