/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.openmdx.base.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Adapts a {@code Reader} as an {@code InputStream}.
 * Adapted from {@code StringInputStream}.
 *
 */
public class ReaderInputStream extends InputStream {
    private static final int BYTE_MASK = 0xFF;

    /** Source Reader */
    private Reader in;

    private String encoding = System.getProperty("file.encoding");

    private byte[] slack;

    private int begin;

    /**
     * Construct a {@code ReaderInputStream}
     * for the specified {@code Reader}.
     *
     * @param reader   {@code Reader}.  Must not be {@code null}.
     */
    public ReaderInputStream(Reader reader) {
        in = reader;
    }

    /**
     * Construct a {@code ReaderInputStream}
     * for the specified {@code Reader},
     * with the specified encoding.
     *
     * @param reader     non-null {@code Reader}.
     * @param encoding   non-null {@code String} encoding.
     */
    public ReaderInputStream(Reader reader, String encoding) {
        this(reader);
        if (encoding == null) {
            throw new IllegalArgumentException("encoding must not be null");
        } else {
            this.encoding = encoding;
        }
    }

    /**
     * Reads from the {@code Reader}, returning the same value.
     *
     * @return the value of the next character in the {@code Reader}.
     *
     * @exception IOException if the original {@code Reader} fails to be read
     */
    @Override
    public synchronized int read() throws IOException {
        if (in == null) {
            throw new IOException("Stream Closed");
        }

        byte result;
        if (slack != null && begin < slack.length) {
            result = slack[begin];
            if (++begin == slack.length) {
                slack = null;
            }
        } else {
            byte[] buf = new byte[1];
            if (read(buf, 0, 1) <= 0) {
                return -1;
            } else {
                result = buf[0];
            }
        }
        return result & BYTE_MASK;
    }

    /**
     * Reads from the {@code Reader} into a byte array
     *
     * @param b  the byte array to read into
     * @param off the offset in the byte array
     * @param len the length in the byte array to fill
     * @return the actual number read into the byte array, -1 at
     *         the end of the stream
     * @exception IOException if an error occurs
     */
    @Override
    public synchronized int read(
        byte[] b, 
        int off, 
        int len
    ) throws IOException {
        int length = len;
        if (in == null) {
            throw new IOException("Stream Closed");
        }
        if (length == 0) {
            return 0;
        }
        while (slack == null) {
            char[] buf = new char[length]; // might read too much
            int n = in.read(buf);
            if (n == -1) {
                return -1;
            }
            if (n > 0) {
                slack = new String(buf, 0, n).getBytes(encoding);
                begin = 0;
            }
        }

        if (length > slack.length - begin) {
            length = slack.length - begin;
        }

        System.arraycopy(slack, begin, b, off, length);

        if ((begin += length) >= slack.length) {
            slack = null;
        }

        return length;
    }

    /**
     * Marks the read limit of the Reader.
     *
     * @param limit the maximum limit of bytes that can be read before the
     *              mark position becomes invalid
     */
    @Override
    public synchronized void mark(final int limit) {
        try {
            in.mark(limit);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe.getMessage());
        }
    }


    /**
     * @return   the current number of bytes ready for reading
     * @exception IOException if an error occurs
     */
    @Override
    public synchronized int available() throws IOException {
        if (in == null) {
            throw new IOException("Stream Closed");
        }
        if (slack != null) {
            return slack.length - begin;
        }
        if (in.ready()) {
            return 1;
        }
        return 0;
    }

    /**
     * @return false - mark is not supported
     */
    @Override
    public boolean markSupported () {
        return false;   // would be imprecise
    }

    /**
     * Resets the Reader.
     *
     * @exception IOException if the Reader fails to be reset
     */
    @Override
    public synchronized void reset() throws IOException {
        if (in == null) {
            throw new IOException("Stream Closed");
        }
        slack = null;
        in.reset();
    }

    /**
     * Closes the Reader.
     *
     * @exception IOException if the original Reader fails to be closed
     */
    @Override
    public synchronized void close() throws IOException {
        if (in != null) {
            in.close();
            slack = null;
            in = null;
        }
    }
}
