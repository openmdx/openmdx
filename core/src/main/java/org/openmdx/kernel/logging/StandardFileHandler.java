/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Direct File Handler 
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
package org.openmdx.kernel.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;


/**
 * Simple file logging <tt>Handler</tt>.
 * <p>
 * The <tt>StreamingFileHandler</tt> can either write to a specified file,
 * or it can write to a rotating set of files.  
 * <p>
 * For a rotating set of files, as each file reaches a given size
 * limit, it is closed, rotated out, and a new file opened.
 * Successively older files are named by adding "0", "1", "2", 
 * etc into the base filename.
 * <p>
 * By default buffering is enabled in the IO libraries but each log
 * record is flushed out when it is complete. 
 * <p>
 * The <code>FallbackFileHandler</code> is able to support <code>Formattter</code>s 
 * implementing <code>FormatWithouThrown</code>.
 * <p>
 * <b>Configuration:</b>
 * By default each <tt>FileHandler</tt> is initialized using the following
 * <tt>LogManager</tt> configuration properties.  If properties are not defined
 * (or have invalid values) then the specified default values are used.
 * <ul>
 * <li>   java.util.logging.FileHandler.level 
 *    specifies the default level for the <tt>Handler</tt>
 *    (defaults to <tt>Level.ALL</tt>).
 * <li>   java.util.logging.FileHandler.filter 
 *    specifies the name of a <tt>Filter</tt> class to use
 *    (defaults to no <tt>Filter</tt>).
 * <li>   java.util.logging.FileHandler.formatter 
 *    specifies the name of a <tt>Formatter</tt> class to use
 *        (defaults to <tt>java.util.logging.XMLFormatter</tt>)
 * <li>   java.util.logging.FileHandler.encoding 
 *    the name of the character set encoding to use (defaults to
 *    the default platform encoding).
 * <li>   java.util.logging.FileHandler.limit 
 *    specifies an approximate maximum amount to write (in bytes)
 *    to any one file.  If this is zero, then there is no limit.
 *    (Defaults to no limit).
 * <li>   java.util.logging.FileHandler.count 
 *    specifies how many output files to cycle through (defaults to 1).
 * <li>   java.util.logging.FileHandler.pattern 
 *    specifies a pattern for generating the output file name.  See
 *        below for details. (Defaults to "%h/java%u.log").
 * <li>   java.util.logging.FileHandler.append
 *    specifies whether the FileHandler should append onto
 *        any existing files (defaults to false).
 * </ul>
 * <p>
 * <p>
 * A pattern consists of a string that includes the following special
 * components that will be replaced at runtime:
 * <ul>
 * <li>    "/"    the local pathname separator 
 * <li>     "%t"   the system temporary directory
 * <li>     "%h"   the value of the "user.home" system property
 * <li>     "%g"   the generation number to distinguish rotated logs
 * <li>     "%u"   a unique number to resolve conflicts
 * <li>     "%%"   translates to a single percent sign "%"
 * </ul>
 * If no "%g" field has been specified and the file count is greater
 * than one, then the generation number will be added to the end of
 * the generated filename, after a dot.
 * <p> 
 * Thus for example a pattern of "%t/java%g.log" with a count of 2
 * would typically cause log files to be written on Solaris to 
 * /var/tmp/java0.log and /var/tmp/java1.log whereas on Windows 95 they
 * would be typically written to C:\TEMP\java0.log and C:\TEMP\java1.log
 * <p> 
 * Generation numbers follow the sequence 0, 1, 2, etc.
 * <p>
 * Normally the "%u" unique field is set to 0.  However, if the <tt>FileHandler</tt>
 * tries to open the filename and finds the file is currently in use by
 * another process it will increment the unique number field and try
 * again.  This will be repeated until <tt>FileHandler</tt> finds a file name that
 * is  not currently in use. If there is a conflict and no "%u" field has
 * been specified, it will be added at the end of the filename after a dot.
 * (This will be after any automatically added generation number.)
 * <p>
 * Thus if three processes were all trying to log to fred%u.%g.txt then 
 * they  might end up using fred0.0.txt, fred1.0.txt, fred2.0.txt as
 * the first file in their rotating sequences.
 * <p>
 * Note that the use of unique ids to avoid conflicts is only guaranteed
 * to work reliably when using a local disk file system.
 */
