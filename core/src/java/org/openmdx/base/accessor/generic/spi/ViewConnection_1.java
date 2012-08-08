/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: ViewConnection_1.java,v 1.16 2008/03/19 17:13:11 hburger Exp $
 * Description: StateConnection_1 
 * Revision:    $Revision: 1.16 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/19 17:13:11 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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

package org.openmdx.base.accessor.generic.spi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.resource.cci.InteractionSpec;

import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0;
import org.openmdx.base.accessor.generic.cci.ObjectFactory_1_3;
import org.openmdx.base.accessor.generic.cci.Object_1_0;
import org.openmdx.base.accessor.generic.cci.Structure_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.transaction.UnitOfWork_1_0;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.transport.spi.Connection_1Factory;
import org.openmdx.compatibility.base.marshalling.CachingMarshaller;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.compatibility.state1.view.DateStateContext;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;

/**
 * StateConnection_1
 */
@SuppressWarnings("unchecked")
public class ViewConnection_1
    extends CachingMarshaller
    implements ObjectFactory_1_3
{

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -9196597251941226707L;


    /**
     * Constructor 
     *
     * @param sink
     * @param viewContext 
     * 
     * @throws ServiceException
     */
    ViewConnection_1(
        SinkConnection_1 sink,
        ObjectFactory_1_0 source,
        InteractionSpec viewContext
    ) throws ServiceException {
        this.sink = sink;
        this.source = source;
        this.context = (DateStateContext) viewContext;
    }

    /**
     * 
     */
    private final DateStateContext context;
    
    /**
     * 
     */
    private ObjectFactory_1_0 source;

    /**
     * 
     */
    private SinkConnection_1 sink;

    Model_1_0 getModel(
    ){
        return this.sink.getModel();
    }
    
    /**
     * Retrieve the view connection's context
     * 
     * @return the view connection's context
     */
    DateStateContext getContext(
    ){
        return this.context;
    }

    /**
     * Retrieve the view connectioon's sink
     * 
     * @return the view connectioon's sink
     */
    SinkConnection_1 getSink(
    ){
        return this.sink;
    }
    
    ObjectFactory_1_0 getSource(
    ) throws ServiceException{
        return this.source;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0#close()
     */
    public void close(
    ) throws ServiceException {
        if(isOpen()) {
            this.source.close();
            this.source = null;
            this.sink = null;
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0#createObject(java.lang.String)
     */
    public Object_1_0 createObject(
        String objectClass
    ) throws ServiceException {
        assertOpen();
        return (Object_1_0) marshal(
            this.sink.getDelegate().createObject(
                objectClass
            )
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0#createObject(java.lang.String, org.openmdx.base.accessor.generic.cci.Object_1_0)
     */
    public Object_1_0 createObject(
        String objectClass, 
        Object_1_0 initialValues
    ) throws ServiceException {
        assertOpen();
        return (Object_1_0) marshal(
            this.sink.getDelegate().createObject(
                objectClass, 
                (Object_1_0) unmarshal(initialValues)
            )
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0#createStructure(java.lang.String, java.util.List, java.util.List)
     */
    public Structure_1_0 createStructure(
        String type,
        List fieldNames,
        List fieldValues
    ) throws ServiceException {
        assertOpen();
        return new MarshallingStructure_1(
            this.sink.getDelegate().createStructure(
                type, 
                fieldNames, 
                unmarshalStructureValues(fieldValues)
            ),
            this
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0#getObject(java.lang.Object)
     */
    public Object_1_0 getObject(
        Object accessPath
    ) throws ServiceException {
        return getObject(
            accessPath,
            null, // dateStateInstance
            false // initializeCacheWithDelegate
        );
    }
    
    public Object_1_0 getObject(
        Object accessPath,
        Boolean dateStateInstance, 
        boolean initializeCacheWithDelegate
    ) throws ServiceException {
        if(accessPath == null) {
            return null;
        } else {
            assertOpen();
            Path path = accessPath instanceof Path ?
                (Path)accessPath :
                new Path(accessPath.toString());
            SinkObject_1 sinkObject = dateStateInstance == null ? this.sink.getObject(
                path
            ) : this.sink.getObject(
                path, 
                dateStateInstance.booleanValue(), 
                initializeCacheWithDelegate
            );    
            return sinkObject == null || !sinkObject.isDirty() ? (Object_1_0) marshal(
                this.source.getObject(path)
            ) : new ViewObject_1(
                this, 
                sinkObject
            );
        }
    }

    void marshalPaths(
        SparseList paths
    ) throws ServiceException {
        for(
            Iterator i = paths.populationIterator();
            i.hasNext();
        ){
            marshal(
                this.source.getObject(i.next())
            );
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.ObjectFactory_1_0#getUnitOfWork()
     */
    public UnitOfWork_1_0 getUnitOfWork(
    ) throws ServiceException {
        assertOpen();
        return this.sink.getUnitOfWork();
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.marshalling.CachingMarshaller#createMarshalledObject(java.lang.Object)
     */
    protected Object createMarshalledObject(
        Object source
    ) throws ServiceException {
        return new ViewObject_1(
            this,
            (Object_1_0)source
        );
    }

    /**
     * Tells whether the connection is open of not
     * 
     * @return <code>true</code> if the connection is open.
     */
    protected final boolean isOpen(){
        return this.source != null;
    }
    
    /**
     * Asserts that the connection is open
     * 
     * @throws ServiceException ILLEGAL_STATE if the connection is closed
     */
    protected void assertOpen(
    ) throws ServiceException{
        if(!isOpen()) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ILLEGAL_STATE,
            null,
            "The connection is already closed"
        );
    }
    
    

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.marshalling.CachingMarshaller#unmarshal(java.lang.Object)
     */
    public Object unmarshal(
        Object source
    ) throws ServiceException {
        return 
            source instanceof Delegating_1_0 ? ((Delegating_1_0)source).objGetDelegate() :
            source;
    }

    /**
     * Unmarshal structure values
     * 
     * @param source
     * 
     * @return the unmarshalled structure values
     * 
     * @throws ServiceException
     */
    private Object unmarshalStructureValues(
       Object source
    ) throws ServiceException {
        return 
            source instanceof List ? unmarshalStructureValues((List)source) :
            source instanceof Set ? unmarshalStructureValues((Set)source) :
            unmarshal(source);
    }

    /**
     * Unmarshal structure values
     * 
     * @param source
     * 
     * @return the unmarshalled structure values
     * 
     * @throws ServiceException
     */
    private List unmarshalStructureValues(
       List source
    ) throws ServiceException {
        List target = new ArrayList(source.size());
        for(
            Iterator i = source.iterator();
            i.hasNext();
        ){
            target.add(unmarshalStructureValues(i.next()));
        }
        return target;
    }

    /**
     * Unmarshal structure values
     * 
     * @param source
     * 
     * @return the unmarshalled structure values
     * 
     * @throws ServiceException
     */
    private Set unmarshalStructureValues(
       Set source
    ) throws ServiceException {
        Set target = new HashSet(source.size());
        for(
            Iterator i = source.iterator();
            i.hasNext();
        ){
            target.add(unmarshalStructureValues(i.next()));
        }
        return target;
    }

    void invalidate(
        ViewObject_1 object
    ){
        this.mapping.remove(object.objGetDelegate());
    }

    
    //------------------------------------------------------------------------
    // Implements ObjectFactory_1_1
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.ObjectFactory_1_1#createObject(java.lang.String, java.lang.String, org.openmdx.base.accessor.generic.cci.Object_1_0)
     */
    public Object_1_0 createObject(
        String roleClass,
        String roleId,
        Object_1_0 roleCapable
    ) throws ServiceException {
        return getSink().getDelegate().createObject(
            roleClass,
            roleId,
            roleCapable
        );
    }

    
    //------------------------------------------------------------------------
    // Implements ObjectFatory_1_2
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.ObjectFactory_1_2#getConnectionFactory()
     */
    public Connection_1Factory getConnectionFactory() {
        return getSink().getDelegate().getConnectionFactory();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.ObjectFactory_1_2#hasContainerManagedUnitOfWork()
     */
    public Boolean hasContainerManagedUnitOfWork() {
        return getSink().getDelegate().hasContainerManagedUnitOfWork();
    }

    
    //------------------------------------------------------------------------
    // Implements ObjectFactory_1_3
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.ObjectFactory_1_3#cloneObject(org.openmdx.compatibility.base.naming.Path, org.openmdx.base.accessor.generic.cci.Object_1_0, boolean)
     */
    public Object_1_0 cloneObject(
        Path target,
        Object_1_0 original,
        boolean completelyDirty
    ) throws ServiceException {
        return getSink().getDelegate().cloneObject(
            target, 
            original, 
            completelyDirty
        );
    }

    /**
     * Clears the cache 
     */
    public void clear(
    ){
        super.clear();
        if(this.source instanceof ObjectFactory_1_3) {
            ((ObjectFactory_1_3)this.source).clear();
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.ObjectFactory_1_3#evict()
     */
    public void evict() {
        if(this.source instanceof ObjectFactory_1_3) {
            ((ObjectFactory_1_3)this.source).evict();
        }
    }
    
}
