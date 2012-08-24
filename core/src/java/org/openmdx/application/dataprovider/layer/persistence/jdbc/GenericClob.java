/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: Generic Character Large Object 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
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
package org.openmdx.application.dataprovider.layer.persistence.jdbc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Collection;

import org.openmdx.kernel.log.SysLog;

/**
 * Generic Character Large Object 
 */
@SuppressWarnings({"rawtypes","unchecked"})
class GenericClob
    implements Clob {

    GenericClob(
        Reader source,
        long length, 
        Collection temporaryFiles, 
        File streamBufferDirectory, 
        String unitOfWorkId, 
        int chunkSize
    ) throws IOException{
        if(length < 0) {
            File file = File.createTempFile(
                unitOfWorkId,
                null,
                streamBufferDirectory
            );
            temporaryFiles.add(file);
            char[] chunk = new char[chunkSize];
            Writer target = new OutputStreamWriter(
                new FileOutputStream(file),
                ENCODING
            );
            this.length = 0L;
            try {
                for(
                    int i = source.read(chunk);
                    i >= 0;
                    i = source.read(chunk)
                ){
                    this.length += i;
                    target.write(chunk, 0, i);
                }
                source.close();
                target.flush();
                target.close();
            } catch (IOException exception) {
                try {
                    target.close();
                } catch (IOException ignore) {
                    // ignore
                }
                throw exception;
            }
            this.source = new InputStreamReader(
                new FileInputStream(file),
                ENCODING
            );
            SysLog.detail("Length of buffered CLOB", new Long(this.length));
        } else {
            this.source = source;
            this.length = length;
            SysLog.detail("Length of passthrough CLOB", new Long(this.length));
        }
    }
    
    /**
     * 
     */
    private final static String ENCODING = "UTF-16";

    /**
     * 
     */
    private Reader source;
    
    /**
     * 
     */
    private long length;

    /* (non-Javadoc)
     * @see java.sql.Clob#getAsciiStream()
     */
    public InputStream getAsciiStream()
        throws SQLException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.sql.Clob#getCharacterStream()
     */
    public Reader getCharacterStream()
        throws SQLException {
        return this.source;
    }

    /* (non-Javadoc)
     * @see java.sql.Clob#getSubString(long, int)
     */
    public String getSubString(long pos, int length)
        throws SQLException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.sql.Clob#length()
     */
    public long length()
        throws SQLException {
        return this.length;
    }

    /* (non-Javadoc)
     * @see java.sql.Clob#position(java.sql.Clob, long)
     */
    public long position(Clob searchstr, long start)
        throws SQLException {
        throw new UnsupportedOperationException();
    }

    
    //------------------------------------------------------------------------
    // Since JRE 1.4
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.sql.Clob#position(java.lang.String, long)
     */
    public long position(
        String searchstr, 
        long start
    ) throws SQLException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.sql.Clob#setAsciiStream(long)
     */
    public OutputStream setAsciiStream(
        long pos
    ) throws SQLException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.sql.Clob#setCharacterStream(long)
     */
    public Writer setCharacterStream(
        long pos
    ) throws SQLException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.sql.Clob#setString(long, java.lang.String, int, int)
     */
    public int setString(
        long pos, 
        String str, 
        int offset, 
        int len
    ) throws SQLException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.sql.Clob#setString(long, java.lang.String)
     */
    public int setString(
        long pos, 
        String str
    ) throws SQLException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see java.sql.Clob#truncate(long)
     */
    public void truncate(
        long len
    ) throws SQLException {
        if(len > this.length) this.length = len;
    }

    
    //------------------------------------------------------------------------
    // Since JRE 6
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.sql.Clob#free()
     */
    public void free(
    ) throws SQLException {
        this.source = null;
    }

    /* (non-Javadoc)
     * @see java.sql.Clob#getCharacterStream(long, long)
     */
    public Reader getCharacterStream(
        long pos, 
        long length
    ) throws SQLException {
        // Would not be efficient if it were supported at all...
        return new StringReader(getSubString(pos, (int) length));
    }

}
