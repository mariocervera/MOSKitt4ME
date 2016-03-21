package moskitt4me.toolgenerator.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import moskitt4me.toolgenerator.util.TemplatesUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.exports.FeatureExportInfo;
import org.eclipse.pde.internal.core.exports.ProductExportOperation;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.iproduct.IProductFeature;
import org.eclipse.pde.internal.core.product.WorkspaceProductModel;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.wizards.exports.BaseExportWizard;
import org.eclipse.pde.internal.ui.wizards.product.SynchronizationOperation;
import org.eclipse.ui.progress.IProgressConstants;

/*
* This class is used by the "ToolGenerationAction" only to invoke the functionality that is provided
* by the Eclipse PDE, which enables  the automatic generation of the CASE environment. This environment
* is an Eclipse RCP product. The wizard is never opened.
*/
public class ToolGenerationWizard extends BaseExportWizard {

	private static final String STORE_SECTION = "ProductExportWizard"; //$NON-NLS-1$
	private WorkspaceProductModel fProductModel;
	
	private String destination;
	private String productRoot;
	
	private ProductExportOperation job;
	
	public ToolGenerationWizard(String destination, String productRoot) {
		
		this.destination = destination;
		this.productRoot = productRoot;
		
		File dest = new File(this.destination);
		if(!dest.exists()) {
			dest.mkdirs();
		}
	}
	
	public ProductExportOperation getJob() {
		return job;
	}
	
	@Override
	protected void saveSettings() {
		
	}
	
	protected String getSettingsSectionName() {
		return STORE_SECTION;
	}

	protected void scheduleExportJob() {
		FeatureExportInfo info = new FeatureExportInfo();
		info.toDirectory = true;
		info.exportSource = false;
		info.exportSourceBundle = true;
		info.allowBinaryCycles = true;
		info.exportMetadata = true;
		info.destinationDirectory = destination;
		info.zipFileName = null;
		info.targets = null;
		info.items = getFeatureModels();

		String rootDirectory = productRoot;
		
		job = new ProductExportOperation(info, PDEUIMessages.ProductExportJob_name,
				fProductModel.getProduct(), rootDirectory);
		job.setUser(true);
		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		job.schedule();
		job.setProperty(IProgressConstants.ICON_PROPERTY, PDEPluginImages.DESC_FEATURE_OBJ);
	}

	private IFeatureModel[] getFeatureModels() {
		ArrayList list = new ArrayList();
		FeatureModelManager manager = PDECore.getDefault().getFeatureModelManager();
		IProductFeature[] features = fProductModel.getProduct().getFeatures();
		for (int i = 0; i < features.length; i++) {
			IFeatureModel model = manager.findFeatureModel(features[i].getId(), features[i].getVersion());
			if (model != null)
				list.add(model);
		}
		return (IFeatureModel[]) list.toArray(new IFeatureModel[list.size()]);
	}

	protected boolean performPreliminaryChecks() {
		fProductModel = new WorkspaceProductModel(getProductFile(), false);
		try {
			fProductModel.load();
			if (!fProductModel.isLoaded()) {
				MessageDialog.openError(getContainer().getShell(), PDEUIMessages.ProductExportWizard_error, PDEUIMessages.ProductExportWizard_corrupt); // 
				return false;
			}
		} catch (CoreException e) {
			MessageDialog.openError(getContainer().getShell(), PDEUIMessages.ProductExportWizard_error, PDEUIMessages.ProductExportWizard_corrupt); // 
			return false;
		}

		try {
			getContainer().run(false, false, new SynchronizationOperation(fProductModel.getProduct(), getContainer().getShell(), null));
		} catch (InvocationTargetException e) {
			MessageDialog.openError(getContainer().getShell(), PDEUIMessages.ProductExportWizard_syncTitle, e.getTargetException().getMessage());
			return false;
		} catch (InterruptedException e) {
			return false;
		}
			
		return true;
	}

	protected boolean confirmDelete() {

		return true;
	}
	
	protected IFile getProductFile() {

		String path = "/" + TemplatesUtil.definingBundleName() + "/productConfiguration.product";
		
		IPath thePath = new Path(path);
		return thePath.segmentCount() < 2 ? null : PDEPlugin.getWorkspace().getRoot().getFile(new Path(path));
	}
	
}
