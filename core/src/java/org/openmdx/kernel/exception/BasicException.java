/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: BasicException.java,v 1.24 2008/12/08 11:30:42 hburger Exp $
 * Description: Basic Exception
 * Revision:    $Revision: 1.24 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/12/08 11:30:42 $
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
package org.openmdx.kernel.exception;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openmdx.compatibility.kernel.application.cci.Classes;
import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.kernel.enumeration.IntegerEnumeration;
import org.openmdx.kernel.text.MultiLineStringRepresentation;
import org.slf4j.LoggerFactory;


/**
 * The StackedException implements stackable exceptions. Stackable exceptions
 * are useful to avoid hidden exceptions and to allow for remapping exceptions
 * while keeping the complete exception stack.
 *
 * <p>Instead of defining a central resource defining error codes the
 * <code>StackedException</code> follows the approach of a decentralized error
 * code management using error domains. An organization may define as many error
 * domains as required. Each error domain defines it's own error codes as
 * positive values starting with the value of '1'. An error code itself is not
 * unique, uniqueness is only achieved together with an error domain.
 *
 * <p>To simplify things a number of common error codes are defined that are
 * are shared within all error domains. These error codes have negative values.
 * The <code>StackedException</code> provides error code mappers to map error
 * codes from foreign domains to strings. This mappers are used when requesting
 * a stringified form of the exception.
 *
 * <p>To enable interoperation with application domain specific exceptions
 * exception mappers are used to convert foreign exceptions to stacked
 * exceptions. These mappers are implicitely called when creating a new
 * <code>StackedException</code> passing an embedded exception that is not
 * of type <code>StackedException</code>.
 *
 * <p>A sample demonstrating the application of exception mappers in Corba
 * applications:
 *
 * <pre>
 *    Corba Server                         Corba Client
 *    -----------------------------------  -----------------------------------
 *    :    :
 *    1) throw StackedException
 *    2) convert to Corba User Exception
 *    3) throw Corba User Exception
 *
 *                                          4) catch Corba User Exception
 *                                          5) create StackedException passing
 *                                             the Corba Exception -> invokes
 *                                             the Corba Exception mapper
 *                                          6) throw StackException
 *                                          :    :
 * </pre>
 *
 * <p>Each <code>StackedException</code> provides several information elements:
 * <ul>
 * <li> an error domain
 * <li> an error code
 * <li> exception parameters
 * <li> a description
 * <li> an embedded exception (optionally)
 * </ul>
 */
