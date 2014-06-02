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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DC_11;
import com.hp.hpl.jena.vocabulary.RDF;

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.ui.ModelLabelProvider;
import equip.data.DataManager;
import equip.data.DataProxy;
import equip.data.DataSession;
import equip.data.GUID;
import equip.data.Server;
import equip.data.TupleImpl;
import equip.data.beans.DataspaceBean;
import equip.data.beans.DataspaceEvent;
import equip.data.beans.DataspaceEventListener;
import equip.data.beans.DataspaceInactiveException;
import equip.discovery.DiscoveryServerAgent;
import equip.discovery.DiscoveryServerAgentImpl;
import equip.discovery.ServerDiscoveryInfo;
import equip.discovery.ServerDiscoveryInfoImpl;
import equip.ect.Capability;
import equip.ect.CompInfo;
import equip.ect.ComponentAdvert;
import equip.ect.ComponentProperty;
import equip.ect.ComponentRequest;
import equip.ect.Container;
import equip.ect.ContainerManager;
import equip.ect.PropertyLinkRequest;
import equip.ect.RDFStatement;
import equip.ect.util.DirectoryMonitor;

public class DataspaceMonitor implements ECTInferenceEventListener
{
	private static final boolean debug = false;

	private ContainerManager containerManager;
	private DataProxy dataProxy;
	private final List<DataSession> dataspaceEventListeners = new ArrayList<DataSession>();
	private final List<ECTEventListener> listeners = new ArrayList<ECTEventListener>();
	private final PropertyChangeListener propertyListener = new PropertyChangeListener()
	{
		public void propertyChange(final PropertyChangeEvent evt)
		{
			for (final PropertyChangeListener listener : dataspaceListeners)
			{
				listener.propertyChange(evt);
			}
		}
	};
	protected final List<PropertyChangeListener> dataspaceListeners = new ArrayList<PropertyChangeListener>();

	protected DataspaceBean dataspace;

	public GUID addComponentPropertyLink(final Resource source, final Resource target)
	{
		if (debug)
		{
			System.out.println("Add Property Link: " + ModelLabelProvider.text(source) + "->"
					+ ModelLabelProvider.text(target));
		}
		if (source == null || target == null)
		{
			System.err.println("Cannot create property link for null values");
			return null;
		}
		final GUID sourceGUID = RDFStatement.urlToGUID(source.getURI());
		final String sourceName = source.getProperty(DC_11.title).getString();
		final GUID sourceComponent = RDFStatement.urlToGUID(((Resource) source.getProperty(Schema.softwareComponent).getObject()).getURI());

		final GUID targetGUID = RDFStatement.urlToGUID(target.getURI());
		final String targetName = target.getProperty(DC_11.title).getString();
		final GUID targetComponent = RDFStatement.urlToGUID(((Resource) target.getProperty(Schema.softwareComponent).getObject()).getURI());

		final PropertyLinkRequest linkreq = new PropertyLinkRequest(dataspace.allocateId());
		linkreq.setSourceComponentID(sourceComponent);
		linkreq.setSourcePropertyName(sourceName);
		linkreq.setSourcePropID(sourceGUID);
		linkreq.setDestComponentID(targetComponent);
		linkreq.setDestinationPropertyName(targetName);
		linkreq.setDestinationPropID(targetGUID);

		try
		{
			linkreq.addtoDataSpacePersistent(dataspace, null);
			return linkreq.getID();
		}
		catch (final DataspaceInactiveException ex)
		{
			System.out.println("Error: Can't add Link to dataspace - inactive");
			ex.printStackTrace();
			return null;
		}
	}

