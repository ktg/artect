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

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.StmtIteratorImpl;

import ect.equip.physconf.ECTEvent;

import org.eclipse.gef.commands.Command;

public class StatementListCommand extends Command
{
	protected final Model model;
	protected final HashMap<Statement, Statement> statements = new HashMap<Statement, Statement>();
	protected HashSet<Statement> original;
	private final ECTEvent.Type type;

	public StatementListCommand(final Model model, final ECTEvent.Type type)
	{
		this.model = model;
		this.type = type;
		if (type == ECTEvent.Type.UPDATED)
		{
			original = new HashSet<Statement>();
		}
	}

	public void add(final Resource resource, final Property property, final boolean bool)
	{
		add(model.createStatement(resource, property, bool));
	}

	public void add(final Resource resource, final Property property, final Object object)
	{
		add(model.createStatement(resource, property, object));
	}

	public void add(final Statement statement)
	{
		if (type != ECTEvent.Type.UPDATED)
		{
			statements.put(statement, statement);
		}
		else
		{
			final Statement key = model.createStatement(statement.getSubject(), statement.getPredicate(), false);
			statements.put(key, statement);
		}
	}

	public void add(final StmtIterator statements)
	{
		while (statements.hasNext())
		{
			add(statements.nextStatement());
		}
	}

	@Override
	public void execute()
	{
		while (true)
		{
			try
			{
				if (type == ECTEvent.Type.ADDED)
				{
					model.add(new StmtIteratorImpl(statements.keySet().iterator()));
				}
				else if (type == ECTEvent.Type.REMOVED)
				{
					model.remove(new StmtIteratorImpl(statements.keySet().iterator()));
				}
				else if (type == ECTEvent.Type.UPDATED)
				{
					for (final Statement statement : statements.values())
					{
						final Statement toChange = model.getProperty(statement.getSubject(), statement.getPredicate());
						if (toChange == null)
						{
							model.add(statement);
						}
						else
						{
							original.add(toChange);
							toChange.changeObject(statement.getObject());
						}
					}
				}
				return;
			}
			catch (final ConcurrentModificationException e)
			{

			}
		}
	}

	public ECTEvent.Type getEventType()
	{
		return type;
	}

	@Override
	public void undo()
	{
		while (true)
		{
			try
			{
				if (type == ECTEvent.Type.ADDED)
				{
					model.remove(new StmtIteratorImpl(statements.keySet().iterator()));
				}
				else if (type == ECTEvent.Type.REMOVED)
				{
					model.add(new StmtIteratorImpl(statements.keySet().iterator()));
				}
				else if (type == ECTEvent.Type.UPDATED)
				{
					model.remove(new StmtIteratorImpl(statements.values().iterator()));
					model.add(new StmtIteratorImpl(original.iterator()));
				}
				return;
			}
			catch (final ConcurrentModificationException e)
			{

			}
		}
	}
}