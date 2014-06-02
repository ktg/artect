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
package ect.equip.physconf.ui;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public abstract class ModelContentProvider implements IStructuredContentProvider, ModelChangedListener
{
	protected Model model;
	protected StructuredViewer viewer;

	public void addedStatement(final Statement s)
	{
		if (RDFS.label.equals(s.getPredicate()))
		{
			viewer.update(s.getSubject(), null);
		}
	}

	@SuppressWarnings("unchecked")
	public void addedStatements(final List statements)
	{
		for (final Statement statement : (List<Statement>) statements)
		{
			addedStatement(statement);
		}
	}

	public void addedStatements(final Model m)
	{
		addedStatements(m.listStatements());
	}

	public void addedStatements(final Statement[] statements)
	{
		for (final Statement statement : statements)
		{
			addedStatement(statement);
		}
	}

	public void addedStatements(final StmtIterator statements)
	{
		while (statements.hasNext())
		{
			addedStatement(statements.nextStatement());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose()
	{
		if (model != null)
		{
			model.unregister(this);
			model = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface
	 * .viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
	{
		this.viewer = (StructuredViewer) viewer;
		Model newModel = null;
		if (newInput instanceof Model)
		{
			newModel = (Model) newInput;
		}
		else if (newInput instanceof Resource)
		{
			newModel = ((Resource) newInput).getModel();
		}
		if (model != null)
		{
			model.unregister(this);
		}
		if (newModel != null)
		{
			newModel.register(this);
		}
		model = newModel;
	}

	public void notifyEvent(final Model m, final Object event)
	{
		// Do nothing?
	}

	public void removedStatement(final Statement s)
	{
		if (RDFS.label.equals(s.getPredicate()))
		{
			viewer.update(s.getSubject(), null);
		}
	}

	@SuppressWarnings("unchecked")
	public void removedStatements(final List statements)
	{
		for (final Statement statement : (List<Statement>) statements)
		{
			removedStatement(statement);
		}
	}

	public void removedStatements(final Model m)
	{
		removedStatements(m.listStatements());
	}

	public void removedStatements(final Statement[] statements)
	{
		for (final Statement statement : statements)
		{
			removedStatement(statement);
		}
	}

	public void removedStatements(final StmtIterator statements)
	{
		while (statements.hasNext())
		{
			removedStatement(statements.nextStatement());
		}
	}
}