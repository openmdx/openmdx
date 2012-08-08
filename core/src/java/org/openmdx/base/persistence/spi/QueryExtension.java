/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: QueryExtension.java,v 1.5 2011/11/26 01:35:00 hburger Exp $
 * Description: Query Extension 
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/11/26 01:35:00 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010-2011, OMEX AG, Switzerland
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
package org.openmdx.base.persistence.spi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.query.Extension;
import org.openmdx.base.resource.Records;


/**
 * Query Extension
 * <p>
 * This class is bean-compliant. Hence, it can be externalized
 * with the XMLDecoder.
 * 
 * @see java.beans.XMLDecoder
 * @see java.beans.XMLEncoder
 */
public class QueryExtension implements Extension {

    /**
     * Constructor 
     */
    public QueryExtension() {
        super();
    }

    /**
     * The query extension clause
     */
    private String clause;

    /**
     * Boolean parameter holder
     */
    private List<Boolean> booleanParam;
    
    /**
     * Date parameter holder
     */
    private List<XMLGregorianCalendar> dateParam;
    
    /**
     * Date-time parameter holder
     */
    private List<Date> dateTimeParam;
    
    /**
     * Decimal parameter holder
     */
    private List<BigDecimal> decimalParam;
    
    /**
     * Integer parameter holder
     */
    private List<Integer> integerParam;
    
    /**
     * String parameter holder
     */
    private List<String> stringParam;

