/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: Boolean Marshaller 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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
package org.openmdx.base.dataprovider.layer.persistence.jdbc.datatypes;

import java.sql.Connection;

import org.openmdx.base.dataprovider.layer.persistence.jdbc.LayerConfigurationEntries;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.DataTypes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;

/**
 * Boolean Marshaller
 */
public class BooleanMarshaller {

    /**
     * Factory
     * 
     * @param type the duration type
     * 
     * @return an new <code>DurationMarshaller</code> instance
     * 
     * @throws ServiceException 
     */
    public static BooleanMarshaller newInstance(
        String configuredBooleanFalse,
        String configuredBooleanTrue,
        DataTypes sqlDataTypes
    ) throws ServiceException{
        return new BooleanMarshaller(
            configuredBooleanFalse,
            configuredBooleanTrue,
            sqlDataTypes
        );
    }

    /**
     * Constructor 
     *
     * @param booleanFalse
     * @param booleanTrue
     * 
     * @throws ServiceException 
     */
    protected BooleanMarshaller(
        String configuredBooleanFalse,
        String configuredBooleanTrue,
        DataTypes sqlDataTypes
    ) throws ServiceException {
        this.configuredBooleanFalse = configuredBooleanFalse;
        this.configuredBooleanTrue = configuredBooleanTrue;
        this.sqlDataTypes = sqlDataTypes;
    }

    /**
     * The value corresponding to Boolean.FALSE, e.g.<ul>
     * <li><code>false</code> <i>(fix in case of <code>booleanType BOOLEAN</code>
     * <li><code>##false##</code> <i>(default in case of <code>booleanType CHARACTER</code>)</i>
     * <li><code>0</code> <i>(default in case of <code>booleanType NUMERIC</code>)</i>
     * </ul>
     */
    private Object getBooleanFalse(
        String booleanType
    ) throws ServiceException {
        if(LayerConfigurationEntries.BOOLEAN_TYPE_BOOLEAN.equals(booleanType)) {
            return Boolean.FALSE;
        } 
        else if(LayerConfigurationEntries.BOOLEAN_TYPE_YN.equals(booleanType)) {
            return "N";
        } 
        else if (LayerConfigurationEntries.BOOLEAN_TYPE_CHARACTER.equals(booleanType)) {
            return this.configuredBooleanFalse == null
            ? "##false##"
                : this.configuredBooleanFalse;
        } 
        else if(LayerConfigurationEntries.BOOLEAN_TYPE_NUMERIC.equals(booleanType)) {
            return this.configuredBooleanFalse == null
            ? Integer.valueOf(0)
            : Integer.valueOf(this.configuredBooleanFalse);
        } 
        else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION, 
                "Unsupported booleanType",
                new BasicException.Parameter(
                    "supported", 
                    "[" +
                    LayerConfigurationEntries.BOOLEAN_TYPE_BOOLEAN + ", " +
                    LayerConfigurationEntries.BOOLEAN_TYPE_YN + ", " +
                    LayerConfigurationEntries.BOOLEAN_TYPE_NUMERIC + ", " +
                    LayerConfigurationEntries.BOOLEAN_TYPE_CHARACTER + 
                    "]"
                ),    
                new BasicException.Parameter("requested", booleanType)
            );
        }
    }

    /**
     * The value corresponding to Boolean.TRUE, e.g.<ul>
     * <li><code>true</code> <i>(fix in case of <code>booleanType BOOLEAN</code>
     * <li><code>##true##</code> <i>(default in case of <code>booleanType CHARACTER</code>)</i>
     * <li><code>1</code> <i>(default in case of <code>booleanType NUMERIC</code>)</i>
     * </ul>
     */
    private Object getBooleanTrue(
        String booleanType
    ) throws ServiceException {
        if(LayerConfigurationEntries.BOOLEAN_TYPE_BOOLEAN.equals(booleanType)) {
            return Boolean.TRUE;
        } 
        else if(LayerConfigurationEntries.BOOLEAN_TYPE_YN.equals(booleanType)) {
            return "Y";
        } 
        else if (LayerConfigurationEntries.BOOLEAN_TYPE_CHARACTER.equals(booleanType)) {
            return this.configuredBooleanTrue == null
            ? "##true##"
                : this.configuredBooleanTrue;
        } 
        else if(LayerConfigurationEntries.BOOLEAN_TYPE_NUMERIC.equals(booleanType)) {
            return this.configuredBooleanTrue == null
            ? Integer.valueOf(1)
            : Integer.valueOf(this.configuredBooleanTrue);
        } 
        else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION, 
                "Unsupported booleanType",
                new BasicException.Parameter(
                    "supported", 
                    "[" +
                    LayerConfigurationEntries.BOOLEAN_TYPE_BOOLEAN + ", " +
                    LayerConfigurationEntries.BOOLEAN_TYPE_YN + ", " +
                    LayerConfigurationEntries.BOOLEAN_TYPE_NUMERIC + ", " +
                    LayerConfigurationEntries.BOOLEAN_TYPE_CHARACTER + 
                    "]"
                ),    
                new BasicException.Parameter("requested", booleanType)
            );
        }        
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.marshalling.Marshaller#marshal(java.lang.Object)
     */
    public Object marshal(
        Object source,
        Connection connection        
    ) throws ServiceException {        
        String booleanType = this.sqlDataTypes.getBooleanType(connection).intern();  
        Object booleanFalse = this.getBooleanFalse(booleanType);
        Object booleanTrue = this.getBooleanTrue(booleanType);
        return source instanceof Boolean ?
            (((Boolean)source).booleanValue() ? booleanTrue : booleanFalse) :
                source;
    }

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.marshalling.Marshaller#unmarshal(java.lang.Object)
     */
    public Object unmarshal(
        Object source,
        Connection connection        
    ) throws ServiceException {
        String booleanType = this.sqlDataTypes.getBooleanType(connection).intern();  
        Object booleanTrue = this.getBooleanTrue(booleanType);
        return source instanceof Boolean ? source : Boolean.valueOf(
            source instanceof Number && booleanTrue instanceof Number ?
                ((Number)source).intValue() == ((Number)booleanTrue).intValue() :
                    source instanceof String && booleanTrue instanceof String ?
                        ((String)source).startsWith((String)booleanTrue) :
                            source instanceof String && booleanTrue instanceof Boolean ? 
                                ((String)source).startsWith("1") || ((String)source).startsWith(booleanTrue.toString()) :
                                    ((String)source).startsWith(booleanTrue.toString())
        );

    }

    //-----------------------------------------------------------------------
    private final DataTypes sqlDataTypes;
    private final String configuredBooleanFalse;
    private final String configuredBooleanTrue;

}
