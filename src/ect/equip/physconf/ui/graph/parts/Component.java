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

import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDFS;

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.ModelUpdateListener;
import ect.equip.physconf.ui.ModelLabelProvider;
import ect.equip.physconf.ui.commands.DeleteComponentCommand;
import ect.equip.physconf.ui.graph.figures.ComponentFigure;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class Component extends ResourcePart
{
	/**
	 * @return the Content pane for adding or removing child figures
	 */
	@Override
	public IFigure getContentPane()
	{
		final ComponentFigure figure = (ComponentFigure) getFigure();
		return figure.getPropertiesFigure();
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
				return new DeleteComponentCommand(getResource());
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
		return new ComponentFigure(getResource());
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
					if (Schema.softwareComponent.equals(s.getPredicate()))
					{
						if (getParent() != null) { return true; }
					}
				}
				else if (s.getSubject().equals(getResource())) { return RDFS.label.equals(s.getPredicate())
						|| Schema.xOffset.equals(s.getPredicate()) || Schema.yOffset.equals(s.getPredicate()); }
				return false;
			}

			@Override
			public void update()
			{
				if (getFigure().getParent() != null)
				{
					refresh();
					getFigure().getParent().getLayoutManager().invalidate();
				}
			}
		};
	}

	@Override
	protected List<Resource> getModelChildren()
	{
		final Resource component = getResource();
		final ArrayList<Resource> list = new ArrayList<Resource>();
		final ResIterator iterator = component.getModel().listSubjectsWithProperty(Schema.softwareComponent, component);
		while (iterator.hasNext())
		{
			final Resource resource = iterator.nextResource();
			if (!resource.hasProperty(Schema.isVisible, false))
			{
				list.add(resource);
			}

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
		final ComponentFigure componentFigure = (ComponentFigure) getFigure();
		final Point location = componentFigure.getLocation();
		final ModelPart parent = (ModelPart) getParent();
		final Rectangle constraint = new Rectangle(location.x, location.y, -1, -1);
		if (parent != null)
		{
			parent.setLayoutConstraint(this, componentFigure, constraint);
		}
		componentFigure.setName(ModelLabelProvider.text(getModel()));
	}
}