/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: State Import Plug-In
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
package org.openmdx.application.xml.jmi;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javax.jdo.JDOException;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jmi.reflect.JmiException;
import javax.jmi.reflect.RefObject;
import #if JAVA_8 javax.resource.cci.MappedRecord #else jakarta.resource.cci.MappedRecord #endif;
import javax.xml.datatype.XMLGregorianCalendar;

import org.oasisopen.cci2.QualifierType;
import org.oasisopen.jmi1.RefContainer;
import org.openmdx.application.dataprovider.cci.JmiHelper;
import org.openmdx.application.xml.spi.ImportMode;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.spi.PersistenceManager_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.jdo.ReducedJDOHelper;
import org.openmdx.state2.cci.DateStateViews;
import org.openmdx.state2.jmi1.DateState;
import org.openmdx.state2.jmi1.Legacy;
import org.openmdx.state2.jmi1.StateCapable;
import org.openmdx.state2.spi.DateStateViewContext;
import org.openmdx.state2.spi.Parameters;
import org.openmdx.state2.spi.TechnicalAttributes;

/**
 * State Import Plug-In
 */
public class StateImportPlugIn implements ImportPlugIn {

    /**
     * Constructor 
     *
     * @param delegate
     */
    public StateImportPlugIn(
        ImportPlugIn delegate
    ) {
        this.delegate = delegate;
    }

    /**
     * The delegate
     */
    private final ImportPlugIn delegate;
    
    /**
     * The features not to be propagated by the JMI helper for {@code {@code StateCapable} instances
     */
    private static final Collection<String> IGNORABLE_FEATURES_FOR_STATE_CAPABLE_INSTANCES = Arrays.asList(
        SystemAttributes.CORE,
        TechnicalAttributes.STATE_VALID_FROM,
        TechnicalAttributes.STATE_VALID_TO
    );
    
