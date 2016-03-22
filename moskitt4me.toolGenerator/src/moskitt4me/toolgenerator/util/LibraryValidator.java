package moskitt4me.toolgenerator.util;

import java.io.File;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.epf.library.LibraryService;
import org.eclipse.epf.library.util.LibraryUtil;
import org.eclipse.epf.uma.DeliveryProcess;
import org.eclipse.epf.uma.MethodLibrary;
import org.eclipse.epf.uma.MethodPlugin;
import org.eclipse.epf.uma.Process;
import org.eclipse.epf.uma.ProcessComponent;
import org.eclipse.epf.uma.ProcessPackage;

/*
 * This class provides a method for validating the method library prior to the
 * invokation of the CASE generation process. Constraints:
 * 
 * - The method library must define at least one delivery process
 * 
 * - Every delivery process must be associated to a BPMN 2.0 model
 * 
 * @author Mario Cervera
 */
public class LibraryValidator {

	public static void validateLibrary() {
		
		MethodLibrary lib = LibraryService.getInstance().getCurrentMethodLibrary();
		if (lib != null) {
			
			List<MethodPlugin> plugins = lib.getMethodPlugins();
			if(plugins == null || plugins.size() == 0) {
				throw new RuntimeException("This method library does not define any method plugin");
			}
			
			boolean deliveryProcesses = false;
			
			for(MethodPlugin mp : plugins) {
				List packages = LibraryUtil.getAllPackages(mp);
				for(Object obj : packages) {
					if(obj instanceof ProcessPackage) {
						ProcessPackage pp = (ProcessPackage) obj;
						List<EObject> contents = pp.eContents();
						for(EObject eobj : contents) {
							if(eobj instanceof ProcessComponent) {
								ProcessComponent pc = (ProcessComponent) eobj;
								Process p = pc.getProcess();
								if(p instanceof DeliveryProcess) {
									DeliveryProcess dp = (DeliveryProcess) p;
									deliveryProcesses = true;
									if(!hasBPMNmodel(dp)) {
										throw new RuntimeException("BPMN 2.0 diagram does not exist for: " + dp.getName() + "\n\nInvoke \"Diagrams -> Open BPMN 2.0 Diagram\"");
									}
								}
							}
						}
					}
				}
			}
			
			if(!deliveryProcesses) {
				throw new RuntimeException("This method library does not define any delivery process");
			}
		}	
	}

	private static boolean hasBPMNmodel(DeliveryProcess dp) {

		String activitiFolder = getActivitiFolder(dp);

		File f = new File(activitiFolder);

		return (f.isDirectory() && f.listFiles().length > 0 &&
					!(f.listFiles().length == 1 && f.listFiles()[0].isDirectory() && f
							.listFiles()[0].getName().equals(".svn")));

	}
	
	private static String getActivitiFolder(DeliveryProcess dp) {
		
		String activitiFolder = replaceLastSegment(dp.eResource().getURI().toString(), "activiti");
		
		MethodLibrary lib = LibraryService.getInstance().getCurrentMethodLibrary();
		MethodPlugin plugin = getMethodPlugin(dp);
		
		if (lib != null && plugin != null) {
			String libraryLocation = replaceLastSegment(
					lib.eResource().getURI().toString(), "");
			
			activitiFolder = libraryLocation + plugin.getName()
			+ "/deliveryprocesses/"+ dp.getName() + "/activiti/";
		}
		
		return activitiFolder.replaceFirst("file:", "");
	}
	
	private static MethodPlugin getMethodPlugin(DeliveryProcess dp) {
		
		EObject container = dp.eContainer();
		
		while(container != null && !(container instanceof MethodPlugin)) {
			container = container.eContainer();
		}
		
		if(container != null) {
			return (MethodPlugin) container;
		}
		else {
			return null;
		}
	}
	
	private static String replaceLastSegment(String uri, String newSegment) {
		
		StringTokenizer tokenizer = new StringTokenizer(uri, "/");
		
		String newUri = "";
		int numTokens = tokenizer.countTokens();
		
		for(int i = 0; i < numTokens - 1; i++) {
			newUri += tokenizer.nextToken() + "/";
		}
		
		if(newSegment != null && !newSegment.equals("")) {
			return (newUri + newSegment + "/");
		}
		else {
			return newUri;
		}
	}
}
