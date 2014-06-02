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
import java.util.EventObject;
import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

import ect.equip.physconf.ECTEvent;
import ect.equip.physconf.PhysConfPlugin;
import ect.equip.physconf.ui.PhysicalEditDomain;
import ect.equip.physconf.ui.commands.DeleteThingCommand;
import ect.equip.physconf.ui.filters.TypeMapper;
import ect.equip.physconf.ui.wizards.AddPropertyWizard;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.commands.CommandStackListener;
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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISaveablesSource;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.actions.ActionFactory;

public class ActiveThingsView extends ResourceView implements ISelectionListener, ISaveablePart, ISaveablesSource,
		CommandStackListener
{
	public static final String ID = "ect.equip.physconf.ui.views.ActiveThingsView";
	private Action addPropertyAction;
	protected Action deleteThingAction;

	public ActiveThingsView()
	{
		super();
		PhysicalEditDomain.INSTANCE.getCommandStack().addCommandStackListener(this);
	}

	public void commandStackChanged(final EventObject arg0)
	{
		firePropertyChange(ISaveablePart.PROP_DIRTY);
	}

	@Override
	public void createPartControl(final Composite parent)
	{
		super.createPartControl(parent);
		viewer.setAutoExpandLevel(0);
		getSite().getPage().addSelectionListener(PhysicalGraphView.ID, this);
	}

	@Override
	public void dispose()
	{
		super.dispose();
		PhysicalEditDomain.INSTANCE.getCommandStack().removeCommandStackListener(this);
	}

	public void doSave(final IProgressMonitor monitor)
	{
	}

	public void doSaveAs()
	{
		PhysicalEditDomain.INSTANCE.doSaveAs();
	}

	public Saveable[] getActiveSaveables()
	{
		return PhysicalEditDomain.INSTANCE.getSaveables();
	}

	public Saveable[] getSaveables()
	{
		return PhysicalEditDomain.INSTANCE.getSaveables();
	}

	public boolean isDirty()
	{
		return PhysicalEditDomain.INSTANCE.getCommandStack().isDirty();
	}

	public boolean isSaveAsAllowed()
	{
		return true;
	}

	public boolean isSaveOnCloseNeeded()
	{
		return isDirty();
	}

	public void selectionChanged(final IWorkbenchPart part, final ISelection selection)
	{
		final ArrayList<Object> selected = new ArrayList<Object>();
		final Iterator<?> iterator = ((StructuredSelection) selection).iterator();
		while (iterator.hasNext())
		{
			selected.add(TypeMapper.getModelObject(iterator.next()));
		}
		viewer.setSelection(new StructuredSelection(selected));
	}

	private void deleteElement(final Object element, final DeleteThingCommand command)
	{
		if (element instanceof Resource)
		{
			command.deleteResource((Resource) element);
		}
		else if (element instanceof Classification)
		{
			final Classification classification = (Classification) element;
			for (final Object child : classification)
			{
				deleteElement(child, command);
			}
		}
	}

	@Override
	protected void fillContextMenu(final IMenuManager manager)
	{
		manager.add(addPropertyAction);
		manager.add(deleteThingAction);
		manager.add(new Separator());
		super.fillContextMenu(manager);

		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	@Override
	protected void fillLocalPullDown(final IMenuManager manager)
	{
		manager.add(addPropertyAction);
		manager.add(deleteThingAction);
		manager.add(new Separator());
		super.fillLocalPullDown(manager);
	}

	@Override
	protected void fillLocalToolBar(final IToolBarManager manager)
	{
		manager.add(addPropertyAction);
		manager.add(deleteThingAction);
		manager.add(new Separator());

		super.fillLocalToolBar(manager);
	}

	@Override
	protected ITreeContentProvider getContentProvider()
	{
		return new PhysicalThingTreeContentProvider();
	}

	@Override
	protected void makeActions()
	{
		addPropertyAction = new Action()
		{
			@Override
			public void run()
			{
				// Create the wizard
				final ISelection selection = viewer.getSelection();
				final Resource resource = (Resource) ((IStructuredSelection) selection).getFirstElement();
				final AddPropertyWizard wizard = new AddPropertyWizard(resource);

				// Create the wizard dialog
				final WizardDialog dialog = new WizardDialog(getSite().getShell(), wizard);
				// Open the wizard dialog
				dialog.open();
			}
		};
		addPropertyAction.setText("Add Property");
		addPropertyAction.setToolTipText("Add Property");
		addPropertyAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
				ISharedImages.IMG_OBJS_INFO_TSK));

		deleteThingAction = new Action("Delete")
		{
			@Override
			public void run()
			{
				final Iterator<?> iter = ((IStructuredSelection) viewer.getSelection()).iterator();
				final Model model = PhysConfPlugin.getDefault().getModel();
				final DeleteThingCommand command = new DeleteThingCommand(model, ECTEvent.Type.REMOVED);
				while (iter.hasNext())
				{
					final Object element = iter.next();
					deleteElement(element, command);
				}
				command.setLabel("Delete");
				PhysicalEditDomain.INSTANCE.getCommandStack().execute(command);
			}
		};
		deleteThingAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
				ISharedImages.IMG_TOOL_DELETE));
		deleteThingAction.setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
				ISharedImages.IMG_TOOL_DELETE_DISABLED));
		deleteThingAction.setEnabled(false);
		getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.DELETE.getId(), deleteThingAction);
		getViewSite().setSelectionProvider(viewer);
		getSite().getSelectionProvider().addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(final SelectionChangedEvent event)
			{
				final Iterator<?> iter = ((IStructuredSelection) viewer.getSelection()).iterator();
				while (iter.hasNext())
				{
					final Object element = iter.next();
					if (!(element instanceof Resource) && !(element instanceof Classification))
					{
						deleteThingAction.setEnabled(false);
						return;
					}
				}
				deleteThingAction.setEnabled(!viewer.getSelection().isEmpty());
			}
		});
	}

	void showMessage(final String message)
	{
		MessageDialog.openInformation(viewer.getControl().getShell(), "Physical Things", message);
	}
}