    /* (non-Javadoc)
     * @see org.openmdx.application.xml.jmi.ImportPlugIn#getInstance(javax.jdo.PersistenceManager, javax.resource.cci.MappedRecord, java.lang.Class, boolean, boolean)
     */
    @SuppressWarnings("unchecked")
    public <T extends RefObject> T getInstance(
        PersistenceManager persistenceManager,
        ImportMode mode,
        MappedRecord objectHolder,
        Class<T> objectClass
    ) throws ServiceException {
        if(DateState.class.isAssignableFrom(objectClass)) {
            Object_2Facade facade = Facades.asObject(objectHolder);
            if(Boolean.TRUE.equals(facade.attributeValue(TechnicalAttributes.VALID_TIME_UNIQUE))) {
                Path externalId = Object_2Facade.getPath(objectHolder);
                T refObject = null;
                switch(mode) {
                    case UPDATE:
                        try {
                            refObject = (T) persistenceManager.getObjectById(externalId);
                        } catch (JDOObjectNotFoundException exception) {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.NOT_FOUND,
                                "Could find neither persistent nor transient object for the given id",
                                new BasicException.Parameter("externalId", externalId)
                            ); 
                        } catch (JDOException exception) {
                            throw new ServiceException(exception);
                        }
                    case SET:
                        try {
                            refObject = (T) persistenceManager.getObjectById(externalId);
                        } catch (JDOObjectNotFoundException exception) {
                            refObject = persistenceManager.newInstance(objectClass);
                            this.makeObjectWithUnqiueTimesPersistent(persistenceManager, externalId, refObject);
                        } catch (JDOException exception) {
                            throw new ServiceException(exception);
                        }
                        break;
                    case CREATE:
                        refObject = persistenceManager.newInstance(objectClass);
                        this.makeObjectWithUnqiueTimesPersistent(persistenceManager, externalId, refObject);
                        break;
                }
                return refObject;
            } else if (facade.attributeValue(SystemAttributes.REMOVED_AT) != null){
            	return null;
            } else {
                XMLGregorianCalendar validFrom =  (XMLGregorianCalendar) facade.attributeValue(TechnicalAttributes.STATE_VALID_FROM);
                XMLGregorianCalendar validTo = (XMLGregorianCalendar) facade.attributeValue(TechnicalAttributes.STATE_VALID_TO);
                Path objectId = (Path)facade.attributeValue(SystemAttributes.CORE);
                if(objectId == null) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        "Missing core value, objectId can't be determined",
                        new BasicException.Parameter("externalId", facade.getPath())
                    );
                }
                switch(mode) {
                    case UPDATE:
                        try {
                        	RefObject core = (RefObject) persistenceManager.getObjectById(objectId);
                            return DateStateViews.<RefObject,T>getViewForTimeRange(
                            	core, 
                                validFrom, 
                                validTo
                            );
                        } catch (JDOObjectNotFoundException exception) {
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.NOT_FOUND,
                                "Could find no valid state in the given time range",
                                new BasicException.Parameter("objectId", objectId),
                                new BasicException.Parameter("externalId", facade.getPath())
                            ); 
                        } catch (JDOException exception) {
                            throw new ServiceException(exception);
                        }
                    case SET:
                        try {
                            RefContainer<?> refContainer = (RefContainer<?>) persistenceManager.getObjectById(objectId.getParent());
                            String qualifier = objectId.getLastSegment().toClassicRepresentation();
                            StateCapable core = qualifier.startsWith("!") ? (StateCapable) refContainer.refGet(
                                QualifierType.PERSISTENT,
                                qualifier.substring(1)
                            ) : (StateCapable) refContainer.refGet(
                                QualifierType.REASSIGNABLE,
                                qualifier
                            );
                            if(core != null) {
                                return (T) DateStateViews.getViewForInitializedState(
                                    (Class<? extends DateState>) objectClass,
                                    core, 
                                    validFrom, 
                                    validTo, 
                                    true // override
                                );
                            } // else fall through to CREATE
                        } catch (JDOObjectNotFoundException exception) {
                            // fall through to CREATE
                        } catch (JDOException exception) {
                            throw new ServiceException(exception);
                        } catch (JmiException exception) {
                            throw new ServiceException(exception);
                        }
                    case CREATE: 
                        try {
                            StateCapable core = this.delegate.getInstance(
                                persistenceManager, 
                                ImportMode.UPDATE, 
                                Facades.newObject(
                                    (Path)facade.getValue().get(SystemAttributes.CORE)
                                ).getDelegate(), 
                                StateCapable.class
                            );
                            PersistenceManager viewManager = ((PersistenceManager_1_0)persistenceManager).getPersistenceManager(
                                DateStateViewContext.newTimeRangeViewContext(validFrom, validTo)
                            );
                            T view = viewManager.newInstance(objectClass); // states must not be initialized!
                            DateStateViews.linkStateAndCore((DateState)view,core);
                            return viewManager.newInstance(objectClass); 
                        } catch (JDOException exception) {
                            throw new ServiceException(exception);
                        } catch (JmiException exception) {
                            throw new ServiceException(exception);
                        }
                }
            }
        }
        return this.delegate.getInstance(
            persistenceManager, 
            mode, 
            objectHolder, 
            objectClass
         );
    }

    /**
     * Initialize a (potentially new) object.
     * 
     * @param objectId
     * @param refObject
     * 
     * @throws ServiceException
     */
    private void makeObjectWithUnqiueTimesPersistent(
        PersistenceManager persistenceManager,
        Path objectId,
        RefObject refObject
    ) throws ServiceException{
        //
        // Set the validTimeUnique flag
        //
        refObject.refSetValue(TechnicalAttributes.VALID_TIME_UNIQUE, Boolean.TRUE);
        refObject.refSetValue(TechnicalAttributes.TRANSACTION_TIME_UNIQUE, Boolean.TRUE);
        //
        // Make transient instances persistent
        //
        Path containerId = objectId.getParent();
        String qualifier = objectId.getLastSegment().toClassicRepresentation();
        RefContainer<?> refContainer = (RefContainer<?>) persistenceManager.getObjectById(containerId);
        if(qualifier.startsWith("!")) {
            refContainer.refAdd(
                QualifierType.PERSISTENT,
                qualifier.substring(1),
                refObject
            );
        } else {
            refContainer.refAdd(
                QualifierType.REASSIGNABLE,
                qualifier,
                refObject
            );
        }
    }
    
    /**
     * Tests the "validTimeUnqiue" flag
     * 
     * @param target
     * @param source
     * 
     * @return {@code true} in case of unique valid time
     * 
     * @throws ServiceException 
     */
    private static boolean isValidTimeUnique(
        RefObject target,
        MappedRecord source
    ) throws ServiceException {
        return target instanceof Legacy && Boolean.TRUE.equals(
            Facades.asObject(source).attributeValue(TechnicalAttributes.VALID_TIME_UNIQUE)
        );
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.application.xml.jmi.ImportPlugIn#prepareInstance(javax.jmi.reflect.RefObject, javax.resource.cci.MappedRecord, java.util.Map)
     */
    public void prepareInstance(
        PersistenceManager persistenceManager,
        RefObject refObject,
        MappedRecord objectHolder, 
        Map<Path, RefObject> cache
     ) throws ServiceException {
        if(
		    refObject instanceof DateState && 
		    !StateImportPlugIn.isValidTimeUnique(refObject, objectHolder)
		) {
		    DateState dateState = (DateState) refObject;
		    JmiHelper.toRefObject(
		        objectHolder, // source
		        dateState, // target
		        cache, // maps externalIds to refObjects
		        StateImportPlugIn.IGNORABLE_FEATURES_FOR_STATE_CAPABLE_INSTANCES, 
		        !Parameters.STRICT_QUERY
		    );
		    if(!ReducedJDOHelper.isPersistent(refObject)){
		        Path objectId = (Path)Facades.asObject(objectHolder).attributeValue(SystemAttributes.CORE);
	            if(objectId == null) {
	                throw new ServiceException(
	                    BasicException.Code.DEFAULT_DOMAIN,
	                    BasicException.Code.BAD_PARAMETER,
	                    "Missing core value, objectId can't be determined",
	                    new BasicException.Parameter("externalId", Facades.asObject(objectHolder).getPath())
	                );
	            }
	            dateState.setCore(
	                this.getInstance(
	                    persistenceManager,
	                    ImportMode.UPDATE,
	                    Facades.newObject(objectId).getDelegate(), 
	                    StateCapable.class
	                )
	            );
		    }
		} else {
		    this.delegate.prepareInstance(
		        persistenceManager, 
		        refObject, 
		        objectHolder, 
		        cache
		    );
		}
    }

}
