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

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.DC_11;

import digitalrecord.wrapped.Schema;

import org.eclipse.jface.wizard.Wizard;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class SelectDestinationWizard extends Wizard
{
	protected SelectDestinationWizardPage page;
	final protected Resource association;

	/**
	 * @param model
	 * @param resource
	 */
	public SelectDestinationWizard(final Resource association)
	{
		super();
		this.association = association;
		setWindowTitle("Add Property Wizard");
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages()
	{
		final Resource link = association.getProperty(Schema.createLink).getResource();
		if (link.hasProperty(Schema.selectDestination, "true"))
		{
			if (link.hasProperty(Schema.reverseLink, "true"))
			{
				page = new SelectDestinationWizardPage(link, association.getProperty(Schema.source).getResource());
			}
			else
			{
				page = new SelectDestinationWizardPage(link, association.getProperty(Schema.destination).getResource());
			}
			addPage(page);
		}
	}

	@Override
	public boolean performCancel()
	{
		association.removeProperties();
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish()
	{
		final StmtIterator iterator = association.listProperties(Schema.createLink);
		while (iterator.hasNext())
		{
			final Statement statement = iterator.nextStatement();
			final Resource link = statement.getResource();
			if (link.hasProperty(Schema.selectDestination, "true"))
			{
				for (final Object object : page.propertyList.getCheckedElements())
				{
					link.addProperty(Schema.destination, ((Resource) object).getProperty(DC_11.title).getString());
				}
			}
		}
		return true;
	}
}