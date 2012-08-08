/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: Control.java,v 1.17 2007/01/21 20:46:18 wfro Exp $
 * Description: Control
 * Revision:    $Revision: 1.17 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/01/21 20:46:18 $
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
package org.openmdx.portal.servlet.control;

import java.io.Serializable;
import java.util.Locale;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.portal.servlet.HtmlPage;
import org.openmdx.portal.servlet.texts.Texts_1_0;

public abstract class Control
  implements Serializable {
  
  //-------------------------------------------------------------------------
  public Control(
      String id,
      String locale,
      int localeAsIndex,
      ControlFactory controlFactory
  ) {
      this.id = id;
      this.locale = locale;
      this.localeAsIndex = localeAsIndex;
      this.controlFactory = controlFactory;
  }

  //-------------------------------------------------------------------------
  protected Locale getCurrentLocale(
  ) {
      return new Locale(
          this.locale.substring(0, 2), 
          this.locale.substring(locale.indexOf("_") + 1)
      );      
  }

    //-------------------------------------------------------------------------
    public void setId(
        String id
    ) {
        this.id = id;
    }
    
    //-------------------------------------------------------------------------
    public String getId(
    ) {
        return this.id;
    }
    
    //-------------------------------------------------------------------------
    public String getPropertyName(
        String featureName,
        String type
    ) {
        return featureName.replace(':', '.') + "." + type;
    }
  
  //-------------------------------------------------------------------------
    public Texts_1_0 getTexts(
    ) {
        return this.controlFactory.getTextsFactory().getTexts(
            this.locale
        );
    }
    
  //-------------------------------------------------------------------------
  public abstract void paint(
      HtmlPage p,
      String frame,
      boolean forEditing
  ) throws ServiceException;
  
  //-------------------------------------------------------------------------
  public void paint(
      HtmlPage p,
      boolean forEditing
  ) throws ServiceException {
      this.paint(
          p,
          null,
          forEditing
      );
  }
  
  //-------------------------------------------------------------------------
  public ControlFactory getControlFactory(
  ) {
      return this.controlFactory;
  }
  
  //-------------------------------------------------------------------------
  protected String id;
  protected String locale;
  protected int localeAsIndex;
  protected final ControlFactory controlFactory;
  
}

//--- End of File -----------------------------------------------------------
