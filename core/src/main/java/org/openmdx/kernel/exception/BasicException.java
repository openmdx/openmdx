/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Basic Exception
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
 * This product includes or is based on software developed by other
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.kernel.exception;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.openmdx.kernel.collection.ArraysExtension;
import org.openmdx.kernel.log.ForeignLogRecord;
import org.openmdx.kernel.log.LoggerFactory;

/**
 * An exception stack is a linked list of {@code BasicException}s.
 * A {@code BasicException.Holder} has an exception stack as member<ul>
 * <li>The exception stack's first element represents its holder
 * <li>The exception stack's last element represents the initial exception 
 * </ul>
 * <p>
 * Each {@code BasicException} provides the following information:<ul>
 * <li> an exception domain
 * <li> an exception code
 * <li> a description
 * <li> optional exception parameters
 * <li> an optional time stamp
 * <li> an optional exception class
 * <li> an optional link to the next element
 * </ul>
 * <p>
 * <em>Note:<br>
 * A {@code BasicException} does not clone exception stacks it accepts
 * or returns (as opposed to other {@code Throwable}'s behaviour. 
 * 
 */
public final class BasicException extends Exception {

    /**
     * Creates a stand-alone exception stack
     *
     * @param cause An embedded exception
     * @param exceptionDomain An exception domain. A null objects references
     * the default exception domain with negative exception codes only.
     * @param exceptionCode  An exception code. Negative codes describe common
     * exceptions codes. Positive exception codes are specific for a given
     * exception domain.
     * @param description A readable description
     * @param parameters  Any exception parameters
     */
    private BasicException(
        Throwable cause,
        String exceptionDomain,
        int exceptionCode,
        String description,
        Parameter... parameters
    ){
        super();
        this.source = null;
        this.domain = exceptionDomain;
        this.code = exceptionCode;
        this.description = description;
        this.parameter = parameters;
        this.timestamp = System.currentTimeMillis();
        if(cause != null) {
            initCauseInternal(cause);
        }
    }

    /**
     * Creates a {@code BasicException} to be completed by {@code initHolder}.
     *
     * @param cause An embedded exception
     * @param exceptionDomain An exception domain. A null objects references
     * the default exception domain with negative exception codes only.
     * @param exceptionCode  An exception code. Negative codes describe common
     * exceptions codes. Positive exception codes are specific for a given
     * exception domain.
     * @param parameters  Any exception parameters
     */
    private BasicException(
        BasicException cause,
        String exceptionDomain,
        int exceptionCode,
        Parameter... parameters
    ){
        super();
        this.source = null;
        this.domain = exceptionDomain;
        this.code = exceptionCode;
        this.description = null;
        this.parameter = parameters;
        this.timestamp = System.currentTimeMillis();
        if(cause != null) {
            initCauseInternal(cause);
        }
    }
    
    /**
     * Creates the exception stack for a {@code BasicException} holder.
     * @param cause the optional cause
     * @param exceptionDomain the mandatory exception domain
     * @param exceptionCode the mandatory exception code
     * @param parameters the optional parameters
     * @param description an optional description
     * @param holder the mandatory {@code BasicException} holder
     */
    public BasicException(
        Throwable cause,
        String exceptionDomain,
        int exceptionCode,
        Parameter[] parameters,
        String description,
        Throwable holder
    ) {
        super();
        this.source = holder;
        this.domain = exceptionDomain;
        this.code = exceptionCode;
        this.description = description;
        this.parameter = parameters;
        this.timestamp = System.currentTimeMillis();
        if(cause != null) {
            initCauseInternal(cause);
        }
    }

    /**
     * Constructor for the XML parser
     *
     * @param exceptionDomain
     * @param exceptionCode
     * @param exceptionClass
     * @param exceptionTime
     * @param exceptionMethod
     * @param exceptionLine
     * @param description
     * @param parameters
     */
    public BasicException(
        String exceptionDomain,
        int exceptionCode,
        String exceptionClass,
        #if CLASSIC_CHRONO_TYPES java.util.Date #else Instant #endif exceptionTime,
        String exceptionMethod,
        Integer exceptionLine,
        String description,
        Parameter[] parameters
    ){
        super();
        this.source = null;
        this.domain = exceptionDomain;
        this.code = exceptionCode;
        this.exceptionClass = exceptionClass;
        this.timestamp = exceptionTime == null ? Long.MIN_VALUE : exceptionTime.#if CLASSIC_CHRONO_TYPES getTime() #else toEpochMilli() #endif;
        this.methodName = exceptionMethod;
        this.lineNumber = exceptionLine == null ? -1 : exceptionLine.intValue();
        this.description = description;
        this.parameter = parameters;
    }

