/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Abstract org::openmdx::state2 Plug-In
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2012, OMEX AG, Switzerland
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
package org.openmdx.state2.aop0;

import java.util.List;

import javax.jdo.listener.InstanceLifecycleEvent;

import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.rest.DataObject_1;
import org.openmdx.base.accessor.rest.UnitOfWork_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.UserObjects;
import org.openmdx.state2.spi.LegacyConfiguration;
import org.openmdx.state2.spi.LegacyPlugInHelper;

/**
 * Add valid time unique support org::openmdx::state2 Plug-In
 */
public class LegacyPlugIn_1 extends ColonPlugIn_1 implements LegacyConfiguration {

    /**
     * To support the lack of valid-time-unique patterns
     */
    private static final Path[] NO_XRIS = {};

    /**
     * To support the lack of valid-time-unique containers
     */
    private static final String[] NO_TYPES = {};

    /**
     * Valid-time-unique XRI patterns
     */
    private Path[] validTimeUniquePattern = NO_XRIS;

    /**
     * Valid-time-unique types
     */
    private String[] validTimeUniqueTypes = NO_TYPES;
    
    /**
     * Defines where the valid time of state capable objects is unique
     * 
     * @param values the object id patterns
     */
    public void setValidTimeUniquePattern(
        String[] values
        ) {
        if(values == null || values.length == 0) {
            this.validTimeUniquePattern = NO_XRIS;
        } else {
            this.validTimeUniquePattern = new Path[values.length];
            int i = 0;
            for (String value : values) {
                this.validTimeUniquePattern[i++] = new Path(value);
            }
        }
    }

    /**
     * Tells where the valid time of state capable objects is unique
     * 
     * @return the object id patterns
     */
    public String[] getValidTimeUniquePattern(
        ) {
        String[] validTimeUniquePattern = new String[this.validTimeUniquePattern.length];
        int i = 0;
        for (Path value : this.validTimeUniquePattern) {
            validTimeUniquePattern[i++] = value.toXRI();
        }
        return validTimeUniquePattern;
    }

    /**
     * Defines where the valid time of state capable objects is unique
     * 
     * @param index
     * @param value
     */
    public void setValidTimeUniquePattern(int index, String value) {
        this.validTimeUniquePattern[index] = new Path(value);
    }

    /**
     * Tells where the valid time of state capable objects is unique
     * 
     * @param index
     * 
     * @return an object id pattern
     */
    public String getValidTimeUniquePattern(
        int index
        ) {
        return this.validTimeUniquePattern[index].toXRI();
    }

    /**
     * @return the validTimeUniqueTypes
     */
    public String[] getValidTimeUniqueType() {
        return validTimeUniqueTypes;
    }

    /**
     * @return the validTimeUniqueTypes
     */
    public String getValidTimeUniqueType(int index) {
        return validTimeUniqueTypes[index];
    }

    /**
     * @param validTimeUniqueTypes the validTimeUniqueTypes to set
     */
    public void setValidTimeUniqueType(String[] validTimeUniqueTypes) {
        this.validTimeUniqueTypes = validTimeUniqueTypes;
    }

