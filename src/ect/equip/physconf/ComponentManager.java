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

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import equip.data.beans.DataspaceInactiveException;
import equip.ect.BeanDescriptorHelper;
import equip.ect.Capability;
import equip.ect.ContainerManager;
import equip.ect.util.DirectoryEventListener;

public class ComponentManager implements DirectoryEventListener
{
	private class ContainerClassLoader extends URLClassLoader
	{
		private final List<String> libraryDirectories = new ArrayList<String>();

		public ContainerClassLoader(final ClassLoader cl)
		{
			super(new URL[] {}, cl);
			final String commonClassPath = System.getProperty("containerClassPath", null);
			if (commonClassPath != null)
			{
				final java.util.StringTokenizer toks = new java.util.StringTokenizer(commonClassPath,
						java.io.File.pathSeparator, false);
				while (toks.hasMoreTokens())
				{
					final String t = toks.nextToken();
					if (t.equals(""))
					{
						continue;
					}
					System.out.println("Add to common class path: " + t);
					try
					{
						addURL(new File(t).toURI().toURL());
					}
					catch (final Exception e)
					{
						System.err.println("ERROR adding file " + t + ": " + e);
					}
				}
			}
		}

		public void addLibraryDir(final String libraryDir)
		{
			libraryDirectories.add(libraryDir);
		}

		public synchronized void forceResolve(final Class<?> c)
		{
			resolveClass(c);
		}

		@Override
		public Class<?> loadClass(final String name) throws ClassNotFoundException
		{
			return loadClass(name, false);

		}

		@Override
		protected synchronized void addURL(final URL url)
		{
			final URL urls[] = getURLs();
			for (final URL url2 : urls)
			{
				if (url2.equals(url)) { return; }
			}
			super.addURL(url);
		}

		@Override
		protected String findLibrary(final String libname)
		{
			for (final String libDir : libraryDirectories)
			{
				final File libfile = new File(libDir, System.mapLibraryName(libname));
				if (libfile.exists()) { return libfile.getAbsolutePath(); }
			}

			return super.findLibrary(libname);
		}

		@Override
		protected synchronized Class<?> loadClass(final String name, final boolean resolve)
				throws ClassNotFoundException
		{
			Class<?> cl = findLoadedClass(name);
			if (cl != null) { return cl; }
			try
			{
				cl = findClass(name);
			}
			catch (final ClassNotFoundException e)
			{
				// try parent
				return super.loadClass(name, resolve);
			}
			return cl;
		}
	}

	private final ContainerClassLoader classLoader = new ContainerClassLoader(getClass().getClassLoader());

	public static final String JAR_EXTENSION = ".jar";

	public static final String BEAN_CLASSNAME_SUFFIX = "BeanInfo.class";

	private final Map<File, List<Capability>> capabilityMap = new HashMap<File, List<Capability>>();

	private final ContainerManager containerManager;

	public ComponentManager(final ContainerManager containerManager)
	{
		super();
		this.containerManager = containerManager;
	}