    /**
     * The fields for the toString() method
     */
    private static final String[] TO_STRING_FIELDS = {
        "clause",
        "booleanParam",
        "dateParam",
        "dateTimeParam",
        "decimalParam",
        "integerParam",
        "stringParam"
    };
    
    
    //------------------------------------------------------------------------
    // Implements Extension
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#getBooleanParam()
     */
//  @Override
    public List<Boolean> getBooleanParam() {
        if(this.booleanParam == null) {
            this.booleanParam = new ArrayList<Boolean>();
        }
        return this.booleanParam;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#getClause()
     */
//  @Override
    public String getClause() {
        return this.clause;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#getDateParam()
     */
//  @Override
    public List<XMLGregorianCalendar> getDateParam() {
        if(this.dateParam == null) {
            this.dateParam = new ArrayList<XMLGregorianCalendar>();
        }
        return this.dateParam;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#getDateTimeParam()
     */
//  @Override
    public List<Date> getDateTimeParam() {
        if(this.dateTimeParam == null) {
            this.dateTimeParam = new ArrayList<Date>();
        }
        return this.dateTimeParam;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#getDecimalParam()
     */
//  @Override
    public List<BigDecimal> getDecimalParam() {
        if(this.decimalParam == null) {
            this.decimalParam = new ArrayList<BigDecimal>();
        }
        return this.decimalParam;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#getIntegerParam()
     */
//  @Override
    public List<Integer> getIntegerParam() {
        if(this.integerParam == null) {
            this.integerParam = new ArrayList<Integer>();
        }
        return this.integerParam;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#getStringParam()
     */
//  @Override
    public List<String> getStringParam() {
        if(this.stringParam == null) {
            this.stringParam = new ArrayList<String>();
        }
        return this.stringParam;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#setBooleanParam(boolean[])
     */
//  @Override
    public void setBooleanParam(boolean... booleanParam) {
        List<Boolean> parameters = this.getBooleanParam();
        parameters.clear();
        if(booleanParam != null) {
            for(boolean parameter : booleanParam) {
                parameters.add(parameter);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#setClause(java.lang.String)
     */
//  @Override
    public void setClause(String clause) {
        this.clause = clause;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#setDateParam(javax.xml.datatype.XMLGregorianCalendar[])
     */
//  @Override
    public void setDateParam(XMLGregorianCalendar... dateParam) {
        List<XMLGregorianCalendar> parameters = this.getDateParam();
        parameters.clear();
        if(dateParam != null) {
            for(XMLGregorianCalendar parameter : dateParam) {
                parameters.add(parameter);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#setDateTimeParam(java.util.Date[])
     */
//  @Override
    public void setDateTimeParam(Date... dateTimeParam) {
        List<Date> parameters = this.getDateTimeParam();
        parameters.clear();
        if(dateTimeParam != null) {
            for(Date parameter : dateTimeParam) {
                parameters.add(parameter);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#setDecimalParam(java.math.BigDecimal[])
     */
//  @Override
    public void setDecimalParam(BigDecimal... decimalParam) {
        List<BigDecimal> parameters = this.getDecimalParam();
        parameters.clear();
        if(decimalParam != null) {
            for(BigDecimal parameter : decimalParam) {
                parameters.add(parameter);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#setIntegerParam(int[])
     */
//  @Override
    public void setIntegerParam(int... integerParam) {
        List<Integer> parameters = this.getIntegerParam();
        parameters.clear();
        if(integerParam != null) {
            for(int parameter : integerParam) {
                parameters.add(parameter);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#setStringParam(java.lang.String[])
     */
//  @Override
    public void setStringParam(String... stringParam) {
        List<String> parameters = this.getStringParam();
        parameters.clear();
        if(stringParam != null) {
            for(String parameter : stringParam) {
                parameters.add(parameter);
            }
        }
    }
    

    //------------------------------------------------------------------------
    // Is JavaBean
    //------------------------------------------------------------------------

    /**
     * Set the booleanParam values
     * 
     * @param booleanParam
     */
    public void setBooleanParam(
        List<Boolean> booleanParam
    ) {
        List<Boolean> parameters = getBooleanParam();
        parameters.clear();
        parameters.addAll(booleanParam);
    }

    /**
     * Set the dateParam values
     * 
     * @param dateParam
     */
    public void setDateParam(
        List<XMLGregorianCalendar> dateParam
    ) {
        List<XMLGregorianCalendar> parameters = getDateParam();
        parameters.clear();
        parameters.addAll(dateParam);
    }

    /**
     * Set the dateTimeParam values
     * 
     * @param dateTimeParam
     */
    public void setDateTimeParam(
        List<Date> dateTimeParam
    ) {
        List<Date> parameters = getDateTimeParam();
        parameters.clear();
        parameters.addAll(dateTimeParam);
    }

    /**
     * Set the decimalParam values
     * 
     * @param decimalParam
     */
    public void setDecimalParam(
        List<BigDecimal> decimalParam
    ) {
        List<BigDecimal> parameters = getDecimalParam();
        parameters.clear();
        parameters.addAll(decimalParam);
    }

    /**
     * Set the integerParam values
     * 
     * @param integerParam
     */
    public void setIntegerParam(
        List<Integer> integerParam
    ) {
        List<Integer> parameters = getIntegerParam();
        parameters.clear();
        parameters.addAll(integerParam);
    }

    /**
     * Set the stringParam values
     * 
     * @param stringParam
     */
    public void setStringParam(
        List<String> stringParam
    ) {
        List<String> parameters = getStringParam();
        parameters.clear();
        parameters.addAll(stringParam);
    }
    
    
    //------------------------------------------------------------------------
    // Implements Cloneable
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Extension clone(
    ){
        QueryExtension clone = new QueryExtension();
        clone.setClause(this.getClause());
        if(this.booleanParam != null) clone.setBooleanParam(this.booleanParam);
        if(this.dateParam != null) clone.setDateParam(this.dateParam);
        if(this.dateTimeParam != null) clone.setDateTimeParam(this.dateTimeParam);
        if(this.decimalParam != null) clone.setDecimalParam(this.decimalParam);
        if(this.integerParam != null) clone.setIntegerParam(this.integerParam);
        if(this.stringParam != null) clone.setStringParam(this.stringParam);
        return clone;
    }

    
    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString(
    ) {
        return Records.getRecordFactory().asMappedRecord(
		    this.getClass().getName(),
		    null,
		    TO_STRING_FIELDS, 
		    new Object[]{
		        this.clause,
		        this.booleanParam,
		        this.dateParam,
		        this.dateTimeParam,
		        this.decimalParam,
		        this.integerParam,
		        this.stringParam
		    }
		).toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof QueryExtension) {
            QueryExtension that = (QueryExtension) obj;
            return
                (this.clause == null ? that.clause == null : this.clause.equals(that.clause)) &&
                areEquivalent(this.booleanParam, that.booleanParam) &&
                areEquivalent(this.dateParam, that.dateParam) &&
                areEquivalent(this.dateTimeParam, that.dateTimeParam) &&
                areEquivalent(this.decimalParam, that.decimalParam) &&
                areEquivalent(this.integerParam, that.integerParam) &&
                areEquivalent(this.stringParam, that.stringParam);
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode(
    ) {
        return this.clause == null ? super.hashCode() : this.clause.hashCode();
    }

    /**
     * Compares two value lists treating <code>null</code> as empty lists
     * 
     * @param left
     * @param right
     * 
     * @return if the two lists are similar
     */
    private boolean areEquivalent(
        List<?> left,
        List<?> right
    ){
        return left == null || left.isEmpty() ? 
            right == null || right.isEmpty() :
            left.equals(right);
    }

}
