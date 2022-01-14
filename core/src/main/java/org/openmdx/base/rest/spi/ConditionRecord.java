/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Condition Record 
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

import java.util.EnumSet;

import javax.resource.cci.IndexedRecord;

import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.query.ConditionType;
import org.openmdx.base.query.Quantifier;

/**
 * Condition Record
 */
public class ConditionRecord 
    extends AbstractMappedRecord<org.openmdx.base.rest.cci.ConditionRecord.Member>
    implements org.openmdx.base.rest.cci.ConditionRecord 
{
    
	/**
     * Constructor 
     */
    public ConditionRecord() {
        this.values = newList(); // maybe it has to be replaced later on
    }

    /**
     * Constructor 
     *
     * @param type the condition type
     */
    protected ConditionRecord(
        ConditionType type
    ){
        this.type = type;
        this.values = TYPES_WITH_UNORDERED_VALUES.contains(type) ? newSet() : newList();
    }

    /**
     * Constructor 
     * 
     * @param quantifier the quantifier
     * @param feature the feature name
     * @param type the condition type
     * @param values the condition values with their type dependent semantic
     */
    public ConditionRecord(
	    Quantifier quantifier,
	    String feature,
	    ConditionType type,
	    Object... values
    ){
        this(type);
        this.quantifier = quantifier;
        this.feature = feature;
        setValue(values);
    }
    
    /**
     * Constructor for clone
     * 
     * @param that the condition to be clone
     */
    protected ConditionRecord(
		ConditionRecord that
	){
    	super(that);
    }
    
    /**
     * Allows to share the member information among the instances
     */
    private static final Members<Member> MEMBERS = Members.newInstance(Member.class);
    
    /**
	 * Implements <code>Serializable</code>
	 */
	private static final long serialVersionUID = -2195250394522870037L;

    /**
     * Defines whether the value array is treaded as SET or LIST
     */
    private static final EnumSet<ConditionType> TYPES_WITH_UNORDERED_VALUES = EnumSet.of(
    	ConditionType.IS_IN,
    	ConditionType.IS_NOT_IN,
    	ConditionType.IS_LIKE,
    	ConditionType.IS_UNLIKE,
    	ConditionType.SOUNDS_LIKE,
    	ConditionType.SOUNDS_UNLIKE
    );
	
    /**
     * The condition type   
     */
    private ConditionType type;

    /**
     * The quantifier, i.e. &#x2200; or &#x2203;  
     */
    private Quantifier quantifier;
    
    /**
     * The unqualified feature name
     */
    private String feature;
    
    /**
     * The condition specific values
     */
    private IndexedRecord values;


	/* (non-Javadoc)
	 * @see org.openmdx.base.rest.spi.AbstractMappedRecord#makeImmutable()
	 */
	@Override
	public void makeImmutable() {
		super.makeImmutable();
		freeze(this.values);
	}

	/* (non-Javadoc)
	 * @see javax.resource.cci.Record#getRecordName()
	 */
	@Override
	public String getRecordName() {
		return NAME;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.rest.spi.AbstractMappedRecord#get(int)
	 */
	@Override
	protected Object get(Member index) {
		switch(index) {
			case feature: return getFeature();
			case quantifier: return jcaValue(getQuantifier());
			case type: return jcaValue(getType());
			case value: return this.values;
			default: return super.get(index);
		}
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.rest.spi.AbstractMappedRecord#put(int, java.lang.Object)
	 */
	@Override
	protected void put(Member index, Object value) {
		switch(index) {
			case feature:
				setFeature((String) value);
				break;
			case quantifier: 
				setQuantifier(Quantifier.valueOf(((Short)value).shortValue()));
				break;				 
			case type: 
				setType(ConditionType.valueOf(((Short)value).shortValue()));
				break;
			case value: 
				setValues((IndexedRecord)value);
				break;
			default: 
				super.put(index, value);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.openmdx.base.rest.spi.AbstractMappedRecord#clone()
	 */
	@Override
	public ConditionRecord clone() {
		return new ConditionRecord(this);
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.rest.cci.ConditionRecord#getType()
	 */
	@Override
	public ConditionType getType() {
		return this.type;
	}

	/**
	 * Set the feature type
	 * 
	 * @param type
	 */
	protected void setType(
		ConditionType type
	){
		assertMutability();
		this.type = type;
		// fix NullPointer: may be null while cloning
		final boolean unordered = TYPES_WITH_UNORDERED_VALUES.contains(type);
		if(this.values == null) {
		    this.values = unordered ? newSet() : newList();
		} else {
    		if(this.values.getRecordName().equals(Multiplicity.SET.code()) != unordered) {
    			this.values = unordered ? newSet(this.values) : newList(this.values);
    		}
		}
	}

	@Override
	public Quantifier getQuantifier() {
		return this.quantifier;
	}

	/**
	 * @param quantifier the quantifier to set
	 */
	protected void setQuantifier(Quantifier quantifier) {
		this.quantifier = quantifier;
	}

	@Override
	public String getFeature() {
		return this.feature;
	}

	/**
	 * @param feature the feature to set
	 */
	protected void setFeature(String feature) {
		assertMutability();
		this.feature = feature;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object[] getValue() {
		return this.values.toArray(new Object[this.values.size()]);
	}

    /**
     * Retrieve one of the values (with a condition type specific semantic)
     * 
     * @param index
     * 
     * @return the values
     */
    @Override
    public Object getValue(
        int index
    ) {
        return getValue()[index];
    }

    /**
     * Replace the values (with a condition type specific semantic and cardinality)
     * 
     * @param values the values
     */
    @SuppressWarnings("unchecked")
	public void setValue(
        Object... values
    ) {
		assertMutability();
    	this.values.clear();
    	for(Object value : values) {
    		this.values.add(value);
    	}
	}

    /**
     * Replace the values (with a condition type specific semantic and cardinality)
     * 
     * @param values the values
     */
    @SuppressWarnings("unchecked")
	public void setValues(
        IndexedRecord values
    ) {
		assertMutability();
    	this.values.clear();
    	this.values.addAll(values);
	}
    
    /**
     * Retrieve the condition's string representation, e.g.<code> 
     * &#x2026;(&#x2200; color | color &#x2208; ["RED", "GREEN"])&#x2026;</code> 
     * 
     * @return the condition's string representation
     */
	@Override
	public String getRecordShortDescription() {
        ConditionType type = this.getType();
        if(type == null) {
            return super.getRecordShortDescription();
        } else {
            StringBuilder description = new StringBuilder();
            if(this.quantifier != null) {
                description.append(
                    this.quantifier.symbol()
                ).append(
                    ' '
                ).append(
                    this.feature
                ).append(
                    " | "
                ).append(
                    this.feature
                ).append(
                    ' '
                );
            }
            return description.append(
                type.symbol()
            ).append(
                ' '
            ).append(
                this.values
            ).toString();
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
