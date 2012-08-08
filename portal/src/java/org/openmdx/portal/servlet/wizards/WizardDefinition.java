/*
 * ====================================================================
 * Project:     openMDX/Portal, http://www.openmdx.org/
 * Name:        $Id: WizardDefinition.java,v 1.13 2008/04/04 11:51:24 hburger Exp $
 * Description: WizardDefinition 
 * Revision:    $Revision: 1.13 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/04 11:51:24 $
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 *
 * This product includes yui, the Yahoo! UI Library
 * (License - based on BSD).
 *
 * This product includes yui-ext, the yui extension
 * developed by Jack Slocum (License - based on BSD).
 * 
 */

package org.openmdx.portal.servlet.wizards;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.openmdx.base.exception.ServiceException;

public class WizardDefinition
    implements Serializable {
    
    //-----------------------------------------------------------------------
    public WizardDefinition(
        String name,
        String locale,
        short index,
        InputStream is
    ) throws ServiceException {

        this.name = name;
        this.locale = locale;
        this.index = index;

        // Wizard names are of the form 
        // wizards/<locale>/[<class name>-]<wizard name>.ext
        // Extract the type from the name
        String s = name.substring(name.lastIndexOf("/") + 1);
        // As default take file name as forClass
        this.forClass = new ArrayList<String>();
        this.forClass.add(
            s.indexOf("-") > 0
                ? s.substring(0, s.indexOf("-")).replace('.', ':')
                : "org:openmdx:base:Void"
        );
        this.label = null;
        this.toolTip = null;
        this.targetType = "";
        this.openParameter = "";
        this.order = new ArrayList<String>();
        this.order.add("0");
    }
    
    //-----------------------------------------------------------------------
    public short getLocaleIndex(
    ) {
        return this.index;
    }
  
    //-----------------------------------------------------------------------
    public String getLocale(
    ) {
        return this.locale;
    }
      
    //-----------------------------------------------------------------------
    public String getName(
    ) {
        return this.name;
    }

    //-----------------------------------------------------------------------
    public List getForClass(
    ) {
        return this.forClass;
    }

    //-----------------------------------------------------------------------
    public String getTargetType(
    ) {
        return this.targetType;
    }

    //-----------------------------------------------------------------------
    public String getLabel(
    ) {
        return this.label;
    }

    //-----------------------------------------------------------------------
    public String getToolTip(
    ) {
        return this.toolTip;
    }

    //-----------------------------------------------------------------------
    public List getOrder(
    ) {
        return this.order;
    }

    //-----------------------------------------------------------------------
    public String getOpenParameter(
    ) {
        return this.openParameter;
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static final long serialVersionUID = 1734820694768521769L;

    protected final String locale;
    protected final short index;
    protected final String name;
    // Wizard is applied for the specified class
    protected List<String> forClass;
    protected String label;
    protected String toolTip;
    protected String targetType;
    protected String openParameter;
    protected List<String> order;
    
}

//--- End of File -----------------------------------------------------------
