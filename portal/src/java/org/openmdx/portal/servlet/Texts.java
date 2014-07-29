/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Description: TextsFactory
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
package org.openmdx.portal.servlet;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.log.SysLog;

/**
 * Texts
 *
 */
public class Texts implements Serializable {
  
	public class TextsBundle implements Texts_1_0, Serializable {

		/**
		 * Constructor 
		 *
		 * @param locale
		 * @param localeIndex
		 * @param resourceStreams
		 * @throws ServiceException
		 */
		public TextsBundle(
			String locale,
			short localeIndex,
			ResourceBundle defaultBundle
		) throws ServiceException {
			this.locale = locale;
			this.localeIndex = localeIndex;
			this.defaultBundle = defaultBundle;
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getLocaleIndex()
		 */
		@Override
		public short getLocaleIndex(
			) {
			return this.localeIndex;
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getLocale()
		 */
		@Override
		public String getLocale(
			) {
			return this.locale;
		}

		/**
		 * Get object with given key. First try to get object from codes. If it does
		 * not exist fall back to default bundle.
		 * 
		 * @param key
		 * @return
		 */
		protected String getObject(
			String key
		) {
			String object = null;
			if(Texts.this.codes != null) {
                Map<String,Short> shortTexts = Texts.this.codes.getShortTextByText(
					TextsBundle.class.getSimpleName(), 
					this.localeIndex, 
					true // includeAll
				);
				if(shortTexts != null && shortTexts.get(key) != null) {
                    Map<Short,String> longTexts = Texts.this.codes.getLongTextByCode(
						TextsBundle.class.getSimpleName(), 
						this.localeIndex, 
						true // includeAll
					);
					object = longTexts.get(shortTexts.get(key));
				}
			}
			return object != null ? object :
				(String)this.defaultBundle.getObject(key);
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getDir()
		 */
		@Override
		public String getDir(
		) {
			return this.getObject("dir");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getLocaleTitle()
		 */
		@Override
		public String getLocaleTitle(
		) {
			return this.getObject("LocaleTitle");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getCancelTitle()
		 */
		@Override
		public String getCancelTitle(
		) {
			return this.getObject("CancelTitle");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getOkTitle()
		 */
		@Override
		public String getOkTitle(
		) {
			return this.getObject("OkTitle");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getSaveTitle()
		 */
		@Override
		public String getSaveTitle(
		) {
			return this.getObject("SaveTitle");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getSortAscendingText()
		 */
		@Override
		public String getSortAscendingText(
		) {
			return this.getObject("SortAscendingText");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getSortDescendingText()
		 */
		@Override
		public String getSortDescendingText(
		) {
			return this.getObject("SortDescendingText");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getDisableSortText()
		 */
		@Override
		public String getDisableSortText(
		) {
			return this.getObject("DisableSortText");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getDeleteTitle()
		 */
		@Override
		public String getDeleteTitle(
		) {
			return this.getObject("DeleteTitle");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getEditTitle()
		 */
		@Override
		public String getEditTitle(
		) {
			return this.getObject("EditTitle");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getViewTitle()
		 */
		@Override
		public String getViewTitle(
		) {
			return this.getObject("ViewTitle");      
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getShowDetailsTitle()
		 */
		@Override
		public String getShowDetailsTitle(
		) {
			return this.getObject("ShowDetailsTitle");      
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getWideGridLayoutText()
		 */
		@Override
		public String getWideGridLayoutText(
		) {
			return this.getObject("WideGridLayoutText");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getNarrowGridLayoutText()
		 */
		@Override
		public String getNarrowGridLayoutText(
			) {
			return this.getObject("NarrowGridLayoutText");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getAdvancedSearchText()
		 */
		@Override
		public String getAdvancedSearchText(
		) {
			return this.getObject("AdvancedSearchText");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getReloadText()
		 */
		@Override
		public String getReloadText() {
			return this.getObject("ReloadText");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getSelectAllText()
		 */
		@Override
		public String getSelectAllText(
		) {
			return this.getObject("SelectAllText");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getHistoryText()
		 */
		@Override
		public String getHistoryText(
		) {
			return this.getObject("HistoryText");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getPageRequiresScriptText()
		 */
		@Override
		public String getPageRequiresScriptText(
		) {
			return this.getObject("PageRequiresScriptText");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getAssertExecutionText()
		 */
		@Override
		public String getAssertExecutionText(
		) {
			return this.getObject("AssertExecutionText");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getYesText()
		 */
		@Override
		public String getYesText(
		) {
			return this.getObject("YesText");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getNoText()
		 */
		@Override
		public String getNoText(
		) {
			return this.getObject("NoText");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getNewText()
		 */
		@Override
		public String getNewText(
		) {
			return this.getObject("NewText");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getQualifierText()
		 */
		@Override
		public String getQualifierText(
		) {
			return this.getObject("QualifierText");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getNavigateToParentText()
		 */
		@Override
		public String getNavigateToParentText(
		) {
			return this.getObject("NavigateToParentText");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getTrueText()
		 */
		@Override
		public String getTrueText(
		) {
			return this.getObject("TrueText");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getFalseText()
		 */
		@Override
		public String getFalseText(
		) {
			return this.getObject("FalseText");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getErrorTextCanNotEditNumber()
		 */
		@Override
		public String getErrorTextCanNotEditNumber(
		) {
			return this.getObject("ErrorTextCanNotEditNumber");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getErrorTextMandatoryField()
		 */
		@Override
		public String getErrorTextMandatoryField(
		) {
			return this.getObject("ErrorTextMandatoryField");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getErrorTextCanNotEditDate()
		 */
		@Override
		public String getErrorTextCanNotEditDate(
		) {
			return this.getObject("ErrorTextCanNotEditDate");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getErrorTextCanNotEditObjectReference()
		 */
		@Override
		public String getErrorTextCanNotEditObjectReference(
		) {
			return this.getObject("ErrorTextCanNotEditObjectReference");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getErrorTextInvalidObjectReference()
		 */
		@Override
		public String getErrorTextInvalidObjectReference(
		) {
			return this.getObject("ErrorTextInvalidObjectReference");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getErrorTextCanNotEditCode()
		 */
		@Override
		public String getErrorTextCanNotEditCode(
		) {
			return this.getObject("ErrorTextCanNotEditCode");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getErrorTextAttributeTypeNotSupported()
		 */
		@Override
		public String getErrorTextAttributeTypeNotSupported(
		) {
			return this.getObject("ErrorTextAttributeTypeNotSupported");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getErrorTextCanNotCreateOrEditObject()
		 */
		@Override
		public String getErrorTextCanNotCreateOrEditObject(
		) {
			return this.getObject("ErrorTextCanNotCreateOrEditObject");  
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getErrorTextCanNotLookupObject()
		 */
		@Override
		public String getErrorTextCanNotLookupObject(
		) {
			return this.getObject("ErrorTextCanNotLookupObject");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getErrorTextCannotRestrictView()
		 */
		@Override
		public String getErrorTextCannotRestrictView(
		) {
			return this.getObject("ErrorTextCannotRestrictView");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getErrorTextCannotSelectObject()
		 */
		@Override
		public String getErrorTextCannotSelectObject(
		) {
			return this.getObject("ErrorTextCannotSelectObject");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getErrorTextCannotEditObject()
		 */
		@Override
		public String getErrorTextCannotEditObject(
		) {
			return this.getObject("ErrorTextCannotEditObject");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getErrorTextCannotNavigate()
		 */
		@Override
		public String getErrorTextCannotNavigate(
		) {
			return this.getObject("ErrorTextCannotNavigate");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getErrorTextCannotSetLocale()
		 */
		@Override
		public String getErrorTextCannotSetLocale(
		) {
			return this.getObject("ErrorTextCannotSetLocale");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getErrorTextCannotSetPerspective()
		 */
		@Override
		public String getErrorTextCannotSetPerspective(
		) {
			return this.getObject("ErrorTextCannotSetPerspective");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getErrorTextCannotDeleteObject()
		 */
		@Override
		public String getErrorTextCannotDeleteObject(
		) {
			return this.getObject("ErrorTextCannotDeleteObject");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getErrorTextCanNotInvokeOperation()
		 */
		@Override
		public String getErrorTextCanNotInvokeOperation(
		) {
			return this.getObject("ErrorTextCanNotInvokeOperation");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getErrorTextCanNotSetOperationResult()
		 */
		@Override
		public String getErrorTextCanNotSetOperationResult(
		) {
			return this.getObject("ErrorTextCanNotSetOperationResult");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getErrorTextCanNotRetrieveReferencedObjects()
		 */
		@Override
		public String getErrorTextCanNotRetrieveReferencedObjects(
		) {
			return this.getObject("ErrorTextCanNotRetrieveReferencedObjects");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getClickToDownloadText()
		 */
		@Override
		public String getClickToDownloadText(
		) {
			return this.getObject("ClickToDownloadText");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getEnterNullToDeleteText()
		 */
		@Override
		public String getEnterNullToDeleteText(
		) {
			return this.getObject("EnterNullToDeleteText");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getSearchIncrementallyText()
		 */
		@Override
		public String getSearchIncrementallyText(
		) {
			return this.getObject("SearchIncrementallyText");
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getSetCurrentAsDefaultText()
		 */
		@Override
		public String getSetCurrentAsDefaultText(
		) {
			return this.getObject("SetCurrentAsDefaultText");      
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getLogoffText()
		 */
		@Override
		public String getLogoffText(
		) {
			return this.getObject("LogoffText");            
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getSaveSettingsText()
		 */
		@Override
		public String getSaveSettingsText(
		) {
			return this.getObject("SaveSettingsText");            
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getFavoritesText()
		 */
		@Override
		public String getFavoritesText(
		) {
			return this.getObject("FavoritesText");            
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getSearchText()
		 */
		@Override
		public String getSearchText(
		) {
			return this.getObject("SearchText");      
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getCloseText()
		 */
		@Override
		public String getCloseText(
		) {
			return this.getObject("CloseText");      
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getAddObjectTitle()
		 */
		@Override
		public String getAddObjectTitle(
		) {
			return this.getObject("AddObjectTitle");      
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getRemoveObjectTitle()
		 */
		@Override
		public String getRemoveObjectTitle(
		) {
			return this.getObject("RemoveObjectTitle");      
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getMoveUpObjectTitle()
		 */
		@Override
		public String getMoveUpObjectTitle(
		) {
			return this.getObject("MoveUpObjectTitle");      
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getMoveDownObjectTitle()
		 */
		@Override
		public String getMoveDownObjectTitle(
		) {
			return this.getObject("MoveDownObjectTitle");      
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getAddFilterTitle()
		 */
		@Override
		public String getAddFilterTitle(
		) {
			return this.getObject("AddFilterTitle");      
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getClassFilterTitle()
		 */
		@Override
		public String getClassFilterTitle(
		) {
			return this.getObject("ClassFilterTitle");      
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getCloneTitle()
		 */
		@Override
		public String getCloneTitle(
		) {
			return this.getObject("CloneTitle");      
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getShowRowsOnInitTitle()
		 */
		@Override
		public String getShowRowsOnInitTitle(
		) {
			return this.getObject("EnableDefaultFilterOnInitTitle");      
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getHideRowsOnInitTitle()
		 */
		@Override
		public String getHideRowsOnInitTitle(
		) {
			return this.getObject("DisableDefaultFilterOnInitTitle");      
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getSelectUserRoleText()
		 */
		@Override
		public String getSelectUserRoleText(
		) {
			return this.getObject("SelectUserRoleText");      
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getShowRowsText()
		 */
		@Override
		public String getShowRowsText(
		) {
			return this.getObject("ShowRowsText");      
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getExploreText()
		 */
		@Override
		public String getExploreText(
		) {
			return this.getObject("ExploreText");      
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getShowHeaderTitle()
		 */
		@Override
		public String getShowHeaderTitle(
		) {
			return this.getObject("ShowHeaderTitle");              
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getHideHeaderTitle()
		 */
		@Override
		public String getHideHeaderTitle(
		) {
			return this.getObject("HideHeaderTitle");              
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getUserDefinedText(java.lang.String)
		 */
		@Override
		public String getUserDefinedText(
			String key
		) {
			return this.getObject(key);
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getShowTitle()
		 */
		@Override
		public String getShowTitle(
		) {
			return this.getObject("ShowTitle");      
		}    

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getWorkspacesTitle()
		 */
		@Override
		public String getWorkspacesTitle(
		) {
			return this.getObject("WorkspacesTitle");      
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.texts.Texts_1_0#getActionsTitle()
		 */
		@Override
		public String getActionsTitle(
		) {
			return this.getObject("ActionsTitle");  
		}

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.Texts_1_0#getOpenTitle()
		 */
        @Override
        public String getOpenTitle() {
			return this.getObject("OpenTitle");  
        }

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.Texts_1_0#getQuitTitle()
		 */
        @Override
        public String getQuitTitle() {
			return this.getObject("QuitTitle");  
        }

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.Texts_1_0#getExitTitle()
		 */
        @Override
        public String getExitTitle() {
			return this.getObject("ExitTitle");  
        }

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.Texts_1_0#getSaveAsTitle()
		 */
        @Override
        public String getSaveAsTitle() {
			return this.getObject("SaveAsTitle");  
        }

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.Texts_1_0#getAcceptTitle()
		 */
        @Override
        public String getAcceptTitle() {
			return this.getObject("AcceptTitle");  
        }

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.Texts_1_0#getDeclineTitle()
		 */
        @Override
        public String getDeclineTitle() {
			return this.getObject("DeclineTitle");  
        }

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.Texts_1_0#getRepeatTitle()
		 */
        @Override
        public String getRepeatTitle() {
			return this.getObject("RepeatTitle");  
        }

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.Texts_1_0#getRetryTitle()
		 */
        @Override
        public String getRetryTitle() {
			return this.getObject("RetryTitle");  
        }

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.Texts_1_0#getNextTitle()
		 */
        @Override
        public String getNextTitle() {
			return this.getObject("NextTitle");  
        }

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.Texts_1_0#getPreviousTitle()
		 */
        @Override
        public String getPreviousTitle() {
			return this.getObject("PreviousTitle");  
        }

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.Texts_1_0#getForwardTitle()
		 */
        @Override
        public String getForwardTitle() {
			return this.getObject("ForwardTitle");  
        }

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.Texts_1_0#getBackTitle()
		 */
        @Override
        public String getBackTitle() {
			return this.getObject("BackTitle");  
        }

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.Texts_1_0#getContinueTitle()
		 */
        @Override
        public String getContinueTitle() {
			return this.getObject("ContinueTitle");  
        }

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.Texts_1_0#getResetTitle()
		 */
        @Override
        public String getResetTitle() {
			return this.getObject("ResetTitle");  
        }

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.Texts_1_0#getCompleteTitle()
		 */
        @Override
        public String getCompleteTitle() {
			return this.getObject("CompleteTitle");  
        }

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.Texts_1_0#getFinishTitle()
		 */
        @Override
        public String getFinishTitle() {
			return this.getObject("FinishTitle");  
        }

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.Texts_1_0#getExpandTitle()
		 */
        @Override
        public String getExpandTitle() {
			return this.getObject("ExpandTitle");  
        }

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.Texts_1_0#getCollapseTitle()
		 */
        @Override
        public String getCollapseTitle() {
			return this.getObject("CollapseTitle");  
        }

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.Texts_1_0#getSelectExistingText()
		 */
        @Override
        public String getSelectExistingText() {
			return this.getObject("SelectExistingText");  
        }

		/* (non-Javadoc)
		 * @see org.openmdx.portal.servlet.Texts_1_0#getWizardsMenuTitle()
		 */
		@Override
		public String getWizardsMenuTitle(
		) {
			return this.getObject("WizardsMenuTitle");  
		}

		//-------------------------------------------------------------------------
		// Variables
		//-------------------------------------------------------------------------
		private static final long serialVersionUID = 3905245628128114744L;

		private final String locale;
		private final short localeIndex;
		private final ResourceBundle defaultBundle;
	}

	/**
	 * Constructor 
	 *
	 * @param locales
	 * @param defaultBundles
	 * @param codes texts are retrieved from codes. If codes is null or code is not defined then fall back to default bundles.
	 */
	public Texts(
		String[] locales,
		List<ResourceBundle> defaultBundles,
		Codes codes
	) {
		this.textsBundles = new LinkedHashMap<String,Texts_1_0>();
		this.codes = codes;
		for(int i = 0; i < locales.length; i++) {
			if(locales[i] != null) {
				try {
					this.textsBundles.put(
						locales[i],
						new TextsBundle(
							locales[i],
							(short)i,
							defaultBundles.get(i)
						)
					);
				}
				catch(ServiceException e) {
					SysLog.warning("can not load texts " + i);
				}
			}
		}
		SysLog.info("Loaded texts", this.textsBundles.keySet());
	}

	/**
	 * Get available locales.
	 * @return
	 */
	public String[] getLocale(
	) {
		return (String[])this.textsBundles.keySet().toArray(new String[this.textsBundles.size()]);    
	}
  
	/**
	 * Get available text resources.
	 * @return
	 */
	public Texts_1_0[] getTextsBundles(
	) {
		return this.textsBundles.values().toArray(new Texts_1_0[this.textsBundles.size()]);    
	}

	/**
	 * Get texts resource for given locale.
	 * @param locale
	 * @return
	 */
	public Texts_1_0 getTextsBundle(
		String locale
	) {
		return this.textsBundles.get(
			locale
		);
	}
 
	//-------------------------------------------------------------------------
	// Variables
	//-------------------------------------------------------------------------
	private static final long serialVersionUID = 3760559780497798706L;

	private final Map<String,Texts_1_0> textsBundles;
	protected final Codes codes;
  
}

//--- End of File -----------------------------------------------------------
