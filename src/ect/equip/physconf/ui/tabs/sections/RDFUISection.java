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
package ect.equip.physconf.ui.tabs.sections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DC_11;
import com.hp.hpl.jena.vocabulary.RDF;

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.ECTEvent;
import ect.equip.physconf.ui.ModelLabelProvider;
import ect.equip.physconf.ui.PhysicalEditDomain;
import ect.equip.physconf.ui.commands.StatementListCommand;
import ect.equip.physconf.ui.commands.StatementQueueCommand;
import ect.equip.physconf.ui.filters.TypeMapper;

import org.eclipse.jface.viewers.IFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

public class RDFUISection extends ResourceSection
{
	public static final class UIFilter implements IFilter
	{
		public boolean select(final Object toTest)
		{
			final Object modelObject = TypeMapper.getModelObject(toTest);
			if (!(modelObject instanceof Resource)) { return false; }
			final Resource resource = (Resource) modelObject;
			return resource.hasProperty(Schema.ui);
		}
	}

	private Composite composite;
	private Resource uiResource;

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage tabbedPropertySheetPage)
	{
		super.createControls(parent, tabbedPropertySheetPage);
		composite = getWidgetFactory().createFlatFormComposite(parent);
	}

	@Override
	protected ModelChangedListener createListener()
	{
		return null;
	}

	protected Statement getPropertyValue(final Resource control)
	{
		final Model model = resource.getModel();
		final String propertyTitle = control.getProperty(DC_11.title).getString();
		StmtIterator iterator = resource.listProperties(Schema.hasProxy);
		while (iterator.hasNext())
		{
			final Resource component = iterator.nextStatement().getResource();
			final ResIterator propertyIterator = model.listSubjectsWithProperty(Schema.softwareComponent, component);
			while (propertyIterator.hasNext())
			{
				final Resource property = propertyIterator.nextResource();
				if (property.hasProperty(DC_11.title, propertyTitle) && property.hasProperty(RDF.value)) { return property.getProperty(RDF.value); }
			}
		}

		iterator = resource.listProperties(Schema.proxyComponent);
		while (iterator.hasNext())
		{
			final Resource componentDetails = iterator.nextStatement().getResource();
			final StmtIterator propertyIterator = componentDetails.listProperties(Schema.setProperty);
			while (propertyIterator.hasNext())
			{
				final Resource property = propertyIterator.nextStatement().getResource();
				if (property.hasProperty(DC_11.title, propertyTitle) && property.hasProperty(RDF.value)) { return property.getProperty(RDF.value); }
			}
		}

		iterator = resource.listProperties(Schema.createComponent);
		while (iterator.hasNext())
		{
			final Resource componentDetails = iterator.nextStatement().getResource();
			if (componentDetails.hasProperty(Schema.proxyComponent, true))
			{
				final StmtIterator propertyIterator = componentDetails.listProperties(Schema.setProperty);
				while (propertyIterator.hasNext())
				{
					final Resource property = propertyIterator.nextStatement().getResource();
					if (property.hasProperty(DC_11.title, propertyTitle) && property.hasProperty(RDF.value)) { return property.getProperty(RDF.value); }
				}
			}
		}

		iterator = resource.listProperties(Schema.plugsInto);
		while (iterator.hasNext())
		{
			final Resource componentDetails = iterator.nextStatement().getResource();
			final StmtIterator propertyIterator = componentDetails.listProperties(Schema.setProperty);
			while (propertyIterator.hasNext())
			{
				final Resource property = propertyIterator.nextStatement().getResource();
				if (property.hasProperty(DC_11.title, propertyTitle) && property.hasProperty(RDF.value)) { return property.getProperty(RDF.value); }
			}
		}

		return null;
	}

	protected void setPropertyValue(final Resource control, final Object value)
	{
		final StatementQueueCommand commandQueue = new StatementQueueCommand(resource.getModel());

		final StmtIterator proxyIterator = resource.listProperties(Schema.proxyComponent);
		while (proxyIterator.hasNext())
		{
			final Resource proxyDetails = proxyIterator.nextStatement().getResource();
			final StmtIterator iterator = proxyDetails.listProperties(Schema.setProperty);
			while (iterator.hasNext())
			{

				final Resource propertyDetails = iterator.nextStatement().getResource();
				if (propertyDetails.getProperty(DC_11.title).getString().equals(
						control.getProperty(DC_11.title).getString()))
				{
					final StatementListCommand command = commandQueue.getEventList(ECTEvent.Type.UPDATED);
					command.add(propertyDetails, RDF.value, value);
					PhysicalEditDomain.INSTANCE.getCommandStack().execute(command);
					return;
				}
			}

			final StatementListCommand command = commandQueue.getEventList(ECTEvent.Type.ADDED);
			final Resource propertyDetails = resource.getModel().createResource();
			command.add(propertyDetails, RDF.type, Schema.PropertyDetails);
			command.add(propertyDetails, DC_11.title, control.getProperty(DC_11.title).getString());
			command.add(propertyDetails, RDF.value, value);
			command.add(proxyDetails, Schema.setProperty, propertyDetails);
		}

		final StmtIterator createIterator = resource.listProperties(Schema.createComponent);
		while (createIterator.hasNext())
		{
			final Resource createDetails = createIterator.nextStatement().getResource();
			if (createDetails.hasProperty(Schema.proxyComponent, true))
			{
				final StmtIterator iterator = createDetails.listProperties(Schema.setProperty);
				while (iterator.hasNext())
				{

					final Resource propertyDetails = iterator.nextStatement().getResource();
					if (propertyDetails.getProperty(DC_11.title).getString().equals(
							control.getProperty(DC_11.title).getString()))
					{
						final StatementListCommand command = commandQueue.getEventList(ECTEvent.Type.UPDATED);
						command.add(propertyDetails, RDF.value, value);
						PhysicalEditDomain.INSTANCE.getCommandStack().execute(command);
						return;
					}
				}

				final StatementListCommand command = commandQueue.getEventList(ECTEvent.Type.ADDED);
				final Resource propertyDetails = resource.getModel().createResource();
				command.add(propertyDetails, RDF.type, Schema.PropertyDetails);
				command.add(propertyDetails, DC_11.title, control.getProperty(DC_11.title).getString());
				command.add(propertyDetails, RDF.value, value);
				command.add(createDetails, Schema.setProperty, propertyDetails);
			}
		}

		final StmtIterator plugsIntoIterator = resource.listProperties(Schema.plugsInto);
		while (plugsIntoIterator.hasNext())
		{
			final Resource createDetails = plugsIntoIterator.nextStatement().getResource();
			final StmtIterator iterator = createDetails.listProperties(Schema.setProperty);
			while (iterator.hasNext())
			{

				final Resource propertyDetails = iterator.nextStatement().getResource();
				if (propertyDetails.getProperty(DC_11.title).getString().equals(
						control.getProperty(DC_11.title).getString()))
				{
					final StatementListCommand command = commandQueue.getEventList(ECTEvent.Type.UPDATED);
					command.add(propertyDetails, RDF.value, value);
					PhysicalEditDomain.INSTANCE.getCommandStack().execute(command);
					return;
				}
			}
		}

		PhysicalEditDomain.INSTANCE.getCommandStack().execute(commandQueue);
	}

	// private Collection<Resource> getComponentDetails(Resource control)
	// {
	// Collection<Resource> componentDetails = new ArrayList<Resource>();
	// if(control.hasProperty(Schema.component))
	// {
	//			
	// }
	//		
	// return null;
	// }

	@Override
	protected void setResource(final Resource resource)
	{
		super.setResource(resource);

		try
		{
			final Statement statement = resource.getProperty(Schema.ui);
			final Resource uiResource = statement != null ? statement.getResource() : null;
			if (this.uiResource == uiResource) { return; }

			this.uiResource = uiResource;
			for (final Control child : composite.getChildren())
			{
				child.dispose();
			}

			if (uiResource == null) { return; }

			final List<Resource> controls = new ArrayList<Resource>();
			final StmtIterator iterator = uiResource.listProperties(Schema.control);
			while (iterator.hasNext())
			{
				final Resource control = iterator.nextStatement().getResource();
				// TODO Check that property exists
				if (control.hasProperty(RDF.type, Schema.Control))
				{
					controls.add(control);
				}
			}

			Collections.sort(controls, new Comparator<Resource>()
			{
				public int compare(final Resource o1, final Resource o2)
				{
					final String label1 = ModelLabelProvider.text(o1);
					final String label2 = ModelLabelProvider.text(o2);
					return label1.compareTo(label2);
				}
			});

			Control lastControl = null;
			for (final Resource control : controls)
			{
				final CLabel label = getWidgetFactory().createCLabel(composite, ModelLabelProvider.text(control) + ":");

				FormData data = new FormData();
				data.left = new FormAttachment(0, ITabbedPropertyConstants.HSPACE);
				if (lastControl == null)
				{
					data.top = new FormAttachment(0, 0);
				}
				else
				{
					data.top = new FormAttachment(lastControl, 0);
				}
				label.setLayoutData(data);
				final FormAttachment top = data.top;

				if (control.hasProperty(RDF.type, Schema.Slider))
				{
					final Spinner spinner = new Spinner(composite, SWT.BORDER);
					getWidgetFactory().adapt(spinner, true, true);

					final String maxString = control.getProperty(Schema.rangeMax).getString();
					final String minString = control.getProperty(Schema.rangeMin).getString();
					int digits = 0;
					if (control.hasProperty(Schema.decimalPlaces))
					{
						digits = control.getProperty(Schema.decimalPlaces).getInt();
						spinner.setDigits(digits);
					}

					spinner.setMinimum((int) (Float.parseFloat(minString) * Math.pow(10, digits)));
					spinner.setMaximum((int) (Float.parseFloat(maxString) * Math.pow(10, digits)));
					final Statement valueStatement = getPropertyValue(control);
					if (valueStatement != null)
					{
						int value = (int) (valueStatement.getFloat() * Math.pow(10, digits));
						if (control.hasProperty(Schema.scale))
						{
							final int scale = control.getProperty(Schema.scale).getInt();
							value = value * scale;
						}
						spinner.setSelection(value);
					}
					else if (control.hasProperty(Schema.origin))
					{
						final int origin = (int) (control.getProperty(Schema.origin).getFloat() * Math.pow(10, digits));
						spinner.setSelection(origin);
					}
					data = new FormData();
					data.left = new FormAttachment(0, 2 * STANDARD_LABEL_WIDTH);
					data.top = top;
					spinner.setLayoutData(data);
					spinner.pack();
					spinner.addModifyListener(new ModifyListener()
					{
						public void modifyText(final ModifyEvent e)
						{
							Object value = spinner.getSelection();
							if (control.hasProperty(Schema.scale))
							{
								final int scale = control.getProperty(Schema.scale).getInt();
								value = (float) spinner.getSelection() / scale;
							}

							if (spinner.getDigits() != 0)
							{
								value = spinner.getSelection() * (1 / Math.pow(10, spinner.getDigits()));
							}

							setPropertyValue(control, value);
						}
					});
					lastControl = spinner;
				}
				else if (control.hasProperty(RDF.type, Schema.FileSelect))
				{
					final Statement valueStatement = getPropertyValue(control);
					String value = "";
					if (valueStatement != null)
					{
						value = valueStatement.getString();
					}
					final Text text = getWidgetFactory().createText(composite, value);

					final Button button = getWidgetFactory().createButton(composite, "Browse...", SWT.PUSH);
					data = new FormData();
					data.right = new FormAttachment(100, 0);
					data.top = top;
					button.setLayoutData(data);
					button.addSelectionListener(new SelectionAdapter()
					{
						@Override
						public void widgetDefaultSelected(final SelectionEvent e)
						{
						}

						@Override
						public void widgetSelected(final SelectionEvent e)
						{
							final FileDialog dialog = new FileDialog(getPart().getSite().getShell(), SWT.OPEN);
							final String result = dialog.open();
							if (result != null)
							{
								setPropertyValue(control, result);
								text.setText(result);
							}
						}
					});

					data = new FormData();
					data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
					data.right = new FormAttachment(button, 0);
					data.top = top;
					text.setLayoutData(data);
					text.addSelectionListener(new SelectionAdapter()
					{
						@Override
						public void widgetDefaultSelected(final SelectionEvent e)
						{
							setPropertyValue(control, text.getText());
						}
					});

					lastControl = button;
				}
			}

			composite.layout(composite.getChildren());
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
}