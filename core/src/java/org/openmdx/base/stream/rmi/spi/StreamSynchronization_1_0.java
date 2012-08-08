/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: StreamSynchronization_1_0.java,v 1.1 2005/10/09 10:02:23 hburger Exp $
 * Description: Stream Synchronization Interface 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/10/09 10:02:23 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2005, OMEX AG, Switzerland
 * All rights reserved.
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

package org.openmdx.base.stream.rmi.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.openmdx.base.stream.cci.Sink_1_0;
import org.openmdx.base.stream.cci.Source_1_0;
import org.openmdx.base.transaction.Synchronization_1_0;

/**
 * Stream Synchronization Interface
 */
public interface StreamSynchronization_1_0 
    extends Synchronization_1_0 
{

    /**
     * Create a source entry
     * <p>
     * The source will be copied to the cache before the unit of work starts.
     * 
     * @param nonTransactionalSource
     * 
     * @throws IOException 
     */
    SourceEntry add(
        Source_1_0 nonTransactionalSource
    ) throws IOException;

    /**
     * Create a sink entry
     * <p>
     * The sink will be flushed after the unit of work's commitment.
     * 
     * @param nonTransactionalSink
     * 
     * @throws IOException 
     */
    SinkEntry add(
        Sink_1_0 nonTransactionalSink
    ) throws IOException;

    /**
     * Source Entry
     */
    interface SourceEntry {

        /**
         * To retrieve a binary source's <code>InputStream</code>.
         * 
         * @return the entry's binary stream
         * @throws IOException
         */
        InputStream getBinaryStream(
        ) throws IOException;
        
        /**
         * To retrieve a character source's <code>Reader</code>.
         * 
         * @return the entry's character stream
         * @throws IOException
         */
        Reader getCharacterStream(
        ) throws IOException;

        /**
         * Set transactionalSource.
         * 
         * @param transactionalSource The transactionalSource to set.
         */
        void setTransactionalSource(
            Source_1_0 transactionalSource
        );
        
        /**
         * Retrieve length.
         *
         * @return Returns the length.
         */
        long getLength();
        
        
    }

    /**
     * Sink Entry
     */
    interface SinkEntry {

        /**
         * To retrieve a binary sink's <code>OutputStream</code>.
         * 
         * @return the entry's binary stream
         * @throws IOException
         */
        OutputStream getBinaryStream(
        ) throws IOException;
        
        /**
         * To retrieve a character sink's <code>Writer</code>.
         * 
         * @return the entry's character stream
         * @throws IOException
         */
        Writer getCharacterStream(
        ) throws IOException;

        /**
         * Set transactional sink.
         * 
         * @param transactionalSink The transactionalSink to set.
         */
        void setTransactionalSink(
            Sink_1_0 transactionalSink
        );
        
    }
    
}
