/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: DataObjectIdBuilder.java,v 1.9 2008/02/22 17:56:42 hburger Exp $
 * Description: Standard ObjectId Builder 
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/02/22 17:56:42 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2008, OMEX AG, Switzerland
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
package org.oasisopen.spi2;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.ietf.jgss.Oid;
import org.openmdx.base.text.conversion.UUIDConversion;
import org.openmdx.kernel.id.UUIDs;
import org.openmdx.kernel.id.cci.UUIDGenerator;
import org.openxri.XRISegment;
import org.w3c.cci2.Datatypes;

/**
 * Standard ObjectId Builder
 */
public class DataObjectIdBuilder
    implements ObjectIdBuilder
{

    /**
     * Constructor 
     */
    protected DataObjectIdBuilder() {
    }

    /* (non-Javadoc)
     * @see org.oasisopen.spi2.ApplicationIdentityBuilder#newIdentity(java.lang.Boolean, java.lang.Object, java.util.List, java.util.List, java.util.List, java.util.List)
     */
    public String newObjectId(
        Boolean mixinParent,
        String parentObjectId,
        String referenceName,
        List<Boolean> persistentQualifier,
        List<?> qualifier,
        List<String> baseClass, List<String> objectClass
    ) {
        return null; // TODO
    }

//  /**
//   * Test whether all sub-segments are persistent
//   * 
//   * @param persistentQualifier a list containing the peristency modifiers for
//   * all sub-segments
//   * 
//   * @return <code>true</code> if any of the sub-segemnts is non-persistent,
//   * <code>false</code> otherwise
//   */
//  private boolean useBaseClass(
//      List<Boolean> persistentQualifier
//  ){
//      for(Boolean persistent : persistentQualifier) {
//          if(persistent == null || !persistent.booleanValue()) {
//              return true;
//          }
//      }
//      return false;
//  }
    
    /* (non-Javadoc)
     * @see org.oasisopen.spi2.ApplicationIdentityBuilder#newIdentity(java.lang.Boolean, java.lang.Object, java.util.List, java.util.List)
     */
    public <T> String newObjectId(
        Boolean mixinParent, String parentObjectId,
        String referenceName,
        List<Class<T>> qualifierClass, List<String> objectClass
    ) {
        return null; // TODO
    }

    /* (non-Javadoc)
     * @see org.oasisopen.spi2.ApplicationIdentityBuilder#getIdentity(java.lang.String)
     */
    public ObjectId toObjectId(String identity) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Retrieve a standard application identity builder instance
     * 
     * @return a standard application identity builder instance
     */
    public static ObjectIdBuilder getInstance(
    ){
        return DataObjectIdBuilder.instance;
    }

    /**
     * A singleton is sufficiont for the StandardIdentityBuilder as the
     * StandardIdentityBuilder itself is not configurable.
     */
    static final ObjectIdBuilder instance = new DataObjectIdBuilder();
    
    /**
     * The qualifier provider instance used by this application identity builder.
     */
//  private QualifierProvider qualifierProvider;
    
    
    //------------------------------------------------------------------------
    // Class Identity
    //------------------------------------------------------------------------
    
    /**
     * Standard Application Identity
     */
    protected static class StandardObjectId
        implements ObjectId
    {

        StandardObjectId(
            String objectId
        ) {
            int classDelimiter = objectId.lastIndexOf(' ');
            int segmentDelimiter = objectId.lastIndexOf('/');
            if(segmentDelimiter < 0) {
                this.parent = null;
                if(classDelimiter < 0) {
                    this.targetClass = AUTHORITY_CLASS;
                    this.segment = new XRISegment(objectId);
                } else {
                    this.targetClass = toClass(
                        objectId.substring(0, classDelimiter),
                        objectId.substring(classDelimiter + 1)
                    );
                    this.segment = new XRISegment(
                        objectId.substring(0, classDelimiter)
                    );
                }
            } else {
                this.parent = objectId.substring(0, segmentDelimiter);
                if(classDelimiter < segmentDelimiter) {
                    this.targetClass = null;
                    this.segment = new XRISegment(
                        objectId.substring(segmentDelimiter + 1)
                    );
                } else {
                    this.targetClass = toClass(
                        objectId.substring(0, objectId.indexOf('/')),
                        objectId.substring(classDelimiter + 1)
                    );
                    this.segment = new XRISegment(
                        objectId.substring(segmentDelimiter + 1, segmentDelimiter)
                    );
                }
            }
        }
        
        private static List<String> toClass(
            String authorityPart,
            String classPart
        ){
            String[] authorityComponents = authorityPart.split("\\.");
            String[] classComponents = classPart.split(":");
            if("".equals(classComponents[0])){
                //
                // Relative class name
                // 
                int count = Integer.valueOf(classPart.substring(0, 1), Character.MAX_RADIX);
                List<String> targetClass = new ArrayList<String>();
                for(
                    int i = 0;
                    i < count;
                    i++
                ) {
                    targetClass.add(authorityComponents[i]);
                }
                targetClass.add(classComponents[0].substring(1));
                for(
                    int i = 1;
                    i < classComponents.length;
                    i++
                ) {
                    targetClass.add(classComponents[i]);
                }
                return targetClass;
            } else {
                //
                // Absolute class name
                // 
                return Arrays.asList(classComponents);
            }
        }

        /* (non-Javadoc)
         * @see org.oasisopen.spi2.ObjectId#getParentIdentity(java.util.List)
         */
        public ObjectId getParentObjectId(List<String> baseClass) {
            return new StandardObjectId(this.parent);
        }

        /* (non-Javadoc)
         * @see org.oasisopen.spi2.ObjectId#getQualifier(java.lang.Class, int)
         */
        @SuppressWarnings("unchecked")
        public <E> E getQualifier(
            Class<E> qualifierClass, 
            int index
        ) {
            String q = XRISegments.toString(
                this.segment.getSubSegmentAt(index),
                qualifierClass == URI.class // get rid of parenthesis
            ); 
            return qualifierClass == Object.class ? (E) q : Datatypes.create(
                qualifierClass,
                q
            );
        }

        /* (non-Javadoc)
         * @see org.oasisopen.spi2.ObjectId#getQualifierCount()
         */
        public int getQualifierCount() {
            return this.segment.getNumSubSegments();
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
            return this.segment.getSubSegmentAt(index).isPersistant();
        }

        /**
         * The target class, if encoded into the object id
         */
        private final List<String> targetClass;
        
        /**
         * The stringified form of the parent object id
         */
        private final String parent;
        
        /**
         * The object id's last segment
         */
        private final XRISegment segment;
        
        /**
         * The default authority class
         */
        private static final List<String> AUTHORITY_CLASS = Arrays.asList(
            "org", "openmdx", "base", "Authority"
        );
        
    }

    
    //------------------------------------------------------------------------
    // Class QualifierProvider
    //------------------------------------------------------------------------
    
    /**
     * Qualifier Provider
     */
    public static class QualifierProvider {
        
        /**
         * Constructor 
         */
        public QualifierProvider() {
            this(UUIDs.getGenerator());
        }

        /**
         * Constructor 
         *
         * @param uuidGenerator
         */
        protected QualifierProvider(
            UUIDGenerator uuidGenerator
        ) {
            this.uuidGenerator = uuidGenerator;
        }

        /**
         * Create a List describing all qualifier values as being persistent
         * 
         * @param qualifierClass the types of the qualifiers to be generated
         * 
         * @return a list with the same size as qualifierClass
         */
        public List<Boolean> newPersistent(
            List<Class<?>> qualifierClass
        ){
            return Collections.nCopies(qualifierClass.size(), Boolean.TRUE);
        }
        
        /**
         * Create a List containing newly generated qualifiers of the requested type
         * 
         * @param qualifierClass the qualifier classes are<ul>
         * <li><code>java.lang.String.class</code> to provide a UUID value as<ul>
         * <li><code>org::w3c::string</code>
         * </ul>
         * <li><code>java.net.URI.class</code> to provide a UUID value as<ul>
         * <li><code>org::w3c::anyURI</code>
         * </ul>
         * <li><code>java.util.Date.class</code> to provide the current date and time as<ul>
         * <li><code>org::w3c::dateTime</code>
         * </ul>
         * <li><code>java.util.UUID.class</code> to provide a UUID value as<ul>
         * <li><code>org::ietf::uuid</code>
         * </ul>
         * <li><code>org.ietf.jgss.Oid.class</code> to provide a UUID value as<ul>
         * <li><code>org::ietf::oid</code>
         * </ul>
         * </ul>
         * 
         * @return a list with the same size as qualifierClass
         * 
         * @throws IllegalArgumentException in case of an unsupported qualifier 
         * class
         */
        public List<?> newQualifier(
            List<Class<?>> qualifierClass
        ){
            if(qualifierClass.size() == 1) {
                return Collections.singletonList(
                    newQualifier(qualifierClass.get(0))
                );
            } else {
                Object[] qualifiers = new Object[qualifierClass.size()];
                for(
                    int i = 0;
                    i < qualifiers.length;
                    i++
                ) {
                    qualifiers[i] = newQualifier(qualifierClass.get(i));
                }
                return Arrays.asList(qualifiers);
            }
        }

       /**
        * Create a new qualifier value
        * 
        * @param qualifierClass the qualifier classe is<ul>
        * <li><code>java.lang.String.class</code> to provide a persistent UUID value as<ul>
        * <li><code>org::w3c::string</code>
        * </ul>
        * <li><code>java.net.URI.class</code> to provide a persistent UUID value as<ul>
        * <li><code>org::w3c::anyURI</code>
        * </ul>
        * <li><code>java.util.Date.class</code> to provide the current date and time as<ul>
        * <li><code>org::w3c::dateTime</code>
        * </ul>
        * <li><code>java.util.UUID.class</code> to provide a persistent UUID value as<ul>
        * <li><code>org::ietf::uuid</code>
        * </ul>
        * <li><code>org.ietf.jgss.Oid.class</code> to provide a persistent UUID value as<ul>
        * <li><code>org::ietf::oid</code>
        * </ul>
        * </ul>
        * 
        * @return a new qaulifier being an instance of the requested class
        * 
        * @throws IllegalArgumentException in case of an unsupported qualifier 
        * class
        */
        protected Object newQualifier(
            Class<?> qualifierClass
        ){
            if(qualifierClass == java.util.Date.class) {
                return new java.util.Date();
            } else if(qualifierClass == UUID.class) {
                return this.uuidGenerator.next();
            } else if (qualifierClass == String.class){
                return this.uuidGenerator.next().toString();
            } else if (qualifierClass == URI.class) {
                return UUIDConversion.toURN(this.uuidGenerator.next());
            } else if (qualifierClass == Oid.class) {
                return UUIDConversion.toOid(this.uuidGenerator.next());
            } else throw new IllegalArgumentException(
                "Can't provider a qualifier of type " + qualifierClass.getName()
            );
        }

        /**
         * The UUID generator instance
         */
        protected final UUIDGenerator uuidGenerator;
        
    }

}
