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
package ect.equip.physconf.ui.tabs.sections;

import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.Resource;

import ect.equip.physconf.ui.filters.TypeMapper;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;

public abstract class ResourceSection extends AbstractPropertySection
{
	private ModelChangedListener listener;
	protected Resource resource;

	@Override
	public void dispose()
	{
		if (resource != null && listener != null)
		{
			resource.getModel().unregister(listener);
		}
		super.dispose();
	}

	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection)
	{
		super.setInput(part, selection);
		setResource(getResource(selection));
	}

	protected abstract ModelChangedListener createListener();

	protected Resource getResource(final Object toTest)
	{
		final Object object = TypeMapper.getModelObject(toTest);
		if (object instanceof Resource) { return (Resource) object; }
		return null;
	}

	protected void setResource(final Resource resource)
	{
		final Resource oldResource = this.resource;
		this.resource = resource;
		if (oldResource != null)
		{
			if (resource != null)
			{
				if (!resource.getModel().equals(oldResource.getModel()))
				{
					oldResource.getModel().unregister(listener);
					resource.getModel().register(listener);
				}
			}
			else
			{
				oldResource.getModel().unregister(listener);
			}
		}
		else
		{
			if (resource != null)
			{
				if (listener == null)
				{
					listener = createListener();
				}
				if (listener != null)
				{
					resource.getModel().register(listener);
				}
			}
		}
	}
}