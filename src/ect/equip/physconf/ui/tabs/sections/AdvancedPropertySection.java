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

import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import ect.equip.physconf.ModelUpdateListener;
import ect.equip.physconf.PhysConfPlugin;
import ect.equip.physconf.ui.ModelLabelProvider;
import ect.equip.physconf.ui.filters.TypeMapper;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySource2;
import org.eclipse.ui.views.properties.IPropertySourceProvider;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class AdvancedPropertySection extends AbstractPropertySection
{
	/**
	 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
	 */
	private class ResourcePropertySource implements IPropertySource2
	{
		final protected Resource resource;

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
			return resource;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.eclipse.ui.views.properties.IPropertySource#getPropertyDescriptors
		 * ()
		 */
		public IPropertyDescriptor[] getPropertyDescriptors()
		{
			final StmtIterator iterator = resource.listProperties();
			if (iterator != null)
			{
				final ArrayList<PropertyDescriptor> list = new ArrayList<PropertyDescriptor>();
				while (iterator.hasNext())
				{
					final Statement statement = iterator.nextStatement();
					final PropertyDescriptor prop = new PropertyDescriptor(statement,
							ModelLabelProvider.text(statement.getPredicate()));
					prop.setLabelProvider(new ModelLabelProvider());
					list.add(prop);
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
			if (id instanceof Statement) { return ((Statement) id).getObject(); }
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
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.eclipse.ui.views.properties.IPropertySource#setPropertyValue(
		 * java.lang.Object, java.lang.Object)
		 */
		public void setPropertyValue(final Object id, final Object value)
		{
		}
	}

	private ModelChangedListener listener;

	protected final PropertyEntry entry = new PropertyEntry();
	protected final PropertySheetPage page = new PropertySheetPage();
	protected Resource resource;
	protected boolean showDerived = false;

	@Override
	public void aboutToBeHidden()
	{
		super.aboutToBeHidden();
		if (listener != null)
		{
			PhysConfPlugin.getDefault().getModel().unregister(listener);
		}
	}

	@Override
	public void aboutToBeShown()
	{
		super.aboutToBeShown();
		if (listener != null)
		{
			PhysConfPlugin.getDefault().getModel().register(listener);
		}
	}

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage tabbedPropertySheetPage)
	{
		super.createControls(parent, tabbedPropertySheetPage);
		final Composite composite = getWidgetFactory().createComposite(parent);
		final FormLayout layout = new FormLayout();
		layout.marginWidth = ITabbedPropertyConstants.HSPACE;
		layout.marginHeight = ITabbedPropertyConstants.VSPACE;
		// layout.spacing = ITabbedPropertyConstants.VMARGIN + 1;
		composite.setLayout(layout);

		final FormData data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, 0);
		// data.height = 120;
		data.bottom = new FormAttachment(100, 0);

		page.createControl(composite);
		page.setRootEntry(entry);
		page.setPropertySourceProvider(new IPropertySourceProvider()
		{
			public IPropertySource getPropertySource(final Object object)
			{
				final Object input = TypeMapper.getModelObject(object);
				if (input instanceof Resource) { return new ResourcePropertySource((Resource) input); }
				return null;
			}
		});

		final Tree tree = (Tree) page.getControl();
		tree.setLayoutData(data);
		tree.setSize(400, 10);
		tree.setLinesVisible(false);
		tree.setHeaderVisible(false);

		listener = createListener();
	}

	@Override
	public void dispose()
	{
		super.dispose();
		if (listener != null)
		{
			PhysConfPlugin.getDefault().getModel().unregister(listener);
		}
		if (page != null)
		{
			page.dispose();
		}
	}

	@Override
	public void refresh()
	{
		page.refresh();
	}

	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection)
	{
		super.setInput(part, selection);
		page.selectionChanged(part, selection);
		final Object object = TypeMapper.getModelObject(selection);
		if (object instanceof Resource)
		{
			resource = (Resource) object;
		}
	}

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ISection#shouldUseExtraSpace()
	 */
	@Override
	public boolean shouldUseExtraSpace()
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * ect.equip.physconf.ui.tabs.sections.PropertyListSection#createListener()
	 */
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
				entry.refresh();
			}
		};
	}
}