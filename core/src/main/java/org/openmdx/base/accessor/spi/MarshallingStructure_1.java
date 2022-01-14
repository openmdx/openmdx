/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Marshalling Structure
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
package org.openmdx.base.accessor.spi;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.openmdx.base.accessor.cci.Structure_1_0;
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.MarshallingSortedMap;
import org.openmdx.base.collection.MarshallingSparseArray;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.w3c.cci2.SparseArray;

/**
 * Marshalling Structure
 */
public class MarshallingStructure_1 
    extends DelegatingStructure_1
{

    /**
     * Constructor 
     *
     * @param structure
     * @param marshaller
     */
    public MarshallingStructure_1(
        Structure_1_0 structure,
        Marshaller marshaller
    ) {
        super(structure);
        this.marshaller = marshaller;
    }

    /**
     * Constructor 
     * <p>
     * A subclass must override getMarshaller(String)!
     *
     * @param structure
     */
    protected MarshallingStructure_1(
        Structure_1_0 structure
    ) {
        this(structure, null);
    }
    
    /**
     * The marshaller, unless getMarshaller is overridden;
     */
    private Marshaller marshaller;

    /**
     * A subclass must override this method if the marshaller is feature dependent.
     * 
     * @param feature
     * 
     * @return the (maybe feature specific) marshaller
     */
    protected Marshaller getMarshaller(
        String feature
    ) throws ServiceException {
        return this.marshaller;
    }

    
    //------------------------------------------------------------------------
    // Implements Structure_1_0
    //------------------------------------------------------------------------
        
    /**
     * Returns the object's model class.
     *
     * @return  the object's model class
     */
    @Override
    public String objGetType(
    ){
        return getDelegate().objGetType();
    }

    /**
     * Return the field names in this structure.
     *
     * @return  the (String) field names contained in this structure
     */
    @Override
    public List<String> objFieldNames(
    ){
        return getDelegate().objFieldNames();
    }
     
    /**
     * Get a field.
     *
     * @param       fieldName
     *              the fields's name
     *
     * @return      the fields value which may be null.
     *
     * @exception   ServiceException BAD_MEMBER_NAME
     *              if the structure has no such field
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object objGetValue(
        String fieldName
    ) throws ServiceException {
        Object value = getDelegate().objGetValue(fieldName);
        Marshaller marshaller = getMarshaller(fieldName);
        return value instanceof List ? new MarshallingList<Object>(
            marshaller,
            (List<?>)value
        ) : value instanceof Set ? new MarshallingSet<Object>(
            marshaller,
            (Set<?>)value
        ) : value instanceof SparseArray ? new MarshallingSparseArray(
            marshaller,
            (SparseArray<Object>)value
        ) : value instanceof SortedMap ? new MarshallingSortedMap(
            marshaller,
            (SortedMap<Integer,Object>)value
        ) : marshaller.marshal(value);
    }

}
