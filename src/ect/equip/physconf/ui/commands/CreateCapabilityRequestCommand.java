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

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.ECTEvent;

import org.eclipse.draw2d.geometry.Point;

public class CreateCapabilityRequestCommand extends StatementListCommand
{
	private final CreateResourceRequest request;
	private final Point constraint;

	public CreateCapabilityRequestCommand(final Model model, final CreateResourceRequest request, final Point constraint)
	{
		super(model, ECTEvent.Type.ADDED);
		this.request = request;
		this.constraint = constraint;
		setLabel(request.toString());
	}

	@Override
	public void execute()
	{
		// final Resource resource = request.getResource();
		final Resource newResource = request.getNewResource();
		// final StmtIterator stmtIterator = listStatements(resource);
		// processStatements(newResource, stmtIterator);
		final Point point = request.getLocation();
		if (point != null && !newResource.hasProperty(Schema.xOffset))
		{
			if (constraint != null)
			{
				add(newResource, Schema.xOffset, constraint.x);
				add(newResource, Schema.yOffset, constraint.y);
			}
			else
			{
				add(newResource, Schema.xOffset, point.x);
				add(newResource, Schema.yOffset, point.y);
			}
		}
		super.execute();
	}
}