public final class BasicException
    extends java.lang.Exception
    implements MultiLineStringRepresentation
{

    /**
     * Creates a new <code>BasicException</code>.
     *
     * @param exceptionDomain An exception domain. A null objects references
     * the default exception domain with negative exception codes only.
     * @param exceptionCode  An exception code. Negative codes describe common
     * exceptions codes. Positive exception codes are specific for a given
     * exception domain.
     * @param parameters  Any exception parameters
     * @param description A readable description
     * @deprecated Use {@link #BasicException(String,int,String,Parameter[])} instead
     */
    public BasicException(
        String exceptionDomain,
        int exceptionCode,
        Parameter[] parameters,
        String description)
    {
        this(exceptionDomain, exceptionCode, description, parameters);
    }


    /**
     * Creates a new <code>BasicException</code>.
     *
     * @param exceptionDomain An exception domain. A null objects references
     * the default exception domain with negative exception codes only.
     * @param exceptionCode  An exception code. Negative codes describe common
     * exceptions codes. Positive exception codes are specific for a given
     * exception domain.
     * @param description A readable description
     * @param parameters  Any exception parameters
     */
    public BasicException(
        String exceptionDomain,
        int exceptionCode,
        String description,
        Parameter... parameters
    ){
        super(
            BasicException.getSimpleMessage(exceptionDomain, exceptionCode)
        );
        this.backtrace = this.getStackTrace();
        this.exceptionDomain = validateExceptionDomain(exceptionDomain);
        this.exceptionCode = exceptionCode;
        this.parameters = (parameters == null) ? new Parameter[0] : parameters;
        this.description = (description == null) ? "" : description;
        this.timestamp = new Date();
    }

    /**
     * Creates a new <code>BasicException</code>.
     *
     * @param throwable An embedded exception
     * @param exceptionDomain An exception domain. A null objects references
     * the default exception domain with negative exception codes only.
     * @param exceptionCode  An exception code. Negative codes describe common
     * exceptions codes. Positive exception codes are specific for a given
     * exception domain.
     * @param description A readable description
     * @param parameters  Any exception parameters
     */
    public BasicException(
        Throwable throwable,
        String exceptionDomain,
        int exceptionCode,
        String description,
        Parameter... parameters)
    {
        super(
            BasicException.getSimpleMessage(exceptionDomain, exceptionCode)
        );
        this.backtrace = this.getStackTrace();
        this.exceptionDomain = validateExceptionDomain(exceptionDomain);
        this.exceptionCode = exceptionCode;
        this.parameters = (parameters == null) ? new Parameter[0] : parameters;
        this.description = (description == null) ? "" : description;
        initCause(throwable);
        this.timestamp = new Date();
    }


    /**
     * Creates a new <code>BasicException</code>.
     *
     * <p>This constructor is primarily used by exception mappers.
     *
     * @param className A class name
     * @param methodName A method name
     * @param lineNr A line number
     * @param exceptionDomain An exception domain. A null objects references
     * the default exception domain with negative exception codes only.
     * @param exceptionCode  An exception code. Negative codes describe common
     * exceptions codes. Positive exception codes are specific for a given
     * exception domain.
     * @param parameters  Any exception parameters
     * @param description A readable description
     * @param callStack
     * @param timestamp
     */
    public BasicException(
        String className,
        String methodName,
        int lineNr,
        String exceptionDomain,
        int exceptionCode,
        Parameter[] parameters,
        String description,
        StackTraceElement[] callStack,
        Date timestamp)
    {
        super(
            BasicException.getSimpleMessage(exceptionDomain, exceptionCode)
        );
        this.exceptionDomain = validateExceptionDomain(exceptionDomain);
        this.exceptionCode = exceptionCode;
        this.parameters = (parameters == null) ? new Parameter[0] : parameters;
        this.description = (description == null) ? "" : description;
        this.backtrace = callStack == null ? MISSING_STACK_TRACE : callStack;
        this.timestamp = timestamp == null ? new Date() : timestamp;
    }



    /**
     * Creates a new <code>BasicException</code>.
     *
     * <p>This constructor is primarily used by exception mappers.
     *
     * @param throwable An embedded exception
     * @param className A class name
     * @param methodName A method name
     * @param lineNr A line number
     * @param exceptionDomain An exception domain. A null objects references
     * the default exception domain with negative exception codes only.
     * @param exceptionCode  An exception code. Negative codes describe common
     * exceptions codes. Positive exception codes are specific for a given
     * exception domain.
     * @param parameters  Any exception parameters
     * @param description A readable description
     * @param callStack
     * @param timestamp
     */
    public BasicException(
        Throwable throwable,
        String className,
        String methodName,
        int lineNr,
        String exceptionDomain,
        int exceptionCode,
        Parameter[] parameters,
        String description,
        StackTraceElement[] callStack,
        Date timestamp)
    {
        super(
            BasicException.getSimpleMessage(exceptionDomain, exceptionCode)
        );
        this.exceptionDomain = validateExceptionDomain(exceptionDomain);
        this.exceptionCode = exceptionCode;
        this.parameters = (parameters == null) ? new Parameter[0] : parameters;
        this.description = (description == null) ? "" : description;
        initCause(throwable);
        this.backtrace = callStack == null ? MISSING_STACK_TRACE : callStack;
        this.timestamp = timestamp == null ? new Date() : timestamp;
    }


    /**
     * Creates a new <code>BasicException</code>.
     *
     * <p>This constructor is primarily used by exception mappers.
     *
     * @param throwable An embedded exception
     * @param exceptionDomain An exception domain. A null objects references
     * the default exception domain with negative exception codes only.
     * @param exceptionCode  An exception code. Negative codes describe common
     * exceptions codes. Positive exception codes are specific for a given
     * exception domain.
     * @param parameters  Any exception parameters
     * @param description A readable description
     * @param that A throwable from which the backtrace and other exception
     * information is used. If null this information is taken from the
     * <code>this</code> object.
     */
    public BasicException(
        Throwable throwable,
        String exceptionDomain,
        int exceptionCode,
        Parameter[] parameters,
        String description,
        Throwable that)
    {
        super(
            BasicException.getSimpleMessage(exceptionDomain, exceptionCode)
        );
        this.backtrace = that == null ? that.getStackTrace() : this.getStackTrace();
        this.exceptionDomain = validateExceptionDomain(exceptionDomain);
        this.exceptionCode = exceptionCode;
        this.parameters = applyThrowable(parameters,that);
        this.description = (description == null) ? "" : description;
        initCause(toStackedException(throwable));
        this.timestamp = new Date();
    }

    
    /**
     * The placeholder in case the source is unknown
     */
    private static final String MISSING_SOURCE = "n/a";
    
    /**
     * The placeholder in case the class name is unknown
     */
    private static final String MISSING_CLASS_NAME = "";
    
    /**
     * The placeholder in case the method name is unknown
     */
    private static final String MISSING_METHOD_NAME = "";
    
    /**
     * The placeholder in case the line number is unknown
     */
    private static final int MISSING_LINE_NUMBER = -1;

    /**
     * The placeholder in case the time stamp is unknown
     */
    private static final String MISSING_TIME_STAMP = "???";
    
    
    /**
     * The placeholder in case the stack trace is unknown
     */
    private static final StackTraceElement[] MISSING_STACK_TRACE = {};
    

    /**
     * @serial The exception domain.
     */
    private final String exceptionDomain;

    /**
     * @serial The exception code.
     */
    private final int exceptionCode;

    /**
     * @serial The exception's timestamp.
     */
    private final Date timestamp;

    /**
     * @serial The exception parameters.
     */
    private final Parameter[] parameters;

    /**
     * @serial The exception description.
     */
    private final String description;

    /**
     * @serial The backtrace of this exception.
     */
    private StackTraceElement[] backtrace;

    /**
     * The exception source object.
     */
    static Object source = MISSING_SOURCE;

    /**
     * The serial version UID
     */
    private static final long serialVersionUID = 3421693369141411893L;

    /**
     * 
     */
    private static final Map<String,IntegerEnumeration.Mapper> exceptionCodeMapperMap = 
        new HashMap<String,IntegerEnumeration.Mapper>();

    /**
     * 
     */
    private final static Map<Class<? extends Throwable>,BasicException.Mapper> exceptionMapperMap = 
        new HashMap<Class<? extends Throwable>,BasicException.Mapper>();

    /**
     * 
     */
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    
    /**
     * Initializes the cause of this throwable to the specified value. 
     * (The cause is the throwable that caused this throwable to get thrown.)
     * 
     * The cause can only be set once.
     * <p>Added for JDK 1.4 compliancy.
     * 
     * @param throwable
     * @return A reference to this <code>Throwable</code> instance
     * @throws IllegalArgumentException if cause is this throwable. 
     * (A throwable cannot be its own cause.)
     * @throws IllegalStateException if the cause has already been set
     */
    public Throwable initCause(
        Throwable throwable
    ){
        BasicException tail = toStackedException(throwable);
        if(throwable != null) {
            Check: for(
                BasicException cursor = tail;
                cursor != null;
                cursor = cursor.getCause()
            ){
                if(cursor == this) {
                    tail = new BasicException(
                        Code.DEFAULT_DOMAIN,
                        Code.ASSERTION_FAILURE,
                        "Detected a recursion flaw within the exception stack. " +
                        " The exception is fixed but some information had to be discarded."
                    );
                    break Check;
                }
            }
        }
        return super.initCause(tail);
    }

    /* (non-Javadoc)
     * @see java.lang.Throwable#getCause()
     */
    @Override
    public final BasicException getCause() {
        return (BasicException) super.getCause();
    }


    /**
     * Appends the specified throwable to the end of the exception stack of this 
     * <code>BasicException</code>
     * 
     * @param throwable A throwable to append
     * @return A reference to this <code>Throwable</code> instance
     */
    public BasicException appendCause(
        Throwable throwable
    ){
        if (throwable != null) {
            getInitialCause().initCause(throwable);
        }
        return this;
    }

    /**
     * Retrieve the initial cause
     * 
     * @return the initial cause
     */
    private BasicException getInitialCause(
    ){
        BasicException cause = null;
        for(
            BasicException cursor = this;
            cursor != null;
            cursor = cursor.getCause()
        ){
            cause = cursor;
        }
        return cause;
    }

    /**
     * Maps a throwable to a <code>BasicException</code> using the registered
     * exception mappers
     *
     * @param throwable
     * @return BasicException or null if the passed throwable is null
     */
    public static synchronized BasicException toStackedException(
        Throwable throwable
    ) {
        if (throwable == null) {
            return null;
        } else if (throwable instanceof BasicException) {
            return (BasicException)throwable;
        } else if (throwable instanceof BasicException.Wrapper) {
            return ((BasicException.Wrapper)throwable).getCause();
        } else if (throwable.getCause() instanceof BasicException) {
            return (BasicException)throwable.getCause();
        } else {
            Mapper mapper = null;
            BasicException ex;
            try {
                Class<?> clazz = throwable.getClass();
                do {
                    mapper = exceptionMapperMap.get(clazz);
                    if (mapper != null) {
                        ex = mapper.map(throwable);
                        if (ex != null) {
                            return ex;
                        }
                    }
                    clazz = clazz.getSuperclass(); // try with its superclass
                } while(clazz != Object.class);
                return null;
            } catch(Exception e) {
                // An exception mapper may throw a runtime exception even if 
                // it should not do so. The default mapper is considered as
                // save.
                return new BasicException(
                    exceptionMapperMap.get(Throwable.class).map(throwable),
                    Code.DEFAULT_DOMAIN,
                    Code.ASSERTION_FAILURE,
                    "Caught an exception while mapping exceptions: " + e.getMessage(),
                    new Parameter[] {
                        new Parameter(
                            "mapper.name",
                            mapper==null ? "unknown" : mapper.getClass().getName()
                        ),
                        new Parameter(
                            "mapper.exception",
                            throwable.getClass().getName()
                        )
                    }
                );
            }
        }
    }

    /**
     * Create a <code>BasicException</code> representing a <code>cause</code> 
     * wrapped into a <code>wrapper</code>.
     * 
     * @param cause
     * @param wrapper
     * 
     * @return a <code>BasicException</code> representing the <code>cause</code> 
     * wrapped into <code>wrapper</code>
     */
    public static BasicException toStackedException(
        Throwable cause,
        Throwable wrapper
    ){
        BasicException stack = BasicException.toStackedException(cause);
        return stack == null ? new BasicException(
            stack,
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.GENERIC,
            null,
            wrapper.getMessage(),
            wrapper
        ) : new BasicException(
            stack,
            stack.getExceptionDomain(),
            stack.getExceptionCode(),
            stack.getParameters(),
            stack.getDescription(),
            wrapper
        );
    }

    /**
     * Create a <code>BasicException</code> representing a <code>cause</code> 
     * wrapped into a <code>wrapper</code>.
     * 
     * @param cause
     * @param wrapper
     * 
     * @return a <code>BasicException</code> representing the <code>cause</code> 
     * wrapped into <code>wrapper</code>
     */
    public static BasicException toStackedException(
        Throwable cause,
        Throwable wrapper,
        BasicException.Parameter[] parameters
    ){
        BasicException stack = BasicException.toStackedException(cause);
        return stack == null ? new BasicException(
            stack,
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.GENERIC,
            parameters,
            wrapper.getMessage(),
            wrapper
        ) : new BasicException(
            stack,
            stack.getExceptionDomain(),
            stack.getExceptionCode(),
            BasicException.Parameter.add(stack.getParameters(), parameters),
            stack.getDescription(),
            wrapper
        );
    }

    /**
     * Returns a String representation for the exception's top level object. The
     * string contains the timestamp, class, method, line number, domain, error
     * code, description and the parameters and the exception's stack trace.
     *
     * @return  a String representation
     */
    public String toStringTopLevel()
    {
        return toStringTopLevel(true);
    }


    /**
     * Returns a String representation for the exception's top level object. The
     * string contains the timestamp, class, method, line number, domain, error
     * code, description and the parameters. It does not include the stack
     * trace.
     *
     * @param addBacktrace  if true add the exception's backtrace
     * @return  a String representation
     */
    public String toStringTopLevel(boolean addBacktrace)
    {
        final StringWriter writer = new StringWriter();

        printException(writer, this, -1, addBacktrace);

        return writer.getBuffer().toString();
    }


    /**
     * Returns a formatted multiline String representation for the exception
     * including all stacked exceptions. Includes all available exception
     * information as timestamp, class, method, line, number, domain, error code,
     * description, parameters and the stack trace for each exception.
     *
     * @return  a String representation
     */
    public String toString()
    {
        return toString(true);
    }


    /**
     * Returns a formatted multiline String representation for the exception
     * including all stacked exceptions. Includes all available exception
     * information as timestamp, class, method, line, number, domain, error code,
     * description, parameters and the stack trace for each exception.
     *
     * @param addBacktrace  if true add the exception's backtrace
     * @return  a String representation
     */
    public String toString(boolean addBacktrace)
    {
        final StringWriter writer = new StringWriter();

        printStack(this, new PrintWriter(writer), addBacktrace);

        return writer.getBuffer().toString();
    }


    /**
     * Prints the exception including all stacked exceptions to the specified
     * print stream.
     * @param holder 
     * @param stream <code>PrintStream</code> to use for output
     * @param addBacktrace If true add the exception's backtrace
     */
    public void printStack(
        Throwable holder,
        PrintStream stream,
        boolean addBacktrace
    ){
        final List<BasicException> stack = getExceptionStack();
        synchronized(stream){
            stream.println(holder.getClass().getName());
            for(
                int ii=0, size = stack.size();
                ii<size;
                ii++
            ) printException(
                stream,
                stack.get(size-1-ii),
                ii,
                addBacktrace
            );
        }
    }

    /**
     * Prints the exception including all stacked exceptions to the specified
     * print writer.
     * @param holder 
     * @param addBacktrace If true add the exception's backtrace
     * @param stream <code>PrintWriter</code> to use for output
     */
    public void printStack(
        Throwable holder,
        PrintWriter writer,
        boolean addBacktrace
    ){
        final List<BasicException> stack = getExceptionStack();
        synchronized(writer){
            writer.println(holder.getClass().getName());
            for(
                int ii=0, size = stack.size();
                ii<stack.size();
                ii++
            ) printException(
                writer,
                stack.get(size-1-ii),
                ii,
                addBacktrace
            );
        }
    }


    /**
     * Returns the exception stack as a list of exceptions beginning with the
     * first thrown exception.
     *
     * @return List The exception stack. A list of <code>BasicException</code>
     * objects.
     */
    public List<BasicException> getExceptionStack()
    {
        ArrayList<BasicException> stack = new ArrayList<BasicException>();
        for(
            BasicException cursor = this;
            cursor != null;
            cursor = cursor.getCause()
        ){
            stack.add(0,cursor);
        }
        return stack;
    }


    /**
     * Searches for the parameter with the specified name in the parameter list.
     * If the parameter is not found in the parameter list, a null is returned.
     *
     * @return the parameter's value if found
     */
    public String getParameter(String name)
    {
        for(int ii=0; ii<this.parameters.length; ii++) {
            if (name.equals(this.parameters[ii].name)) {
                return this.parameters[ii].value;
            }
        }

        return null;
    }


    /**
     * Retrieves the class for this <code>BasicException</code> object.
     *
     * @return the class
     */
    public String getClassName() {
        return this.backtrace.length > 0 ? this.backtrace[0].getClassName() : MISSING_CLASS_NAME;
    }


    /**
     * Retrieves the method for this <code>BasicException</code> object.
     *
     * @return the method
     */
    public String getMethodName()
    {
        return this.backtrace.length > 0 ? this.backtrace[0].getMethodName() : MISSING_METHOD_NAME;
    }


    /**
     * Retrieves the line number for this <code>BasicException</code> object.
     *
     * @return the line nr
     */
    public int getLineNr()
    {
        return this.backtrace.length > 0 ? this.backtrace[0].getLineNumber() : MISSING_LINE_NUMBER;
    }

    /**
     * Retrieves the domain for this <code>BasicException</code> toplevel
     * object.
     *
     * @return the domain value
     */
    public String getExceptionDomain()
    {
        return (this.exceptionDomain);
    }

    /**
     * Retrieves the exception code for this <code>BasicException</code>
     * toplevel object.
     *
     * @return the error code
     */
    public int getExceptionCode()
    {
        return (this.exceptionCode);
    }

    /**
     * Retrieves the exception code for this <code>BasicException</code>
     * toplevel object.
     *
     * @return the error code
     */
    public String getExceptionCodeString()
    {
        return BasicException.toString(
            this.exceptionDomain,
            this.exceptionCode);
    }

    /**
     * Retrieves the timestamp for this <code>BasicException</code> toplevel
     * object.
     *
     * @return the timestamp
     */
    public Date getTimestamp()
    {
        return (this.timestamp);
    }


    /**
     * Retrieves the parameters for this <code>BasicException</code> toplevel
     * object.
     *
     * @return the parameters
     */
    public Parameter[] getParameters()
    {
        return (this.parameters);
    }


    /**
     * Retrieves the context for this <code>BasicException</code> toplevel
     * object.
     *
     * @return the context
     */
    public String getDescription()
    {
        return (this.description);
    }


    /**
     * Breaks up an exception description into simple text lines
     *
     * @param description A descripion
     * @return A list of simple text lines
     */
    public static List<String> breakupDescription(String description)
    {
        ArrayList<String> al = new ArrayList<String>();

        if (description != null) {
            // Check for LineFeed characters
            if (description.indexOf('\n') < 0) {
                al.add(description);
                return al;
            }

            // Break the string up into single lines
            char c = '.', last;
            int startPos = 0;

            int ii = 0;
            for(
                int iLimit = description.length();
                ii<iLimit; 
                ii++
            ) {
                last = c;
                c = description.charAt(ii);
                if (c == '\r' || c == '\n') {
                    if (last != '\r') {
                        al.add(
                            ii-startPos > 0 ? description.substring(startPos, ii) : ""
                        );
                    }
                    startPos = ii+1;
                }
            }

            // Flush last line
            if (startPos < ii) {
                al.add(description.substring(startPos, ii));
            }
        }
        return al;
    }

    private static Parameter[] applyThrowable(
        BasicException.Parameter[] parameters,
        Throwable throwable
    ){
        List<Parameter> target = new ArrayList<Parameter>();
        target.add(
            new Parameter(
                Parameter.EXCEPTION_CLASS,
                throwable.getClass().getName()
            )
        );
        target.add(
            new Parameter(
                Parameter.EXCEPTION_SOURCE,
                BasicException.source
            )
        );
        if(parameters != null){ 
            for(
                int i = 0;
                i < parameters.length;
                i++
            ){
                String name = parameters[i].getName();
                if(
                    !Parameter.EXCEPTION_CLASS.equals(name) &&
                    !Parameter.EXCEPTION_SOURCE.equals(name)
                ) {
                    target.add(parameters[i]);
                }
            }
        }
        return target.toArray(
            new Parameter[target.size()]
        );
    }

    /**
     * Set an exception source object. The exception framework uses the 
     * toString() method to determine the exception source for each 
     * BasicException event.
     * The object may be set at any time.
     *
     * <p>
     * Dynamic exception source example:
     * <code>
     *
     * class ExceptionSource
     * {
     *   public String toString()
     *   {
     * 		return "ExceptionSource-" + System.currentTimeMillis();
     * 	 }
     * }
     *
     * BasicException.setSource(new ExceptionSource())
     *
     * </code>
     *
     * <p>
     * Static exception source example:
     * <code>
     *
     * BasicException.setSource("ExceptionSource")
     *
     * </code>
     *
     * @param source an exception source object
     */
    public static void setSource(Object source)
    {
        if (source == null) return;
        BasicException.source = source;
    }


    /**
     * Registers an error code mapper for a given exception domain
     *
     * @param exceptionDomain
     * @param mapper
     */
    public static synchronized void register(
        String exceptionDomain,
        IntegerEnumeration.Mapper mapper)
    {
        if (mapper == null) return;
        if ((exceptionDomain == null) || (exceptionDomain.length() ==0)) return;

        // Exception code mappers must not be redefined
        if (BasicException.exceptionCodeMapperMap.containsKey(exceptionDomain)) return;

        BasicException.exceptionCodeMapperMap.put(exceptionDomain, mapper);
    }

    /**
     * Registers an exception mapper for a given exception class
     *
     * @param exceptionClassName
     * @param mapperClassName
     */
    protected static void registerLeniently(
        String exceptionClassName,
        String mapperClassName
    ){
        try {
            Class<Throwable> exceptionClass = Classes.getKernelClass(exceptionClassName);
            Class<Mapper> mapperClass = Classes.getKernelClass(mapperClassName);
            register(
                exceptionClass, 
                mapperClass.newInstance()
            );
        } catch (Exception exception) {
            LoggerFactory.getLogger(BasicException.class).info(
                "Unable to register exception mapper " + mapperClassName + " handling " + exceptionClassName,
                exception
            );
        }
    }

    /**
     * Registers an exception mapper for a given exception class
     *
     * @param exClass
     * @param mapper
     */
    public static synchronized void register(
        Class<? extends Throwable> exClass,
        Mapper mapper)
    {
        if (mapper == null) return;
        if (exClass == null) return;

        // Exception mappers must not be redefined
        if (BasicException.exceptionMapperMap.containsKey(exClass)) return;

        BasicException.exceptionMapperMap.put(exClass, mapper);
    }

    /**
     * Prints the exception to the specified print stream
     *
     * @param stream <code>PrintStream</code> to use for output
     * @param ex <code>BasicException</code> to use for output
     * @param stackLevel A stacklevel
     * @param addBacktrace If true add the exception's backtrace
     */
    private void printException(
        PrintStream stream,
        BasicException ex,
        int stackLevel,
        boolean addBacktrace)
    {
        DateFormat dateTimeFormatter = new SimpleDateFormat(DATE_TIME_PATTERN); 
        stream.println();
        if (stackLevel >= 0) {
            stream.println("BasicException.Entry[" + String.valueOf(stackLevel) + "]");
        }
        else {
            stream.println("BasicException");
        }

        if (ex.timestamp != null) {
            stream.println("  Timestamp=" + dateTimeFormatter.format(ex.timestamp));
        }
        else {
            stream.println("  Timestamp=" + MISSING_TIME_STAMP);
        }
        stream.println("  Class=" + ex.getClassName());
        stream.println("  Method=" + ex.getMethodName());
        stream.println("  Line=" + ex.getLineNr());
        stream.println("  ExceptionDomain=" + ex.exceptionDomain);
        stream.println("  ExceptionCode=" + BasicException.toString(
            ex.exceptionDomain, ex.exceptionCode));

        if ((ex.parameters != null) && (ex.parameters.length > 0)) {
            for(int ii=0; ii<ex.parameters.length; ii++) {
                stream.println("  Param[" + ii +"]: " + ex.parameters[ii]);
            }
        }

        if (ex.description != null) {
            List<String> descr = breakupDescription(ex.description);
            if (descr.size() == 0) {
                stream.println("  Description=");
            }else if (descr.size() == 1) {
                stream.println("  Description=" + descr.get(0));
            }else{
                stream.println("  Description:");
                for(String line : descr) {
                    stream.println("    " + line);
                }
            }
        }

        if (addBacktrace && (ex.backtrace != null)) {
            stream.println("  Backtrace:");
            for(StackTraceElement element : ex.backtrace) {
                stream.println("    at " + element);
            }
        }
    }


    /**
     * Prints the exception to the specified print stream
     * @param ex <code>BasicException</code> to use for output
     * @param stackLevel A stacklevel
     * @param addBacktrace If true add the exception's backtrace
     * @param stream <code>Writer</code> to use for output
     */
    private void printException(
        Writer writer,
        BasicException ex,
        int stackLevel,
        boolean addBacktrace)
    {
        DateFormat dateTimeFormatter = new SimpleDateFormat(DATE_TIME_PATTERN); 
        PrintWriter w = new PrintWriter(writer);
        w.println();
        if (stackLevel >= 0) {
            w.println("BasicException.Entry[" + String.valueOf(stackLevel) + "]");
        }
        else {
            w.println("BasicException");
        }
        if (ex.timestamp != null) {
            w.println("  Timestamp=" + dateTimeFormatter.format(ex.timestamp));
        }
        else {
            w.println("  Timestamp=" + MISSING_TIME_STAMP);
        }
        w.println("  Class=" + ex.getClassName());
        w.println("  Method=" + ex.getMethodName());
        w.println("  Line=" + ex.getLineNr());
        w.println("  ExceptionDomain=" + ex.exceptionDomain);
        w.println("  ExceptionCode=" + BasicException.toString(
            ex.exceptionDomain, ex.exceptionCode)
        );

        if ((ex.parameters != null) && (ex.parameters.length > 0)) {
            for(int ii=0; ii<ex.parameters.length; ii++) {
                w.println("  Param[" + ii +"]: " + ex.parameters[ii]);
            }
        }

        if (ex.description != null) {
            List<String> descr = breakupDescription(ex.description);
            if (descr.size() == 0) {
                w.println("  Description=");
            }else if (descr.size() == 1) {
                w.println("  Description=" + descr.get(0));
            }else{
                w.println("  Description:");
                for(String line : descr) {
                    w.println("    " + line);
                }
            }
        }
        if (addBacktrace && (ex.backtrace != null)) {
            w.println("  Backtrace:");
            for(StackTraceElement element : ex.backtrace) {
                w.println("    at " + element);
            }
        }
    }

    /**
     * Returns the cause belonging to a specific exception domain.
     * 
     * @param 	exceptionDomain
     * 			the desired exception domain,
     *          or <code>null</code> to retrieve the initial cause.
     *
     * @return  Either the cause belonging to a specific exception domain
     *          or the initial cause if <code>exceptionDomain</code> is
     * 			<code>null</code>.  
     */
    public BasicException getCause(
        String exceptionDomain
    ){
        if(exceptionDomain == null) {
            return getInitialCause();
        } else {
            for(
                BasicException cursor = this;
                cursor != null;
                cursor = cursor.getCause()
            ){
                if(exceptionDomain.equals(cursor.exceptionDomain)) {
                    return cursor;
                }
            }
            return null;
        }
    }

    /**
     * Returns a simple exception message based on the exception domain and 
     * code.
     * 
     * @param exDomain
     * @param exCode
     * @return A simple exception message
     */
    private static String getSimpleMessage(
        String exDomain,
        int exCode)
    {
        return validateExceptionDomain(exDomain)
        + "."
        + BasicException.toString(
            validateExceptionDomain(exDomain),
            exCode);
    }


    /**
     * Return a validated exception domain
     *
     * @param exDomain
     * @return String
     */
    private static String validateExceptionDomain(String exDomain)
    {
        if ((exDomain == null) || (exDomain.length() == 0)) {
            return Code.DEFAULT_DOMAIN;
        }
        else {
            return exDomain;
        }
    }


    /**
     * Returns the string representation of an exception code. Delegates to the
     * domain's exception code mapper.
     *
     * @param exDomain
     * @param exCode
     * @return String
     */
    private static synchronized String toString(
        String exDomain,
        int exCode)
    {
        IntegerEnumeration.Mapper mapper;
        if (exCode <= 0) {
            mapper = BasicException.exceptionCodeMapperMap.get(
                Code.DEFAULT_DOMAIN
            );
        } else {
            mapper = BasicException.exceptionCodeMapperMap.get(exDomain);
            if (mapper == null) {
                mapper = BasicException.exceptionCodeMapperMap.get(Code.DEFAULT_DOMAIN);
            }
        }
        return mapper.toString(exCode);
    }

    /**
     * The default exception mapper
     */
    static class DefaultMapper
    implements Mapper
    {
        public BasicException map(Throwable throwable)
        {
            if (throwable == null) {
                // This should never happen. So far we're only called from
                // BasicException.toStackedException() where a null
                // throwable is checked. 
                return new BasicException(
                    Code.DEFAULT_DOMAIN,
                    Code.BAD_PARAMETER,
                    "Got a null throwable to map in the DefaultMapper" ,
                    new Parameter[0]);
            }

            if (throwable instanceof BasicException) {
                // This should never happen. So far we're only called from
                // BasicException.toStackedException() where a BasicException
                // throwable is checked.
                return (BasicException)throwable;
            }

            if (throwable instanceof Wrapper) {
                // unwrap
                BasicException se = ((Wrapper)throwable).getCause();

                if (se != null) {
                    return se;
                } // else fall through and map the wrapper exception itself
            }


            StackTraceElement[] backTrace = throwable.getStackTrace();
            Throwable cause = throwable.getCause();

            return cause == null ? new BasicException(
                null,
                null,
                -1,
                Code.DEFAULT_DOMAIN,
                Code.GENERIC,
                new Parameter[] {
                    new Parameter(
                        Parameter.EXCEPTION_CLASS,
                        throwable.getClass().getName()
                    ),
                    new Parameter(
                        Parameter.EXCEPTION_SOURCE,
                        BasicException.source
                    )
                },
                throwable.getMessage(),
                backTrace,
                new Date()
            ) : new BasicException(
                cause,
                null,
                null,
                -1,
                Code.DEFAULT_DOMAIN,
                Code.GENERIC,
                new Parameter[] {
                    new Parameter(
                        Parameter.EXCEPTION_CLASS,
                        throwable.getClass().getName()
                    ),
                    new Parameter(
                        Parameter.EXCEPTION_SOURCE,
                        BasicException.source
                    )
                },
                throwable.getMessage(),
                backTrace,
                new Date()
            );

        }
    }

    /**
     * Register the default mappers
     */
    static {
        BasicException.register(
            Code.DEFAULT_DOMAIN,
            Code.getMapper()
        );
        BasicException.register(
            Throwable.class,
            new DefaultMapper()
        );
        BasicException.register(
            SQLException.class,
            new SQLExceptionMapper()
        );
        BasicException.register(
            InvocationTargetException.class,
            new InvocationTargetExceptionMapper()
        );
        BasicException.register(
            RemoteException.class,
            new RemoteExceptionMapper()
        );
        BasicException.register(
            UndeclaredThrowableException.class,
            new UndeclaredThrowableExceptionMapper()
        );
        BasicException.register(
            PrivilegedActionException.class,
            new PrivilegedActionExceptionMapper()
        );
        BasicException.registerLeniently(
            "javax.resource.ResourceException",
            "org.openmdx.kernel.exception.ResourceExceptionMapper"
        );
        BasicException.registerLeniently(
            "javax.naming.NamingException",
            "org.openmdx.kernel.exception.NamingExceptionMapper"
        );
        BasicException.registerLeniently(
            "javax.ejb.EJBException",
            "org.openmdx.kernel.exception.EJBExceptionMapper"
        );

    }

    //------------------------------------------------------------------------
    // Class Code
    //------------------------------------------------------------------------
    
    /**
     * The BasicException's DEFAULT_DOMAIN
     */
    public static class Code
        extends IntegerEnumeration
    {

        /**
         * Avoid instantiation
         */
        protected Code() {
            super();
        }

        /**
         * The default domain. Use this domain if there is no specific error domain
         * defined and the error codes are negativ.
         */
        public static final String DEFAULT_DOMAIN = "DefaultDomain";

        /**
         * No error condition
         */
        public static final int NONE = 0;

        /**
         * An ABORT exception is thrown to report that a non-transactional unit of
         * work has been aborted.
         */
        public static final int ABORT = -44;

        /**
         * Activation failure
         */
        public static final int ACTIVATION_FAILURE = -10;

        /**
         * Assertion error.
         * <p>
         * Assertion errors signal "unexpected" exceptions such as programming
         * errors.
         */
        public static final int ASSERTION_FAILURE = -2;

        /**
         * authentication failure.
         */
        public static final int AUTHENTICATION_FAILURE = -31;

        /**
         * authorization failure.
         */
        public static final int AUTHORIZATION_FAILURE = -9;

        /**
         * Error processing context
         */
        public static final int BAD_CONTEXT = -29;

        /**
         * Non-existing attribute or property.
         */
        public static final int BAD_MEMBER_NAME = -41;

        /**
         * An error code that signals bad/inconsistent parameters.
         */
        public static final int BAD_PARAMETER = -30;

        /**
         * Wrong or bad resource.
         */
        public static final int BAD_RESOURCE = -28;

        /**
         * Bad query criteria.
         */
        public static final int BAD_QUERY_CRITERIA = -8;

        /**
         * Invalid/Unexpected length (nr of elements) of a sequence.
         */
        public static final int BAD_SEQUENCE_LENGTH = -11;

        /**
         * A bind to an object or service failed
         */
        public static final int BIND_FAILURE = -27;

        /**
         * A communication failure
         */
        public static final int COMMUNICATION_FAILURE = -7;

        /**
         * A concurrent access error condition.
         */
        public static final int CONCURRENT_ACCESS_FAILURE = -20;

        /**
         * The creation of a resource failed
         */
        public static final int CREATION_FAILURE = -25;

        /**
         * Deactivation failure
         */
        public static final int DEACTIVATION_FAILURE = -33;

        /**
         * Duplicate element
         */
        public static final int DUPLICATE = -26;

        /**
         * This code is used for technical error conditions such as
         * NullPointerExceptions, ClassNotFoundExceptions, ... .
         */
        public static final int GENERIC = -23;

        /**
         * A HEURISTIC exception is thrown by the commit operation to indicate that
         * a heuristic decision was made and that some relevant updates have been
         * committed and others have been rolled back.
         */
        public static final int HEURISTIC = -43;

        /**
         * Signals that a method has been invoked at an illegal or
         * inappropriate time. In other words, the environment or application
         * is not in an appropriate state for the requested operation.
         */
        public static final int ILLEGAL_STATE = -6;

        /**
         * A initialization failure
         */
        public static final int INITIALIZATION_FAILURE = -3;

        /**
         * Invalid cardinality.
         */
        public static final int INVALID_CARDINALITY = -4;

        /**
         * Invalid configuration.
         */
        public static final int INVALID_CONFIGURATION = -32;

        /**
         * A transformation failure
         */
        public static final int TRANSFORMATION_FAILURE = -12;

        /**
         * Persistent media access error. Files, databases, or any other external
         * resource cannot be accessed
         */
        public static final int MEDIA_ACCESS_FAILURE = -13;

        /**
         * A resource does not exist.
         */
        public static final int NO_RESOURCE = -45;

        /**
         * Information is not available.
         * <p>
         * This exception code means that the request itself is valid but
         * the requested data is not available at the moment.
         * (A specific stock quote for example might be unavailable due to the
         * fact that corresponding market is not opened yet.)
         */
        public static final int NOT_AVAILABLE = -22;

        /**
         * An information or resource could not be found.
         */
        public static final int NOT_FOUND = -34;

        /**
         * Unimplemented operation or action.
         */
        public static final int NOT_IMPLEMENTED = -35;

        /**
         * No response available
         */
        public static final int NO_RESPONSE = -14;

        /**
         * Unsupported operation or action.
         */
        public static final int NOT_SUPPORTED = -36;

        /**
         * Parse error
         */
        public static final int PARSE_FAILURE = -37;

        /**
         * Processing failure
         */
        public static final int PROCESSING_FAILURE = -19;

        /**
         * The resource usage exceeded the allowed range
         */
        public static final int QUOTA_EXCEEDED = -15;

        /**
         * A ROLLBACK exception is thrown when the transaction has been marked for
         * rollback only or the transaction has been rolled back instead of
         * committed.
         */
        public static final int ROLLBACK = -42;

        /**
         * A security failure
         */
        public static final int SECURITY_FAILURE = -38;

        /**
         * A system exception (Corba, EJB, ...)
         */
        public static final int SYSTEM_EXCEPTION = -39;

        /**
         * A timeout.
         */
        public static final int TIMEOUT = -16;

        /**
         * The result of a query is too large to be handled.
         * <p>
         * This exception is thrown when a method produces a result that
         * exceeds a size-related limit. This can happen, for example, when
         * the size of the result exceeds some implementation-specific limit.
         */
        public static final int TOO_LARGE_RESULT_SET = -21;

        /**
         * The presence of a "TOO_MANY_EVENT_LISTENERS"
         * clause on any given concrete implementation of the normally multicast
         * "void addEventListener" event listener registration pattern is used
         * to annotate that interface as implementing a unicast Listener special
         * case, that is, that one and only one Listener may be registered on the
         * particular event listener source concurrently.
         */
        public static final int TOO_MANY_EVENT_LISTENERS = -40;

        /**
         * A transaction failure. Used with severe transaction manager problems. For
         * all other causes use specific exceptions like
         * <code>CONCURRENT_ACCESS_FAILURE</code> or <code>TIMEOUT</code>
         *
         * @see #CONCURRENT_ACCESS_FAILURE
         * @see #TIMEOUT
         */
        public static final int TRANSACTION_FAILURE = -17;

        /**
         * Validation failure
         */
        public static final int VALIDATION_FAILURE = -18;


        //------------------------------------------------------------------------
        // Friend of BaseException
        //------------------------------------------------------------------------

        /**
         * The BaseException's default mapper
         */
        static IntegerEnumeration.Mapper getMapper(
        ){
            return Code.mapper;
        }


        //------------------------------------------------------------------------
        // Extends IntegerEnumeration
        //------------------------------------------------------------------------

        /**
         * Finds the value corresponding to the String.
         * <p>
         * This method is case-sensitive.
         * 
         * @param string
         * 
         * @exception   IllegalArgumentException
         *              if no such value is found
         */
        public static int toValue(
            String string
        ){
            return Code.mapper.toValue(string);
        }

        /**
         * Converst a value to the corresponding to the String.
         * <p>
         * The value's numeric string representation is returned if it can't be found.
         */
        public static String toString(
            int value
        ){
            return Code.mapper.toString(value);
        }

        /**
         * 
         */
        protected static final ReflectiveMapper mapper = new ReflectiveMapper(
            IntegerEnumeration.mapper,
            Code.class
        );

    }

    
    //------------------------------------------------------------------------
    // Interface Mapper
    //------------------------------------------------------------------------
    
    /**
     * An <code>Mapper</code> implementation maps foreign exceptions to
     * <code>BasicException</code>s.
     *
     * <p>The default mapper operates on any <code>Throwable</code>
     * <p>The <code>BasicException</code> searches first for an
     * <code>Mapper</code> that matches the exception class itself, if
     * none is registered it searches recursively for a mapper that maps its
     * superclasses.
     */
    public interface Mapper {

        /**
         * Maps a <code>Throwable</code> to a <code>BasicException</code>.
         *
         * <p><p>Sample: Lets assume you've defined a PluginException as
         * <pre>
         *   public class PluginException extends Exception
         *   {
         *     public PluginException(
         *                String pluginId, String message, Throwable cause)
         *     {
         *       super(message);
         *       this.pluginId = pluginId;
         *       this.cause    = cause;
         *     }
         *
         *     public String toString()
         *     {
         *        return "PluginException: " + this.pluginID + " " + this.message;
         *     }
         *
         *     public String    getPluginId() { return this.pluginId; }
         *     public Throwable getCause()    { return this.cause; }
         *
         *     private String    pluginId;
         *     private String    message;
         *     private Throwable cause;
         *   }
         * </pre>
         *
         * <p><p>The associated mapper could look like:
         * <pre>
         *   public class PluginExceptionMapper implements Mapper
         *   {
         *     public BasicException map(Throwable throwable)
         *     {
         *       PluginException   pex = (PluginException)throwable;
         *
         *       String[] backTrace = BasicException.breakupStackTrace(
         *                                                           throwable);
         *
         *       HashMap map = BasicException.parseStackTraceEntry(
         *                                                        backTrace, 0);
         *
         *       // Note: This constructor maps nested cause Throwables for free
         *       BasicException se = new BasicException(
         *                                   pex.getCause(),
         *                                   (String)map.get("class"),
         *                                   (String)map.get("method"),
         *                                   ((Integer)map.get("line")).intValue(),
         *                                   BasicException.Code.DEFAULT_DOMAIN,
         *                                   BasicException.Code.GENERIC,
         *                                   new BasicException.Parameter[] {
         *                                     new BasicException.Parameter(
         *                                       "plugin-id",pex.getPluginId())
         *                                   }
         *                                   pex.getMessage(),
         *                                   backTrace,
         *                                   new Date());
         *     }
         *   }
         * </pre>
         *
         * <p><p>The following sample shows a 3-level BasicException
         * produced by the mapper
         * <pre>
         *      PluginException pe = new PluginException(
         *                                  CorbaExportObjectPlugin.PLUGIN_ID,
         *                                  "A plugin exception message",
         *                                  new IllegalArgumentException(
         *                                              "An exception message"));
         *
         *      BasicException se = new BasicException(
         *                                  pe,
         *                                  BasicException.Code.DEFAULT_DOMAIN,
         *                                  BasicException.Code.SYSTEM_EXCEPTION,
         *                                  new BasicException.Parameter[] { new BasicException.Parameter("0", 0) },
         *                                  "A stacked exception message");
         *
         *
         *      BasicException.Entry[0]
         *        Timestamp=2003-04-18 18:55:59.556
         *        Class=org.openmdx.sample.Test
         *        Method=init
         *        Line=128
         *        ExceptionDomain=DefaultDomain
         *        ExceptionCode=SYSTEM_EXCEPTION
         *        Param[0]: 0=0
         *        Description=A stacked exception message
         *        Backtrace:
         *          org.openmdx.sample.Test.init(Test.java:128)
         *          ...
         *          org.openmdx.sample.Test.main(Test.java:81)
         *
         *      BasicException.Entry[1]
         *        Timestamp=2003-04-18 18:55:59.556
         *        Class=org.openmdx.sample.Test
         *        Method=init
         *        Line=122
         *        ExceptionDomain=DefaultDomain
         *        ExceptionCode=GENERIC
         *        Param[0]: plugin-id=CorbaExportObject
         *        Description=org.openmdx.PluginException: A plugin exception message
         *        Backtrace:
         *          org.openmdx.sample.Test.init(Test.java:122)
         *          ...
         *          org.openmdx.sample.Test.main(Test.java:81)
         *
         *      BasicException.Entry[2]
         *        Timestamp=2003-04-18 18:55:59.556
         *        Class=org.openmdx.sample.Test
         *        Method=init
         *        Line=126
         *        ExceptionDomain=DefaultDomain
         *        ExceptionCode=GENERIC
         *        Param[0]: class=java.lang.IllegalArgumentException
         *        Description=An exception message
         *        Backtrace:
         *          org.openmdx.sample.Test.init(Test.java:126)
         *          ...
         *          org.openmdx.sample.Test.main(Test.java:81)
         * </pre>
         *
         *
         * @param throwable A throwable
         * @return  A mapped BasicException or null if the mapper cannot map
         * the Throwable
         */
        BasicException map(Throwable throwable);
    }


    //------------------------------------------------------------------------
    // Interface Wrapper
    //------------------------------------------------------------------------
    
    /**
     * The interface <code>Wrapper</code> defines an exception as a wrapping
     * exception that wraps a cause (a <code>BasicException</code>)
     *
     * <p>The default mapper knows the <code>Wrapper</code> interface
     * and extracts the wrapped BasicException when building an exception
     * stack.
     */
    public interface Wrapper
        extends MultiLineStringRepresentation
    {
        
        /**
         * Returns the detail message string of this Wrapper exception.
         * 
         * @return the detail message string of this Wrapper exception instance
         *           (which may be null).
         */
        String getMessage(
        );

        /**
         * Returns the wrapped <code>BasicException</code> exception.
         *
         * @return The wrapped exception.
         */
        BasicException getCause(
        );

        /**
         * Return a StackedException, this exception object's cause.
         * 
         * @return the StackedException wrapped by this object.
         * 
         * @deprecated use getCause()
         */
        public BasicException getExceptionStack();

        /**
         * Retrieves the exception domain for the wrapped 
         * <code>BasicException</code>.
         *
         * @return the domain value
         * 
         * @deprecated use getCause().getExceptionDomain()
         */
        String getExceptionDomain();

        /**
         * Retrieves the exception code for the wrapped 
         * <code>BasicException</code>.
         *
         * @return the error code
         * 
         * @deprecated use getCause().getExceptionCode()
         */
        int getExceptionCode();

        /**
         * Returns the cause belonging to a specific exception domain.
         * 
         * @param   exceptionDomain
         *          the desired exception domain,
         *          or <code>null</code> to retrieve the initial cause.
         *
         * @return  Either the cause belonging to a specific exception domain
         *          or the initial cause if <code>exceptionDomain</code> is
         *          <code>null</code>.  
         * 
         * @deprecated use getCause().getCause(java.lang.String)
         */
        BasicException getCause(
            String exceptionDomain
        );

    }

    
    //------------------------------------------------------------------------
    // Class Parameter
    //------------------------------------------------------------------------
    
    /**
     * The Parameter class represents a name-value pair. The name and value hold
     * internally are always of type string.
     */
    public static final class Parameter implements Serializable {

        /**
         * Creates a <code>Parameter</code> object. The constructor converts the passed
         * values to a <code>String</code> object.
         * <ul>
         * <li>  new Parameter("name", "text")
         * <li>  new Parameter("name", Integer.valueOf(1), Integer.valueOf(2))
         * </ul>
         *
         * @param name The property's name.
         * @param values The property's values are converted internally to a
         * <code>String</code>.
         */
        public Parameter(
            String name,
            Object... values
        ){
            this.name = name;
            this.value = values == null || values.length == 0 ? null : Arrays.deepToString(values);
        }

        /**
         * Creates a <code>Parameter</code> object. The constructor converts the passed
         * value to a <code>String</code> object.
         * <p>null objects are preserved in that the accessor <code>getValue</code>
         * returns a null object.
         *
         * <p>The constructor accepts various value types as <String>, array of
         * simple java types and a null object.
         * <ul>
         * <li>  new Parameter("name", "text")
         * <li>  new Parameter("name", new int[]{1,2,3})
         * <li>  new Parameter("name", new Integer[]{new Integer(1), new Integer(2))
         * <li>  new Parameter("name", null)
         * </ul>
         *
         * @param name The property's name.
         * @param value The property's value converted internally to a
         * <code>String</code>.
         */
        public Parameter(
            String name,
            Object value
        ) {
            this.name = name;
            this.value = 
                value == null ? null :
                    value instanceof Object[] ? Arrays.deepToString((Object[]) value) :
                        value.getClass().isArray() ? ArraysExtension.asList(value).toString() :
                            value.toString();
        }


        /**
         * Creates a <code>Parameter</code> object.
         *
         * @param name The property's name.
         * @param value The property's value converted internally to a
         * <code>String</code>.
         */
        public Parameter(
            String name,
            long value)
        {
            this.name = name;
            this.value = String.valueOf(value);
        }


        /**
         * Creates a <code>Parameter</code> object.
         *
         * @param name The property's name.
         * @param value The property's value converted internally to a
         * <code>String</code>.
         */
        public Parameter(
            String name,
            int value)
        {
            this.name = name;
            this.value = String.valueOf(value);
        }


        /**
         * Creates a <code>Parameter</code> object.
         *
         * @param name The property's name.
         * @param value The property's value converted internally to a
         * <code>String</code>.
         */
        public Parameter(
            String name,
            short value)
        {
            this.name = name;
            this.value = String.valueOf(value);
        }


        /**
         * Creates a <code>Parameter</code> object.
         *
         * @param name The property's name.
         * @param value The property's value converted internally to a
         * <code>String</code>.
         */
        public Parameter(
            String name,
            byte value)
        {
            this.name = name;
            this.value = String.valueOf(value);
        }


        /**
         * Creates a <code>Parameter</code> object.
         *
         * @param name The property's name.
         * @param value The property's value converted internally to a
         * <code>String</code>.
         */
        public Parameter(
            String name,
            char value)
        {
            this.name = name;
            this.value = String.valueOf(value);
        }


        /**
         * Creates a <code>Parameter</code> object.
         *
         * @param name The property's name.
         * @param value The property's value converted internally to a
         * <code>String</code>.
         */
        public Parameter(
            String name,
            boolean value)
        {
            this.name = name;
            this.value = String.valueOf(value);
        }

        /**
         * Creates a <code>Parameter</code> object.
         * 
         * @param name The parameter's name.
         * @param duration The parameter's duration value.
         * @param unit The parameter's time unit.
         */
        public Parameter(
            String name,
            long duration,
            TimeUnit unit
        ){
            this.name = name;
            this.value = new StringBuilder(
            ).append(
                duration
            ).append(
                ' '
            ).append(
                unit
            ).toString();
        }

        /**
         * Creates a <code>Parameter</code> object.
         *
         * @param name The property's name.
         * @param value The property's value converted internally to a
         * <code>String</code>.
         */
        public Parameter(
            String name,
            double value)
        {
            this.name = name;
            this.value = String.valueOf(value);
        }


        /**
         * Creates a <code>Parameter</code> object.
         *
         * @param name The property's name.
         * @param value The property's value converted internally to a
         * <code>String</code>.
         */
        public Parameter(
            String name,
            float value)
        {
            this.name = name;
            this.value = String.valueOf(value);
        }


        /**
         * Returns the property name
         *
         * @return String
         */
        public final String getName()
        {
            return this.name;
        }


        /**
         * Returns the property value
         *
         * @return String
         */
        public final String getValue()
        {
            return this.value;
        }


        /**
         * Indicates whether some other object is "equal to" this one.
         *
         * @param   object  the reference object with which to compare.
         * @return  true if this object is the same as the object argument;
         * false otherwise.
         */
        public boolean equals (Object object)
        {
            if (this == object) return true;
            if (! (object instanceof Parameter)) return false;
            final Parameter that = (Parameter)object;
            return (this.name.equals(that.name) && this.value.equals(that.value));
        }


        /**
         * Returns a string representation of the <code>Parameter</code> object.
         *
         * <p>Format:  "<name>=<value>"
         *
         * @return a String
         */
        public String toString()
        {
            return this.name + '=' + this.value;
        }


        /**
         * Returns a hash code value for the object. This method is supported for
         * the benefit of hashtables such as those provided by
         * java.util.Hashtable.
         *
         * @return a hash code value for this object.
         */
        public int hashCode()
        {
            return this.name.hashCode();
        }


        /**
         * Add an Parameter array to another Parameter array
         *
         * @param first
         * @param second Add this array to the array named 'first'
         * @return Parameter[]
         */
        public static Parameter[] add(Parameter[] first, Parameter[] second)
        {
            if (first == null) {
                return second;
            }else{
                if (second == null) {
                    return first;
                }else{
                    Parameter[] newArr = new Parameter[first.length + second.length];

                    System.arraycopy(first,0,newArr,0, first.length);
                    System.arraycopy(second,0,newArr, first.length, second.length);
                    return newArr;
                }
            }
        }


        /**
         * The serial version UID
         */
        private static final long serialVersionUID = -7161563495226434698L;

        /**
         * @serial The parameter name
         */
        final String name;

        /**
         * @serial The parameter value
         */
        final String value;

        /**
         * Name of the parameter representing the excption's java class.
         */
        final static public String EXCEPTION_CLASS = "exception.class";

        /**
         * Name of the parameter representing the excption's source set by
         * {link @see BasicException#setSource(java.lang.Object) setSource()}
         */
        final static public String EXCEPTION_SOURCE = "exception.source";

    }

}
