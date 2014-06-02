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

import java.io.File;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

import ect.equip.physconf.PhysConfPlugin;
import ect.equip.physconf.ui.PhysicalEditDomain;
import ect.equip.physconf.ui.commands.CreateResourceRequest;
import ect.equip.physconf.ui.filters.TypeMapper;
import ect.equip.physconf.ui.graph.parts.PhysicalPartFactory;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.gef.dnd.TemplateTransfer;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISaveablesSource;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.Saveable;

public class PhysicalGraphView extends GraphView implements ISelectionListener, ISaveablePart, ISaveablesSource,
		CommandStackListener
{
	public static final ScalableFreeformRootEditPart root = new ScalableFreeformRootEditPart();

	public static final String ID = "ect.equip.physconf.ui.views.PhysicalGraphView";

	protected Action saveAction;

	public PhysicalGraphView()
	{
		super();
		PhysicalEditDomain.INSTANCE.getCommandStack().addCommandStackListener(this);
	}

	public void commandStackChanged(final EventObject arg0)
	{
		firePropertyChange(ISaveablePart.PROP_DIRTY);
		final String filename = PhysicalEditDomain.INSTANCE.getFilename();
		if (filename != null)
		{
			final File file = new File(filename);
			setPartName("ARTECT Editor [" + file.getName() + "]");
		}
	}

	@Override
	public void createPartControl(final Composite parent)
	{
		super.createPartControl(parent);
		viewer.addDropTargetListener(new TemplateTransferDropTargetListener(viewer)
		{
			@Override
			protected Request createTargetRequest()
			{
				final Model model = PhysConfPlugin.getDefault().getModel();
				return new CreateResourceRequest(model, (Resource) TemplateTransfer.getInstance().getTemplate());
			}
		});
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

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(final Class adapter)
	{
		if (adapter == ISaveablePart.class) { return this; }
		return super.getAdapter(adapter);
	}

	public Saveable[] getSaveables()
	{
		return PhysicalEditDomain.INSTANCE.getSaveables();
	}

	public boolean isDirty()
	{
		return getEditDomain().getCommandStack().isDirty();
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
		// viewer.setSelection(new StructuredSelection(selected));
	}

	@Override
	protected EditDomain getEditDomain()
	{
		return PhysicalEditDomain.INSTANCE;
	}

	@Override
	protected EditPartFactory getEditPartFactory()
	{
		return new PhysicalPartFactory();
	}

	@Override
	protected ScalableFreeformRootEditPart getRootEditPart()
	{
		return root;
	}
}