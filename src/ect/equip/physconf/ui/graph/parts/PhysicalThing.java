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
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.ECTEvent;
import ect.equip.physconf.ModelUpdateListener;
import ect.equip.physconf.PhysConfPlugin;
import ect.equip.physconf.ui.ModelLabelProvider;
import ect.equip.physconf.ui.commands.CreateAssociationCommand;
import ect.equip.physconf.ui.commands.DeleteThingCommand;
import ect.equip.physconf.ui.graph.figures.PhysicalFigure;
import ect.equip.physconf.ui.graph.policies.PhysicalNodeEditPolicy;
import ect.equip.physconf.ui.views.ResourceView;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class PhysicalThing extends ResourcePart implements NodeEditPart, ITabbedPropertySheetPageContributor
{
	public String getContributorId()
	{
		return ResourceView.VIEWID;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef
	 * .ConnectionEditPart)
	 */
	public ConnectionAnchor getSourceConnectionAnchor(final ConnectionEditPart connection)
	{
		return new ChopboxAnchor(getFigure());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef
	 * .Request)
	 */
	public ConnectionAnchor getSourceConnectionAnchor(final Request request)
	{
		return new ChopboxAnchor(getFigure());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef
	 * .ConnectionEditPart)
	 */
	public ConnectionAnchor getTargetConnectionAnchor(final ConnectionEditPart connection)
	{
		return new ChopboxAnchor(getFigure());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef
	 * .Request)
	 */
	public ConnectionAnchor getTargetConnectionAnchor(final Request request)
	{
		return new ChopboxAnchor(getFigure());
	}

	private Resource getPropertyProxy(final Resource physicalThing)
	{
		final StmtIterator iterator = physicalThing.listProperties(Schema.hasProxy);
		final boolean found = false;
		while (iterator.hasNext() && !found)
		{
			final Resource resource = iterator.nextStatement().getResource();
			if (resource.hasProperty(RDF.type, Schema.SoftwareComponentProperty)) { return resource; }
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies()
	{
		removeEditPolicy(EditPolicy.LAYOUT_ROLE);
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ComponentEditPolicy()
		{
			@Override
			protected Command getDeleteCommand(final GroupRequest request)
			{
				// TODO Group delete?
				final Resource resource = getResource();
				final DeleteThingCommand command = new DeleteThingCommand(resource.getModel(), ECTEvent.Type.REMOVED);
				command.deleteResource(resource);
				command.setLabel("Delete " + ModelLabelProvider.text(resource));

				return command;
			}
		});
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new PhysicalNodeEditPolicy(CreateAssociationCommand.class));
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
					return Schema.hasPart.equals(s.getPredicate()) || RDFS.label.equals(s.getPredicate())
							|| Schema.matchState.equals(s.getPredicate()) || Schema.xOffset.equals(s.getPredicate())
							|| Schema.yOffset.equals(s.getPredicate());
				}
				else if (s.getObject().equals(getResource())
						&& (Schema.destination.equals(s.getPredicate()) || Schema.source.equals(s.getPredicate())))
				{
					return true;
				}
				else if (getResource().hasProperty(Schema.hasProxy, s.getObject()))
				{
					return Schema.source.equals(s.getPredicate()) || Schema.destination.equals(s.getPredicate());
				}
				else if (RDF.value.equals(s.getPredicate())
						&& getResource().hasProperty(Schema.hasProxy, s.getSubject())) { return true; }
				return false;
			}

			@Override
			public void update()
			{
				final IFigure figure = getFigure();
				if (figure.getParent() != null)
				{
					refresh();
					figure.getParent().getLayoutManager().layout(figure.getParent());
				}
			}
		};
	}

	@Override
	protected List<Resource> getModelChildren()
	{
		final Resource physicalThing = getResource();
		final List<Resource> list = new ArrayList<Resource>();
		final StmtIterator iterator = physicalThing.listProperties(Schema.hasPart);
		while (iterator.hasNext())
		{
			try
			{
				list.add(iterator.nextStatement().getResource());
			}
			catch (final Exception e)
			{

			}
		}
		Collections.sort(list, new Comparator<RDFNode>()
		{
			public int compare(final RDFNode o1, final RDFNode o2)
			{
				return ModelLabelProvider.text(o1).compareTo(ModelLabelProvider.text(o2));
			}
		});
		return list;
	}

	@Override
	protected List<Resource> getModelSourceConnections()
	{
		final Resource resource = getResource();
		final Model model = PhysConfPlugin.getDefault().getModel();
		final List<Resource> list = new ArrayList<Resource>();
		final ResIterator iterator = model.listSubjectsWithProperty(Schema.source, resource);
		while (iterator.hasNext())
		{
			list.add(iterator.nextResource());
		}
		final Resource proxy = getPropertyProxy(resource);
		if (proxy != null)
		{
			final ResIterator resIterator = model.listSubjectsWithProperty(Schema.source, proxy);
			while (resIterator.hasNext())
			{
				list.add(resIterator.nextResource());
			}
		}
		return list;
	}

	@Override
	protected List<Resource> getModelTargetConnections()
	{
		final Resource resource = getResource();
		final Model model = PhysConfPlugin.getDefault().getModel();
		final List<Resource> list = new ArrayList<Resource>();
		final ResIterator iterator = model.listSubjectsWithProperty(Schema.destination, resource);
		while (iterator.hasNext())
		{
			list.add(iterator.nextResource());
		}
		final Resource proxy = getPropertyProxy(resource);
		if (proxy != null)
		{
			final ResIterator resIterator = model.listSubjectsWithProperty(Schema.destination, proxy);
			while (resIterator.hasNext())
			{
				list.add(resIterator.nextResource());
			}
		}
		return list;
	}

	@Override
	protected void refreshVisuals()
	{
		super.refreshVisuals();
		final Figure figure = (Figure) getFigure();
		// figure.getParent().getLayoutManager().invalidate();
		if (figure instanceof PhysicalFigure)
		{
			((PhysicalFigure) figure).refresh();
		}
		else
		{
			figure.repaint();
		}
	}
}