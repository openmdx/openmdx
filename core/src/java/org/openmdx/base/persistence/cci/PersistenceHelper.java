/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: PersistenceHelper.java,v 1.30 2011/11/19 16:38:04 hburger Exp $
 * Description: PersistenceHelper 
 * Revision:    $Revision: 1.30 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/11/19 16:38:04 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2011, OMEX AG, Switzerland
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
package org.openmdx.base.persistence.cci;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import javax.jdo.Extent;
import javax.jdo.JDOHelper;
import javax.jdo.Query;
import javax.jmi.reflect.RefObject;

import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.jmi.cci.RefQuery_1_0;
import org.openmdx.base.accessor.spi.PersistenceManager_1_0;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.ExtentCollection;
import org.openmdx.base.persistence.spi.FilterCollection;
import org.openmdx.base.persistence.spi.QueryExtension;
import org.openmdx.base.query.ConditionType;
import org.openmdx.base.query.Extension;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.Quantifier;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.AnyTypePredicate;

/**
 * PersistenceHelper
 */
public class PersistenceHelper {

    /**
     * Constructor 
     */
    private PersistenceHelper() {
        // Avoid instantiation
    }

    /**
     * Return a clone of the object
     * 
     * @param object
     * 
     * @return a clone, or <code>null</code> if the class is not cloneable
     * 
     * @exception RuntimeException if cloning fails 
     */
    @SuppressWarnings("unchecked")
    public static <T> T clone(
        T object,
        String... exclude
    ) {
        if(object instanceof org.openmdx.base.persistence.spi.Cloneable) {
            return ((org.openmdx.base.persistence.spi.Cloneable<T>)object).openmdxjdoClone(exclude);
        }
        if(object instanceof java.lang.Cloneable) try {
            return (T) object.getClass(
            ).getMethod(
                "clone"
            ).invoke(
                object
            );
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new RuntimeServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.GENERIC,
                "A class declared as Cloneable can't be cloned",
                new BasicException.Parameter("interface", java.lang.Cloneable.class.getName()),
                new BasicException.Parameter("class", object.getClass().getName())

            );
        }
        return null;
    }

    /**
     * A way to avoid fetching an object just to retrieve its object id
     * 
     * @param pc a persistence capable object
     * @param featureName
     * 
     * @return the value where each object is replaced by its id
     */
    public static Object getFeatureReplacingObjectById(
        Object pc,
        String featureName
    ){  
        PersistenceManager_1_0 pm = (PersistenceManager_1_0) JDOHelper.getPersistenceManager(pc);
        return pm.getFeatureReplacingObjectById(
            (UUID) JDOHelper.getTransactionalObjectId(pc), 
            featureName
        );
    }

    /**
     * Retrieve a candidate collection
     * 
     * @param extent the extent
     * @param pattern the object id pattern either as a Path or XRI string representation
     * 
     * @return the candidate collection
     */
    public static Query newQuery(
        Extent<?> extent,
        Object xriPattern
    ){
        Query query = extent.getPersistenceManager().newQuery(extent);
        query.setCandidates(getCandidates(extent, xriPattern));
        return query;
    }

    /**
     * Create and register a query extension
     * 
     * @param query the query to be amended
     * 
     * @return the new query extension 
     */
    public static Extension newQueryExtension(
        AnyTypePredicate query
    ){
        Extension queryExtension = new QueryExtension();
        ((Query)query).addExtension(Queries.QUERY_EXTENSION, queryExtension);
        return queryExtension;
    }
    
    /**
     * Retrieve a candidate collection
     * 
     * @param extent the extent
     * @param xriPattern the object id pattern either as a Path or XRI string representation
     * 
     * @return the candidate collection
     * 
     * @exception ClassCastException if the xriPattern is neither assignable to 
     * org.openmdx.base.naming.Path nor to java-lang.String
     * @exception RuntimeServiceException in case of an invalid xriPattern String
     */
    public static <E> Collection<E> getCandidates(
        Extent<E> extent,
        Object xriPattern
    ){
        return new ExtentCollection<E>(
            extent, 
            xriPattern instanceof String ? new Path((String)xriPattern) : (Path)xriPattern
        );
    }

    /**
     * Retrieve a filter collection for a sub-query
     * 
     * @param predicate the predicate to be used as sub-query
     * 
     * @return the filter collection
     * 
     * @exception NullPointerException if the predicate is null
     * @exception IllegalArgumentException if the predicate is neither a Filter nor a
     * RefPRedicate_1_0.
     */
    public static <E> Collection<E> asSubquery(
        AnyTypePredicate predicate
    ){
        if (predicate instanceof RefQuery_1_0) {
            return new FilterCollection<E>(((RefQuery_1_0)predicate).refGetFilter());
        } else if(predicate instanceof Filter) {
            return new FilterCollection<E>((Filter)predicate);
        } else if(predicate == null) {
            throw new NullPointerException("The predicate must not be null");
        } else {
            throw BasicException.initHolder(
                new IllegalArgumentException(
                    "The given argument is inapprpriate for creating a filter collection",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN, 
                        BasicException.Code.BAD_PARAMETER, 
                        new BasicException.Parameter("supported", Filter.class.getName(), RefQuery_1_0.class.getName()),
                        new BasicException.Parameter("actual", predicate.getClass().getName())
                    )
                )
            );
        }
    }
    
    public static void setClasses(
        AnyTypePredicate query,
        Class<? extends RefObject>... classes 
    ){
        ((RefQuery_1_0)query).refAddValue(
            SystemAttributes.OBJECT_INSTANCE_OF,
            Quantifier.THERE_EXISTS,
            ConditionType.IS_IN,
            Arrays.asList(classes)
        );
    }
    
    /**
     * Retrieve the object's last XRI segment
     * 
     * @return the last segment of the actual or future XRI; or <code>null</code> if the object is not contained yet
     */
    public static String getLastXRISegment(
    	Object pc
    ){
        PersistenceManager_1_0 pm = (PersistenceManager_1_0) JDOHelper.getPersistenceManager(pc);
    	return pm.getLastXRISegment(pc);
    }
    
}

