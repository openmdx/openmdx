/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Extent Implementation
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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

package org.openmdx.base.accessor.jmi.spi;

import java.util.Iterator;

import javax.jdo.Extent;
import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;

import org.openmdx.base.persistence.spi.StandardFetchPlan;


/**
 * Extent_1
 */
public class Extent_1<E> implements Extent<E> {

    /**
     * Constructor 
     *
     * @param persistenceManager
     * @param candidateClass
     * @param subclasses
     */
    Extent_1(
        PersistenceManager persistenceManager,
        Class<E> candidateClass,
        boolean subclasses
    ){
        this.persistenceManager = persistenceManager;
        this.candidateClass = candidateClass;
        this.subclasses = subclasses;
    }
    
    private final PersistenceManager persistenceManager;
    
    private final Class<E> candidateClass;
    
    private final boolean subclasses;
    
    private FetchPlan fetchPlan = null;

    /* (non-Javadoc)
     * @see javax.jdo.Extent#close()
     */
    public void close() {
        // Nothing to do yet
    }

    /* (non-Javadoc)
     * @see javax.jdo.Extent#close(java.util.Iterator)
     */
    public void close(Iterator<E> it) {
        // Nothing to do yet
    }

    /* (non-Javadoc)
     * @see javax.jdo.Extent#closeAll()
     */
    public void closeAll() {
        // Nothing to do yet
    }

    /* (non-Javadoc)
     * @see javax.jdo.Extent#getCandidateClass()
     */
    public Class<E> getCandidateClass() {
        return this.candidateClass;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Extent#getFetchPlan()
     */
    public FetchPlan getFetchPlan() {
        if(this.fetchPlan == null) {
            this.fetchPlan = StandardFetchPlan.newInstance(this.persistenceManager);
        }
        return this.fetchPlan;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Extent#getPersistenceManager()
     */
    public PersistenceManager getPersistenceManager() {
        return this.persistenceManager;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Extent#hasSubclasses()
     */
    public boolean hasSubclasses() {
        return this.subclasses;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Extent#iterator()
     */
    public Iterator<E> iterator(
    ) {
        throw new UnsupportedOperationException(
            "Use PersistenceHelper.getCandidates() to restrict the objects to a segment"
        );
    }

}
