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
import ect.equip.physconf.ui.ModelLabelProvider;
import ect.equip.physconf.ui.ModelTreeContentProvider;
import ect.equip.physconf.ui.commands.DeleteComponentCommand;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class ComponentView extends ResourceView
{
	Action deleteComponentAction;

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
					if (resource.hasProperty(RDF.type, Schema.SoftwareContainer)) { return "Components on "
							+ super.getText(obj); }
				}
				return super.getText(obj);
			}
		});
	}

	@Override
	protected void fillContextMenu(final IMenuManager manager)
	{
		manager.add(deleteComponentAction);
		manager.add(new Separator());
		super.fillContextMenu(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	@Override
	protected void fillLocalPullDown(final IMenuManager manager)
	{
		manager.add(deleteComponentAction);
	}

	@Override
	protected void fillLocalToolBar(final IToolBarManager manager)
	{
		manager.add(deleteComponentAction);
		manager.add(new Separator());
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
			/*
			 * (non-Javadoc)
			 * @see
			 * com.hp.hpl.jena.rdf.model.ModelChangedListener#addedStatement
			 * (com.hp.hpl.jena.rdf.model.Statement)
			 */
			@Override
			public void addedStatement(final Statement s)
			{
				if (RDF.type.equals(s.getPredicate()))
				{
					if (Schema.SoftwareContainer.equals(s.getObject()))
					{
						getTreeViewer().add(model, s.getSubject());
					}
					else if (Schema.SoftwareComponent.equals(s.getObject()))
					{
						final Resource container = s.getSubject().getProperty(Schema.softwareContainer).getResource();
						getTreeViewer().add(container, new Object[] { s.getSubject() });
						getTreeViewer().expandToLevel(2);
					}
					else
					{
						super.addedStatement(s);
					}
				}
				else if (Schema.softwareComponent.equals(s.getPredicate()))
				{
					getTreeViewer().add(s.getObject(), new Object[] { s.getSubject() });
					getTreeViewer().expandToLevel(2);
				}
				else
				{
					super.addedStatement(s);
				}
			}

			public Object[] getChildren(final Object parent)
			{
				if (parent instanceof Resource)
				{
					final Resource resource = (Resource) parent;
					final ArrayList<Resource> list = new ArrayList<Resource>();
					if (resource.hasProperty(RDF.type, Schema.SoftwareContainer))
					{
						final ResIterator iterator = model.listSubjectsWithProperty(Schema.softwareContainer, resource);
						while (iterator.hasNext())
						{
							final Resource component = iterator.nextResource();
							if (component.hasProperty(RDF.type, Schema.SoftwareComponent))
							{
								list.add(component);
							}
						}
					}
					else if (resource.hasProperty(RDF.type, Schema.SoftwareComponent))
					{
						final ResIterator iterator = model.listSubjectsWithProperty(Schema.softwareComponent, resource);
						while (iterator.hasNext())
						{
							list.add(iterator.nextResource());
						}
					}
					return list.toArray();
				}
				return new Object[0];
			}

			public Object[] getElements(final Object parent)
			{
				if (parent.equals(model))
				{
					final ArrayList<Resource> list = new ArrayList<Resource>();
					final ResIterator iterator = model.listSubjectsWithProperty(RDF.type, Schema.SoftwareContainer);
					while (iterator.hasNext())
					{
						final Resource resource = iterator.nextResource();
						list.add(resource);
					}
					return list.toArray();
				}
				return getChildren(parent);
			}

			/*
			 * (non-Javadoc)
			 * @see
			 * com.hp.hpl.jena.rdf.model.ModelChangedListener#removedStatement
			 * (com.hp.hpl.jena.rdf.model.Statement)
			 */
			@Override
			public void removedStatement(final Statement s)
			{
				if (Schema.softwareContainer.equals(s.getPredicate()))
				{
					getTreeViewer().remove(s.getObject(), new Object[] { s.getSubject() });
				}
				else
				{
					super.removedStatement(s);
				}
			}
		};
	}

	@Override
	protected void makeActions()
	{
		deleteComponentAction = new Action()
		{
			@Override
			public void run()
			{
				final ISelection selection = viewer.getSelection();
				if (selection.isEmpty()) { return; }
				final Resource resource = (Resource) ((IStructuredSelection) selection).getFirstElement();
				final DeleteComponentCommand command = new DeleteComponentCommand(resource);
				command.execute();
			}
		};
		deleteComponentAction.setText("Delete Component");
		deleteComponentAction.setToolTipText("Delete Comment");
		final ISharedImages images = PlatformUI.getWorkbench().getSharedImages();

		deleteComponentAction.setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		deleteComponentAction.setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));
		deleteComponentAction.setEnabled(false);

		getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.DELETE.getId(), deleteComponentAction);
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
						deleteComponentAction.setEnabled(false);
						return;
					}
					else if (!((Resource) element).hasProperty(RDF.type, Schema.SoftwareComponent))
					{
						deleteComponentAction.setEnabled(false);
						return;
					}
				}
				deleteComponentAction.setEnabled(!viewer.getSelection().isEmpty());
			}
		});
	}

	void showMessage(final String message)
	{
		MessageDialog.openInformation(viewer.getControl().getShell(), "Physical Things", message);
	}
}