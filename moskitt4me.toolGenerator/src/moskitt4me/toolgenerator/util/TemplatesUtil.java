package moskitt4me.toolgenerator.util;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Platform;
import org.eclipse.epf.library.LibraryService;
import org.eclipse.epf.uma.MethodLibrary;
import org.eclipse.ui.branding.IProductConstants;
import org.osgi.framework.Version;

/*
 * This class provides Java extensions for the Xpand templates
 *
 * @author Mario Cervera
 */
public class TemplatesUtil {
	
	public static String productName() {
		return Platform.getProduct().getName();
	}
	
	public static String productId() {
		return Platform.getProduct().getId();
	}
	
	public static String productApplication() {
		return Platform.getProduct().getApplication();
	}
	
	public static String productVersion() {
		Version version = Platform.getProduct().getDefiningBundle().getVersion();
		
		if(version != null) {
			String v = version.getMajor() + "." + version.getMinor() + "." + version.getMicro();
			if(version.getQualifier() != null && !version.getQualifier().equals("")) {
				return v + ".qualifier";
			}
			else return v;
		}
		
		return "";
	}
	
	public static String aboutImage() {
		String aboutImage = Platform.getProduct().getProperty(IProductConstants.ABOUT_IMAGE);
		
		if(aboutImage != null && !aboutImage.equals("")) {
			return aboutImage;
		}
		
		return "";
	}
	
	public static String aboutText() {
		String aboutText = Platform.getProduct().getProperty(IProductConstants.ABOUT_TEXT);
		
		if(aboutText != null && !aboutText.equals("")) {
			return aboutText;
		}
		
		return "";
	}
	
	public static String windowImages() {
		
		String images = Platform.getProduct().getProperty(IProductConstants.WINDOW_IMAGES);
		
		if(images != null && !images.equals("")) {
			return images;
		}
		
		return "";
	}
	
	public static String windowImagesForProductFile() {
		
		String result = "";
		
		String images = Platform.getProduct().getProperty(IProductConstants.WINDOW_IMAGES);
		
		if(images != null && !images.equals("")) {
			
			StringTokenizer st = new StringTokenizer(images, ",");
			
			if(st.hasMoreTokens()) {
				result += "i16=\"" + st.nextToken() + "\"";
				
				if(st.hasMoreTokens()) {
					result += " i32=\"" + st.nextToken() + "\"";
					
					if(st.hasMoreTokens()) {
						result += " i48=\"" + st.nextToken() + "\"";
						
						if(st.hasMoreTokens()) {
							result += " i64=\"" + st.nextToken() + "\"";
							
							if(st.hasMoreTokens()) {
								result += " i128=\"" + st.nextToken() + "\"";
							}
						}
					}
				}
			}
		}
		
		return result;
	}
	
	public static String windowImagesForBuildProperties() {
		
		String result = "";
		
		String images = Platform.getProduct().getProperty(IProductConstants.WINDOW_IMAGES);
		
		if(images != null && !images.equals("")) {
			
			StringTokenizer st = new StringTokenizer(images, ",");
			while(st.hasMoreTokens()) {
				result += ",\\\n" + st.nextToken();
			}
		}
		
		return result;
	}
	
	public static String splashLocation() {
		
		String location = Platform.getProduct().getDefiningBundle().getSymbolicName();
		
		if(location != null && !location.equals("")) {
			URL url = Platform.getBundle(location).getEntry("splash.bmp");
			if(url != null) {
				return location;
			}
		}
		
		return "";
	}
	

	public static String startupProgressRect() {

		String progressRect = Platform.getProduct().getProperty(IProductConstants.STARTUP_PROGRESS_RECT);
		
		if(progressRect != null && !progressRect.equals("")) {
			return progressRect;
		}
		
		return "";
	}

	public static String startupMessageRect() {

		String messageRect = Platform.getProduct().getProperty(IProductConstants.STARTUP_MESSAGE_RECT);
		
		if(messageRect != null && !messageRect.equals("")) {
			return messageRect;
		}
		
		return "";
	}

	public static String startupForegroundColor() {

		String foregroundColor = Platform.getProduct().getProperty(IProductConstants.STARTUP_FOREGROUND_COLOR);
		
		if(foregroundColor != null && !foregroundColor.equals("")) {
			return foregroundColor;
		}
		
		return "";
	}
	
	public static String definingBundleName() {
		
		String name = Platform.getProduct().getDefiningBundle().getSymbolicName();
		
		if(name != null && !name.equals("")) {
			return name;
		}
		
		return "";
	}
	
	public static String appName() {
		
		String appName = Platform.getProduct().getProperty(IProductConstants.APP_NAME);
		
		if(appName != null && !appName.equals("")) {
			return appName;
		}
		
		return "";
	}
	
	public static String preferenceCustomization() {
		
		String pref = Platform.getProduct().getProperty(IProductConstants.PREFERENCE_CUSTOMIZATION);
		
		if(pref != null && !pref.equals("")) {
			return pref;
		}
		
		return "";
	}
	
	public static String methodDefinitionBundleName() {
		
		return "moskitt4me.methodDefinitions";
	}
	
	public static String technicalFragmentsFeatureName() {
		
		return "moskitt4me.technicalFragments.feature";
	}
	
	public static List<String> technicalFragmentFeatures() {
		
		List<String> features = new ArrayList<String>();
		
		File tfragmentsFolder = new File(GeneratorUtil.technicalFragmentsPath);
		if(tfragmentsFolder.exists()) {
			File[] files = tfragmentsFolder.listFiles();
			
			for(File f : files) {
				if(f.isDirectory()) {
					String uri = f.toURI().toString().replaceFirst("file:", "");
					File featureXML = new File(uri + "/feature.xml");
					if(featureXML.exists()) {
						features.add(f.getName());
					}
				}
			}
		}
		
		return features;
	}
	
	public static String libraryPath() {
		
		String libraryPath = "";
		
		MethodLibrary lib = LibraryService.getInstance().getCurrentMethodLibrary();
		
		if(lib != null) {
			int scount= lib.eResource().getURI().segmentCount();
			String s1 = lib.eResource().getURI().segment(scount - 2);
			String s2 = lib.eResource().getURI().segment(scount - 1);
			
			return "lib/" + s1 + "/" + s2;
		}
		
		return libraryPath;
	}
}
