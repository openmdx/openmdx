/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractObjectIdBuilder.java,v 1.3 2007/07/11 17:30:50 hburger Exp $
 * Description: AbstractObjectIdBuiler 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2007/07/11 17:30:50 $
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

import org.oasisopen.spi2.ObjectId;
import org.oasisopen.spi2.ObjectIdBuilder;
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

    /* (non-Javadoc)
     * @see org.oasisopen.spi2.ObjectIdBuilder#getObjectId(java.lang.String)
     */
    public ObjectId toObjectId(
        String objectId
    ) {
        return new ObjectIdParser(
            toPath(objectId), 
            getTargetClass(objectId)
        );
    }

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
          persistentQualifier.get(0) ?
            "This object id builder expects the qualifier being re-assignable" :
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
        String component = qualifier.get(0).toString();
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
        List<Class<T>> qualifierClass, List<String> objectClass
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
    protected abstract String fromPath(
        Path path
    );
    
    /**
     * Convert an object id to a path
     * 
     * @param objectId the object id to be converted to a path
     * 
     * @return the path represented by the given object id
     */
    protected abstract Path toPath(
        String objectId
    );

    /**
     * Retrieve an object id's target class
     * 
     * @param objectId the object id to be inspected
     * 
     * @return the object id's target class
     */
    protected abstract List<String> getTargetClass(
        String objectId
    );
    
    /**
     * The UUID generator
     */
    protected final UUIDGenerator uuidGenerator = UUIDs.getGenerator();

    /**
     * Class Object Id Parser
     */
    protected class ObjectIdParser implements ObjectId {
        
        /**
         * Constructor 
         *
         * @param path
         * @param targetClass TODO
         */
        protected ObjectIdParser(
            final Path path, 
            final List<String> targetClass
        ) {
            this.path = path;
            this.targetClass = targetClass;
            if(path.size() % 2 != 1) throw new RuntimeServiceException(           
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                new BasicException.Parameter[]{
                    new BasicException.Parameter("path", path.getSuffix(0))
                },
                "An object id requires an odd number of path components"
            );
        }
        
        /**
         * 
         */
        private final Path path;

        /**
         * 
         */
        private final List<String> targetClass;
        
        /* (non-Javadoc)
         * @see org.oasisopen.spi2.ObjectId#getParentIdentity(java.util.List)
         */
        public ObjectId getParentObjectId(
            List<String> baseClass
        ) {
            return path.size() == 1 ? 
                null :
                toObjectId(fromPath(this.path.getPrefix(this.path.size() - 2)));
        }

        /* (non-Javadoc)
         * @see org.oasisopen.spi2.ObjectId#getQualifier(java.lang.Class, int)
         */
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
                    new BasicException.Parameter("ObjectIdBuilder", getClass().getName()),
                    new BasicException.Parameter("qualifierClass", qualifierClass),
                    new BasicException.Parameter("index", index)
                },
                message
            );
            return (E) this.path.getBase();
        }

        /* (non-Javadoc)
         * @see org.oasisopen.spi2.ObjectId#getQualifierCount()
         */
        public int getQualifierCount() {
            return 1;
        }

        /* (non-Javadoc)
         * @see org.oasisopen.spi2.ObjectId#getTargetClass()
         */
        public List<String> getTargetClass() {
            return this.targetClass;
        }

        /* (non-Javadoc)
         * @see org.oasisopen.spi2.ObjectId#isQualifierPersistent(int)
         */
        public boolean isQualifierPersistent(int index) {
            return false;
        }

    }
}
