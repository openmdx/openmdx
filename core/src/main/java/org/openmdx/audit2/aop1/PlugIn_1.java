/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Audit Plug-in
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
package org.openmdx.audit2.aop1;

import #if JAVA_8 javax.resource.cci.InteractionSpec #else jakarta.resource.cci.InteractionSpec #endif;

import org.openmdx.audit2.spi.Configuration;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.rest.DataObject_1;
import org.openmdx.base.accessor.view.Interceptor_1;
import org.openmdx.base.accessor.view.ObjectView_1_0;
import org.openmdx.base.aop1.PlugIn_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.jdo.ReducedJDOHelper;

/**
 * Audit Plug-in
 */
public class PlugIn_1 implements PlugIn_1_0 {

    /**
     * The data-prefix to audit-prefix mapping
     */
    private transient Configuration configuration;

    /**
     * Retrieve the audit configuration
     * 
     * @param context the context is used to to retrieve the persistence manager
     * 
     * @return the audit configuration
     */
    private Configuration getConfiguration(
        DataObject_1_0 context
    ){
        if(this.configuration == null) {
            this.configuration = (Configuration) ReducedJDOHelper.getPersistenceManager(
                context
            ).getUserObject(
                Configuration.class
            );
        }
        return this.configuration;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.aop1.PlugIn#getInterceptor(org.openmdx.base.accessor.view.Interceptor_1)
     */
    public Interceptor_1 getInterceptor(
        final ObjectView_1_0 view,
        final Interceptor_1 next
    ) throws ServiceException {
        final DataObject_1_0 dataObject = view.objGetDelegate();
        if(dataObject.jdoIsPersistent()) {
            final Path objectId = view.jdoGetObjectId();
            final Configuration configuration = this.getConfiguration(dataObject);
            if(objectId.startsWith(configuration.getAuditSegmentId(view.jdoGetPersistenceManager()))) {
                String type = objectId.getSegment(objectId.size() - 2).toClassicRepresentation();
                //
                // org::openmdx::audit2
                //
                if("unitOfWork".equals(type)) {
                    return new UnitOfWork_1(view, next);
                }
                if("involvement".equals(type)) {
                    return new Involvement_1(view, next);
                }
            }
        }
        return next;
    }

	/* (non-Javadoc)
	 * @see org.openmdx.base.aop1.PlugIn_1_0#propagatedEagerly(org.openmdx.base.accessor.rest.DataObject_1, java.lang.String, java.lang.Object)
	 */
    @Override
	public boolean propagatedEagerly(
		DataObject_1 object, 
		String feature,
		Object value
	) throws ServiceException {
		return false;
	}

    /* (non-Javadoc)
     * @see org.openmdx.base.aop1.PlugIn_1_0#resolveObjectClass(java.lang.String, javax.resource.cci.InteractionSpec)
     */
    @Override
    public String resolveObjectClass(
        String objectClass,
        InteractionSpec interactionSpec
    ) throws ServiceException {
        return objectClass;
    }

}
