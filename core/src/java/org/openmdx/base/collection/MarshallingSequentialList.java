/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Marshalling Sequential List
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2010, OMEX AG, Switzerland
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
package org.openmdx.base.collection;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.AbstractSequentialList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.ExceptionListenerMarshaller;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.persistence.spi.PersistenceCapableCollection;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.jdo.ReducedJDOHelper;

/**
 * A Marshalling Sequential List
 */
public class MarshallingSequentialList<E>
    extends AbstractSequentialList<E>
    implements PersistenceCapableCollection, Reconstructable, Serializable
{

    /**
     * Deserializer
     */
    protected MarshallingSequentialList(
    ){    
        super();
    }

    /**
     * Constructor
     * 
     * @param marshaller
     * @param list
     */    
    @SuppressWarnings("unchecked")
    public MarshallingSequentialList(
        Marshaller marshaller,
        List<?> list 
    ) {
        this.marshaller = new ExceptionListenerMarshaller(marshaller);
        this.list = (List<Object>) list;
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3257852069179110709L;

    /**
     * 
     */
    protected transient List<Object> list;

    /**
     * @serial
     */
    protected Marshaller marshaller;

    /**
     * Make the marshaller dynamically selectable
     * 
     * @return the marshalle prvided upon construction
     */
    protected Marshaller getMarshaller(){
        return this.marshaller;
    }
    
    /* (non-Javadoc)
     * @see java.util.List#add(int, java.lang.Object)
     */
    @Override
    public void add(
        int index, 
        E element
    ) {
        try {
            getDelegate().add(index, getMarshaller().unmarshal(element));
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }
    }

    /* (non-Javadoc)
     * @see java.util.Collection#add(java.lang.Object)
     */
    @Override
    public boolean add(
        E element
    ) {
        try {
            return getDelegate().add(getMarshaller().unmarshal(element));
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }        
    }

    /* (non-Javadoc)
     * @see java.util.Collection#clear()
     */
    @Override
    public void clear() {
        getDelegate().clear();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#contains(java.lang.Object)
     */
    @Override
    public boolean contains(
        Object element
    ) {
        try {
            return getDelegate().contains(getMarshaller().unmarshal(element));
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }        
    }

    /* (non-Javadoc)
     * @see java.util.List#get(int)
     */
    @SuppressWarnings("unchecked")
    @Override
    public E get(
        int index
    ) {
        try {
            return (E) getMarshaller().marshal(getDelegate().get(index));
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }        
    }

    /**
     * 
     * @return
     */
    protected List<Object> getDelegate(
    ){
        return this.list;  
    }

    /* (non-Javadoc)
     * @see java.util.List#indexOf(java.lang.Object)
     */
    @Override
    public int indexOf(
        Object arg0
    ) {
        try {
            return getDelegate().indexOf(getMarshaller().unmarshal(arg0));
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }        
    }

    /* (non-Javadoc)
     * @see java.util.Collection#isEmpty()
     */
    @Override
    public boolean isEmpty(
    ) {
        return getDelegate().isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Collection#iterator()
     */
    @Override
    public Iterator<E> iterator() {
        return listIterator();
    }

    /* (non-Javadoc)
     * @see java.util.List#lastIndexOf(java.lang.Object)
     */
    @Override
    public int lastIndexOf(
        Object arg0
    ) {
        try {
            return getDelegate().lastIndexOf(getMarshaller().unmarshal(arg0));
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }        
    }

    @Override
    public ListIterator<E> listIterator(
    ) {
        return new MarshallingIterator<E>(
            getMarshaller(),
            getDelegate().listIterator()
        );
    }

    @Override
    public ListIterator<E> listIterator(
        int index
    ) {
        return new MarshallingIterator<E>(
            getMarshaller(),
            getDelegate().listIterator(index)
        );
    }

    /* (non-Javadoc)
     * @see java.util.List#remove(int)
     */
    @SuppressWarnings("unchecked")
    @Override
    public E remove(
        int index
    ) {
        try {
            return (E) getMarshaller().marshal(
                getDelegate().remove(index)
            );
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }        
    }

    /* (non-Javadoc)
     * @see java.util.Collection#remove(java.lang.Object)
     */
    @Override
    public boolean remove(
        Object element
    ) {
        try {
            return getDelegate().remove(getMarshaller().unmarshal(element));
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }        
    }

    /* (non-Javadoc)
     * @see java.util.List#set(int, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public E set(
        int index, 
        E arg1
    ) {
        try {
            return (E) getMarshaller().marshal(
                getDelegate().set(index, getMarshaller().unmarshal(arg1))
            );
        }
        catch(ServiceException e) {
            throw new RuntimeServiceException(e);
        }        
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#toString()
     */
    @Override
    public String toString() {
        return getDelegate().toString();
    }


    //------------------------------------------------------------------------
    // Extends AbstractSequentialList 
    //------------------------------------------------------------------------

    @Override
    public int size(
    ) {
        return getDelegate().size();
    }

    
    //------------------------------------------------------------------------
    // Implements PersistenceCapable
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableContainer#openmdxjdoGetPersistenceManager()
     */
//  @Override
    public PersistenceManager jdoGetPersistenceManager(
    ){
        throw new UnsupportedOperationException("This method is not yet supported");
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoGetContainerId()
     */
//  @Override
    public Object jdoGetObjectId(
    ) {
        return ReducedJDOHelper.getObjectId(this.list);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoGetTransientContainerId()
     */
//  @Override
    public Object jdoGetTransactionalObjectId(
    ) {
        return ReducedJDOHelper.getTransactionalObjectId(this.list);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoIsPersistent()
     */
//  @Override
    public boolean jdoIsPersistent(
    ) {
        return ReducedJDOHelper.isPersistent(this.list);
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceStateManager(javax.jdo.spi.StateManager)
     */
//  @Override
    public void jdoReplaceStateManager(
        StateManager sm
    ) throws SecurityException {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideField(int)
     */
//  @Override
    public void jdoProvideField(int fieldNumber) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoProvideFields(int[])
     */
//  @Override
    public void jdoProvideFields(int[] fieldNumbers) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceField(int)
     */
//  @Override
    public void jdoReplaceField(int fieldNumber) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFields(int[])
     */
//  @Override
    public void jdoReplaceFields(int[] fieldNumbers) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoReplaceFlags()
     */
//  @Override
    public void jdoReplaceFlags() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyFields(java.lang.Object, int[])
     */
//  @Override
    public void jdoCopyFields(Object other, int[] fieldNumbers) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoMakeDirty(java.lang.String)
     */
//  @Override
    public void jdoMakeDirty(String fieldName) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoGetVersion()
     */
//  @Override
    public Object jdoGetVersion() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDirty()
     */
//  @Override
    public boolean jdoIsDirty() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsTransactional()
     */
//  @Override
    public boolean jdoIsTransactional() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsNew()
     */
//  @Override
    public boolean jdoIsNew() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDeleted()
     */
//  @Override
    public boolean jdoIsDeleted() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoIsDetached()
     */
//  @Override
    public boolean jdoIsDetached() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager)
     */
//  @Override
    public PersistenceCapable jdoNewInstance(StateManager sm) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewInstance(javax.jdo.spi.StateManager, java.lang.Object)
     */
//  @Override
    public PersistenceCapable jdoNewInstance(StateManager sm, Object oid) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance()
     */
//  @Override
    public Object jdoNewObjectIdInstance() {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoNewObjectIdInstance(java.lang.Object)
     */
//  @Override
    public Object jdoNewObjectIdInstance(Object o) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(java.lang.Object)
     */
//  @Override
    public void jdoCopyKeyFieldsToObjectId(Object oid) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsToObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldSupplier, java.lang.Object)
     */
//  @Override
    public void jdoCopyKeyFieldsToObjectId(ObjectIdFieldSupplier fm, Object oid) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    /* (non-Javadoc)
     * @see javax.jdo.spi.PersistenceCapable#jdoCopyKeyFieldsFromObjectId(javax.jdo.spi.PersistenceCapable.ObjectIdFieldConsumer, java.lang.Object)
     */
//  @Override
    public void jdoCopyKeyFieldsFromObjectId(
        ObjectIdFieldConsumer fm,
        Object oid
    ) {
        throw new UnsupportedOperationException("Not supported by persistence capable collections");
    }

    
    //------------------------------------------------------------------------
    // Implements PersistenceCapableCollection
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoEvict(boolean)
     */
//  @Override
    public void openmdxjdoEvict(
        boolean allMembers, 
        boolean allSubSets
    ) {
        if(this.list instanceof PersistenceCapableCollection){
            ((PersistenceCapableCollection)this.list).openmdxjdoEvict(allMembers, allSubSets);
        } else {
            throw new UnsupportedOperationException("The delegate is not a PersistenceCapableCollection");
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoGetPersistenceManager()
     */
//  @Override
    public PersistenceManager openmdxjdoGetDataObjectManager() {
        if(this.list instanceof PersistenceCapableCollection){
            return ((PersistenceCapableCollection)this.list).openmdxjdoGetDataObjectManager();
        } else {
            throw new UnsupportedOperationException("The delegate is not a PersistenceCapableCollection");
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoRefresh()
     */
//  @Override
    public void openmdxjdoRefresh() {
        if(this.list instanceof PersistenceCapableCollection){
            ((PersistenceCapableCollection)this.list).openmdxjdoRefresh();
        } else {
            throw new UnsupportedOperationException("The delegate is not a PersistenceCapableCollection");
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.persistence.spi.PersistenceCapableCollection#openmdxjdoRetrieve(javax.jdo.FetchPlan)
     */
//  @Override
    public void openmdxjdoRetrieve(
        FetchPlan fetchPlan
    ) {
        if(this.list instanceof PersistenceCapableCollection){
            ((PersistenceCapableCollection)this.list).openmdxjdoRetrieve(fetchPlan);
        } else {
            throw new UnsupportedOperationException("The delegate is not a PersistenceCapableCollection");
        }
    }
    
    
    //------------------------------------------------------------------------
    // Implements Reconstructable
    //------------------------------------------------------------------------

    /**
     * Write part of a reconstructable object's state to an OutputStream
     * (optional operation).
     *
     * @param   stream
     *          OutputStream that holds part of a reconstructable object's 
     *          state.
     *
     * @exception   ServiceException
     *              if partial state streaming fails
     * @exception   ServiceException NOT_SUPPORTED
     *              if the instance is not reconstructable
     */
    public void write(
        OutputStream stream
    ) throws ServiceException {
        List<Object> list = getDelegate();
        if(list instanceof Reconstructable){
            ((Reconstructable)list).write(stream);
        } else {    
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN, 
                BasicException.Code.NOT_SUPPORTED,
                "List to delegate to is not reconstructable",
                new BasicException.Parameter("class",list.getClass().getName())
            );
        }
    }


    //------------------------------------------------------------------------
    // Class MarshallingIterator
    //------------------------------------------------------------------------

    protected static class MarshallingIterator<E>
        implements ListIterator<E> 
    {

        @SuppressWarnings("unchecked")
        MarshallingIterator(
            Marshaller marshaller,
            ListIterator<?> iterator
        ) {
            this.iterator = (ListIterator<Object>) iterator;
            this.marshaller = marshaller;
        }

        private final ListIterator<Object> iterator;

        private final Marshaller marshaller;
        
        public void add(
            Object o
        ) {
            try {
                this.iterator.add(this.marshaller.unmarshal(o));
            }
            catch(ServiceException e) {
                throw new RuntimeServiceException(e);
            }            
        }

        public boolean hasNext(
        ) {
            return this.iterator.hasNext();
        }

        public boolean hasPrevious(
        ) {
            return this.iterator.hasPrevious();
        }

        @SuppressWarnings("unchecked")
        public E next(
        ) {
            try {
                return (E) this.marshaller.marshal(this.iterator.next());
            }
            catch(ServiceException e) {
                throw new RuntimeServiceException(e);
            }            
        }

        public int nextIndex(
        ) {
            return this.iterator.nextIndex();
        }

        @SuppressWarnings("unchecked")
        public E previous(
        ) {
            try {
                return (E) this.marshaller.marshal(this.iterator.previous());
            }
            catch(ServiceException e) {
                throw new RuntimeServiceException(e);
            }
        }

        public int previousIndex(
        ) {
            return this.iterator.previousIndex();
        }

        public void remove(
        ) {
            this.iterator.remove();
        }

        public void set(
            Object o
        ) {
            try {
                this.iterator.set(this.marshaller.unmarshal(o));
            }
            catch(ServiceException e) {
                throw new RuntimeServiceException(e);
            }            
        }

    }

}
