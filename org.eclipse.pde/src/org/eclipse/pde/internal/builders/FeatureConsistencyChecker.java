package org.eclipse.pde.internal.builders;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.*;
import java.util.Map;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.PDE;
import org.eclipse.pde.internal.core.*;
import org.eclipse.pde.internal.core.feature.WorkspaceFeatureModel;
import org.eclipse.pde.internal.core.ifeature.*;
import org.xml.sax.*;

public class FeatureConsistencyChecker extends IncrementalProjectBuilder {
	public static final String BUILDERS_VERIFYING = "Builders.verifying";
	public static final String BUILDERS_FEATURE_REFERENCE =
		"Builders.Feature.reference";
	public static final String BUILDERS_UPDATING = "Builders.updating";

	class DeltaVisitor implements IResourceDeltaVisitor {
		private IProgressMonitor monitor;
		public DeltaVisitor(IProgressMonitor monitor) {
			this.monitor = monitor;
		}
		public boolean visit(IResourceDelta delta) {
			IResource resource = delta.getResource();

			if (resource instanceof IProject) {
				// Only check projects with feature nature
				IProject project = (IProject) resource;
				try {
					return (project.hasNature(PDE.FEATURE_NATURE));
				} catch (CoreException e) {
					PDE.logException(e);
					return false;
				}
			}
			if (resource instanceof IFile) {
				// see if this is it
				IFile candidate = (IFile) resource;
				if (isManifestFile(candidate)) {
					// That's it, but only check it if it has been added or changed
					if (delta.getKind() != IResourceDelta.REMOVED) {
						checkFile(candidate, monitor);
						return true;
					}
				}
			}
			return true;
		}
	}

	public FeatureConsistencyChecker() {
		super();
	}
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
		throws CoreException {

		IResourceDelta delta = null;
		if (kind != FULL_BUILD)
			delta = getDelta(getProject());

		if (delta == null || kind == FULL_BUILD) {
			// Full build
			IProject project = getProject();
			IFile file = project.getFile("feature.xml");
			if (file.exists()) {
				checkFile(file, monitor);
			}
		} else {
			delta.accept(new DeltaVisitor(monitor));
		}
		return null;
	}
	private void checkFile(IFile file, IProgressMonitor monitor) {
		String message =
			PDE.getFormattedMessage(BUILDERS_VERIFYING, file.getFullPath().toString());
		monitor.subTask(message);
		PluginErrorReporter reporter = new PluginErrorReporter(file);
		ValidatingSAXParser parser = new ValidatingSAXParser();
		parser.setErrorHandler(reporter);
		InputStream source = null;
		try {
			source = file.getContents();
			InputSource inputSource = new InputSource(source);
			parser.parse(inputSource);
			if (reporter.getErrorCount() == 0) {
				testPluginReferences(file, reporter);
			}
		} catch (CoreException e) {
			PDE.logException(e);
		} catch (SAXException e) {
		} catch (IOException e) {
			PDE.logException(e);
		} finally {
			if (source != null) {
				try {
					source.close();
				} catch (IOException e) {
				}
			}
		}
		monitor.subTask(PDE.getResourceString(BUILDERS_UPDATING));
		monitor.done();
	}
	private boolean isManifestFile(IFile file) {
		return file.getName().toLowerCase().equals("feature.xml");
	}
	private boolean isValidReference(IFeaturePlugin plugin) {
		WorkspaceModelManager manager = PDECore.getDefault().getWorkspaceModelManager();
		IPluginModelBase[] models =
			plugin.isFragment()
				? (IPluginModelBase[]) manager.getWorkspaceFragmentModels()
				: (IPluginModelBase[]) manager.getWorkspacePluginModels();
		for (int i = 0; i < models.length; i++) {
			IPluginModelBase model = models[i];
			if (model.getPluginBase().getId().equals(plugin.getId())) {
				return true;
			}
		}
		return false;
	}

	private void testPluginReferences(IFile file, PluginErrorReporter reporter) {
		WorkspaceFeatureModel model = new WorkspaceFeatureModel(file);
		model.load();
		if (model.isLoaded()) {
			IFeature feature = model.getFeature();
			IFeaturePlugin[] plugins = feature.getPlugins();
			for (int i = 0; i < plugins.length; i++) {
				IFeaturePlugin plugin = plugins[i];
				if (isValidReference(plugin) == false) {
					String message =
						PDE.getFormattedMessage(BUILDERS_FEATURE_REFERENCE, plugin.getLabel());
					reporter.reportError(message);
				}
			}
		}
	}
}