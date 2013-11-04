/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: DefaultActionFactory 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2011, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */

package org.openmdx.portal.servlet;

import org.openmdx.portal.servlet.action.AbstractAction;
import org.openmdx.portal.servlet.action.CancelAction;
import org.openmdx.portal.servlet.action.DeleteAction;
import org.openmdx.portal.servlet.action.EditAction;
import org.openmdx.portal.servlet.action.EditAsNewAction;
import org.openmdx.portal.servlet.action.FindObjectAction;
import org.openmdx.portal.servlet.action.FindObjectsAction;
import org.openmdx.portal.servlet.action.GetOperationDialogAction;
import org.openmdx.portal.servlet.action.GridAddColumnFilterAction;
import org.openmdx.portal.servlet.action.GridAddObjectAction;
import org.openmdx.portal.servlet.action.GridAddOrderAnyAction;
import org.openmdx.portal.servlet.action.GridAddOrderAscendingAction;
import org.openmdx.portal.servlet.action.GridAddOrderDescendingAction;
import org.openmdx.portal.servlet.action.GridGetRowMenuAction;
import org.openmdx.portal.servlet.action.GridMoveDownObjectAction;
import org.openmdx.portal.servlet.action.GridMoveUpObjectAction;
import org.openmdx.portal.servlet.action.GridPageNextAction;
import org.openmdx.portal.servlet.action.GridPagePreviousAction;
import org.openmdx.portal.servlet.action.GridSelectFilterAction;
import org.openmdx.portal.servlet.action.GridSelectReferenceAction;
import org.openmdx.portal.servlet.action.GridSetAlignmentNarrowAction;
import org.openmdx.portal.servlet.action.GridSetAlignmentWideAction;
import org.openmdx.portal.servlet.action.GridSetColumnFilterAction;
import org.openmdx.portal.servlet.action.GridSetCurrentFilterAsDefaultAction;
import org.openmdx.portal.servlet.action.GridSetHideRowsOnInitAction;
import org.openmdx.portal.servlet.action.GridSetOrderAnyAction;
import org.openmdx.portal.servlet.action.GridSetOrderAscendingAction;
import org.openmdx.portal.servlet.action.GridSetOrderDescendingAction;
import org.openmdx.portal.servlet.action.GridSetPageAction;
import org.openmdx.portal.servlet.action.GridSetShowRowsOnInitAction;
import org.openmdx.portal.servlet.action.InvokeOperationAction;
import org.openmdx.portal.servlet.action.LogoffAction;
import org.openmdx.portal.servlet.action.MacroAction;
import org.openmdx.portal.servlet.action.MultiDeleteAction;
import org.openmdx.portal.servlet.action.NewObjectAction;
import org.openmdx.portal.servlet.action.ObjectGetAttributesAction;
import org.openmdx.portal.servlet.action.ReloadAction;
import org.openmdx.portal.servlet.action.SaveAction;
import org.openmdx.portal.servlet.action.CreateAction;
import org.openmdx.portal.servlet.action.SaveGridAction;
import org.openmdx.portal.servlet.action.SaveSettingsAction;
import org.openmdx.portal.servlet.action.SelectAndEditObjectAction;
import org.openmdx.portal.servlet.action.SelectAndNewObjectAction;
import org.openmdx.portal.servlet.action.SelectLocaleAction;
import org.openmdx.portal.servlet.action.SelectObjectAction;
import org.openmdx.portal.servlet.action.SelectPerspectiveAction;
import org.openmdx.portal.servlet.action.SelectViewportAction;
import org.openmdx.portal.servlet.action.SetPanelStateAction;

/**
 * DefaultActionFactory
 *
 */
public class DefaultActionFactory implements ActionFactory_1_0 {

