/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: RefQuery_1.java,v 1.14 2009/06/02 23:18:31 hburger Exp $
 * Description: RefQuery_1 
 * Revision:    $Revision: 1.14 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/06/02 23:18:31 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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

package org.openmdx.base.accessor.jmi.spi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.Extent;
import javax.jdo.FetchPlan;
import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jmi.reflect.JmiException;
import javax.jmi.reflect.RefObject;

import org.oasisopen.jmi1.RefContainer;
import org.openmdx.base.accessor.jmi.cci.RefFilter_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.persistence.cci.Queries;
import org.openmdx.base.query.AttributeSpecifier;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.kernel.exception.BasicException;

/**
 * RefQuery_1
 *
 */
public class RefQuery_1
    extends RefFilter_1
    implements Query
{

    private boolean unique = false;
    private boolean unmodifiable = false;
    private boolean ignoreCache = false;
    private Map<String,Object> extensions = new HashMap<String,Object>();
    private transient Collection<?> pcs = null;
    private FetchPlan fetchPlan = null;

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3076448949052023632L;
    
    private static final String ARGUMENTS = 
        "Expression parsing and arguments not supported";

    private static final String PARSING = 
        "Expression parsing and arguments not supported";

    private static final String SETRANGE = 
        "set range operations are not supported. Use listIterator(position) instead";

    private static final String RESULT  = 
        "Result clases, projections and aggregate function results not supported";
    
    /**
     * Constructor 
     *
     * @param refPackage
     * @param filterType
     * @param filterProperties
     * @param attributeSpecifiers
     */
    protected RefQuery_1(
        RefPackage_1_0 refPackage,
        String filterType,
        FilterProperty[] filterProperties,
        AttributeSpecifier[] attributeSpecifiers
    ) {
        super(refPackage, filterType, filterProperties, attributeSpecifiers);
    }

    
    //------------------------------------------------------------------------
    // Extends RefFilter_1
    //------------------------------------------------------------------------
    
    private void assertModifiable(){
        if(isUnmodifiable()) {
            throw new JDOUserException("The query is unmodifiable");
        }
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.spi.RefFilter_1#refAddValue(org.openmdx.model1.accessor.basic.cci.ModelElement_1_0, int, short)
     */
    @Override
    public void refAddValue(ModelElement_1_0 featureDef, int index, short order) {
        assertModifiable();
        super.refAddValue(featureDef, index, order);
    }


    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.spi.RefFilter_1#refAddValue(org.openmdx.model1.accessor.basic.cci.ModelElement_1_0, short, short, java.util.Collection)
     */
    @Override
    public void refAddValue(
        ModelElement_1_0 featureDef,
        short quantor,
        short operator,
        Collection<?> value
    ) {
        assertModifiable();
        super.refAddValue(featureDef, quantor, operator, value);
    }


    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.spi.RefFilter_1#refAddValue(java.lang.String, int, short)
     */
    @Override
    public void refAddValue(
        String fieldName, 
        int index, 
        short order
    ) throws JmiException {
        assertModifiable();
        super.refAddValue(fieldName, index, order);
    }


    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.spi.RefFilter_1#refAddValue(java.lang.String, short, short, java.util.Collection)
     */
    @Override
    public void refAddValue(
        String fieldName,
        short quantor,
        short operator,
        Collection<?> value
    ) {
        assertModifiable();
        super.refAddValue(fieldName, quantor, operator, value);
    }


    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.spi.RefFilter_1#refAddValue(java.lang.String, short, short, org.openmdx.base.accessor.jmi.cci.RefFilter_1_0)
     */
    @Override
    public void refAddValue(
        String fieldName,
        short quantor,
        short operator,
        RefFilter_1_0 filter
    ) {
        assertModifiable();
        super.refAddValue(fieldName, quantor, operator, filter);
    }


    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.jmi.spi.RefFilter_1#refAddValue(java.lang.String, short)
     */
    @Override
    public void refAddValue(
        String fieldName, 
        short order
    ) throws JmiException {
        assertModifiable();
        super.refAddValue(fieldName, order);
    }


    /* (non-Javadoc)
     * @see javax.jdo.Query#addExtension(java.lang.String, java.lang.Object)
     */
    public void addExtension(String key, Object value) {
        assertModifiable();
        if(key.startsWith("org.openmdx.")) {
            if(Queries.FORCE_CACHE.equals(key)) {
                this.extensions.put(key, value);
            } else {
                throw BasicException.initHolder(
                    new JDOFatalUserException(
                        "Invalid openMDX extension",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_PARAMETER,
                            new BasicException.Parameter("key", key),
                            new BasicException.Parameter("value", value),
                            new BasicException.Parameter("supported", Queries.FORCE_CACHE)
                        )
                    )
                );
            }
        } 
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#close(java.lang.Object)
     */
    public void close(Object queryResult) {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#closeAll()
     */
    public void closeAll() {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#compile()
     */
    public void compile() {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#declareImports(java.lang.String)
     */
    public void declareImports(String imports) {
        throw new UnsupportedOperationException(ARGUMENTS);
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#declareParameters(java.lang.String)
     */
    public void declareParameters(String parameters) {
        throw new UnsupportedOperationException(ARGUMENTS);
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#declareVariables(java.lang.String)
     */
    public void declareVariables(String variables) {
        throw new UnsupportedOperationException(ARGUMENTS);
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#deletePersistentAll()
     */
    public long deletePersistentAll(
    ) {
        if(this.pcs instanceof RefContainer) {
            RefContainer refContainer = (RefContainer) this.pcs;
            return refContainer.refRemoveAll(this);
        } 
        else if (this.pcs == null) {
            throw new JDOUserException(
                "No candidates set"
            );
        } 
        else {
            throw new JDOUserException(
                "Unsupported candidate class: " + this.pcs.getClass().getName()
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#deletePersistentAll(java.util.Map)
     */
    @SuppressWarnings("unchecked")
    public long deletePersistentAll(Map parameters) {
        throw new UnsupportedOperationException(ARGUMENTS);
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#deletePersistentAll(java.lang.Object[])
     */
    public long deletePersistentAll(Object... parameters) {
        throw new UnsupportedOperationException(ARGUMENTS);
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#execute()
     */
    public Object execute(
    ) {
        if(this.pcs instanceof RefContainer) {
            if(Boolean.TRUE.equals(this.extensions.get(Queries.FORCE_CACHE))) {
                RefPackage_1_0 refPackage = (RefPackage_1_0) ((RefContainer)this.pcs).refOutermostPackage();
                refPackage.refPersistenceManager().retrieveAll(this.pcs);
            }
            RefContainer refContainer = (RefContainer) this.pcs;
            List<?> result = refContainer.refGetAll(this);
            if(this.unique) {
                Iterator<?> i = result.iterator();
                return i.hasNext() ? i.next() : null;
            }
            else {
                return result;
            }
        } 
        else if (this.pcs == null) {
            throw new JDOUserException(
                "No candidates set"
            );
        } 
        else {
            throw new JDOUserException(
                "Unsupported candidate class: " + this.pcs.getClass().getName()
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#execute(java.lang.Object, java.lang.Object, java.lang.Object)
     */
    public Object execute(Object p1, Object p2, Object p3) {
        throw new UnsupportedOperationException(ARGUMENTS);
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#execute(java.lang.Object, java.lang.Object)
     */
    public Object execute(Object p1, Object p2) {
        throw new UnsupportedOperationException(ARGUMENTS);
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#execute(java.lang.Object)
     */
    public Object execute(Object p1) {
        throw new UnsupportedOperationException(ARGUMENTS);
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#executeWithArray(java.lang.Object[])
     */
    public Object executeWithArray(Object... parameters) {
        throw new UnsupportedOperationException(ARGUMENTS);
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#executeWithMap(java.util.Map)
     */
    @SuppressWarnings("unchecked")
    public Object executeWithMap(Map parameters) {
        throw new UnsupportedOperationException(ARGUMENTS);
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#getFetchPlan()
     */
    public FetchPlan getFetchPlan() {
        return this.fetchPlan;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#getIgnoreCache()
     */
    public boolean getIgnoreCache() {
        return this.ignoreCache;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#getPersistenceManager()
     */
    public PersistenceManager getPersistenceManager() {
        RefPackage_1_0 refPackage = refGetPackage();
        return refPackage == null ? null : refPackage.refPersistenceManager();
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#isUnmodifiable()
     */
    public boolean isUnmodifiable() {
        return this.unmodifiable;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setCandidates(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    public void setCandidates(Collection pcs) {
        this.pcs = pcs;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setCandidates(javax.jdo.Extent)
     */
    @SuppressWarnings("unchecked")
    public void setCandidates(Extent pcs) {
        throw new UnsupportedOperationException(
            "Extent can't be set via JDO query yet"
        );
        
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setClass(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public void setClass(Class cls) {
        String packageId = refGetPackage().refPackage(cls.getName()).refMofId();
        if(
            !cls.isInterface() ||
            !RefObject.class.isAssignableFrom(cls)
        ) {
            throw new JDOUserException(
                "The JMI interface should be a subclass of RefObject: " + cls.getName()
            );
        }
        setInstanceOf( // TODO Reverse Lookup
            packageId.substring(0, packageId.lastIndexOf(':') + 1) + cls.getSimpleName()
        );
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setExtensions(java.util.Map)
     */
    @SuppressWarnings("unchecked")
    public void setExtensions(
        Map extensions
    ) {
        assertModifiable();
        this.extensions.clear();
        for(Map.Entry extension : (Set<Map.Entry<?,?>>)extensions.entrySet()) {
            Object key = extension.getKey();
            if(key instanceof String) {
                addExtension((String) key, extension.getValue());
            } else {
                throw new JDOUserException("The extension key must be a non-null String");
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setFilter(java.lang.String)
     */
    public void setFilter(
        String filter
    ) {
        throw new UnsupportedOperationException(PARSING);
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setGrouping(java.lang.String)
     */
    public void setGrouping(
        String group
    ) {
        throw new UnsupportedOperationException(PARSING);
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setIgnoreCache(boolean)
     */
    public void setIgnoreCache(
        boolean ignoreCache
    ) {
        this.ignoreCache = ignoreCache;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setOrdering(java.lang.String)
     */
    public void setOrdering(String ordering) {
        throw new UnsupportedOperationException(PARSING);
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setRange(long, long)
     */
    public void setRange(
        long fromIncl, 
        long toExcl
    ) {
        throw new UnsupportedOperationException(SETRANGE);
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setRange(java.lang.String)
     */
    public void setRange(String fromInclToExcl) {
        throw new UnsupportedOperationException(PARSING);
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setResult(java.lang.String)
     */
    public void setResult(String data) {
        throw new UnsupportedOperationException(RESULT);
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setResultClass(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public void setResultClass(Class cls) {
        throw new UnsupportedOperationException(RESULT);
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setUnique(boolean)
     */
    public void setUnique(boolean unique) {
        assertModifiable();
        this.unique = unique;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#setUnmodifiable()
     */
    public void setUnmodifiable() {
        this.unmodifiable = true;
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#addSubquery(javax.jdo.Query, java.lang.String, java.lang.String)
     */
    public void addSubquery(Query arg0, String arg1, String arg2) {
        throw new UnsupportedOperationException("Operation not supported by RefQuery_1");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#addSubquery(javax.jdo.Query, java.lang.String, java.lang.String, java.lang.String)
     */
    public void addSubquery(Query arg0, String arg1, String arg2, String arg3) {
        throw new UnsupportedOperationException("Operation not supported by RefQuery_1");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#addSubquery(javax.jdo.Query, java.lang.String, java.lang.String, java.lang.String[])
     */
    public void addSubquery(
        Query arg0,
        String arg1,
        String arg2,
        String... arg3
    ) {
        throw new UnsupportedOperationException("Operation not supported by RefQuery_1");        
    }

    /* (non-Javadoc)
     * @see javax.jdo.Query#addSubquery(javax.jdo.Query, java.lang.String, java.lang.String, java.util.Map)
     */
    @SuppressWarnings("unchecked")
    public void addSubquery(Query arg0, String arg1, String arg2, Map arg3) {
        throw new UnsupportedOperationException("Operation not supported by RefQuery_1");        
    }

}
