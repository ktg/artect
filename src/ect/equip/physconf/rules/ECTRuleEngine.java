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
package ect.equip.physconf.rules;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.reasoner.rulesys.impl.RETEEngine;

import ect.equip.physconf.ECTInferenceEvent;
import ect.equip.physconf.ECTInferenceEventListener;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class ECTRuleEngine extends RETEEngine
{
	private final List<ECTInferenceEventListener> listeners = new ArrayList<ECTInferenceEventListener>();

	/**
	 * @param parent
	 */
	public ECTRuleEngine(final ECTRuleInfGraph parent)
	{
		super(parent);
	}

	/**
	 * @param parent
	 * @param rules
	 */
	@SuppressWarnings("unchecked")
	public ECTRuleEngine(final ECTRuleInfGraph parent, final List rules)
	{
		super(parent, rules);
	}

	/**
	 * @param listener
	 */
	public void addECTInferenceEventListener(final ECTInferenceEventListener listener)
	{
		listeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.hp.hpl.jena.reasoner.rulesys.impl.RETEEngine#addTriple(com.hp.hpl
	 * .jena.graph.Triple, boolean)
	 */
	@Override
	public void addTriple(final Triple triple, final boolean deduction)
	{
		super.addTriple(triple, deduction);
		if (deduction)
		{
			fireInfGraphEvent(new ECTInferenceEvent(infGraph, triple, true));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.hp.hpl.jena.reasoner.rulesys.impl.RETEEngine#deleteTriple(com.hp.
	 * hpl.jena.graph.Triple, boolean)
	 */
	@Override
	public void deleteTriple(final Triple triple, final boolean deduction)
	{
		super.deleteTriple(triple, deduction);
		if (deduction)
		{
			fireInfGraphEvent(new ECTInferenceEvent(infGraph, triple, false));
		}
	}

	/**
	 * @param listener
	 */
	public void removeECTInferenceEventListener(final ECTInferenceEventListener listener)
	{
		listeners.remove(listener);
	}

	/**
	 * @param triple
	 */
	private void fireInfGraphEvent(final ECTInferenceEvent event)
	{
		final ECTInferenceEventListener[] list = listeners.toArray(new ECTInferenceEventListener[0]);
		for (final ECTInferenceEventListener listener : list)
		{
			listener.inferenceEvent(event);
		}
	}
}