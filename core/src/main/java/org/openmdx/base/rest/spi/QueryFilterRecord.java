/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Query Filter Record 
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

import java.util.Collection;
import java.util.List;

import #if JAVA_8 javax.resource.cci.IndexedRecord #else jakarta.resource.cci.IndexedRecord #endif;

import org.openmdx.base.rest.cci.ConditionRecord;
import org.openmdx.base.rest.cci.FeatureOrderRecord;
import org.openmdx.base.rest.cci.QueryExtensionRecord;

/**
 * Query Filter Record
 */
public class QueryFilterRecord 
    extends AbstractMappedRecord<org.openmdx.base.rest.cci.QueryFilterRecord.Member>
    implements org.openmdx.base.rest.cci.QueryFilterRecord 
{
    
    /**
     * Constructor 
     */
    public QueryFilterRecord() {
        super();
    }

    /**
     * Constructs a filter with the specified conditions and
     * order specifiers.
     */
    public QueryFilterRecord(
        List<? extends ConditionRecord> conditions,
        List<? extends FeatureOrderRecord> orderSpecifiers,
        List<? extends QueryExtensionRecord> extensions
    ) {
    	if(has(conditions)) {
    		getCondition().addAll(conditions);
    	}
    	if(has(orderSpecifiers)) {
    		getOrderSpecifier().addAll(orderSpecifiers);
    	}
    	if(has(extensions)) {
    		getExtension().addAll(extensions);
    	}
    }

    /**
     * Constructor 
     *
     * @param that
     */
    protected QueryFilterRecord(
    	QueryFilterRecord that
    ){
        super(that);
    }
    
    /**
     * Allows to share the member information among the instances
     */
    private static final Members<Member> MEMBERS = Members.newInstance(Member.class);

    /**
     * Implements {@code Serializable}
     */
    private static final long serialVersionUID = 6030385859226659109L;

    /**
     * The {@code "condition"} entry
     */
    private IndexedRecord condition;
    
    /**
     * The {@code "orderSpecifier"} entry
     */
    private IndexedRecord orderSpecifier;

    /**
     * The {@code "extension"} entry
     */
    private IndexedRecord extension;
    
    /* (non-Javadoc)
	 * @see org.openmdx.base.rest.spi.AbstractMappedRecord#makeImmutable()
	 */
	@Override
	public void makeImmutable() {
		super.makeImmutable();
		freeze(this.condition);
		freeze(this.extension);
		freeze(this.orderSpecifier);
	}

	/**
     * Tests whether a collection exists and contains at least one element
     * 
     * @return {@code true} if the collection is neither {@code null} nore empty
     */
    private static boolean has(Collection<?> collection) {
    	return collection != null && !collection.isEmpty();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.resource.spi.AbstractRecord#clone()
     */
    @Override
    public QueryFilterRecord clone(
    ){
        return new QueryFilterRecord(this);
    }

	/* (non-Javadoc)
	 * @see javax.resource.cci.Record#getRecordName()
	 */
	@Override
	public String getRecordName() {
		return NAME;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.rest.cci.QueryFilterRecord#getCondition()
	 */
	@SuppressWarnings("unchecked")
    @Override
	public List<ConditionRecord> getCondition() {
		if(this.condition == null) {
			this.condition = newList();
		}
		return this.condition;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.rest.cci.QueryFilterRecord#getOrderSpecifier()
	 */
	@SuppressWarnings("unchecked")
    @Override
	public List<FeatureOrderRecord> getOrderSpecifier() {
		if(this.orderSpecifier == null) {
			this.orderSpecifier = newList();
		}
		return this.orderSpecifier;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.rest.cci.QueryFilterRecord#getExtension()
	 */
	@SuppressWarnings("unchecked")
    @Override
	public List<QueryExtensionRecord> getExtension() {
		if(this.extension == null) {
			this.extension = newList();
		}
		return this.extension;
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.rest.spi.AbstractMappedRecord#get(int)
	 */
	@Override
	protected Object get(Member index) {
		switch(index) {
			case condition: return getCondition();
			case extension: return getExtension();
			case orderSpecifier: return getOrderSpecifier();
			default: return super.get(index);
		}
	}

	/* (non-Javadoc)
	 * @see org.openmdx.base.rest.spi.AbstractMappedRecord#put(int, java.lang.Object)
	 */
	@Override
	protected void put(Member index, Object value) {
		switch(index) {
			case condition: 
				replaceValues(getCondition(), (Collection<?>) value);
				break;
			case extension: 
				replaceValues(getExtension(), (Collection<?>) value);
				break;
			case orderSpecifier: 
				replaceValues(getOrderSpecifier(), (Collection<?>) value);
				break;
			default: 
				super.put(index, value);
		}
	}
    
    //-------------------------------------------------------------------------
    // Implements AnyTypePredicate
    //-------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#elementOf(java.lang.Object[])
     */
    @Override
    public void elementOf(Object... operands) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#elementOf(java.util.Collection)
     */
    @Override
    public void elementOf(Collection<?> operands) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#equalTo(java.lang.Object)
     */
    @Override
    public void equalTo(Object operand) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#notAnElementOf(java.lang.Object[])
     */
    @Override
    public void notAnElementOf(Object... operands) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#notAnElementOf(java.util.Collection)
     */
    @Override
    public void notAnElementOf(Collection<?> operands) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#notEqualTo(java.lang.Object)
     */
    @Override
    public void notEqualTo(Object operand) {
        throw new UnsupportedOperationException();
    }

	/* (non-Javadoc)
	 * @see org.openmdx.base.rest.spi.AbstractMappedRecord#members()
	 */
	@Override
	protected Members<Member> members() {
		return MEMBERS;
	}

}
