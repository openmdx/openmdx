/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: Texts.java,v 1.20 2011/08/26 11:57:59 wfro Exp $
 * Description: Texts
 * Revision:    $Revision: 1.20 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/08/26 11:57:59 $
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
  
	@Override
	public short getLocaleIndex(
	) {
		return this.index;
	}
  
	@Override
	public String getLocale(
	) {
		return this.locale;
	}
  
  @Override
  public String getDir(
  ) {
    return this.properties.getProperty("dir");
  }
    
  @Override
  public String getLocaleTitle(
  ) {
    return this.properties.getProperty("LocaleTitle");
  }
    
  @Override
  public String getCancelTitle() {
    return this.properties.getProperty("CancelTitle");
  }
  
  @Override
  public String getOkTitle() {
      return this.properties.getProperty("OkTitle");
  }

  @Override
  public String getSaveTitle() {
    return this.properties.getProperty("SaveTitle");
  }
  
  @Override
  public String getSortAscendingText() {
    return this.properties.getProperty("SortAscendingText");
  }

  @Override
  public String getSortDescendingText() {
    return this.properties.getProperty("SortDescendingText");
  }

  @Override
  public String getDisableSortText() {
    return this.properties.getProperty("DisableSortText");
  }

  @Override
  public String getDeleteTitle() {
    return this.properties.getProperty("DeleteTitle");
  }

  @Override
  public String getEditTitle() {
    return this.properties.getProperty("EditTitle");
  }
  
  @Override
  public String getViewTitle() {
    return this.properties.getProperty("ViewTitle");      
  }
  
  @Override
  public String getShowDetailsTitle() {
    return this.properties.getProperty("ShowDetailsTitle");      
  }
  
  @Override
  public String getWideGridLayoutText() {
    return this.properties.getProperty("WideGridLayoutText");
  }

  @Override
  public String getNarrowGridLayoutText() {
    return this.properties.getProperty("NarrowGridLayoutText");
  }
  
  @Override
  public String getAdvancedSearchText() {
    return this.properties.getProperty("AdvancedSearchText");
  }

  @Override
  public String getReloadText() {
    return this.properties.getProperty("ReloadText");
  }

  @Override
  public String getSelectAllText() {
    return this.properties.getProperty("SelectAllText");
  }
  
  @Override
  public String getHistoryText() {
    return this.properties.getProperty("HistoryText");
  }

  @Override
  public String getPageRequiresScriptText() {
    return this.properties.getProperty("PageRequiresScriptText");
  }
  
  @Override
  public String getAssertExecutionText() {
    return this.properties.getProperty("AssertExecutionText");
  }

  @Override
  public String getYesText() {
    return this.properties.getProperty("YesText");
  }
  
  @Override
  public String getNoText() {
    return this.properties.getProperty("NoText");
  }

  @Override
  public String getNewText() {
    return this.properties.getProperty("NewText");
  }

  @Override
  public String getQualifierText(
  ) {
    return this.properties.getProperty("QualifierText");
  }
 
  @Override
  public String getNavigateToParentText(
  ) {
    return this.properties.getProperty("NavigateToParentText");
  }

  @Override
  public String getTrueText(
  ) {
    return this.properties.getProperty("TrueText");
  }
  
  @Override
  public String getFalseText(
  ) {
    return this.properties.getProperty("FalseText");
  }

  @Override
  public String getErrorTextCanNotEditNumber(
  ) {
    return this.properties.getProperty("ErrorTextCanNotEditNumber");
  }
  
  @Override
  public String getErrorTextMandatoryField(
  ) {
    return this.properties.getProperty("ErrorTextMandatoryField");
  }
  
  @Override
  public String getErrorTextCanNotEditDate(
  ) {
    return this.properties.getProperty("ErrorTextCanNotEditDate");
  }
  
  @Override
  public String getErrorTextCanNotEditObjectReference(
  ) {
    return this.properties.getProperty("ErrorTextCanNotEditObjectReference");
  }
  
  @Override
  public String getErrorTextInvalidObjectReference(
  ) {
    return this.properties.getProperty("ErrorTextInvalidObjectReference");
  }
  
  @Override
  public String getErrorTextCanNotEditCode(
  ) {
    return this.properties.getProperty("ErrorTextCanNotEditCode");
  }
  
  @Override
  public String getErrorTextAttributeTypeNotSupported(
  ) {
    return this.properties.getProperty("ErrorTextAttributeTypeNotSupported");
  }

  @Override
  public String getErrorTextCanNotCreateOrEditObject(
  ) {
    return this.properties.getProperty("ErrorTextCanNotCreateOrEditObject");  
  }

  @Override
  public String getErrorTextCanNotLookupObject(
  ) {
    return this.properties.getProperty("ErrorTextCanNotLookupObject");
  }
  
  @Override
  public String getErrorTextCannotRestrictView(
  ) {
    return this.properties.getProperty("ErrorTextCannotRestrictView");
  }

  @Override
  public String getErrorTextCannotSelectObject(
  ) {
    return this.properties.getProperty("ErrorTextCannotSelectObject");
  }
  
  @Override
  public String getErrorTextCannotEditObject(
  ) {
    return this.properties.getProperty("ErrorTextCannotEditObject");
  }
  
  @Override
  public String getErrorTextCannotNavigate(
  ) {
    return this.properties.getProperty("ErrorTextCannotNavigate");
  }
  
  @Override
  public String getErrorTextCannotSetLocale(
  ) {
    return this.properties.getProperty("ErrorTextCannotSetLocale");
  }
  
  @Override
  public String getErrorTextCannotSetPerspective(
  ) {
    return this.properties.getProperty("ErrorTextCannotSetPerspective");
  }
  
  @Override
  public String getErrorTextCannotDeleteObject(
  ) {
    return this.properties.getProperty("ErrorTextCannotDeleteObject");
  }

  @Override
  public String getErrorTextCanNotInvokeOperation(
  ) {
    return this.properties.getProperty("ErrorTextCanNotInvokeOperation");
  }
  
  @Override
  public String getErrorTextCanNotSetOperationResult(
  ) {
    return this.properties.getProperty("ErrorTextCanNotSetOperationResult");
  }
  
  @Override
  public String getErrorTextCanNotRetrieveReferencedObjects(
  ) {
    return this.properties.getProperty("ErrorTextCanNotRetrieveReferencedObjects");
  }
  
  @Override
  public String getClickToDownloadText(
  ) {
    return this.properties.getProperty("ClickToDownloadText");
  }
  
  @Override
  public String getEnterNullToDeleteText(
  ) {
    return this.properties.getProperty("EnterNullToDeleteText");
  }

  @Override
  public String getSearchIncrementallyText(
  ) {
    return this.properties.getProperty("SearchIncrementallyText");
  }
  
  @Override
  public String getSetCurrentAsDefaultText(
  ) {
      return this.properties.getProperty("SetCurrentAsDefaultText");      
  }

  	@Override
  	public String getLogoffText(
	) {
	    return this.properties.getProperty("LogoffText");            
	}
  
  	@Override
  	public String getSaveSettingsText(
  	) {
  		return this.properties.getProperty("SaveSettingsText");            
  	}
  
  	@Override
  	public String getFavoritesText(
  	) {
  		return this.properties.getProperty("FavoritesText");            
  	}

  	@Override
  	public String getSearchText(
  	) {
  		return this.properties.getProperty("SearchText");      
  	}
  
  	@Override
    public String getCloseText(
    ) {
        return this.properties.getProperty("CloseText");      
    }

    @Override
    public String getAddObjectTitle(
    ) {
        return this.properties.getProperty("AddObjectTitle");      
    }

    @Override
    public String getRemoveObjectTitle(
    ) {
        return this.properties.getProperty("RemoveObjectTitle");      
    }

    @Override
    public String getMoveUpObjectTitle(
    ) {
        return this.properties.getProperty("MoveUpObjectTitle");      
    }

    @Override
    public String getMoveDownObjectTitle(
    ) {
        return this.properties.getProperty("MoveDownObjectTitle");      
    }

    @Override
    public String getAddFilterTitle(
    ) {
        return this.properties.getProperty("AddFilterTitle");      
    }
        
    @Override
    public String getClassFilterTitle(
    ) {
        return this.properties.getProperty("ClassFilterTitle");      
    }
        
    @Override
    public String getCloneTitle(
    ) {
        return this.properties.getProperty("CloneTitle");      
    }
        
    @Override
    public String getShowRowsOnInitTitle(
    ) {
        return this.properties.getProperty("EnableDefaultFilterOnInitTitle");      
    }
        
    @Override
    public String getHideRowsOnInitTitle(
    ) {
        return this.properties.getProperty("DisableDefaultFilterOnInitTitle");      
    }
        
    @Override
    public String getSelectUserRoleText(
    ) {
        return this.properties.getProperty("SelectUserRoleText");      
    }
        
    @Override
    public String getShowRowsText(
    ) {
        return this.properties.getProperty("ShowRowsText");      
    }

    @Override
    public String getExploreText(
    ) {
        return this.properties.getProperty("ExploreText");      
    }
  
    @Override
    public String getShowHeaderTitle(
    ) {
        return this.properties.getProperty("ShowHeaderTitle");              
    }
    
    @Override
    public String getHideHeaderTitle(
    ) {
        return this.properties.getProperty("HideHeaderTitle");              
    }
    
    @Override
    public String getUserDefinedText(
        String key
    ) {
        return this.properties.getProperty(key);
    }
    
    @Override
    public String getShowTitle(
    ) {
        return this.properties.getProperty("ShowTitle");      
    }    

    @Override
    public String getWorkspacesTitle(
    ) {
        return this.properties.getProperty("WorkspacesTitle");      
    }

    @Override
    public String getActionsTitle(
    ) {
        return this.properties.getProperty("ActionsTitle");      
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
