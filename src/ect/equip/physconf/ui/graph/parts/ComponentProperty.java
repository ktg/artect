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
import java.util.List;

import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.ModelUpdateListener;
import ect.equip.physconf.ui.commands.CreateComponentPropertyLinkCommand;
import ect.equip.physconf.ui.graph.figures.PropertyFigure;
import ect.equip.physconf.ui.graph.policies.ComponentNodeEditPolicy;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class ComponentProperty extends ResourcePart implements NodeEditPart
{
	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef
	 * .ConnectionEditPart)
	 */
	public ConnectionAnchor getSourceConnectionAnchor(final ConnectionEditPart connection)
	{
		return getRightAnchor();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef
	 * .Request)
	 */
	public ConnectionAnchor getSourceConnectionAnchor(final Request request)
	{
		return getRightAnchor();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef
	 * .ConnectionEditPart)
	 */
	public ConnectionAnchor getTargetConnectionAnchor(final ConnectionEditPart connection)
	{
		return getLeftAnchor();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef
	 * .Request)
	 */
	public ConnectionAnchor getTargetConnectionAnchor(final Request request)
	{
		return getLeftAnchor();
	}

	@Override
	public void refresh()
	{
		super.refresh();
		final PropertyFigure figure = (PropertyFigure) getFigure();
		if (figure != null)
		{
			figure.refresh();
		}
	}

	/**
	 * Sets the width of the line when selected
	 */
	@Override
	public void setSelected(final int value)
	{
		super.setSelected(value);
		final PropertyFigure figure = (PropertyFigure) getFigure();
		if (value != EditPart.SELECTED_NONE)
		{
			figure.setBackgroundColor(ColorConstants.menuBackgroundSelected);
		}
		else
		{
			figure.setBackgroundColor(ColorConstants.listBackground);
		}
		figure.repaint();
	}

	private ConnectionAnchor getLeftAnchor()
	{
		if (getResource().hasProperty(Schema.readOnly, true))
		{
			return null;
		}
		else
		{
			return new AbstractConnectionAnchor(getFigure())
			{
				public Point getLocation(final Point reference)
				{
					final Rectangle r = getOwner().getBounds().getCopy();
					getOwner().translateToAbsolute(r);
					final int off = r.height / 2;
					if (r.contains(reference) || r.x < reference.x) { return r.getTopLeft().translate(0, off); }
					return r.getTopLeft().translate(0, off);
				}
			};
		}
	}

	private ConnectionAnchor getRightAnchor()
	{
		return new AbstractConnectionAnchor(getFigure())
		{
			public Point getLocation(final Point reference)
			{
				final Rectangle r = getOwner().getBounds().getCopy();
				getOwner().translateToAbsolute(r);
				final int off = r.height / 2;
				if (r.contains(reference) || r.right() > reference.x) { return r.getTopRight().translate(0, off); }
				return r.getTopRight().translate(0, off);
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies()
	{
		installEditPolicy(EditPolicy.LAYOUT_ROLE, null);
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new ComponentNodeEditPolicy(
				CreateComponentPropertyLinkCommand.class));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure()
	{
		final PropertyFigure propertyFigure = new PropertyFigure(getResource());
		return propertyFigure;
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
				if (s.getObject().equals(getResource()))
				{
					if (Schema.source.equals(s.getPredicate()))
					{
						return true;
					}
					else if (Schema.destination.equals(s.getPredicate())) { return true; }
				}
				else if (s.getSubject().equals(getResource()))
				{
					if (RDFS.label.equals(s.getPredicate()))
					{
						return true;
					}
					else if (RDF.value.equals(s.getPredicate())) { return true; }
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
	protected List<Resource> getModelSourceConnections()
	{
		final Resource property = getResource();
		final List<Resource> list = new ArrayList<Resource>();
		final ResIterator iterator = property.getModel().listSubjectsWithProperty(Schema.source, property);
		while (iterator.hasNext())
		{
			list.add(iterator.nextResource());
		}
		return list;
	}

	@Override
	protected List<Resource> getModelTargetConnections()
	{
		final Resource property = getResource();
		final List<Resource> list = new ArrayList<Resource>();
		final ResIterator iterator = property.getModel().listSubjectsWithProperty(Schema.destination, property);
		while (iterator.hasNext())
		{
			list.add(iterator.nextResource());
		}
		return list;
	}
}