/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: JCA: variable-size IndexedRecord implementation
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
package org.openmdx.base.resource.spi;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import #if JAVA_8 javax.resource.cci.IndexedRecord #else jakarta.resource.cci.IndexedRecord #endif;

import org.openmdx.base.resource.cci.Freezable;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.text.MultiLineStringRepresentation;
import org.openmdx.kernel.text.format.IndentingFormatter;

/**
 * Java Connector Architecture:
 * A variable-size IndxedRecord implementation.
 */
@SuppressWarnings({"rawtypes"})
public class VariableSizeIndexedRecord 
    extends AbstractList
    implements IndexedRecord, MultiLineStringRepresentation, Freezable
{

	/**
     * Creates an IndexedRecord with the specified name and the given content.  
     *
     * @param     recordName
     *            The name of the record acts as a pointer to the meta 
     *            information (stored in the metadata repository) for a specific
     *            record type. 
     * @param     recordShortDescription
     *            The short description of the Record; or null.
     */
    VariableSizeIndexedRecord(
        String recordName,
        String recordShortDescription
    ){
        this.recordName = recordName;
        this.description = recordShortDescription;
        this.delegate = new ArrayList<Object>();
    }

    /**
     * Creates an IndexedRecord with the specified name and the given content.  
     *
     * @param     recordName
     *            The name of the record acts as a pointer to the meta 
     *            information (stored in the meta data repository) for a specific
     *            record type. 
     */
    protected VariableSizeIndexedRecord(
        String recordName
    ){
        this(recordName,null);
    }

    /**
     * Creates an IndexedRecord with the specified name and the given content.  
     *
     * @param     recordName
     *            The name of the record acts as a pointer to the meta 
     *            information (stored in the metadata repository) for a specific
     *            record type. 
     * @param     description
     *            The description of the Record; or null.
     * @param     initialContent
     *            The list's initial content
     */
    VariableSizeIndexedRecord(
        String recordName,
        String description,
        Collection<?> initialContent
    ){
        this.recordName = recordName;
        this.description = description;
        this.delegate = new ArrayList<Object>(initialContent);
    }
    
    /**
     * The record name
     */
    private String recordName;    

    /**
     * The record short description
     */
    private String description;

    /**
     * Implements {@code Freezable}
     */
    private boolean immutable = false;

    /**
     * Extends {@code AbstractList}
     */
    private final List<Object> delegate;
    
    /**
     * Implements {@code Serializable}
     */
	private static final long serialVersionUID = 169692966585077358L;


    //--------------------------------------------------------------------------
    // Implements Freezable
    //--------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.Freezable#makeImmutable()
     */
    @Override
    public void makeImmutable() {
    	if(!this.immutable) {
	    	this.immutable = true;
	    	for(ListIterator<Object> i = this.delegate.listIterator(); i.hasNext();){
	    		Object original = i.next();
//	    		Object immutable = Isolation.toImmutable(original);
//	    		if(original != immutable) {
//	    			i.set(immutable);
//	    		}
	    	}
    	}
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.resource.cci.Freezable#isImmutable()
     */
    @Override
    public boolean isImmutable() {
    	return this.immutable;
    }
    
	/**
	 * Asserts that the object is mutable
	 * 
	 * @throws IllegalStateException if the record is immutable
	 */
	protected void assertMutability(){
		if(this.immutable) {
	        throw new IllegalStateException(
                "This record is frozen",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ILLEGAL_STATE,
                    new BasicException.Parameter("class", getClass().getName()),
                    new BasicException.Parameter("name", getRecordName()),
                    new BasicException.Parameter("immutable", Boolean.TRUE)
                )
            );
		}
	}

	
    //--------------------------------------------------------------------------
    // Implements Record
    //--------------------------------------------------------------------------
    
    /**
     * Gets the name of the Record. 
     *
     * @return  String representing name of the Record
     */
    @Override
    public final String getRecordName(
    ){
        return this.recordName;
    }
    
	/**
     * Sets the name of the Record. 
     *
     * @param name
     *        Name of the Record
     */
    @Override
    public final void setRecordName(
        String name
    ){
        this.recordName = name;
    }

    /**
     * Gets a short description string for the Record.
     * This property is used primarily by application development tools. 
     *
     * @return   String representing a short description of the Record
     */
    @Override
    public final String getRecordShortDescription(
    ){
        return this.description;
    }

    /**
     * Sets a short description string for the Record.
     * This property is used primarily by application development tools. 
     *
     * @param description
     *        Description of the Record
     */
    @Override
    public final void setRecordShortDescription(
        String description
    ){
        this.description = description;
    }

    
    //--------------------------------------------------------------------------
    // Implements Cloneable
    //--------------------------------------------------------------------------
  
    /**
     * Creates and returns a copy of this object.
     *
     * @return  a copy of this IndexedRecord instance
     */
    @Override
    public VariableSizeIndexedRecord clone(
	){
    	return new VariableSizeIndexedRecord(
			this.recordName,
			this.description,
			this
		);
    }

    
    //--------------------------------------------------------------------------
    // Implements List
    //--------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
    	return IndexedRecords.getHashCode(this);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object that) {
    	return IndexedRecords.areEqual(this, that);
    }
    
    /**
     * Returns a multi-line string representation of this IndexedRecord.
     * <p>
     * The string representation consists of the record name, follwed by the
     * optional short description enclosed in parenthesis (" (...)"), followed 
     * by a colon and the values enclosed in square brackets (": [...]"). Each
     * value is written on a separate line and indented while embedded lines are
     * indented as well.
     *
     * @return   a multi-line String representation of this Record.
     */
    @Override
    public String toString(
    ){
        return IndentingFormatter.toString(this);
    }

	/* (non-Javadoc)
	 * @see java.util.AbstractList#get(int)
	 */
	@Override
	public Object get(int index) {
		return this.delegate.get(index);
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#add(int, java.lang.Object)
	 */
	@Override
	public void add(int index, Object element) {
		assertMutability();
		delegate.add(index, element);
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#remove(int)
	 */
	@Override
	public Object remove(int index) {
		assertMutability();
		return delegate.remove(index);
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractList#set(int, java.lang.Object)
	 */
	@Override
	public Object set(int index, Object element) {
		assertMutability();
		return delegate.set(index, element);
	}

	/* (non-Javadoc)
	 * @see java.util.AbstractCollection#size()
	 */
	@Override
	public int size() {
		return delegate.size();
	}

}
