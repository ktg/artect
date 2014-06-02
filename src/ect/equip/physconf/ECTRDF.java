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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DC_11;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.ui.commands.StatementListCommand;
import ect.equip.physconf.ui.commands.StatementQueueCommand;
import equip.data.StringBox;
import equip.ect.Capability;
import equip.ect.CompInfo;
import equip.ect.ComponentAdvert;
import equip.ect.ComponentProperty;
import equip.ect.ComponentRequest;
import equip.ect.ConnectionPointTypeException;
import equip.ect.Container;
import equip.ect.PropertyLinkRequest;
import equip.ect.RDFStatement;

import org.eclipse.swt.widgets.Display;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class ECTRDF implements Runnable, ECTEventListener
{
	private final List<ECTEvent> events = new ArrayList<ECTEvent>();

	private final StatementQueueCommand statementQueue;
	private final Resource image;
	protected final Model model;

	/**
	 * 
	 */
	public ECTRDF(final Model model)
	{
		this.model = model;
		image = model.createResource();
		statementQueue = new StatementQueueCommand(model)
		{
			@Override
			public void execute()
			{
				final Iterator<StatementListCommand> statementEvents = clear();
				Display.getDefault().asyncExec(new Runnable()
				{
					public void run()
					{
						while (statementEvents.hasNext())
						{
							statementEvents.next().execute();
						}
					}
				});
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * ect.equip.physconf.ECTEventListener#ECTEvent(ect.equip.physconf.ECTEvent)
	 */
	public void ECTEvent(final ECTEvent event)
	{
		synchronized (this)
		{
			events.add(event);
			notifyAll();
		}
	}

	public void run()
	{
		while (true)
		{
			synchronized (this)
			{
				while (events.isEmpty())
				{
					try
					{
						wait(20000);
					}
					catch (final InterruptedException e)
					{
						e.printStackTrace();
					}
				}
				do
				{
					while (!events.isEmpty())
					{
						try
						{
							handleEvent(events.remove(0));
						}
						catch (final Exception e)
						{
							e.printStackTrace();
						}
					}
					try
					{
						wait(100);
					}
					catch (final InterruptedException e)
					{
						e.printStackTrace();
					}
				}
				while (!events.isEmpty());
				statementQueue.execute();
			}
		}
	}

	private void handleEvent(final ECTEvent event)
	{
		final String guid = RDFStatement.GUIDToUrl(event.getID());
		final StatementListCommand statementList = statementQueue.getEventList(event.getType());
		if (event.getType() == ECTEvent.Type.ADDED)
		{
			final Resource resource = model.createResource(guid);
			statementList.add(resource, DC_11.identifier, event.getID());

			final CompInfo compInfo = event.getObject();
			final StringBox description = (StringBox) compInfo.getAttributeValue("htmlDescription");
			if (description != null)
			{
				statementList.add(resource, RDFS.comment, description.value);
			}

			if (compInfo instanceof Container)
			{
				final Container container = (Container) compInfo;
				statementList.add(resource, RDF.type, Schema.SoftwareContainer);
				final String hostID = container.getHostID();
				if (hostID != null)
				{
					final Resource host = model.createResource(RDFStatement.GUID_NAMESPACE + hostID);
					statementList.add(host, Schema.classification, "Computers");
					statementList.add(host, RDF.type, Schema.Computer);
					statementList.add(host, RDFS.label, "Computer '" + hostID + "'");
					statementList.add(host, DC_11.identifier, hostID);
					statementList.add(host, Schema.image, image);
					statementList.add(image, RDF.type, Schema.MediaFileLocation);
					statementList.add(image, Schema.filePath, "images/shuttle.jpg");
					statementList.add(resource, Schema.host, host);
					statementList.add(resource, RDFS.label, hostID);
				}
			}
			else if (compInfo instanceof Capability)
			{
				final Capability capability = (Capability) compInfo;
				statementList.add(resource, RDF.type, Schema.SoftwareComponentCapability);
				final StringBox value = (StringBox) capability.getAttributeValue("displayName");
				if (value != null)
				{
					statementList.add(resource, RDFS.label, value.value);
				}
				else
				{
					statementList.add(resource, RDFS.label, capability.getCapabilityName());
				}
				statementList.add(resource, DC_11.title, capability.getCapabilityName());

				final String containerID = RDFStatement.GUIDToUrl(capability.getContainerID());
				if (containerID != null)
				{
					final Resource container = model.createResource(containerID);
					statementList.add(resource, Schema.softwareContainer, container);
				}

				String classification = capability.getClassification();
				if (classification == null)
				{
					classification = "Unclassified";
				}
				statementList.add(resource, Schema.classification, classification);
			}
			else if (compInfo instanceof ComponentRequest)
			{
				final ComponentRequest componentRequest = (ComponentRequest) compInfo;
				statementList.add(resource, RDF.type, Schema.SoftwareComponentRequest);
				final String containerID = RDFStatement.GUIDToUrl(componentRequest.getContainerID());
				if (containerID != null)
				{
					final Resource container = model.createResource(containerID);
					statementList.add(resource, Schema.softwareContainer, container);
					statementList.add(container, RDF.type, Schema.SoftwareContainer);
				}

				final String capURL = RDFStatement.GUIDToUrl(componentRequest.getCapabilityID());
				final Resource capability = model.createResource(capURL);
				statementList.add(resource, Schema.capability, capability);
			}
			else if (compInfo instanceof ComponentAdvert)
			{
				final ComponentAdvert component = (ComponentAdvert) compInfo;
				statementList.add(resource, RDF.type, Schema.SoftwareComponent);
				if (!resource.hasProperty(RDFS.label))
				{
					final StringBox value = (StringBox) component.getAttributeValue("displayName");
					if (value != null)
					{
						statementList.add(resource, RDFS.label, value.value);
					}
					else
					{
						statementList.add(resource, RDFS.label, component.getComponentName());
					}
				}

				final StringBox classificationBox = (StringBox) component.getAttributeValue("classification");
				String classification;
				if (classificationBox == null)
				{
					classification = "Unclassified";
				}
				else
				{
					classification = classificationBox.value;
				}
				statementList.add(resource, Schema.classification, classification);

				statementList.add(resource, DC_11.title, component.getComponentName());
				final String containerID = RDFStatement.GUIDToUrl(component.getContainerID());
				if (containerID != null)
				{
					final Resource container = model.createResource(containerID);
					statementList.add(resource, Schema.softwareContainer, container);
					statementList.add(container, RDF.type, Schema.SoftwareContainer);
				}

				if (component.getCapabilityID() != null)
				{
					final String capURL = RDFStatement.GUIDToUrl(component.getCapabilityID());
					final Resource capability = model.createResource(capURL);
					statementList.add(resource, Schema.capability, capability);
				}

				if (component.getComponentRequestID() != null)
				{
					final String requestURL = RDFStatement.GUIDToUrl(component.getComponentRequestID());
					final Resource request = model.createResource(requestURL);
					statementList.add(resource, Schema.request, request);
				}
			}
			else if (compInfo instanceof ComponentProperty)
			{
				final ComponentProperty property = (ComponentProperty) compInfo;
				if (!"SetValuePopup".equals(property.getPropertyName()))
				{
					statementList.add(resource, RDF.type, Schema.SoftwareComponentProperty);
					final StringBox value = (StringBox) property.getAttributeValue("displayName");
					if (value != null)
					{
						statementList.add(resource, RDFS.label, value.value);
					}
					else
					{
						statementList.add(resource, RDFS.label, property.getPropertyName());
					}
					statementList.add(resource, DC_11.title, property.getPropertyName());
					statementList.add(resource, Schema.readOnly, property.isReadonly());
					final String propertyClass = property.getPropertyClass();
					statementList.add(resource, Schema.propertyType, propertyClass);
					if (propertyClass.indexOf('.') >= 0 && !propertyClass.startsWith("java.lang."))
					{
						statementList.add(resource, Schema.isVisible, false);
					}
					final String propertyConnectionType = property.getConnectionPointType();
					statementList.add(resource, Schema.propertyConnectionType, propertyConnectionType);

					try
					{
						if (ComponentProperty.CONNECTION_POINT_PROPERTY_VALUE.equals(propertyConnectionType))
						{
							final String propertyValue = property.getPropertyValueAsString();
							if (propertyValue != null)
							{
								statementList.add(resource, RDF.value, propertyValue);
							}
						}
					}
					catch (final Exception e)
					{
						e.printStackTrace();
					}

					final String compURL = RDFStatement.GUIDToUrl(property.getComponentID());
					final Resource component = model.createResource(compURL);
					statementList.add(resource, Schema.softwareComponent, component);
				}
			}
			else if (compInfo instanceof PropertyLinkRequest)
			{
				final PropertyLinkRequest request = (PropertyLinkRequest) compInfo;
				if (!"SetValuePopup".equals(request.getSourcePropertyName()))
				{
					statementList.add(resource, RDF.type, Schema.SoftwareComponentPropertyLink);
					final String sourceName = request.getSourcePropertyName();
					final String destinationName = request.getDestinationPropertyName();
					statementList.add(resource, RDFS.label, sourceName + " -> " + destinationName);

					final String sourceURL = RDFStatement.GUIDToUrl(request.getSourcePropID());
					Resource property = model.createResource(sourceURL);
					statementList.add(resource, Schema.source, property);
					final String destURL = RDFStatement.GUIDToUrl(request.getDestinationPropID());
					property = model.createResource(destURL);
					statementList.add(resource, Schema.destination, property);
				}
			}
		}
		else if (event.getType() == ECTEvent.Type.REMOVED)
		{
			final Resource resource = model.getResource(guid);
			final StmtIterator iterator = resource.listProperties();
			while (iterator.hasNext())
			{
				final Statement statement = iterator.nextStatement();
				if (!Schema.xOffset.equals(statement.getPredicate())
						&& !Schema.yOffset.equals(statement.getPredicate()))
				{
					statementList.add(statement);
				}
			}
		}
		else if (event.getType() == ECTEvent.Type.UPDATED)
		{
			final CompInfo compInfo = event.getObject();
			if (compInfo instanceof ComponentProperty)
			{
				final Resource resource = model.getResource(guid);
				try
				{
					final ComponentProperty property = (ComponentProperty) compInfo;
					final String propertyValue = property.getPropertyValueAsString();
					if (propertyValue != null)
					{
						statementList.add(resource, RDF.value, propertyValue);
					}
					else
					{
						final Statement statement = resource.getProperty(RDF.value);
						if (statement != null)
						{
							final StatementListCommand list = statementQueue.getEventList(ECTEvent.Type.REMOVED);
							list.add(resource.getProperty(RDF.value));
						}
					}
				}
				catch (final ConnectionPointTypeException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}