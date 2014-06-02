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

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.PhysConfPlugin;
import ect.equip.physconf.ui.ModelLabelProvider;
import ect.equip.physconf.ui.filters.TypeMapper;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class PhysicalFigure extends ResourceFigure
{
	private Image image;
	// private Point2D perPixelScale;
	// private Point2D scale;
	private Dimension size;
	private String text;

	public PhysicalFigure(final Resource resource)
	{
		super(resource);
		setBackgroundColor(ColorConstants.titleBackground);
		setForegroundColor(ColorConstants.titleForeground);
		final Label label = new Label();
		label.setBorder(new MarginBorder(2, 4, 2, 4));
		setToolTip(label);
		setLayoutManager(new ResourceLayout());
	}

	@Override
	public Dimension getPreferredSize(final int wHint, final int hHint)
	{
		if (size == null)
		{
			refresh();
		}
		return size;
	}

	// protected Point2D getPerPixelScale()
	// {
	// if (perPixelScale == null)
	// {
	// if (getParent() instanceof PhysicalFigure)
	// {
	// perPixelScale = ((PhysicalFigure) getParent()).getPerPixelScale();
	// return perPixelScale;
	// }
	//
	// if (getImage() != null)
	// {
	// double scaleWidth = 1;
	// if (resource.hasProperty(Schema.width))
	// {
	// final double width = resource.getProperty(Schema.width).getDouble();
	// scaleWidth = image.getBounds().width / width;
	// }
	//
	// double scaleHeight = 1;
	// if (resource.hasProperty(Schema.depth))
	// {
	// final double height = resource.getProperty(Schema.depth).getDouble();
	// scaleHeight = image.getBounds().height / height;
	// }
	//
	// perPixelScale = new Point2D.Double(scaleWidth, scaleHeight);
	// scale = new Point2D.Float(1, 1);
	// }
	// }
	// return perPixelScale;
	// }
	//
	// protected Point2D getScale()
	// {
	// if (perPixelScale == null)
	// {
	// getPerPixelScale();
	// }
	// if (scale == null)
	// {
	// double scaleWidth = 1;
	// double scaleHeight = 1;
	// if (getImage() != null)
	// {
	// if (getParent() instanceof PhysicalFigure)
	// {
	// final Point2D parentScale = ((PhysicalFigure) getParent()).getScale();
	// scaleWidth = parentScale.getX();
	// scaleHeight = parentScale.getY();
	// }
	//
	// if (resource.hasProperty(Schema.width))
	// {
	// final double width = resource.getProperty(Schema.width).getDouble();
	// scaleWidth = (perPixelScale.getX() * width) / image.getBounds().width;
	// }
	//
	// if (resource.hasProperty(Schema.depth))
	// {
	// final double height = resource.getProperty(Schema.depth).getDouble();
	// scaleHeight = (perPixelScale.getY() * height) / image.getBounds().height;
	// }
	// }
	// scale = new Point2D.Double(scaleWidth, scaleHeight);
	// }
	// return scale;
	// }

	public void refresh()
	{
		if (resource.hasProperty(Schema.image))
		{
			final Resource mediaFile = resource.getProperty(Schema.image).getResource();
			final Image newImage = PhysConfPlugin.getDefault().getImage(mediaFile);
			if (newImage == null || !newImage.equals(image))
			{
				image = newImage;
				if (image != null)
				{
					size = new Dimension(image.getBounds().width, image.getBounds().height);
					// final Point2D scale = getScale();
					// if (scale != null)
					// {
					// size.scale(scale.getX(), scale.getY());
					// }
					setBorder(null);
					setOpaque(false);
					repaint();
				}
			}
		}
		final String newText = ModelLabelProvider.text(resource);
		if (!newText.equals(text))
		{
			if (image == null)
			{
				final Font font = getFont();
				if (font != null)
				{
					text = newText;
					size = FigureUtilities.getTextExtents(text, font);
					// final Point2D scale = getScale();
					// if (scale != null)
					// {
					// size.scale(scale.getX(), scale.getY()).expand(10, 6);
					// }
					setBorder(new LineBorder(ColorConstants.darkGray, 1));
					setOpaque(true);
					((Label) getToolTip()).setText(newText);
					repaint();
				}
				final Image icon = ModelLabelProvider.image(resource);
				if (size != null)
				{
					size.expand(icon.getBounds().width + 3, 0);
					if (size.height < icon.getBounds().height)
					{
						size.height = icon.getBounds().height;
					}
				}
				else
				{
					size = new Dimension(icon.getBounds().width, icon.getBounds().height);
				}
				size.expand(10, 6);
			}
			else
			{
				text = newText;
				((Label) getToolTip()).setText(newText);
			}
		}
		final StmtIterator iter = resource.listProperties(Schema.hasProxy);
		while (iter.hasNext())
		{
			final Resource resource = iter.nextStatement().getResource();
			if (resource.hasProperty(RDF.type, Schema.SoftwareComponentProperty))
			{
				final Statement statement = resource.getProperty(RDF.value);
				if (statement != null)
				{
					final String value = statement.getString();
					((Label) getToolTip()).setText(text + System.getProperty("line.separator") + "Value: " + value);
					return;
				}
			}
		}
		((Label) getToolTip()).setText(text);
	}

	@Override
	public String toString()
	{
		return ModelLabelProvider.text(resource);
	}

	protected Image getImage()
	{
		if (image == null && resource.hasProperty(Schema.image))
		{
			refresh();
		}
		return image;
	}

	@Override
	protected void paintFigure(final Graphics graphics)
	{
		super.paintFigure(graphics);
		final Rectangle area = getClientArea();
		// final Point2D scale = getScale();
		// if (scale != null)
		// {
		// graphics.scale(scale.getX(), scale.getY());
		// area.scale(1 / scale.getX(), 1/scale.getY());
		// }
		if (getImage() != null)
		{
			graphics.setInterpolation(SWT.LOW);
			if (!resource.hasProperty(Schema.matchState))
			{
				graphics.drawImage(getImage(), area.x, area.y);
			}
			else
			{
				final Resource property = TypeMapper.getResourceOfType(resource, Schema.SoftwareComponentProperty);
				if (property != null
						&& property.hasProperty(RDF.value, resource.getProperty(Schema.matchState).getString()))
				{
					graphics.drawImage(getImage(), area.x, area.y);
				}
			}
		}
		else
		{
			int x = area.x + 4;
			final int y = area.y + 2;
			final Image icon = ModelLabelProvider.image(resource);
			if (icon != null)
			{
				graphics.setInterpolation(SWT.LOW);
				graphics.drawImage(icon, x, y);
				x = area.x + 7 + icon.getBounds().width;
			}

			graphics.drawText(text, x, y + 1);
		}
	}
}