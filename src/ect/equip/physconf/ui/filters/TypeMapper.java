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
package ect.equip.physconf.ui.filters;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import digitalrecord.ontology.wrapper.Thing;
import digitalrecord.wrapped.Schema;

import org.eclipse.gef.EditPart;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.views.properties.tabbed.AbstractTypeMapper;

public class TypeMapper extends AbstractTypeMapper
{
	public static Object getModelObject(final Object object)
	{
		Object result = object;
		if (result instanceof IStructuredSelection)
		{
			result = ((IStructuredSelection) result).getFirstElement();
		}

		if (result instanceof EditPart)
		{
			result = ((EditPart) result).getModel();
		}

		if (result instanceof Thing)
		{
			result = ((Thing) result).getResource();
		}

		return result;
	}

	public static Resource getResourceOfType(final Object wrappedResource, final Resource type)
	{
		final Object potentialResource = getModelObject(wrappedResource);
		if (potentialResource instanceof Resource)
		{
			Resource resource = (Resource) potentialResource;
			if (resource.hasProperty(RDF.type, type)) { return resource; }
			final StmtIterator iterator = resource.listProperties(Schema.hasProxy);
			while (iterator.hasNext())
			{
				resource = iterator.nextStatement().getResource();
				if (resource.hasProperty(RDF.type, type)) { return resource; }
			}
		}
		return null;
	}

	public static boolean isResourceOfType(final Object wrappedResource, final Resource type)
	{
		final Object potentialResource = getModelObject(wrappedResource);
		if (potentialResource instanceof Resource)
		{
			final Resource resource = (Resource) potentialResource;
			if (resource.hasProperty(RDF.type, type)) { return true; }
		}
		return false;
	}

	@Override
	public Class<?> mapType(final Object object)
	{
		return getModelObject(object).getClass();
	}
}