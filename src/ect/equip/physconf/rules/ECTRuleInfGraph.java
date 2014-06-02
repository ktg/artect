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

import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.reasoner.rulesys.RETERuleInfGraph;

import ect.equip.physconf.ECTInferenceEventListener;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class ECTRuleInfGraph extends RETERuleInfGraph
{
	/**
	 * @param reasoner
	 * @param rules
	 * @param schema
	 */
	@SuppressWarnings("unchecked")
	public ECTRuleInfGraph(final ECTRuleReasoner reasoner, final List rules, final Graph schema)
	{
		super(reasoner, rules, schema);
	}

	/**
	 * @param reasoner
	 * @param rules
	 * @param schema
	 * @param data
	 */
	@SuppressWarnings("unchecked")
	public ECTRuleInfGraph(final ECTRuleReasoner reasoner, final List rules, final Graph schema, final Graph data)
	{
		super(reasoner, rules, schema, data);
	}

	/**
	 * @param dataspaceMonitor
	 */
	public void addECTInferenceEventListener(final ECTInferenceEventListener listener)
	{
		if (engine != null)
		{
			((ECTRuleEngine) engine).addECTInferenceEventListener(listener);
		}
	}

	/**
	 * @param dataspaceMonitor
	 */
	public void removeECTInferenceEventListener(final ECTInferenceEventListener listener)
	{
		if (engine != null)
		{
			((ECTRuleEngine) engine).removeECTInferenceEventListener(listener);
		}
	}

	/*
	 * (non-Javadoc)
	 * @seecom.hp.hpl.jena.reasoner.rulesys.BasicForwardRuleInfGraph#
	 * instantiateRuleEngine(java.util.List)
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void instantiateRuleEngine(final List rules)
	{
		if (rules != null)
		{
			engine = new ECTRuleEngine(this, rules);
		}
		else
		{
			engine = new ECTRuleEngine(this);
		}
	}
}