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
package ect.equip.physconf.ui.views;

import java.util.ArrayList;
import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.PhysConfPlugin;
import ect.equip.physconf.ui.ModelLabelProvider;
import ect.equip.physconf.ui.ModelTreeContentProvider;
import equip.data.beans.DataspaceInactiveException;

import org.eclipse.gef.dnd.TemplateTransfer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchActionConstants;

public class CapabilitiesView extends ResourceView
{
	protected Action requestComponentAction;
	protected Action showListAction;
	protected Action showTreeAction;
	protected boolean classify = true;

	@Override
	public void createPartControl(final Composite parent)
	{
		super.createPartControl(parent);
		viewer.setLabelProvider(new ModelLabelProvider()
		{
			@Override
			public String getText(final Object obj)
			{
				if (obj instanceof Resource)
				{
					final Resource resource = (Resource) obj;
					if (resource.hasProperty(RDF.type, Schema.SoftwareContainer)) { return "Capabilities on "
							+ super.getText(obj); }
				}
				return super.getText(obj);
			}
		});
		viewer.addDragSupport(DND.DROP_COPY, new Transfer[] { TemplateTransfer.getInstance() }, new DragSourceAdapter()
		{
			@Override
			public void dragSetData(final DragSourceEvent event)
			{
				if (TemplateTransfer.getInstance().isSupportedType(event.dataType))
				{
					final Object selection = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
					event.data = selection;
				}
			}

			@Override
			public void dragStart(final DragSourceEvent event)
			{
				final Object selection = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				if (!(selection instanceof Resource))
				{
					event.doit = false;
				}
				else
				{
					TemplateTransfer.getInstance().setTemplate(selection);
					// final Resource resource = (Resource) selection;
					// if (resource.hasProperty(Schema.image))
					// {
					// event.image =
					// PhysConfPlugin.getDefault().getImage(
					// resource.getProperty(Schema.image).getResource());
					// }
				}
			}
		});
		viewer.addDoubleClickListener(new IDoubleClickListener()
		{
			public void doubleClick(final DoubleClickEvent event)
			{
				final IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (!selection.isEmpty())
				{
					final Object selected = selection.getFirstElement();
					if (selected instanceof Classification)
					{
						if (viewer.getExpandedState(selected))
						{
							viewer.collapseToLevel(selected, 1);
						}
						else
						{
							viewer.expandToLevel(selected, 1);
						}
					}
					else
					{
						// TODO Expand container nodes?
						requestComponentAction.run();
					}
				}
			}
		});
	}

