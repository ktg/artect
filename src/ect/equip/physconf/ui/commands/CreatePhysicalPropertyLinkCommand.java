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

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.PhysConfPlugin;
import equip.data.GUID;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class CreatePhysicalPropertyLinkCommand extends CreateConnectionCommand
{
	private GUID linkGUID;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	@Override
	public void execute()
	{
		linkGUID = PhysConfPlugin.getDefault().getDataspaceMonitor().addComponentPropertyLink(getPropertyProxy(source),
				getPropertyProxy(destination));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	@Override
	public void undo()
	{
		PhysConfPlugin.getDefault().getDataspaceMonitor().remove(linkGUID);
	}

	/**
	 * Set the target endpoint for the connection.
	 * 
	 * @param target
	 *            that target endpoint (a non-null Shape instance)
	 * @throws IllegalArgumentException
	 *             if target is null
	 */
	@Override
	public boolean validateDestination(final Resource target)
	{
		final Resource property = getPropertyProxy(target);
		return target != null && property != null && !property.hasProperty(Schema.readOnly, true);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * ect.equip.physconf.ui.graph.commands.CreateConnectionCommand#setSource
	 * (com.hp.hpl.jena.rdf.model.Resource)
	 */
	@Override
	public boolean validateSource(final Resource source)
	{
		return source != null && getPropertyProxy(source) != null;
	}

	private Resource getPropertyProxy(final Resource physicalThing)
	{
		final StmtIterator iterator = physicalThing.listProperties(Schema.hasProxy);
		final boolean found = false;
		while (iterator.hasNext() && !found)
		{
			final Resource resource = iterator.nextStatement().getResource();
			if (resource.hasProperty(RDF.type, Schema.SoftwareComponentProperty)) { return resource; }
		}
		return null;
	}

	@Override
	protected boolean connectionExists(final Resource source, final Resource destination)
	{
		// TODO Check for existing connection
		return false;
	}
}