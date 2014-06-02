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
package ect.equip.physconf.ui.graph.parts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDFS;

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.ECTEvent;
import ect.equip.physconf.ModelUpdateListener;
import ect.equip.physconf.PhysConfPlugin;
import ect.equip.physconf.ui.ModelLabelProvider;
import ect.equip.physconf.ui.commands.StatementListCommand;
import ect.equip.physconf.ui.commands.StatementQueueCommand;
import ect.equip.physconf.ui.graph.figures.PhysicalFigure;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.GroupRequest;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class Location extends PhysicalThing
{
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies()
	{
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new XYLayoutEditPolicy()
		{
			@Override
			public Rectangle getCurrentConstraintFor(final GraphicalEditPart child)
			{
				final IFigure fig = child.getFigure();
				Rectangle rectangle = (Rectangle) fig.getParent().getLayoutManager().getConstraint(fig);
				if (rectangle == null)
				{
					rectangle = fig.getBounds();
				}
				return rectangle;
			}

			@Override
			protected Command createChangeConstraintCommand(final EditPart child, final Object constraint)
			{
				return null;
			}

			@Override
			@SuppressWarnings("unchecked")
			protected Command getAddCommand(final Request generic)
			{
				final ChangeBoundsRequest request = (ChangeBoundsRequest) generic;
				final Model model = PhysConfPlugin.getDefault().getModel();
				final StatementQueueCommand command = new StatementQueueCommand(model);
				final StatementListCommand addStatements = command.getEventList(ECTEvent.Type.ADDED);
				final StatementListCommand updateStatements = command.getEventList(ECTEvent.Type.UPDATED);
				command.setDebugLabel("Add in ConstrainedLayoutEditPolicy");//$NON-NLS-1$

				for (final GraphicalEditPart child : (List<GraphicalEditPart>) request.getEditParts())
				{
					if (child instanceof ResourcePart)
					{
						Rectangle r = child.getFigure().getBounds().getCopy();
						// convert r to absolute from childpart figure
						child.getFigure().translateToAbsolute(r);
						r = request.getTransformedRectangle(r);
						// convert this figure to relative
						getLayoutContainer().translateToRelative(r);
						getLayoutContainer().translateFromParent(r);
						r.translate(getLayoutOrigin().getNegated());
						final Object constraint = getConstraintFor(r);

						final ResourcePart resourcePart = (ResourcePart) child;
						final Resource resource = resourcePart.getResource();

						// Rectangle locationRect = getFigure().getBounds();
						final Rectangle newRect = (Rectangle) constraint;
						addStatements.add(resource, Schema.isIn, getResource());
						updateStatements.add(resource, Schema.xOffset, newRect.x);
						updateStatements.add(resource, Schema.yOffset, newRect.y);
					}
				}
				return command;
			}

			@Override
			protected Command getCreateCommand(final CreateRequest request)
			{
				return null;
			}

			@Override
			@SuppressWarnings("unchecked")
			protected Command getMoveChildrenCommand(final Request moveRequest)
			{
				final Model model = PhysConfPlugin.getDefault().getModel();
				final StatementQueueCommand command = new StatementQueueCommand(model);
				final StatementListCommand addStatements = command.getEventList(ECTEvent.Type.ADDED);
				final StatementListCommand updateStatements = command.getEventList(ECTEvent.Type.UPDATED);
				final ChangeBoundsRequest request = (ChangeBoundsRequest) moveRequest;
				for (final GraphicalEditPart child : (List<GraphicalEditPart>) request.getEditParts())
				{
					if (child instanceof ResourcePart)
					{
						final Object constraint = getConstraintFor(request, child);
						if (!(constraint instanceof Rectangle))
						{
							continue;
						}
						final ResourcePart resourcePart = (ResourcePart) child;
						final Resource resource = resourcePart.getResource();
						final Rectangle newBounds = (Rectangle) constraint;
						addStatements.add(resource, Schema.isIn, getResource());
						updateStatements.add(resource, Schema.xOffset, newBounds.x);
						updateStatements.add(resource, Schema.yOffset, newBounds.y);
					}
				}
				return command;
			}

			@Override
			@SuppressWarnings("unchecked")
			protected Command getOrphanChildrenCommand(final Request generic)
			{
				final Resource location = getResource();
				final Model model = location.getModel();
				final GroupRequest request = (GroupRequest) generic;
				final StatementListCommand command = new StatementListCommand(model, ECTEvent.Type.REMOVED);
				for (final ResourcePart child : (List<ResourcePart>) request.getEditParts())
				{
					final Resource resource = child.getResource();
					command.add(resource, Schema.isIn, location);
					command.add(resource.listProperties(Schema.xOffset));
					command.add(resource.listProperties(Schema.yOffset));
				}
				return command;
			}

			@Override
			protected Command getResizeChildrenCommand(final ChangeBoundsRequest request)
			{
				return null;
			}
		});
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ComponentEditPolicy()
		{
			@Override
			protected Command getDeleteCommand(final GroupRequest request)
			{
				final Model model = PhysConfPlugin.getDefault().getModel();
				final StatementListCommand command = new StatementListCommand(model, ECTEvent.Type.REMOVED);
				command.add(model.listStatements(null, Schema.isIn, getResource()));
				command.add(getResource().listProperties());
				return command;
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure()
	{
		return new PhysicalFigure(getResource());
	}

	/*
	 * (non-Javadoc)
	 * @see ect.equip.physconf.ui.graph.parts.ResourceEditPart#getListener()
	 */
	@Override
	protected ModelChangedListener createListener()
	{
		return new ModelUpdateListener()
		{
			@Override
			public boolean shouldUpdate(final Statement s)
			{
				if (s.getSubject().equals(getResource()))
				{
					if (Schema.hasPart.equals(s.getPredicate()))
					{
						return true;
					}
					else if (RDFS.label.equals(s.getPredicate())) { return true; }
				}
				else if (s.getObject().equals(getResource()))
				{
					if (Schema.isIn.equals(s.getPredicate())) { return true; }
				}
				return false;
			}

			@Override
			public void update()
			{
				refresh();
			}
		};
	}

	@Override
	protected List<Resource> getModelChildren()
	{
		final Resource physicalThing = getResource();
		final List<Resource> list = new ArrayList<Resource>();
		final ResIterator iterator = physicalThing.getModel().listSubjectsWithProperty(Schema.isIn, physicalThing);
		while (iterator.hasNext())
		{
			list.add(iterator.nextResource());
		}
		Collections.sort(list, new Comparator<Resource>()
		{
			public int compare(final Resource o1, final Resource o2)
			{
				return ModelLabelProvider.text(o1).compareTo(ModelLabelProvider.text(o2));
			}
		});
		return list;
	}

	@Override
	protected void refreshVisuals()
	{
		super.refreshVisuals();
		getFigure().getLayoutManager().layout(getFigure());
	}
}