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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.PhysConfPlugin;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

public class ExportPhysicalRDFWizard extends Wizard implements IExportWizard
{
	ExportPhysicalRDFWizardPage mainPage;

	public ExportPhysicalRDFWizard()
	{
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#addPages()
	 */
	@Override
	public void addPages()
	{
		super.addPages();
		addPage(mainPage);
	}

	public void init(final IWorkbench workbench, final IStructuredSelection selection)
	{
		setWindowTitle("File Export Wizard"); // NON-NLS-1
		setNeedsProgressMonitor(true);
		mainPage = new ExportPhysicalRDFWizardPage("Export RDF"); // NON-NLS-1
	}

	@Override
	public boolean performFinish()
	{
		final File file = mainPage.getFile();
		try
		{
			if (!file.exists())
			{
				file.createNewFile();
			}
			final Object[] resources = mainPage.getResources();
			final Model originalModel = ((InfModel) PhysConfPlugin.getDefault().getModel()).getRawModel();
			final Model model = ModelFactory.createDefaultModel();
			final ResIterator resourceIterator = originalModel.listSubjectsWithProperty(RDF.type, Schema.Association);
			while (resourceIterator.hasNext())
			{
				addResource(model, resourceIterator.nextResource());
			}
			model.setNsPrefix("ect", "http://www.ncess.ac.uk/nodes/digitalrecord/digitalrecord.owl#");
			for (final Object element : resources)
			{
				if (element instanceof Resource)
				{
					addResource(model, (Resource) element);
				}
			}

			model.write(new FileWriter(file), "RDF/XML-ABBREV");
			return true;
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	private void addResource(final Model model, final Resource resource)
	{
		final Model originalModel = ((InfModel) PhysConfPlugin.getDefault().getModel()).getRawModel();
		final StmtIterator iterator = originalModel.listStatements(resource, null, (RDFNode) null);
		while (iterator.hasNext())
		{
			final Statement statement = iterator.nextStatement();
			if (!statement.getPredicate().equals(Schema.createdComponentRequest) && !model.contains(statement))
			{
				model.add(statement);
				System.out.println("ExportPhysicalRDFWizard.addResource(): " + statement);
				if (statement.getObject() instanceof Resource)
				{
					addResource(model, statement.getResource());
				}
			}
		}
	}
}