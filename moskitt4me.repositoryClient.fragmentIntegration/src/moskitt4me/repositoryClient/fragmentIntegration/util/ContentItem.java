package moskitt4me.repositoryClient.fragmentIntegration.util;

import java.util.ArrayList;
import java.util.Map;


/**This is an auxiliar class to save the information retrieved from the xmi file
 * attributes - common attributes of the element
 * contentDescription - attributes related to content
 */
public class ContentItem {

	private Map<String,String> attributes;
	private Map<String,String> contentDescription;
	private ArrayList<ContentItem> subElements;
	
	public ContentItem( Map<String,String> attributes, Map<String,String> contentDescription, ArrayList<ContentItem> subElements){
		
		this.setAttributes(attributes);
		this.setContentDescription(contentDescription);
		this.setSubElements(subElements);
	}
	public void setAttributes(Map<String,String> attributes) {
		this.attributes = attributes;
	}
	public Map<String,String> getAttributes() {
		return attributes;
	}
	public void setContentDescription(Map<String,String> contentDescription) {
		this.contentDescription = contentDescription;
	}
	public Map<String,String> getContentDescription() {
		return contentDescription;
	}
	public void setSubElements(ArrayList<ContentItem> subElements) {
		this.subElements = subElements;
	}
	public ArrayList<ContentItem> getSubElements() {
		return subElements;
	}
	
}
