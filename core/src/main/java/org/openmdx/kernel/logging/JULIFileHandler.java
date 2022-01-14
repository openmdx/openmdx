/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: JULI File Handler
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

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;


/**
 * Implementation of <b>Handler</b> that appends log messages to a file
 * named {prefix}.{date}.{suffix} in a configured directory, with an
 * optional preceding timestamp.
 */
public class JULIFileHandler extends Handler {

    // ------------------------------------------------------------ Constructor

    
    public JULIFileHandler() {
        this(null, null, null);
    }
    
    
    public JULIFileHandler(String directory, String prefix, String suffix) {
        this.directory = directory;
        this.prefix = prefix;
        this.suffix = suffix;
        configure();
        open();
    }
    

    // ----------------------------------------------------- Instance Variables


    /**
     * The as-of date for the currently open log file, or a zero-length
     * string if there is no open log file.
     */
    private String date = "";


    /**
     * The directory in which log files are created.
     */
    private String directory = null;


    /**
     * The prefix that is added to log file filenames.
     */
    private String prefix = null;


    /**
     * The suffix that is added to log file filenames.
     */
    private String suffix = null;


    /**
     * The PrintWriter to which we are currently logging, if any.
     */
    private PrintWriter writer = null;


    // --------------------------------------------------------- Public Methods


    /**
     * Format and publish a <tt>LogRecord</tt>.
     *
     * @param  record  description of the log event
     */
    @Override
    public void publish(LogRecord record) {

        if (!isLoggable(record)) {
            return;
        }

        // Construct the timestamp we will use, if requested
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        String tsString = ts.toString().substring(0, 19);
        String tsDate = tsString.substring(0, 10);

        // If the date has changed, switch log files
        if (!date.equals(tsDate)) {
            synchronized (this) {
                if (!date.equals(tsDate)) {
                    close();
                    date = tsDate;
                    open();
                }
            }
        }

        String result = null;
        Throwable pending;
        try {
            Formatter formatter = getFormatter();
            if(formatter instanceof AdaptiveFormatter) {
                ((AdaptiveFormatter)formatter).setExcludeThrown(true);
                pending = record.getThrown();
            } else {
            	pending = null;
            }
            result = formatter.format(record);
        } catch (Exception e) {
            reportError(null, e, ErrorManager.FORMAT_FAILURE);
            return;
        }
        
        try {
            writer.write(result);
            if(pending != null) {
            	pending.printStackTrace(writer);
            }
            writer.flush();
        } catch (Exception e) {
            reportError(null, e, ErrorManager.WRITE_FAILURE);
            return;
        }
        

    
    }
    
/*
        if(isLoggable(record)) {
            if(this.writer != null) {
                        super.publish(record);
                        throwable.printStackTrace(this.writer);
                        this.writer.flush();
                        return;
                    }
                }
            }
            super.publish(record);
        }
 */
    // -------------------------------------------------------- Private Methods


    /**
     * Close the currently open log file (if any).
     */
    @Override
    public void close() {
        
        try {
            if (writer == null)
                return;
            writer.write(getFormatter().getTail(this));
            writer.flush();
            writer.close();
            writer = null;
            date = "";
        } catch (Exception e) {
            reportError(null, e, ErrorManager.CLOSE_FAILURE);
        }
        
    }


    /**
     * Flush the writer.
     */
    @Override
    public void flush() {

        try {
            writer.flush();
        } catch (Exception e) {
            reportError(null, e, ErrorManager.FLUSH_FAILURE);
        }
        
    }
    
    
    /**
     * Configure from <code>LogManager</code> properties.
     */
    private void configure() {

        Timestamp ts = new Timestamp(System.currentTimeMillis());
        String tsString = ts.toString().substring(0, 19);
        date = tsString.substring(0, 10);

        String className = JULIFileHandler.class.getName();
        
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        
        // Retrieve configuration of logging file name
        if (directory == null)
            directory = getProperty(className + ".directory", "logs");
        if (prefix == null)
            prefix = getProperty(className + ".prefix", "juli.");
        if (suffix == null)
            suffix = getProperty(className + ".suffix", ".log");

        // Get logging level for the handler
        setLevel(Level.parse(getProperty(className + ".level", "" + Level.ALL)));

        // Get filter configuration
        String filterName = getProperty(className + ".filter", null);
        if (filterName != null) {
            try {
                setFilter((Filter) cl.loadClass(filterName).newInstance());
            } catch (Exception e) {
                // Ignore
            }
        }

        // Set formatter
        String formatterName = getProperty(className + ".formatter", null);
        if (formatterName != null) {
            try {
                setFormatter((Formatter) cl.loadClass(formatterName).newInstance());
            } catch (Exception e) {
                // Ignore
            }
        } else {
            setFormatter(new SimpleFormatter());
        }
        
        // Set error manager
        setErrorManager(new ErrorManager());
        
    }

    
    private String getProperty(String name, String defaultValue) {
        String value = LogManager.getLogManager().getProperty(name);
        if (value == null) {
            value = defaultValue;
        } else {
            value = value.trim();
        }
        return value;
    }
    
    
    /**
     * Open the new log file for the date specified by <code>date</code>.
     */
    private void open() {

        // Create the directory if necessary
        File dir = new File(directory);
        dir.mkdirs();

        // Open the current log file
        try {
            String pathname = dir.getAbsolutePath() + File.separator +
                prefix + date + suffix;
            writer = new PrintWriter(new FileWriter(pathname, true), true);
            writer.write(getFormatter().getHead(this));
        } catch (Exception e) {
            reportError(null, e, ErrorManager.OPEN_FAILURE);
            writer = null;
        }

    }

}
