/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Set Record 
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
package org.openmdx.base.resource.cci;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Spliterator;

import javax.resource.cci.IndexedRecord;

/**
 * {@code "set"} Record
 */
@SuppressWarnings("rawtypes")
public interface SetRecord extends IndexedRecord, Set {

    String NAME = "set";

    //------------------------------------------------------------------------
    // Implements {@code List}
    //------------------------------------------------------------------------

    /**
     * @deprecated this {@IndexedRecord} represents a {@code Set}
     */
    @Override
    @Deprecated
    Object get(
        int index
    );

    /**
     * @deprecated this {@IndexedRecord} represents a {@code Set}
     */
    @Override
    @Deprecated
    void add(
        int index,
        Object element
    );

    /**
     * @deprecated this {@IndexedRecord} represents a {@code Set}
     */
    @Override
    @Deprecated
    Object remove(
        int index
    );

    /**
     * @deprecated this {@IndexedRecord} represents a {@code Set}
     */
    @Override
    @Deprecated
    Object set(
        int index,
        Object element
    );

    /**
     * @deprecated this {@IndexedRecord} represents a {@code Set}
     */
    @Override
    @Deprecated
    int indexOf(
        Object o
    );

    /**
     * @deprecated this {@IndexedRecord} represents a {@code Set}
     */
    @Override
    @Deprecated
    int lastIndexOf(
        Object o
    );

    /**
     * @deprecated this {@IndexedRecord} represents a {@code Set}
     */
    @Override
    @Deprecated
    boolean addAll(
        int index,
        Collection c
    );

    /**
     * @deprecated this {@IndexedRecord} represents a {@code Set}
     */
    @Override
    @Deprecated
    ListIterator listIterator();

    /**
     * @deprecated this {@IndexedRecord} represents a {@code Set}
     */
    @Override
    @Deprecated
    ListIterator listIterator(
        int index
    );

    /**
     * @deprecated this {@IndexedRecord} represents a {@code Set}
     */
    @Override
    @Deprecated
    List subList(
        int fromIndex,
        int toIndex
    );

    /* (non-Javadoc)
     * @see java.util.List#spliterator()
     */
    @Override
    default Spliterator spliterator() {
        return Set.super.spliterator();
    }

}
