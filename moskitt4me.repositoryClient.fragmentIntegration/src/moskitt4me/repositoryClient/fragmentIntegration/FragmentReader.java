package moskitt4me.repositoryClient.fragmentIntegration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import moskitt4me.repositoryClient.fragmentIntegration.util.ContentItem;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class FragmentReader {
	private String assetFolder;
	private String assetName;
	ArrayList<ContentItem> contentItems;
	Map<String, String> pairsGuid; 

	
	public FragmentReader(String assetFolder, String assetName) {
		
		this.assetFolder = assetFolder;
		this.assetName = assetName;
		pairsGuid = new HashMap<String, String>();
	}
	
	/**This method reads the content elements on the xmi file 
	 * and create ContentItem objects to save the information
	 * @return A list of ContentItems
	 */
	
	public ArrayList<ContentItem> getItems() {
		
		contentItems = new ArrayList<ContentItem>();
		
		
		Document doc = getManifestDocument();
		
		if(doc != null) {
			/**Read Conceptual Contents, (Tasks, Roles, WorkProducts)*/
		if(doc.getElementsByTagName("Contents").item(0) != null){
				Node contents = doc.getElementsByTagName("Contents").item(0);
				for(int i = 0; i<contents.getChildNodes().getLength(); i++){
					Node content = contents.getChildNodes().item(i);
					
					if(content != null) {
						//Get the attributes
						Map<String, String> attributes = new HashMap<String, String>();
						for(int j = 0; j<content.getAttributes().getLength(); j++){
							
							Node n = content.getAttributes().item(j);
							if(n.getNodeName().equals("xmi:id"));
							else if(n.getNodeName().equals("guid")){
								CharSequence aux ="";
								aux = n.getNodeValue();
								if(pairsGuid.containsKey(aux)){
									attributes.put(n.getNodeName(), pairsGuid.get(n.getNodeValue()));
								}else{
								String name = randomString(23);
								pairsGuid.put(content.getAttributes().getNamedItem("guid").getNodeValue(), name);
								attributes.put(n.getNodeName(), name);
								}
							}else if(n.getNodeName().equals("predecessors")
									||n.getNodeName().equals("performedBy")
									|| n.getNodeName().equals("performedBy")
									|| n.getNodeName().equals("additionallyPerformedBy")
									|| n.getNodeName().equals("mandatoryInput")
									|| n.getNodeName().equals("output")
									|| n.getNodeName().equals("optionalInput")
									|| n.getNodeName().equals("responsibleFor")
									){
								String elems="";
								String [] list = n.getNodeValue().split(" "); 
								for(String s: list){
								CharSequence aux ="";
								if(s.endsWith(" ")){
									aux = s.subSequence(0, s.length()-1);
								}else{ aux = s;}
								
								if(pairsGuid.containsKey(aux)){
									elems+=pairsGuid.get(s)+" ";
								}else if(!aux.equals("")){
									String name = randomString(23);
									pairsGuid.put( aux.toString(), name);
									elems+=name+" ";
								}
								}
								attributes.put(n.getNodeName(), elems);
							}
							else{
									attributes.put(n.getNodeName(), n.getNodeValue());
							}
						}
						//Get the contentDescription
						Map<String, String> descriptions = new HashMap<String, String>();
						if(content.getChildNodes().getLength()>0){
						Node description = content.getChildNodes().item(0);//Node <contentDescription>
						for(int k = 0; k< description.getChildNodes().getLength(); k++){
							Node d = description.getChildNodes().item(k);
							descriptions.put(d.getNodeName(), d.getTextContent());
						}
						}
					ArrayList<ContentItem> subElements = new ArrayList<ContentItem>();
						
					ContentItem ci = new ContentItem(attributes, descriptions, subElements);
					contentItems.add(ci);
					}
				}
			
			}
			else{
				/**Read Process Elements (Patterns, and its Activities and TaskDescriptor)*/
				Node proceses = doc.getElementsByTagName("Processes").item(0);
				for(int i = 0; i<proceses.getChildNodes().getLength(); i++){
					
					Node node = proceses.getChildNodes().item(i);
					
					//Get the attributes
					Map<String, String> attributes = new HashMap<String, String>();
					for(int j = 0; j<node.getAttributes().getLength(); j++){
					
						Node n = node.getAttributes().item(j);
						if(n.getNodeName().equals("xmi:id"));
						else if(n.getNodeName().equals("guid")){
							CharSequence aux ="";
							if(n.getNodeValue().endsWith(" ")){
								aux = n.getNodeValue().subSequence(0, n.getNodeValue().length()-1);
							}else{ aux = n.getNodeValue();}
							
							if(pairsGuid.containsKey(aux)){
								attributes.put(n.getNodeName(), pairsGuid.get(n.getNodeValue()));
								attributes.put("xmi:id", pairsGuid.get(n.getNodeValue()));
							}else{
								String name = randomString(23);
								pairsGuid.put( aux.toString(), name);
								attributes.put(n.getNodeName(), name);
								attributes.put("xmi:id", name);
							}
						}else if((n.getNodeName().equals("predecessors")&& !n.getNodeValue().equals(""))
								||(n.getNodeName().equals("performedBy")&& !n.getNodeValue().equals(""))
								||(n.getNodeName().equals("additionallyPerformedBy")&& !n.getNodeValue().equals(""))
								||(n.getNodeName().equals("mandatoryInput")&& !n.getNodeValue().equals(""))
								||(n.getNodeName().equals("output")&& !n.getNodeValue().equals(""))
								||(n.getNodeName().equals("optionalInput")&& !n.getNodeValue().equals(""))
								||(n.getNodeName().equals("responsibleFor")&& !n.getNodeValue().equals(""))
								){
							String elems="";
							String [] list = n.getNodeValue().split(" "); 
							for(String s: list){
							CharSequence aux ="";
							if(s.endsWith(" ")){
								aux = s.subSequence(0, s.length()-1);
							}else{ aux = s;}
							
							if(pairsGuid.containsKey(aux)){
								elems+=pairsGuid.get(s)+" ";
							}else{
								String name = randomString(23);
								pairsGuid.put( aux.toString(), name);
								elems+=name+" ";
							}
							}
							attributes.put(n.getNodeName(), elems);
						}
						else attributes.put(n.getNodeName(), n.getNodeValue());
					}
					
					//Get the sub elements
					ArrayList<ContentItem> subElements = new ArrayList<ContentItem>();
					
					for(int j = 0; j<node.getChildNodes().getLength(); j++){
						ContentItem pi = null;
						Node n = node.getChildNodes().item(j);
						for(ContentItem aux : contentItems){
							if(aux.getAttributes().get("guid").equals(pairsGuid.get(n.getAttributes().getNamedItem("guid"))))
								pi=aux;
							else
								pi = getContentItem(aux, n);
						}
						if(pi==null) pi = crearProcessItem(n);
						subElements.add(pi);
					}
					
					Map<String, String> descriptions = new HashMap<String, String>();
				
					ContentItem ci = new ContentItem(attributes, descriptions, subElements);
					contentItems.add(ci);
					}
			
				}
			
			}
		
	
		return contentItems;
			
	}
	

	private ContentItem getContentItem(ContentItem aux, Node n) {
		// TODO Auto-generated method stub
		for(ContentItem subAux: aux.getSubElements()){
			if(pairsGuid.containsKey(n.getAttributes().getNamedItem("guid")) 
					&& subAux.getAttributes().get("guid").equals(pairsGuid.get(n.getAttributes().getNamedItem("guid")))){
				return subAux;
			}
			else{
				return getContentItem(subAux, n);
			}
		}
		return null;
	}

	//Recursive method for each process item
	private ContentItem crearProcessItem(Node node){
		
			//Get the attributes
			Map<String, String> attributes = new HashMap<String, String>();
			for(int j = 0; j<node.getAttributes().getLength(); j++){
				Node n = node.getAttributes().item(j);
				if(n.getNodeName().equals("xmi:id"));
				else if(n.getNodeName().equals("guid")){
					CharSequence aux ="";
					if(n.getNodeValue().endsWith(" ")){
						aux = n.getNodeValue().subSequence(0, n.getNodeValue().length()-1);
					}else{ aux = n.getNodeValue();}
					
					if(pairsGuid.containsKey(aux)){
						attributes.put(n.getNodeName(), pairsGuid.get(n.getNodeValue()));
						attributes.put("xmi:id", pairsGuid.get(n.getNodeValue()));
					}else{
						String name = randomString(23);
						pairsGuid.put( aux.toString(), name);
						attributes.put(n.getNodeName(), name);
						attributes.put("xmi:id", name);
					}
					}
				else if((n.getNodeName().equals("predecessors")&& !n.getNodeValue().equals(""))
						||(n.getNodeName().equals("performedBy")&& !n.getNodeValue().equals(""))
						||(n.getNodeName().equals("additionallyPerformedBy")&& !n.getNodeValue().equals(""))
						||(n.getNodeName().equals("mandatoryInput")&& !n.getNodeValue().equals(""))
						||(n.getNodeName().equals("output")&& !n.getNodeValue().equals(""))
						||(n.getNodeName().equals("optionalInput")&& !n.getNodeValue().equals(""))
						||(n.getNodeName().equals("responsibleFor")&& !n.getNodeValue().equals(""))
						){
					String elems="";
					String [] list = n.getNodeValue().split(" "); 
					for(String s: list){
					CharSequence aux ="";
					if(s.endsWith(" ")){
						aux = s.subSequence(0, s.length()-1);
					}else{ aux = s;}
					
					if(pairsGuid.containsKey(aux)){
						elems+=pairsGuid.get(s)+" ";
					}else{
						String name = randomString(23);
						pairsGuid.put( aux.toString(), name);
						elems+=name+" ";
					}
					}
					attributes.put(n.getNodeName(), elems);
				}
				else{
					attributes.put(n.getNodeName(), n.getNodeValue());
				}
			}
			//Get the sub elements
			ArrayList<ContentItem> subElements = new ArrayList<ContentItem>();
			
			for(int j = 0; j<node.getChildNodes().getLength(); j++){
				ContentItem pi = null;
				Node n = node.getChildNodes().item(j);
				for(ContentItem aux : contentItems){
					if(aux.getAttributes().get("guid").equals(n.getAttributes().getNamedItem("guid")))
						pi=aux;
					else
						pi = getContentItem(aux, n);
				}
				if(pi==null) pi = crearProcessItem(n);
				subElements.add(pi);
			}
			Map<String, String> descriptions = new HashMap<String, String>();
		
			ContentItem ci = new ContentItem(attributes, descriptions, subElements);
			
			return ci;
	}
	
	private Document getManifestDocument() {
		
		File manifest = null;
		Document document = null;
		
		try {
			File zipFile = new File(assetFolder + "/" + assetName);
			
			if(zipFile.exists()) {
				//Extract manifest file
				
				String manifestPath = extractManifest(zipFile, assetFolder);
				
				//Create XML Document
				
				manifest = new File(manifestPath);
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				document = db.parse(manifest);
			}
		}
		catch(Exception e) {
			return null;
		}
		finally {
			if(manifest != null) {
				manifest.delete();
			}
		}
		
		return document;
		
	}
	
	private String extractManifest(File zipFile, String dir) {
		
		String manifestName = "fragment.xmi";
		
		String manifestPath = "";
		
		try {
			FileInputStream fis = new FileInputStream(zipFile);
			ZipInputStream zis = new ZipInputStream(fis);
			ZipEntry ze;
			
			boolean finded = false;
			
			while (!finded && (ze = zis.getNextEntry()) != null) {
				if(ze.getName().equals(manifestName)) {
					
					finded = true;
					
					manifestPath = dir + "/" + manifestName;
					
					//ExtractManifest
					
					byte[] buf = new byte[1024];
					
					FileOutputStream fileoutputstream = new FileOutputStream(manifestPath);
					
					int n;
					while ((n = zis.read(buf, 0, 1024)) > -1) {
	                    fileoutputstream.write(buf, 0, n);
					}
					
					fileoutputstream.close(); 
				}
				zis.closeEntry();
			}

			zis.close();
			fis.close();
			
			return manifestPath;

		} catch (Exception e) {
			return "";
		}
	}
	
protected static String randomString(int size){
		
		char[] chars = "abcdefghijklmnopqrstuvwxyz12345678_-".toCharArray();
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < 20; i++) {
		    char c = chars[random.nextInt(chars.length)];
		    sb.append(c);
		}
		String output = sb.toString();
		return output;
	}
	
	
	}

