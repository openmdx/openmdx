/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: InstanceCallbackEvent.java,v 1.7 2005/02/21 13:08:02 hburger Exp $
 * Description: openMDX: Instance Callback Event
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/02/21 13:08:02 $
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
package org.openmdx.base.event;

import java.beans.PropertyChangeListener;
import java.util.EventObject;

import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.kernel.text.format.IndentingFormatter;

/**
 * openMDX
 * Instance Callback Event
 */
public class InstanceCallbackEvent extends EventObject {

    /**
     * 
     */
    private static final long serialVersionUID = 3256442525421089842L;


    /**
     * Constructor
     * 
     * @param type
     * @param source
     *        The object on which the Event initially occurred.
     * @param target
     */
    public InstanceCallbackEvent(
        short type,
        Object source, 
        PropertyChangeListener target
    ) {
        super(source);
        this.type = type;
        this.target = target;
    }
    
    /**
     * Returns the type of this event.
     * 
     * @return the type of this event
     * 
     * @see #POST_LOAD
     * @see #PRE_STORE
     * @see #PRE_CLEAR
     * @see #PRE_DELETE
     */ 
    short getType(
    ){
        return this.type;
    }
    
    /**
     * 
     */
    PropertyChangeListener getTarget(
    ){
        return this.target;
    }
    
    //------------------------------------------------------------------------
    // Instance Members
    //------------------------------------------------------------------------

    /**
     * Contains the type of this event.
     * 
     * @see #POST_LOAD
     * @see #POST_RELOAD
     * @see #PRE_STORE
     * @see #PRE_CLEAR
     * @see #PRE_DELETE
     */
    protected final short type;
        
    /**
     * 
     */
    protected transient PropertyChangeListener target;
        
        
    //------------------------------------------------------------------------
    // Extends EventObject
    //------------------------------------------------------------------------

    /**
     * Returns a String representation of this EventObject.
     *
     * @return  A a String representation of this EventObject.
     */
    public String toString(
    ) {
        return getClass().getName() + IndentingFormatter.toString(
    		ArraysExtension.asMap(
                new String[]{
                    "type",
                    "object"
                }, 
                new Object[]{
            		EVENT_DESCRIPTION[this.type],
                    this.source
                }
        	)
        );
    }


    //------------------------------------------------------------------------
    // Constant Field Values
    //------------------------------------------------------------------------

    /**
     * 
     */
    private static final String[] EVENT_DESCRIPTION = new String[]{
        "post-load",
        "post-reload",
        "pre-clear",
        "pre-store",
        "pre-delete"
    };
    
    /**
     * Type of the event fired after the values are loaded from the data store into 
     * the Object_1_0 instance.
     */
    public static final short POST_LOAD = 0;

    /**
     * Type of the event fired after the values are loaded from the data store into 
     * the Object_1_0 instance.
     */
    public static final short POST_RELOAD = 1;

    /**
     * Type of the event fired  before the values in the instance are cleared. 
     */
    public static final short PRE_CLEAR = 2;

    /**
     * Type of the event fired before the values are stored from the Object_1_0 
     * instance. 
     */
    public static final short PRE_STORE = 3;

    /**
     * Type of the event fired before the instance is deleted. 
     */
    public static final short PRE_DELETE = 4;

}
