/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Abstract Formatter 
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.text.Format;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Abstract Formatter
 */
public abstract class AbstractFormatter    
    extends Formatter 
    implements AdaptiveFormatter
{

    /**
     * Constructor 
     */
    protected AbstractFormatter(
    ){
    }

    /**
     * The line separator cache
     */
    private final static String lineSeparator = getProperty("line.separator", "\n");
    
    /**
     * 
     */
    private String hostName = null; // lazily initialized
    
    /**
     * Exclude thrown must be enabled explicitly
     */
    private boolean excludeThrown = false;

    /**
     * Re-usable string builder
     */
    private final StringBuilder appendable = new StringBuilder();

    /**
     * Retrieve the line separator
     * 
     * @return the line separator
     */
    protected String getLineSeparator(){
        return lineSeparator;
    }
    
    /**
     * Retrieve the field separator
     * 
     * @return the field separator
     */
    protected String getFieldSeparator(){
        return "|";
    }
    
    /**
     * Lenient System Property retrieval
     * 
     * @param key
     * @param defaultValue
     * 
     * @return the system property value or its default value
     */
    protected static final String getProperty(
        String key,
        String defaultValue
    ){
        try {
            return System.getProperty(key, defaultValue);
        } catch (Exception exception) {
            return defaultValue;
        }
    }

    /**
     * Append a line separator
     */
    protected StringBuilder newLine(){
        return this.appendable.append(getLineSeparator());
    }

    /**
     * Append a field separator
     */
    protected StringBuilder newField(){
        return this.appendable.append(getFieldSeparator());
    }

    /**
     * Append a value
     * 
     * @param value
     * 
     * @return the appendable
     */
    protected StringBuilder append(
        CharSequence value
    ){
        return this.appendable.append(value);
    }

    /**
     * Retrieve the host name
     * 
     * @return the host name
     */
    protected void appendHostName(){
        if(this.hostName == null) {
            try {
                this.hostName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException exception) {
                this.hostName = "localhost";
            }
        }
        this.appendable.append(this.hostName);
    }

    /**
     * Format the time stamp in UTC
     * 
     * @param record the Log Record
     * 
     * @return the formatted time stamp
     */
    protected void appendTimestamp(
        LogRecord record
    ){
        final long timestamp = record.getMillis();
        this.appendable.append(
            Instant.ofEpochMilli(timestamp)
        );
    }

    /**
     * A class and its sub-classes compose the message without thrown
     * 
     * @param record
     * 
     * @return a StringBuilder containing the message
     */
    protected abstract void appendFields(
        LogRecord record
    );

    /**
     * A class and its sub-classes compose the message without thrown
     * 
     * @param record
     * 
     * @return a StringBuilder containing the message
     */
    protected void appendMessage(
        LogRecord record
    ){
        this.appendable.append(formatMessage(record));
    }

    /**
     * Append a log record's throwable
     * 
     * @param record
     */
    protected void appendThrown(
        LogRecord record
    ){
        if(this.excludeThrown) return;
        Throwable throwable = record.getThrown();
        if(throwable != null) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            throwable.printStackTrace(printWriter);
            printWriter.flush();
            newField().append(stringWriter);    
        }
    }
    
    /**
     * Allows format information to be included in the file header
     */
    protected void appendLogFormat(){
        // nothing to do
    }

    
    //------------------------------------------------------------------------
    // Extends Formatter
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
     */
    @Override
    public String format(
        LogRecord record
    ) {
        this.appendable.setLength(0);
        appendFields(record);
        appendThrown(record);
        newLine();
        return this.appendable.toString();
    }

    /* (non-Javadoc)
     * @see java.util.logging.Formatter#getHead(java.util.logging.Handler)
     */
    @Override
    public String getHead(Handler h) {
        this.appendable.setLength(0);
        appendLogFormat();
        return this.appendable.toString();
    }

    
    //------------------------------------------------------------------------
    // Implements AdaptiveFormatter
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.kernel.logging.AdaptiveFormatter#excludeThrown()
     */
    public void setExcludeThrown(
        boolean excludeThrown
    ) {
        this.excludeThrown = excludeThrown;
    }
        
}
