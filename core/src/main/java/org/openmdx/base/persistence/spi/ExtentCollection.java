/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: ExtentCollection 
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
package org.openmdx.base.persistence.spi;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

import javax.jdo.Extent;
import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;

import org.openmdx.base.naming.Path;

/**
 * Extent Collection
 */
public final class ExtentCollection<E> extends AbstractCollection<E> implements PersistenceCapableCollection{

    /**
     * Constructor 
     *
     * @param extent
     * @param xriPattern
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
     * Retrieve the delegate lazily
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

    //------------------------------------------------------------------------
    // Implements PersistenceCapableCollection
    //------------------------------------------------------------------------

    PersistenceCapableCollection  pcc;

    /**
     * @return
     * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoGetDataObjectManager()
     */
    public PersistenceManager openmdxjdoGetDataObjectManager() {
        return ((PersistenceCapableCollection)getDelegate()).openmdxjdoGetDataObjectManager();
    }

    /**
     * @param allMembers
     * @param allSubSets
     * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoEvict(boolean, boolean)
     */
    public void openmdxjdoEvict(boolean allMembers, boolean allSubSets) {
        ((PersistenceCapableCollection)getDelegate()).openmdxjdoEvict(allMembers, allSubSets);
    }

    /**
     * 
     * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoRefresh()
     */
    public void openmdxjdoRefresh() {
        ((PersistenceCapableCollection)getDelegate()).openmdxjdoRefresh();
    }

    /**
     * @param fetchPlan
     * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoRetrieve(javax.jdo.FetchPlan)
     */
    public void openmdxjdoRetrieve(FetchPlan fetchPlan) {
        ((PersistenceCapableCollection)getDelegate()).openmdxjdoRetrieve(fetchPlan);
    }

    /**
     * @return
     * @see javax.jdo.spi.PersistenceCapable#jdoGetPersistenceManager()
     */
    public PersistenceManager jdoGetPersistenceManager() {
        return ((PersistenceCapableCollection)getDelegate()).jdoGetPersistenceManager();
    }

    /**
     * @param sm
     * @throws SecurityException
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceStateManager(javax.jdo.spi.StateManager)
     */
    public void jdoReplaceStateManager(StateManager sm)
        throws SecurityException {
        ((PersistenceCapableCollection)getDelegate()).jdoReplaceStateManager(sm);
    }

    /**
     * @param fieldNumber
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideField(int)
     */
    public void jdoProvideField(int fieldNumber) {
        ((PersistenceCapableCollection)getDelegate()).jdoProvideField(fieldNumber);
    }

    /**
     * @param fieldNumbers
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideFields(int[])
     */
    public void jdoProvideFields(int[] fieldNumbers) {
        ((PersistenceCapableCollection)getDelegate()).jdoProvideFields(fieldNumbers);
    }

    /**
     * @param fieldNumber
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceField(int)
     */
    public void jdoReplaceField(int fieldNumber) {
        ((PersistenceCapableCollection)getDelegate()).jdoReplaceField(fieldNumber);
    }

    /**
     * @param fieldNumbers
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFields(int[])
     */
    public void jdoReplaceFields(int[] fieldNumbers) {
        ((PersistenceCapableCollection)getDelegate()).jdoReplaceFields(fieldNumbers);
    }

    /**
     * 
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFlags()
     */
    public void jdoReplaceFlags() {
        ((PersistenceCapableCollection)getDelegate()).jdoReplaceFlags();
    }

    /**
     * @param other
     * @param fieldNumbers
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyFields(java.lang.Object, int[])
     */
    public void jdoCopyFields(Object other, int[] fieldNumbers) {
        ((PersistenceCapableCollection)getDelegate()).jdoCopyFields(other, fieldNumbers);
    }

    /**
     * @param fieldName
     * @see javax.jdo.spi.PersistenceCapable#jdoMakeDirty(java.lang.String)
     */
    public void jdoMakeDirty(String fieldName) {
        ((PersistenceCapableCollection)getDelegate()).jdoMakeDirty(fieldName);
    }

