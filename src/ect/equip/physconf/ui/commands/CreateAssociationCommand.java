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
package ect.equip.physconf.ui.commands;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DC_11;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.PhysConfPlugin;
import ect.equip.physconf.ui.ModelLabelProvider;
import ect.equip.physconf.ui.wizards.SelectDestinationWizard;

import org.eclipse.jface.wizard.WizardDialog;

public class CreateAssociationCommand extends CreateConnectionCommand
{
	private final static Resource ARType = PhysConfPlugin.getDefault().getModel().getResource(
			"http://www.ncess.ac.uk/nodes/digitalrecord/digitalrecord.owl#ARMarker");

	private static Resource image;
	private static Resource baseTransform;
	private static Resource satelliteTransform;
	private static Resource baseVisible;
	private static Resource satelliteVisible;
	private static Resource markerDetails;
	private static Resource markerInteractionType;

	private boolean reverse = false;
	private Resource thingDetails = null;
	private final List<Statement> statements = new ArrayList<Statement>();

	@Override
	public void execute()
	{
		final Model model = PhysConfPlugin.getDefault().getModel();
		if (!source.hasProperty(RDF.type, ARType) || !destination.hasProperty(RDF.type, ARType))
		{
			Resource source = this.source;
			Resource destination = this.destination;
			if (reverse)
			{
				source = this.destination;
				destination = this.source;
			}
			final Resource association = model.createResource();
			final String name = ModelLabelProvider.text(source) + " -> " + ModelLabelProvider.text(destination);
			statements.add(model.createStatement(association, RDF.type, Schema.Association));
			statements.add(model.createStatement(association, RDFS.label, name));
			statements.add(model.createStatement(association, Schema.source, source));
			statements.add(model.createStatement(association, Schema.destination, destination));
			final StmtIterator iterator = thingDetails.listProperties(Schema.createLink);
			boolean launchWizard = false;
			while (iterator.hasNext())
			{
				final Statement statement = iterator.nextStatement();
				if (statement.getResource().hasProperty(Schema.selectDestination, "true"))
				{
					final Resource link = model.createResource();
					statements.add(model.createStatement(association, Schema.createLink, link));
					final StmtIterator iterator2 = statement.getResource().listProperties();
					while (iterator2.hasNext())
					{
						final Statement statement2 = iterator2.nextStatement();
						statements.add(model.createStatement(link, statement2.getPredicate(), statement2.getObject()));
					}
					launchWizard = true;
				}
				else
				{
					statements.add(model.createStatement(association, Schema.createLink, statement.getObject()));
				}
			}
			model.add(statements);
			if (launchWizard)
			{
				final SelectDestinationWizard wizard = new SelectDestinationWizard(association);
				final WizardDialog dialog = new WizardDialog(null, wizard);
				// Open the wizard dialog
				dialog.open();
			}
		}
		else
		{
			setupStatements();

			final Resource markerInteraction = model.createResource();
			final Resource association1 = model.createResource();
			final Resource association2 = model.createResource();

			statements.add(model.createStatement(markerInteraction, RDF.type, markerInteractionType));
			statements.add(model.createStatement(markerInteraction, Schema.plugsInto, markerDetails));
			statements.add(model.createStatement(markerInteraction, RDFS.label, "Marker Interation"));
			statements.add(model.createStatement(markerInteraction, Schema.image, image));

			int sourceX = 45;
			int sourceY = 45;
			int destX = 45;
			int destY = 45;
			try
			{
				sourceX += source.getProperty(Schema.xOffset).getInt();
			}
			catch (final Exception e)
			{
			}
			try
			{
				sourceY += source.getProperty(Schema.yOffset).getInt();
			}
			catch (final Exception e)
			{
			}
			try
			{
				destX += destination.getProperty(Schema.xOffset).getInt();
			}
			catch (final Exception e)
			{
			}
			try
			{
				destY += destination.getProperty(Schema.yOffset).getInt();
			}
			catch (final Exception e)
			{
			}
			final int midX = (sourceX + destX) / 2;
			final int midY = (sourceY + destY) / 2;
			statements.add(model.createStatement(markerInteraction, Schema.xOffset, midX));
			statements.add(model.createStatement(markerInteraction, Schema.yOffset, midY));

			final Resource component = model.createResource();
			statements.add(model.createStatement(component, RDF.type, Schema.ComponentDetails));
			statements.add(model.createStatement(component, DC_11.title, "RelativeDistance"));
			statements.add(model.createStatement(component, Schema.proxyComponent, "true"));
			statements.add(model.createStatement(markerInteraction, Schema.createComponent, component));

			String name;
			name = ModelLabelProvider.text(markerInteraction) + " -> " + ModelLabelProvider.text(source);
			statements.add(model.createStatement(association1, RDF.type, Schema.Association));
			statements.add(model.createStatement(association1, RDFS.label, name));
			statements.add(model.createStatement(association1, Schema.source, markerInteraction));
			statements.add(model.createStatement(association1, Schema.destination, source));
			statements.add(model.createStatement(association1, Schema.createLink, baseTransform));
			statements.add(model.createStatement(association1, Schema.createLink, baseVisible));

			name = ModelLabelProvider.text(markerInteraction) + " -> " + ModelLabelProvider.text(destination);
			statements.add(model.createStatement(association2, RDF.type, Schema.Association));
			statements.add(model.createStatement(association2, RDFS.label, name));
			statements.add(model.createStatement(association2, Schema.source, markerInteraction));
			statements.add(model.createStatement(association2, Schema.destination, destination));
			statements.add(model.createStatement(association2, Schema.createLink, satelliteTransform));
			statements.add(model.createStatement(association2, Schema.createLink, satelliteVisible));

			model.add(statements);
		}
	}

