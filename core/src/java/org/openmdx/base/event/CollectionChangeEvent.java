/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: CollectionChangeEvent.java,v 1.14 2008/09/09 14:32:16 hburger Exp $
 * Description: openMDX Collection Change Event
 * Revision:    $Revision: 1.14 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/09 14:32:16 $
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.event;

import java.beans.IndexedPropertyChangeEvent;

import javax.resource.ResourceException;

import org.openmdx.base.resource.Records;


/**
 * openMDX
 * Collection Change Event
 */
public class CollectionChangeEvent extends IndexedPropertyChangeEvent {

    /**
     * 
     */
    private static final long serialVersionUID = 3256719563629737013L;

    /**
     * Creates a new collection change event
     * 
     * @param source
     *        the bean that fired the event
     * @param propertyName
     *        the programmatic name of the property that was changed
     * @param oldValue
     *        the old value of the element
     * @param newValue
     *        the new value of the element
     * @param index
     *        the index of the element that was changed;
     *        of -1 if the index is unknown or an arbitrary number
     *        of elements is involved.
     * @param type
     *        the type of the change event
     */
    public CollectionChangeEvent(
        Object source,
        String propertyName,
        Object oldElement,
        Object newElement,
        int index,
        short type
    ) {
        super(source, propertyName, oldElement, newElement, index);
        this.type = type;
    }

    /**
     * Returns the type of this event.
     * 
     * @return the type of this event
     * 
     * @see #CLEAR
     * @see #ADD
     * @see #SET
     * @see #REMOVE
     */
    public short getType(
    ){
        return this.type;
    }

    /**
     * 
     */
    protected String getSourceDescription(
    ){
        Object source = getSource();
        return source == null ?
            null :
            source.getClass().getName() + '@' + Integer.toHexString(
                System.identityHashCode(getSource())
            );
    }

    /**
     * 
     */
    protected Integer getIndexDescription(
    ){
        int index = getIndex();
        return index == -1 ? null : new Integer(index);
    }

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
        try {
            return Records.getRecordFactory().asMappedRecord(
                getClass().getName(), // recordName
                null, // recordShortDescription, 
                TO_STRING_FIELDS,
                new Object[]{
                    getSourceDescription(),
                    getPropertyName(),
                    getIndexDescription(),
                    EVENT_TYPE[this.type],
                    getOldValue(),
                    getNewValue(),
                    getPropagationId()
                }
            ).toString();
        } catch (ResourceException exception) {
            return super.toString();
        }
    }


    //------------------------------------------------------------------------
    // Instance members
    //------------------------------------------------------------------------

    /**
     * The collection change event type.
     * 
     * @see #CLEAR
     * @see #ADD
     * @see #SET
     * @see #REMOVE
     */
    protected final short type;


    //------------------------------------------------------------------------
    // Constant Field Values
    //------------------------------------------------------------------------

    /**
     * Collection change event type to indicate that the collection is 
     * cleared. 
     */
    public static final short CLEAR = 0;

    /**
     * Collection change event type to indicate that a new element is added
     * to the collection. 
     */
    public static final short ADD = 1;

    /**
     * Collection change event type for indicating that an element has been 
     * replaced. 
     */
    public static final short SET = 2;

    /**
     * Collection change event type for indicating that an element has been 
     * removed. 
     */
    public static final short REMOVE = 3;

    /**
     * 
     */
    private static final String[] EVENT_TYPE = {
        "clear", //CLEAR
        "add", // ADD
        "set", // SET
        "remove" // REMOVE
    };

    private final static String[] TO_STRING_FIELDS = {
        "object",
        "property",
        "index",
        "type",
        "oldValue",
        "newValue",
        "propagationId"
    };
    
}
