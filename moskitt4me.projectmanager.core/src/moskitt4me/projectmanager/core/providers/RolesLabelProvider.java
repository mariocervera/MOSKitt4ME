package moskitt4me.projectmanager.core.providers;

import java.io.IOException;

import moskitt4me.projectmanager.core.Activator;
import moskitt4me.projectmanager.core.actions.RoleSelectionAction;
import moskitt4me.projectmanager.methodspecification.MethodElements;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.epf.uma.Role;
import org.eclipse.epf.uma.RoleDescriptor;
import org.eclipse.epf.uma.RoleSet;
import org.eclipse.epf.uma.RoleSetGrouping;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * A Label Provider for the Role Selection Dialog
 * 
 * @author Mario Cervera
 */
public class RolesLabelProvider extends LabelProvider {

	/*
	 * This method returns the textual label of a given element
	 */
	@Override
	public String getText(Object element) {
		
		if(element instanceof RoleSelectionAction.NullRole) {
			return "<None>";
		}
		else if(element instanceof RoleDescriptor) {
			RoleDescriptor rd = (RoleDescriptor) element;
			Role r = rd.getRole();
			if(r.eIsProxy()) {
				r = MethodElements.getRole(r);
			}
			if(r != null) {
				return r.getPresentationName() != null ? r.getPresentationName() : r.getName();
			}
			else {
				return rd.getPresentationName() != null ? rd.getPresentationName() : rd.getName();
			}
		}
		else if(element instanceof RoleSet) {
			RoleSet rs = (RoleSet) element;
			return rs.getPresentationName() != null ? rs.getPresentationName() : rs.getName();
		}
		else if(element instanceof RoleSetGrouping) {
			RoleSetGrouping rsg = (RoleSetGrouping) element;
			return rsg.getPresentationName() != null ? rsg.getPresentationName() : rsg.getName();
		}
		
		return super.getText(element);
	}
	
	/*
	 * This method returns an icon for a given element
	 */
	@Override
	public Image getImage(Object element) {
		
		try {
			String imagePath = FileLocator.toFileURL(Platform.getBundle(
			Activator.PLUGIN_ID).getResource("icons/full/obj16")).getPath();
			
			if(element instanceof RoleDescriptor) {
				Image image = new Image(Display.getCurrent(), imagePath + "Role.gif");
				return image;
			}
			else if(element instanceof RoleSet) {
				Image image = new Image(Display.getCurrent(), imagePath + "RoleSet.gif");
				return image;
			}
			else if(element instanceof RoleSetGrouping) {
				Image image = new Image(Display.getCurrent(), imagePath + "RoleSetGrouping.gif");
				return image;
			}
		}
		catch(IOException e) {
			return null;
		}
		
		return super.getImage(element);
	}
}
