/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: StateCapables.java,v 1.6 2009/01/17 02:37:25 hburger Exp $
 * Description: StateCapables 
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/17 02:37:25 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.compatibility.state1.spi;

import javax.jmi.reflect.RefBaseObject;
import javax.jmi.reflect.RefObject;

import org.openmdx.base.accessor.jmi.cci.RefObject_1_0;
import org.openmdx.base.naming.Path;
import org.openmdx.base.naming.PathComponent;

/**
 * StateCapables
 */
public class StateCapables {

    /**
     * Constructor 
     */
    protected StateCapables() {
        // Avoid instantiation
    }

    /**
     * The the virtual core objects' container's parent
     */
    public static final Path CORE_SEGMENT = new Path(
        "xri://@openmdx*org.openmdx.compatibility.state1/provider/-/segment/-"
    );
    
    /**
     * The container for the virtual core objects
     */
    public static String CORE_REFERENCE = "state1Core";
    
    /**
     * The container for the virtual core objects
     */
    private static final Path CORE_CONTAINER = CORE_SEGMENT.getChild(CORE_REFERENCE);
    
    /**
     * The container for transient object id's
     */
    public static final Path TRANSIENT_CONTAINER = CORE_SEGMENT.getChild("extent");

    private  static final Path CORE_OBJECT = CORE_CONTAINER.getChild(":*");

    public static final Path TRANSIENT_OBJECT = TRANSIENT_CONTAINER.getChild(":*");
    
    private static boolean XRI2 = false;
    
    public static String getResourceIdentifier(
        RefBaseObject refContainer,
        String qualifier
    ){
        return new Path(refContainer.refMofId()).add(qualifier).toString();
    }
    
    /**
     * Calculate the virtual core object's id
     * 
     * @param refContainer
     * @param qualifier
     *  
     * @return the virtual core object's id
     */
    public static Path getStateCapable(
        RefBaseObject refContainer,
        String qualifier
    ){
        return CORE_CONTAINER.getChild(
            getResourceIdentifier(refContainer, qualifier)
        );
    }

    /**
     * Calculate the virtual core object's id
     * 
     * @param resourceIdentifier
     *  
     * @return the virtual core object's id
     */
    public static String getStateCapable(
        String resourceIdentifier
    ){
        return toString(
            getStateCapable(
                new Path(resourceIdentifier)
            )
        );
    }
    
    /**
     * Calculate the virtual core object's id
     * 
     * @param resourceIdentifier
     *  
     * @return the virtual core object's id
     */
    public static Path getStateCapable(
        Path resourceIdentifier
    ){
        return resourceIdentifier.isLike(CORE_OBJECT) ? resourceIdentifier : CORE_CONTAINER.getChild(
            resourceIdentifier.toString()
        );
    }

    /**
     * Calculate the virtual core object's id
     * 
     * @param resourceIdentifier
     *  
     * @return the virtual core object's id
     */
    public static Path getResourceIdentifier(
        Path resourceIdentifier
    ){
        return resourceIdentifier.isLike(CORE_OBJECT) ? new Path(resourceIdentifier.getBase()) : resourceIdentifier;
    }
    
    /**
     * Calculate the virtual core object's id
     * 
     * @param refContainer
     * @param qualifier
     *  
     * @return the virtual core object's id
     */
    public static String getStateId(
        RefObject core
    ){
        Path coreId = core instanceof RefObject_1_0 ? ((RefObject_1_0)core).refGetPath() :  new Path(core.refMofId());
        return toString(new Path(coreId.getBase()));
    }

    public static Path newTransientObjectId(){
        return TRANSIENT_CONTAINER.getChild(PathComponent.createPlaceHolder());
    }
    
    /**
     * Convert a  path to its XRI normal form
     * 
     * @param id
     * 
     * @return the id's XRI representation
     */
    public static String toString(
        Path id
    ){
        return 
            id == null ? null :
            XRI2 ? id.toResourceIdentifier() : 
            id.toXri();
    }
    
    public static boolean isCoreObject(
        Path path
    ){
        return path != null && path.isLike(CORE_OBJECT);
    }
    
    public static boolean isTransientObject(
        Path path
    ){
        return path != null && path.isLike(TRANSIENT_OBJECT);
    }

}
