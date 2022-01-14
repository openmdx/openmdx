/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Target 
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

import java.util.HashSet;
import java.util.Set;

import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Database_2_0;
import org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Target;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.cci.ObjectRecord;
import org.openmdx.kernel.exception.BasicException;

/**
 * Target
 */
abstract class AbstractTarget implements Target {

    /**
     * Constructor 
     */
    protected AbstractTarget(
        Database_2_0 completer
    ) {
        this.completer = completer;
    }
    
    private final Database_2_0 completer;
    private final Set<Path> xris = new HashSet<Path>();
    
    @Override
    public int count(){
        return xris.size();
    }
    
    @Override
    public final void accept(ObjectRecord object) throws ServiceException {
        final Path xri = object.getResourceIdentifier();
        if(!xris.add(xri)) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.MEDIA_ACCESS_FAILURE,
                "Result contains duplicates",
                new BasicException.Parameter(BasicException.Parameter.XRI, xri));
        }
        if(completer != null) {
            completer.completeObject(object);
        }
        propagate(object);
    }

    protected void close(){
        xris.clear();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.spi.Target#getObjectBatchSize(int)
     */
    @Override
    public int getObjectBatchSize(
    ) {
        return this.completer.getObjectBatchSize();
    }
    
    protected abstract void propagate(ObjectRecord object) throws ServiceException;
            
}
