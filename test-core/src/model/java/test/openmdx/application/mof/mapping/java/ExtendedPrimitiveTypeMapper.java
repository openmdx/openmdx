/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Extended Primitive Type Mapper 
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
package test.openmdx.application.mof.mapping.java;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmdx.application.mof.mapping.java.Format;
import org.openmdx.application.mof.mapping.java.StandardPrimitiveTypeMapper;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;

/**
 * Extended Primitive Type Mapper
 */
public class ExtendedPrimitiveTypeMapper extends StandardPrimitiveTypeMapper {

    /**
     * Constructor 
     * <p>
     * Fallback requireed for example for {@code est:openmdx:app1:AddressFormatType}
     */
    public ExtendedPrimitiveTypeMapper() {
        super(true);
    }

    /**
     * E.g. test:openmdx:datatypes1:CountryCode
     */
    Pattern CODE_PATTERN = Pattern.compile("(.+):([^:]+Code)");
    
    
    /* (non-Javadoc)
     * @see org.openmdx.application.mof.mapping.java.StandardPrimitiveTypeMapper#getPrimitiveType(java.lang.String, boolean)
     */
    @Override
    public String getFeatureType(
        String qualifiedTypeName, 
        Format format, 
        boolean asObject
    ) throws ServiceException {
        Matcher matcher = CODE_PATTERN.matcher(qualifiedTypeName);
        if(matcher.matches()) {
            System.out.println("ExtendedPrimitiveTypeMapper: " + qualifiedTypeName + " matches CODE_PATTERN");
            if(format == Format.JPA3) {
                return "java.lang.String";
            } else {
                return getJavaClassName(matcher);
            }
        } else {
            return super.getFeatureType(qualifiedTypeName, format, asObject);
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.mof.mapping.java.StandardPrimitiveTypeMapper#getMappingMethod(java.lang.String, org.openmdx.application.mof.mapping.java.Format, org.openmdx.application.mof.mapping.java.Format)
     */
    @Override
    public String getMappingPattern(
        String qualifiedTypeName,
        Format from,
        Format to
    ) throws ServiceException {
        Matcher matcher = CODE_PATTERN.matcher(qualifiedTypeName);
        if(matcher.matches()) {
            if(from == Format.CCI2 && to == Format.JPA3) {
                return EXPRESSION_PLACEHOLDER + ".getValue()";
            } else if (from == Format.JPA3 && to == Format.CCI2) {
                return getJavaClassName(matcher) + ".valueOf(" + EXPRESSION_PLACEHOLDER + ")";
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
        } else {
            return super.getMappingPattern(qualifiedTypeName, from, to);
        }
    }

    private String getJavaClassName(Matcher matcher) {
        return matcher.group(1).replace(':', '.') + ".dto." + matcher.group(2);
    }
    
}

