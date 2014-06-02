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
package ect.equip.physconf.ui.tabs.sections;

import com.hp.hpl.jena.rdf.model.Resource;

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.PhysConfPlugin;
import ect.equip.physconf.ui.filters.TypeMapper;

import org.eclipse.jface.viewers.IFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

public class ImageSection extends ResourcePropertySection
{
	public static class ImagePropertyFilter implements IFilter
	{
		public boolean select(final Object toTest)
		{
			final Resource physicalThing = TypeMapper.getResourceOfType(toTest, Schema.PhysicalThing);
			if (physicalThing != null) { return physicalThing.hasProperty(Schema.image); }
			return false;
		}
	}

	protected Label imageLabel;

	/**
	 * @param property
	 */
	public ImageSection()
	{
		super(Schema.image);
	}

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage)
	{
		super.createControls(parent, aTabbedPropertySheetPage);
		final Composite composite = getWidgetFactory().createFlatFormComposite(parent);
		FormData data;

		imageLabel = getWidgetFactory().createLabel(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		imageLabel.setLayoutData(data);

		final CLabel labelLabel = getWidgetFactory().createCLabel(composite, "Image:"); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(imageLabel, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(imageLabel, 0, SWT.CENTER);
		labelLabel.setLayoutData(data);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * ect.equip.physconf.ui.tabs.sections.ResourcePropertySection#setValue(
	 * java.lang.Object)
	 */
	@Override
	protected void setValue(final Object value)
	{
		if (imageLabel.isDisposed()) { return; }
		if (value == null)
		{
			imageLabel.setImage(null);
			reflow();
		}
		else
		{
			if (value instanceof Resource)
			{
				final Resource imageRes = (Resource) value;
				final Image newImage = PhysConfPlugin.getDefault().getImage(imageRes);
				if (newImage != null)
				{
					imageLabel.setImage(newImage);
					reflow();
				}
			}
		}
	}

	void reflow()
	{
		Composite c = imageLabel.getParent();
		while (c != null)
		{
			c.setRedraw(false);
			c = c.getParent();
			if (c instanceof ScrolledForm)
			{
				break;
			}
		}
		c = imageLabel.getParent();
		while (c != null)
		{
			c.layout(true);
			c = c.getParent();
			if (c instanceof ScrolledForm)
			{
				((ScrolledForm) c).reflow(true);
				break;
			}
		}
		c = imageLabel.getParent();
		while (c != null)
		{
			c.setRedraw(true);
			c = c.getParent();
			if (c instanceof ScrolledForm)
			{
				break;
			}
		}
	}
}