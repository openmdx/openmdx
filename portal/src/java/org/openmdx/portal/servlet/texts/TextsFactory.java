/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: TextsFactory.java,v 1.6 2009/05/26 12:41:15 wfro Exp $
 * Description: TextsFactory
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/26 12:41:15 $
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

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openmdx.application.log.AppLog;
import org.openmdx.base.exception.ServiceException;

public class TextsFactory
  implements Serializable {
  
  //-------------------------------------------------------------------------
  public TextsFactory(
      String[] locales,
      List[] textsStreams
  ) {
      this.texts = new LinkedHashMap<String,Texts_1_0>();
      for(int i = 0; i < locales.length; i++) {
          if(locales[i] != null) {
	          try {
		          this.setTexts(
		              new Texts(
		                  locales[i],
		                  (short)i,
		                  textsStreams[i]
		              )
		          );
              }
		      catch(ServiceException e) {
		          AppLog.warning("can not load texts " + i);
		      }
          }
      }
      AppLog.info("loaded texts=" + this.texts.keySet());
  }
  
  //-------------------------------------------------------------------------
  public String[] getLocale(
  ) {
    return (String[])this.texts.keySet().toArray(new String[this.texts.size()]);    
  }
  
  //-------------------------------------------------------------------------
  public Texts_1_0[] getTexts(
  ) {
    return (Texts_1_0[])this.texts.values().toArray(new Texts_1_0[this.texts.size()]);    
  }
  
  //-------------------------------------------------------------------------
  public void setTexts(
    Texts_1_0 resource
  ) {
    this.texts.put(
      resource.getLocale(),
      resource
    );
  }
  
  //-------------------------------------------------------------------------
  public Texts_1_0 getTexts(
     String locale
  ) {
    return (Texts_1_0)this.texts.get(
      locale
    );
  }
  
  //-------------------------------------------------------------------------
  // Variables
  //-------------------------------------------------------------------------
  private static final long serialVersionUID = 3760559780497798706L;

  private final Map<String,Texts_1_0> texts;
  
}

//--- End of File -----------------------------------------------------------
