/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: OutputRecords 
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
package org.openmdx.base.dataprovider.layer.persistence.jdbc;

#if JAVA_8
import javax.resource.ResourceException;
import javax.resource.cci.IndexedRecord;
import javax.resource.cci.Record;
#else
import jakarta.resource.ResourceException;
import jakarta.resource.cci.IndexedRecord;
import jakarta.resource.cci.Record;
#endif

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.rest.cci.ConsumerRecord;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.base.rest.cci.ResultRecord;
import org.openmdx.kernel.exception.BasicException;

class OutputRecords {

    @SuppressWarnings("unchecked")
    static void discloseEntry(Record target, ObjectRecord source) throws ResourceException {
	    if(target instanceof IndexedRecord) {
	        ((IndexedRecord)target).add(source);
	    } else if (target instanceof ConsumerRecord) {
	        ((ConsumerRecord)target).accept(source);
	    } else if (target != null) {
	        throw ResourceExceptions.toResourceException(
    	        new ServiceException(
    	            BasicException.Code.DEFAULT_DOMAIN,
    	            BasicException.Code.BAD_PARAMETER,
    	            "Unsuported target",
    	            new BasicException.Parameter("actual", target.getRecordName()),
                    new BasicException.Parameter(
                        "expected", 
                        ConsumerRecord.NAME, ResultRecord.NAME, Multiplicity.LIST.code()
                    )
                )
            );
	    }
	}

    static void discloseTotal(Record target, long total) {
        if (target instanceof ResultRecord) {
            final ResultRecord resultRecord = (ResultRecord) target;
            resultRecord.setTotal(total);
        }
    }

    static void discloseContinuation(Record target, boolean hasMore) {
        if (target instanceof ResultRecord) {
            final ResultRecord resultRecord = (ResultRecord) target;
            resultRecord.setHasMore(hasMore);
        }
    }

}