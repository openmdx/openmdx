/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DataproviderReply.java,v 1.8 2008/09/09 14:20:01 hburger Exp $
 * Description: DataproviderReply class
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/09 14:20:01 $
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.resource.ResourceException;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.resource.Records;

@SuppressWarnings("unchecked")
public class DataproviderReply
    extends DataproviderContext
{

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 4120848863151075378L;

    /**
     * Reply to successfull requests returning no objects
     */
    public DataproviderReply(
    ){
        this.objects = new DataproviderObject[]{};
    }

    /**
     * Reply to successfull requests returning a single object
     */
    public DataproviderReply(
        DataproviderObject object
    ){
        this.objects = new DataproviderObject[]{
            object
        };
    }

    /**
     * Reply to successfull requests returning a collection of objects
     */
    public DataproviderReply(
        List objects
    ){
        this.objects = (DataproviderObject[])objects.toArray(
            new DataproviderObject[objects.size()]
        );
    }

    /**
     * Constructor for clone()
     * 
     * @param that
     */
    private DataproviderReply(
        DataproviderReply that
    ){
        super(that);
        this.objects = that.objects;
    }
    
    
    //------------------------------------------------------------------------
    // Accessors
    //------------------------------------------------------------------------

    /**
     * Get the objects list
     *
     * @return      a list containing objects of class DataproviderObject
     */
    public DataproviderObject[] getObjects(
    ){
        return this.objects;
    }

    /**
     * Get the object
     *
     * @return      the dataprovider object
     */
    public DataproviderObject getObject(
    ){
        return this.objects[0];
    }

                
    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException {
        return new DataproviderReply(this);
    }


    //------------------------------------------------------------------------
    // Extends DataproviderContext
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.cci.DataproviderContext#keys()
     */
    protected Collection keys() {
        return KEYS;
    }


    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key) {
        try {
            return "objects".equals(key) ? Records.getRecordFactory().asIndexedRecord(
                "list", null, getObjects()
            ) : super.get(key);
        } catch (ResourceException e) {
            throw new RuntimeServiceException(e);
        }
    }

    //------------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------------
    private DataproviderObject[] objects;

    private static final List<String> KEYS = Collections.unmodifiableList(
        Arrays.asList(
            "objects",
            "contexts"
        )
    );
    
}

