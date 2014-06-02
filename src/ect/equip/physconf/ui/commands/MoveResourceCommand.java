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

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.ui.ModelLabelProvider;
import ect.equip.physconf.ui.graph.parts.ResourcePart;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;

/**
 * Command to move the bounds of an existing table. Only used with
 * XYLayoutEditPolicy (manual layout)
 * 
 * @author Phil Zoio
 */
public class MoveResourceCommand extends Command
{
	private final ResourcePart resourcePart;
	private final Point oldLocation;
	private final Point newLocation;

	public MoveResourceCommand(final ResourcePart resourcePart, final Point oldLocation, final Point newLocation)
	{
		super();
		this.resourcePart = resourcePart;
		this.oldLocation = oldLocation;
		this.newLocation = newLocation;
		setLabel("Move " + ModelLabelProvider.text(resourcePart));
	}

	@Override
	public void execute()
	{
		updateValue(resourcePart.getResource(), Schema.xOffset, Integer.toString(newLocation.x));
		updateValue(resourcePart.getResource(), Schema.yOffset, Integer.toString(newLocation.y));
	}

	@Override
	public void undo()
	{
		updateValue(resourcePart.getResource(), Schema.xOffset, Integer.toString(oldLocation.x));
		updateValue(resourcePart.getResource(), Schema.yOffset, Integer.toString(oldLocation.y));
	}

	private void updateValue(final Resource resource, final Property property, final Object value)
	{
		final Statement statement = resource.getProperty(property);
		if (statement != null)
		{
			statement.changeObject(value);
		}
		else
		{
			resource.addProperty(property, value);
		}
	}
}