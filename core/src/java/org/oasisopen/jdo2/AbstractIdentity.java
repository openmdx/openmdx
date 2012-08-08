/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractIdentity.java,v 1.6 2008/04/04 01:11:49 hburger Exp $
 * Description: Abstract Identity 
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/04 01:11:49 $
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
package org.oasisopen.jdo2;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.ietf.jgss.Oid;
import org.oasisopen.cci2.QualifierType;
import org.oasisopen.cci2.Identity;
import org.oasisopen.spi2.XRISegments;

/**
 * Abstract Identity
 */
public abstract class AbstractIdentity 
    implements Identity 
{

    /**
     * Constructor 
     *
     * @param persistentQualifier
     * @param qualifier
     * @param objectClass
     */
    protected AbstractIdentity(
        List<QualifierType> persistentQualifier,
        List<?> qualifier, 
        List<String> objectClass
    ){
        this.identifierType = persistentQualifier;
        this.qualifier = qualifier;
        this.objectClass = getObjectClass(persistentQualifier, objectClass);
    }

    /**
     * Retrieve the parent
     * 
     * @return the identity of the target object's parent;
     * or <code>null</code> if the object identity lacks this
     * information
     */
    protected abstract Identity parent();

    /**
     * Evaluate the object class
     * 
     * @return the object class if the object can't be replaced; 
     * <code>null</code> otherwise.
     */
    private final static List<String> getObjectClass(
        List<QualifierType> persistentQualifier,
        List<String> objectClass
    ){
        for(QualifierType identifierType : persistentQualifier){
            if(identifierType != QualifierType.PERSISTENT) {
                return null;
            }
        }
        return objectClass;
    }
    
    /**
     * Retrieve the name of the reference connecting the instances of the 
     * target object's container with the instances of the target object's 
     * class.
     * 
     * @return the name of the reference from the parent object's parent to
     * the object; or <ocde>null</code> if the object has no container.
     */
    protected abstract String referenceName();
    
    /* (non-Javadoc)
     * @see org.oasisopen.spi2.Identity#toXRI(java.util.List)
     */
    protected String toXRI(
        List<String> objectClass
    ) {
        Identity parent = parent();
        String referenceName = referenceName();
        StringBuilder xri = referenceName == null ? new StringBuilder(
            "xri://@openmdx"
        ) : new StringBuilder(
            parent instanceof AbstractIdentity ? ((AbstractIdentity)parent).toXRI(objectClass) : parent.toString()
        ).append(
            '/'
        ).append(
            referenceName
        ).append(
            '/'
        );
        for(
            int i = 0;
            i < this.qualifier.size();
            i++
        ){
            Object q = this.qualifier.get(i); 
            boolean p = QualifierType.PERSISTENT == this.identifierType.get(i);
            XRISegments.append(
                xri, // target
                i > 0 || referenceName == null, // optionalDelimiterRequired
                p, // persistent
                q instanceof UUID || q instanceof URI || q instanceof Oid, // crossReference
                q instanceof UUID ? "urn:uuid:" + q : q instanceof Oid ? "urn:oid:" + q : q
            );
        }
        if(objectClass != null && referenceName == null ) {
            List<String> namespace = Arrays.asList(
                this.qualifier.get(0).toString().split("\\.")
            );
            xri.append("*($t*(+class)*");
            int iLimit = objectClass.size() - 1;
            if(!namespace.equals(objectClass.subList(0, iLimit))) {
                for(
                   int i = 0;
                   i < iLimit;
                   i++
                ){
                    xri.append(
                        objectClass.get(i)
                    ).append(
                        "::"
                    );
                }
            } 
            xri.append(
                objectClass.get(iLimit)
            ).append(
                ")"
            );
        }
        return xri.toString();
    }
    
    /**
     * Qualifier persistency accessor
     * 
     * @param index
     * 
     * @return <code>true</code> if the corresponding qualifier is persistent
     */
    protected QualifierType identifierType(
        int index
    ){
        return this.identifierType.get(index);
    }

    /**
     * Qualifier persistency accessor
     * 
     * @param index
     * 
     * @return <code>true</code> if the corresponding qualifier is persistent
     */
    @SuppressWarnings("unchecked")
    protected <Q> Q qualifier(
        Class<Q> qualifierClass,
        int index
    ){
        return (Q) this.qualifier.get(index);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.xri == null ? 
            this.xri = toXRI(this.objectClass) : 
            this.xri;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        } else if (obj.getClass() == this.getClass()) {
            AbstractIdentity that = (AbstractIdentity) obj;
            return 
                equal(this.parent(), that.parent()) && 
                this.identifierType.equals(that.identifierType) && 
                this.qualifier.equals(that.qualifier);
        } else {
            return false;
        }
    }

    /**
     * Compare two identity references
     * 
     * @param left
     * @param right
     * 
     * @return <code>true</code> if they are equal or both <code>null</code>;
     * <code>false</code> otherwise.
     */
    private static boolean equal(
        Identity left,
        Identity right
    ){
        return left == null ? right == null : left.equals(right);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        Identity parent = parent();
        return (
             parent == null ? 0 : 31 * parent.hashCode()
        ) + this.qualifier.hashCode();
    }


    /**
     * The cached XRI
     */
    private transient String xri = null;

    /**
     * The object class if all qualifiers are persistent, <code>null</code> 
     * otherwise.
     */
    private final List<String> objectClass;
    
    /**
     * 
     */
    private final List<QualifierType> identifierType;

    /**
     * 
     */
    private final List<?> qualifier;

}