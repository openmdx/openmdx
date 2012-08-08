/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: RequestedObject.java,v 1.6 2005/02/21 13:10:34 hburger Exp $
 * Description: spice: dataprovider object proxy
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/02/21 13:10:34 $
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
package org.openmdx.compatibility.base.dataprovider.cci;

import java.io.Serializable;
import java.util.Set;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;


/**
 * This eimplementation delegates to the AbstractReply's first object. 
 */
public class RequestedObject
    implements DataproviderReplyListener, Serializable, DataproviderObject_1_0
{
        
    /**
     * 
     */
    private static final long serialVersionUID = 3257565088054654263L;

    /**
     *
     */
    protected DataproviderObject getObject(
    ){
        if (this.object == null) throw new RuntimeServiceException(
            this.exception == null ?
                new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.ILLEGAL_STATE,
                    null,
                    "The corresponding request has not been processed yet"
                ) :
                this.exception
        );
        return this.object;
    }


    //------------------------------------------------------------------------
    // Implements Dataprovider_1_0
    //------------------------------------------------------------------------
        
    /**
     * Returns the dataprovider object's path object.
     */
    public Path path(
    ){
        return getObject().path(); 
    }
    
    /**
     * Returns the attribute value list.
     *
     * @param       attributeName
     *              the attribute's name
     *
     * @return      the attribute value list; or null if no list with this
     *              name exists
     */
    public SparseList getValues(
        String attributeName
    ){
        return getObject().getValues(attributeName);
    }

    /**
     * Returns the existing or newly created attribute value list (optional
     * method).
     *
     * @param       attributeName
     *              the attribute's name
     *
     * @return      the attribute value list (never null)
     */
    public SparseList values(
        String attributeName
    ){
        return getObject().values(attributeName);
    }

    /**
     * Returns the existing and cleared or newly created attribute value list
     * (optional method).
     *
     * @param       attributeName
     *              the attribute's name
     *
     * @return      the attribute value list (never null)
     */
    public SparseList clearValues(
        String attributeName
    ){
        return getObject().clearValues(attributeName);
    }

    /**
     * Clear the dataprovider object's attributes (optional operation).
     */
    public void clear(
    ){
        getObject().clear();
    }
    
    /**
     * Get a set view of the dataprovider object's attribute names
     */
    public Set attributeNames(
    ){
        return getObject().attributeNames();
    }
        
    /**
     * Checks whether the dataprovider object contains an attribute with the
     * given name.
     */
    public boolean containsAttributeName(
        String attributeName
    ){
        return getObject().containsAttributeName(attributeName);
    }

    /**
     * Get the object's digest (which must not be modified!)
     */
    public byte[] getDigest(
    ){
        return getObject().getDigest();
    }

    /** 
     * Set the object's digest (optional operation)
     */
    public void setDigest(
        byte[] digest
    ){
        getObject().setDigest(digest);
    }
    

    //------------------------------------------------------------------------
    // Implements DataproviderReplyListener
    //------------------------------------------------------------------------

    /**
     * Called if the work unit has been processed successfully
     */
    public void onReply(
        DataproviderReply reply
    ){
        this.object = reply.getObject();
        this.exception = null;
    }
            
    /**
     * Called if the work unit processing failed
     */
    public void onException(
        ServiceException exception
    ){
        this.object = null;
        this.exception = exception;
    }

        
    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /**
     * Returns a string representation of the object. In general, the toString
     * method returns a string that "textually represents" this object. The
     * result should be a concise but informative representation that is easy
     * for a person to read. It is recommended that all subclasses override
     * this method. 
     *
     * @return the requested dataprovider object's string representation
     */
    public String toString(
    ){
        return this.object != null ? this.object.toString() :
            this.exception != null ? this.exception.toString() :
            "n/a";
    }

    
    //------------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------------

    /**
     * The object if the request succeeds; null otherwise.
     */
    protected DataproviderObject object = null;

    /**
     * The exception if the request fails; null otherwise.
     */
    protected ServiceException exception = null;

}
