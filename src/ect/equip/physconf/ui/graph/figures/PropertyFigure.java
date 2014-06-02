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
import com.hp.hpl.jena.vocabulary.RDF;

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.ui.ModelLabelProvider;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PositionConstants;

/**
 * Figure used to represent a table in the schema
 * 
 * @author Phil Zoio
 */
public class PropertyFigure extends ResourceFigure
{
	private final Label name;
	private final Label value;

	public PropertyFigure(final Resource resource)
	{
		super(resource);

		final GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayoutManager(layout);
		setForegroundColor(ColorConstants.black);
		setOpaque(true);

		name = new Label(ModelLabelProvider.text(resource), ModelLabelProvider.image(resource));
		name.setOpaque(true);
		name.setBorder(new MarginBorder(1, 3, 1, 5));

		final GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL);
		value = new Label();
		value.setOpaque(true);
		value.setBorder(new MarginBorder(1, 3, 1, 5));
		value.setTextAlignment(PositionConstants.RIGHT);
		layout.setConstraint(value, data);

		add(name);
		add(value);
	}

	public void refresh()
	{
		name.setText(ModelLabelProvider.text(resource));
		if (resource.hasProperty(RDF.value) && (!resource.hasProperty(Schema.isVisible, false)))
		{
			value.setText(resource.getProperty(RDF.value).getString());
			value.setVisible(true);
		}
		else
		{
			value.setVisible(false);
		}
	}
}