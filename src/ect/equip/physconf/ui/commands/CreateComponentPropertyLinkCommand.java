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
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.PhysConfPlugin;
import equip.data.GUID;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class CreateComponentPropertyLinkCommand extends CreateConnectionCommand
{
	private GUID linkGUID;

	public CreateComponentPropertyLinkCommand()
	{
		setLabel("connection creation");
	}

	@Override
	public void execute()
	{
		linkGUID = PhysConfPlugin.getDefault().getDataspaceMonitor().addComponentPropertyLink(source, destination);
	}

	@Override
	public void undo()
	{
		PhysConfPlugin.getDefault().getDataspaceMonitor().remove(linkGUID);
	}

	@Override
	protected boolean connectionExists(final Resource source, final Resource destination)
	{
		final Model model = PhysConfPlugin.getDefault().getModel();
		final ResIterator iterator = model.listSubjectsWithProperty(Schema.source, source);
		while (iterator.hasNext())
		{
			final Resource resource = iterator.nextResource();
			if (resource.hasProperty(Schema.destination, destination)
					&& resource.hasProperty(RDF.type, Schema.SoftwareComponentPropertyLink)) { return true; }
		}
		return false;
	}

	@Override
	protected boolean validateDestination(final Resource destination)
	{
		if (destination == null) { return false; }
		if (destination.hasProperty(Schema.readOnly, true)) { return false; }
		return true;
	}

	@Override
	protected boolean validateSource(final Resource source)
	{
		if (source == null) { return false; }
		return true;
	}
}