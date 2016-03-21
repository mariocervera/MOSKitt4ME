package moskitt4me.toolgenerator.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.mwe.core.WorkflowRunner;
import org.eclipse.emf.mwe.core.monitor.NullProgressMonitor;

/*
 * This class invokes the Workflow that is in charge of launching the Xpand transformations
 * that will generate the plug-ins to be imported into the workspace. These plug-ins
 * are required during the CASE generation process.
 */
public class PluginsGenerator {

	private Map<String, String> oawProperties;
	private Map<String, String> oawSlotContents;
	
	public PluginsGenerator() {
		
		oawProperties = new HashMap<String, String>();
		oawSlotContents = new HashMap<String, String>();
	}
	
	public boolean generatePlugins(String libraryXMIPath,
			String rcpPluginProjectLocation, String mDefPluginProjectLocation,
			String rcpFeatureProjectLocation, String dependenciesFeatureProjectLocation,
			String mDefFeatureProjectLocation, String tFragmentsFeatureProjectLocation) {
		
		oawProperties.put("inputModelFile", libraryXMIPath);
		oawProperties.put("srcGenPath", rcpPluginProjectLocation);
		oawProperties.put("srcGenPath2", mDefPluginProjectLocation);
		oawProperties.put("srcGenPath3", rcpFeatureProjectLocation);
		oawProperties.put("srcGenPath4", dependenciesFeatureProjectLocation);
		oawProperties.put("srcGenPath5", mDefFeatureProjectLocation);
		oawProperties.put("srcGenPath6", tFragmentsFeatureProjectLocation);
		
		String workFlowFilePath = "/transf/SPEM2ProductDefinition.mwe";
		
		WorkflowRunner wr = new WorkflowRunner();
		
		boolean result = wr.run(workFlowFilePath, new NullProgressMonitor(),
				oawProperties, oawSlotContents);
		
		return result;
	}
	
}
