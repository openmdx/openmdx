/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ObjectIdBuilder.java,v 1.7 2008/05/15 18:07:25 hburger Exp $
 * Description: Application Identity Builder
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/05/15 18:07:25 $
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

import java.util.List;

/**
 * Application Identity Builder
 */
public interface ObjectIdBuilder {

    /**
     * Create a new application identity
     * 
     * @param mixinParent tells whether the <code>parent</code> is a reference to a 
     * mix-in class<ul>
     * <li><code>null</code> if the object has no parent 
     * <li><code>TRUE</code> if the object is contained in a mix-in class 
     * <li><code>FALSE</code> otherwise
     * </ul> 
     * @param parentObjectId the parent object's id; 
     * or <code>null</code> if <code>mixinParent</code> is <code>null</code>
     * @param referenceName The name of the reference from the parent; 
     * or <code>null</code> if <code>mixinParent</code> is <code>null</code>
     * @param qualifierIsPersistent tells whether the corresponding <code>qualifier</code>
     * value is persistent or not
     * @param qualifier the qualifier values are instances of<ul>
     * <li><code>java.math.BigDecimal</code> for values of type<ul>
     * <li><code>org::w3c::decimal</code>
     * </ul>
     * <li><code>java.lang.Integer</code> for values of type<ul>
     * <li><code>org::w3c::integer</code>
     * </ul>
     * <li><code>java.lang.String</code> for values of type<ul>
     * <li><code>org::w3c::string</code>
     * </ul>
     * <li><code>java.net.URI</code> for values of type<ul>
     * <li><code>org::w3c::anyURI</code>
     * </ul>
     * <li><code>java.util.Date</code> for values of type<ul>
     * <li><code>org::w3c::dateTime</code>
     * </ul>
     * <li><code>java.util.UUID</code> for values of type<ul>
     * <li><code>org::ietf::uuid</code>
     * </ul>
     * <li><code>javax.xml.datatype.Duration</code> for values of type<ul>
     * <li><code>org::w3c::duration</code>
     * </ul>
     * <li><code>javax.xml.datatype.XMLGregorianCalendar</code> for values of type<ul>
     * <li><code>org::w3c::date</code>
     * <li><code>org::w3c::time</code>
     * </ul>
     * <li><code>org.ietf.jgss.Oid</code> for values of type<ul>
     * <li><code>org::ietf::oid</code>
     * </ul>
     * </ul>
     * @param baseClass the object's fully qualified base class name;
     * this argument may be <code>null</code> if all qualifiers are persistent
     * @param objectClass the object's fully qualified class name
     * @return a new object id based on the given arguments
     * 
     * @throws IllegalArgumentException in case of an unsupported qualifier 
     * class
     */
    String newObjectId(
        Boolean mixinParent, String parentObjectId,
        String referenceName, 
        List<Boolean> qualifierIsPersistent, List<?> qualifier, 
        List<String> baseClass, List<String>objectClass
    );
    
    /**
     * Create a new application identity
     * 
     * @param mixinParent tells whether the <code>parent</code> is a reference to a 
     * mix-in class<ul>
     * <li><code>null</code> if the object has no parent 
     * <li><code>TRUE</code> if the object is contained in a mix-in class 
     * <li><code>FALSE</code> otherwise
     * </ul> 
     * @param parentObjectId the parent object's id unless <code>mixinParent</code> is <code>null</code>
     * @param referenceName TODO
     * @param qualifierClass the qualifier classes are<ul>
     * <li><code>java.lang.String.class</code> to provide a persistent UUID value as<ul>
     * <li><code>org::w3c::string</code>
     * </ul>
     * <li><code>java.net.URI.class</code> to provide a persistent UUID value as<ul>
     * <li><code>org::w3c::anyURI</code>
     * </ul>
     * <li><code>java.util.Date.class</code> to provide a persistent date and time value as<ul>
     * <li><code>org::w3c::dateTime</code>
     * </ul>
     * <li><code>java.util.UUID.class</code> to provide a persistent UUID value as<ul>
     * <li><code>org::ietf::uuid</code>
     * </ul>
     * <li><code>org.ietf.jgss.Oid.class</code> to provide a persistent UUID value as<ul>
     * <li><code>org::ietf::oid</code>
     * </ul>
     * </ul>
     * @param objectClass the object's fully qualified class name
     * @param baseClass the object's fully qualified base class name
     * @return a new object id based on the given arguments
     * 
     * @throws IllegalArgumentException in case of an unsupported qualifier 
     * class
     */
    <T> String newObjectId(
        Boolean mixinParent, String parentObjectId,
        String referenceName,
        List<Class<T>> qualifierClass, 
        List<String> baseClass, List<String>objectClass
    );

    ObjectIdParser parseObjectId(
        String objectId,
        List<String> baseClass
    );

}
