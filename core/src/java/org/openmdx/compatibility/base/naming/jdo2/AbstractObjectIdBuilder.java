/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractObjectIdBuilder.java,v 1.7 2008/05/16 09:21:43 hburger Exp $
 * Description: AbstractObjectIdBuiler 
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/05/16 09:21:43 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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

package org.openmdx.compatibility.base.naming.jdo2;

import java.util.List;

import org.oasisopen.spi2.ObjectIdBuilder;
import org.oasisopen.spi2.ObjectIdParser;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.id.cci.UUIDGenerator;

/**
 * The Abstract Object Id Builder assumes at the moment that each segment 
 * consists of a single re-assignable sub-segment of type org::w3c::string.
 */
public abstract class AbstractObjectIdBuilder
    implements ObjectIdBuilder
{

    /**
     * Constructor 
     */
    protected AbstractObjectIdBuilder() {
    }

    /**
     * The UUID generator
     */
    protected final UUIDGenerator uuidGenerator = UUIDs.getGenerator();
    
    /* (non-Javadoc)
     * @see org.oasisopen.spi2.ObjectIdBuilder#newObjectId(java.lang.Boolean, java.lang.String, java.util.List, java.util.List, java.util.List, java.util.List)
     */
    public String newObjectId(
        Boolean mixinParent, String parentObjectId,
        String referenceName,
        List<Boolean> persistentQualifier, List<?> qualifier,
        List<String> baseClass, List<String> objectClass
     ) {
        String message = persistentQualifier.size() != qualifier.size() ?
            "The arguments qualifier und persistentQualifier must have the same size" :
          qualifier.size() != 1 ?
            "This object id builder expects a single qualifier" :
          !(qualifier.get(0) instanceof String) ?
            "This object id builder expects the qualifier being of type String" :
            null;
        if(message != null) throw new RuntimeServiceException(           
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            new BasicException.Parameter[]{
                new BasicException.Parameter("ObjectIdBuilder", getClass().getName()),
                new BasicException.Parameter("mixinParent", mixinParent),
                new BasicException.Parameter("parentObjectId", parentObjectId),
                new BasicException.Parameter("qualifier", qualifier),
                new BasicException.Parameter("persistentQualifier", persistentQualifier),
                new BasicException.Parameter("baseClass", baseClass),
                new BasicException.Parameter("objectClass", objectClass)
            },
            message
        );
        String component = (
            persistentQualifier.get(0) ? "!" : ""
        ) + qualifier.get(0);
        return fromPath(
            mixinParent == null ? new Path(
                new String[]{component}
            ) : toPath(
                parentObjectId
            ).add(
                referenceName
            ).add(
                component
            )
        );
    }

    /* (non-Javadoc)
     * @see org.oasisopen.spi2.ObjectIdBuilder#newObjectId(java.lang.Boolean, java.lang.String, java.util.List, java.util.List)
     */
    public <T> String newObjectId(
        Boolean mixinParent, String parentObjectId,
        String referenceName,
        List<Class<T>> qualifierClass, 
        List<String> baseClass, List<String> objectClass
    ) {
        String message = mixinParent == null ?
            "The authority's id can't be generated" :
          qualifierClass.size() != 1 ?
            "This object id builder expects a single qualifier" :
          qualifierClass.get(0) != String.class ?
            "This object id builder expects the qualifier being of type String" :
            null;
        if(message != null) throw new RuntimeServiceException(           
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_PARAMETER,
            new BasicException.Parameter[]{
                new BasicException.Parameter("ObjectIdBuilder", getClass().getName()),
                new BasicException.Parameter("mixinParent", mixinParent),
                new BasicException.Parameter("parentObjectId", parentObjectId),
                new BasicException.Parameter("qualifierClass", qualifierClass),
                new BasicException.Parameter("objectClass", objectClass)
            },
            message
        );
        String component = UUIDConversion.toUID(this.uuidGenerator.next());
        return fromPath(
            toPath(
                parentObjectId
            ).add(
                referenceName
            ).add(
                component
            )
        );
    }

    /**
     * Convert a path to an object id
     * 
     * @param path the path to be converted to an object id
     * 
     * @return the object id representing the given path
     */
    abstract protected String fromPath(
        Path path
    );
    
    /**
     * Convert an object id to a path.
     * <p><em>
     * Note:</br>
     * A subclass must override <code>toPath()</code> or <code>newObjectId()</code>.
     * </em>
     * 
     * @param objectId the object id to be converted to a path
     * 
     * @return the path represented by the given object id
     */
    abstract protected Path toPath(
        String objectId
    );

    
    //------------------------------------------------------------------------
    // Class AbstractObjectIdParser
    //------------------------------------------------------------------------
    
    /**
     * Abstract Object Id Parser
     */
    public abstract static class AbstractObjectIdParser implements ObjectIdParser {
        
        /**
         * Constructor 
         *
         * @param path
         */
        protected AbstractObjectIdParser(
            final Path path
        ) {
            this.path = path;
            if(path.size() % 2 != 1) throw new RuntimeServiceException(           
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("ObjectIdParser", getClass().getName()),
                    new BasicException.Parameter("path", path.getSuffix(0))
                },
                "An object id requires an odd number of path components"
            );
        }
        
        /**
         * 
         */
        protected final Path path;

        /* (non-Javadoc)
         * @see org.oasisopen.spi2.ObjectId#getQualifier(java.lang.Class, int)
         */
        @SuppressWarnings("unchecked")
        public <E> E getQualifier(
            Class<E> qualifierClass, 
            int index
         ) {
            String message = qualifierClass != String.class ?
                "This object id builder expects the qualifier being of type String" :
              index != 0 ?
                "This object id builder expects a single qualifier" :
                null;
            if(message != null) throw new RuntimeServiceException(           
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("ObjectIdParser", getClass().getName()),
                    new BasicException.Parameter("qualifierClass", qualifierClass),
                    new BasicException.Parameter("index", index)
                },
                message
            );
            String value = this.path.getBase();
            return (E) (
                value.startsWith("!") ? value.substring(1) : value
            );
        }

        /* (non-Javadoc)
         * @see org.oasisopen.spi2.ObjectId#isQualifierPersistent(int)
         */
        public boolean isQualifierPersistent(int index) {
            return this.path.getBase().startsWith("!");
        }

    }
    
}
