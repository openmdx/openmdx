/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: OperationResult.java,v 1.4 2005/02/21 13:10:11 hburger Exp $
 * Description: Dataprovider Adapter: Operation Result
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/02/21 13:10:11 $
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
package org.openmdx.compatibility.base.dataprovider.transport.adapter;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.resource.cci.MappedRecord;

import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;


class OperationResult 
    extends AbstractMap 
implements MappedRecord {

    /**
     * 
     */
    private static final long serialVersionUID = 3258134665209590321L;

    OperationResult(
        DataproviderObject_1_0 source
    ){
        this.source = source;
    }

    public void setRecordName(
        String recordName
    ){
        source.clearValues(SystemAttributes.OBJECT_CLASS).set(
            0,
            recordName
        );
    }
    
    public String getRecordName(
    ){
        return (String)source.values(
            SystemAttributes.OBJECT_CLASS
        ).get(0);
    }
    
    public void setRecordShortDescription(
        String recordShortDescription
    ){
        this.recordShortDescription = recordShortDescription;
    }
    
    public String getRecordShortDescription(
    ){
        return this.recordShortDescription;
    }
    
    public Object clone(
    ){
        return new OperationResult(this.source);
    }

    public int size(
    ){
        return getMapping().size();
    }
    
    public Set entrySet(
    ){
        return getMapping().entrySet();
    }

    private Map getMapping(
    ){
        if(this.mapping == null){
            this.mapping = new HashMap();
            for(
                Iterator i = source.attributeNames().iterator();
                i.hasNext();
            ){
                String key = (String)i.next();
                if(
                    !SystemAttributes.OBJECT_CLASS.equals(key)
                )this.mapping.put(
                    key,
                    source.getValues(key)
                );
            }
        }
        return this.mapping;
    }
            
    private final DataproviderObject_1_0 source;
    
    private String recordShortDescription = null;

    private Map mapping = null;
        
}
