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
package ect.equip.physconf.ui.wizards;

import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import ect.equip.physconf.ui.ModelContentProvider;
import ect.equip.physconf.ui.ModelLabelProvider;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class AddPropertyValueWizardPage extends WizardPage
{
	protected Resource resource;
	private TableViewer valueList;

	/**
	 * @param model
	 * @param resource
	 */
	public AddPropertyValueWizardPage(final Resource resource)
	{
		super("PropertyValue");
		this.resource = resource;
		setTitle("Select Property Values");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	public void createControl(final Composite parent)
	{
		valueList = new TableViewer(parent);
		setControl(valueList.getControl());
		valueList.setContentProvider(new ModelContentProvider()
		{
			private Property property;

			public Object[] getElements(final Object inputElement)
			{
				final NodeIterator ranges = model.listObjectsOfProperty(property, RDFS.range);
				final ArrayList<Resource> list = new ArrayList<Resource>();
				while (ranges.hasNext())
				{
					final RDFNode range = ranges.nextNode();
					System.err.println(range);
					final ResIterator resources = model.listSubjectsWithProperty(RDF.type, range);
					while (resources.hasNext())
					{
						final Resource next = resources.nextResource();
						if (!property.getModel().contains(resource, property, next))
						{
							list.add(next);
						}
					}
				}
				return list.toArray();
			}

			@Override
			public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
			{
				super.inputChanged(viewer, oldInput, newInput);
				if (newInput instanceof Property)
				{
					property = (Property) newInput;
				}
				else if (newInput instanceof Resource)
				{
					final Resource resource = (Resource) newInput;
					property = resource.getModel().getProperty(resource.getURI());
				}
				else
				{
					property = null;
				}
			}
		});
		valueList.addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(final SelectionChangedEvent event)
			{
				if (event.getSelection().isEmpty())
				{
					setPageComplete(false);
				}
				else
				{
					((AddPropertyWizard) getWizard()).values = ((IStructuredSelection) event.getSelection()).toArray();
					setPageComplete(true);
				}
			}
		});
		valueList.setLabelProvider(new ModelLabelProvider());
	}

	@Override
	public void setVisible(final boolean visible)
	{
		super.setVisible(visible);
		final Resource property = ((AddPropertyWizard) getWizard()).property;
		final NodeIterator iterator = property.getModel().listObjectsOfProperty(property, RDFS.comment);
		while (iterator.hasNext())
		{
			System.err.println(iterator.next());
		}
		setMessage("Choose values for the property " + ModelLabelProvider.text(property));
		valueList.setInput(property);
	}
}