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

import java.util.ArrayList;
import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.Model;

import ect.equip.physconf.ECTEvent;

import org.eclipse.gef.commands.Command;

public class StatementQueueCommand extends Command
{
	private ArrayList<StatementListCommand> eventList = new ArrayList<StatementListCommand>();
	private StatementListCommand lastEvent;
	private final Model model;

	/**
	 * @param ectrdf
	 */
	public StatementQueueCommand(final Model model)
	{
		this.model = model;
	}

	@Override
	public void execute()
	{
		for (final StatementListCommand event : eventList)
		{
			event.execute();
		}
	}

	public StatementListCommand getEventList(final ECTEvent.Type type)
	{
		if (lastEvent == null || lastEvent.getEventType() != type)
		{
			lastEvent = new StatementListCommand(model, type);
			eventList.add(lastEvent);
		}
		return lastEvent;
	}

	@Override
	public void undo()
	{
		for (int index = eventList.size() - 1; index >= 0; index--)
		{
			eventList.get(index).undo();
		}
	}

	protected Iterator<StatementListCommand> clear()
	{
		final Iterator<StatementListCommand> iterator = eventList.iterator();
		eventList = new ArrayList<StatementListCommand>();
		lastEvent = null;
		return iterator;
	}
}