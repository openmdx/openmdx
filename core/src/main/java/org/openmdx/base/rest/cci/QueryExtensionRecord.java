/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: JDO Query Extension 
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
package org.openmdx.base.rest.cci;

import javax.resource.cci.MappedRecord;

/**
 * openMDX' JDO Query Extension
 */
public interface QueryExtensionRecord extends MappedRecord {
    
    /**
     * A query extension record's name
     */
    String NAME = "org:openmdx:kernel:QueryExtension";
	
    
    //------------------------------------------------------------------------
    // The clause
    //------------------------------------------------------------------------
    
    /**
     * Retrieves the value for the attribute {@code clause}.
     * @return The non-null value for attribute {@code clause}.
     */
    java.lang.String getClause(
    );

    /**
     * Sets a new value for the attribute {@code clause}.
     * @param clause The non-null new value for attribute {@code clause}.
     */
    void setClause(
        java.lang.String clause
    );

    
    //------------------------------------------------------------------------
    // Its parameters
    //------------------------------------------------------------------------
    
    /**
     * Retrieves a list containing all the elements for the attribute {@code booleanParam}.
     * @return A list containing all elements for this attribute.
     */
    java.util.List<java.lang.Boolean> getBooleanParam(
    );
    
    /**
     * Clears {@code booleanParam} and adds the given value(s).
     * <p>
     * This method is equivalent to<pre>
     *   list.clear();
     *   for(boolean e : attributeName){
     *     list.add(e);
     *   }
     * </pre>
     * @param booleanParam value(s) to be added to {@code booleanParam}
     */
    void setBooleanParam(
        boolean... booleanParam
    );

    void setBooleanParam(
    	Boolean... booleanParam
    );

    void setBooleanParam(
    	java.util.List<Boolean> booleanParam
    );

    /**
     * Retrieves a list containing all the elements for the attribute {@code dateParam}.
     * @return A list containing all elements for this attribute.
     */
    java.util.List<javax.xml.datatype.XMLGregorianCalendar> getDateParam(
    );

    /**
     * Clears {@code dateParam} and adds the given value(s).
     * <p>
     * This method is equivalent to<pre>
     *   list.clear();
     *   for(javax.xml.datatype.XMLGregorianCalendar e : attributeName){
     *     list.add(e);
     *   }
     * </pre>
     * @param dateParam value(s) to be added to {@code dateParam}
     */
    void setDateParam(
        javax.xml.datatype.XMLGregorianCalendar... dateParam
    );

    void setDateParam(
        java.util.List<javax.xml.datatype.XMLGregorianCalendar> dateParam
    );

    /**
     * Retrieves a list containing all the elements for the attribute {@code dateTimeParam}.
     * @return A list containing all elements for this attribute.
     */
    java.util.List<java.util.Date> getDateTimeParam(
    );

    /**
     * Clears {@code dateTimeParam} and adds the given value(s).
     * <p>
     * This method is equivalent to<pre>
     *   list.clear();
     *   for(java.util.Date e : attributeName){
     *     list.add(e);
     *   }
     * </pre>
     * @param dateTimeParam value(s) to be added to {@code dateTimeParam}
     */
    void setDateTimeParam(
        java.util.Date... dateTimeParam
    );

    void setDateTimeParam(
        java.util.List<java.util.Date> dateTimeParam
    );

    /**
     * Retrieves a list containing all the elements for the attribute {@code decimalParam}.
     * @return A list containing all elements for this attribute.
     */
    java.util.List<java.math.BigDecimal> getDecimalParam(
    );

    /**
     * Clears {@code decimalParam} and adds the given value(s).
     * <p>
     * This method is equivalent to<pre>
     *   list.clear();
     *   for(java.math.BigDecimal e : attributeName){
     *     list.add(e);
     *   }
     * </pre>
     * @param decimalParam value(s) to be added to {@code decimalParam}
     */
    void setDecimalParam(
        java.math.BigDecimal... decimalParam
    );

    void setDecimalParam(
    	java.util.List<java.math.BigDecimal> decimalParam
    );
    
    /**
     * Retrieves a list containing all the elements for the attribute {@code integerParam}.
     * @return A list containing all elements for this attribute.
     */
    java.util.List<java.lang.Integer> getIntegerParam(
    );

    /**
     * Clears {@code integerParam} and adds the given value(s).
     * <p>
     * This method is equivalent to<pre>
     *   list.clear();
     *   for(int e : attributeName){
     *     list.add(e);
     *   }
     * </pre>
     * @param integerParam value(s) to be added to {@code integerParam}
     */
    void setIntegerParam(
        int... integerParam
    );

    void setIntegerParam(
    	Integer... integerParam
    );
    
    void setIntegerParam(
    	java.util.List<Integer> integerParam
    );
    
    /**
     * Retrieves a list containing all the elements for the attribute {@code stringParam}.
     * @return A list containing all elements for this attribute.
     */
    java.util.List<java.lang.String> getStringParam(
    );

    /**
     * Clears {@code stringParam} and adds the given value(s).
     * <p>
     * This method is equivalent to<pre>
     *   list.clear();
     *   for(java.lang.String e : attributeName){
     *     list.add(e);
     *   }
     * </pre>
     * @param stringParam value(s) to be added to {@code stringParam}
     */
    void setStringParam(
        java.lang.String... stringParam
    );
    
    void setStringParam(
    	java.util.List<java.lang.String> stringParam
    );

    //------------------------------------------------------------------------
    // Extends Cloneable
    //------------------------------------------------------------------------

    /**
     * Deep clone
     * 
     * @return a clone of this record
     */
    QueryExtensionRecord clone();

    
    //------------------------------------------------------------------------
    // Class Member
    //------------------------------------------------------------------------

    enum Member {
        booleanParam,
        clause,
        dateParam,
        dateTimeParam,
        decimalParam,
        integerParam,
        stringParam
    }

}