    /**
     * @param validTimeUniqueType the validTimeUniqueTypes to set
     */
    public void setValidTimeUniqueType(int index, String validTimeUniqueType) {
        this.validTimeUniqueTypes[index] = validTimeUniqueType;
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.spi.Configuration#isValidTimeUnique(org.openmdx.base.naming.Path)
     */
//  @Override
    public boolean isValidTimeUnique(Path xri) {
        if(this.validTimeUniquePattern != NO_XRIS){
            Path oid = xri.size() % 2 == 1 ? xri : xri.getChild("-");
            for(Path validTimeUniqePattern : this.validTimeUniquePattern) {
                if(oid.isLike(validTimeUniqePattern)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.spi.Configuration#isTheChildrensValidTimeUnique(org.openmdx.base.accessor.view.ObjectView_1_0)
     */
//  @Override
    public boolean isTheChildrensValidTimeUnique(ModelElement_1_0 parentClassifierDef) throws ServiceException {
        if(this.validTimeUniqueTypes != NO_TYPES) {
            for(String validTimeUniqueType : this.validTimeUniqueTypes) {
                if(validTimeUniqueType.equals(parentClassifierDef.objGetValue("qualifiedName"))){
                    return true;
                }
                for(Object subType : parentClassifierDef.objGetList("supertype")) {
                    if(((Path)subType).getBase().equals(validTimeUniqueType)) {
                        return true;
                    }
                }            
            }
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.aop0.PlugIn_1_0#getPlugInObject(java.lang.Class)
     */
//  @Override
    public <T> T getPlugInObject(Class<T> type) {
        return LegacyConfiguration.class == type ? type.cast(this) : null;
    }
        
    /* (non-Javadoc)
     * @see org.openmdx.state2.aop0.AbstractPlugIn_1#getBasicStateQualifier(org.openmdx.base.accessor.rest.DataObject_1, java.lang.String)
     */
    @Override
    protected String getBasicStateQualifier(
        DataObject_1 object,
        String qualifier
    ) throws ServiceException {
        return isLegacyWithValidTimeUnique(object) ? qualifier : super.getBasicStateQualifier(object, qualifier);
    }

    /**
     * Tells whether a transient object's valid time is unique 
     * 
     * @param object
     * 
     * @return the value of the validTimeUnique property
     * 
     * @throws ServiceException
     */
    protected boolean isLegacyWithValidTimeUnique(
        DataObject_1 object
    ) throws ServiceException {
        return 
            isInstanceOf(object, "org:openmdx:state2:Legacy") && 
            Boolean.TRUE.equals(object.objGetValue("validTimeUnique"));
    }

    private boolean isValidTimeUnqiue(DataObject_1 object) throws ServiceException {
        return object.jdoIsPersistent() ? isValidTimeUnique(object.jdoGetObjectId()) : isLegacyWithValidTimeUnique(object);
    }

    @Override
    protected boolean isStateOnly(DataObject_1 object) throws ServiceException {
        return super.isStateOnly(object) && !isValidTimeUnqiue(object);
    }

    @Override
    public boolean isExemptFromValidation(
        DataObject_1 object, 
        ModelElement_1_0 feature
    ) throws ServiceException {
        return super.isExemptFromValidation(object, feature) || ( 
            "org:openmdx:base:Aspect:core".equals(feature.objGetValue("qualifiedName")) && isValidTimeUnqiue(object)
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.aop0.AbstractPlugIn_1#basicStatePreStore(org.openmdx.base.accessor.rest.DataObject_1)
     */
    @Override
    protected void basicStatePreStore(
        InstanceLifecycleEvent event
    ) throws ServiceException {
        DataObject_1 persistentInstance = (DataObject_1) event.getPersistentInstance();
        if(!persistentInstance.jdoIsDeleted() && isValidTimeUnqiue(persistentInstance)) {
            UnitOfWork_1 unitOfWork = persistentInstance.getUnitOfWork();
            persistentInstance.objSetValue(SystemAttributes.MODIFIED_AT, unitOfWork.getTransactionTime());
            List<Object> modifiedBy = persistentInstance.objGetList(SystemAttributes.MODIFIED_BY);
            modifiedBy.clear();
            modifiedBy.addAll(UserObjects.getPrincipalChain(persistentInstance.jdoGetPersistenceManager()));                
        } else {
            super.basicStatePreStore(event);
        }
    }

	/* (non-Javadoc)
	 * @see org.openmdx.base.aop0.PlugIn_1_0#isAspect(org.openmdx.base.accessor.rest.DataObject_1)
	 */
    @Override
	public Boolean isAspect(
		DataObject_1 object
	) throws ServiceException {
		return LegacyPlugInHelper.isValidTimeUnique(object, object.getModel()) ? Boolean.FALSE : null;
	}

}