/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractFileLoggingMechanism.java,v 1.1 2008/03/21 18:21:57 hburger Exp $
 * Description: Abstract File Logging Mechanism
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:21:57 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.kernel.log.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.openmdx.compatibility.kernel.log.LogEntity;
import org.openmdx.compatibility.kernel.log.LogEntityReader;
import org.openmdx.compatibility.kernel.log.LogEvent;
import org.openmdx.compatibility.kernel.log.SysLog;


/**
 * AbstractFileLoggingMechanism is an abstract class which
 * defines the format and location of log files,
 * so they all work consistently for the same application.  The default
 * file name where logs are created is:
 * <pre>
 *      [Path][App][LogClass][date].[extn]
 *
 *      where:      [Path]      = "c:\temp\"
 *                  [App]       = "App"
 *                  [LogClass]  = The Java Class name of the Log Class
 *                                (only used for unique logs)
 *                  [date]      = today's date (only used for dated logs)
 *                  [extn]      = "log"
 *
 * </pre>
 * The [App] default can be overridden by the application, it is taken
 * from the associated logger class. The [Path] and [extn] defaults can be
 * overridden by settings in a properties file, see the LogProperties
 * class for more information.
 */
@SuppressWarnings("unchecked")
public abstract class AbstractFileLoggingMechanism
    extends AbstractLoggingMechanism
{

    /**
     * Opens a file log.
     *
     * @param log A logger that opens the mechanism
     */
    synchronized
    protected void open(Log log)
    {
        // Get log directory from the log properties
        String logName = log.getName(); // log name { AppLog, SysLog, ...}


        LogProperties logProperties;
        if (isSharedLog()) {
            // A shared mechanism must be configured using SysLog properties!!!
            logProperties = SysLog.getLogger().getLogProperties();
        }else{
            logProperties = log.getLogProperties();
        }

        this.logDir = new File(logProperties.getLoggingPath(logName));

        // Check if the log directory exists
        if (!this.logDir.isDirectory()) {
            LogLog.error(
                this.getClass(),
                "open",
                "The configured log file directory '"
                + this.logDir + "' does not exist. Using the"
                + " current working directory instead",
                "");
            this.logDir = new File(".");
        }

        // Determine the log file name ...
        this.filePrefix   = logFileNamePrefix(log);
//      this.fileInstance = logFileNameInstance(log);
        this.fileSuffix   = logFileNameSuffix(log);

        // Determine the log file name ...
        this.fileName = logFileName(log);
        this.logFile  = new File(this.logDir, this.fileName);


        // ... and open the log file
        try {
            this.fileOutputStream = new FileOutputStream(logFile.getPath(), true);  // append
            this.writer           = new PrintWriter(this.fileOutputStream, true);  // autoflush
        } catch (IOException e) {
            super.close();

            // Need a fallback stream
            this.writer = new PrintWriter(System.err);

            LogLog.error(
                this.getClass(),
                "open",
                "Caught IOException: " + e.getMessage()
                + " when trying to open the file named " +  logFile.getPath()
                + ". Falling back to 'stderr'",
                "");

            this.fileName = null;
            this.logDir   = null;
            this.logFile  = null;
        }

        // Write the log event format only if the file has been newly created
        // The log event format goes to the top line in the file. This
        // information is read by the log file parser!
        if (this.logFile != null && this.logFile.length() == 0) {
            this.writer.println(
                getFormatter().getLogFileSpecifier());
        }

        super.open(log); // The mechanism is now open
    }


    /**
     *  Closes a file log
     */
    synchronized
    protected void close()
    {
        super.close(); // The mechanism is now closed

        this.fileName = null;
        this.logDir   = null;
        this.logFile  = null;

        try {
            this.fileOutputStream.close();
            this.fileOutputStream = null;
            this.writer = null;
        } catch (IOException e) {
            LogLog.error(
                this.getClass(),
                "close",
                "Caught IOException: " + e.getMessage()
                + " when trying to close log file",
                "");
        }
    }


    /**
     * Return a nice debug string.
     */
    public String toString()
    {
        return super.toString() + " is logging to file <" +  this.fileName + ">";
    }


    /**
     * Mechanisms can be dated or non-dated.  If dated, the log
     * needs to have a date appended to a
     * file name (if it has one) and it must be rolled over to a new
     * one when the current date changes.  The default setting is
     * non-dated, this method must be overridden when dated.
     */
    abstract protected boolean isDatedLog();


    /**
     * Logs an event on the mechanism.
     *
     * @param log the logger this mechanism belongs to
     * @param event a log event
     */
    protected void logEvent(
        Log       log,
        LogEvent  event)
    {
        if ((event != null) && isOpen()) {
            synchronized(this) {
                // If the log is dated and we rolled over a date, must close and
                // reopen the log to switch dates!
                if (isDatedLog() && hasDateRolledOver()) {
                    rollLogOver(log, "date rollover");
                }

                this.writer.println(getFormatter().format(event));
                this.writer.flush();
            }
        }
    }


    /**
     * Returns the <code>PrintWriter</code> the mechanism writes to.
     *
     * @return a <code>PrintWriter</code>
     */
    protected PrintWriter getPrintWriter()
    {
        return this.writer;
    }


    /**
     * Returns a list of all active (in use) log entities that are available
     * by the file logging mechanisms controlled by this logger
     *
     * @return a list of {@link LogEntity} objects
     */
	public List<LogEntity> getActiveEntities()
    {
        ArrayList entities = new ArrayList();

        if (this.fileName != null) {
            entities.add(new LogEntity(this.fileName, getName()));
        }

        return entities;
    }


    /**
     * Returns a list of all readable log entities that are available
     * by the file logging mechanisms controlled by this logger
     *
     * @return a list of log entity names
     */
    public List<LogEntity> getReadableEntities()
    {
        ArrayList entities = new ArrayList();

        if (this.fileName != null) {
            entities.add(new LogEntity(this.fileName, getName()));
            entities.addAll(getRemoveableEntities());
        }

        return entities;
    }


    /**
     * Returns a list of all removeable log entities that are available
     * by the file logging mechanisms controlled by this logger
     *
     * @return a list of log entitiy names
     */
    public List<LogEntity> getRemoveableEntities()
    {
        String    file, instance;
        String    files[] = this.logDir.list();
        ArrayList entities = new ArrayList();

        for(int ii=0; ii<files.length; ii++) {
            file = files[ii];

            // The currently used file is not eligible for removal :-)
            if (file.equals(this.fileName)) {
                continue;
            }

            // parse the filename
            if (file.startsWith(this.filePrefix) && file.endsWith(this.fileSuffix)) {
                instance = file.substring(
                                this.filePrefix.length(),
                                file.length() - this.fileSuffix.length());

                if (isDatedLog()) {
                    // dated logs have a date as instance modifier
                    if (instance.length() == (AbstractFileLoggingMechanism.dateFormat.length()+1)) {
                        entities.add(new LogEntity(file, getName()));
                    }
                }else{
                    // non dated logs do not have an instance modifier
                    if (instance.length() == 0) {
                        entities.add(new LogEntity(file, getName()));
                    }
                }
            }
        }

        return entities;
    }


    /**
     * Remove a log entity
     *
     * @param String an entity name
     */
    public void removeEntity(LogEntity entity)
    {
        File   file = new File(this.logDir, entity.getName());

        if (file.canWrite()) {
            SysLog.trace("Deleting log file: " + entity.getName());
            file.delete();
        }
    }


    /**
     * Returns an entity reader for given log entity
     *
     * @param String an entity name
     */
    public LogEntityReader getReader(LogEntity entity)
    {
        File   file = new File(this.logDir, entity.getName());

        if (file.canRead()) {
            return new LogFileParser(file);
        }

        return null;
    }


    /**
     * Rolls a log over, typically on a date change, by closing the log
     * and reopening it.
     *
     * <p>NOTE: Do not call any method that itself calls logEvent() to
     * prevent any loop. E.g. any notification calls do that!
     *
     * @param log  The logger under which an event caused a rollover
     * @param reason A rollover reason
     */
    protected void rollLogOver(
        Log log,
        String reason)
    {
        if (acceptsNotificationLogs()) {
            LogEvent event = createNotificationEvent(
                                log,
                                "Log closed (" + reason + ")",
                                null);

            this.writer.println(getFormatter().format(event));
            this.writer.flush();
        }

        close();
        open(log);

        if (acceptsNotificationLogs()) {
            LogEvent event = createNotificationEvent(
                                log,
                                "Log reopenend (" + reason + ") for "
                                + log.getName() + " "
                                + org.openmdx.kernel.Version.getImplementationVersion(),
                                null);

            this.writer.println(getFormatter().format(event));
            this.writer.flush();
        }
    }

    /**
     * Checks the current date verses the date the log was opened.
     * If the current date is one or more days greater (that is,
     * have we passed midnight), return true.  Else, return false.
     * To make this method reasonably fast, it does not compare years, only
     * days within a year.
     */
    protected boolean hasDateRolledOver()
    {
        int todaysDayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        Calendar logOpenedCalendar = Calendar.getInstance();
        logOpenedCalendar.setTime(getDateOpend());

        int logOpenedDayOfYear = logOpenedCalendar.get(Calendar.DAY_OF_YEAR);

        return (todaysDayOfYear != logOpenedDayOfYear);
    }


    /**
     * Return the log file name prefix.
     *
     * <p>Format:
     * <ul>
     * <li> not shared, not dated  "{appid}.{logname}.{suffix}"
     * <li> not shared, dated      "{appid}.{logname}.{time}.{suffix}"
     * <li> shared    , not dated  "{appid}.shared.{suffix}"
     * <li> shared    , dated      "{appid}.shared.{time}.{suffix}"
     * </ul>
     *
     * <pre>
     * {appid}   log property "ApplicationID"       e.g.: "AuthClient"
     * {logname} taken from the logger              e.g.: "AppLog", "SysLog"
     * {time}    the opening time as "YYYYMMDD"     e.g.: "20020723"
     * {suffix}  A suffix following ".{extModifier}.{ext}" or if {extModifier} is
     *           empty ".{ext}"
     *
     * {extModifier} taken AbstractFileLoggingMechanism.getFileNameSuffixModifier(..)
     * {ext}         taken from the logging properties
     * </pre>
     *
     * @param  log  a log
     * @return a string
     */
    private String logFileName(
        Log log)
    {
    	StringBuilder sb = new StringBuilder();


        // filename prefix
        if (! isSharedLog() ) {
            // UNIQUE Logs
            sb.append(
                log.getConfig().getApplicationId()
            ).append(
                "."
            ).append(
                log.getName()
            );
        }else{
            // SHARED Logs
            sb.append(
                SysLog.getLogger().getLogProperties().getApplicationId(log.getName())
            ).append(
                "."
            ).append("shared");
        }

        // Dated logs also have the current date
        if (isDatedLog()) {
            sb.append(
                "."
            ).append(
                fileNameDateFormat.format(new Date())
            );
        }

        // filename suffix
        String modifier = getFileNameSuffixModifier(log);
        sb.append(
            "."
        );
        if (modifier.length() != 0) {
            sb.append(
                modifier
            ).append(
                "."
            );
        }
        sb.append(
            log.getLogProperties().getLoggingExtension(log.getName())
        );

        return sb.toString();
    }


    /**
     * Return the log file name prefix
     *
     * @param  log  a log
     * @return a string
     */
    private String logFileNamePrefix(Log log)
    {
    	StringBuilder sb = new StringBuilder();


        // filename prefix
        if (! isSharedLog() ) {
            // UNIQUE Logs
            sb.append(
                log.getConfig().getApplicationId()
            ).append(
                "."
            ).append(
                log.getName()
            );
        }else{
            // SHARED Logs
            sb.append(
                SysLog.getLogger().getLogProperties().getApplicationId(log.getName())
            ).append(
                "."
            ).append(
                "shared"
            );
        }

        return sb.toString();
    }


//  /**
//   * Return the log file name instance. This is the current date with
//   * dated logs
//   *
//   * @param  log  a log
//   * @return a string
//   */
//  private String logFileNameInstance(Log log)
//  {
//      StringBuffer sb = new StringBuffer();
///        // Dated logs also have the current date
//      if (isDatedLog()) {
//          sb.append(".");
//          sb.append(fileNameDateFormat.format(new Date()));
//      }
//
//      return sb.toString();
//  }


    /**
     * Return the log file name suffix
     *
     * @param  log  a log
     * @return a string
     */
    private String logFileNameSuffix(Log log)
    {
    	StringBuilder sb = new StringBuilder();

        // filename suffix
        String modifier = getFileNameSuffixModifier(log);
        sb.append(".");
        if (modifier.length() != 0) {
            sb.append(
                modifier
            ).append(
                "."
            );
        }
        sb.append(
            log.getLogProperties().getLoggingExtension(log.getName())
        );

        return sb.toString();
    }


//  private void parseRotationProperties(
//          LogProperties logProperties,
//          String        logName)
//  {
//      String sRotationMode = logProperties.getProperty(
//                              logName,
//                              getName() + "." + AbstractFileLoggingMechanism.CFG_ROTATION_MODE,
//                              AbstractFileLoggingMechanism.CFG_ROTATION_MODE_NONE);
//
//      if (sRotationMode.equals(AbstractFileLoggingMechanism.CFG_ROTATION_MODE_DATE)) {
//          this.rotationMode = AbstractFileLoggingMechanism.ROTATION_MODE_DATE;
//      }
//      else if (sRotationMode.equals(AbstractFileLoggingMechanism.CFG_ROTATION_MODE_FILE)) {
//          long multiplier = 1;
//          String size = logProperties.getProperty(
//                              logName,
//                              getName() + "." + AbstractFileLoggingMechanism.CFG_ROTATION_SIZE,
//                              "10MB");
//
//          if (size.endsWith("MB")) {
//              size = size.substring(0, size.length()-2);
//              multiplier = 1024*1024;
//          }
//          else if (size.endsWith("KB")) {
//              size = size.substring(0, size.length()-2);
//              multiplier = 1024;
//          }
//
//          this.rotationMode = AbstractFileLoggingMechanism.ROTATION_MODE_FILE;
//          try {
//              this.rotationFileSize = Long.parseLong(size) * multiplier;
//          }
//          catch(NumberFormatException ne) {}
//      }
//      else {
//          this.rotationMode = AbstractFileLoggingMechanism.ROTATION_MODE_NONE;
//      }
//  }


    /**
     * Returns the file name suffix modifier. This is the only part of the
     * log file name a logging mechanism can modify.
     *
     * @param  log  a log
     * @return a string
     */
    protected String getFileNameSuffixModifier(
        Log log)
    {
        return "";
    }


    private PrintWriter writer = new PrintWriter(System.err);

    /** The file instance date format */
    private static final String  dateFormat = "yyyyMMdd"; // "yyyyMMdd-HHmmss"


//  private static final String CFG_ROTATION_MODE      = "rotation.mode";
//  private static final String CFG_ROTATION_SIZE      = "rotation.size";
//  private static final String CFG_ROTATION_MODE_NONE = "none";
//  private static final String CFG_ROTATION_MODE_DATE = "date";
//  private static final String CFG_ROTATION_MODE_FILE = "file";
//
//  private static final int ROTATION_MODE_NONE = 0;
//  private static final int ROTATION_MODE_DATE = 1;
//  private static final int ROTATION_MODE_FILE = 2;
//
//  private int rotationMode = AbstractFileLoggingMechanism.ROTATION_MODE_NONE;
//  private long rotationFileSize = 1024 * 1024; // 1MB

    /** A date format used in log file names */
    private final SimpleDateFormat fileNameDateFormat =
                                    new SimpleDateFormat(dateFormat);


    /** The file output stream to be used */
    private FileOutputStream fileOutputStream = null;

    /** The log file suffix used */
    private String fileSuffix = null;

    /** The log file suffix used */
//  private String fileInstance = null;

    /** The log file prefix used */
    private String filePrefix = null;

    /** The currently used filename (=basename of the file)*/
    private String fileName = null;

    /** The currently used log directory */
    private File logDir = null;

    /** The log file */
    private File logFile = null;
}

