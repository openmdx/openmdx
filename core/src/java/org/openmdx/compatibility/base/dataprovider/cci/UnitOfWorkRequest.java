/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: UnitOfWorkRequest.java,v 1.12 2008/01/25 17:18:46 wfro Exp $
 * Description: UnitOfWorkRequest class
 * Revision:    $Revision: 1.12 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/25 17:18:46 $
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

import javax.resource.ResourceException;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.resource.Records;


public class UnitOfWorkRequest 
    extends DataproviderContext
{

    private static final long serialVersionUID = 3906366034620658742L;

    /**
     * Constructor
     *
     * @param       transactionalUnit
     *              Defines whether the working unit is a transactional unit;
     *              false means that it is either a part of a bigger
     *              transactional unit or a non-transactional unit
     * @param       requests
     *              This working unit's requests
     */
    public UnitOfWorkRequest(
        boolean transactionalUnit,
        DataproviderRequest[] requests
    ){
        this.requests = requests;
        this.transactionalUnit = transactionalUnit;     
    }

    /**
     * Constructor for clone()
     * 
     * @param that
     */
    private UnitOfWorkRequest(
        UnitOfWorkRequest that,
        DataproviderRequest[] requests
    ){
        super(that);
        this.transactionalUnit = that.transactionalUnit;
        this.requests = requests;
    }


    //------------------------------------------------------------------------
    // Members
    //------------------------------------------------------------------------

    /**
     * Get the objects list
     *
     * @return      a list of dataprovider requests
     */
    public DataproviderRequest[] getRequests(
    ){
        return this.requests;
    }

    /**
     * Defines whether the working unit is a transactional unit.
     *
     * @return      true unless it is either a part of a bigger
     *              transactional unit or a non-transactional unit
     */
    public boolean isTransactionalUnit(
    ){
        return this.transactionalUnit;
    }
    
    /**
     * Defines whether the working unit is a transactional unit.
     *
     * @param       transactionalUnit
     *              false means that it is either a part of a bigger
     *              transactional unit or a non-transactional unit
     */
    public void setTransactionalUnit(
        boolean transactionalUnit
    ){
        this.transactionalUnit = transactionalUnit;
    }
        
        
    //------------------------------------------------------------------------
    // Extends DataproviderContext
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.cci.DataproviderContext#keys()
     */
    protected Collection keys() {
        return Arrays.asList(new String[]{"requests","contexts"});
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key) {
        try {
            return "requests".equals(key) ? Records.getRecordFactory().asIndexedRecord(
                "list", null, getRequests()
            ) : super.get(key);
        } catch (ResourceException e) {
            throw new RuntimeServiceException(e);
        }
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#getRecordShortDescription()
     */
    public String getRecordShortDescription() {
        return isTransactionalUnit() ? 
            "transactional unit of work" : 
            "non-transactional unit of work or participant in unit of work";
    }


    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException {
        return new UnitOfWorkRequest(
            this,
            this.requests
        );
    }


    //------------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------------

    /**
     *
     */
    private final DataproviderRequest[] requests;

    /**
     *
     */
    private boolean transactionalUnit;

}