    /**
     * Constructor 
     *
     * @param throwable
     */
    private BasicException(
        Throwable throwable
    ){
        super();
        this.source = throwable;
        this.domain = Code.DEFAULT_DOMAIN;
        this.code = Code.GENERIC;
        this.parameter = null;
        this.timestamp = Long.MIN_VALUE; // we do not know when the exception has been thrown
    }
    
    /**
     * The holder will not be serialized
     */
    private transient Throwable source;
    
    /**
     * A representation of the exception stack
     */
    private transient Iterable<BasicException> exceptionStack = null;

    /**
     * The source's class
     */
    private String exceptionClass = null;
    
    /**
     * @serial The exception domain
     */
    private final String domain;
    
    /**
     * The exception code
     */
    private final int code;
    
    /**
     * The date and time when the exception has been created
     */
    private final long timestamp;

    /**
     * The exception description is either set upon construction or 
     * lazily retrieved from the source
     */
    private String description;
    
    /**
     * 
     */
    private Parameter[] parameter;
    
    /**
     * The message containing the domain and the exception code separated by 
     * a dot is built lazily.
     */
    private String message = null;
    
    /**
     * 
     */
    private String methodName = null;
    
    /**
     * 
     */
    private int lineNumber = -1;
    
    /**
     * The stack trace is lazily retrieved from the throwable
     */
    private StackTraceElement[] stackTrace = null;
        
    /**
     * Implements {@code Serializable}
     */
    private static final long serialVersionUID = -1081067273393341482L;

    /**
     * To format the timestamp
     */
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    
    /**
     * Exception Mappers
     */
    private static final Mapper mappers = new Mappers();
    
    /**
     * Lazy initialization leads to an empty parameter list
     */
    private static final Parameter[] NO_PARAMETERS = {};

    /**
     * Lazy initialization leads to an empty stack trace
     */
    private static final StackTraceElement[] NO_STACK_TRACE = {};
    
    /**
     * The environment specific line separator
     */
    static final String lineSeparator;
    
    static {
        dateFormat.setLenient(false);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        lineSeparator = getProperty("line.separator", "\n");
    }

