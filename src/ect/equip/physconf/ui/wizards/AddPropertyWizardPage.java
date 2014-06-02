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
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class AddPropertyWizardPage extends WizardPage
{
	protected Resource resource;
	private TableViewer propertyList;

	/**
	 * @param pageName
	 */
	protected AddPropertyWizardPage(final Resource resource)
	{
		super("AddProperty");
		this.resource = resource;
		setTitle("Add Property");
		setMessage("Choose a property to add");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	public void createControl(final Composite parent)
	{
		setPageComplete(false);
		propertyList = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		propertyList.setContentProvider(new ModelContentProvider()
		{
			public Object[] getElements(final Object inputElement)
			{
				final NodeIterator nodes = model.listObjectsOfProperty(resource, RDF.type);
				final ArrayList<Resource> list = new ArrayList<Resource>();
				while (nodes.hasNext())
				{
					final RDFNode node = nodes.nextNode();
					final ResIterator iterator = model.listSubjectsWithProperty(RDFS.domain, node);
					while (iterator.hasNext())
					{
						list.add(iterator.nextResource());
					}
				}
				return list.toArray();
			}
		});
		setControl(propertyList.getControl());
		propertyList.setInput(resource);
		propertyList.addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(final SelectionChangedEvent event)
			{
				if (event.getSelection().isEmpty())
				{
					setPageComplete(false);
				}
				else
				{
					final Resource selection = (Resource) ((IStructuredSelection) event.getSelection()).getFirstElement();
					((AddPropertyWizard) getWizard()).property = selection;
					setPageComplete(true);
				}
			}
		});
		propertyList.setLabelProvider(new ModelLabelProvider());
	}
}