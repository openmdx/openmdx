/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Marshalling Consumer 
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
package org.openmdx.base.accessor.rest;

import java.util.function.Consumer; 

import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.rest.cci.ConsumerRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.kernel.exception.BasicException;

class MarshallingConsumer implements ConsumerRecord {
    
    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -7320802040758252486L;
    
    final DataObjectManager_1 dataObjectManager;
    final Consumer<DataObject_1_0> delegate;

    public MarshallingConsumer(
        final DataObjectManager_1 dataObjectManager,
        final Consumer<DataObject_1_0> delegate
    ) {
        this.dataObjectManager = dataObjectManager;
        this.delegate = delegate;
    }
    
    /* (non-Javadoc)
     * @see javax.resource.cci.Record#getRecordName()
     */
    @Override
    public String getRecordName() {
        return ConsumerRecord.NAME;
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#setRecordName(java.lang.String)
     */
    @Override
    public void setRecordName(String recordName) {
        if(!getRecordName().equals(recordName)) throw BasicException.initHolder(
            new IllegalArgumentException(
                "Unmodifiable Record Name",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    new BasicException.Parameter("fixed", getRecordName()),
                    new BasicException.Parameter("requested", recordName)
                )
            )
        );
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#setRecordShortDescription(java.lang.String)
     */
    @Override
    public void setRecordShortDescription(String description) {
        // The short description set by this method is ignored
    }

    /* (non-Javadoc)
     * @see javax.resource.cci.Record#getRecordShortDescription()
     */
    @Override
    public String getRecordShortDescription() {
        return null;
    }

    @Override
    public void accept(ObjectRecord record) {
        this.delegate.accept(
            dataObjectManager.receive(record)
        );
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone(
    ) throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    
}
