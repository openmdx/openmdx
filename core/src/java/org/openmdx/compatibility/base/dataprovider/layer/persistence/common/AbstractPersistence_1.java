/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractPersistence_1.java,v 1.17 2009/03/02 13:38:15 wfro Exp $
 * Description: Abstract persistence layer
 * Revision:    $Revision: 1.17 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/02 13:38:15 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2006, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.compatibility.base.dataprovider.layer.persistence.common;

import java.io.File;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.dataprovider.cci.SharedConfigurationEntries;
import org.openmdx.application.dataprovider.spi.Layer_1_0;
import org.openmdx.application.dataprovider.spi.OperationAwareLayer_1;
import org.openmdx.base.exception.ServiceException;

/**
 * Database_1 implements a OO-to-Relational mapping and makes DataproviderObjects
 * persistent. Any JDBC-compliant data store can be used.
 * 
 * You can use the java.io.PrintStream.DriverManager.setLogStream() method to
 * log JDBC calls. This method sets the logging/tracing PrintStream used by
 * the DriverManager and all drivers.
 * Insert the following line at the location in your code where you want to
 * start logging JDBC calls: DriverManager.setLogStream(System.out);
 */

abstract public class AbstractPersistence_1
extends OperationAwareLayer_1 
{

    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0#activate(short, org.openmdx.compatibility.base.application.configuration.Configuration, org.openmdx.compatibility.base.dataprovider.spi.Layer_1_0)
     */
    public void activate(
        short id, 
        Configuration configuration,
        Layer_1_0 delegation
    ) throws ServiceException {
        super.activate(
            id, 
            configuration, 
            delegation
        );
        this.chunkSize = getConfigurationValue(
            CommonConfigurationEntries.CHUNNK_SIZE,
            DEFAULT_CHUNK_SIZE
        );
        {
            String value = configuration.getFirstValue(
                CommonConfigurationEntries.STREAM_BUFFER_DIRECTORY
            );
            String pathname = value == null ? null : value.toString().trim();
            this.streamBufferDirectory = pathname == null || pathname.length() == 0 ? 
                null :
                    new File(pathname);
        }
        this.sequenceSupported = configuration.isOn(
            SharedConfigurationEntries.SEQUENCE_SUPPORTED
        );
    }

    /**
     * Defines the large objects' default buffer size
     */
    protected static final int DEFAULT_CHUNK_SIZE = 10000;

    /**
     * The chunk size defines the large objects' buffer size
     */
    private int chunkSize;

    /**
     * 
     */
    private File streamBufferDirectory;

    /**
     * Remembers whether sequences should be supported.
     */
    private boolean sequenceSupported;

    /**
     * Tells whether this peristence plug-in should suuport sequences
     * 
     * @return true if sequences should be supported
     */
    protected boolean isSequenceSupported(
    ){
        return this.sequenceSupported;
    }

    /**
     * Retrieve chunkSize.
     *
     * @return Returns the chunkSize.
     */
    protected int getChunkSize() {
        return this.chunkSize;
    }


    /**
     * Retrieve streamBufferDirectory.
     *
     * @return Returns the streamBufferDirectory.
     */
    protected File getStreamBufferDirectory() {
        return this.streamBufferDirectory;
    }

}