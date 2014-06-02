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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

import ect.equip.physconf.ui.ModelLabelProvider;

import org.eclipse.gef.requests.CreateRequest;

// TODO Handle more than one resource?
public class CreateResourceRequest extends CreateRequest
{
	private final Model model;
	private final Resource resource;
	private Resource newResource;

	public CreateResourceRequest(final Model model, final Resource resource)
	{
		this.model = model;
		this.resource = resource;
	}

	@Override
	public Object getNewObject()
	{
		return newResource;
	}

	public Resource getNewResource()
	{
		if (newResource == null)
		{
			if (resource.isAnon())
			{
				newResource = model.createResource();
			}
			else
			{
				newResource = model.createResource(resource.getURI());
			}
		}
		return newResource;
	}

	public Resource getResource()
	{
		return resource;
	}

	public void setNewResource(final Resource newResource)
	{
		this.newResource = newResource;
	}

	@Override
	public String toString()
	{
		return "Create " + ModelLabelProvider.text(resource);
	}
}