package moskitt4me.repositoryClient.core.providers;

import java.io.IOException;

import moskitt4me.repositoryClient.core.Activator;
import moskitt4me.repositoryClient.core.util.TechnicalFragment;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class DependenciesTreeLabelProvider extends ColumnLabelProvider {

	@Override
	public String getText(Object element) {
		
		if(element instanceof TechnicalFragment) {
			TechnicalFragment tf = (TechnicalFragment) element;
			return  tf.getName();
		}
		
		return super.getText(element);
	}
	
	@Override
	public Image getImage(Object element) {

		try {
			
			String imagePath = FileLocator.toFileURL(Platform.getBundle(
			Activator.PLUGIN_ID).getResource("icons/full/obj16")).getPath();
			
			if(element instanceof TechnicalFragment) {
				TechnicalFragment tf = (TechnicalFragment) element;
				if(tf.isImported()) {
					Image image = new Image(Display.getCurrent(), imagePath + "TechnicalFragmentImported.gif");
					return image;
				}
				else if(tf.isResolved()) {
					Image image = new Image(Display.getCurrent(), imagePath + "TechnicalFragment.gif");
					return image;
				}
				else {
					Image image = new Image(Display.getCurrent(), imagePath + "TechnicalFragmentError.gif");
					return image;
				}
			}
		}
		catch(IOException e) {
			return null;
		}
		
		return super.getImage(element);
	}
	
	@Override
	public String getToolTipText(Object element) {
		
		if(element instanceof TechnicalFragment) {
			TechnicalFragment tf = (TechnicalFragment) element;
			String errors = tf.getErrors();
			if(errors != null && !errors.equals("")) {
				return errors;
			}
		}
		
		return super.getToolTipText(element);
	}
}
