package moskitt4me.projectmanager.core.providers;

import java.io.IOException;

import moskitt4me.projectmanager.core.Activator;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.epf.uma.Checklist;
import org.eclipse.epf.uma.Concept;
import org.eclipse.epf.uma.EstimationConsiderations;
import org.eclipse.epf.uma.Example;
import org.eclipse.epf.uma.Guidance;
import org.eclipse.epf.uma.Guideline;
import org.eclipse.epf.uma.ReusableAsset;
import org.eclipse.epf.uma.SupportingMaterial;
import org.eclipse.epf.uma.Task;
import org.eclipse.epf.uma.TermDefinition;
import org.eclipse.epf.uma.ToolMentor;
import org.eclipse.epf.uma.Whitepaper;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * A Label Provider for the Guides view. Provides labels and icons for the graphical elements.
 * 
 * @author Mario Cervera
 */
public class GuidesLabelProvider extends LabelProvider {

	/*
	 * This method returns the textual label of a given element
	 */
	@Override
	public String getText(Object element) {
		
		// The names of the elements of type Task and Guidance are their labels
		
		if(element instanceof Task) {
			Task t = (Task) element;
			return t.getPresentationName() != null ? t.getPresentationName() : t.getName();
		}
		else if(element instanceof Guidance) {
			Guidance g = (Guidance) element;
			return g.getPresentationName() != null ? g.getPresentationName() : g.getName();
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
			
			if(element instanceof Task) {
				Image image = new Image(Display.getCurrent(), imagePath + "Task.gif");
				return image;
			}
			else if(element instanceof Checklist) {
				Image image = new Image(Display.getCurrent(), imagePath + "Checklist.gif");
				return image;
			}
			else if(element instanceof Whitepaper) {
				Image image = new Image(Display.getCurrent(), imagePath + "Whitepaper.gif");
				return image;
			}
			else if(element instanceof Concept) {
				Image image = new Image(Display.getCurrent(), imagePath + "Concept.gif");
				return image;
			}
			else if(element instanceof Example) {
				Image image = new Image(Display.getCurrent(), imagePath + "Example.gif");
				return image;
			}
			else if(element instanceof Guideline) {
				Image image = new Image(Display.getCurrent(), imagePath + "Guideline.gif");
				return image;
			}
			else if(element instanceof EstimationConsiderations) {
				Image image = new Image(Display.getCurrent(), imagePath + "EstimationConsiderations.gif");
				return image;
			}
			else if(element instanceof ReusableAsset) {
				Image image = new Image(Display.getCurrent(), imagePath + "ReusableAsset.gif");
				return image;
			}
			else if(element instanceof SupportingMaterial) {
				Image image = new Image(Display.getCurrent(), imagePath + "SupportingMaterial.gif");
				return image;
			}
			else if(element instanceof TermDefinition) {
				Image image = new Image(Display.getCurrent(), imagePath + "TermDefinition.gif");
				return image;
			}
			else if(element instanceof ToolMentor) {
				Image image = new Image(Display.getCurrent(), imagePath + "ToolMentor.gif");
				return image;
			}
		}
		catch(IOException e) {
			return null;
		}
		
		return super.getImage(element);
	}
}
