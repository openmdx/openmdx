/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Sparse Array Record 
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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.resource.cci.MappedRecord;

import org.openmdx.base.resource.cci.Freezable;
import org.openmdx.kernel.text.MultiLineStringRepresentation;
import org.openmdx.kernel.text.format.IndentingFormatter;
import org.w3c.cci2.AbstractSparseArray;
import org.w3c.cci2.SparseArray;

/**
 * {@code "sparsearray"} Record
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SparseArrayRecord
    extends AbstractSparseArray
    implements org.openmdx.base.resource.cci.SparseArrayRecord, MultiLineStringRepresentation, Freezable {

    /**
     * Constructor
     */
    SparseArrayRecord() {
        this(new TreeMap<>(), false);
    }

    /**
     * Constructor
     * 
     * @param delegate
     *            the delegate
     * @param immutable
     *            {@code true} if the delegate is immutable
     */
    private SparseArrayRecord(
        SortedMap<Integer, Object> delegate,
        boolean immutable
    ) {
        this.values = delegate;
        this.immutable = immutable;
    }

    /**
     * @serial
     */
    private boolean immutable = false;

    /**
     * The values are serialized explicitly
     */
    private transient SortedMap<Integer, Object> values;

    /**
     * Implements {@code Serializable}
     */
    private static final long serialVersionUID = -3160894638270297937L;

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.cci2.AbstractSparseArray#delegate()
     */
    @Override
    protected SortedMap<Integer, Object> delegate() {
        return values;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.w3c.cci2.AbstractSparseArray#subArray(java.util.SortedMap)
     */
    @Override
    protected SparseArray subArray(SortedMap delegate) {
        return new SparseArrayRecord(delegate, immutable);
    }

    //------------------------------------------------------------------------
    // Implements Serializable
    //------------------------------------------------------------------------

    /**
     * Serialize
     * 
     * @param out
     *            the output stream
     * 
     * @throws IOException
     */
    private void writeObject(
        ObjectOutputStream out
    )
        throws IOException {
        out.defaultWriteObject();
        int size = this.values.size();
        out.writeInt(size);
        for (Map.Entry<?, ?> entry : this.values.entrySet()) {
            out.writeObject(entry.getKey());
            out.writeObject(entry.getValue());
        }
    }

    /**
     * De-serialize
     * 
     * @param in
     *            the input stream
     * 
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(
        java.io.ObjectInputStream in
    )
        throws IOException,
        ClassNotFoundException {
        in.defaultReadObject();
        final int size = in.readInt();
        final TreeMap<Integer, Object> values = new TreeMap<>();
        for (int i = 0; i < size; i++) {
            values.put(
                (Integer) in.readObject(),
                in.readObject()
            );
        }
        this.values = this.immutable ? Collections.unmodifiableSortedMap(values) : values;
    }

    //------------------------------------------------------------------------
    // Implements Record
    //------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see javax.resource.cci.Record#getRecordName()
     */
    @Override
    public String getRecordName() {
        return NAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.resource.cci.Record#setRecordName(java.lang.String)
     */
    @Override
    public void setRecordName(String name) {
        if (!NAME.equals(name)) {
            throw new IllegalArgumentException("SparseArrayRecord requires the record name '" + NAME + "': '" + name + "'");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.resource.cci.Record#setRecordShortDescription(java.lang.String)
     */
    @Override
    public void setRecordShortDescription(String description) {
        if (description != null) {
            throw new UnsupportedOperationException("SparseArrayRecord does not support a short description: '" + description + "'");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.resource.cci.Record#getRecordShortDescription()
     */
    @Override
    public String getRecordShortDescription() {
        return null;
    }

    //--------------------------------------------------------------------------
    // Implements Freezable
    //--------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.resource.cci.Freezable#makeImmutable()
     */
    @Override
    public synchronized void makeImmutable() {
        if (!this.immutable) {
            this.values = Collections.unmodifiableSortedMap(this.values);
            this.immutable = true;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openmdx.base.resource.cci.Freezable#isImmutable()
     */
    @Override
    public boolean isImmutable() {
        return this.immutable;
    }

    //--------------------------------------------------------------------------
    // Implements {@code Cloneable}
    //--------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    @Override
    public org.openmdx.base.resource.cci.SparseArrayRecord clone() {
        final SparseArrayRecord that = new SparseArrayRecord();
        that.putAll(this);
        return that;
    }

    
    //--------------------------------------------------------------------------
    // Extends {@code MultiLineStringRepresentation}
    //--------------------------------------------------------------------------
    
    /**
     * Returns a multi-line string representation of this MappedRecord.
     * <p>
     * The string representation consists of the record name, follwed by the
     * optional short description enclosed in parenthesis (" (...)"), followed 
     * by a colon and the mappings enclosed in braces (": {...}"). Each
     * key-value mapping is rendered as the key followed by an equals sign ("=")
     * followed by the associated value written on a separate line and indented
     * while embedded lines are indented as well.
     *
     * @return   a multi-line String representation of this Record.
     */
    @Override
    public String toString(
    ){
        return IndentingFormatter.toString(this);
    }


    //--------------------------------------------------------------------------
    // Extends {@code Object}
    //--------------------------------------------------------------------------
    
    /**
     * Returns the hash code for the Record instance. 
     *
     * @return hash code
     */
    @Override
    public int hashCode(
    ){
        return this.values.hashCode();
    }

    /**
     * Check whether this instance has the same content as another Map.
     * <p>
     * The Record's name and short description are ignored.
     *
     * @return  true if two instances are equal
     */
    @Override
    public boolean equals(
        Object that
    ){
        return this == that || (
            that instanceof MappedRecord && 
            VariableSizeMappedRecord.areEqual(this, (MappedRecord)that)
        );
    }

}
