/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: RequestedObject.java,v 1.15 2011/11/26 01:34:58 hburger Exp $
 * Description: Request Object
 * Revision:    $Revision: 1.15 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/11/26 01:34:58 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2011, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.cci;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.resource.cci.MappedRecord;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.BasicException;


/**
 * This implementation delegates to the AbstractReply's first object. 
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class RequestedObject
    implements DataproviderReplyListener, Serializable, ObjectRecord
{

    private static final long serialVersionUID = 3257565088054654263L;

    protected ObjectRecord getObject(
    ) {
        if (this.object == null) {
            throw new RuntimeServiceException(
                this.exception == null ?
                    new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN, 
                        BasicException.Code.ILLEGAL_STATE,
                        "The corresponding request has not been processed yet"
                    ) :
                    this.exception
            );
        }
        return this.object;
    }

    //------------------------------------------------------------------------
    public ServiceException getException(
    ) {
        return this.exception;
    }
    
    //------------------------------------------------------------------------
    // Implements DataproviderReplyListener
    //------------------------------------------------------------------------

    /**
     * Called if the work unit has been processed successfully
     */
    public void onReply(
        DataproviderReply reply
    ){
        MappedRecord[] objects = reply.getObjects();
        if(objects.length > 0) {
            this.object = (ObjectRecord)objects[0];
        } else {
            this.object = null;
        }
        this.exception = null;
    }

    /**
     * Called if the work unit processing failed
     */
    public void onException(
        ServiceException exception
    ){
        this.object = null;
        this.exception = exception;
    }

    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /**
     * Returns a string representation of the object. In general, the toString
     * method returns a string that "textually represents" this object. The
     * result should be a concise but informative representation that is easy
     * for a person to read. It is recommended that all subclasses override
     * this method. 
     *
     * @return the requested dataprovider object's string representation
     */
    @Override
    public String toString(
    ){
        return this.object != null ? this.object.toString() :
            this.exception != null ? this.exception.toString() :
                "n/a";
    }


    //------------------------------------------------------------------------
    // Variables
    //------------------------------------------------------------------------

    /**
     * The object if the request succeeds; null otherwise.
     */
    protected ObjectRecord object = null;

    /**
     * The exception if the request fails; null otherwise.
     */
    protected ServiceException exception = null;

    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.cci.DataproviderObject_1_0#addClones(org.openmdx.application.dataprovider.cci.DataproviderObject_1_0, boolean)
     */
    public boolean addClones(
        MappedRecord source, 
        boolean overwrite
    ) {
        throw new UnsupportedOperationException(); 
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#getRecordName()
     */
    public String getRecordName(
    ) {
        return this.getObject().getRecordName();
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#getRecordShortDescription()
     */
    public String getRecordShortDescription() {
        return this.getObject().getRecordShortDescription();
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#setRecordName(java.lang.String)
     */
    public void setRecordName(String name) {
        this.getObject().setRecordName(name);
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#setRecordShortDescription(java.lang.String)
     */
    public void setRecordShortDescription(String description) {
        this.getObject().setRecordShortDescription(description);
    }

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    public void clear() {
       this.getObject().clear();
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        return this.getObject().containsKey(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
        return this.getObject().containsValue(value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#entrySet()
     */
    public Set entrySet() {
        return this.getObject().entrySet();
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key) {
        return this.getObject().get(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return this.getObject().isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    public Set keySet() {
        return this.getObject().keySet();
    }

    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(Object key, Object value) {
        return this.getObject().put(key, value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#putAll(java.util.Map)
     */
    public void putAll(Map t) {
        this.getObject().putAll(t);
    }

    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(Object key) {
        return this.getObject().remove(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    public int size() {
        return this.getObject().size();
    }

    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
    public Collection values() {
        return this.getObject().values();
    }

    @Override
    public Object clone(
    ){
        try {
            return Object_2Facade.cloneObject(this.getObject());
        } catch(ServiceException exception) {
            throw new RuntimeServiceException(exception.getCause());
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.ObjectRecord#getPath()
     */
//  @Override
    public Path getPath() {
        return this.getObject().getPath();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.ObjectRecord#getValue()
     */
//  @Override
    public MappedRecord getValue() {
        return this.getObject().getValue();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.ObjectRecord#getVersion()
     */
//  @Override
    public Object getVersion() {
        return this.getObject().getVersion();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.ObjectRecord#setPath(org.openmdx.base.naming.Path)
     */
//  @Override
    public void setPath(Path path) {
        this.getObject().setPath(path);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.ObjectRecord#setValue(javax.resource.cci.MappedRecord)
     */
//  @Override
    public void setValue(MappedRecord value) {
        this.getObject().setValue(value);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.ObjectRecord#setVersion(java.lang.Object)
     */
//  @Override
    public void setVersion(Object version) {
        this.getObject().setVersion(version);
    }

}
