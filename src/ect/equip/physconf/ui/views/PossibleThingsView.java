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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.ModelUpdateListener;
import ect.equip.physconf.PhysConfPlugin;
import ect.equip.physconf.rules.ECTInfModel;
import ect.equip.physconf.rules.ECTRuleInfGraph;
import ect.equip.physconf.rules.ECTRuleReasoner;
import ect.equip.physconf.ui.ModelLabelProvider;
import ect.equip.physconf.ui.PhysicalEditDomain;
import ect.equip.physconf.ui.commands.CreateResourceCommand;
import ect.equip.physconf.ui.commands.CreateResourceRequest;
import equip.ect.RDFStatement;
import equip.ect.util.DirectoryEventListener;
import equip.ect.util.DirectoryMonitor;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.dnd.TemplateTransfer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISaveablesSource;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.Saveable;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class PossibleThingsView extends ResourceView implements ISaveablePart, ISaveablesSource
{
	private class ColouredModelLabelProvider extends ModelLabelProvider implements IColorProvider
	{
		private final Model model = PhysConfPlugin.getDefault().getModel();

		public Color getBackground(final Object element)
		{
			return null;
		}

		public Color getForeground(final Object element)
		{
			if (element instanceof Resource)
			{
				if (model.containsResource((RDFNode) element)) { return Display.getDefault().getSystemColor(
						SWT.COLOR_GRAY); }
			}
			return null;
		}

		@Override
		public Image getImage(final Object obj)
		{
			final Image image = super.getImage(obj);
			if (obj instanceof Resource)
			{
				if (model.containsResource((RDFNode) obj))
				{
					// return new Image(image.getDevice(), image,
					// SWT.IMAGE_DISABLE);
				}
			}
			return image;
		}
	}

	public static final String ID = "ect.equip.physconf.ui.views.PossibleThingsView";

	private final ModelUpdateListener listener = new ModelUpdateListener()
	{
		@Override
		public boolean shouldUpdate(final Statement s)
		{
			return infModel.containsResource(s.getSubject());
		}

		@Override
		public void update()
		{
			if (!viewer.getControl().isDisposed())
			{
				viewer.refresh();
			}
		}
	};

	protected Action createAction;
	protected Model infModel;

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createPartControl(final Composite parent)
	{
		super.createPartControl(parent);

		PhysConfPlugin.getDefault().getModel().register(listener);
		viewer.addDoubleClickListener(new IDoubleClickListener()
		{
			public void doubleClick(final DoubleClickEvent event)
			{
				final Object selected = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				if (selected instanceof Resource)
				{
					createAction.run();
				}
				else
				{
					final TreeItem item = (TreeItem) viewer.testFindItem(selected);
					if (item.getExpanded())
					{
						viewer.collapseToLevel(selected, 1);
					}
					else
					{
						viewer.expandToLevel(selected, 1);
					}
				}
			}
		});
		viewer.setInput(getModel());
		viewer.setAutoExpandLevel(0);
		viewer.collapseAll();
		viewer.setLabelProvider(new ColouredModelLabelProvider());
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
					final Resource resource = (Resource) selection;
					if (PhysConfPlugin.getDefault().getModel().containsResource(resource))
					{
						event.doit = false;
					}
					else if (resource.hasProperty(Schema.image))
					{
						event.image = PhysConfPlugin.getDefault().getImage(
								resource.getProperty(Schema.image).getResource());
						event.image = new Image(event.image.getDevice(), event.image.getImageData());
					}
				}
			}
		});
		viewer.setSorter(new ViewerSorter()
		{
			@Override
			public int category(final Object element)
			{
				if (element instanceof Classification) { return 0; }
				return 1;
			}
		});
	}

	@Override
	public void dispose()
	{
		super.dispose();
		PhysConfPlugin.getDefault().getModel().unregister(listener);
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

	@SuppressWarnings("unchecked")
	private Model getModel()
	{
		final Model model = ModelFactory.createDefaultModel();
		// model.setNsPrefix("ect", Schema.getURI());
		model.setNsPrefix("ectguid", RDFStatement.GUID_NAMESPACE);

		PhysConfPlugin.loadRDF(model, PhysConfPlugin.getDirectory("/ontologies/").toFile());

		final List rules = PhysConfPlugin.loadRules(PhysConfPlugin.getDirectory("/rules/").toFile());
		final ECTRuleReasoner reasoner = new ECTRuleReasoner(rules);
		reasoner.setMode(GenericRuleReasoner.FORWARD_RETE);
		// reasoner.setTraceOn(true);

		final ECTRuleInfGraph infGraph = (ECTRuleInfGraph) reasoner.bind(model.getGraph());
		infModel = new ECTInfModel(infGraph);

		final IPath rdfPath = PhysConfPlugin.getDirectory("/rdf/");
		PhysConfPlugin.getDefault().addPath(rdfPath.toOSString());

		try
		{
			final DirectoryMonitor dirMon = new DirectoryMonitor(rdfPath.toFile(), true, false);
			dirMon.addDirectoryEventListener(new DirectoryEventListener()
			{

				public void fileAdd(final File file)
				{

				}

				public void fileAddComplete(final File file)
				{
					if (file.getName().endsWith(".rdf"))
					{
						Display.getDefault().asyncExec(new Runnable()
						{

							public void run()
							{
								try
								{
									infModel.read(new FileInputStream(file), "");
									viewer.refresh();
								}
								catch (final Exception e)
								{
									e.printStackTrace();
								}
							}
						});

					}
				}

				public void fileDeleted(final File file)
				{

				}

				public void fileModified(final File file)
				{
				}
			});
			new Thread(dirMon).start();
		}
		catch (final IOException e1)
		{
			e1.printStackTrace();
		}

		return infModel;
	}

	@Override
	protected void fillContextMenu(final IMenuManager manager)
	{
		manager.add(createAction);
		manager.add(new Separator());
		super.fillContextMenu(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	@Override
	protected void fillLocalPullDown(final IMenuManager manager)
	{
		manager.add(createAction);
		manager.add(new Separator());
	}

	@Override
	protected void fillLocalToolBar(final IToolBarManager manager)
	{
		manager.add(createAction);
		manager.add(new Separator());
		super.fillLocalToolBar(manager);
	}

	/*
	 * (non-Javadoc)
	 * @see ect.equip.physconf.ui.views.ResourceView#getContentProvider()
	 */
	@Override
	protected ITreeContentProvider getContentProvider()
	{
		return new PhysicalThingTreeContentProvider();
	}

	@Override
	protected void makeActions()
	{
		super.makeActions();

		createAction = new Action("Create Thing")
		{
			@Override
			public void run()
			{
				final Model target = PhysConfPlugin.getDefault().getModel();
				final Object selected = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				if (selected instanceof Resource)
				{
					final Resource resource = (Resource) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
					final CreateResourceRequest request = new CreateResourceRequest(target, resource);
					final CreateResourceCommand command = new CreateResourceCommand(target, request, null);
					PhysicalEditDomain.INSTANCE.getCommandStack().execute(command);
				}
			}
		};
		createAction.setEnabled(false);
		viewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(final SelectionChangedEvent event)
			{
				createAction.setEnabled(!event.getSelection().isEmpty());
			}
		});
	}
}