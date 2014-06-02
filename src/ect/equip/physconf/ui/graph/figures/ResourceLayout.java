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
package ect.equip.physconf.ui.graph.figures;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;

import digitalrecord.wrapped.Schema;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class ResourceLayout extends XYLayout
{
	private static final int SEPARATION = 5;

	@Override
	public Object getConstraint(final IFigure figure)
	{
		if (figure instanceof ResourceFigure)
		{
			final Resource resource = ((ResourceFigure) figure).getResource();
			final Dimension dim = figure.getPreferredSize();
			try
			{
				final int x = resource.getProperty(Schema.xOffset).getInt();
				final int y = resource.getProperty(Schema.yOffset).getInt();
				return new Rectangle(x, y, dim.width, dim.height);
			}
			catch (final Exception e)
			{
				// Create initial position
				final Rectangle rect = new Rectangle(0, 0, dim.width, dim.height);
				boolean loop = true;
				while (loop)
				{
					loop = false;
					for (final IFigure sibling : getChildren(figure.getParent()))
					{
						if (!hasConstraint(sibling))
						{
							continue;
						}
						final Rectangle siblingRect = (Rectangle) getConstraint(sibling);
						if (siblingRect != null && siblingRect.intersects(rect))
						{
							rect.x = siblingRect.x + siblingRect.width + SEPARATION;
							loop = true;
						}
					}
				}
				resource.addProperty(Schema.xOffset, rect.x);
				resource.addProperty(Schema.yOffset, rect.y);
				return rect;
			}
		}
		return null;
	}

	@Override
	public Point getOrigin(final IFigure parent)
	{
		return new Point(0, 0);
	}

	public boolean hasConstraint(final IFigure figure)
	{
		if (figure instanceof ResourceFigure)
		{
			final Resource resource = ((ResourceFigure) figure).getResource();
			return resource.hasProperty(Schema.xOffset) && resource.hasProperty(Schema.yOffset);
		}
		return false;
	}

	@Override
	public void layout(final IFigure container)
	{
		final Point offset = getOrigin(container);
		for (final IFigure figure : getChildren(container))
		{
			Rectangle bounds = (Rectangle) getConstraint(figure);
			if (bounds == null)
			{
				continue;
			}

			if (bounds.width == -1 || bounds.height == -1)
			{
				final Dimension preferredSize = figure.getPreferredSize(bounds.width, bounds.height);
				bounds = bounds.getCopy();
				if (bounds.width == -1)
				{
					bounds.width = preferredSize.width;
				}
				if (bounds.height == -1)
				{
					bounds.height = preferredSize.height;
				}
			}
			bounds = bounds.getTranslated(offset);
			figure.setBounds(bounds);
		}
	}

	@SuppressWarnings("unchecked")
	private List<IFigure> getChildren(final IFigure container)
	{
		return container.getChildren();
	}

	@Override
	protected Dimension calculatePreferredSize(final IFigure figure, final int wHint, final int hHint)
	{
		final Rectangle rect = new Rectangle();
		for (final IFigure child : getChildren(figure))
		{
			Rectangle constraint = (Rectangle) getConstraint(child);
			if (constraint == null)
			{
				continue;
			}

			if (constraint.width == -1 || constraint.height == -1)
			{
				final Dimension preferredSize = child.getPreferredSize(constraint.width, constraint.height);
				constraint = constraint.getCopy();
				if (constraint.width == -1)
				{
					constraint.width = preferredSize.width;
				}
				if (constraint.height == -1)
				{
					constraint.height = preferredSize.height;
				}
			}
			rect.union(constraint);
		}
		return rect.expand(figure.getInsets()).getSize();
	}
}