/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: StateCapable_2.java,v 1.3 2009/06/01 15:44:34 wfro Exp $
 * Description: State Capable Layer
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/01 15:44:34 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
package org.openmdx.compatibility.state1.aop0;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.InteractionSpec;
import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;

import org.openmdx.base.collection.SparseList;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.Records;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.spi.AbstractRestPlugIn;
import org.openmdx.base.rest.spi.ObjectHolder_2Facade;
import org.openmdx.kernel.exception.BasicException;

/**
 * State Capable Layer
 */
public class StateCapable_2 extends AbstractRestPlugIn {

    /**
     * The identity prefix for objects with unique transaction time
     */
    private Collection<Path> transactionTimeUnique = Collections.emptySet();
    
    /**
     * The identity prefix for objects with unique valid time
     */
    private Collection<Path> validTimeUnique = Collections.emptySet();
    
    
    //------------------------------------------------------------------------
    // Is Java Bean
    //------------------------------------------------------------------------
    
    /**
     * Convert internal type to Java Bean type
     * 
     * @param source the internal values
     * 
     * @return the Java Bean values
     */
    private static String[] toArray(
        Collection<Path> source
    ){
        String[] target = new String[source.size()];
        int i = 0;
        for(Path xri : source){
            target[i++] = xri.toXRI();
        }
        return target;
    }
    
    /**
     * Convert Java Bean type to internal type
     * 
     * @param source the Java Bean values
     * 
     * @return the internal values
     */
    private static Set<Path> toSet(
        String[] source
    ){
        if(source == null) {
            return null;
        } else {
            Set<Path> target = new HashSet<Path>(source.length);
            for(String xri : source) {
                target.add(new Path(xri));
            }
            return target;
        }
    }
    
    /**
     * Retrieve transactionTimeUnique.
     *
     * @return Returns the transactionTimeUnique.
     */
    public String[] getTransactionTimeUnique() {
        return toArray(this.transactionTimeUnique);
    }

    
    /**
     * Set transactionTimeUnique.
     * 
     * @param transactionTimeUnique The transactionTimeUnique to set.
     */
    public void setTransactionTimeUnique(String[] transactionTimeUnique) {
        this.transactionTimeUnique = toSet(transactionTimeUnique);
    }

    
    /**
     * Retrieve validTimeUnique.
     *
     * @return Returns the validTimeUnique.
     */
    public String[] getValidTimeUnique() {
        return toArray(this.validTimeUnique);
    }

    
    /**
     * Set validTimeUnique.
     * 
     * @param validTimeUnique The validTimeUnique to set.
     */
    public void setValidTimeUnique(String[] validTimeUnique) {
        this.validTimeUnique = toSet(validTimeUnique);
    }


    //------------------------------------------------------------------------
    // Implements RestConnection
    //------------------------------------------------------------------------

    /**
     * Convert configuration values to a path set
     * 
     * @param values the paths string representations
     * 
     * @return the corresponding path set
     */
    protected static Set<Path> toPaths(
        SparseList<?> values
    ){
        Set<Path> paths = new HashSet<Path>();
        for(
            Iterator<?> i = values.populationIterator();
            i.hasNext();
        ){
            paths.add(
                new Path((String)i.next())
            );
        }
        return paths;
    }
    
     /**
     * Tells whether the given identity belongs to an object with unique transaction time
     * 
     * @param identity
     * @return <code>true</code> if the given identity belongs to an object with unique transaction time
     */
    private Boolean isTransactionTimeUnique(
        Path identity
    ){
        for(Path pattern : this.transactionTimeUnique) {
            if(identity.isLike(pattern)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    /**
     * Tells whether the given identity belongs to an object with unique transaction time
     * 
     * @param identity
     * @return <code>true</code> if the given identity belongs to an object with unique transaction time
     */
    private Boolean isValidTimeUnique(
        Path identity
    ){
        for(Path pattern : this.validTimeUnique) {
            if(identity.isLike(pattern)) {
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.rest.RestConnection#execute(javax.resource.cci.InteractionSpec, javax.resource.cci.Record, javax.resource.cci.Record)
     */
    @SuppressWarnings("unchecked")
    public boolean execute(
            InteractionSpec ispec, 
            Record input, 
            Record output
    ) throws ResourceException {
        RestInteractionSpec interactionSpec = (RestInteractionSpec) ispec;
        switch(interactionSpec.getFunction()) {
            case GET:
                if(input instanceof IndexedRecord && output instanceof IndexedRecord){
                    IndexedRecord inputRecord = (IndexedRecord) input;
                    IndexedRecord outputRecord = (IndexedRecord) output;
                    for(Object entry : inputRecord){
                        Path accessPath = new Path(entry.toString());
                        Path resourceIdentifier = new Path(accessPath.getBase());
                        MappedRecord stateCapable = Records.getRecordFactory().createMappedRecord("org:openmdx:compatibility:state1:StateCapable");
                        if(!stateCapable.containsKey("transactionTimeUnique")) {
                            stateCapable.put(
                                "transactionTimeUnique",
                                isTransactionTimeUnique(resourceIdentifier)
                           );
                        }
                        if(!stateCapable.containsKey("validTimeUnique")) {
                            stateCapable.put(
                                "validTimeUnique",
                                isValidTimeUnique(resourceIdentifier)
                           );
                        }
                        ObjectHolder_2Facade holder = ObjectHolder_2Facade.newInstance();
                        holder.setPath(accessPath);
                        holder.setValue(stateCapable);
                        holder.setVersion(Integer.valueOf(0));
                        outputRecord.add(holder.getDelegate());
                    }
                    return !inputRecord.isEmpty();
                }
                // Fall through
            default: throw BasicException.initHolder(
                new NotSupportedException(
                    "The explorer supports GET without query only",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.NOT_SUPPORTED,
                        new BasicException.Parameter("function", interactionSpec.getFunction())
                    )
                )
            );
        }
    }

}
