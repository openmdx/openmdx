/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: Context_1.java,v 1.8 2009/05/16 22:17:47 wfro Exp $
 * Description: Embedded Context Object
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/05/16 22:17:47 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.base.aop1;

import javax.jdo.JDOUserException;

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.view.EmbeddedObjectView_1;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;

/**
 * Embedded Context Object
 */
class Context_1 extends EmbeddedObjectView_1 {

    /**
     * Constructor 
     *
     * @param object
     * @param objectClass
     * @param prefix
     * @param qualifier
     * @throws ServiceException
     */
    Context_1(
        DataObject_1_0 nextObject,
        String objectClass,
        String prefix, 
        String qualifier
    ) throws ServiceException{
        super(
            nextObject, 
            objectClass, 
            prefix
        );
        this.suffix = SystemAttributes.CONTEXT_CAPABLE_CONTEXT + '=' + qualifier;
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 3257005445276514356L;
    
    /**
     * 
     */
    private final String suffix;


    //--------------------------------------------------------------------------
    // Extends EmbeddedObject_1
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.Object_1_0#objGetPath()
     */
    public Path jdoGetObjectId(
    ) {
        try {
            Path objectPath = objGetDelegate().jdoGetObjectId();
            if(objectPath == null) return null;
            String[] components = objectPath.getSuffix(0);
            components[components.length-1] += ';' + this.suffix;
            return new Path(components);
        }
        catch(Exception e) {
            throw new JDOUserException(
                "Unable to get object id",
                e,
                this
            );
        }
    }
    
    //--------------------------------------------------------------------------
    // Extends Object
    //--------------------------------------------------------------------------

    /**
     * 
     */
    public String toString(
    ){
        return org.openmdx.base.accessor.spi.AbstractDataObject_1.toString(this, this.suffix);
    }

}