	@Override
	protected void fillContextMenu(final IMenuManager manager)
	{
		manager.add(requestComponentAction);
		manager.add(new Separator());
		super.fillContextMenu(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	@Override
	protected void fillLocalPullDown(final IMenuManager manager)
	{
		manager.add(requestComponentAction);
		manager.add(new Separator());
		manager.add(showListAction);
		manager.add(showTreeAction);
	}

	@Override
	protected void fillLocalToolBar(final IToolBarManager manager)
	{
		manager.add(requestComponentAction);
		super.fillLocalToolBar(manager);
	}

	/*
	 * (non-Javadoc)
	 * @see ect.equip.physconf.views.ResourceView#getContentProvider()
	 */
	@Override
	protected ITreeContentProvider getContentProvider()
	{
		return new ModelTreeContentProvider()
		{
			protected Classification root;

			@Override
			public void addedStatement(final Statement s)
			{
				super.addedStatement(s);
				if (RDF.type.equals(s.getPredicate()) && Schema.SoftwareComponentCapability.equals(s.getObject()))
				{
					final Resource container = s.getSubject().getProperty(Schema.softwareContainer).getResource();
					final Classification classification = root.getChild(container.getURI());
					classification.add(s.getSubject());
					if (!classify)
					{
						getTreeViewer().add(container, s.getSubject());
					}
					else
					{
						getTreeViewer().refresh(container);
					}
					getTreeViewer().expandToLevel(2);
				}
				else if (Schema.classification.equals(s.getPredicate()))
				{
					if (!s.getSubject().hasProperty(RDF.type, Schema.SoftwareComponentCapability)) { return; }
					final Resource container = s.getSubject().getProperty(Schema.softwareContainer).getResource();
					final Classification classification = root.getChild(container.getURI());
					classification.add(s.getSubject());
				}
			}

			public Object[] getChildren(final Object parent)
			{
				if (parent instanceof Resource)
				{
					final Resource resource = (Resource) parent;
					if (resource.hasProperty(RDF.type, Schema.SoftwareContainer))
					{
						if (!classify)
						{
							final ResIterator iterator = model.listSubjectsWithProperty(Schema.softwareContainer,
									resource);
							final ArrayList<Resource> list = new ArrayList<Resource>();
							while (iterator.hasNext())
							{
								final Resource containee = iterator.nextResource();
								if (containee.hasProperty(RDF.type, Schema.SoftwareComponentCapability))
								{
									list.add(containee);
								}
							}
							return list.toArray();
						}
						return root.getChild(resource.getURI()).toArray();
					}
				}
				else if (parent instanceof Classification) { return ((Classification) parent).toArray(); }
				return new Object[0];
			}

			public Object[] getElements(final Object parent)
			{
				if (parent.equals(model))
				{
					final ResIterator iterator = model.listSubjectsWithProperty(RDF.type, Schema.SoftwareContainer);
					final ArrayList<Resource> list = new ArrayList<Resource>();
					while (iterator.hasNext())
					{
						final Resource resource = iterator.nextResource();
						list.add(resource);
					}
					return list.toArray();
				}
				return getChildren(parent);
			}

			@Override
			public void inputChanged(final Viewer v, final Object oldInput, final Object newInput)
			{
				super.inputChanged(v, oldInput, newInput);
				root = new Classification((TreeViewer) v, "root");
				if (newInput != null)
				{
					final ResIterator iterator = model.listSubjectsWithProperty(RDF.type,
							Schema.SoftwareComponentCapability);
					while (iterator.hasNext())
					{
						final Resource resource = iterator.nextResource();
						try
						{
							final Resource container = resource.getProperty(Schema.softwareContainer).getResource();
							final Classification classification = root.getChild(container.getURI());
							classification.add(resource);
						}
						catch (final Exception e)
						{
							continue;
						}
					}
				}
			}

			@Override
			public void removedStatement(final Statement s)
			{
				super.removedStatement(s);
				if (Schema.softwareContainer.equals(s.getPredicate()))
				{
					final Classification classification = root.getChild(s.getObject().toString());
					classification.remove(s.getSubject());
					if (!classify)
					{
						getTreeViewer().remove(s.getObject(), new Object[] { s.getSubject() });
					}
				}
				else if (RDF.type.equals(s.getPredicate()))
				{
					if (Schema.SoftwareContainer.equals(s.getObject()))
					{
						root.removeChild(s.getSubject().getURI());
						getTreeViewer().remove(s.getSubject());
					}
					else if (Schema.SoftwareComponentCapability.equals(s.getObject()))
					{
						root.remove(s.getSubject());
					}
				}
				else if (Schema.classification.equals(s.getPredicate()))
				{

				}
			}
		};
	}

	@Override
	protected void makeActions()
	{
		requestComponentAction = new Action("Request Component")
		{
			@Override
			public void run()
			{
				final ISelection selection = viewer.getSelection();
				if (selection.isEmpty()) { return; }
				final Object selected = ((IStructuredSelection) selection).getFirstElement();
				if (selected instanceof Resource)
				{
					final Resource resource = (Resource) selected;
					if (!resource.hasProperty(RDF.type, Schema.SoftwareComponentCapability)) { return; }
					try
					{
						PhysConfPlugin.getDefault().getDataspaceMonitor().addComponentRequest(resource);
					}
					catch (final DataspaceInactiveException e)
					{
						e.printStackTrace();
					}
				}
			}
		};
		requestComponentAction.setToolTipText("Request Component");
		requestComponentAction.setImageDescriptor(PhysConfPlugin.getDescriptor("component"));
		requestComponentAction.setEnabled(false);

		viewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(final SelectionChangedEvent event)
			{
				final Iterator<?> iter = ((IStructuredSelection) viewer.getSelection()).iterator();
				while (iter.hasNext())
				{
					final Object element = iter.next();
					if (!(element instanceof Resource))
					{
						requestComponentAction.setEnabled(false);
						return;
					}
					else if (!((Resource) element).hasProperty(RDF.type, Schema.SoftwareComponentCapability))
					{
						requestComponentAction.setEnabled(false);
						return;
					}
				}
				requestComponentAction.setEnabled(!viewer.getSelection().isEmpty());
			}
		});

		showListAction = new Action("Show as List", IAction.AS_RADIO_BUTTON)
		{
			@Override
			public void run()
			{
				classify = false;
				setChecked(true);
				showTreeAction.setChecked(false);
				viewer.refresh();
			}
		};
		showListAction.setChecked(!classify);
		showListAction.setToolTipText("Show as List");
		showListAction.setImageDescriptor(PhysConfPlugin.getDescriptor("listLayout"));

		showTreeAction = new Action("Show as Tree", IAction.AS_RADIO_BUTTON)
		{
			@Override
			public void run()
			{
				classify = true;
				setChecked(true);
				showListAction.setChecked(false);
				viewer.refresh();
			}
		};
		showTreeAction.setChecked(classify);
		showTreeAction.setToolTipText("Show as Tree");
		showTreeAction.setImageDescriptor(PhysConfPlugin.getDescriptor("treeLayout"));
	}
}