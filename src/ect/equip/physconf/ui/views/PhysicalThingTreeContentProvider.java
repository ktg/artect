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

import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import digitalrecord.wrapped.Schema;
import ect.equip.physconf.ui.ModelTreeContentProvider;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class PhysicalThingTreeContentProvider extends ModelTreeContentProvider
{
	private boolean showRoot = false;
	private final boolean classify = true;
	private boolean dirty = false;
	protected Classification root;

	/**
	 * @param view
	 */
	public PhysicalThingTreeContentProvider()
	{
		super();
	}

	public PhysicalThingTreeContentProvider(final boolean showRoot)
	{
		super();
		this.showRoot = showRoot;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.hp.hpl.jena.rdf.model.ModelChangedListener#addedStatement(com.hp.
	 * hpl.jena.rdf.model.Statement)
	 */
	@Override
	public void addedStatement(final Statement s)
	{
		if (Schema.hasPart.equals(s.getPredicate()))
		{
			if (showRoot)
			{
				getTreeViewer().remove(root, new Object[] { s.getObject() });
			}
			else
			{
				internalUpdate();
			}
			getTreeViewer().add(s.getSubject(), s.getObject());
		}
		else if (RDF.type.equals(s.getPredicate()) && s.getSubject().hasProperty(RDF.type, Schema.PhysicalThing)
				&& !model.contains(null, Schema.hasPart, s.getSubject()))
		{
			root.add(s.getSubject());
			if (showRoot)
			{
				getTreeViewer().add(root, s.getSubject());
			}
			else
			{
				internalUpdate();
			}
		}
		else if (Schema.classification.equals(s.getPredicate())
				&& s.getSubject().hasProperty(RDF.type, Schema.PhysicalThing)
				&& !model.contains(null, Schema.hasPart, s.getSubject()))
		{
			root.add(s.getSubject());
		}
		else
		{
			super.addedStatement(s);
		}
	}

	public Object[] getChildren(final Object parent)
	{
		if (root.equals(parent))
		{
			if (classify)
			{
				return root.toArray();
			}
			else
			{
				final ArrayList<Resource> list = new ArrayList<Resource>();
				final ResIterator iterator = model.listSubjectsWithProperty(RDF.type, Schema.PhysicalThing);
				while (iterator.hasNext())
				{
					final Resource resource = iterator.nextResource();
					if (!model.contains(null, Schema.hasPart, resource))
					{
						list.add(resource);
					}
				}
				return list.toArray();
			}
		}
		else if (parent instanceof Resource)
		{
			final Resource resource = (Resource) parent;
			final NodeIterator iterator = model.listObjectsOfProperty(resource, Schema.hasPart);
			final ArrayList<RDFNode> list = new ArrayList<RDFNode>();
			while (iterator.hasNext())
			{
				list.add(iterator.nextNode());
			}
			return list.toArray();
		}
		else if (parent instanceof Classification) { return ((Classification) parent).toArray(); }
		return new Object[0];
	}

	public Object[] getElements(final Object parent)
	{
		if (parent.equals(model))
		{
			if (showRoot) { return new Object[] { root }; }
			return getChildren(root);
		}
		return getChildren(parent);
	}

	@Override
	public Object getParent(final Object child)
	{
		if (child instanceof Resource) { return root; }
		return null;
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput)
	{
		super.inputChanged(viewer, oldInput, newInput);
		root = new Classification((TreeViewer) viewer, "Physical Things");
		if (newInput != null)
		{
			final ResIterator iterator = model.listSubjectsWithProperty(RDF.type, Schema.PhysicalThing);
			while (iterator.hasNext())
			{
				final Resource resource = iterator.nextResource();
				if (!model.contains(null, Schema.hasPart, resource))
				{
					root.add(resource);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.hp.hpl.jena.rdf.model.ModelChangedListener#removedStatement(com.hp
	 * .hpl.jena.rdf.model.Statement)
	 */
	@Override
	public void removedStatement(final Statement s)
	{
		if (RDF.type.equals(s.getPredicate())
				&& (Schema.PhysicalThing.equals(s.getObject()) || model.contains((Resource) s.getObject(),
						RDFS.subClassOf, Schema.PhysicalThing))
				|| !model.contains(s.getSubject(), RDF.type, Schema.PhysicalThing))
		{
			root.remove(s.getSubject());
			if (showRoot)
			{
				getTreeViewer().remove(root, new Object[] { s.getSubject() });
			}
			else
			{
				internalUpdate();
			}
		}
		else if (RDFS.subClassOf.equals(s.getPredicate()) && Schema.PhysicalThing.equals(s.getObject()))
		{
			final ResIterator iterator = model.listSubjectsWithProperty(RDF.type, s.getSubject());
			while (iterator.hasNext())
			{
				System.out.println(iterator.next());
			}
		}
		else
		{
			super.removedStatement(s);
		}
	}

	private void internalUpdate()
	{
		if (!dirty)
		{
			dirty = true;
			Display.getDefault().asyncExec(new Runnable()
			{
				public void run()
				{
					getTreeViewer().refresh();
					dirty = false;
				}
			});
		}
	}
}