	public GUID addComponentRequest(final Resource capability) throws DataspaceInactiveException
	{
		if (debug)
		{
			System.out.println("Request Component: " + ModelLabelProvider.text(capability));
		}
		final GUID capGUID = RDFStatement.urlToGUID(capability.getURI());

		final GUID id = dataspace.allocateId();
		final ComponentRequest req = new ComponentRequest(id);
		req.setCapabilityID(capGUID);

		final StmtIterator iterator = capability.listProperties(Schema.softwareContainer);
		while (iterator.hasNext())
		{
			final Statement statement = iterator.nextStatement();
			try
			{
				final GUID containerGUID = RDFStatement.urlToGUID(statement.getResource().getURI());
				req.setContainerID(containerGUID);
				break;
			}
			catch (final Exception e)
			{

			}
		}

		// unused : req.setRequestID(r.id.toString());
		// unused: req.setHostID(cap.getHostID().toString());
		req.addtoDataSpacePersistent(dataspace, null);
		return id;
	}

	public void addECTEventListener(final ECTEventListener listener)
	{
		listeners.add(listener);
	}

	public void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		dataspaceListeners.add(listener);
	}

	public void disconnect()
	{

	}

	public String getDataspaceURL()
	{
		return dataspace.getDataspaceUrl();
	}

	public void inferenceEvent(final ECTInferenceEvent event)
	{
		final Model model = PhysConfPlugin.getDefault().getModel();
		final Resource source = (Resource) model.getRDFNode(event.getTriple().getSubject());
		final Resource target = (Resource) model.getRDFNode(event.getTriple().getObject());
		final Property predicate = model.getProperty(event.getTriple().getPredicate().getURI());

		if (event.isAdded())
		{
			if (predicate.equals(Schema.capability) || predicate.equals(Schema.createComponentRequest))
			{
				checkComponentDetails(model, source);
			}
			else if (predicate.equals(Schema.createPropertyLink))
			{
				addComponentPropertyLink(source, target);
			}
		}
		else
		{
			if (predicate.equals(Schema.capability) || predicate.equals(Schema.createComponentRequest))
			{
				checkComponentDetails(model, source);
			}
			else if (predicate.equals(Schema.createPropertyLink))
			{
				final ResIterator iterator = model.listSubjectsWithProperty(RDF.type,
						Schema.SoftwareComponentPropertyLink);
				while (iterator.hasNext())
				{
					final Resource resource = iterator.nextResource();
					if (resource.hasProperty(Schema.source, source) && resource.hasProperty(Schema.destination, target))
					{
						remove(RDFStatement.urlToGUID(resource.getURI()));
						// Break?
					}
				}
			}
		}
	}

	public boolean isConnected()
	{
		return dataspace != null && dataspace.isConnected();
	}

	public void remove(final GUID requestGUID)
	{
		try
		{
			dataspace.delete(requestGUID);
		}
		catch (final DataspaceInactiveException e)
		{
			e.printStackTrace();
		}
	}

	public void removeECTEventListener(final ECTEventListener listener)
	{
		listeners.remove(listener);
	}

	public void setPreferences(final DataspacePreferences prefs)
	{
		stop();
		connect(prefs);

		final IEclipsePreferences prefStore = new InstanceScope().getNode("ect.equip.physconf");
		prefStore.put("dataspaceURL", prefs.getDataspaceURL());
		prefStore.putBoolean("dataspaceOptionConnect", prefs.getDataspaceOptionConnect());
		prefStore.putBoolean("dataspaceOptionCreate", prefs.getDataspaceOptionCreate());
		prefStore.putBoolean("dataspaceOptionStartup", prefs.getDataspaceOptionStartup());

		try
		{
			prefStore.flush();
		}
		catch (final BackingStoreException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setPropertyValue(final Resource property, final String value)
	{
		if (debug)
		{
			System.out.println("Set Property: " + ModelLabelProvider.text(property) + " = " + value);
		}
		if (property == null)
		{
			System.err.println("Cannot set null property");
			return;
		}
		try
		{
			final GUID myPropertyID = dataspace.allocateId();
			final GUID myComponentID = dataspace.allocateId();
			final ComponentProperty prop = new ComponentProperty(myPropertyID);
			prop.setPropertyName("SetValuePopup");
			prop.setPropertyClass(String.class);
			prop.setPropertyValue(value);
			prop.setComponentID(myComponentID);
			final GUID myLinkID = dataspace.allocateId();
			final PropertyLinkRequest link = new PropertyLinkRequest(myLinkID);
			link.setSourcePropertyName("SetValuePopup");
			link.setSourcePropID(myPropertyID);
			link.setSourceComponentID(myComponentID);
			link.setDestinationPropertyName(property.getProperty(DC_11.title).getObject().toString());
			link.setDestinationPropID(RDFStatement.urlToGUID(property.getURI()));
			final Resource component = (Resource) property.getProperty(Schema.softwareComponent).getObject();
			link.setDestComponentID(RDFStatement.urlToGUID(component.getURI()));

			prop.addtoDataSpacePersistent(dataspace, null);
			link.addtoDataSpacePersistent(dataspace, null);
			final Thread t = new Thread("Set Property: " + ModelLabelProvider.text(property) + " = " + value)
			{
				@Override
				public void run()
				{
					while (true)
					{
						try
						{
							sleep(1000);
						}
						catch (final InterruptedException e1)
						{
							e1.printStackTrace();
						}
						if (property.hasProperty(RDF.value, value))
						{
							try
							{
								dataspace.delete(myLinkID);
								dataspace.delete(myPropertyID);
							}
							catch (final Exception e)
							{
								System.err.println("ERROR deleting temp link/property in SetValuePopup: " + e);
								e.printStackTrace(System.err);
							}
							return;
						}
					}
				}
			};
			t.start();
		}
		catch (final Exception e)
		{
			System.err.println("Error putting SetValuePopup property or link into DS: " + e);
			e.printStackTrace(System.err);
		}
	}

	public void start()
	{
		final DataspacePreferences prefs = new DataspacePreferencesImpl();
		if (prefs.getDataspaceOptionStartup())
		{
			connect(prefs);
		}
	}

	public void stop()
	{
		if (dataProxy != null)
		{
			dataProxy.deactivate();
			// dataProxy.terminate();
			dataProxy = null;
		}
		if (dataspace != null)
		{
			for (final DataSession session : dataspaceEventListeners)
			{
				try
				{
					dataspace.removeDataspaceEventListener(session);
				}
				catch (final DataspaceInactiveException e)
				{
					e.printStackTrace();
				}
			}
			dataspaceEventListeners.clear();
			dataspace.removePropertyChangeListener(propertyListener);
			dataspace = null;
		}
	}

	private void addDataspaceEventListener(final CompInfo info, final DataspaceEventListener listener)
	{
		try
		{
			dataspaceEventListeners.add(dataspace.addDataspaceEventListener(info.tuple, false, listener));
		}
		catch (final DataspaceInactiveException e)
		{
			e.printStackTrace();
		}
	}

	private void checkComponentDetails(final Model model, final Resource componentDetails)
	{
		if (componentDetails.hasProperty(Schema.createComponentRequest)
				&& componentDetails.hasProperty(Schema.capability))
		{
			if (!componentDetails.hasProperty(Schema.createdComponentRequest))
			{
				try
				{
					final GUID id = addComponentRequest(componentDetails.getProperty(Schema.capability).getResource());
					model.add(componentDetails, Schema.createdComponentRequest,
							model.createResource(RDFStatement.GUIDToUrl(id)));
				}
				catch (final Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		else
		{
			if (componentDetails.hasProperty(Schema.createdComponentRequest))
			{
				final Resource componentRequest = componentDetails.getProperty(Schema.createdComponentRequest).getResource();
				remove(RDFStatement.urlToGUID(componentRequest.getURI()));
				componentDetails.removeAll(Schema.createdComponentRequest);
			}
		}
	}

	private void connect(final DataspacePreferences prefs)
	{
		if (prefs.getDataspaceOptionCreate())
		{
			createDataspace(prefs.getDataspaceURL());
		}
		else if (prefs.getDataspaceOptionConnect())
		{
			connectDataspace(prefs.getDataspaceURL());
		}

		loadComponents();
	}

	private void connectDataspace(final String url)
	{
		if (isConnected())
		{
			stop();
		}
		dataspace = new DataspaceBean();
		dataspace.addPropertyChangeListener("connected", propertyListener);
		try
		{
			dataspace.setDataspaceUrl(url, false);
		}
		catch (final DataspaceInactiveException e1)
		{
			e1.printStackTrace();
		}
		addDataspaceEventListener(new Capability((GUID) null), new DataspaceEventListener()
		{
			public void dataspaceEvent(final DataspaceEvent event)
			{
				if (event.getAddItem() != null)
				{
					fireECTEvent(new ECTEvent(new Capability((TupleImpl) event.getAddItem()), ECTEvent.Type.ADDED));
				}
				else if (event.getDeleteId() != null)
				{
					fireECTEvent(new ECTEvent(new Capability((TupleImpl) event.getOldValue()), ECTEvent.Type.REMOVED));
				}
				else if (event.getUpdateItem() != null)
				{
					fireECTEvent(new ECTEvent(new Capability((TupleImpl) event.getOldValue()), ECTEvent.Type.UPDATED));
				}
			}
		});
		addDataspaceEventListener(new ComponentAdvert((GUID) null), new DataspaceEventListener()
		{
			public void dataspaceEvent(final DataspaceEvent event)
			{
				if (event.getAddItem() != null)
				{
					final ComponentAdvert component = new ComponentAdvert((TupleImpl) event.getAddItem());
					fireECTEvent(new ECTEvent(component, ECTEvent.Type.ADDED));
					// properties?
					final ComponentProperty pt = new ComponentProperty((GUID) null);
					pt.setComponentID(component.getID());
					try
					{
						final ComponentProperty[] properties = pt.copyCollectAsComponentProperty(dataspace);
						for (final ComponentProperty element : properties)
						{
							fireECTEvent(new ECTEvent(element, ECTEvent.Type.ADDED));
						}
					}
					catch (final Exception e)
					{
						System.err.println("ERROR collecting existing properties for new component "
								+ component.getID() + ": " + e);
						e.printStackTrace(System.err);
					}
				}
				else if (event.getDeleteId() != null)
				{
					final ComponentAdvert component = new ComponentAdvert((TupleImpl) event.getOldValue());
					fireECTEvent(new ECTEvent(component, ECTEvent.Type.REMOVED));
				}
			}
		});
		addDataspaceEventListener(new ComponentProperty((GUID) null), new DataspaceEventListener()
		{
			public void dataspaceEvent(final DataspaceEvent event)
			{
				if (event.getAddItem() != null)
				{
					fireECTEvent(new ECTEvent(new ComponentProperty((TupleImpl) event.getAddItem()),
							ECTEvent.Type.ADDED));
				}
				else if (event.getDeleteId() != null)
				{
					fireECTEvent(new ECTEvent(new ComponentProperty((TupleImpl) event.getOldValue()),
							ECTEvent.Type.REMOVED));
				}
				else if (event.getUpdateItem() != null)
				{
					fireECTEvent(new ECTEvent(new ComponentProperty((TupleImpl) event.getUpdateItem()),
							ECTEvent.Type.UPDATED));
				}
			}
		});
		addDataspaceEventListener(new ComponentRequest((GUID) null), new DataspaceEventListener()
		{
			public void dataspaceEvent(final DataspaceEvent event)
			{
				if (event.getAddItem() != null)
				{
					fireECTEvent(new ECTEvent(new ComponentRequest((TupleImpl) event.getAddItem()), ECTEvent.Type.ADDED));
				}
				else if (event.getDeleteId() != null)
				{
					fireECTEvent(new ECTEvent(new ComponentRequest((TupleImpl) event.getOldValue()),
							ECTEvent.Type.REMOVED));
				}
				else if (event.getUpdateItem() != null)
				{
					fireECTEvent(new ECTEvent(new ComponentRequest((TupleImpl) event.getUpdateItem()),
							ECTEvent.Type.UPDATED));
				}
			}
		});
		addDataspaceEventListener(new PropertyLinkRequest((GUID) null), new DataspaceEventListener()
		{
			public void dataspaceEvent(final DataspaceEvent event)
			{
				if (event.getAddItem() != null)
				{
					fireECTEvent(new ECTEvent(new PropertyLinkRequest((TupleImpl) event.getAddItem()),
							ECTEvent.Type.ADDED));
				}
				else if (event.getDeleteId() != null)
				{
					fireECTEvent(new ECTEvent(new PropertyLinkRequest((TupleImpl) event.getOldValue()),
							ECTEvent.Type.REMOVED));
				}
				else if (event.getUpdateItem() != null)
				{
					fireECTEvent(new ECTEvent(new PropertyLinkRequest((TupleImpl) event.getOldValue()),
							ECTEvent.Type.UPDATED));
				}
			}
		});
		addDataspaceEventListener(new Container((GUID) null), new DataspaceEventListener()
		{
			public void dataspaceEvent(final DataspaceEvent event)
			{
				if (event.getAddItem() != null)
				{
					fireECTEvent(new ECTEvent(new Container((TupleImpl) event.getAddItem()), ECTEvent.Type.ADDED));
				}
				else if (event.getDeleteId() != null)
				{
					fireECTEvent(new ECTEvent(new Container((TupleImpl) event.getOldValue()), ECTEvent.Type.REMOVED));
				}
				else if (event.getUpdateItem() != null)
				{
					fireECTEvent(new ECTEvent(new Container((TupleImpl) event.getOldValue()), ECTEvent.Type.UPDATED));
				}
			}
		});
	}

	private void createDataspace(final String url)
	{
		System.err.println("Starting dataspace at " + url);
		dataProxy = DataManager.getInstance().getDataspace(url, DataManager.DATASPACE_SERVER, true);
		final equip.net.ServerURL surl = new equip.net.ServerURL(((Server) dataProxy).getMoniker());

		final ServerDiscoveryInfo[] servers = new ServerDiscoveryInfo[1];
		servers[0] = new ServerDiscoveryInfoImpl();
		servers[0].serviceTypes = new String[1];
		servers[0].serviceTypes[0] = "equip.data.DataProxy:2.0";
		/*
		 * should be this but that means my new version of data...
		 * DATASPACE_SERVICE_TYPE.value
		 */
		servers[0].groups = new String[1];
		servers[0].groups[0] = "equip.ect.default";
		System.err.println("- group = " + servers[0].groups[0]);
		servers[0].urls = new String[1];
		servers[0].urls[0] = surl.getURL(); // args[0];
		System.err.println("- url = " + servers[0].urls[0]);

		// go...
		final DiscoveryServerAgent agent = new DiscoveryServerAgentImpl();
		agent.startDefault(servers);

		try
		{
			dataspace = new DataspaceBean(dataProxy);

			connectDataspace(url);
		}
		catch (final DataspaceInactiveException e)
		{
			e.printStackTrace();
		}
	}

	private void loadComponents()
	{
		if (containerManager == null)
		{
			containerManager = new ContainerManager(dataspace, "ARTECT", dataspace.allocateId());

			try
			{
				final DirectoryMonitor dirMon = new DirectoryMonitor(PhysConfPlugin.getDirectory("/components/").toFile(), true,
						false);
				final ComponentManager compManager = new ComponentManager(containerManager);
				compManager.addJarDirectory(PhysConfPlugin.getDirectory("/common/").toFile());
				compManager.addLibraryDirectory(PhysConfPlugin.getInstallDirectory().toFile());
				dirMon.addDirectoryEventListener(compManager);
				new Thread(dirMon).start();
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			// TODO Reload components.
		}
	}

	protected void fireECTEvent(final ECTEvent event)
	{
		for (final ECTEventListener listener : listeners)
		{
			listener.ECTEvent(event);
		}
	}
}