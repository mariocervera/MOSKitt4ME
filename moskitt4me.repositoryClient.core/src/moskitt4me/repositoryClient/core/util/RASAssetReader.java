package moskitt4me.repositoryClient.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class provides functionality for accessing the content of technical fragments, which are stored in a
 * repository as ZIP files according to the Reusable Asset Specification (RAS) standard. 
 *
 * @author Mario Cervera
 */ 
public class RASAssetReader {
	
	//The location, file name and type of the asset
	
	private String assetFolder;
	private String assetName;
	private String asset_type;
	
	/*
	 * Constructor
	 */
	public RASAssetReader(String assetFolder, String assetName) {
		
		this.assetFolder = assetFolder;
		this.assetName = assetName;
		this.asset_type = "";
	}
	
	/*
	* This method reads the fragment properties, which are stored in an XML manifest document
	*/
	public Map<String, String> getProperties() {
		
		Map<String, String> result = new HashMap<String, String>();
		
		Document doc = getManifestDocument();
		
		if(doc != null) {
			Node type = doc.getElementsByTagName("type").item(0);
			
			if(type != null) {
				asset_type = type.getAttributes().getNamedItem("value").getNodeValue();
			}
			else {
				asset_type = "";
			}
			result.put("Type", asset_type);
			
			if(RepositoryClientUtil.isTechnicalFragment(asset_type)) { // Technical fragment
			
				Node origin = doc.getElementsByTagName("origin").item(0);
				Node objective = doc.getElementsByTagName("objective").item(0);
				Node input = doc.getElementsByTagName("input").item(0);
				Node output = doc.getElementsByTagName("output").item(0);
				Node toolId = doc.getElementsByTagName("toolID").item(0);
				Node description = doc.getElementsByTagName("description").item(0);
				
				if(origin != null) {
					result.put("Origin", origin.getAttributes().getNamedItem("value").getNodeValue());
				}
				else {
					result.put("Origin", "");
				}
				if(objective != null) {
					result.put("Objective", objective.getAttributes().getNamedItem("value").getNodeValue());
				}
				else {
					result.put("Objective", "");
				}
				if(input != null) {
					result.put("Input", input.getAttributes().getNamedItem("value").getNodeValue());
				}
				else {
					result.put("Input", "");
				}
				if(output != null) {
					result.put("Output", output.getAttributes().getNamedItem("value").getNodeValue());
				}
				else {
					result.put("Output", "");
				}
				if(toolId != null) {
					result.put("ToolId", toolId.getAttributes().getNamedItem("value").getNodeValue());
				}
				else {
					result.put("ToolId", "");
				}
				if(description != null) {
					result.put("Description", description.getTextContent());
				}
				else {
					result.put("Description", "");
				}
			}
			else {
				// Conceptual fragments
				
				Node objective = doc.getElementsByTagName("objective").item(0);
				Node origin = doc.getElementsByTagName("origin").item(0);
			
				if(objective != null) {
					result.put("Objective", objective.getAttributes().getNamedItem("value").getNodeValue());
				}
				else {
					result.put("Objective", "");
				}
				if(origin != null) {
					result.put("Origin", origin.getAttributes().getNamedItem("value").getNodeValue());
				}
				else {
					result.put("Origin", "");
				}
			}
		}
		
		return result;
	}
	
	/*
	* Extracts the manifest file from the RAS asset and returns an instance
	* of org.w3c.dom.Document
	*/
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
		
		String manifestName = "rasset.xml";
		
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
}
