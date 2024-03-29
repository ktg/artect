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
package ect.equip.physconf.ui.views;

import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import ect.equip.physconf.PhysConfPlugin;
import ect.equip.physconf.ui.ModelTreeContentProvider;

import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class AllResourceView extends ResourceView
{
	/*
	 * (non-Javadoc)
	 * @see ect.equip.physconf.ui.views.ResourceView#getContentProvider()
	 */
	@Override
	protected ITreeContentProvider getContentProvider()
	{
		return new ModelTreeContentProvider()
		{
			@Override
			public void addedStatement(final Statement s)
			{
				super.addedStatement(s);
				// viewer.refresh();
			}

			public Object[] getChildren(final Object parentElement)
			{
				return new Object[0];
			}

			public Object[] getElements(final Object inputElement)
			{
				final ResIterator iterator = PhysConfPlugin.getDefault().getModel().listSubjects();
				final ArrayList<Resource> list = new ArrayList<Resource>();
				while (iterator.hasNext())
				{
					list.add(iterator.nextResource());
				}
				return list.toArray();
			}

			@Override
			public void removedStatement(final Statement s)
			{
				super.removedStatement(s);
				// viewer.refresh();
			}
		};
	}
}