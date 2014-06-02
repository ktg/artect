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

import java.util.Collection;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.ui.filters.TypeMapper;

import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class ListPropertyValueSection extends ResourcePropertyValueSection
{
	public static class ListPropertyFilter implements IFilter
	{
		public boolean select(final Object toTest)
		{
			final Object potentialProperty = TypeMapper.getModelObject(toTest);
			if (potentialProperty instanceof Resource)
			{
				final Resource resource = (Resource) potentialProperty;
				if (resource.hasProperty(RDF.type, Schema.SoftwareComponentProperty))
				{
					final String type = resource.getProperty(Schema.propertyType).getString();
					try
					{
						final Class<?> clazz = Class.forName(type);
						if (clazz.isArray())
						{
							return true;
						}
						else if (Collection.class.isAssignableFrom(clazz)) { return true; }
					}
					catch (final ClassNotFoundException e)
					{
						if (type.startsWith("[")) { return true; }
					}
				}
			}
			return false;
		}
	}

	protected TableViewer viewer;

	/*
	 * (non-Javadoc)
	 * @seeect.equip.physconf.ui.tabs.sections.ResourcePropertyValueSection#
	 * createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createControl(final Composite parent)
	{
		final Table table = new Table(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.HIDE_SELECTION);
		getWidgetFactory().adapt(table, false, false);
		// table.setHeaderVisible(true);
		// TableLayout tableLayout = new TableLayout();
		// tableLayout.addColumnData(new ColumnWeightData(1, 50, true));
		// tableLayout.addColumnData(new ColumnWeightData(3, 50, true));
		// table.setLayout(tableLayout);

		final FormData data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		data.bottom = new FormAttachment(100, 0);
		data.height = 100;
		data.width = 100;
		table.setLayoutData(data);

		// new TableColumn(table, SWT.LEFT).setText("Property");
		// new TableColumn(table, SWT.NONE).setText("Value");

		viewer = new TableViewer(table);
		viewer.setColumnProperties(new String[] { RDFS.label.getURI(), RDF.value.getURI() });
		viewer.setSorter(new ViewerSorter());
		viewer.setContentProvider(new IStructuredContentProvider()
		{
			private Object input;

			public void dispose()
			{

			}

			@SuppressWarnings("unchecked")
			public Object[] getElements(final Object inputElement)
			{
				if (input == null) { return new Object[] {}; }
				if (input.getClass().isArray())
				{
					return (Object[]) input;
				}
				else if (input instanceof Collection) { return ((Collection) input).toArray(); }
				return new Object[] {};
			}

			public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
			{
				input = newInput;
			}
		});

		return table;
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
		viewer.setInput(value);
	}
}