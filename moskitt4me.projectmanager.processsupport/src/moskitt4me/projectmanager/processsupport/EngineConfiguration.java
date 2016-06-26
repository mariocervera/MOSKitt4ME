package moskitt4me.projectmanager.processsupport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.eclipse.core.runtime.Platform;

/**
 * In order to create an Activiti Engine instance, a configuration file (activiti.cfg.xml)
 * must be created. This class provides methods for creating this file and deleting it.
 * 
 * This file could have been just copied into a folder within the 
 * "moskitt4me.projectmanager.processsupport" plugin. The problem of this approach is that
 * it does not enable the creation of the H2 database within the ".method" folder of the projects.
 *
 * @author Mario Cervera
 */
public class EngineConfiguration {

	public static String createConfigurationFile(String fileName, String dbLocation) throws Exception {
		
		String eclipseInstallationDirectory = Platform.getInstallLocation()
				.getURL().getPath();
		
		String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
		"<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" + 
		       "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
		       "xsi:schemaLocation=\"http://www.springframework.org/schema/beans   http://www.springframework.org/schema/beans/spring-beans.xsd\">\n" +
		  "<bean id=\"processEngineConfiguration\" class=\"org.activiti.engine.impl.cfg.StandaloneProcessEngineConfiguration\">\n" +		  
		    "<!-- Database configurations -->\n" +
		    "<property name=\"databaseSchemaUpdate\" value=\"true\" />\n" +
		    "<property name=\"jdbcUrl\" value=\"jdbc:h2:" + dbLocation + "/db\" />\n" +
		    "<property name=\"jdbcDriver\" value=\"org.h2.Driver\" />\n"+
		    "<property name=\"jdbcUsername\" value=\"sa\" />\n" +
		    "<property name=\"jdbcPassword\" value=\"\" />\n" +
		    "<property name=\"dbCycleUsed\" value=\"true\" />\n" +
		  "</bean>\n" +
		"</beans>";
		
		String finalDir = createFolder(eclipseInstallationDirectory + "tmp", 0);
		
		File f = new File(finalDir + "/" + fileName);
		f.createNewFile();
		
		FileWriter writer = new FileWriter(finalDir + "/" + fileName);
		BufferedWriter out = new BufferedWriter(writer);
		
		out.write(content);
		
		out.close();
		
		return finalDir;
	}
	
	public static void deleteConfigurationFile(String dir) {
		
		File folder = new File(dir);
		
		if(folder.isDirectory()) {
			File[] files = folder.listFiles();
			if(files.length > 0) {
				//Empty the folder
				for(File f : files) {
					if(!f.isDirectory()) {
						f.delete();
					}
					else {
						deleteConfigurationFile(f.getPath());
					}
				}
			}
			//Folder is now empty
			folder.delete();
		}
	}
	
	private static String createFolder(String path, int index) {

		String newPath = path + index;

		String createdFolderPath = "";

		File folder = new File(newPath);
		if (!folder.exists()) {
			folder.mkdirs();
			createdFolderPath = newPath;
		} else {
			createdFolderPath = createFolder(path, index + 1);
		}

		return createdFolderPath;
	}
}
