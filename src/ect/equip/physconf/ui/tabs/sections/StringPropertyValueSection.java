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
import com.hp.hpl.jena.vocabulary.RDF;

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.PhysConfPlugin;
import ect.equip.physconf.ui.filters.TypeMapper;

import org.eclipse.jface.viewers.IFilter;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class StringPropertyValueSection extends ResourcePropertyValueSection implements ModifyListener
{
	public static class StringPropertyFilter implements IFilter
	{
		public boolean select(final Object toTest)
		{
			final Object potentialProperty = TypeMapper.getModelObject(toTest);
			if (potentialProperty instanceof Resource)
			{
				final Resource resource = (Resource) potentialProperty;
				return resource.hasProperty(RDF.type, Schema.SoftwareComponentProperty)
						&& resource.hasProperty(Schema.readOnly, false)
						&& resource.hasProperty(Schema.propertyType, "java.lang.String");
			}
			return false;
		}
	}

	protected Text labelText;
	protected boolean modified = false;

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events
	 * .ModifyEvent)
	 */
	public void modifyText(final ModifyEvent e)
	{
		modified = true;
	}

	@Override
	protected Control createControl(final Composite parent)
	{
		labelText = getWidgetFactory().createText(parent, ""); //$NON-NLS-1$
		final FormData data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		labelText.setLayoutData(data);
		labelText.addModifyListener(this);
		labelText.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				setValue();
			}

		});
		labelText.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(final FocusEvent e)
			{
				setValue();
			}
		});
		return labelText;
	}

	protected void setValue()
	{
		if (labelText.isDisposed()) { return; }
		if (modified)
		{
			PhysConfPlugin.getDefault().getDataspaceMonitor().setPropertyValue(resource, labelText.getText());
			modified = false;
		}
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
		if (labelText.isDisposed()) { return; }
		if (value != null)
		{
			if (!labelText.getText().equals(value.toString()))
			{
				labelText.setText(value.toString());
			}
		}
		else
		{
			labelText.setText("");
		}
		modified = false;
	}
}