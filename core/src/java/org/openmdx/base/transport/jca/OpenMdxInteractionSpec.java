/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: OpenMdxInteractionSpec.java,v 1.3 2007/03/07 09:26:37 wfro Exp $
 * Description: AbstractInteractionSpec
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/03/07 09:26:37 $
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
 */
package org.openmdx.base.transport.jca;

import javax.resource.cci.InteractionSpec;

/** 
 * An openMDX JCA interaction specification.
 * 
 * Concrete subclasses of <code>AbstractInteractionSpec</code> either take <code>IndexedRecord</code>
 * or <code>MappedRecord</code> as parameters.
 * 
 * Feature lists are <code>MappedRecord</code>s where the key is configuration dependent. Two key formats
 * are supported:
 * <ul>
 *   <li>field number of type <code>Integer</code>
 *   <li>field name of type <code>String</code>
 * </ul>
 * 
 * In case of asynchronous interactions, results are returned in the output record of an execution.
 *  
 */
public class OpenMdxInteractionSpec 
    implements InteractionSpec {
    
    //------------------------------------------------------------------------
    public OpenMdxInteractionSpec(
    ) {
        super();
    }
    
    //------------------------------------------------------------------------
    public OpenMdxInteractionSpec(
         OpenMdxInteractionSpec source
    ) {
        this.setFunctionName(
            source.getFunctionName()
        );
        this.setInteractionVerb(
            source.getInteractionVerb()
        );
        this.setObjectId(
            source.getObjectId()
        );
        this.setObjectId(
            source.getObjectId()
        );
        this.setDeletePersistent(
            source.getDeletePersistent()
        );
        this.setFetchSize(
            source.getFetchSize()
        );
        this.setRangeFrom(
            source.getRangeFrom()
        );
        this.setRangeTo(
            source.getRangeTo()
        );
        this.setOperationName(
            source.getOperationName()
        );
    }
    
    //------------------------------------------------------------------------
    public String getObjectId(
    ) {
        return this.objectId;
    }

    //------------------------------------------------------------------------
    public void setObjectId(
        String objectId
    ) {
        this.objectId = objectId;
    }

    //------------------------------------------------------------------------
    public String getFunctionName(
    ) {
        return this.functionName;
    }

    //------------------------------------------------------------------------
    public void setFunctionName(
        String functionName
    ) {
        this.functionName = functionName;
    }

    //------------------------------------------------------------------------
    public int getInteractionVerb(
    ) {
        return this.interactionVerb;
    }

    //------------------------------------------------------------------------
    public void setInteractionVerb(
        int interactionVerb
    ) {
        this.interactionVerb = interactionVerb;
    }

    //------------------------------------------------------------------------
    public void setDeletePersistent(
        boolean newValue
    ) {
        this.deletePersistent = newValue;
    }
    
    //------------------------------------------------------------------------
    public boolean getDeletePersistent(
    ) {
        return this.deletePersistent;
    }
    
    //------------------------------------------------------------------------
    public void setFetchSize(
        int newValue
    ) {
        this.fetchSize = newValue;
    }

    //------------------------------------------------------------------------
    public Integer getFetchSize(
    ) {
        return this.fetchSize;
    }

    //------------------------------------------------------------------------
    public void setRangeFrom(
        int newValue
    ) {
        this.rangeFrom = newValue;
    }

    //------------------------------------------------------------------------
    public Integer getRangeFrom(
    ) {
        return this.rangeFrom;
    }

    //------------------------------------------------------------------------
    public void setRangeTo(
        int newValue
    ) {
        this.rangeTo = newValue;
    }

    //------------------------------------------------------------------------
    public Integer getRangeTo(
    ) {
        return this.rangeTo;
    }
    
    //------------------------------------------------------------------------
    public void setOperationName(
        String newValue
    ) {
        this.operationName = newValue;
    }

    //------------------------------------------------------------------------
    public String getOperationName(
    ) {
        return this.operationName;
    }
    
    //-----------------------------------------------------------------------
    private static boolean objectsMatch(
        Object left,
        Object right
    ){
        return left == null ? right == null : left.equals(right);            
    }

    //-----------------------------------------------------------------------
    public boolean equals(
        Object candidate
    ) {
        if(this.getClass() != candidate.getClass()) return false;
        OpenMdxInteractionSpec that = (OpenMdxInteractionSpec) candidate;
        return 
             objectsMatch(this.getFunctionName(), that.getFunctionName()) &&
             this.getInteractionVerb() == that.getInteractionVerb() &&
             objectsMatch(this.getObjectId(), that.getObjectId()) &&
             this.deletePersistent == that.deletePersistent &&
             objectsMatch(this.fetchSize, that.fetchSize) &&
             objectsMatch(this.rangeFrom, that.rangeFrom) &&
             objectsMatch(this.rangeTo, that.rangeTo) &&
             objectsMatch(this.operationName, that.operationName);
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------    
    protected String functionName;
    protected int interactionVerb;
    protected String objectId;
    protected Boolean deletePersistent = null;
    protected Integer fetchSize = null;
    protected Integer rangeFrom = null;
    protected Integer rangeTo = null;
    protected String operationName;
                
}
