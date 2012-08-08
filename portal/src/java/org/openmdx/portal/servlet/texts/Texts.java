/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: Texts.java,v 1.11 2007/05/22 12:04:15 wfro Exp $
 * Description: Texts
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/05/22 12:04:15 $
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
 * This product includes yui-ext, the yui extension
 * developed by Jack Slocum (License - based on BSD).
 * 
 */
package org.openmdx.portal.servlet.texts;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Properties;

import org.openmdx.base.exception.ServiceException;

public class Texts
  implements Texts_1_0, Serializable {

public Texts(
    String locale,
    short index,
    List is
  ) throws ServiceException {
    this.locale = locale;
    this.index = index;
    try {
      this.properties = new Properties();
      for(int i = 0; i < is.size(); i++) {
          this.properties.load((InputStream)is.get(i));
      }
    }
    catch(IOException e) {
      throw new ServiceException(e);
    }
  }
  
  public short getLocaleIndex(
  ) {
    return this.index;
  }
  
  public String getLocale(
  ) {
    return this.locale;
  }
  
  public String getDir(
  ) {
    return this.properties.getProperty("dir");
  }
    
  public String getLocaleTitle(
  ) {
    return this.properties.getProperty("LocaleTitle");
  }
    
  public String getCancelTitle() {
    return this.properties.getProperty("CancelTitle");
  }
  
  public String getOkTitle() {
      return this.properties.getProperty("OkTitle");
  }

  public String getSaveTitle() {
    return this.properties.getProperty("SaveTitle");
  }
  
  public String getSortAscendingText() {
    return this.properties.getProperty("SortAscendingText");
  }

  public String getSortDescendingText() {
    return this.properties.getProperty("SortDescendingText");
  }

  public String getDisableSortText() {
    return this.properties.getProperty("DisableSortText");
  }

  public String getDeleteTitle() {
    return this.properties.getProperty("DeleteTitle");
  }

  public String getEditTitle() {
    return this.properties.getProperty("EditTitle");
  }
  
  public String getViewTitle() {
    return this.properties.getProperty("ViewTitle");      
  }
  
  public String getShowDetailsTitle() {
    return this.properties.getProperty("ShowDetailsTitle");      
  }
  
  public String getWideGridLayoutText() {
    return this.properties.getProperty("WideGridLayoutText");
  }

  public String getNarrowGridLayoutText() {
    return this.properties.getProperty("NarrowGridLayoutText");
  }
  
  public String getAdvancedSearchText() {
    return this.properties.getProperty("AdvancedSearchText");
  }

  public String getReloadText() {
    return this.properties.getProperty("ReloadText");
  }

  public String getSelectAllText() {
    return this.properties.getProperty("SelectAllText");
  }
  
  public String getHistoryText() {
    return this.properties.getProperty("HistoryText");
  }

  public String getPageRequiresScriptText() {
    return this.properties.getProperty("PageRequiresScriptText");
  }
  
  public String getAssertExecutionText() {
    return this.properties.getProperty("AssertExecutionText");
  }

  public String getYesText() {
    return this.properties.getProperty("YesText");
  }
  
  public String getNoText() {
    return this.properties.getProperty("NoText");
  }

  public String getNewText() {
    return this.properties.getProperty("NewText");
  }

  public String getQualifierText(
  ) {
    return this.properties.getProperty("QualifierText");
  }
 
  public String getNavigateToParentText(
  ) {
    return this.properties.getProperty("NavigateToParentText");
  }

  public String getTrueText(
  ) {
    return this.properties.getProperty("TrueText");
  }
  
  public String getFalseText(
  ) {
    return this.properties.getProperty("FalseText");
  }

  public String getErrorTextCanNotEditNumber(
  ) {
    return this.properties.getProperty("ErrorTextCanNotEditNumber");
  }
  
  public String getErrorTextCanNotEditDate(
  ) {
    return this.properties.getProperty("ErrorTextCanNotEditDate");
  }
  
  public String getErrorTextCanNotEditObjectReference(
  ) {
    return this.properties.getProperty("ErrorTextCanNotEditObjectReference");
  }
  
  public String getErrorTextInvalidObjectReference(
  ) {
    return this.properties.getProperty("ErrorTextInvalidObjectReference");
  }
  
  public String getErrorTextCanNotEditCode(
  ) {
    return this.properties.getProperty("ErrorTextCanNotEditCode");
  }
  
  public String getErrorTextAttributeTypeNotSupported(
  ) {
    return this.properties.getProperty("ErrorTextAttributeTypeNotSupported");
  }

  public String getErrorTextCanNotCreateOrEditObject(
  ) {
    return this.properties.getProperty("ErrorTextCanNotCreateOrEditObject");
    
  }
  public String getErrorTextCanNotLookupObject(
  ) {
    return this.properties.getProperty("ErrorTextCanNotLookupObject");
  }
  
  public String getErrorTextCannotRestrictView(
  ) {
    return this.properties.getProperty("ErrorTextCannotRestrictView");
  }

  public String getErrorTextCannotSelectObject(
  ) {
    return this.properties.getProperty("ErrorTextCannotSelectObject");
  }
  
  public String getErrorTextCannotEditObject(
  ) {
    return this.properties.getProperty("ErrorTextCannotEditObject");
  }
  
  public String getErrorTextCannotNavigate(
  ) {
    return this.properties.getProperty("ErrorTextCannotNavigate");
  }
  
  public String getErrorTextCannotSetLocale(
  ) {
    return this.properties.getProperty("ErrorTextCannotSetLocale");
  }
  
  public String getErrorTextCannotDeleteObject(
  ) {
    return this.properties.getProperty("ErrorTextCannotDeleteObject");
  }

  public String getErrorTextCanNotInvokeOperation(
  ) {
    return this.properties.getProperty("ErrorTextCanNotInvokeOperation");
  }
  
  public String getErrorTextCanNotSetOperationResult(
  ) {
    return this.properties.getProperty("ErrorTextCanNotSetOperationResult");
  }
  
  public String getErrorTextCanNotRetrieveReferencedObjects(
  ) {
    return this.properties.getProperty("ErrorTextCanNotRetrieveReferencedObjects");
  }
  
  public String getClickToDownloadText(
  ) {
    return this.properties.getProperty("ClickToDownloadText");
  }
  
  public String getEnterNullToDeleteText(
  ) {
    return this.properties.getProperty("EnterNullToDeleteText");
  }

  public String getSearchIncrementallyText(
  ) {
    return this.properties.getProperty("SearchIncrementallyText");
  }
  
  public String getSetCurrentAsDefaultText(
  ) {
      return this.properties.getProperty("SetCurrentAsDefaultText");      
  }

  public String getLogoffText(
  ) {
      return this.properties.getProperty("LogoffText");            
  }
  
  public String getSaveSettingsText(
  ) {
      return this.properties.getProperty("SaveSettingsText");            
  }

  public String getFavoritesText(
  ) {
      return this.properties.getProperty("FavoritesText");            
  }

  public String getSearchText(
  ) {
      return this.properties.getProperty("SearchText");      
  }
  
    public String getCloseText(
    ) {
        return this.properties.getProperty("CloseText");      
    }
  
    public String getAddObjectTitle(
    ) {
        return this.properties.getProperty("AddObjectTitle");      
    }
        
    public String getRemoveObjectTitle(
    ) {
        return this.properties.getProperty("RemoveObjectTitle");      
    }
        
    public String getAddFilterTitle(
    ) {
        return this.properties.getProperty("AddFilterTitle");      
    }
        
    public String getClassFilterTitle(
    ) {
        return this.properties.getProperty("ClassFilterTitle");      
    }
        
    public String getCloneTitle(
    ) {
        return this.properties.getProperty("CloneTitle");      
    }
        
    public String getShowRowsOnInitTitle(
    ) {
        return this.properties.getProperty("EnableDefaultFilterOnInitTitle");      
    }
        
    public String getHideRowsOnInitTitle(
    ) {
        return this.properties.getProperty("DisableDefaultFilterOnInitTitle");      
    }
        
    public String getSelectUserRoleText(
    ) {
        return this.properties.getProperty("SelectUserRoleText");      
    }
        
    public String getShowRowsText(
    ) {
        return this.properties.getProperty("ShowRowsText");      
    }

    public String getUserDefinedText(
        String key
    ) {
        return this.properties.getProperty(key);
    }
  
    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3905245628128114744L;
  
    private final String locale;
    private final short index;
    private final Properties properties;
  
}

//--- End of File -----------------------------------------------------------