	public AbstractAction getAction(
		short event
	) {
		switch(event) {
			case SaveAction.EVENT_ID:
				return new SaveAction();
			case CancelAction.EVENT_ID:
				return new CancelAction();
			case FindObjectsAction.EVENT_ID:
				return new FindObjectsAction();
			case GridSelectReferenceAction.EVENT_ID:
				return new GridSelectReferenceAction();
			case GridPageNextAction.EVENT_ID:
				return new GridPageNextAction();
			case GridSetPageAction.EVENT_ID:
				return new GridSetPageAction();
			case GridPagePreviousAction.EVENT_ID:
				return new GridPagePreviousAction();
			case GridSelectFilterAction.EVENT_ID:
				return new GridSelectFilterAction();
			case GridSetColumnFilterAction.EVENT_ID:
				return new GridSetColumnFilterAction();
			case GridAddColumnFilterAction.EVENT_ID:
				return new GridAddColumnFilterAction();
			case GridSetOrderAscendingAction.EVENT_ID:
				return new GridSetOrderAscendingAction();
			case GridAddOrderAscendingAction.EVENT_ID:
				return new GridAddOrderAscendingAction();
			case GridSetOrderDescendingAction.EVENT_ID:
				return new GridSetOrderDescendingAction();
			case GridAddOrderDescendingAction.EVENT_ID:
				return new GridAddOrderAscendingAction();
			case GridSetOrderAnyAction.EVENT_ID:
				return new GridSetOrderAnyAction();
			case GridAddOrderAnyAction.EVENT_ID:
				return new GridAddOrderAnyAction();
			case GridSetAlignmentWideAction.EVENT_ID:
				return new GridSetAlignmentWideAction();
			case GridSetAlignmentNarrowAction.EVENT_ID:
				return new GridSetAlignmentNarrowAction();
			case GridSetCurrentFilterAsDefaultAction.EVENT_ID:
				return new GridSetCurrentFilterAsDefaultAction();
			case GridAddObjectAction.EVENT_ID:
				return new GridAddObjectAction();
			case GridSetShowRowsOnInitAction.EVENT_ID:
				return new GridSetShowRowsOnInitAction();
			case GridGetRowMenuAction.EVENT_ID:
				return new GridGetRowMenuAction();
			case GridSetHideRowsOnInitAction.EVENT_ID:
				return new GridSetHideRowsOnInitAction();
			case FindObjectAction.EVENT_ID:
				return new FindObjectAction();
			case SetPanelStateAction.EVENT_ID:
				return new SetPanelStateAction();
			case SaveSettingsAction.EVENT_ID:
				return new SaveSettingsAction();
			case LogoffAction.EVENT_ID:
				return new LogoffAction();		
			case SelectObjectAction.EVENT_ID:
				return new SelectObjectAction();
			case ReloadAction.EVENT_ID:
				return new ReloadAction();
			case SelectAndEditObjectAction.EVENT_ID:
				return new SelectAndEditObjectAction();
			case SelectAndNewObjectAction.EVENT_ID:
				return new SelectAndNewObjectAction();
			case EditAction.EVENT_ID:
				return new EditAction();
			case NewObjectAction.EVENT_ID:
				return new NewObjectAction();
			case SelectLocaleAction.EVENT_ID:
				return new SelectLocaleAction();
			case DeleteAction.EVENT_ID:
				return new DeleteAction();
			case MultiDeleteAction.EVENT_ID:
				return new MultiDeleteAction();
			case InvokeOperationAction.EVENT_ID:
				return new InvokeOperationAction();
			case MacroAction.EVENT_ID:
				return new MacroAction();
			case ObjectGetAttributesAction.EVENT_ID:
				return new ObjectGetAttributesAction();
			case SaveGridAction.EVENT_ID:
				return new SaveGridAction();
			case SelectPerspectiveAction.EVENT_ID:
				return new SelectPerspectiveAction();
			case SelectViewportAction.EVENT_ID:
				return new SelectViewportAction();
			case GridMoveDownObjectAction.EVENT_ID:
				return new GridMoveDownObjectAction();				
			case GridMoveUpObjectAction.EVENT_ID:
				return new GridMoveUpObjectAction();
			case GetOperationDialogAction.EVENT_ID:
				return new GetOperationDialogAction();
			case EditAsNewAction.EVENT_ID:
				return new EditAsNewAction();
			case CreateAction.EVENT_ID:
				return new CreateAction();
			default:
				return null;
		}
	}

}
