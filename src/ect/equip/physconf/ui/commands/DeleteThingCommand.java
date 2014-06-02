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

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.ECTEvent;
import ect.equip.physconf.PhysConfPlugin;

public class DeleteThingCommand extends StatementListCommand
{

	public DeleteThingCommand(final Model model, final ECTEvent.Type type)
	{
		super(model, type);
	}

	public void deleteResource(final Resource resource)
	{
		StmtIterator iterator = resource.listProperties(Schema.hasPart);
		while (iterator.hasNext())
		{
			deleteResource(iterator.nextStatement().getResource());
		}
		final Model model = PhysConfPlugin.getDefault().getModel();
		iterator = model.listStatements(null, Schema.source, resource);
		while (iterator.hasNext())
		{
			deleteResource(iterator.nextStatement().getSubject());
		}
		iterator = model.listStatements(null, Schema.destination, resource);
		while (iterator.hasNext())
		{
			deleteResource(iterator.nextStatement().getSubject());
		}
		Model otherModel = model;
		if (model instanceof InfModel)
		{
			otherModel = ((InfModel) model).getRawModel();
		}

		// TODO Delete any anon resources that won't have any statements to
		// them?
		// iterator = otherModel.listStatements(resource, null, (RDFNode)null);
		// while (iterator.hasNext())
		// {
		// deleteResource(iterator.nextStatement().getResource());
		// }
		add(otherModel.listStatements(resource, null, (RDFNode) null));
	}
}