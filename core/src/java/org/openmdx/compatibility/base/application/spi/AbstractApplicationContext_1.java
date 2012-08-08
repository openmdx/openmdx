/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractApplicationContext_1.java,v 1.5 2007/10/10 16:05:56 hburger Exp $
 * Description: WlsManager_1 class 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/10/10 16:05:56 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
 */
package org.openmdx.compatibility.base.application.spi;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.application.cci.ApplicationContext_1_0;
import org.openmdx.compatibility.base.application.cci.Manageable_1_0;
import org.openmdx.kernel.application.container.lightweight.LightweightContainer;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * The Application Context Singleton Holder
 */
public abstract class AbstractApplicationContext_1
    implements ApplicationContext_1_0, Manageable_1_0
{ 

    /**
     * Constructor
     * <p>
     * Remembers and registers the last created AbstractApplicationContext_1 instance.
     */
    protected AbstractApplicationContext_1(
    ){
        AbstractApplicationContext_1.instance = this;
        BasicException.setSource(this);
        SysLog.setLogSource(this);
    }

    /**
     * Get the most recently created AbstractApplicationContext_1 instance
     * 
     * @return the most recently created AbstractApplicationContext_1 instance
     */
    public static synchronized AbstractApplicationContext_1 getInstance(
    ){
        if(
            ! AbstractApplicationContext_1.hasInstance()
        ) try {
            AbstractApplicationContext_1.instance = LightweightContainer.hasInstance() ?
                (AbstractApplicationContext_1) new LightweightApplicationContext_1() :
                new DefaultApplicationContext_1();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AbstractApplicationContext_1.instance;
    }

    /**
     * Tells whether an AbstractApplicationContext_1 has been created
     * 
     * @return true if an AbstractApplicationContext_1 has been created
     */
    public static boolean hasInstance(){
        return AbstractApplicationContext_1.instance != null;
    }

    /**
     * 
     */
    private static AbstractApplicationContext_1 instance;
    

    //------------------------------------------------------------------------
    // Implements Manageable_1_0
    //------------------------------------------------------------------------

    /**
     * The activate method initializes a layer or component.
     * <p>
     * An activate() implementation of a subclass should be of the form:
     * <pre>
     *   {
     *     super.activate();
     *     \u00ablocal activation code\u00bb
     *   }
     * </pre>
     */
    public void activate (
    ) throws Exception, ServiceException {
        //
    }

    /**
     * The deactivate method releases a layer or component.
     * <p>
     * A deactivate() implementation of a subclass should be of the form:
     * of the form:
     * <pre>
     *   {
     *     \u00ablocal deactivation code\u00bb
     *     super.deactivate();
     *   }
     * </pre>
     */
    public void deactivate (
    ) throws Exception, ServiceException {
        //
    }

    
    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------
    
    /**
     * Cache the Application Context's String Representation
     */
    private String applicationContext = null;
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        if(this.applicationContext == null) this.applicationContext = getContainerId();
        return this.applicationContext;
    }

}
