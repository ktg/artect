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

import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.ECTEvent;
import ect.equip.physconf.ModelUpdateListener;
import ect.equip.physconf.PhysConfPlugin;
import ect.equip.physconf.ui.ModelLabelProvider;
import ect.equip.physconf.ui.PhysicalEditDomain;
import ect.equip.physconf.ui.commands.DeleteThingCommand;

import org.eclipse.gef.dnd.TemplateTransfer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class PhysicalThingSection extends ResourceSection
{
	private Label labelImage;
	private CLabel labelType;
	private Button deleteButton;

	private final ModifyListener listener = new ModifyListener()
	{
		public void modifyText(final ModifyEvent arg0)
		{
			final Statement statement = resource.getProperty(RDFS.label);
			if (!statement.getObject().equals(textName.getText()))
			{
				statement.changeObject(textName.getText());
			}
		}
	};

	// private Label labelPlugs; // List?
	// private Label labelComment;
	private Text textName;

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage aTabbedPropertySheetPage)
	{
		super.createControls(parent, aTabbedPropertySheetPage);
		final Composite composite = getWidgetFactory().createFlatFormComposite(parent);
		FormData data;

		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		labelImage = getWidgetFactory().createLabel(composite, "");
		labelImage.setLayoutData(data);
		final DragSource dragSource = new DragSource(labelImage, DND.DROP_COPY);
		dragSource.setTransfer(new Transfer[] { TemplateTransfer.getInstance() });
		dragSource.addDragListener(new DragSourceAdapter()
		{
			@Override
			public void dragSetData(final DragSourceEvent event)
			{
				if (TemplateTransfer.getInstance().isSupportedType(event.dataType))
				{
					event.data = resource;
				}
			}

			@Override
			public void dragStart(final DragSourceEvent event)
			{
				TemplateTransfer.getInstance().setTemplate(resource);
				if (PhysConfPlugin.getDefault().getModel().containsResource(resource))
				{
					event.doit = false;
				}
				else if (resource.hasProperty(Schema.image))
				{
					event.image = PhysConfPlugin.getDefault().getImage(resource.getProperty(Schema.image).getResource());
					event.image = new Image(event.image.getDevice(), event.image.getImageData());
				}
			}
		});

		data = new FormData();
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		data.left = new FormAttachment(labelImage, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		textName = getWidgetFactory().createText(composite, "");
		textName.setLayoutData(data);

		data = new FormData();
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(textName, ITabbedPropertyConstants.VSPACE);
		deleteButton = getWidgetFactory().createButton(composite, "Delete", SWT.FLAT | SWT.PUSH);
		final ISharedImages sharedImages = aTabbedPropertySheetPage.getSite().getWorkbenchWindow().getWorkbench().getSharedImages();
		deleteButton.setImage(sharedImages.getImage(ISharedImages.IMG_TOOL_DELETE));
		deleteButton.setLayoutData(data);
		deleteButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(final SelectionEvent e)
			{
				final DeleteThingCommand command = new DeleteThingCommand(resource.getModel(), ECTEvent.Type.REMOVED);
				command.deleteResource(resource);
				PhysicalEditDomain.INSTANCE.getCommandStack().execute(command);
				getPart().setFocus();
			}
		});

		data = new FormData();
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		data.left = new FormAttachment(labelImage, ITabbedPropertyConstants.HSPACE);
		final CLabel labelLabel = getWidgetFactory().createCLabel(composite, "Name:");
		labelLabel.setLayoutData(data);

		data = new FormData();
		data.top = new FormAttachment(textName, ITabbedPropertyConstants.VSPACE);
		data.left = new FormAttachment(labelImage, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(deleteButton, 0);
		labelType = getWidgetFactory().createCLabel(composite, "");
		labelType.setLayoutData(data);
		labelType.setImage(PhysConfPlugin.getImage("class"));

		data = new FormData();
		data.left = new FormAttachment(labelImage, ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(textName, ITabbedPropertyConstants.VSPACE);
		final CLabel labelTypeLabel = getWidgetFactory().createCLabel(composite, "Type:");
		labelTypeLabel.setLayoutData(data);

	}

	@Override
	public void refresh()
	{
		if (textName.isDisposed()) { return; }
		if (resource == null) { return; }
		Statement statement = resource.getProperty(Schema.image);
		if (statement != null)
		{
			labelImage.setImage(PhysConfPlugin.getDefault().getImage(statement.getResource()));
		}
		else
		{
			labelImage.setImage(null);
		}
		labelImage.pack(true);
		labelImage.getParent().layout();

		final String text = ModelLabelProvider.text(resource);
		if (!textName.getText().equals(text))
		{
			textName.removeModifyListener(listener);
			textName.setText(text);
			textName.addModifyListener(listener);
		}
		statement = resource.getProperty(RDF.type);
		if (statement != null)
		{
			labelType.setText(ModelLabelProvider.text(statement.getObject()));
		}

		deleteButton.setVisible(resource.getModel().equals(PhysConfPlugin.getDefault().getModel()));
		deleteButton.setText("Delete " + textName.getText());
	}

	@Override
	protected ModelChangedListener createListener()
	{
		return new ModelUpdateListener()
		{
			@Override
			public boolean shouldUpdate(final Statement s)
			{
				if (s.getSubject().equals(resource)) { return true; }
				return false;
			}

			@Override
			public void update()
			{
				refresh();
			}
		};
	}
}