    /**
     * @return
     * @see javax.jdo.spi.PersistenceCapable#jdoGetObjectId()
     */
    public Object jdoGetObjectId() {
        return ((PersistenceCapableCollection)getDelegate()).jdoGetObjectId();
    }

    /**
     * @return
     * @see javax.jdo.spi.PersistenceCapable#jdoGetTransactionalObjectId()
     */
    public Object jdoGetTransactionalObjectId() {
        return ((PersistenceCapableCollection)getDelegate()).jdoGetTransactionalObjectId();
    }

    /**
     * @return
     * @see javax.jdo.spi.PersistenceCapable#jdoGetVersion()
     */
    public Object jdoGetVersion() {
        return ((PersistenceCapableCollection)getDelegate()).jdoGetVersion();
    }

    /**
     * @return
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDirty()
     */
    public boolean jdoIsDirty() {
        return ((PersistenceCapableCollection)getDelegate()).jdoIsDirty();
    }

    /**
     * @return
     * @see javax.jdo.spi.PersistenceCapable#jdoIsTransactional()
     */
    public boolean jdoIsTransactional() {
        return ((PersistenceCapableCollection)getDelegate()).jdoIsTransactional();
    }

    /**
     * @return
     * @see javax.jdo.spi.PersistenceCapable#jdoIsPersistent()
     */
    public boolean jdoIsPersistent() {
        return ((PersistenceCapableCollection)getDelegate()).jdoIsPersistent();
    }

    /**
     * @return
     * @see javax.jdo.spi.PersistenceCapable#jdoIsNew()
     */
    public boolean jdoIsNew() {
        return ((PersistenceCapableCollection)getDelegate()).jdoIsNew();
    }

    /**
     * @return
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDeleted()
     */
    public boolean jdoIsDeleted() {
        return ((PersistenceCapableCollection)getDelegate()).jdoIsDeleted();
    }

    /**
     * @return
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDetached()
     */
    public boolean jdoIsDetached() {
        return ((PersistenceCapableCollection)getDelegate()).jdoIsDetached();
    }

    /**
     * @param sm
     * @return
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager)
     */
    public PersistenceCapable jdoNewInstance(StateManager sm) {
        return ((PersistenceCapableCollection)getDelegate()).jdoNewInstance(sm);
    }

    /**
     * @param sm
     * @param oid
     * @return
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager, java.lang.Object)
     */
    public PersistenceCapable jdoNewInstance(StateManager sm, Object oid) {
        return ((PersistenceCapableCollection)getDelegate()).jdoNewInstance(sm, oid);
    }

    /**
     * @return
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance()
     */
    public Object jdoNewObjectIdInstance() {
        return ((PersistenceCapableCollection)getDelegate()).jdoNewObjectIdInstance();
    }

    /**
     * @param o
     * @return
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance(java.lang.Object)
     */
    public Object jdoNewObjectIdInstance(Object o) {
        return ((PersistenceCapableCollection)getDelegate()).jdoNewObjectIdInstance(o);
    }

    /**
     * @param oid
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(java.lang.Object)
     */
    public void jdoCopyKeyFieldsToObjectId(Object oid) {
        ((PersistenceCapableCollection)getDelegate()).jdoCopyKeyFieldsToObjectId(oid);
    }

    /**
     * @param fm
     * @param oid
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldSupplier, java.lang.Object)
     */
    public void jdoCopyKeyFieldsToObjectId(ObjectIdFieldSupplier fm, Object oid) {
        ((PersistenceCapableCollection)getDelegate()).jdoCopyKeyFieldsToObjectId(fm, oid);
    }

    /**
     * @param fm
     * @param oid
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsFromObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldConsumer, java.lang.Object)
     */
    public void jdoCopyKeyFieldsFromObjectId(
        ObjectIdFieldConsumer fm,
        Object oid) {
        ((PersistenceCapableCollection)getDelegate()).jdoCopyKeyFieldsFromObjectId(fm, oid);
    }
    
}
