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
package ect.equip.physconf.ui.wizards;

import java.net.URI;
import java.net.URISyntaxException;

import ect.equip.physconf.DataspacePreferences;
import ect.equip.physconf.DataspacePreferencesImpl;
import ect.equip.physconf.PhysConfPlugin;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

/**
 * @author <a href="ktg@cs.nott.ac.uk">Kevin Glover</a>
 */
public class DataspaceConnectionWizardPage extends WizardPage implements DataspacePreferences
{
	protected Button dataspaceOptionCreate = null;
	protected Button dataspaceOptionConnect = null;
	protected Button dataspaceOptionDisconnect = null;
	protected Text dataspaceURL = null;
	protected Button dataspaceOptionStartup = null;

	/**
	 * @param model
	 * @param resource
	 */
	public DataspaceConnectionWizardPage()
	{
		super("DataspaceConnectionWizardPage");
		setTitle("Dataspace Connection");
		setMessage("Create a dataspace, or enter a url to connect to an existing dataspace.");
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	public void createControl(final Composite parent)
	{
		parent.setLayout(new GridLayout(2, false));

		Group group = new Group(parent, SWT.NONE);
		group.setText("Dataspace");
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));

		dataspaceOptionCreate = new Button(group, SWT.RADIO);
		dataspaceOptionCreate.setText("Create a dataspace");
		dataspaceOptionCreate.setLayoutData(new GridData());
		dataspaceOptionCreate.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				validate();
			}

			public void widgetSelected(final SelectionEvent e)
			{
				validate();
			}
		});

		dataspaceOptionConnect = new Button(group, SWT.RADIO);
		dataspaceOptionConnect.setText("Connect to a dataspace");
		dataspaceOptionConnect.setLayoutData(new GridData());
		dataspaceOptionConnect.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				validate();
			}

			public void widgetSelected(final SelectionEvent e)
			{
				validate();

			}
		});

		dataspaceOptionDisconnect = new Button(group, SWT.RADIO);
		dataspaceOptionDisconnect.setLayoutData(new GridData());
		dataspaceOptionDisconnect.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(final SelectionEvent e)
			{
				validate();
			}

			public void widgetSelected(final SelectionEvent e)
			{
				validate();
			}
		});

		if (!PhysConfPlugin.getDefault().getDataspaceMonitor().isConnected())
		{
			dataspaceOptionDisconnect.setText("Disconnect from dataspace");
			dataspaceOptionDisconnect.setEnabled(false);
		}
		else
		{
			dataspaceOptionDisconnect.setText("Disconnect from "
					+ PhysConfPlugin.getDefault().getDataspaceMonitor().getDataspaceURL());
		}

		dataspaceOptionStartup = new Button(group, SWT.CHECK);

		group = new Group(parent, SWT.NONE);
		group.setText("Dataspace URL");
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));

		dataspaceURL = new Text(group, SWT.BORDER);
		dataspaceURL.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		dataspaceURL.clearSelection();
		dataspaceURL.addModifyListener(new ModifyListener()
		{
			public void modifyText(final ModifyEvent e)
			{
				validate();
			}
		});

		group = new Group(parent, SWT.NONE);
		group.setText("Component Directory");
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));

		setControl(dataspaceOptionCreate);
		getInitalValues();

		validate();
	}

	public boolean getDataspaceOptionConnect()
	{
		return dataspaceOptionConnect.getSelection();
	}

	public boolean getDataspaceOptionCreate()
	{
		return dataspaceOptionCreate.getSelection();
	}

	public boolean getDataspaceOptionDisconnect()
	{
		return dataspaceOptionDisconnect.getSelection();
	}

	public boolean getDataspaceOptionStartup()
	{
		return dataspaceOptionStartup.getSelection();
	}

	public String getDataspaceURL()
	{
		return dataspaceURL.getText();
	}

	/**
	 * 
	 */
	private void getInitalValues()
	{
		final DataspacePreferences prefs = new DataspacePreferencesImpl();
		dataspaceOptionStartup.setSelection(prefs.getDataspaceOptionStartup());
		dataspaceOptionConnect.setSelection(prefs.getDataspaceOptionConnect());
		dataspaceOptionCreate.setSelection(prefs.getDataspaceOptionCreate());
		dataspaceURL.setText(prefs.getDataspaceURL());
	}

	protected void validate()
	{
		if (dataspaceOptionDisconnect != null && dataspaceOptionDisconnect.getSelection())
		{
			dataspaceOptionStartup.setEnabled(false);
			dataspaceURL.setEnabled(false);

			setMessage("Create a dataspace, or enter a url to connect to an existing dataspace.");
		}
		else
		{
			dataspaceOptionStartup.setEnabled(true);
			dataspaceURL.setEnabled(true);
			if (dataspaceOptionConnect.getSelection())
			{
				dataspaceOptionStartup.setText("Connect to dataspace at '" + dataspaceURL.getText()
						+ "' when application starts");
			}
			else if (dataspaceOptionCreate.getSelection())
			{
				dataspaceOptionStartup.setText("Create dataspace at '" + dataspaceURL.getText()
						+ "' when application starts");
			}

			if (PhysConfPlugin.getDefault().getDataspaceMonitor().isConnected())
			{
				setMessage("You are already connected to the dataspace '"
						+ PhysConfPlugin.getDefault().getDataspaceMonitor().getDataspaceURL()
						+ "'. If you connect or start a new dataspace, you will be disconnected from this one.",
						IMessageProvider.WARNING);
			}
		}

		try
		{
			final URI uri = new URI(dataspaceURL.getText().trim());
			setPageComplete(uri.getScheme().equals("equip"));
			setErrorMessage(null);
		}
		catch (final URISyntaxException e)
		{
			setPageComplete(false);
			setErrorMessage("Not a valid dataspace URL");
		}

		dataspaceOptionStartup.getParent().layout();
	}
}