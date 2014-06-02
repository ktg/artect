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

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import ect.equip.physconf.ui.ModelLabelProvider;
import ect.equip.physconf.ui.ModelTreeContentProvider;
import ect.equip.physconf.ui.filters.TypeMapper;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

public class TypeSection extends AbstractPropertySection
{
	protected TreeViewer typeList;

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage)
	{
		super.createControls(parent, aTabbedPropertySheetPage);
		final Composite composite = getWidgetFactory().createFlatFormComposite(parent);

		typeList = new TreeViewer(new Tree(composite, SWT.NONE));
		typeList.setColumnProperties(new String[] { "Type Hierarchy" });
		typeList.setLabelProvider(new ModelLabelProvider());
		typeList.addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(final SelectionChangedEvent event)
			{
				if (!event.getSelection().isEmpty())
				{
					typeList.setSelection(null);
				}
			}
		});
		typeList.getTree().addTreeListener(new TreeListener()
		{
			public void treeCollapsed(final TreeEvent e)
			{
				final int items = getItemHeight(typeList.getTree().getItems(), (TreeItem) e.item, false);
				final FormData data = new FormData();
				data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
				data.right = new FormAttachment(100, 0);
				data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
				data.height = typeList.getTree().getItemHeight() * items;
				typeList.getTree().setLayoutData(data);
				reflow();
			}

			public void treeExpanded(final TreeEvent e)
			{
				final int items = getItemHeight(typeList.getTree().getItems(), (TreeItem) e.item, true);
				final FormData data = new FormData();
				data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
				data.right = new FormAttachment(100, 0);
				data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
				data.height = typeList.getTree().getItemHeight() * items;
				typeList.getTree().setLayoutData(data);
				reflow();
			}

			private int getItemHeight(final TreeItem[] nodes, final TreeItem item, final boolean expand)
			{
				int height = nodes.length;
				for (final TreeItem element : nodes)
				{
					if ((expand && (item == element || element.getExpanded()))
							|| (!expand && item != element && element.getExpanded()))
					{
						height += getItemHeight(element.getItems(), item, expand);
					}
				}
				return height;
			}
		});
		typeList.setContentProvider(new ModelTreeContentProvider()
		{
			private Resource resource;

			public Object[] getChildren(final Object parentElement)
			{
				if (parentElement instanceof Resource)
				{
					final Resource resource = (Resource) parentElement;
					final Model model = resource.getModel();
					if (model instanceof InfModel) { return new Object[0]; }
					final ArrayList<RDFNode> list = new ArrayList<RDFNode>();
					final NodeIterator iterator = model.listObjectsOfProperty(resource, RDFS.subClassOf);
					while (iterator.hasNext())
					{
						list.add(iterator.nextNode());
					}
					return list.toArray();
				}
				return new Object[0];
			}

			public Object[] getElements(final Object inputElement)
			{
				Model model = resource.getModel();
				if (model instanceof InfModel)
				{
					model = ((InfModel) resource.getModel()).getRawModel();
				}
				final ArrayList<RDFNode> list = new ArrayList<RDFNode>();
				final NodeIterator iterator = model.listObjectsOfProperty(resource, RDF.type);
				while (iterator.hasNext())
				{
					list.add(iterator.nextNode());
				}
				return list.toArray();
			}

			@Override
			public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
			{
				super.inputChanged(viewer, oldInput, newInput);
				if (newInput instanceof Resource)
				{
					resource = (Resource) newInput;
				}
			}
		});
		FormData data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		data.height = typeList.getTree().getItemHeight();
		typeList.getTree().setLayoutData(data);

		final CLabel labelLabel = getWidgetFactory().createCLabel(composite, "Type:"); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(typeList.getTree(), -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(typeList.getTree(), 0, SWT.TOP);
		labelLabel.setLayoutData(data);
	}

	@Override
	public void refresh()
	{
		typeList.refresh();
	}

	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection)
	{
		super.setInput(part, selection);
		typeList.setInput(TypeMapper.getModelObject(selection));
	}

	@Override
	public boolean shouldUseExtraSpace()
	{
		return false;
	}

	void reflow()
	{
		Composite c = typeList.getTree();
		while (c != null)
		{
			c.setRedraw(false);
			c = c.getParent();
			if (c instanceof ScrolledForm)
			{
				break;
			}
		}
		c = typeList.getTree();
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
		c = typeList.getTree();
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