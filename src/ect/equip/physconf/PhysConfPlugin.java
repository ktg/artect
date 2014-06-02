/*
 <COPYRIGHT>

 Copyright (c) 2006-2009, University of Nottingham
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 - Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 - Neither the name of the University of Nottingham
 nor the names of its contributors may be used to endorse or promote products
 derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 </COPYRIGHT>

 Created by: Kevin Glover (University of Nottingham)
 */
package ect.equip.physconf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.rulesys.BuiltinRegistry;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.vocabulary.RDF;

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.rules.ECTInfModel;
import ect.equip.physconf.rules.ECTRuleInfGraph;
import ect.equip.physconf.rules.ECTRuleReasoner;
import ect.equip.physconf.rules.SetPropertyValueBuiltin;
import equip.ect.RDFStatement;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class PhysConfPlugin extends AbstractUIPlugin
{
	// The shared instance.
	private static PhysConfPlugin plugin;

	static
	{
		BuiltinRegistry.theRegistry.register(new SetPropertyValueBuiltin());
	}

	public static String createUniqueID()
	{
		return null;
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance.
	 */
	public static PhysConfPlugin getDefault()
	{
		return plugin;
	}

	public static ImageDescriptor getDescriptor(final String key)
	{
		return getDefault().getImageRegistry().getDescriptor(key);
	}

	public static Image getImage(final String key)
	{
		return getDefault().getImageRegistry().get(key);
	}

	public static IPath getInstallDirectory()
	{
		final String path = Platform.getInstallLocation().getURL().getPath();
		if (path.endsWith("/eclipse/plugins/")) { return new Path("F:/artect/"); }
		return new Path(Platform.getInstallLocation().getURL().getPath());
	}
	
	public static IPath getDirectory(final String directory)
	{
		IPath path = new Path(Platform.getInstallLocation().getURL().getPath()).append(directory);
		if(path.makeAbsolute().toFile().exists())
		{
			return path;
		}
		
		path = Platform.getLocation().append(directory);
		if(path.makeAbsolute().toFile().exists())
		{
			return path;
		}
		
		path = new Path("F:/artect/").append(directory);
		if(path.makeAbsolute().toFile().exists())
		{
			return path;
		}
		return null;
	}	

	public static void loadRDF(final Model model, final File directory)
	{
		if (!directory.exists()) { return; }
		final File[] files = directory.listFiles();
		for (final File file : files)
		{
			if (file.getName().endsWith(".owl") || file.getName().endsWith(".rdf"))
			{
				try
				{
					model.read(new FileInputStream(file), "");
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static List loadRules(final File directory)
	{
		if (!directory.exists()) { return Collections.EMPTY_LIST; }
		final File[] files = directory.listFiles();
		final List rules = new ArrayList();
		for (final File file : files)
		{
			if (file.getName().endsWith(".rules"))
			{
				try
				{
					rules.addAll(Rule.rulesFromURL(file.toURI().toURL().toString()));
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}

		return rules;
	}

	private static ImageDescriptor getImageDescriptor(final String path)
	{
		return AbstractUIPlugin.imageDescriptorFromPlugin("ect.equip.physconf", path);
	}

	private final DataspaceMonitor dataspace = new DataspaceMonitor();

	private InfModel infModel;

	private final Collection<String> paths = new HashSet<String>();

	/**
	 * The constructor.
	 */
	public PhysConfPlugin()
	{
		plugin = this;
	}

	public void addPath(final String path)
	{
		paths.add(path);
	}

	public DataspaceMonitor getDataspaceMonitor()
	{
		return dataspace;
	}

	public Image getImage(final Resource resource)
	{
		if (resource.hasProperty(RDF.type, Schema.MediaFileLocation))
		{
			Image image = getImageRegistry().get(resource.toString());
			if (image == null)
			{
				if (!resource.hasProperty(Schema.filePath)) { return null; }
				final String filePath = resource.getProperty(Schema.filePath).getString();
				final URL file = getFile(filePath);
				if (file != null)
				{
					ImageDescriptor imageDesc = getImageRegistry().getDescriptor(file.toString());
					if (imageDesc == null)
					{
						imageDesc = ImageDescriptor.createFromURL(file);
						if (imageDesc != null)
						{
							getImageRegistry().put(file.toString(), imageDesc);
						}
					}
					getImageRegistry().put(resource.toString(), imageDesc);
					image = getImageRegistry().get(resource.toString());
				}
			}
			return image;
		}
		return null;
	}

	public Model getModel()
	{
		return infModel;
	}

	@Override
	public void start(final BundleContext context) throws Exception
	{
		super.start(context);
		final IPath installPath = PhysConfPlugin.getInstallDirectory();
		System.setProperty("java.library.path", installPath.toOSString() + File.pathSeparator
				+ getDirectory("/common/").toOSString() + File.pathSeparator
				+ System.getProperty("java.library.path"));

		addPath("/");
		addPath(installPath.toOSString());
		addPath(getDirectory("/rdf/").toOSString());
		final Model model = ModelFactory.createDefaultModel();
		// model.setNsPrefix("ect", Schema.getURI());
		model.setNsPrefix("ectguid", RDFStatement.GUID_NAMESPACE);

		final List<?> rules = loadRules(getDirectory("/rules/").toFile());
		final ECTRuleReasoner reasoner = new ECTRuleReasoner(rules);
		reasoner.setMode(GenericRuleReasoner.FORWARD_RETE);
		// reasoner.setTraceOn(true);

		final ECTRuleInfGraph infGraph = (ECTRuleInfGraph) reasoner.bind(model.getGraph());
		infGraph.addECTInferenceEventListener(dataspace);
		// infGraph.setTraceOn(true);

		infModel = new ECTInfModel(infGraph);

		new Thread(new Runnable()
		{
			public void run()
			{
				final MessageConsole console = new MessageConsole("Standard Out", null);
				ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
				System.setErr(new PrintStream(console.newOutputStream()));
				System.setOut(new PrintStream(console.newOutputStream()));

				loadRDF(model, getDirectory("/ontologies/").toFile());

				final ECTRDF mapper = new ECTRDF(infModel);

				new Thread(mapper, "ECT to RDF Mapper").start();

				dataspace.addECTEventListener(mapper);
				dataspace.start();
			}
		}).start();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(final BundleContext context) throws Exception
	{
		dataspace.stop();
		super.stop(context);
		plugin = null;
	}

	private URL getFile(final String filePath)
	{
		for (final String pathString : paths)
		{
			IPath path = new Path(pathString);
			path = path.append(filePath);
			final File file = path.toFile();
			if (file.exists())
			{
				try
				{
					return file.toURI().toURL();
				}
				catch (final MalformedURLException e)
				{
					e.printStackTrace();
				}
			}
			final URL url = FileLocator.find(getBundle(), path, null);
			if (url != null) { return url; }
		}
		return null;
	}

	@Override
	protected void initializeImageRegistry(final ImageRegistry reg)
	{
		super.initializeImageRegistry(reg);
		reg.put("property", getImageDescriptor("icons/property_obj.gif"));
		reg.put("read_only", getImageDescriptor("icons/read_only.gif"));
		reg.put("class", getImageDescriptor("icons/class_obj.gif"));
		// reg.put("capability",
		// getImageDescriptor("icons/capability_obj.gif"));
		// reg.put("component", getImageDescriptor("icons/component_obj.gif"));
		// reg.put("request",
		// getImageDescriptor("icons/componentRequest_obj.gif"));
		reg.put("rule", getImageDescriptor("icons/rule_obj.gif"));
		reg.put("restriction", getImageDescriptor("icons/restriction_obj.gif"));
		reg.put("connection", getImageDescriptor("icons/connect.gif"));

		reg.put("refresh", getImageDescriptor("icons/refresh.gif"));

		reg.put("openthing", getImageDescriptor("icons/openthing.gif"));
		reg.put("thing", getImageDescriptor("icons/thing.gif"));

		reg.put("dataspace", getImageDescriptor("icons/dataspace.gif"));

		reg.put("listLayout", getImageDescriptor("icons/listLayout.gif"));
		reg.put("treeLayout", getImageDescriptor("icons/treeLayout.gif"));

		reg.put("warning", getImageDescriptor("icons/warning.gif"));

		reg.put("connection_wizard", getImageDescriptor("icons/connection_wiz.gif"));

		reg.put("dataspace_connection", getImageDescriptor("icons/connectDataspace.gif"));
	}
}