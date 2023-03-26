/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Persistence Helper 
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
package org.openmdx.base.persistence.cci;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import javax.jdo.Extent;
import javax.jdo.JDOFatalInternalException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jmi.reflect.RefObject;

import org.omg.mof.spi.Names;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.accessor.jmi.cci.RefQuery_1_0;
import org.openmdx.base.accessor.spi.PersistenceManager_1_0;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.ExtentCollection;
import org.openmdx.base.persistence.spi.FilterCollection;
import org.openmdx.base.persistence.spi.QueryExtension;
import org.openmdx.base.persistence.spi.UnitOfWorkFactory;
import org.openmdx.base.query.ConditionType;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.rest.cci.QueryExtensionRecord;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.jdo.ReducedJDOHelper;
import org.openmdx.kernel.loading.Classes;
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
     * The unit of work factory
     */
    private static UnitOfWorkFactory unitOfWorkFactory;
    
    /**
     * Retrieve transactionFactory.
     *
     * @return Returns the transactionFactory.
     */
    private static UnitOfWorkFactory getUnitOfWorkFactory() {
        if(unitOfWorkFactory == null) try {
            unitOfWorkFactory = Classes.newApplicationInstance(
                UnitOfWorkFactory.class, 
                "org.openmdx.application.persistence.adapter.UnitOfWorkAdapterFactory"
             );
        } catch (Exception exception) {
            throw new JDOFatalInternalException(
                "Transaction factory acquisition failure",
                exception
            );
        }
        return unitOfWorkFactory;
    }

    /**
     * Determine an object's class name<ul>
     * <li>{@code null} in case of a {@code null} object reference
     * <li>the MOF class' id in case of a JDO object
     * <li>the Java class' name in case of a non-JDO object
     * </ul<
     * 
     * @param pc the object for which its class name should be determined
     * 
     * @return the object's class name
     */
    public static String getClassName(
        Object pc
    ){
        return 
            pc == null ? null :
            pc instanceof RefObject ? ((RefObject)pc).refClass().refMofId() :
            pc.getClass().getName();
    }
    
    /**
     * Return a clone of the object
     * 
     * @param object
     * 
     * @return a clone, or {@code null} if the object is {@code null}
     * 
     * @exception RuntimeException if cloning fails or if the object is not cloneable 
     */
    @SuppressWarnings("unchecked")
    public static <T> T clone(
        T object,
        String... exclude
    ) {
        if(object instanceof org.openmdx.base.persistence.spi.Cloneable) {
            return ((org.openmdx.base.persistence.spi.Cloneable<T>)object).openmdxjdoClone(exclude);
        } else if(exclude != null && exclude.length > 0) {
        	throw new RuntimeServiceException(
	            BasicException.Code.DEFAULT_DOMAIN,
	            BasicException.Code.BAD_PARAMETER,
	            "Exclude is supported for org.openmdx.base.persistence.spi.Cloneable objects only",
	            new BasicException.Parameter("exclude", (Object[])exclude),
	            new BasicException.Parameter("supported", java.lang.Cloneable.class.getName(), org.openmdx.base.persistence.spi.Cloneable.class.getName()),
	            new BasicException.Parameter("class", object.getClass().getName())
	        );
        } else if(object instanceof java.lang.Cloneable) {
        	return Classes.clone(object);
        } else {
	        throw new RuntimeServiceException(
	            BasicException.Code.DEFAULT_DOMAIN,
	            BasicException.Code.NOT_SUPPORTED,
	            "A class not declared as Cloneable can't be cloned",
	            new BasicException.Parameter("supported", java.lang.Cloneable.class.getName(), org.openmdx.base.persistence.spi.Cloneable.class.getName()),
	            new BasicException.Parameter("class", object.getClass().getName())
	        );
        }
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
        PersistenceManager_1_0 pm = (PersistenceManager_1_0) ReducedJDOHelper.getPersistenceManager(pc);
        return pm.getFeatureReplacingObjectById(
            (UUID) ReducedJDOHelper.getTransactionalObjectId(pc), 
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
    public static QueryExtensionRecord newQueryExtension(
        AnyTypePredicate query
    ){
        QueryExtensionRecord queryExtension = new QueryExtension();
        ((RefQuery_1_0)query).refGetFilter().getExtension().add(queryExtension);
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
    
    @SuppressWarnings("unchecked")
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
     * @return the last segment of the actual or future XRI; or {@code null} if the object is not contained yet
     */
    public static String getLastXRISegment(
    	Object pc
    ){
        return ((PersistenceManager_1_0) ReducedJDOHelper.getPersistenceManager(pc)).getLastXRISegment(pc);
    }
    
    /**
     * Retrieve all descendants of a given object
     * 
     * @param pc
     * 
     * @exception NullPointerException if the argument is {@code null}
     * @exception IllegalArgumentException if the argument is not a {@code javax.jmi.reflect.RefObject} instance
     */
    public static void retrieveAllDescendants(
        Object pc
    ){
        if(!(pc instanceof RefObject)) {
            throw BasicException.initHolder(
                new IllegalArgumentException(
                    "The given argument is inapprpriate for descendant retrieval",
                    BasicException.newEmbeddedExceptionStack(
                        BasicException.Code.DEFAULT_DOMAIN, 
                        BasicException.Code.BAD_PARAMETER, 
                        new BasicException.Parameter("supported", RefObject.class.getName()),
                        new BasicException.Parameter("actual", pc.getClass().getName())
                    )
                )
            );        
        }
        try {
            RefObject refObject = (RefObject) pc;
            RefPackage_1_0 refPackage = (RefPackage_1_0) refObject.refOutermostPackage();
            retrieveAllDescendants(
                refPackage,
                (Path)ReducedJDOHelper.getObjectId(pc),
                refPackage.refModel().getElement(refObject.refClass().refMofId())
            );
        } catch (ServiceException exception) {
            throw new RuntimeServiceException(exception);
        }
    }

    private static void retrieveAllDescendants(
        RefPackage_1_0 refPackage,
        Path objectId,
        ModelElement_1_0 type
    ) throws ServiceException {
        Map<String,ModelElement_1_0> nonDerivedAttributes = refPackage.refModel().getStructuralFeatureDefs(
            type, 
            true, // includeSubtypes
            false, // includeDerived
            false // attributesOnly
        );  
        for(Map.Entry<String,ModelElement_1_0> e : nonDerivedAttributes.entrySet()){
            ModelElement_1_0 candidate = e.getValue();
            if(
                candidate.isReference() && 
                !ModelHelper.isStoredAsAttribute(candidate) &&
                ModelHelper.isCompositeEnd(candidate, false)
            ){
                Path childPattern = objectId.getDescendant(e.getKey(),":*");
                ModelElement_1_0 childType = refPackage.refModel().getElementType(candidate);
                String extentClassName = childType.getQualifiedName();
                Class<?> extentClass;
                try {
                    extentClass = Classes.getApplicationClass(
                        Names.toClassName(extentClassName, Names.JMI1_PACKAGE_SUFFIX)
                    );
                } catch (ClassNotFoundException exception) {
                    throw new ServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN, 
                        BasicException.Code.BAD_QUERY_CRITERIA, 
                        "Unable to retrieve extent class",
                        new BasicException.Parameter("feature", candidate.getQualifiedName()),
                        new BasicException.Parameter("pattern", childPattern),
                        new BasicException.Parameter("type", extentClassName)
                    );
                }
                Collection<?> extent = getCandidates(
                    refPackage.refPersistenceManager().getExtent(extentClass),
                    childPattern
                );
                refPackage.refPersistenceManager().retrieveAll(extent);
                retrieveAllDescendants(refPackage,childPattern,childType);
            }
                
        }
    }
    
    /**
     * Provide the persistence manager's current unit of work
     * 
     * @param persistenceManager
     * 
     * @return the persistence manager's current unit of work
     */
    public static UnitOfWork currentUnitOfWork(
        PersistenceManager persistenceManager
    ){
        return persistenceManager instanceof PersistenceManager_1_0 ?
            ((PersistenceManager_1_0)persistenceManager).currentUnitOfWork() :
            getUnitOfWorkFactory().toUnitOfWork(persistenceManager.currentTransaction());
    }

}
