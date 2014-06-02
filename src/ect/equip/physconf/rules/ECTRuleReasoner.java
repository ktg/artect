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

import java.util.HashSet;
import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.InfGraph;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerException;
import com.hp.hpl.jena.reasoner.ReasonerFactory;
import com.hp.hpl.jena.reasoner.rulesys.BasicForwardRuleInfGraph;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.RulePreprocessHook;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class ECTRuleReasoner extends GenericRuleReasoner
{

	/**
	 * @param rules
	 */
	@SuppressWarnings("unchecked")
	public ECTRuleReasoner(final List rules)
	{
		super(rules);
	}

	/**
	 * @param rules
	 * @param schemaGraph
	 * @param factory
	 * @param mode
	 */
	@SuppressWarnings("unchecked")
	public ECTRuleReasoner(final List rules, final Graph schemaGraph, final ReasonerFactory factory, final RuleMode mode)
	{
		super(rules, schemaGraph, factory, mode);
	}

	/**
	 * @param rules
	 * @param factory
	 */
	@SuppressWarnings("unchecked")
	public ECTRuleReasoner(final List rules, final ReasonerFactory factory)
	{
		super(rules, factory);
	}

	/**
	 * @param factory
	 * @param configuration
	 */
	public ECTRuleReasoner(final ReasonerFactory factory, final Resource configuration)
	{
		super(factory, configuration);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner#bind(com.hp.hpl.
	 * jena.graph.Graph)
	 */
	@Override
	public InfGraph bind(final Graph data) throws ReasonerException
	{
		final Graph schemaArg = schemaGraph == null ? getPreload() : schemaGraph;
		final BasicForwardRuleInfGraph graph = new ECTRuleInfGraph(this, rules, schemaArg);
		graph.setDerivationLogging(recordDerivations);
		// graph.setTraceOn(traceOn);
		graph.rebind(data);
		return graph;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner#bindSchema(com.hp
	 * .hpl.jena.graph.Graph)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Reasoner bindSchema(final Graph tbox) throws ReasonerException
	{
		if (schemaGraph != null) { throw new ReasonerException(
				"Can only bind one schema at a time to a GenericRuleReasoner"); }
		final InfGraph graph = new ECTRuleInfGraph(this, rules, null, tbox);
		graph.prepare();
		final GenericRuleReasoner grr = new ECTRuleReasoner(rules, graph, factory, mode);
		grr.setDerivationLogging(recordDerivations);
		// grr.setTraceOn(traceOn);
		grr.setTransitiveClosureCaching(enableTGCCaching);
		grr.setFunctorFiltering(filterFunctors);
		if (preprocessorHooks != null)
		{
			for (final RulePreprocessHook hook : (HashSet<RulePreprocessHook>) preprocessorHooks)
			{
				grr.addPreprocessingHook(hook);
			}
		}
		return grr;
	}

	/*
	 * (non-Javadoc)
	 * @see com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner#getPreload()
	 */
	@Override
	protected synchronized InfGraph getPreload()
	{
		if (cachePreload && preload == null)
		{
			preload = new ECTRuleInfGraph(this, rules, null);
			preload.prepare();
		}
		return preload;
	}
}