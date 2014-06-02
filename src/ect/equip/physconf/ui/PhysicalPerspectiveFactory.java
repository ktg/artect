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

import ect.equip.physconf.ui.views.ActiveThingsView;
import ect.equip.physconf.ui.views.PossibleThingsView;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class PhysicalPerspectiveFactory implements IPerspectiveFactory
{
	public void createInitialLayout(final IPageLayout layout)
	{
		// Get the editor area.
		layout.setEditorAreaVisible(false);
		layout.addPerspectiveShortcut("ect.equip.physconf.ui.SoftwarePerspective");

		layout.addShowViewShortcut(PossibleThingsView.ID);
		layout.addShowViewShortcut(ActiveThingsView.ID);
		layout.addShowViewShortcut("ect.equip.physconf.ui.views.PhysicalOverview");
		layout.addShowViewShortcut("ect.equip.physconf.ui.views.PhysicalGraphView");

		final String editorArea = layout.getEditorArea();

		// Top left: Resource Navigator view and Bookmarks view placeholder
		final IFolderLayout topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.25f, editorArea);
		topLeft.addView(PossibleThingsView.ID);

		// Bottom left: Outline view and Property Sheet view
		final IFolderLayout bottomLeft = layout.createFolder("bottomLeft", IPageLayout.BOTTOM, 0.65f, "topLeft");
		bottomLeft.addView(ActiveThingsView.ID);

		// Bottom right: Properties view and task List view
		final IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, 0.75f, editorArea);
		bottom.addView(IPageLayout.ID_PROP_SHEET);

		final IFolderLayout bottomRight = layout.createFolder("bottomRight", IPageLayout.RIGHT, 0.70f, "bottom");
		bottomRight.addView("ect.equip.physconf.ui.views.PhysicalOverview");

		layout.addStandaloneView("ect.equip.physconf.ui.views.PhysicalGraphView", true, IPageLayout.TOP, 1f, editorArea);
	}
}