	public void addJarDirectory(final File jarDir)
	{
		if (!jarDir.isDirectory()) { return; }
		classLoader.addLibraryDir(jarDir.toString());
		final File[] jars = jarDir.listFiles(new FileFilter()
		{
			public boolean accept(final File pathname)
			{
				return pathname.getName().endsWith(JAR_EXTENSION);
			}
		});
		for (final File jarFile : jars)
		{
			try
			{
				classLoader.addURL(jarFile.toURI().toURL());
			}
			catch (final MalformedURLException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void addLibraryDirectory(final File libDir)
	{
		if (!libDir.isDirectory()) { return; }
		classLoader.addLibraryDir(libDir.toString());
		try
		{
			classLoader.addURL(libDir.toURI().toURL());
		}
		catch (final MalformedURLException e)
		{
			e.printStackTrace();
		}
	}

	public void fileAdd(final File file)
	{

	}

	public void fileAddComplete(final File file)
	{
		if (!file.getName().endsWith(JAR_EXTENSION)) { return; }
		addCapabilities(file);
	}

	public void fileDeleted(final File file)
	{
		if (!file.getName().endsWith(JAR_EXTENSION)) { return; }
		removeCapabilities(file);
	}

	public void fileModified(final File file)
	{
		if (!file.getName().endsWith(JAR_EXTENSION)) { return; }
		fileDeleted(file);
		fileAddComplete(file);
	}

	private void addCapabilities(final File file)
	{
		try
		{
			final Class<?> beanClasses[] = loadFromJarFile(file);
			final List<Capability> capabilities = new ArrayList<Capability>();

			if (beanClasses != null)
			{
				for (final Class<?> beanClass : beanClasses)
				{
					final Capability capability = addCapability(beanClass);
					if (capability != null)
					{
						capabilities.add(capability);
					}
				}
			}

			capabilityMap.put(file, capabilities);
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	private Capability addCapability(final Class<?> beanClass)
	{
		if (containerManager.dataspace == null || beanClass == null) { return null; }

		BeanInfo beaninfo = null;
		java.beans.Introspector.setBeanInfoSearchPath(new String[] { "." });
		try
		{
			beaninfo = java.beans.Introspector.getBeanInfo(beanClass, beanClass.getSuperclass());
		}
		catch (final IntrospectionException e)
		{
			e.printStackTrace();
			return null;
		}

		final BeanDescriptor beandesc = beaninfo.getBeanDescriptor();
		final Capability capability = new Capability(containerManager.dataspace.allocateId());

		capability.setCapabilityName(beandesc.getName());
		capability.setCapablityClass(beandesc.getBeanClass().toString());
		capability.setContainerID(containerManager.getContainerID());
		capability.setHostID(containerManager.getHostID().toString());

		BeanDescriptorHelper.copyInformation(beaninfo, capability, true);

		try
		{
			capability.addtoDataSpace(containerManager.dataspace);
			containerManager.capabilityClasses.put(capability.getID(), beanClass);
			return capability;
		}
		catch (final DataspaceInactiveException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	private Class<?>[] loadFromJarFile(final File file) throws IOException
	{
		if (file != null && file.exists() && file.getName().toLowerCase().endsWith(JAR_EXTENSION))
		{
			final URL jarURL = file.toURI().toURL();
			classLoader.addURL(jarURL);
			final JarFile jarFile = new JarFile(file);
			final Enumeration<JarEntry> entries = jarFile.entries();
			final List<String> classNames = new ArrayList<String>();
			while (entries.hasMoreElements())
			{
				final JarEntry entry = entries.nextElement();
				final String name = entry.getName();
				if (name.endsWith(BEAN_CLASSNAME_SUFFIX))
				{
					final String className = name.replace('/', '.').substring(0,
							name.length() - BEAN_CLASSNAME_SUFFIX.length());
					classNames.add(className);
				}
			}
			final List<Class<?>> classes = new ArrayList<Class<?>>();
			for (final String className : classNames)
			{
				try
				{
					final Class<?> beanClass = classLoader.loadClass(className);
					classLoader.forceResolve(beanClass);
					System.err.println("Component Class " + className + " OK");
					classes.add(beanClass);
				}
				catch (final Exception e)
				{
					System.err.println("ERROR loading bean class " + className + ": " + e);
					e.printStackTrace();
				}
			}
			return classes.toArray(new Class[classes.size()]);
		}
		return null;
	}

	private void removeCapabilities(final File file)
	{
		System.out.println("Removing Capabilities of " + file.toString());

		final List<Capability> capabilities = capabilityMap.get(file);

		for (final Capability capability : capabilities)
		{
			try
			{
				containerManager.dataspace.delete(capability.getID());
				containerManager.capabilityClasses.remove(capability);
			}
			catch (final DataspaceInactiveException e)
			{
				e.printStackTrace();
			}
		}
	}
}