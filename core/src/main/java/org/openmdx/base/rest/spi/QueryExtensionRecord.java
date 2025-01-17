/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Query Extension 
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
package org.openmdx.base.rest.spi;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import #if JAVA_8 javax.resource.cci.IndexedRecord #else jakarta.resource.cci.IndexedRecord #endif;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Query Extension
 * <p>
 * This class is bean-compliant. Hence, it can be externalized with the XMLEncoder.
 * 
 * @see org.openmdx.base.text.conversion.JavaBeans
 */
public class QueryExtensionRecord 
	extends AbstractMappedRecord<org.openmdx.base.rest.cci.QueryExtensionRecord.Member> 
	implements org.openmdx.base.rest.cci.QueryExtensionRecord 
{

	/**
     * Constructor 
     */
    public QueryExtensionRecord() {
        super();
    }

	/**
     * Constructor for clone
     */
    protected QueryExtensionRecord(
    	QueryExtensionRecord that
    ) {
        super(that);
    }
    
    /**
     * The query extension clause
     */
    private String clause;

    /**
     * Boolean parameter holder
     */
    private IndexedRecord booleanParam;
    
    /**
     * Date parameter holder
     */
    private IndexedRecord dateParam;
    
    /**
     * Date-time parameter holder
     */
    private IndexedRecord dateTimeParam;
    
    /**
     * Decimal parameter holder
     */
    private IndexedRecord decimalParam;
    
    /**
     * Integer parameter holder
     */
    private IndexedRecord integerParam;
    
    /**
     * String parameter holder
     */
    private IndexedRecord stringParam;
    
    /**
     * Allows to share the member information among the instances
     */
    private static final Members<Member> MEMBERS = Members.newInstance(Member.class);
    
    /**
	 * Implements {@code Serializable}
	 */
	private static final long serialVersionUID = -2305541713460111707L;
    
	
    //------------------------------------------------------------------------
    // Implements Freezable
    //------------------------------------------------------------------------
	
    /* (non-Javadoc)
	 * @see org.openmdx.base.rest.spi.AbstractMappedRecord#makeImmutable()
	 */
	@Override
	public void makeImmutable() {
		super.makeImmutable();
		freeze(this.booleanParam);
		freeze(this.dateParam);
		freeze(this.dateTimeParam);
		freeze(this.dateTimeParam);
		freeze(this.integerParam);
		freeze(this.stringParam);
	}
	
    //------------------------------------------------------------------------
    // Implements Extension
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#getBooleanParam()
     */
	@SuppressWarnings("unchecked")
    @Override
    public List<Boolean> getBooleanParam() {
    	if(this.booleanParam == null) {
    		this.booleanParam = newList();
    	}
        return this.booleanParam;
    }

	/* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#getClause()
     */
    @Override
    public String getClause() {
        return this.clause;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#getDateParam()
     */
	@SuppressWarnings("unchecked")
    @Override
    public List<XMLGregorianCalendar> getDateParam() {
    	if(this.dateParam == null) {
    		this.dateParam = newList();
    	}
        return this.dateParam;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#getDateTimeParam()
     */
	@SuppressWarnings("unchecked")
    @Override
    public List<Date> getDateTimeParam() {
    	if(this.dateTimeParam == null) {
    		this.dateTimeParam = newList();
    	}
        return this.dateTimeParam;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#getDecimalParam()
     */
	@SuppressWarnings("unchecked")
    @Override
    public List<BigDecimal> getDecimalParam() {
    	if(this.decimalParam == null) {
    		this.decimalParam = newList();
    	}
        return this.decimalParam;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#getIntegerParam()
     */
	@SuppressWarnings("unchecked")
    @Override
    public List<Integer> getIntegerParam() {
    	if(this.integerParam == null) {
    		this.integerParam = newList();
    	}
        return this.integerParam;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#getStringParam()
     */
	@SuppressWarnings("unchecked")
    @Override
    public List<String> getStringParam() {
    	if(this.stringParam == null) {
    		this.stringParam = newList();
    	}
        return this.stringParam;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#setBooleanParam(boolean[])
     */
    @Override
    public void setBooleanParam(boolean... booleanParam) {
        List<Boolean> parameters = this.getBooleanParam();
        parameters.clear();
        if(booleanParam != null) {
            for(boolean parameter : booleanParam) {
                parameters.add(Boolean.valueOf(parameter));
            }
        }
    }

    @Override
    public void setBooleanParam(Boolean... booleanParam) {
		replaceValues(getBooleanParam(), booleanParam);
    }

	@Override
	public void setBooleanParam(
		List<Boolean> booleanParam
	) {
		replaceValues(getBooleanParam(), booleanParam);
	}

    /* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#setClause(java.lang.String)
     */
    @Override
    public void setClause(String clause) {
    	assertMutability();
        this.clause = clause;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#setDateParam(javax.xml.datatype.XMLGregorianCalendar[])
     */
    @Override
    public void setDateParam(XMLGregorianCalendar... dateParam) {
    	replaceValues(getDateParam(), dateParam);
    }

	@Override
	public void setDateParam(List<XMLGregorianCalendar> dateParam) {
		replaceValues(getDateParam(), dateParam);
	}

    /* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#setDateTimeParam(java.util.Date[])
     */
    @Override
    public void setDateTimeParam(Date... dateTimeParam) {
    	replaceValues(getDateTimeParam(), dateTimeParam);
    }

	@Override
	public void setDateTimeParam(List<Date> dateTimeParam) {
		replaceValues(getDateTimeParam(), dateTimeParam);
	}

    /* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#setDecimalParam(java.math.BigDecimal[])
     */
    @Override
    public void setDecimalParam(BigDecimal... decimalParam) {
    	replaceValues(getDecimalParam(), decimalParam);
    }

	@Override
	public void setDecimalParam(List<BigDecimal> decimalParam) {
		replaceValues(getDecimalParam(), decimalParam);
	}

    /* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#setIntegerParam(int[])
     */
    @Override
    public void setIntegerParam(int... integerParam) {
    	final List<Integer> target = getIntegerParam();
    	target.clear();
    	for(int value : integerParam) {
    		target.add(Integer.valueOf(value));
    	}
    }

    @Override
    public void setIntegerParam(Integer... integerParam) {
    	replaceValues(getIntegerParam(), integerParam);
    }

	@Override
	public void setIntegerParam(List<Integer> integerParam) {
		replaceValues(getIntegerParam(), integerParam);
	}
	
    /* (non-Javadoc)
     * @see org.openmdx.base.query.Extension#setStringParam(java.lang.String[])
     */
    @Override
    public void setStringParam(String... stringParam) {
    	replaceValues(getStringParam(), stringParam);
    }

	@Override
	public void setStringParam(List<String> stringParam) {
		replaceValues(getStringParam(), stringParam);
	}

    //------------------------------------------------------------------------
    // Implements Cloneable
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public QueryExtensionRecord clone(
    ){
    	return new QueryExtensionRecord(this);
    }

    
    //------------------------------------------------------------------------
    // Extends AbstractMappedRecord
    //------------------------------------------------------------------------

	@Override
	public String getRecordName() {
		return NAME;
	}

	@Override
	protected Object get(Member index) {
		switch(index) {
			case booleanParam: return getBooleanParam();
			case clause: return getClause();
			case dateParam: return getDateParam();
			case dateTimeParam: return getDateTimeParam();
			case decimalParam: return getDecimalParam();
			case integerParam: return getIntegerParam();
			case stringParam: return getStringParam();
			default: return super.get(index);
		}
	}

	@Override
	protected void put(Member index, Object value) {
		switch(index) {
			case booleanParam: 
				replaceValues(getBooleanParam(), (IndexedRecord) value);
				break;
			case clause: 
				setClause((String) value);
				break;
			case dateParam: 
				replaceValues(getDateParam(), (IndexedRecord) value);
				break;
			case dateTimeParam: 
				replaceValues(getDateTimeParam(), (IndexedRecord) value);
				break;
			case decimalParam: 
				replaceValues(getDecimalParam(), (IndexedRecord) value);
				break;
			case integerParam: 
				replaceValues(getIntegerParam(), (IndexedRecord) value);
				break;
			case stringParam: 
				replaceValues(getStringParam(), (IndexedRecord) value);
				break;
			default: 
				super.get(index);
		}
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.rest.spi.AbstractMappedRecord#members()
	 */
	@Override
	protected Members<Member> members() {
		return MEMBERS;
	}

}
