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

import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.Statement;

import ect.equip.physconf.ModelUpdateListener;
import ect.equip.physconf.PhysConfPlugin;

import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class ComponentSection extends ResourceSection
{
	private ModelChangedListener listener;
	private Label labelImage;

	@Override
	public void aboutToBeHidden()
	{
		PhysConfPlugin.getDefault().getModel().unregister(listener);
	}

	@Override
	public void aboutToBeShown()
	{
		if (listener == null)
		{
			listener = createListener();
		}
		PhysConfPlugin.getDefault().getModel().register(listener);
	}

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage)
	{
		super.createControls(parent, aTabbedPropertySheetPage);
		final Composite composite = getWidgetFactory().createFlatFormComposite(parent);
		FormData data;

		labelImage = getWidgetFactory().createLabel(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		data.height = 50;
		data.width = 50;
		labelImage.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(labelImage, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		// data.height = typeList.getTree().getItemHeight();
		// typeList.getTree().setLayoutData(data);

		final CLabel labelLabel = getWidgetFactory().createCLabel(composite, "Type:"); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		// data.right = new FormAttachment(typeList.getTree(),
		// -ITabbedPropertyConstants.HSPACE);
		// data.top = new FormAttachment(typeList.getTree(), 0, SWT.TOP);
		labelLabel.setLayoutData(data);
	}

	@Override
	public void dispose()
	{
		PhysConfPlugin.getDefault().getModel().unregister(listener);
		super.dispose();
	}

	@Override
	public void refresh()
	{

	}

	@Override
	protected ModelChangedListener createListener()
	{
		return new ModelUpdateListener()
		{
			@Override
			public boolean shouldUpdate(final Statement s)
			{
				if (s.getSubject().equals(resource)) { return true; }
				return false;
			}

			@Override
			public void update()
			{
				refresh();
			}
		};
	}

	protected void setValue(final Object value)
	{

	}
}