public class StandardFileHandler extends FileHandler {

    /**
     * Constructor 
     *
     * @throws IOException
     * @throws SecurityException
     */
    public StandardFileHandler(
    ) throws IOException, SecurityException {
        super();
    }

    /**
     * Constructor 
     *
     * @param pattern
     * @param append
     * @throws IOException
     * @throws SecurityException
     */
    public StandardFileHandler(
        String pattern, 
        boolean append
    ) throws IOException, SecurityException {
        super(pattern, append);
    }

    /**
     * Constructor 
     *
     * @param pattern
     * @param limit
     * @param count
     * @param append
     * @throws IOException
     * @throws SecurityException
     */
    public StandardFileHandler(
        String pattern,
        int limit,
        int count,
        boolean append
    ) throws IOException, SecurityException {
        super(pattern, limit, count, append);
    }

    /**
     * Constructor 
     *
     * @param pattern
     * @param limit
     * @param count
     * @throws IOException
     * @throws SecurityException
     */
    public StandardFileHandler(
        String pattern, 
        int limit, 
        int count
    ) throws IOException, SecurityException {
        super(pattern, limit, count);
    }

    /**
     * Constructor 
     *
     * @param pattern
     * @throws IOException
     * @throws SecurityException
     */
    public StandardFileHandler(
        String pattern
    ) throws IOException, SecurityException {
        super(pattern);
    }

    /**
     * 
     */
    private OutputStream outputStream;
    
    /**
     * 
     */
    private PrintWriter writer;

    /**
     * Change the output stream.
     * <P>
     * If there is a current output stream then the <tt>Formatter</tt>'s 
     * tail string is written and the stream is flushed and closed.
     * Then the output stream is replaced with the new output stream.
     *
     * @param out   New output stream.  May not be null.
     * @exception  SecurityException  if a security manager exists and if
     *             the caller does not have <tt>LoggingPermission("control")</tt>.
     */
    @Override
    protected synchronized void setOutputStream(
        OutputStream out
    ) throws SecurityException {
        super.setOutputStream(this.outputStream = out);
        try {
            newWriter(getEncoding());
        } catch (UnsupportedEncodingException ex) {
            // This shouldn't happen.  The setEncoding method
            // should have validated that the encoding is OK.
            throw new Error("Unexpected exception",ex);
        }
    }

    /**
     * Set (or change) the character encoding used by this <tt>Handler</tt>.
     * <p>
     * The encoding should be set before any <tt>LogRecords</tt> are written
     * to the <tt>Handler</tt>.
     *
     * @param encoding  The name of a supported character encoding.
     *        May be null, to indicate the default platform encoding.
     * @exception  SecurityException  if a security manager exists and if
     *             the caller does not have <tt>LoggingPermission("control")</tt>.
     * @exception  UnsupportedEncodingException if the named encoding is
     *      not supported.
     */
    @Override
    public synchronized void setEncoding(
        String encoding
    ) throws SecurityException, java.io.UnsupportedEncodingException {
        super.setEncoding(encoding);
        newWriter(encoding);
    }

    /**
     * Associate a writer with the given stream
     * 
     * @param encoding
     * 
     * @throws UnsupportedEncodingException
     */
    private void newWriter(
        String encoding
    ) throws UnsupportedEncodingException{
        this.writer = encoding == null ? new PrintWriter(
            this.outputStream
        ) : new PrintWriter(
            new OutputStreamWriter(
                outputStream,
                encoding
            )
        );
    }

    /* (non-Javadoc)
     * @see java.util.logging.FileHandler#close()
     */
    @Override
    public synchronized void close(
    ) throws SecurityException {
        this.writer = null;
        super.close();
    }

    /* (non-Javadoc)
     * @see java.util.logging.FileHandler#publish(java.util.logging.LogRecord)
     */
    @Override
    public synchronized void publish(LogRecord record) {
        if(isLoggable(record)) {
            if(this.writer != null) {
                Throwable throwable = record.getThrown();
                if(throwable != null) {
                    Formatter formatter = getFormatter();
                    if(formatter instanceof AdaptiveFormatter) {
                        ((AdaptiveFormatter)formatter).setExcludeThrown(true);
                        super.publish(record);
                        throwable.printStackTrace(this.writer);
                        this.writer.flush();
                        return;
                    }
                }
            }
            super.publish(record);
        }
    }

}
