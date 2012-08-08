/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: PlugIn_1.java,v 1.6 2010/01/26 15:44:02 hburger Exp $
 * Description: Standard Plug-In
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/01/26 15:44:02 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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

import org.openmdx.state2.spi.Configuration;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.rest.DataObject_1;
import org.openmdx.base.accessor.rest.UnitOfWork_1;
import org.openmdx.base.accessor.spi.ExceptionHelper;
import org.openmdx.base.aop0.PlugIn_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.naming.PathComponent;
import org.openmdx.kernel.exception.BasicException;

/**
 * Standard Plug-In
 */
public class PlugIn_1 implements Configuration, PlugIn_1_0 {

    /**
     * Enumerate the known aspect classes
     */
    private static final String[] ASPECT_TYPE = {
        "org:openmdx:state2:DateState",
        "org:openmdx:state2:DateTimeState"
    };
    
    /**
     * The XRI pattern of the objects to be audited
     */
    protected Path[] validTimeUniquePattern;

    /**
     * Defines where the valid time of state capable objects is unique
     * 
     * @param value the object id patterns
     */
    public void setValidTimeUniquePattern(String[] values) {
        this.validTimeUniquePattern = new Path[values.length];
        int i = 0;
        for (String value : values) {
            this.validTimeUniquePattern[i++] = new Path(value);
        }
    }

    /**
     * Tells where the valid time of state capable objects is unique
     * 
     * @return the object id patterns
     */
    public String[] getValidTimeUniquePattern() {
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
    public String getValidTimeUniquePattern(int index) {
        return this.validTimeUniquePattern[index].toXRI();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop0.PlugIn_1_0#beforeCompletion(org.openmdx.base.accessor.rest.UnitOfWork_1)
     */
    public void beforeCompletion(UnitOfWork_1 dataObjectManager) {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop0.PlugIn_1_0#setCore(org.openmdx.base.accessor.rest.DataObject_1, org.openmdx.base.accessor.rest.DataObject_1)
     */
    public void postSetCore(
        DataObject_1 target, 
        DataObject_1 core
    ) throws ServiceException {
        Model_1_0 model = target.jdoGetPersistenceManager().getModel();
        for(String aspectType : ASPECT_TYPE) {
            if(model.isInstanceof(target, aspectType)) {
                if(target == core){
                    core.objSetValue("validTimeUnique", Boolean.TRUE);
                    return;
                }
                if(model.isInstanceof(core, "org:openmdx:state2:StateCapable")) {
                    core.getAspect(aspectType).values().add(target);
                    return;
                }
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    "A BasicState's core object must be StateCapable",
                    ExceptionHelper.newObjectIdParameter("state", this),
                    ExceptionHelper.newObjectIdParameter("core", core),
                    new BasicException.Parameter("expected","org:openmdx:state2:StateCapable"),   
                    new BasicException.Parameter("actual",core == null ? null : core.objGetClass())   
                );
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop0.PlugIn_1_0#getQualifier(org.openmdx.base.accessor.rest.DataObject_1, java.lang.String)
     */
    public String getQualifier(
        DataObject_1 object, 
        String qualifier
    ) throws ServiceException {
        if(object.jdoGetPersistenceManager().getModel().isInstanceof(object, "org:openmdx:state2:BasicState")) {
            if(!Boolean.TRUE.equals(object.objGetValue("validTimeUnique"))) {
                DataObject_1_0 core = (DataObject_1_0) object.objGetValue("core");
                if(qualifier == null || core == null) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        "A state is added to the container through its core reference ony",
                        ExceptionHelper.newObjectIdParameter("id", this),
                        new BasicException.Parameter("qualifier", qualifier),
                        ExceptionHelper.newObjectIdParameter("core", core)
                     );
                }
                 PathComponent placeholder = new PathComponent(qualifier);
                 if(placeholder.isPlaceHolder()){
                     if(placeholder.size() != 3) throw new ServiceException(
                         BasicException.Code.DEFAULT_DOMAIN,
                         BasicException.Code.BAD_PARAMETER,
                         "The qualifier has an unexpected format for a state's placeholder",
                         ExceptionHelper.newObjectIdParameter("id", this),
                         new BasicException.Parameter("qualifier",qualifier)
                     );
                     Integer id = (Integer) core.objGetValue("stateVersion");
                     if(id == null) {
                         id = Integer.valueOf(0);
                     }
                     core.objSetValue(
                         "stateVersion",
                         Integer.valueOf(id.intValue() + 1)
                     );
                     return new PathComponent(
                         placeholder.getParent().getSuffix(1)
                     ).getDescendant(
                         id.toString(),
                         ""
                     ).toString();
                 } else {
                     Path corePath = core.jdoGetObjectId();
                     if(corePath != null) {
                         StringBuilder aspectQualifier = new StringBuilder(corePath.getBase());
                         for(
                             int i = 1;
                             i < placeholder.size();
                             i++
                         ){
                             String aspectId = placeholder.get(i);
                             if(!aspectId.startsWith("!") && !aspectId.startsWith("*")) {
                                 aspectQualifier.append('*');
                             }
                             aspectQualifier.append(aspectId);
                         }
                         return aspectQualifier.toString();
                     }
                 }
            }
        }
        return qualifier;        
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.aop0.PlugIn_1_0#getSharedObject()
     */
    @Override
    public Object getUserObject(Object key) {
        return Configuration.class == key ? this : null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.state2.spi.Configuration#isValidTimeUnique(org.openmdx.base.naming.Path)
     */
    @Override
    public boolean isValidTimeUnique(Path xri) {
        Path oid = xri.size() % 2 == 1 ? xri : xri.getChild("-");
        for(Path validTimeUniqePattern : this.validTimeUniquePattern) {
            if(oid.isLike(validTimeUniqePattern)) {
                return true;
            }
        }
        return false;
    }

}