	public void setupStatements()
	{
		if (image == null)
		{
			final List<Statement> statements = new ArrayList<Statement>();
			final Model model = PhysConfPlugin.getDefault().getModel();

			image = model.createResource();
			statements.add(model.createStatement(image, RDF.type, Schema.MediaFileLocation));
			statements.add(model.createStatement(image, Schema.filePath, "images/blob.gif"));

			baseTransform = model.createResource();
			statements.add(model.createStatement(baseTransform, RDF.type, Schema.Link));
			statements.add(model.createStatement(baseTransform, Schema.source, "glyphTransform"));
			statements.add(model.createStatement(baseTransform, Schema.destination, "baseTransformationMatrix"));
			statements.add(model.createStatement(baseTransform, Schema.reverseLink, "true"));

			baseVisible = model.createResource();
			statements.add(model.createStatement(baseVisible, RDF.type, Schema.Link));
			statements.add(model.createStatement(baseVisible, Schema.source, "glyphVisible"));
			statements.add(model.createStatement(baseVisible, Schema.destination, "baseVisible"));
			statements.add(model.createStatement(baseVisible, Schema.reverseLink, "true"));

			satelliteTransform = model.createResource();
			statements.add(model.createStatement(satelliteTransform, RDF.type, Schema.Link));
			statements.add(model.createStatement(satelliteTransform, Schema.source, "glyphTransform"));
			statements.add(model.createStatement(satelliteTransform, Schema.destination,
					"satelliteTransformationMatrix"));
			statements.add(model.createStatement(satelliteTransform, Schema.reverseLink, "true"));

			satelliteVisible = model.createResource();
			statements.add(model.createStatement(satelliteVisible, RDF.type, Schema.Link));
			statements.add(model.createStatement(satelliteVisible, Schema.source, "glyphVisible"));
			statements.add(model.createStatement(satelliteVisible, Schema.destination, "satelliteVisible"));
			statements.add(model.createStatement(satelliteVisible, Schema.reverseLink, "true"));

			markerDetails = model.createResource();
			statements.add(model.createStatement(markerDetails, RDF.type, Schema.ThingDetails));
			statements.add(model.createStatement(markerDetails, Schema.thingType, ARType));

			markerInteractionType = model.createResource("http://www.ncess.ac.uk/nodes/digitalrecord/digitalrecord.owl#MarkerInteraction");
			statements.add(model.createStatement(markerInteractionType, RDF.type, OWL.Class));
			statements.add(model.createStatement(markerInteractionType, RDFS.subClassOf, Schema.PhysicalThing));

			model.add(statements);
		}
	}

	@Override
	public void undo()
	{
		while (true)
		{
			try
			{
				PhysConfPlugin.getDefault().getModel().remove(statements);
				return;
			}
			catch (final ConcurrentModificationException e)
			{

			}
		}
	}

	@Override
	protected boolean connectionExists(final Resource source, final Resource destination)
	{
		final Model model = PhysConfPlugin.getDefault().getModel();
		if (reverse)
		{
			final ResIterator iterator = model.listSubjectsWithProperty(Schema.source, destination);
			while (iterator.hasNext())
			{
				final Resource resource = iterator.nextResource();
				if (resource.hasProperty(Schema.destination, source)
						&& resource.hasProperty(RDF.type, Schema.Association)) { return true; }
			}
		}
		else
		{
			final ResIterator iterator = model.listSubjectsWithProperty(Schema.source, source);
			while (iterator.hasNext())
			{
				final Resource resource = iterator.nextResource();
				if (resource.hasProperty(Schema.destination, destination)
						&& resource.hasProperty(RDF.type, Schema.Association)) { return true; }
			}
		}
		return false;
	}

	@Override
	protected boolean validateDestination(final Resource destination)
	{
		reverse = false;
		if (destination == null) { return false; }
		if (source.hasProperty(RDF.type, ARType) && destination.hasProperty(RDF.type, ARType)) { return true; }
		StmtIterator iterator = source.listProperties(Schema.plugsInto);
		while (iterator.hasNext())
		{
			final Statement statement = iterator.nextStatement();
			if (statement.getObject().isResource())
			{
				final Resource thingDetails = statement.getResource();
				final StmtIterator iterator2 = thingDetails.listProperties(Schema.thingType);
				while (iterator2.hasNext())
				{
					if (destination.hasProperty(RDF.type, iterator2.nextStatement().getObject()))
					{
						this.thingDetails = thingDetails;
						return true;
					}
				}
			}
		}
		iterator = destination.listProperties(Schema.plugsInto);
		while (iterator.hasNext())
		{
			final Statement statement = iterator.nextStatement();
			if (statement.getObject().isResource())
			{
				final Resource thingDetails = statement.getResource();
				final StmtIterator iterator2 = thingDetails.listProperties(Schema.thingType);
				while (iterator2.hasNext())
				{
					if (source.hasProperty(RDF.type, iterator2.nextStatement().getObject()))
					{
						reverse = true;
						this.thingDetails = thingDetails;
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	protected boolean validateSource(final Resource source)
	{
		return source != null;
	}
}