/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Stated Object Interceptor
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2012, OMEX AG, Switzerland
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
package org.openmdx.state2.aop1;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.openmdx.base.accessor.cci.Container_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.view.Interceptor_1;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;

/**
 * Stated Object Interceptor
 */
abstract public class Object_1 extends Interceptor_1 {

    /**
     * Constructor 
     *
     * @param self
     * @param next
     */
    protected Object_1(
        ObjectView_1_0 self, 
        Interceptor_1 next
    ) {
        super(self, next);
    }

    /**
     * The container cache
     */
    private ConcurrentMap<String,Container_1_0> containers = new ConcurrentHashMap<String,Container_1_0>();
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.DelegatingObject_1#objGetContainer(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Container_1_0 objGetContainer(
        String feature
    ) throws ServiceException {
        Container_1_0 container = containers.get(feature);
        if(container == null) {
            DataObject_1_0 dataObject = this.self.objGetDelegate();
            String coreClass = dataObject.objGetClass(); 
            Map<String, ModelElement_1_0> references = (Map<String, ModelElement_1_0>) this.getModel().getElement(
                coreClass
            ).objGetValue(
                "reference"
            );
            ModelElement_1_0 reference = references.get(feature);
            if(reference == null) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_MEMBER_NAME,
                    "The requested reference is not a member of the containing class",
                    new BasicException.Parameter("class", coreClass),
                    new BasicException.Parameter("feature", feature)
                );
            }
            String type = ((Path)reference.objGetValue("type")).getBase();
            //
            // A state capable container is required even if there is no state context  
            // in order to avoid the returning of states not requested explicitly.
            //
            container = reference.getModel().isSubtypeOf(type, "org:openmdx:state2:StateCapable") ? newStateCapableContainer(
                this.self,
                dataObject.objGetContainer(feature), 
                type
            ) : super.objGetContainer(
                feature
            );
            Container_1_0 concurrent = this.containers.putIfAbsent(feature, container);
            return concurrent == null ? container : concurrent;
        } else {
            return container;
        }
    }

    /**
     * Create a <code>StateCapable</code> container
     * 
     * @param parent
     * @param container
     * @param type
     * @return a new <code>StateCapable</code> container
     * 
     * @throws ServiceException
     */
    protected abstract Container_1_0 newStateCapableContainer(
        ObjectView_1_0 parent,
        Container_1_0 container, 
        String type 
    ) throws ServiceException;
        
}
