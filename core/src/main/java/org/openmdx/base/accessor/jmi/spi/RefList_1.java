/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Marshalling List 1.0 Implementation
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
 * This product includes or is based on software developed by other
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.accessor.jmi.spi;

import java.util.AbstractList;
import org.openmdx.base.naming.Path;

#if JAVA_8
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.MappedRecord;
#else
import jakarta.resource.cci.IndexedRecord;
import jakarta.resource.cci.MappedRecord;
#endif

/**
 * This class is used for operation dispatching
 */
class RefList_1 extends AbstractList<Object> implements RefList_1_0 {

    RefList_1(IndexedRecord delegate, Jmi1Package_1_0 refPackage) {
        this.delegate = delegate;
        this.refPackage = refPackage;
    }

    private final IndexedRecord delegate;
    private final Jmi1Package_1_0 refPackage;

    @Override
    public Object get(int index) {
        final Object source = this.delegate.get(index);
        return
            source instanceof Path ? refPackage.refObject((Path)source) :
            source instanceof MappedRecord ? refPackage.refCreateStruct((MappedRecord)source) :
            source instanceof IndexedRecord ? refPackage.refCreateList((IndexedRecord)source) :
            source;
    }

    @Override
    public int size() {
        return this.delegate.size();
    }

    @Override
    public IndexedRecord refDelegate() {
        return this.delegate;
    }

}
