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
package ect.equip.physconf.ui;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.vocabulary.DC_11;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.PhysConfPlugin;
import ect.equip.physconf.ui.filters.TypeMapper;
import ect.equip.physconf.ui.views.Classification;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class ModelLabelProvider extends LabelProvider
{
	private static final ModelLabelProvider LABEL_PROVIDER = new ModelLabelProvider();

	public static Image image(final Object obj)
	{
		return LABEL_PROVIDER.getImage(obj);
	}

	public static String text(final Object obj)
	{
		return LABEL_PROVIDER.getText(obj);
	}

	@Override
	public Image getImage(final Object obj)
	{
		final Object object = TypeMapper.getModelObject(obj);

		if (object instanceof Rule)
		{
			return PhysConfPlugin.getImage("rule");
		}
		else if (object instanceof Literal)
		{
			return null;
		}
		else if (object instanceof OntClass)
		{
			return PhysConfPlugin.getImage("class");
		}
		else if (object instanceof Resource)
		{
			final Resource resource = (Resource) object;
			final StmtIterator iterator = resource.listProperties(RDF.type);
			while (iterator.hasNext())
			{
				final Resource type = iterator.nextStatement().getResource();
				if (type.hasProperty(Schema.image))
				{
					final Resource imageResource = type.getProperty(Schema.image).getResource();
					final Image image = PhysConfPlugin.getDefault().getImage(imageResource);
					if (image != null) { return image; }
				}
			}

			if (resource.hasProperty(RDF.type, Schema.SoftwareComponentProperty))
			{
				if (resource.hasProperty(Schema.readOnly, true)) { return PhysConfPlugin.getImage("read_only"); }
				return PhysConfPlugin.getImage("property");
			}
			else if (resource.hasProperty(RDF.type, OWL.Restriction))
			{
				return PhysConfPlugin.getImage("restriction");
			}
			else if (resource.hasProperty(RDF.type, OWL.Class))
			{
				return PhysConfPlugin.getImage("class");
			}
			else if (resource.hasProperty(RDF.type, RDFS.Class))
			{
				return PhysConfPlugin.getImage("class");
			}
			else if (resource.hasProperty(RDF.type, RDF.Property))
			{
				return PhysConfPlugin.getImage("property");
			}
			else if (resource.hasProperty(RDF.type, Schema.SoftwareContainer))
			{
				return PhysConfPlugin.getImage("dataspace");
			}
			else if (resource.hasProperty(RDF.type, Schema.Link))
			{
				return PhysConfPlugin.getImage("connection");
			}
			else
			{
				return PhysConfPlugin.getImage("thing");
			}
		}
		else if (object instanceof Classification) { return PlatformUI.getWorkbench().getSharedImages().getImage(
				ISharedImages.IMG_OBJ_FOLDER); }
		// else if (object instanceof String)
		// {
		// return PlatformUI.getWorkbench().getSharedImages().getImage(
		// ISharedImages.IMG_OBJ_FOLDER);
		// }
		return null; // PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
	}

	@Override
	public String getText(final Object obj)
	{
		final Object object = TypeMapper.getModelObject(obj);

		if (object instanceof Rule)
		{
			return ((Rule) object).getName();
		}
		else if (object instanceof Resource)
		{
			final Resource resource = (Resource) object;
			Statement statement = resource.getProperty(RDFS.label);
			if (statement != null)
			{
				if (statement.getObject().isLiteral()) { return statement.getObject().asNode().getLiteralValue().toString(); }
				return statement.getObject().toString();
			}
			statement = resource.getProperty(DC_11.title);
			if (statement != null)
			{
				if (statement.getObject().isLiteral()) { return statement.getObject().asNode().getLiteralValue().toString(); }
				return statement.getObject().toString();
			}
			if (resource.hasProperty(RDF.type, OWL.Restriction)) { return "Restriction on property '"
					+ getText(resource.getProperty(OWL.onProperty).getObject()) + "'"; }
			if (resource.getURI() != null)
			{
				final int index = resource.getURI().indexOf('#');
				if (index > -1) { return resource.getURI().substring(index + 1); }
			}
		}
		return object.toString();
	}
}