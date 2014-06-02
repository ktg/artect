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
import java.util.List;

import ect.equip.physconf.PhysConfPlugin;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.SharedImages;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackEvent;
import org.eclipse.gef.commands.CommandStackEventListener;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.tools.ConnectionDragCreationTool;
import org.eclipse.gef.tools.PanningSelectionTool;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.DeleteAction;
import org.eclipse.gef.ui.actions.PrintAction;
import org.eclipse.gef.ui.actions.RedoAction;
import org.eclipse.gef.ui.actions.SelectAllAction;
import org.eclipse.gef.ui.actions.UndoAction;
import org.eclipse.gef.ui.actions.ZoomComboContributionItem;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public abstract class GraphView extends ViewPart implements ITabbedPropertySheetPageContributor
{
	protected final ActionRegistry actionRegistry = new ActionRegistry();
	protected DeleteAction deleteAction;
	protected UndoAction undoAction;
	protected RedoAction redoAction;
	protected Action selectAction;
	protected Action connectAction;

	protected final GraphicalViewer viewer = new ScrollingGraphicalViewer();
	protected final ISelectionListener deleteListener = new ISelectionListener()
	{
		public void selectionChanged(final IWorkbenchPart part, final ISelection selection)
		{
			deleteAction.update();
		}
	};

	// protected Thumbnail thumbnail;

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createPartControl(final Composite parent)
	{
		final ScalableFreeformRootEditPart root = getRootEditPart();
		final List<String> zoomLevels = new ArrayList<String>(3);
		zoomLevels.add(ZoomManager.FIT_ALL);
		zoomLevels.add(ZoomManager.FIT_WIDTH);
		zoomLevels.add(ZoomManager.FIT_HEIGHT);
		root.getZoomManager().setZoomLevelContributions(zoomLevels);
		root.getZoomManager().setZoomLevels(
				new double[] { 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.2, 1.4, 1.6, 1.8, 2.0, 2.5, 3.0, 4.0,
						5.0, 6.0 });

		final EditDomain domain = getEditDomain();
		domain.getCommandStack().addCommandStackEventListener(new CommandStackEventListener()
		{
			public void stackChanged(final CommandStackEvent event)
			{
				undoAction.update();
				redoAction.update();
				getViewSite().getActionBars().getToolBarManager().markDirty();
			}
		});
		// final SashForm splitter = new SashForm(parent, SWT.NONE);

		viewer.setEditDomain(domain);
		viewer.setRootEditPart(root);
		viewer.setEditPartFactory(getEditPartFactory());
		viewer.setKeyHandler(new GraphicalViewerKeyHandler(viewer));
		viewer.setContents(PhysConfPlugin.getDefault().getModel());
		// viewer.createControl(splitter);
		viewer.createControl(parent);
		viewer.getControl().setBackground(ColorConstants.listBackground);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
		// viewer.setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.NONE),
		// new MouseWheelHandler()
		// {
		// public void handleMouseWheel(final Event event, final EditPartViewer
		// viewer)
		// {
		// if (event.count < 0)
		// {
		// root.getZoomManager().zoomIn();
		// }
		// else
		// {
		// root.getZoomManager().zoomOut();
		// }
		// }
		// });

		// final Composite paletteComposite = new Composite(splitter, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		// paletteComposite.setLayout(gridLayout);
		// splitter.setWeights(new int[] { 80, 20 });

		// paletteViewer.createControl(paletteComposite);
		// domain.setPaletteViewer(paletteViewer);
		// paletteViewer.setPaletteRoot(getPaletteRoot());
		// paletteViewer.getControl().setLayoutData(new GridData(GridData.FILL,
		// GridData.FILL, true, false));

		// overview = new Canvas(paletteComposite, SWT.NONE);
		// overview.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
		// true, true));
		// initializeOverview(root);

		getSite().setSelectionProvider(viewer);
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(deleteListener);

		final ZoomComboContributionItem zoomCombo = new ZoomComboContributionItem(getSite().getPage());
		zoomCombo.setZoomManager(root.getZoomManager());

		final IAction zoomIn = new ZoomInAction(root.getZoomManager());
		final IAction zoomOut = new ZoomOutAction(root.getZoomManager());
		actionRegistry.registerAction(zoomIn);
		actionRegistry.registerAction(zoomOut);

		final IToolBarManager toolbar = getViewSite().getActionBars().getToolBarManager();

		getEditDomain().setDefaultTool(new PanningSelectionTool());
		selectAction = new Action("Move Things")
		{
			@Override
			public void run()
			{
				getEditDomain().setActiveTool(new PanningSelectionTool());
				setChecked(true);
				connectAction.setChecked(false);
			}
		};
		selectAction.setChecked(true);
		selectAction.setImageDescriptor(SharedImages.DESC_SELECTION_TOOL_16);

		connectAction = new Action("Create Connection")
		{
			@Override
			public void run()
			{
				final ConnectionDragCreationTool tool = new ConnectionDragCreationTool()
				{
					@SuppressWarnings("unchecked")
					@Override
					protected boolean handleButtonUp(final int button)
					{
						if (isInState(STATE_DRAG))
						{
							final EditPartViewer viewer = getCurrentViewer();
							final EditPart selectPart = viewer.findObjectAt(getLocation());
							if (selectPart != null)
							{
								final List<EditPart> selectedObjects = viewer.getSelectedEditParts();

								if (getCurrentInput().isModKeyDown(SWT.MOD1))
								{
									if (selectedObjects.contains(selectPart))
									{
										viewer.deselect(selectPart);
									}
									else
									{
										viewer.appendSelection(selectPart);
									}
								}
								else if (getCurrentInput().isShiftKeyDown())
								{
									viewer.appendSelection(selectPart);
								}
								else
								{
									viewer.select(selectPart);
								}
							}
							else
							{
								viewer.deselectAll();
							}
						}
						return super.handleButtonUp(button);
					}
				};
				tool.setUnloadWhenFinished(false);
				getEditDomain().setActiveTool(tool);
				selectAction.setChecked(false);
				setChecked(true);
			}
		};
		connectAction.setChecked(true);
		connectAction.setChecked(false);
		connectAction.setImageDescriptor(PhysConfPlugin.getDescriptor("connection"));

		final IActionBars actionBars = getViewSite().getActionBars();
		undoAction = new UndoAction(this)
		{
			@Override
			public void setText(final String text)
			{
				super.setText("Undo");
			}
		};
		actionRegistry.registerAction(undoAction);
		actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(), undoAction);

		redoAction = new RedoAction(this)
		{
			@Override
			public void setText(final String text)
			{
				super.setText("Redo");
			}
		};
		actionRegistry.registerAction(redoAction);
		actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(), redoAction);

		final IAction action = new SelectAllAction(this);
		actionRegistry.registerAction(action);
		actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(), action);

		deleteAction = new DeleteAction(this);
		actionRegistry.registerAction(deleteAction);
		actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), deleteAction);

		actionRegistry.registerAction(new PrintAction(this));
		actionBars.setGlobalActionHandler(ActionFactory.PRINT.getId(), action);

		ActionContributionItem item = new ActionContributionItem(selectAction);
		item.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		toolbar.add(item);

		item = new ActionContributionItem(connectAction);
		item.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		toolbar.add(item);

		toolbar.add(new Separator());

		item = new ActionContributionItem(undoAction);
		item.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		toolbar.add(item);

		item = new ActionContributionItem(redoAction);
		item.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		toolbar.add(item);

		item = new ActionContributionItem(deleteAction);
		item.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		// toolbar.add(item);
		// toolbar.add(refreshAction);

		toolbar.add(new Separator());

		toolbar.add(zoomIn);
		toolbar.add(zoomOut);
		toolbar.add(zoomCombo);
	}

	@Override
	public void dispose()
	{
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(deleteListener);
		// paletteViewer.getEditDomain().setActiveTool(null);
		actionRegistry.dispose();
		super.dispose();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object getAdapter(final Class adapter)
	{
		if (adapter.equals(IPropertySheetPage.class))
		{
			return new TabbedPropertySheetPage(this);
		}
		else if (adapter.equals(ZoomManager.class))
		{
			return viewer.getProperty(ZoomManager.class.toString());
		}
		else if (adapter.equals(GraphicalViewer.class))
		{
			return viewer;
		}
		else if (adapter == CommandStack.class)
		{
			return getEditDomain().getCommandStack();
		}
		else if (adapter == ActionRegistry.class)
		{
			return actionRegistry;
		}
		else if (adapter == EditPart.class)
		{
			return getRootEditPart();
		}
		else if (adapter == IFigure.class && viewer != null) { return getRootEditPart().getFigure(); }

		return super.getAdapter(adapter);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor
	 * #getContributorId()
	 */
	public String getContributorId()
	{
		return "ect.equip.physconf.ui.views.ResourceView";
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus()
	{
		viewer.getControl().setFocus();
	}

	protected abstract EditDomain getEditDomain();

	protected abstract EditPartFactory getEditPartFactory();

	protected abstract ScalableFreeformRootEditPart getRootEditPart();

	// protected void initializeOverview(final ScalableFreeformRootEditPart
	// root)
	// {
	// final LightweightSystem lws = new LightweightSystem(overview);
	// thumbnail = new ScrollableThumbnail((Viewport) root.getFigure());
	// thumbnail.setSource(root.getLayer(LayerConstants.PRINTABLE_LAYERS));
	// lws.setContents(thumbnail);
	// final DisposeListener disposeListener = new DisposeListener()
	// {
	// public void widgetDisposed(DisposeEvent e)
	// {
	// if (thumbnail != null)
	// {
	// thumbnail.deactivate();
	// thumbnail = null;
	// }
	// }
	// };
	// viewer.getControl().addDisposeListener(disposeListener);
	// }
}