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
package ect.equip.physconf.ui.graph.figures;

import com.hp.hpl.jena.rdf.model.Resource;

import ect.equip.physconf.ui.ModelLabelProvider;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ToolbarLayout;

/**
 * Figure used to represent a table in the schema
 * 
 * @author Phil Zoio
 */
public class ComponentFigure extends ResourceFigure
{
	private final ComponentPropertiesFigure propertiesFigure = new ComponentPropertiesFigure();
	private final Label name;

	public ComponentFigure(final Resource resource)
	{
		super(resource);
		name = new Label(ModelLabelProvider.text(resource), ModelLabelProvider.image(resource));
		final ToolbarLayout layout = new ToolbarLayout();
		layout.setVertical(true);
		layout.setStretchMinorAxis(true);
		layout.setMinorAlignment(FlowLayout.ALIGN_LEFTTOP);
		setLayoutManager(layout);
		setBorder(new LineBorder(ColorConstants.darkGray, 1));
		setForegroundColor(ColorConstants.black);
		setOpaque(true);

		name.setOpaque(true);
		name.setBorder(new MarginBorder(1, 3, 1, 5));
		name.setBackgroundColor(ColorConstants.titleBackground);
		name.setForegroundColor(ColorConstants.titleForeground);

		add(name);
		add(propertiesFigure);
	}

	public ComponentPropertiesFigure getPropertiesFigure()
	{
		return propertiesFigure;
	}

	public void setName(final String name)
	{
		this.name.setText(name);
	}
}