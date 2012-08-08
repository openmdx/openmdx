/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Marshaller.java,v 1.8 2008/06/17 10:32:09 hburger Exp $
 * Description: Dataprovider Adapter: Marshaller
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/17 10:32:09 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.compatibility.base.dataprovider.transport.adapter;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.Map.Entry;

import javax.resource.cci.MappedRecord;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.collection.PopulationIterator;
import org.openmdx.compatibility.base.collection.SparseArray;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject;
import org.openmdx.compatibility.base.dataprovider.cci.DataproviderObject_1_0;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.compatibility.base.naming.Path;

/**
 * DataproviderObject to MappedRecord marshaller.
 */
//---------------------------------------------------------------------------
@SuppressWarnings("unchecked")
class Marshaller {

    private Marshaller(
    ){
        // Avoid instantiation
    }
    
  //------------------------------------------------------------------------
    /**
     * Retrieves a DataproviderObject_1_0's values as map.
     */
    static MappedRecord toMappedRecord(
        DataproviderObject_1_0 source
    )throws ServiceException{
        return new DataproviderObjectRecord(source);
    }
    
  static MappedRecord toOperationResult(
    DataproviderObject_1_0 source
  ){
    return new OperationResult(source);
  }

  //------------------------------------------------------------------------
    /**
     * Creates a DataproviderObject_1_0's based on the values in a map.
     */
    static DataproviderObject toDataproviderObject(
        Path path,
        MappedRecord values
    ){
        DataproviderObject result = new DataproviderObject(path);
        result.values(
            SystemAttributes.OBJECT_CLASS
        ).add(
            values.getRecordName()
        );
        for(
            Iterator i = values.entrySet().iterator();
            i.hasNext();
        ){
            Map.Entry entry = (Map.Entry)i.next();
            Object source = entry.getValue();
            String name = (String)entry.getKey();
            if(name.startsWith(SystemAttributes.OBJECT_LOCK_PREFIX)) {
                if(name.endsWith(SystemAttributes.OBJECT_DIGEST)){
                    Object digest = source instanceof SparseList ? 
                        ((SparseList)source).get(0) :
                        source;
                    result.setDigest((byte[])digest);
                }
            } else {
                SparseList target = result.values(name);
                if(source instanceof SortedMap){
                    for(
                        Iterator j = ((SortedMap)source).entrySet().iterator();
                        j.hasNext();
                    ){
                        Map.Entry k = (Entry)j.next();
                        target.set(
                            ((Integer)k.getKey()).intValue(),
                            k.getValue()
                        ); 
                    }
                } else if(source instanceof SparseArray){
                    for(
                        PopulationIterator j = (
                            (SparseArray)source
                        ).populationIterator();
                        j.hasNext();
                    ) target.set(j.nextIndex(), j.next());
                } else if(source instanceof Collection){
                    target.addAll((Collection)source);
                } else {
                    target.set(0, source);
                }   
            }
        }
        return result;    
    }
    
}
