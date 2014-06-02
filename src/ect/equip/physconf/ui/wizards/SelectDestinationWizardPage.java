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
import java.util.regex.Pattern;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.PhysConfPlugin;
import ect.equip.physconf.ui.ModelContentProvider;
import ect.equip.physconf.ui.ModelLabelProvider;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class SelectDestinationWizardPage extends WizardPage
{
	private class PatternFilter extends ViewerFilter
	{
		private final ArrayList<Pattern> patterns = new ArrayList<Pattern>();

		public void add(final Pattern pattern)
		{
			patterns.add(pattern);
		}

		public boolean isEmpty()
		{
			return patterns.isEmpty();
		}

		@Override
		public boolean select(final Viewer viewer, final Object parentElement, final Object element)
		{
			final String name = ModelLabelProvider.text(element);
			for (final Pattern pattern : patterns)
			{
				if (pattern.matcher(name).matches()) { return false; }
			}
			return true;
		}
	}

	protected final Resource association;
	protected final Resource destination;

	protected CheckboxTableViewer propertyList;

	/**
	 * @param pageName
	 */
	protected SelectDestinationWizardPage(final Resource association, final Resource destination)
	{
		super("AddProperty");
		this.destination = destination;
		this.association = association;
		setTitle("Select Properties");
		setMessage("Select Properties to connect to");
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

		propertyList = CheckboxTableViewer.newCheckList(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		propertyList.setSorter(new ViewerSorter());
		propertyList.setContentProvider(new ModelContentProvider()
		{
			@Override
			public void addedStatement(final Statement s)
			{
				// TODO Update on right events
				super.addedStatement(s);
			}

			public Object[] getElements(final Object inputElement)
			{
				try
				{
					final Resource destinationComponent = destination.getProperty(Schema.hasProxy).getResource();
					final Model model = PhysConfPlugin.getDefault().getModel();
					final ResIterator iterator = model.listSubjectsWithProperty(Schema.softwareComponent,
							destinationComponent);
					final ArrayList<Resource> items = new ArrayList<Resource>();
					while (iterator.hasNext())
					{
						final Resource property = iterator.nextResource();
						if (!property.hasProperty(Schema.readOnly, "true")
								&& !model.contains(null, Schema.destination, property))
						{
							items.add(property);
						}
					}
					return items.toArray(new Resource[0]);
				}
				catch (final Exception e)
				{
					return new Object[0];
				}
			}

			@Override
			public void removedStatement(final Statement s)
			{
				// TODO Update on right events
				super.removedStatement(s);
			}
		});
		setControl(propertyList.getControl());
		propertyList.setInput(destination);
		propertyList.addCheckStateListener(new ICheckStateListener()
		{
			public void checkStateChanged(final CheckStateChangedEvent event)
			{
				setPageComplete(propertyList.getCheckedElements().length != 0);
			}
		});
		propertyList.setLabelProvider(new ModelLabelProvider());
		final StmtIterator iterator = association.listProperties(Schema.hide);
		final PatternFilter patternFilter = new PatternFilter();
		while (iterator.hasNext())
		{
			final Statement statement = iterator.nextStatement();
			patternFilter.add(Pattern.compile(statement.getString()));
		}
		if (!patternFilter.isEmpty())
		{
			propertyList.addFilter(patternFilter);
		}
	}
}