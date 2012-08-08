/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: StreamSynchronization_1.java,v 1.8 2008/09/10 08:55:30 hburger Exp $
 * Description: Synchronization_1 
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/09/10 08:55:30 $
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

package org.openmdx.compatibility.base.dataprovider.transport.rmi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.stream.cci.Sink_1_0;
import org.openmdx.base.stream.cci.Source_1_0;
import org.openmdx.base.stream.rmi.cci.BinarySink_1_0;
import org.openmdx.base.stream.rmi.cci.BinarySource_1_0;
import org.openmdx.base.stream.rmi.cci.CharacterSink_1_0;
import org.openmdx.base.stream.rmi.cci.CharacterSource_1_0;
import org.openmdx.base.stream.rmi.spi.StreamSynchronization_1_0;
import org.openmdx.base.stream.rmi.spi.StreamSynchronization_1_1;
import org.openmdx.kernel.exception.BasicException;

/**
 * Synchronization_1
 */
@SuppressWarnings("unchecked")
class StreamSynchronization_1 
implements StreamSynchronization_1_1 
{

    /**
     * Constructor 
     * <p>
     * The default temporary-file directory used when streamBufferDirectory is
     * <code>null</code> is specified by the system property 
     * <code>java.io.tmpdir</code>. On UNIX systems the default value of this 
     * property is typically <code>"/tmp"</code> or <code>"/var/tmp"</code>; 
     * on Microsoft Windows systems it is typically <code>"c:\\temp"</code>. A 
     * different value may be given to this system property when the Java 
     * virtual machine is invoked, but programmatic changes to this property 
     * are not guaranteed to have any effect upon the the temporary directory 
     * used by this method.
     * @param streamBufferDirectory the directory used to buffer input streams
     * in case of transaction boundaries; the default temporary-file directory
     * is used in case of <code>null</code>.
     * 
     * @param the chunk size
     */
    StreamSynchronization_1(
        String unitOfWorkId, 
        File streamBufferDirectory, 
        int streamChunkSize
    ){
        this.unitOfWorkId = unitOfWorkId;
        this.streamBufferDirectory = streamBufferDirectory;
        this.chunkSize = streamChunkSize;
    }

    final String unitOfWorkId;
    final File streamBufferDirectory;
    protected final int chunkSize;    
    private final List entries = new ArrayList();
    byte[] binaryChunk = null;
    char[] characterChunk = null;
    private final static String ENCODING = "UTF-16";

    //------------------------------------------------------------------------
    // Implements Synchronization_1_1
    //------------------------------------------------------------------------

    /**
     * Create a source entry
     * <p>
     * The source will be copied to the cache before the unit of work starts.
     * 
     * @param nonTransactionalSource
     * 
     * @throws IOException 
     */
    public SourceEntry add(
        InputStream nonTransactionalSource
    ) throws IOException {
        SourceEntry_1 entry = new SourceEntry_1(
            nonTransactionalSource
        );
        this.entries.add(entry);
        return entry;
    }

    /**
     * Create a source entry
     * <p>
     * The source will be copied to the cache before the unit of work starts.
     * 
     * @param nonTransactionalSource
     * 
     * @throws IOException 
     */
    public SourceEntry add(
        Reader nonTransactionalSource
    ) throws IOException {
        SourceEntry_1 entry = new SourceEntry_1(
            nonTransactionalSource
        );
        this.entries.add(entry);
        return entry;
    }

    /**
     * Create a sink entry
     * <p>
     * The sink will be flushed after the unit of work's commitment.
     * 
     * @param nonTransactionalSink
     * 
     * @throws IOException 
     */
    public SinkEntry add(
        OutputStream nonTransactionalSink
    ) throws IOException {
        SinkEntry_1 entry = new SinkEntry_1(
            nonTransactionalSink
        );
        this.entries.add(entry);
        return entry;
    }

    /**
     * Create a sink entry
     * <p>
     * The sink will be flushed after the unit of work's commitment.
     * 
     * @param nonTransactionalSink
     * 
     * @throws IOException 
     */
    public SinkEntry add(
        Writer nonTransactionalSink
    ) throws IOException {
        SinkEntry_1 entry = new SinkEntry_1(
            nonTransactionalSink
        );
        this.entries.add(entry);
        return entry;
    }

    //------------------------------------------------------------------------
    // Implements Synchronization_1_0
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.openmdx.base.transaction.Synchronization_1_0#afterCompletion(boolean)
     */
    public void afterCompletion(
        boolean committed
    ) throws ServiceException {
        Iterator i = this.entries.iterator();
        try {
            while(i.hasNext()) ((Entry)i.next()).afterCompletion(committed); 
        } finally {
            while(i.hasNext()) ((Entry)i.next()).afterCompletion(false); 
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.transaction.Synchronization_1_0#afterBegin()
     */
    public void afterBegin(
    ) throws ServiceException {
        //
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.transaction.Synchronization_1_0#beforeCompletion()
     */
    public void beforeCompletion(
    ) throws ServiceException {
        //
    }


    //------------------------------------------------------------------------
    // Implements StreamSynchronization_1_0
    //------------------------------------------------------------------------

    /**
     * Add a source entry
     */
    public StreamSynchronization_1_0.SourceEntry add(
        Source_1_0 nonTransactionalSource
    ) throws IOException{
        SourceEntry_1 entry = new SourceEntry_1(
            nonTransactionalSource
        );
        this.entries.add(entry);
        return entry;
    }

    /**
     * Add a sink entry
     */
    public StreamSynchronization_1_0.SinkEntry add(
        Sink_1_0 nonTransactionalSink
    ) throws IOException{
        SinkEntry_1 entry = new SinkEntry_1(
            nonTransactionalSink
        );
        this.entries.add(entry);
        return entry;
    }


    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.unitOfWorkId;
    }


    //------------------------------------------------------------------------
    // Class Entry
    //------------------------------------------------------------------------

    private class Entry {

        protected Entry (
        ) throws IOException {
            this.file = File.createTempFile(
                unitOfWorkId,
                null,
                streamBufferDirectory
            );
        }

        protected final File file;

        public void afterCompletion(
            boolean committed
        ) throws ServiceException {
            if(!this.file.delete()) new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.DEACTIVATION_FAILURE,
                "Could not delete stream buffer file",
                new BasicException.Parameter("file", this.file.toString())
            ).log();
        }

    }

    //------------------------------------------------------------------------
    // Class SourceEntry
    //------------------------------------------------------------------------

    protected final class SourceEntry_1 
    extends Entry
    implements StreamSynchronization_1_0.SourceEntry
    {

        SourceEntry_1(
            Source_1_0 nonTransactionalSource
        ) throws IOException{
            this.length = nonTransactionalSource instanceof BinarySource_1_0 ?
                cache((BinarySource_1_0)nonTransactionalSource) :
                    cache((CharacterSource_1_0)nonTransactionalSource);
        }

        SourceEntry_1(
            InputStream nonTransactionalSource
        ) throws IOException{
            this.length = cache(nonTransactionalSource);
        }

        SourceEntry_1(
            Reader nonTransactionalSource
        ) throws IOException{
            this.length = cache(nonTransactionalSource);
        }

        private Object transactionalSource;
        private final long length;

        public InputStream getBinaryStream(
        ) throws IOException {
            InputStream stream = new FileInputStream(super.file);
            this.transactionalSource = stream;
            return stream;
        }

        public Reader getCharacterStream(
        ) throws IOException {
            Reader stream = new InputStreamReader(
                new FileInputStream(super.file),
                ENCODING
            );
            this.transactionalSource = stream;
            return stream;
        }

        /**
         * Set transactionalSource.
         * 
         * @param transactionalSource The transactionalSource to set.
         */
        public void setTransactionalSource(
            Source_1_0 transactionalSource
        ) {
            this.transactionalSource = transactionalSource;
        }

        /**
         * Retrieve length.
         *
         * @return Returns the length.
         */
        public long getLength() {
            return this.length;
        }        

        public void afterCompletion(
            boolean committed
        ) throws ServiceException {      
            if(this.transactionalSource != null) try {
                if(this.transactionalSource instanceof Source_1_0) {
                    ((Source_1_0)this.transactionalSource).close();
                } else if (this.transactionalSource instanceof InputStream) {
                    ((InputStream)this.transactionalSource).close();
                } else if (this.transactionalSource instanceof Reader) {
                    ((Reader)this.transactionalSource).close();                        
                }
            } catch (IOException cause) {
                new ServiceException(
                    cause,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.COMMUNICATION_FAILURE,
                    "Could not close buffered input stream",
                    new BasicException.Parameter("file", file.toString())
                ).log();
            }
            this.transactionalSource = null;
            super.afterCompletion(committed);
        }

        private long cache(
            BinarySource_1_0 source
        ) throws IOException {
            OutputStream target = new FileOutputStream(file);
            long length = 0L;
            try {
                for(
                        byte[] chunk = source.readBytes(chunkSize);
                        chunk != null;
                        chunk = source.readBytes(chunkSize)
                ) {
                    length += chunk.length;
                    target.write(chunk);
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
            return length;
        }

        private long cache(
            CharacterSource_1_0 source
        ) throws IOException {            
            Writer target = new OutputStreamWriter(
                new FileOutputStream(file),
                ENCODING
            );
            long length = 0L;
            try {
                for(
                        char[] chunk = source.readCharacters(chunkSize);
                        chunk != null;
                        chunk = source.readCharacters(chunkSize)
                ){
                    length += chunk.length;
                    target.write(chunk);
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
            return length;
        }

        private long cache(
            InputStream source
        ) throws IOException {            
            OutputStream target = new FileOutputStream(file);
            long length = 0L;
            if(binaryChunk == null) binaryChunk = new byte[chunkSize];
            try {
                for(
                        int i = source.read(binaryChunk);
                        i >= 0;
                        i = source.read(binaryChunk)
                ){
                    length += i;
                    target.write(binaryChunk, 0, i);
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
            return length;
        }

        private long cache(
            Reader source
        ) throws IOException {            
            Writer target = new OutputStreamWriter(
                new FileOutputStream(file),
                ENCODING
            );
            long length = 0L;
            if(characterChunk == null) characterChunk = new char[chunkSize];
            try {
                for(
                        int i = source.read(characterChunk);
                        i >= 0;
                        i = source.read(characterChunk)
                ){
                    length += i;
                    target.write(characterChunk, 0, i);
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
            return length;
        }

    }


    //------------------------------------------------------------------------
    // Class SinkEntry
    //------------------------------------------------------------------------

    protected final class SinkEntry_1 
    extends Entry 
    implements StreamSynchronization_1_0.SinkEntry
    {

        SinkEntry_1(
            Sink_1_0 nonTransactionalSink
        ) throws IOException{
            this.nonTransactionalSink = nonTransactionalSink;
        }

        SinkEntry_1(
            OutputStream nonTransactionalSink
        ) throws IOException{
            super();
            this.nonTransactionalSink = nonTransactionalSink;
        }

        SinkEntry_1(
            Writer nonTransactionalSink
        ) throws IOException{
            super();
            this.nonTransactionalSink = nonTransactionalSink;
        }

        private Object transactionalSink;
        private Object nonTransactionalSink;

        public OutputStream getBinaryStream(
        ) throws IOException {
            OutputStream stream = new FileOutputStream(super.file); 
            this.transactionalSink = stream;
            return stream;
        }

        public Writer getCharacterStream(
        ) throws IOException {
            Writer stream = new OutputStreamWriter(
                new FileOutputStream(super.file), 
                ENCODING
            ); 
            this.transactionalSink = stream;
            return stream;
        }

        /**
         * Set transactionalSink.
         * 
         * @param transactionalSink The transactionalSink to set.
         */
        public void setTransactionalSink(Sink_1_0 transactionalSink) {
            this.transactionalSink = transactionalSink;
        }

        public void afterCompletion(
            boolean committed
        ) throws ServiceException {
            try {
                if(this.transactionalSink != null) try {
                    if(this.transactionalSink instanceof Sink_1_0) {
                        ((Sink_1_0)transactionalSink).close();
                    } else if (this.transactionalSink instanceof OutputStream) {
                        ((OutputStream)this.transactionalSink).close();
                    } else if (this.transactionalSink instanceof Writer) {
                        ((Writer)this.transactionalSink).close();                        
                    }
                } catch (IOException cause) {
                    ServiceException exception = new ServiceException(
                        cause,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.COMMUNICATION_FAILURE,
                        "Could not prepare buffered output stream",
                        new BasicException.Parameter("file", this.file.toString())
                    ).log();
                    if(committed) throw exception;
                }
                if(committed) try {
                    if(nonTransactionalSink instanceof BinarySink_1_0) {
                        flush((BinarySink_1_0)this.nonTransactionalSink);
                    } else if (nonTransactionalSink instanceof CharacterSink_1_0){
                        flush((CharacterSink_1_0)this.nonTransactionalSink);
                    } else if (nonTransactionalSink instanceof OutputStream) {
                        flush((OutputStream)nonTransactionalSink);
                    } else if (nonTransactionalSink instanceof Writer) {
                        flush((Writer)nonTransactionalSink);
                    }
                } catch (IOException exception) {
                    throw new ServiceException(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.COMMUNICATION_FAILURE,
                        "Could not flush buffered output stream",
                        new BasicException.Parameter("file", this.file.toString())
                    );
                }
            } finally {
                this.transactionalSink = null;
                this.nonTransactionalSink = null;
                super.afterCompletion(committed);
            }
        }

        private void flush(
            BinarySink_1_0 target
        ) throws IOException {
            if(binaryChunk == null) binaryChunk = new byte[chunkSize];
            InputStream source = new FileInputStream(file);
            try {
                for(
                        int i = source.read(binaryChunk);
                        i >= 0;
                        i = source.read(binaryChunk)
                ){
                    if(i == 0) continue;
                    byte[] chunk = i == binaryChunk.length ? binaryChunk : new byte[i];
                    if(chunk != binaryChunk) System.arraycopy(binaryChunk, 0, chunk, 0, i);
                    target.writeBytes(chunk);
                }
                target.close();
            } finally {
                try {
                    source.close();                    
                } catch (IOException exception) {
                    // ignore
                }
            }
        }

        private void flush(
            CharacterSink_1_0 target
        ) throws IOException {
            if(characterChunk == null) characterChunk = new char[chunkSize];
            Reader source = new InputStreamReader(
                new FileInputStream(file),
                ENCODING
            );
            try {
                for(
                        int i = source.read(characterChunk);
                        i >= 0;
                        i = source.read(characterChunk)
                ){
                    if(i == 0) continue;
                    char[] chunk = i == characterChunk.length ? characterChunk : new char[i];
                    if(chunk != characterChunk) System.arraycopy(characterChunk, 0, chunk, 0, i);
                    target.writeCharacters(chunk);
                }
                target.close();
            } finally {
                try {
                    source.close();                    
                } catch (IOException exception) {
                    // ignore
                }
            }
        }

        private void flush(
            OutputStream target
        ) throws IOException {
            if(binaryChunk == null) binaryChunk = new byte[chunkSize];
            InputStream source = new FileInputStream(file);
            try {
                for(
                        int i = source.read(binaryChunk);
                        i >= 0;
                        i = source.read(binaryChunk)
                ) if(
                        i != 0
                ) target.write(binaryChunk, 0, i);
                target.flush();
                target.close();
            } finally {
                try {
                    source.close();                    
                } catch (IOException exception) {
                    // ignore
                }
            }
        }

        private void flush(
            Writer target
        ) throws IOException {
            if(characterChunk == null) characterChunk = new char[chunkSize];
            Reader source = new InputStreamReader(
                new FileInputStream(file),
                ENCODING
            );
            try {
                for(
                        int i = source.read(characterChunk);
                        i >= 0;
                        i = source.read(characterChunk)
                ) if (
                        i != 0
                ) target.write(characterChunk, 0, i);
                target.flush();
                target.close();
            } finally {
                try {
                    source.close();                    
                } catch (IOException exception) {
                    // ignore
                }
            }
        }

    }

}
