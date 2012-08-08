/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: VirtualObjects_2.java,v 1.11 2010/04/27 17:10:46 hburger Exp $
 * Description: Virtual Objects 
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/04/27 17:10:46 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2010, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.rest.spi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.IndexedRecord;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.spi.AbstractRestInteraction;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.base.rest.spi.Query_2Facade;
import org.openmdx.kernel.exception.BasicException;

/**
 * Virtual Objects 
 */
public class VirtualObjects_2 implements Port {
    
    /**
     * Constructor 
     */
    public VirtualObjects_2() {
        super();
    }

    /**
     * Catch Authorities and Providers by default
     */
    protected Path[] finalPattern = {
        new Path("xri://@openmdx*($..)"),
        new Path("xri://@openmdx*($..)/provider/($..)")
    };

    /**
     * The final class type index must match the final class pattern index.
     * 
     * @see #finalPattern
     */
    protected String[] finalClass = {
        "org:openmdx:base:Authority",
        "org:openmdx:base:Provider"
    };

    /**
     * Retrieve finalPattern.
     *
     * @return Returns the finalPattern.
     */
    public String[] getFinalPattern(
    ) {
        String[] finalPattern = new String[this.finalPattern.length];
        for(
            int i = 0;
            i < finalPattern.length;
            i++
        ){
            finalPattern[i] = this.finalPattern[i].toXRI();
        }
        return finalPattern;
    }

    /**
     * Retrieve finalPattern.
     *
     * @param index the array index
     * 
     * @return Returns the finalPattern.
     */
    public String getFinalPattern(
        int index
    ) {
        return this.finalPattern[index].toXRI();
    }
    
    /**
     * Set finalPattern.
     * 
     * @param finalPattern The finalPattern to set.
     */
    public void setFinalPattern(
        String[] finalPattern
    ) {
        this.finalPattern = new Path[finalPattern.length];
        for(
            int i = 0;
            i < finalPattern.length;
            i++
        ){
            this.finalPattern[i] = new Path(finalPattern[i]);
        }
    }

    /**
     * Set finalPattern.
     * 
     * @param index the array index
     * @param finalPattern The finalPattern to set.
     */
    public void setFinalPattern(
        int index, 
        String finalPattern
    ) {
        this.finalPattern[index] = new Path(finalPattern);
    }
    
    /**
     * Retrieve finalClass.
     *
     * @return Returns the finalClass.
     */
    public String[] getFinalClass() {
        return this.finalClass;
    }

    /**
     * Retrieve finalClass.
     *
     * @param index the array index
     * 
     * @return Returns the finalClass.
     */
    public String getFinalClass(
        int index
    ) {
        return this.finalClass[index];
    }
    
    /**
     * Set finalClass.
     * 
     * @param finalClass The finalClass to set.
     */
    public void setFinalClass(String[] finalClass) {
        this.finalClass = finalClass;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.spi.Port#getInteraction(javax.resource.cci.Connection)
     */
    @Override
    public VirtualObjectProvider getInteraction(
        Connection connection
    ) throws ResourceException {
        return new VirtualObjectProvider(connection);
    }

    
    //------------------------------------------------------------------------
    // Class VirtualObjectInterceptor
    //------------------------------------------------------------------------
    
    /**
     * Virtual Object Interceptor
     *
     */
    class VirtualObjectProvider extends AbstractRestInteraction implements VirtualObjects_2_0 {

        /**
         * Constructor 
         *
         * @param connection
         */
        protected VirtualObjectProvider(
            Connection connection
        ) {
            super(connection);
        }

        /**
         * The seeds for virtual objects
         */
        private volatile ConcurrentMap<Path,String> seeds;
        
        /* (non-Javadoc)
         * @see org.openmdx.base.accessor.rest.spi.VirtualObjects_2_0#putSeed(org.openmdx.base.naming.Path, java.lang.String)
         */
        @Override
        public void putSeed(
            Path xri, 
            String objectClass
        ) throws ResourceException {
            if(this.seeds == null) {
                synchronized(this) {
                    if(this.seeds == null) {
                        this.seeds =  new ConcurrentHashMap<Path,String>();
                    }
                }
            }
            String oldClass = seeds.putIfAbsent(xri, objectClass);
            if(
                oldClass != null && 
                !oldClass.equals(objectClass)
            )  throw BasicException.initHolder(
                new ResourceException(
                    "Virtual object class mismatch",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.CONCURRENT_ACCESS_FAILURE,
                        new BasicException.Parameter("old-class", oldClass),
                        new BasicException.Parameter("new-class", objectClass)
                    )
                )
            );
        }
        
        /**
         * Tells whether an object has been sown for the given path
         * 
         * @param xri
         * 
         * @return <code>true</code> if 
         */
        @Override
        public boolean isVirtual(
            Path xri
        ){
            if(xri.isTransientObjectId()) {
                return false;
            }
            //
            // Final Class Instances
            //
            for(Path pattern : VirtualObjects_2.this.finalPattern){
                if(xri.isLike(pattern)) return true;
            }
            //
            // Sown instances
            //
            return this.seeds != null && this.seeds.containsKey(xri);
        }
        
        /**
         * Returns an object's type
         * 
         * @param xri
         * 
         * @return the object's type 
         * @throws ResourceException 
         */
        private String getType(
            Path xri
        ) throws ResourceException{
            //
            // Sown instances
            //
            if(this.seeds != null) {
                String type = this.seeds.get(xri);
                if(type != null) {
                    return type;
                }
            }
            //
            // Final Class Instances
            //
            for(
                int i = 0;
                i < VirtualObjects_2.this.finalPattern.length;
                i++
            ){
                if(xri.isLike(VirtualObjects_2.this.finalPattern[i])) {
                    return VirtualObjects_2.this.finalClass[i];
                }
            }
            //
            // Unsupported object id
            //
            throw new ResourceException(
                "The virtual object plug-in does not support the given XRI",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND,
                    new BasicException.Parameter("xri", xri)
                )
            );
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.rest.spi.AbstractRestInteraction#get(org.openmdx.base.resource.spi.RestInteractionSpec, org.openmdx.base.rest.spi.Query_2Facade, javax.resource.cci.IndexedRecord)
         */
        @SuppressWarnings("unchecked")
        @Override
        public boolean get(
            RestInteractionSpec ispec,
            Query_2Facade input,
            IndexedRecord output
        ) throws ServiceException {
            Path objectId = input.getPath();
            try {
                output.add(
                    Object_2Facade.newInstance(
                        objectId,
                        getType(objectId)
                    ).getDelegate()
                );
            } catch (ResourceException e) {
                throw new ServiceException(e);
            }
            return true;
        }
        
    }
    
}
