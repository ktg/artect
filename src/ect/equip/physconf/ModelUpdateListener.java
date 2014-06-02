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
package ect.equip.physconf;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.StmtIteratorImpl;

import org.eclipse.swt.widgets.Display;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public abstract class ModelUpdateListener implements ModelChangedListener, ECTInferenceEventListener
{
	private boolean dirty = false;

	/*
	 * (non-Javadoc)
	 * @see
	 * com.hp.hpl.jena.rdf.model.ModelChangedListener#addedStatement(com.hp.
	 * hpl.jena.rdf.model.Statement)
	 */
	public void addedStatement(final Statement s)
	{
		if (internalShouldUpdate(s))
		{
			internalUpdate();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.hp.hpl.jena.rdf.model.ModelChangedListener#addedStatements(java.util
	 * .List)
	 */
	@SuppressWarnings("unchecked")
	public void addedStatements(final List statements)
	{
		addedStatements(new StmtIteratorImpl(statements.iterator()));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.hp.hpl.jena.rdf.model.ModelChangedListener#addedStatements(com.hp
	 * .hpl.jena.rdf.model.Model)
	 */
	public void addedStatements(final Model m)
	{
		addedStatements(m.listStatements());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.hp.hpl.jena.rdf.model.ModelChangedListener#addedStatements(com.hp
	 * .hpl.jena.rdf.model.Statement[])
	 */
	public void addedStatements(final Statement[] statements)
	{
		for (final Statement statement : statements)
		{
			if (internalShouldUpdate(statement))
			{
				internalUpdate();
				return;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.hp.hpl.jena.rdf.model.ModelChangedListener#addedStatements(com.hp
	 * .hpl.jena.rdf.model.StmtIterator)
	 */
	public void addedStatements(final StmtIterator statements)
	{
		while (statements.hasNext())
		{
			if (internalShouldUpdate(statements.nextStatement()))
			{
				internalUpdate();
				return;
			}
		}
	}

	public void inferenceEvent(final ECTInferenceEvent event)
	{
		final Statement statement = PhysConfPlugin.getDefault().getModel().asStatement(event.getTriple());
		if (event.isAdded())
		{
			addedStatement(statement);
		}
		else
		{
			removedStatement(statement);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.hp.hpl.jena.rdf.model.ModelChangedListener#notifyEvent(com.hp.hpl
	 * .jena.rdf.model.Model, java.lang.Object)
	 */
	public void notifyEvent(final Model m, final Object event)
	{
		System.out.println("ModelUpdateListener.notifyEvent(): " + event);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.hp.hpl.jena.rdf.model.ModelChangedListener#removedStatement(com.hp
	 * .hpl.jena.rdf.model.Statement)
	 */
	public void removedStatement(final Statement s)
	{
		if (internalShouldUpdate(s))
		{
			internalUpdate();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.hp.hpl.jena.rdf.model.ModelChangedListener#removedStatements(java
	 * .util.List)
	 */
	@SuppressWarnings("unchecked")
	public void removedStatements(final List statements)
	{
		addedStatements(new StmtIteratorImpl(statements.iterator()));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.hp.hpl.jena.rdf.model.ModelChangedListener#removedStatements(com.
	 * hp.hpl.jena.rdf.model.Model)
	 */
	public void removedStatements(final Model m)
	{
		addedStatements(m.listStatements());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.hp.hpl.jena.rdf.model.ModelChangedListener#removedStatements(com.
	 * hp.hpl.jena.rdf.model.Statement[])
	 */
	public void removedStatements(final Statement[] statements)
	{
		for (final Statement statement : statements)
		{
			if (internalShouldUpdate(statement))
			{
				internalUpdate();
				return;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.hp.hpl.jena.rdf.model.ModelChangedListener#removedStatements(com.
	 * hp.hpl.jena.rdf.model.StmtIterator)
	 */
	public void removedStatements(final StmtIterator statements)
	{
		addedStatements(statements);
	}

	public abstract boolean shouldUpdate(Statement s);

	public abstract void update();

	private boolean internalShouldUpdate(final Statement statement)
	{
		return dirty || shouldUpdate(statement);
	}

	private void internalUpdate()
	{
		dirty = true;
		Display.getDefault().asyncExec(new Runnable()
		{
			public void run()
			{
				update();
				dirty = false;
			}
		});
	}
}