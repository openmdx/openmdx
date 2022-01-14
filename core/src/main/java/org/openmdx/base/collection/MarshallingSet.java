/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Marshalling Set
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
package org.openmdx.base.collection;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.openmdx.base.marshalling.Marshaller;

/**
 * A Marshalling Set
 */
public class MarshallingSet<E>
    extends MarshallingCollection<E>
    implements Set<E>, Serializable
{
 
    /**
     * Standard constructor
     * 
     * @param marshaller
     * @param set
     * @param reluctantUnmarshalling 
     */   
    public MarshallingSet(
        Marshaller marshaller,
        Set<?> set, 
        Unmarshalling unmarshalling
    ) {
        super(marshaller, set, unmarshalling);
        this.delegateIsInstanceOfSet = true;
    }

    /**
     * Standard constructor
     * 
     * @param marshaller
     * @param set
     */   
    @SuppressWarnings("rawtypes")
    public MarshallingSet(
        Marshaller marshaller,
        Set set
    ) {
        super(marshaller, set);
        this.delegateIsInstanceOfSet = true;
    }

    /**
     * Be very carefully when using this constructor!
     * 
     * @param marshaller
     * @param set
     *        A collection behaving like a set
     */
    @SuppressWarnings("rawtypes")
    public MarshallingSet(
        Marshaller marshaller,
        Collection set
    ) {
        super(marshaller, set);
        this.delegateIsInstanceOfSet = set instanceof Set;
    }

    /**
     * Be very carefully when using this constructor!
     * 
     * @param marshaller
     * @param set
     *        A collection behaving like a set
     * @param unmarshalling 
     */
    @SuppressWarnings("rawtypes")
    public MarshallingSet(
        Marshaller marshaller,
        Collection set, 
        Unmarshalling unmarshalling
    ) {
        super(marshaller, set, unmarshalling);
        this.delegateIsInstanceOfSet = set instanceof Set;
    }

    private static final long serialVersionUID = 3256439200998961717L;
  
    /**
     * True unless the delegate is a Collection but not a Set.
     */
    private final boolean delegateIsInstanceOfSet;        
    
    //------------------------------------------------------------------------
    // Implements Set
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.util.Collection#add(java.lang.Object)
     */
    @Override
    public boolean add(E element) {
        return (
            this.delegateIsInstanceOfSet || ! this.contains(element)
        ) && super.add(element);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object object) {
    	if (object == this) {
    	    return true;
    	}
    	if (!(object instanceof Set)) {
    	    return false;
    	}
    	Collection<?> that = (Collection<?>) object;
    	return that.size() == this.size() && this.containsAll(that);  
    }

    /**
     * Returns the hash code value for this set.  The hash code of a set is
     * defined to be the sum of the hash codes of the elements in the set,
     * where the hash code of a <tt>null</tt> element is defined to be zero.
     * This ensures that <tt>s1.equals(s2)</tt> implies that
     * <tt>s1.hashCode()==s2.hashCode()</tt> for any two sets <tt>s1</tt>
     * and <tt>s2</tt>, as required by the general contract of
     * {@link Object#hashCode}.
     *
     * <p>This implementation iterates over the set, calling the
     * <tt>hashCode</tt> method on each element in the set, and adding up
     * the results.
     *
     * @return the hash code value for this set
     * @see Object#equals(Object)
     * @see Set#equals(Object)
     */
    @Override
    public int hashCode() {
        int h = 0;
        Iterator<E> i = iterator();
        while (i.hasNext()) {
            E obj = i.next();
                if (obj != null)
                    h += obj.hashCode();
            }
        return h;
    }
    
}
