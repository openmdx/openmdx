/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Primitive Type Mapper 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2013, OMEX AG, Switzerland
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
package org.openmdx.application.mof.mapping.java;

import java.util.List;

import org.omg.mof.spi.Identifier;
import org.omg.mof.spi.Names;
import org.openmdx.application.mof.mapping.spi.MapperUtils;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.PrimitiveTypes;
import org.openmdx.kernel.exception.BasicException;


/**
 * Primitive Type Mapper
 */
public class StandardPrimitiveTypeMapper implements PrimitiveTypeMapper {

    /**
     * Constructor 
     *
     * @param hasDefault defines whether unknown primitive types are mapped to <code>java.lang.String</code>
     */
    protected StandardPrimitiveTypeMapper(
        boolean hasDefault
    ){
        this.hasDefault = hasDefault;
    }
    
    /**
     * Constructor for an instance which maps unknown primitive types to <code>java.lang.String</code>
     */
    public StandardPrimitiveTypeMapper(
    ){
        this(true);
    }
    
    /**
     * Defines whether unknown primitive types are mapped to <code>java.lang.String</code>
     */
    private final boolean hasDefault;

    /**
     * Determines the Java equivalent for a given primitive type
     * <p>
     * <em>This method must be overridden in order to support non-standard primitive types.</em>
     * 
     * @param qualifiedTypeName the qualified model class name
     * @param alwaysAsObject tells whether the object or scalar variant of a Java built-in types is required
     * @return the Java equivalent for the given primitive type
     * 
     * @throws ServiceException
     */
    @Override
    public String getFeatureType(
        String qualifiedTypeName,
        Format format, 
        boolean alwaysAsObject
    ) throws ServiceException{
        if(PrimitiveTypes.STRING.equals(qualifiedTypeName)) return "java.lang.String";
        if(PrimitiveTypes.BOOLEAN.equals(qualifiedTypeName)) return alwaysAsObject ? "java.lang.Boolean" : "boolean";
        if(PrimitiveTypes.SHORT.equals(qualifiedTypeName)) return alwaysAsObject ? "java.lang.Short" : "short";
        if(PrimitiveTypes.LONG.equals(qualifiedTypeName)) return alwaysAsObject ? "java.lang.Long" : "long";
        if(PrimitiveTypes.INTEGER.equals(qualifiedTypeName)) return alwaysAsObject ? "java.lang.Integer" : "int";
        if(PrimitiveTypes.DECIMAL.equals(qualifiedTypeName)) return "java.math.BigDecimal";
        if(PrimitiveTypes.DATETIME.equals(qualifiedTypeName)) return format == Format.JPA3 ? "java.sql.Timestamp" : "java.util.Date";
        if(PrimitiveTypes.DATE.equals(qualifiedTypeName)) return format == Format.JPA3 ? "java.sql.Date" : "javax.xml.datatype.XMLGregorianCalendar";
        if(PrimitiveTypes.DURATION.equals(qualifiedTypeName)) return "javax.xml.datatype.Duration";
        if(PrimitiveTypes.BINARY.equals(qualifiedTypeName)) return "byte[]";
        if(PrimitiveTypes.ANYURI.equals(qualifiedTypeName)) return "java.net.URI";
        if(PrimitiveTypes.XRI.equals(qualifiedTypeName)) return "java.lang.String"; // no standard implementation as openMDX sticks to XRI 2
        if(PrimitiveTypes.UUID.equals(qualifiedTypeName)) return "java.util.UUID";
        if(PrimitiveTypes.OID.equals(qualifiedTypeName)) return "org.ietf.jgss.Oid";
        if(PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)) return "org.openmdx.base.naming.Path";
        if(hasDefault) {
            return "java.lang.String";
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "Unspupported primitive type with default disabled",
                new BasicException.Parameter("qualifiedTypeName", qualifiedTypeName),
                new BasicException.Parameter("hasDefault", Boolean.valueOf(hasDefault))
            );
        }
    }

    /**
     * Determines the Java predicate for a given primitive type
     * <p>
     * <em>This method may be overridden in order to support non-standard primitive type queries.</em>
     * 
     * @param qualifiedTypeName the qualified model class name
     * 
     * @return the Java predicate for the given primitive type
     * 
     * @throws ServiceException
     */
    @Override
    public String getPredicateType(
        String qualifiedTypeName
    ) throws ServiceException {
        if(PrimitiveTypes.BOOLEAN.equals(qualifiedTypeName)) return "org.w3c.cci2.BooleanTypePredicate";
        if(PrimitiveTypes.SHORT.equals(qualifiedTypeName)) return "org.w3c.cci2.ComparableTypePredicate<java.lang.Short>";
        if(PrimitiveTypes.LONG.equals(qualifiedTypeName)) return "org.w3c.cci2.ComparableTypePredicate<java.lang.Long>";
        if(PrimitiveTypes.INTEGER.equals(qualifiedTypeName)) return "org.w3c.cci2.ComparableTypePredicate<java.lang.Integer>";
        if(PrimitiveTypes.STRING.equals(qualifiedTypeName)) return "org.w3c.cci2.StringTypePredicate";
        if(PrimitiveTypes.ANYURI.equals(qualifiedTypeName)) return "org.w3c.cci2.ResourceIdentifierTypePredicate<java.net.URI>";
        if(PrimitiveTypes.OBJECT_ID.equals(qualifiedTypeName)) return "org.w3c.cci2.StringTypePredicate";
        if(PrimitiveTypes.XRI.equals(qualifiedTypeName)) return "org.w3c.cci2.StringTypePredicate";
        if(PrimitiveTypes.DATETIME.equals(qualifiedTypeName)) return "org.w3c.cci2.ComparableTypePredicate<java.util.Date>";
        if(PrimitiveTypes.DATE.equals(qualifiedTypeName)) return "org.w3c.cci2.PartiallyOrderedTypePredicate<javax.xml.datatype.XMLGregorianCalendar>";
        if(PrimitiveTypes.ANYURI.equals(qualifiedTypeName)) return "org.w3c.cci2.ComparableTypePredicate<java.net.URI>";
        if(PrimitiveTypes.DURATION.equals(qualifiedTypeName)) return "org.w3c.cci2.PartiallyOrderedTypePredicate<javax.xml.datatype.Duration>";
        if(PrimitiveTypes.DECIMAL.equals(qualifiedTypeName)) return "org.w3c.cci2.ComparableTypePredicate<java.math.BigDecimal>";
        return "org.w3c.cci2.AnyTypePredicate"; // has always a reasonable default value   
    }
    
    /**
     * Provide the Java pattern to parse a given expression represented by the EXPRESSION_PLACE_HOLDER.
     * <p>
     * There is usually no need to override this method.
     *  
     * @param qualifiedTypeName the qualified model class name
     * @param asObject tells whether the object or scalar variant of a Java built-in types is required
     * @return the Java pattern to parse a given expression, e.g. "Boolean.valueOf({})"
     * 
     * @throws ServiceException
     * 
     * @see {@link PrimitiveTypeMapper#EXPRESSION_PLACEHOLDER}
     */
    @Override
    public String getParsePattern(
        String qualifiedTypeName, 
        Format format, 
        boolean asObject
    ) throws ServiceException {
        if(!asObject){
            if(PrimitiveTypes.BOOLEAN.equals(qualifiedTypeName)) return "java.lang.Boolean.parseBoolean(" + EXPRESSION_PLACEHOLDER + ")";
            if(PrimitiveTypes.SHORT.equals(qualifiedTypeName)) return "java.lang.Short.parseShort(" + EXPRESSION_PLACEHOLDER + ")";
            if(PrimitiveTypes.INTEGER.equals(qualifiedTypeName)) return "java.lang.Integer.parseInt(" + EXPRESSION_PLACEHOLDER + ")";
            if(PrimitiveTypes.LONG.equals(qualifiedTypeName)) return "java.lang.Long.parseLong(" + EXPRESSION_PLACEHOLDER + ")";
        }
        String type = getFeatureType(qualifiedTypeName, format, asObject);
        return "org.w3c.spi2.Datatypes.create(" + type + ".class," + EXPRESSION_PLACEHOLDER + ")";
    }

    private String getMappingClass(
        String qualifiedTypeName
    ){
        List<String> nameComponents = MapperUtils.getNameComponents(qualifiedTypeName);
        return AbstractMapper.getNamespace(
            MapperUtils.getNameComponents(MapperUtils.getPackageName(qualifiedTypeName)),
            Names.JPA3_PACKAGE_SUFFIX
        ) + '.' + Identifier.CLASS_PROXY_NAME.toIdentifier( 
            nameComponents.get(nameComponents.size()-1)
        );
   }
    
    /* (non-Javadoc)
     * @see org.openmdx.application.mof.mapping.java.PrimitiveTypeMapper#getMappingMethod(java.lang.String, org.openmdx.application.mof.mapping.java.Format, org.openmdx.application.mof.mapping.java.Format)
     */
    @Override
    public String getMappingPattern(
        String qualifiedTypeName,
        Format from,
        Format to
    ) throws ServiceException {
        if(from == Format.CCI2 && to == Format.JPA3) {
            return getMappingClass(qualifiedTypeName) + ".toJDO(" + EXPRESSION_PLACEHOLDER + ")";
        } else if (from == Format.JPA3 && to == Format.CCI2) {
            return getMappingClass(qualifiedTypeName) + ".toCCI(" + EXPRESSION_PLACEHOLDER + ")";
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "Unspupported primitive type mapping",
                new BasicException.Parameter("qualifiedTypeName", qualifiedTypeName),
                new BasicException.Parameter("from", from),
                new BasicException.Parameter("to", to)
            );
        }
    }

}
