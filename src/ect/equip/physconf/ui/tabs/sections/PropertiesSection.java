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

import java.util.ArrayList;
import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.ModelUpdateListener;
import ect.equip.physconf.PhysConfPlugin;
import ect.equip.physconf.ui.ModelLabelProvider;
import ect.equip.physconf.ui.filters.TypeMapper;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySource2;
import org.eclipse.ui.views.properties.IPropertySourceProvider;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class PropertiesSection extends ResourcePropertySection
{
	/**
	 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
	 */
	private class ResourcePropertySource implements IPropertySource2
	{
		protected Resource resource;

		public ResourcePropertySource(final Resource resource)
		{
			this.resource = resource;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.eclipse.ui.views.properties.IPropertySource#getEditableValue()
		 */
		public Object getEditableValue()
		{
			final Resource resource = TypeMapper.getResourceOfType(this.resource, Schema.SoftwareComponentProperty);
			if (resource != null && !resource.hasProperty(Schema.readOnly, true))
			{
				final Statement statement = resource.getProperty(RDF.value);
				if (statement != null)
				{
					if (resource.hasProperty(Schema.propertyType, "boolean")
							|| resource.hasProperty(Schema.propertyType, "java.lang.Boolean")) { return new Boolean(
							statement.getString()); }
					return statement.getObject();
				}
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.eclipse.ui.views.properties.IPropertySource#getPropertyDescriptors
		 * ()
		 */
		public IPropertyDescriptor[] getPropertyDescriptors()
		{
			final Model model = PhysConfPlugin.getDefault().getModel();
			Iterator<?> iterator = null;
			if (resource.hasProperty(RDF.type, Schema.SoftwareComponent))
			{
				iterator = model.listSubjectsWithProperty(Schema.softwareComponent, resource);
			}
			else if (resource.hasProperty(RDF.type, Schema.PhysicalThing))
			{
				iterator = model.listObjectsOfProperty(resource, Schema.hasPart);
			}
			if (iterator != null)
			{
				final ArrayList<PropertyDescriptor> list = new ArrayList<PropertyDescriptor>();
				while (iterator.hasNext())
				{
					final Object obj = iterator.next();
					final Resource propertyResource = TypeMapper.getResourceOfType(obj,
							Schema.SoftwareComponentProperty);
					if (propertyResource != null)
					{
						PropertyDescriptor prop;
						final String displayName = ModelLabelProvider.text(obj);
						if (!propertyResource.hasProperty(Schema.readOnly, "true")
								&& (propertyResource.hasProperty(Schema.propertyType, "boolean") || propertyResource.hasProperty(
										Schema.propertyType, "java.lang.Boolean")))
						{
							prop = new PropertyDescriptor(obj, displayName)
							{
								@Override
								public CellEditor createPropertyEditor(final Composite parent)
								{
									final CellEditor editor = new ComboBoxCellEditor(parent, new String[] { "false",
											"true" }, SWT.READ_ONLY)
									{
										@Override
										protected void doSetValue(final Object value)
										{
											Boolean bool = Boolean.FALSE;
											if (value != null)
											{
												bool = new Boolean(value.toString());
											}

											if (Boolean.TRUE.equals(bool))
											{
												super.doSetValue(new Integer(1));
											}
											else
											{
												super.doSetValue(new Integer(0));
											}
										}
									};
									if (getValidator() != null)
									{
										editor.setValidator(getValidator());
									}
									return editor;
								}
							};
						}
						else if (!propertyResource.hasProperty(Schema.readOnly, "true"))
						{
							prop = new TextPropertyDescriptor(obj, displayName)
							{
								@Override
								public CellEditor createPropertyEditor(final Composite parent)
								{
									final CellEditor editor = new TextCellEditor(parent)
									{
										@Override
										protected void doSetValue(final Object value)
										{
											if (value == null)
											{
												super.doSetValue("");
											}
											else
											{
												super.doSetValue(value.toString());
											}
										}
									};
									if (getValidator() != null)
									{
										editor.setValidator(getValidator());
									}
									return editor;
								}
							};
						}
						else
						{
							prop = new PropertyDescriptor(obj, displayName);
						}
						prop.setLabelProvider(new ModelLabelProvider());
						list.add(prop);
					}
				}
				final IPropertyDescriptor[] descriptors = new IPropertyDescriptor[list.size()];
				list.toArray(descriptors);

				return descriptors;
			}
			return new IPropertyDescriptor[0];
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.eclipse.ui.views.properties.IPropertySource#getPropertyValue(
		 * java.lang.Object)
		 */
		public Object getPropertyValue(final Object id)
		{
			final Resource resource = TypeMapper.getResourceOfType(id, Schema.SoftwareComponentProperty);
			final Statement statement = resource.getProperty(RDF.value);
			if (statement != null) { return statement.getObject(); }
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.eclipse.ui.views.properties.IPropertySource2#isPropertyResettable
		 * (java.lang.Object)
		 */
		public boolean isPropertyResettable(final Object id)
		{
			return false;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.eclipse.ui.views.properties.IPropertySource#isPropertySet(java
		 * .lang.Object)
		 */
		public boolean isPropertySet(final Object id)
		{
			return false;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.eclipse.ui.views.properties.IPropertySource#resetPropertyValue
		 * (java.lang.Object)
		 */
		public void resetPropertyValue(final Object id)
		{
			// Reset property?
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.eclipse.ui.views.properties.IPropertySource#setPropertyValue(
		 * java.lang.Object, java.lang.Object)
		 */
		public void setPropertyValue(final Object id, final Object value)
		{
			final Resource resource = TypeMapper.getResourceOfType(id, Schema.SoftwareComponentProperty);
			PhysConfPlugin.getDefault().getDataspaceMonitor().setPropertyValue(resource, value.toString());
		}
	}

	final protected PropertySheetPage viewer = new PropertySheetPage();
	final protected PropertyEntry entry = new PropertyEntry();

	public PropertiesSection()
	{
		super(null);
	}

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage)
	{
		super.createControls(parent, aTabbedPropertySheetPage);
		final Composite composite = getWidgetFactory().createFlatFormComposite(parent);

		viewer.createControl(composite);
		viewer.setRootEntry(entry);
		viewer.setPropertySourceProvider(new IPropertySourceProvider()
		{
			public IPropertySource getPropertySource(final Object object)
			{
				final Object input = TypeMapper.getModelObject(object);
				if (input instanceof Resource) { return new ResourcePropertySource((Resource) input); }
				return null;
			}
		});
		FormData data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(100, 0);
		final Tree tree = (Tree) viewer.getControl();
		tree.setSize(400, 20);
		tree.setLayoutData(data);
		tree.setLinesVisible(false);
		tree.setHeaderVisible(false);

		final CLabel labelLabel = getWidgetFactory().createCLabel(composite, "Properties:"); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(tree, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(tree, 0, SWT.TOP);
		labelLabel.setLayoutData(data);
	}

	@Override
	public void refresh()
	{
		super.refresh();
		if (viewer.getControl().isDisposed()) { return; }
		entry.refresh();
		final Tree tree = (Tree) viewer.getControl();
		final TreeItem[] items = tree.getItems();
		for (final TreeItem element : items)
		{
			final Image image = ((PropertyEntry) element.getData()).getOtherImage();
			if (image != element.getImage(0))
			{
				element.setImage(0, image);
			}
		}
	}

	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection)
	{
		super.setInput(part, selection);
		viewer.selectionChanged(part, selection);
	}

	@Override
	protected ModelChangedListener createListener()
	{
		return new ModelUpdateListener()
		{
			@Override
			public boolean shouldUpdate(final Statement s)
			{
				if (RDF.value.equals(s.getPredicate())) { return true; }
				return false;
			}

			@Override
			public void update()
			{
				refresh();
			}
		};
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
		// Set value?
	}
}