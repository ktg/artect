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

import equip.data.GUID;
import equip.ect.Capability;
import equip.ect.CompInfo;
import equip.ect.ComponentAdvert;
import equip.ect.ComponentProperty;
import equip.ect.ComponentRequest;
import equip.ect.Container;
import equip.ect.PropertyLinkRequest;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class ECTEvent
{
	public enum Type
	{
		ADDED, REMOVED, UPDATED
	}

	private final Type type;
	private final CompInfo compInfo;

	public ECTEvent(final CompInfo compInfo, final Type type)
	{
		this.compInfo = compInfo;
		this.type = type;
	}

	public GUID getID()
	{
		return compInfo.getID();
	}

	public CompInfo getObject()
	{
		return compInfo;
	}

	public Type getType()
	{
		return type;
	}

	@Override
	public String toString()
	{
		String string = "ECT Event: ";
		if (compInfo instanceof Capability)
		{
			string += "Capability " + ((Capability) compInfo).getCapabilityName();
		}
		else if (compInfo instanceof ComponentAdvert)
		{
			string += "Component " + ((ComponentAdvert) compInfo).getComponentName();
		}
		else if (compInfo instanceof ComponentRequest)
		{
			string += "Component Request";
		}
		else if (compInfo instanceof ComponentProperty)
		{
			string += "Property " + ((ComponentProperty) compInfo).getPropertyName();
		}
		else if (compInfo instanceof PropertyLinkRequest)
		{
			string += "Property Link " + ((PropertyLinkRequest) compInfo).getSourcePropertyName() + "->"
					+ ((PropertyLinkRequest) compInfo).getDestinationPropertyName();
		}
		else if (compInfo instanceof Container)
		{
			string += "Container " + ((Container) compInfo).getContainerName();
		}

		if (type == Type.ADDED)
		{
			string += " added";
		}
		else if (type == Type.REMOVED)
		{
			string += " removed";
		}
		else if (type == Type.UPDATED)
		{
			string += " updated";
		}

		return string;
	}
}