    /**
     * Lenient System Property retrieval
     * 
     * @param key
     * @param defaultValue
     * 
     * @return the system property value or its default value
     */
    private static final String getProperty(
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
     * Log a {@code BasicException.Holder}
     * 
     * @param holder
     */
    public static <T extends Holder> T log(
        T holder
    ){
        Logger logger = LoggerFactory.getLogger();
        if(logger.isLoggable(Level.WARNING)) {
            LogRecord record = new ForeignLogRecord(
                logger.getName(), 
                holder.getClass().getName(), 
                Level.WARNING, 
                holder.getCause().getDescription()
            );
            // If we used resource bundles we would them set here
            record.setThrown((Throwable)holder);
            logger.log(record);
        }
        return holder;
    }

    /**
     * Creates an exception stack to be initialized by {@code initHolder}.
     *
     * @param cause An embedded exception
     */
    public static BasicException newEmbeddedExceptionStack(
        Throwable cause
    ){
        BasicException exceptionStack = toExceptionStack(cause);
        return new BasicException(
            exceptionStack,
            exceptionStack.getExceptionDomain(),
            exceptionStack.getExceptionCode()
        );            
    }
    
    /**
     * Creates an exception stack to be initialized by {@code initHolder}.
     *
     * @param cause An embedded exception
     * @param exceptionDomain An exception domain. A null objects references
     * the default exception domain with negative exception codes only.
     * @param exceptionCode  An exception code. Negative codes describe common
     * exceptions codes. Positive exception codes are specific for a given
     * exception domain.
     * @param parameters  Any exception parameters
     */
    public static BasicException newEmbeddedExceptionStack(
        Throwable cause,
        String exceptionDomain,
        int exceptionCode,
        Parameter... parameters
    ){
        return new BasicException(
            toExceptionStack(cause),
            exceptionDomain,
            exceptionCode,
            parameters
        );            
    }

    /**
     * Creates an exception stack to be initialized by {@code initHolder}.
     *
     * @param exceptionDomain An exception domain. A null objects references
     * the default exception domain with negative exception codes only.
     * @param exceptionCode  An exception code. Negative codes describe common
     * exceptions codes. Positive exception codes are specific for a given
     * exception domain.
     * @param parameters  Any exception parameters
     */
    public static BasicException newEmbeddedExceptionStack(
        String exceptionDomain,
        int exceptionCode,
        Parameter... parameters
    ){
        return new BasicException(
            null, // cause
            exceptionDomain,
            exceptionCode,
            parameters
        );            
    }

    
    /**
     * Creates an exception stack for kernel classes.
     *
     * @param cause An embedded exception
     * @param exceptionDomain An exception domain. A null objects references
     * the default exception domain with negative exception codes only.
     * @param exceptionCode  An exception code. Negative codes describe common
     * exceptions codes. Positive exception codes are specific for a given
     * exception domain.
     * @param description A readable description
     * @param parameters  Any exception parameters
     */
    public static BasicException newStandAloneExceptionStack(
        Throwable cause,
        String exceptionDomain,
        int exceptionCode,
        String description,
        Parameter... parameters
    ){
        return new BasicException(
            cause,
            exceptionDomain,
            exceptionCode,
            description,
            parameters
        );
    }

    /**
     * Creates an exception stack for kernel classes.
     *
     * @param exceptionDomain An exception domain. A null objects references
     * the default exception domain with negative exception codes only.
     * @param exceptionCode  An exception code. Negative codes describe common
     * exceptions codes. Positive exception codes are specific for a given
     * exception domain.
     * @param description A readable description
     * @param parameters  Any exception parameters
     */
    public static BasicException newStandAloneExceptionStack(
        String exceptionDomain,
        int exceptionCode,
        String description,
        Parameter... parameters
    ){
        return new BasicException(
            null, // 
            exceptionDomain,
            exceptionCode,
            description,
            parameters
        );
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.kernel.exception.ExceptionStack#getStackTrace()
     */
    @Override
    public final StackTraceElement[] getStackTrace() {
        if(this.stackTrace == null) {
            this.stackTrace = this.source == null ? NO_STACK_TRACE : this.source.getStackTrace();
        }
        return this.stackTrace;
    }

    /**
     * Retrieve the stack trace
     * 
     * @param lazily if {@code true} {@code null} is returned unless
     * the information is already available
     * 
     * @return the stack trace
     */
    private final StackTraceElement[] getStackTrace(
        boolean lazily
    ) {
        return this.stackTrace == null && lazily ? NO_STACK_TRACE : getStackTrace();
    }

    /**
     * Associate the cause, a {@code BasicException} with its holder
     *
     * @param throwable
     *
     * @return the throwable
     *
     * @throws IllegalStateException if the {@code BasicException} is already connected with a holder
     */
    public static <T extends Throwable> T initHolder(
        T throwable
    ){
        BasicException cause = (BasicException) throwable.getCause();
        cause.source = throwable;
        return throwable;
    }
   
    /**
     * Initialize the cause
     * 
     * @param throwable the throwable to be converted into an exception stack
     * 
     * @return the newly appended exception stack
     */
    private BasicException initCauseInternal(
        Throwable throwable
    ){
        BasicException exceptionStack = toExceptionStack(throwable);
        super.initCause(
            this == exceptionStack.getInitialCause() ? new BasicException(
                null, // cause
                Code.DEFAULT_DOMAIN,
                Code.ASSERTION_FAILURE,
                "Detected a recursion within the exception stack. " +
                "The exception is fixed but some information had to be discarded."
            ) : exceptionStack
        );
        return exceptionStack;
        
    }
    
    
    
    /**
     * Initializes the cause of this throwable to the specified value. 
     * (The cause is the throwable that caused this throwable to get thrown.)
     * 
     * The cause can only be set once.
     * 
     * @param throwable
     * @return A reference to this {@code Throwable} instance
     * @throws IllegalArgumentException if cause is this throwable. 
     * (A throwable cannot be its own cause.)
     * @throws IllegalStateException if the cause has already been set
     */
    @Override
    public synchronized BasicException initCause(
        Throwable throwable
    ){
        initCauseInternal(throwable);
        return this;
    }

    /* (non-Javadoc)
     * @see java.lang.Throwable#fillInStackTrace()
     */
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this; // No stack trace required
    }

    /* (non-Javadoc)
     * @see java.lang.Throwable#getCause()
     */
    @Override
    public final synchronized BasicException getCause() {
        BasicException cause = (BasicException) super.getCause();
        if(cause == null && this.source != null && this.timestamp == Long.MIN_VALUE) {
            Throwable source = this.source.getCause();
            if(source != null) {
                return initCauseInternal(source);
            }
        }
        return cause;
    }

    /**
     * Maps a {@code Throwable} to a {@code BasicException} using the 
     * registered exception mappers
     *
     * @param throwable
     * 
     * @return a {@code BasicException} or {@code null} if there is 
     * no mapping
     */
    public static BasicException toExceptionStack(
        Throwable throwable
    ) {
        if(throwable == null || throwable instanceof BasicException){
            return (BasicException) throwable;
        }
        Throwable cause = throwable.getCause();
        if(cause instanceof BasicException) {
            return (BasicException) cause;
        }
        BasicException basicException = mappers.map(throwable);
        return basicException == null ? new BasicException(throwable) : basicException;
    }

    /**
     * Create a {@code BasicException} representing a {@code cause} 
     * wrapped into a {@code wrapper}.
     * 
     * @param cause
     * @param holder
     * @param exceptionDomain 
     * @param exceptionCode 
     * @param description 
     *
     * @return a {@code BasicException} representing the {@code cause} 
     * wrapped into {@code wrapper}
     */
    public static BasicException toStackedException(
        Throwable cause,
        Throwable holder,
        String exceptionDomain,
        int exceptionCode, 
        String description, 
        Parameter... parameters 
    ){
        return new BasicException(
            toExceptionStack(cause),
            exceptionDomain,
            exceptionCode,
            parameters,
            description,
            holder
        );
    }

    /**
     * Retrieve the initial cause
     * 
     * @return initial cause
     */
    private BasicException getInitialCause(
    ){
        BasicException initialCause = null;
        for(
            BasicException element = this;
            element != null;
            element = element.getCause()
        ){
            initialCause = element;
        }
        return initialCause;
    }
    
    /* (non-Javadoc)
     * @see Holder#getCause(java.lang.String)
     */
    public BasicException getCause(String exceptionDomain) {
        if(exceptionDomain == null) {
            return getInitialCause();
        } else {
            for(
                BasicException element = this;
                element != null;
                element = element.getCause()
            ){
                if(exceptionDomain.equals(element.getExceptionDomain())) {
                    return element;
                }
            }
            return null;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Throwable#getMessage()
     */
    @Override
    public String getMessage() {
        if(this.message == null) {
            this.message = this.domain + '.' + Code.toString(this.domain, this.code); 
        }
        return this.message;
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.exception.ExceptionStack#getDescription()
     */
    public String getDescription() {
        if(this.description == null && this.source != null && !(source instanceof Holder)) {
            this.description = this.source.getMessage();
        }
        return this.description;
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.exception.ExceptionStack#getExceptionCode()
     */
    public int getExceptionCode() {
        return this.code;
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.exception.ExceptionStack#getExceptionDomain()
     */
    public String getExceptionDomain() {
        return this.domain;
    }

    /**
     * The name of the exception class represented by this basic exception
     * 
     * @return the name of the exception class 
     */
    public String getExceptionClass() {
        if(this.exceptionClass == null) {
            this.exceptionClass = (this.source == null ? this : this.source).getClass().getName();
        }
        return this.exceptionClass;
    }
    
    /**
     * Retrieves the class for this {@code BasicException} object.
     *
     * @return the class
     */
    public String getClassName() {
        StackTraceElement[] stackTrace = getStackTrace();
        return stackTrace.length > 0 ? stackTrace[0].getClassName() : null;
    }

    /**
     * Retrieves the method for this {@code BasicException} object.
     * 
     * @param lazily if {@code true} {@code null} is returned unless
     * the information is already available
     *
     * @return the method
     */
    public String getMethodName(
        boolean lazily
    ){
        if(this.methodName == null) {
            StackTraceElement[] stackTrace = getStackTrace(lazily);
            this.methodName = stackTrace.length > 0 ? stackTrace[0].getMethodName() : null;
        }
        return this.methodName;
    }


    /**
     * Retrieves the line number for this {@code BasicException} object.
     * 
     * @param lazily if {@code true} {@code null} is returned unless
     * the information is already available
     *
     * @return the line nr
     */
    public Integer getLineNr(
        boolean lazily
    ){
        if(this.lineNumber < 0) {
            StackTraceElement[] stackTrace = getStackTrace(lazily);
            this.lineNumber = stackTrace.length > 0 ? stackTrace[0].getLineNumber() : -1;
        }
        return this.lineNumber < 0 ? null : Integer.valueOf(this.lineNumber);
    }

    /**
     * Retrieves the timestamp for this {@code BasicException} toplevel
     * object.
     *
     * @return the timestamp
     */
    public #if CLASSIC_CHRONO_TYPES java.util.Date #else Instant #endif getTimestamp(
    ){
        return this.timestamp == Long.MIN_VALUE ? null : #if CLASSIC_CHRONO_TYPES new java.util.Date #else Instant.ofEpochMilli#endif(this.timestamp);
    }

    public Parameter[] getParameters(){
        return this.parameter == null ? NO_PARAMETERS : this.parameter;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.kernel.exception.ExceptionStack#getParameter(java.lang.String)
     */
    public String getParameter(String name) {
        for(Parameter parameter : getParameters()) {
            if(name == null ? parameter.getName() == null : name.equals(parameter.getName())) {
                return parameter.getValue();
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.openmdx.kernel.exception.ExceptionStackElement#getExceptionStack()
     */
    public Iterable<BasicException> getExceptionStack() {
        if(this.exceptionStack == null) {
            this.exceptionStack = new Iterable<BasicException>() {

                public Iterator<BasicException> iterator() {
                    return new ElementIterator(BasicException.this);
                }
                
            };
        }
        return this.exceptionStack;
    }

    /* (non-Javadoc)
     * @see java.lang.Throwable#toString()
     */
    @Override
    public String toString() {
    	final StringBuilder out = new StringBuilder(getExceptionClass());
    	try {
	        printProperties(
	        	new Streamable(out) {

	        		@Override
	                void println() {
	        			out.append(lineSeparator);
	                }
	        		
	        	}
	        );
        } catch (IOException ignore) {
            // Ignore I/O Exceptions
        }
    	return out.toString();
    }

	/**
     * Print the whole stack trace
     * 
     * @param out the target
     */
    private void printProperties(
        Streamable out
    ) throws IOException {
        out.println(); 
        out.append("\tExceptionDomain = ").append(this.domain);
        out.println();
        out.append("\tExceptionCode = ").append(Code.toString(this.domain,this.code));
        out.println();
        String description = this.getDescription();
        if(description != null) {
            out.append("\tDescription = ").append(description);
            out.println(); 
        }       
        #if CLASSIC_CHRONO_TYPES java.util.Date #else Instant #endif thrownAt = this.getTimestamp();
        if(thrownAt != null) synchronized(BasicException.dateFormat) {
            out.append("\tTimestamp = ").append(BasicException.dateFormat.format(thrownAt));
            out.println();
        }
        int i = 0;
        for(Parameter parameter : this.getParameters()) {
            if(i == 0) {
                out.append("\tParameters:");
                out.println();
            }
            out.append("\t    ").append(String.valueOf(i++)).append(":\t").append(parameter == null ? "N/A" : parameter.toString());
            out.println();
        }
    }
    
	/**
     * Print the whole stack trace
     * 
	 * @param holderClassName the name of the BasicException.Holder implementation class
	 * @param out the target
     */
    private void printExceptionStack(
        String holderClassName, 
        Streamable out
    ){
        try {
            if(holderClassName != null) {
                out.append(holderClassName).append(": ");
            }
            out.append(getExceptionClass()).append(": ").append(getMessage());
            out.println();
            int exceptionStackElement = 0;
            for(
                BasicException element = this;
                element != null;
                element = element.getCause()
            ){
                out.append("    BasicException[").append(String.valueOf(exceptionStackElement++)).append(']');
                out.println();
                StackTraceElement[] stackTrace = element.getStackTrace();
                if(stackTrace.length > 0) {
                    out.append("\tClass = ").append(stackTrace[0].getClassName());
                    out.println(); 
                    out.append("\tMethod = ").append(stackTrace[0].getMethodName());
                    out.println();
                    out.append("\tLine = ").append(String.valueOf(stackTrace[0].getLineNumber()));
                    out.println(); 
                }
                out.append("\tExceptionClass = ").append(element.getExceptionClass());
                element.printProperties(out);
                if(stackTrace.length > 0) {
                    out.append("\tStackTrace:");
                    out.println();
                    for(StackTraceElement stackTraceElement : stackTrace) {
                        out.append("\t    at ").append(stackTraceElement.toString());
                        out.println();
                    }
                }
            }
        } catch (IOException ignore) {
            // Ignore I/O Exceptions
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Throwable#printStackTrace(java.io.PrintStream)
     */
    @Override
    public void printStackTrace(final PrintStream s) {
        printStackTrace(null, s);
    }

    /**
     * Allows to inject the BasicException holder class name
     * 
     * @param holderClassName the BasicException's holder calss
     * @param s the target stream
     */
    public void printStackTrace(
        final String holderClassName,
        final PrintStream s
    ) {
        synchronized (s) {
            printExceptionStack(
                holderClassName, 
                new Streamable(s) {

                    @Override
                    void println() {
                        s.println();
                    }
                    
                }
            );
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Throwable#printStackTrace(java.io.PrintWriter)
     */
    @Override
    public void printStackTrace(final PrintWriter s) {
        printStackTrace(null, s);
    }

    /**
     * Allows to inject the BasicException holder class name
     * 
     * @param holderClassName the BasicException's holder calss
     * @param s the target stream
     */
    public void printStackTrace(
        final String holderClassName,
        final PrintWriter s
    ) {
        synchronized (s) {
            printExceptionStack(
                holderClassName, 
                new Streamable(s) {

                    @Override
                    void println() {
                        s.println();
                    }
                    
                }
            );
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Throwable#setStackTrace(java.lang.StackTraceElement[])
     */
    @Override
    public void setStackTrace(StackTraceElement[] stackTrace) {
        this.stackTrace = stackTrace;
    }    

    //------------------------------------------------------------------------
    // Implements {@code Serializable}
    //------------------------------------------------------------------------

    private void writeObject(
        ObjectOutputStream out
    ) throws IOException {
        this.getCause();
        this.getMessage();
        this.getDescription();
        this.getStackTrace();
        this.getParameters();
        out.defaultWriteObject();
    }


    //------------------------------------------------------------------------
    // Class Streamable
    //------------------------------------------------------------------------

    /**
     * Streamable
     */
    abstract static class Streamable implements Appendable {

        /**
         * Constructor 
         *
         * @param delegate
         */
        Streamable(
            Appendable delegate
        ){
            this.delegate = delegate;
        }
        
        /**
         * The delegate
         */
        private final Appendable delegate;
        
        /**
         * The new-line sequence may be delegate specific
         */
        abstract void println();
        
        /* (non-Javadoc)
         * @see java.lang.Appendable#append(java.lang.CharSequence)
         */
        public Appendable append(
            CharSequence csq
        ) throws IOException {
            return this.delegate.append(csq);
        }

        /* (non-Javadoc)
         * @see java.lang.Appendable#append(char)
         */
        public Appendable append(
            char c
        ) throws IOException {
            return this.delegate.append(c);
        }

        /* (non-Javadoc)
         * @see java.lang.Appendable#append(java.lang.CharSequence, int, int)
         */
        public Appendable append(
            CharSequence csq, 
            int start, 
            int end
        ) throws IOException {
            return this.delegate.append(csq, start, end);
        }
    
        
    }    
        
    //------------------------------------------------------------------------
    // Class Code
    //------------------------------------------------------------------------
    
    /**
     * The BasicException's DEFAULT_DOMAIN
     */
    public static class Code {

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
<         */
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
         * {@code CONCURRENT_ACCESS_FAILURE} or {@code TIMEOUT}
         *
         * @see #CONCURRENT_ACCESS_FAILURE
         * @see #TIMEOUT
         */
        public static final int TRANSACTION_FAILURE = -17;

        /**
         * Validation failure
         */
        public static final int VALIDATION_FAILURE = -18;

        /**
         * Exception Domains
         */
        private static final Domains domains = new Domains();

        /**
         * Retrieve the string representation of an exception code
         * 
         * @param exceptionDomain
         * @param exceptionCode
         * 
         * @return the string representation of an exception code
         */
        public static String toString(
            String exceptionDomain,
            int exceptionCode
        ){
            return domains.toString(exceptionDomain, exceptionCode);
        }
        
    }

    
    //------------------------------------------------------------------------
    // Interface Mapper
    //------------------------------------------------------------------------
    
    /**
     * A {@code Mapper} implementation maps <cod>Throwable}s to
     * {@code BasicException}s.
     */
    public interface Mapper {

        /**
         * Map a {@code Throwable} to a {@code BasicException}
         * 
         * @param throwable A throwable
         * @return  A {@code BasicException}; or {@code null} if the 
         * mapper cannot map the {@code Throwable}
         */
       BasicException map(Throwable throwable);

    }

    
    //------------------------------------------------------------------------
    // Interface Holder
    //------------------------------------------------------------------------
    
    /**
     * The interface {@code Holder} defines an exception holding an 
     * exception stack, i.e. its chain of causes consists of 
     * {@code BasicException}s.
     */
    public interface Holder {
        
        /**
         * Returns the wrapped {@code BasicException} exception.
         *
         * @return The wrapped exception.
         */
        BasicException getCause(
        );

        /**
         * Selects a domain specific exception stack element
         * 
         * @param exceptionDomain requested domain, or {@code null} to retrieve the initial cause.
         *
         * @return the first exception stack element for the requested domain, 
         * or {@code null} if no such element exists
         */
        BasicException getCause(
            String exceptionDomain
        );
        
        /**
         * Retrieves the exception domain of this {@code ServiceException}.
         *
         * @return the exception domain
         */
        String getExceptionDomain();

        /**
         * Retrieves the exception code of this {@code ServiceException}.
         *
         * @return the exception code
         */
        int getExceptionCode();
        
        /**
         * Log the exception at warning level
         * 
         * @return the exception itself
         */
        Holder log();
        
        /**
         * This method is required by the log handler
         * 
         * @return the exception's message
         */
        String getMessage();

    }
    
    
    //------------------------------------------------------------------------
    // Class ElementIterator
    //------------------------------------------------------------------------
    
    /**
     * Element Iterator
     */
    static class ElementIterator implements Iterator<BasicException> {

        /**
         * Constructor 
         *
         * @param exceptionStack
         */
        ElementIterator(
            BasicException exceptionStack
        ){
            this.element = exceptionStack;
        }

        /**
         * The exception stack cursor
         */
        private BasicException element;
        
        /* (non-Javadoc)
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext() {
            return element != null;
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#next()
         */
        public BasicException next() {
            BasicException element = this.element;
            this.element = element.getCause();
            return element;
        }

        /* (non-Javadoc)
         * @see java.util.Iterator#remove()
         */
        public void remove() {
            throw new UnsupportedOperationException(
                "The exception stack is unmodifiable"
            );
        }
        
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
         * Creates a {@code Parameter} object. The constructor converts the passed
         * values to a {@code String} object.
         * <ul>
         * <li>  new Parameter("name", "text")
         * <li>  new Parameter("name", Integer.valueOf(1), Integer.valueOf(2))
         * </ul>
         *
         * @param name The property's name.
         * @param values The property's values are converted internally to a
         * {@code String}.
         */
        public Parameter(
            String name,
            Object... values
        ){
            this.name = name;
            this.value = values == null || values.length == 0 ? null : Arrays.deepToString(values);
        }

        /**
         * Creates a {@code Parameter} object. The constructor converts the passed
         * value to a {@code String} object.
         * <p>null objects are preserved in that the accessor {@code getValue}
         * returns a null object.
         *
         * <p>The constructor accepts various value types as <String>, array of
         * simple java types and a null object.
         * <ul>
         * <li>  new Parameter("name", "text")
         * <li>  new Parameter("name", new int[]{1,2,3})
         * <li>  new Parameter("name", new Integer[]{Integer.valueOf(1), Integer.valueOf(2))
         * <li>  new Parameter("name", null)
         * </ul>
         *
         * @param name The property's name.
         * @param value The property's value converted internally to a
         * {@code String}.
         */
        public Parameter(
            String name,
            Object value
        ) {
            this.name = name;
            this.value = 
                value == null ? null :
                value instanceof Object[] ? Arrays.deepToString((Object[]) value) :
                value instanceof List ? Arrays.deepToString(((List<?>)value).toArray()) :
                value.getClass().isArray() ? ArraysExtension.asList(value).toString() :
                value.toString();
        }


        /**
         * Creates a {@code Parameter} object.
         *
         * @param name The property's name.
         * @param value The property's value converted internally to a
         * {@code String}.
         */
        public Parameter(
            String name,
            long value
        ){
            this.name = name;
            this.value = String.valueOf(value);
        }


        /**
         * Creates a {@code Parameter} object.
         *
         * @param name The property's name.
         * @param value The property's value converted internally to a
         * {@code String}.
         */
        public Parameter(
            String name,
            int value
        ){
            this.name = name;
            this.value = String.valueOf(value);
        }


        /**
         * Creates a {@code Parameter} object.
         *
         * @param name The property's name.
         * @param value The property's value converted internally to a
         * {@code String}.
         */
        public Parameter(
            String name,
            short value
        ){
            this.name = name;
            this.value = String.valueOf(value);
        }


        /**
         * Creates a {@code Parameter} object.
         *
         * @param name The property's name.
         * @param value The property's value converted internally to a
         * {@code String}.
         */
        public Parameter(
            String name,
            byte value
        ){
            this.name = name;
            this.value = String.valueOf(value);
        }


        /**
         * Creates a {@code Parameter} object.
         *
         * @param name The property's name.
         * @param value The property's value converted internally to a
         * {@code String}.
         */
        public Parameter(
            String name,
            char value
        ){
            this.name = name;
            this.value = String.valueOf(value);
        }


        /**
         * Creates a {@code Parameter} object.
         *
         * @param name The property's name.
         * @param value The property's value converted internally to a
         * {@code String}.
         */
        public Parameter(
            String name,
            boolean value
        ){
            this.name = name;
            this.value = String.valueOf(value);
        }

        /**
         * Creates a {@code Parameter} object.
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
         * Creates a {@code Parameter} object.
         *
         * @param name The property's name.
         * @param value The property's value converted internally to a
         * {@code String}.
         */
        public Parameter(
            String name,
            double value)
        {
            this.name = name;
            this.value = String.valueOf(value);
        }


        /**
         * Creates a {@code Parameter} object.
         *
         * @param name The property's name.
         * @param value The property's value converted internally to a
         * {@code String}.
         */
        public Parameter(
            String name,
            float value)
        {
            this.name = name;
            this.value = String.valueOf(value);
        }

        /**
         * Implements {@code Serializable}
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
         * Name of the parameter representing the object's identifier.
         * 
         * @since openMDX 2.17
         */
        final static public String XRI = "xri";
        
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
         * @return  {@code true{@code  if the given object is a Parameter, too, and
         * if it has the same name as this parameter.
         */
        @Override
        public boolean equals (
            Object object
        ){
            return this == object || (
                object instanceof Parameter && name.equals(((Parameter)object).getName())
            );
        }

        /**
         * Returns a string representation of the {@code Parameter} object.
         *
         * <p>Format:  "<name>=<value>"
         *
         * @return a String
         */
        @Override
        public String toString()
        {
            return this.name + " = " + this.value;
        }

        /**
         * Returns a hash code value for the object. This method is supported for
         * the benefit of hashtables such as those provided by
         * java.util.Hashtable.
         *
         * @return a hash code value for this object.
         */
        @Override
        public int hashCode()
        {
            return this.name.hashCode();
        }

    }

}
