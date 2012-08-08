/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: Texts_1_0.java,v 1.15 2009/02/27 15:52:52 wfro Exp $
 * Description: Texts_1_0
 * Revision:    $Revision: 1.15 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/27 15:52:52 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 *
 * This product includes yui, the Yahoo! UI Library
 * (License - based on BSD).
 *
 */
package org.openmdx.portal.servlet.texts;

public interface Texts_1_0 {
  
    public String getDir();
    public short getLocaleIndex();
    public String getLocale();
    public String getLocaleTitle();
    public String getCancelTitle();
    public String getOkTitle();
    public String getSaveTitle();
    public String getEditTitle();
    public String getViewTitle();
    public String getDeleteTitle();
    public String getShowDetailsTitle();
    public String getSortAscendingText();
    public String getSortDescendingText();
    public String getDisableSortText();
    public String getWideGridLayoutText();
    public String getNarrowGridLayoutText();
    public String getReloadText();
    public String getAdvancedSearchText();
    public String getSelectAllText();
    public String getHistoryText();
    public String getPageRequiresScriptText();
    public String getAssertExecutionText();
    public String getYesText();
    public String getNoText();
    public String getNewText();
    public String getQualifierText();
    public String getNavigateToParentText();
    public String getTrueText();
    public String getFalseText();
    public String getErrorTextCanNotEditNumber();
    public String getErrorTextCanNotEditDate();
    public String getErrorTextCanNotEditObjectReference();
    public String getErrorTextInvalidObjectReference();
    public String getErrorTextCanNotEditCode();
    public String getErrorTextAttributeTypeNotSupported();
    public String getErrorTextCanNotCreateOrEditObject();
    public String getErrorTextCanNotLookupObject();
    public String getErrorTextCannotRestrictView();
    public String getErrorTextCannotSelectObject();
    public String getErrorTextCannotEditObject();
    public String getErrorTextCannotNavigate();
    public String getErrorTextCannotSetLocale();
    public String getErrorTextCannotSetPerspective();
    public String getErrorTextCannotDeleteObject();
    public String getErrorTextCanNotInvokeOperation();
    public String getErrorTextCanNotSetOperationResult();
    public String getErrorTextCanNotRetrieveReferencedObjects();
    public String getErrorTextMandatoryField();
    public String getClickToDownloadText();
    public String getEnterNullToDeleteText();
    public String getSearchIncrementallyText();
    public String getSetCurrentAsDefaultText();
    public String getLogoffText();
    public String getSaveSettingsText();
    public String getFavoritesText();
    public String getUserDefinedText(String key);
    public String getSearchText();
    public String getCloseText();
    public String getAddObjectTitle();
    public String getRemoveObjectTitle();
    public String getAddFilterTitle();
    public String getClassFilterTitle();
    public String getCloneTitle();
    public String getSelectUserRoleText();
    public String getShowRowsOnInitTitle();
    public String getHideRowsOnInitTitle();
    public String getShowRowsText();
    public String getExploreText();
    public String getShowHeaderTitle();
    public String getHideHeaderTitle();
}

//--- End of File -----------------------------------------------------------
