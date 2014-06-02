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
package ect.equip.physconf.ui.commands;

import java.io.File;
import java.io.IOException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.ECTEvent;
import ect.equip.physconf.PhysConfPlugin;
import ect.equip.physconf.ui.PhysicalEditDomain;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class LoadAction implements IWorkbenchWindowActionDelegate
{
	private IWorkbenchWindow window;

	public LoadAction()
	{
	}

	public void dispose()
	{
	}

	public void init(final IWorkbenchWindow window)
	{
		this.window = window;
	}

	public void run(final IAction action)
	{
		final FileDialog dialog = new FileDialog(window.getShell(), SWT.OPEN);
		dialog.setText("Load ARTECT Configuration");
		dialog.setFilterExtensions(new String[] { "*.*", "*.rdf;*.owl" });
		final String filename = dialog.open();
		if (filename != null)
		{
			final File file = new File(filename);
			if (file.exists())
			{
				final String path = file.getParent();
				PhysConfPlugin.getDefault().addPath(path);
				final Model model = ModelFactory.createDefaultModel();
				final Model defaultModel = PhysConfPlugin.getDefault().getModel();
				try
				{
					model.read(file.toURI().toURL().openStream(), "");

					// Clear existing physical things!
					StatementListCommand command = new StatementListCommand(defaultModel, ECTEvent.Type.REMOVED);
					ResIterator iterator = defaultModel.listSubjectsWithProperty(RDF.type, Schema.PhysicalThing);
					while (iterator.hasNext())
					{
						final Resource resource = iterator.nextResource();
						command.add(resource.listProperties());
					}
					iterator = defaultModel.listSubjectsWithProperty(RDF.type, Schema.Association);
					while (iterator.hasNext())
					{
						final Resource resource = iterator.nextResource();
						command.add(resource.listProperties());
					}
					command.execute();

					command = new StatementListCommand(defaultModel, ECTEvent.Type.ADDED);
					command.add(model.listStatements());
					command.execute();
					PhysicalEditDomain.INSTANCE.setFilename(filename);
					PhysicalEditDomain.INSTANCE.getCommandStack().flush();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	public void selectionChanged(final IAction action, final ISelection selection)
	{
	}
}