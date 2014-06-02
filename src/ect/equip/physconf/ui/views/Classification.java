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

import java.util.HashSet;
import java.util.Iterator;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import digitalrecord.wrapped.Schema;

import org.eclipse.jface.viewers.TreeViewer;

public class Classification implements Iterable<Object>
{
	private final HashSet<Object> children = new HashSet<Object>();
	private boolean classify = true;
	private final TreeViewer viewer;
	private final String name;

	public Classification(final TreeViewer viewer, final String name)
	{
		this.name = name;
		this.viewer = viewer;
	}

	public void add(final Resource resource)
	{
		final StmtIterator iterator = resource.listProperties(Schema.classification);
		if (iterator.hasNext())
		{
			while (iterator.hasNext())
			{
				add(resource, iterator.nextStatement().getString());
			}
		}
		else
		{
			children.add(resource);
		}
	}

	public Classification getChild(final String name)
	{
		Classification child = findChild(name);
		if (child == null)
		{
			final Classification newClass = new Classification(viewer, name);
			children.add(newClass);
			if (classify)
			{
				viewer.add(this, newClass);
			}
			child = newClass;
		}
		return child;
	}

	public String getName()
	{
		return name;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#isEmpty()
	 */
	public boolean isEmpty()
	{
		return children.isEmpty();
	}

	public Iterator<Object> iterator()
	{
		return children.iterator();
	}

	public void remove(final Object object)
	{
		final Iterator<Object> iterator = children.iterator();
		while (iterator.hasNext())
		{
			final Object next = iterator.next();
			if (next.equals(object))
			{
				iterator.remove();
				viewer.remove(object);
			}
			else if (next instanceof Classification)
			{
				final Classification child = (Classification) next;
				child.remove(object);
				if (child.isEmpty())
				{
					iterator.remove();
					viewer.remove(child);
				}
			}
		}
	}

	public void removeChild(final String name)
	{
		final Classification child = findChild(name);
		if (child != null)
		{
			children.remove(child);
		}
	}

	public void setClassify(final boolean value)
	{
		classify = value;
	}

	public Object[] toArray()
	{
		return children.toArray();
	}

	@Override
	public String toString()
	{
		return name;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	private void add(final Object o, final String classification)
	{
		String name = classification;
		final int index = classification.indexOf('/');
		if (index >= 0)
		{
			name = classification.substring(0, index);
		}
		final Classification childClass = getChild(name);
		if (index >= 0)
		{
			childClass.add(o, classification.substring(index + 1));
		}
		else
		{
			childClass.children.add(o);
			if (classify)
			{
				viewer.add(childClass, o);
			}
		}
	}

	private Classification findChild(final String classification)
	{
		for (final Object child : children)
		{
			if (child instanceof Classification)
			{
				final Classification classChild = (Classification) child;
				if (classChild.getName().equals(classification)) { return (Classification) child; }
			}
		}
		return null;
	}
}