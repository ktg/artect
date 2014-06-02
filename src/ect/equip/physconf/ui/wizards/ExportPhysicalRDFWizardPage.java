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

import java.io.File;

import ect.equip.physconf.PhysConfPlugin;
import ect.equip.physconf.ui.ModelLabelProvider;
import ect.equip.physconf.ui.views.PhysicalThingTreeContentProvider;

import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class ExportPhysicalRDFWizardPage extends WizardPage
{

	protected FileFieldEditor editor;
	CheckboxTreeViewer viewer;

	public ExportPhysicalRDFWizardPage(final String pageName)
	{
		super(pageName);
		setTitle(pageName);
		setDescription("Export Physical Things to an RDF file.");
		setPageComplete(false);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	public void createControl(final Composite parent)
	{
		final Composite fileSelectionArea = new Composite(parent, SWT.NONE);
		final GridLayout fileSelectionLayout = new GridLayout();
		GridData gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		fileSelectionLayout.numColumns = 3;
		fileSelectionLayout.makeColumnsEqualWidth = false;
		fileSelectionLayout.marginWidth = 0;
		fileSelectionLayout.marginHeight = 0;
		fileSelectionArea.setLayout(fileSelectionLayout);
		fileSelectionArea.setLayoutData(gridData);
		fileSelectionArea.moveAbove(null);
		setControl(fileSelectionArea);

		gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_BOTH);
		gridData.horizontalSpan = 3;
		viewer = new CheckboxTreeViewer(fileSelectionArea);
		viewer.getControl().setLayoutData(gridData);
		viewer.setContentProvider(new PhysicalThingTreeContentProvider(false));
		viewer.setLabelProvider(new ModelLabelProvider());
		viewer.setInput(PhysConfPlugin.getDefault().getModel());
		viewer.addCheckStateListener(new ICheckStateListener()
		{
			public void checkStateChanged(final CheckStateChangedEvent event)
			{
				final Object[] children = ((ITreeContentProvider) viewer.getContentProvider()).getChildren(event.getElement());
				viewer.expandToLevel(event.getElement(), 1);
				for (final Object element : children)
				{
					viewer.setChecked(element, event.getChecked());
				}
				validatePage();
			}
		});

		editor = new FileFieldEditor("fileSelect", "To RDF File: ", fileSelectionArea);
		editor.setFileExtensions(new String[] { "*.rdf", "*.owl", "*.*" });
		editor.getTextControl(fileSelectionArea).addModifyListener(new ModifyListener()
		{
			public void modifyText(final ModifyEvent e)
			{
				validatePage();
			}
		});
	}

	public File getFile()
	{
		return new File(editor.getStringValue());
	}

	public Object[] getResources()
	{
		return viewer.getCheckedElements();
	}

	/**
	 * 
	 */
	protected void validatePage()
	{
		setPageComplete(viewer.getCheckedElements().length > 0 && !"".equals(editor.getStringValue().trim()));
	}
}