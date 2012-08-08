/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: ExtentCollection.java,v 1.2 2010/02/11 14:30:44 hburger Exp $
 * Description: ExtentCollection 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/02/11 14:30:44 $
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
package org.openmdx.base.persistence.spi;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

import javax.jdo.Extent;
import javax.jdo.Query;

import org.openmdx.base.naming.Path;

/**
 * Extent Collection
 */
public final class ExtentCollection<E> extends AbstractCollection<E> {

    /**
     * Constructor 
     *
     * @param extent
     * @param pattern
     */
    public ExtentCollection(
        Extent<E> extent,
        Path xriPattern
    ){
        this.extent = extent;
        this.pattern = xriPattern;
    }
    
    /**
     * 
     */
    private final Extent<E> extent;
    
    /**
     * 
     */
    private final Path pattern;
    
    /**
     * Retriebe the delegate lazily
     */
    private transient Collection<E> delegate;
    
    /**
     * Retrieve extent.
     *
     * @return Returns the extent.
     */
    public Extent<E> getExtent() {
        return this.extent;
    }
    
    /**
     * Retrieve pattern.
     *
     * @return Returns the pattern.
     */
    public Path getPattern() {
        return this.pattern;
    }

    /**
     * Retrieve the delegate lazily
     * 
     * @return the delegate
     */
    @SuppressWarnings("unchecked")
    private Collection<E> getDelegate(
    ){
        if(this.delegate == null){
            Query query = this.extent.getPersistenceManager().newQuery(
                this.extent
            );
            query.setCandidates(this);
            this.delegate = (Collection<E>) query.execute();
        }
        return this.delegate;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#iterator()
     */
    @Override
    public Iterator<E> iterator() {
        return getDelegate().iterator();
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {
        return getDelegate().size();
    }
    
    /**
     * The identity is modelled as String
     * 
     * @param pattern
     * 
     * @return a JDO compliant String pattern
     */
    public static String toIdentityPattern(
        Path pattern
    ){
        return pattern.toXRI().replace(".", "\\.");
    }

}
