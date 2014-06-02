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
package ect.equip.physconf.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import ect.equip.physconf.PhysConfPlugin;
import ect.equip.physconf.ui.wizards.DataspaceConnectionWizard;

import org.eclipse.draw2d.Cursors;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

public class ConnectionControlContribution extends WorkbenchWindowControlContribution implements
		PropertyChangeListener, MouseListener
{
	private static final String TOOLTIP = "Click here to start or connect to a dataspace.";

	private CLabel label;

	public ConnectionControlContribution()
	{
	}

	public ConnectionControlContribution(final String id)
	{
		super(id);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt
	 * .events.MouseEvent)
	 */
	public void mouseDoubleClick(final MouseEvent e)
	{
		// No double clicking
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events
	 * .MouseEvent)
	 */
	public void mouseDown(final MouseEvent e)
	{
		new WizardDialog(getWorkbenchWindow().getShell(), new DataspaceConnectionWizard()).open();
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.
	 * MouseEvent)
	 */
	public void mouseUp(final MouseEvent e)
	{
		// We don't care about mouse up
	}

	public void propertyChange(final PropertyChangeEvent evt)
	{
		if ("connected".equals(evt.getPropertyName()))
		{
			Display.getDefault().asyncExec(new Runnable()
			{
				public void run()
				{
					setConnected(((Boolean) evt.getNewValue()).booleanValue());
				}
			});
		}
	}

	public void setConnected(final boolean connected)
	{
		if (label.isDisposed()) { return; }
		if (connected)
		{
			label.setImage(PhysConfPlugin.getImage("dataspace_connection"));
			label.setText("Connected to " + PhysConfPlugin.getDefault().getDataspaceMonitor().getDataspaceURL());
			label.setCursor(Cursors.ARROW);
			label.setToolTipText(null);
			label.pack(true);
		}
		else
		{
			label.setImage(PhysConfPlugin.getImage("warning"));
			label.setText("Not connected to any Dataspace");
			label.setCursor(Cursors.HAND);
			label.setToolTipText(TOOLTIP);
			label.pack(true);
		}
	}

	@Override
	protected Control createControl(final Composite parent)
	{
		if (label != null)
		{
			label.dispose();
		}
		label = new CLabel(parent, SWT.NONE);
		label.addMouseListener(this);

		PhysConfPlugin.getDefault().getDataspaceMonitor().addPropertyChangeListener(this);
		setConnected(PhysConfPlugin.getDefault().getDataspaceMonitor().isConnected());
		return label;
	}
}