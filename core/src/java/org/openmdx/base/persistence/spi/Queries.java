/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: OPENMDXQL Support
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2012, OMEX AG, Switzerland
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
package org.openmdx.base.persistence.spi;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.Query;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openmdx.base.accessor.jmi.spi.Jmi1ObjectPredicateInvocationHandler;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Classes;
import org.w3c.spi2.Datatypes;

/**
 * OPENMDXQL Support
 */
public class Queries {

    /**
     * Constructor
     */
    private Queries() {
        // Avoid instantiation
    }

    private static final Class<?>[] OBJECT1 = {Object.class};
    private static final Class<?>[] COLLECTION = {Collection.class};
    private static final Class<?>[] COMPARABLE1 = {Comparable.class};
    private static final Class<?>[] COMPARABLE2 = {Comparable.class, Comparable.class};
    private static final Object[] EMPTY = {Collections.EMPTY_LIST};

    private static final Map<String,Class<?>[]> ARGUMENTS = new HashMap<String, Class<?>[]>();
    
    /**
     * Tells whether we are dispatching to a reference predicate
     * 
     * @param target
     * 
     * @return <code>true</code> if we are dispatching to a reference predicate
     */
    private static boolean isReferencePredicate(
    	Object target	
    ){
    	return 
    		Proxy.isProxyClass(target.getClass()) &&
    		Proxy.getInvocationHandler(target) instanceof Jmi1ObjectPredicateInvocationHandler;
    }
    
    private static Class<?> toClass(
    	String name
    ) throws ServiceException{
    	if("string".equalsIgnoreCase(name)) {
    		return String.class;
    	} else if("date".equals(name)) {
    		return XMLGregorianCalendar.class;
    	} else if("datetime".equalsIgnoreCase(name)) {
    		return Date.class;
    	} else if("short".equalsIgnoreCase(name)) {
    		return Short.class;
    	} else  if("int".equalsIgnoreCase(name) || "integer".equalsIgnoreCase(name)) {
    		return Integer.class;
    	} else if("long".equalsIgnoreCase(name)) {
    		return Long.class;
    	} else if("decimal".equalsIgnoreCase(name)) {
    		return BigDecimal.class;
    	} else if("duration".equalsIgnoreCase(name)) {
    		return Duration.class;
    	} else throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_QUERY_CRITERIA,
            "Unsupported argument type",
            new BasicException.Parameter(
                "type", 
                name
            ),
            new BasicException.Parameter(
                "supported", 
                "string", "date", "datetime", "short", "int", "integer", "long", "decimal", "duration"
            )
        );
    }
    
    private static Object toValue(
    	String parameter
    ) throws ServiceException{
    	if(parameter.length() == 0) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_QUERY_CRITERIA,
            "Missing parameter value"
        );
    	switch(parameter.charAt(0)) {
	    	case ':': {
	    		int endOfType = seek(parameter, 1, ':', false);
	    		String value = parameter.substring(endOfType + 1);
				String type = parameter.substring(1, endOfType);
				return Datatypes.create(
	    			toClass(type),
	    			value
	    		);
	    	}
	    	case '(': {
	    		int endOfType = seek(parameter, 1, ')', false);
	    		String type = parameter.substring(1, endOfType);
				String value = parameter.substring(endOfType + 1);
				try {
					return Datatypes.create(
						Classes.getApplicationClass(type),
						value
					);
				} catch (ClassNotFoundException exception) {
					throw new ServiceException(
						exception,
	                    BasicException.Code.DEFAULT_DOMAIN,
	                    BasicException.Code.BAD_QUERY_CRITERIA,
		                "Invalid parameter class",
	                    new BasicException.Parameter(
	                        "parameter", 
	                        parameter
	                    ),
	                    new BasicException.Parameter(
	                        "class",
	                        type
	                    ),
	                    new BasicException.Parameter(
	                        "value",
	                        value
	                    )
		            );
				}
	    	}
	    	case '"': {
	    		int endOfString = seek(parameter, 1, '"', false);
	    		if(endOfString + 1 == parameter.length()) {
	    			return parameter.substring(1, parameter.length() - 1).replace("\\\"", "\"");
	    		} else throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_QUERY_CRITERIA,
	                "End of value expected after \"",
                    new BasicException.Parameter(
                        "parameter", 
                        parameter
                    ),
                    new BasicException.Parameter(
                        "remaining",
                        parameter.substring(endOfString + 1)
                    )
	            );
	    	}
	    	case '-': case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
	    		return Datatypes.create(BigDecimal.class, parameter);
	    	default: throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_QUERY_CRITERIA,
                "Unable to parse parameter value",
                new BasicException.Parameter(
                    "parameter", 
                    parameter
                )
            );	
    	}
    }
     
    /**
     * Convert string values to object references where necessary
     * 
     * @param parameter
     * @param reference
     * 
     * @return converted parameter
     * @throws ServiceException 
     */
    private static Object toValue(
    	String parameter, 
    	boolean reference
    ) throws ServiceException{
    	Object value = toValue(parameter);
    	return reference ?  new Path((String)value) : value;
    }
    
    /**
     * Seek for the given token
     * 
     * @param value
     * @param position
     * @param token
     * @param optional
     * @return the index of the token
     * @throws ServiceException 
     */
    private static int seek(
    	String value,
    	int position,
    	char token, 
    	boolean optional
    ) throws ServiceException{
    	for(
    		int index = position;
			index < value.length();
			index++
    	){
    		char current = value.charAt(index);
    		if(current == token) {
    			return index;
    		} else if (current == '\\'){
    			index++;
    		} else if (token == '"') {
    			// ignore mark-up character in Strings
    		} else if (current == '"') {
    			index = seek(value, index + 1, '"', optional);
    		} else if (current == '{') {
    			index = seek(value, index + 1, '}', optional);
    		}
    	}
    	if(optional) {
    		return -1;
    	} else throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.BAD_QUERY_CRITERIA,
            "Missing closing token",
            new BasicException.Parameter(
                "value", 
                value
            ),
            new BasicException.Parameter(
                "token",
                token
            )
        );
    }
    
    private static Object[] getArguments(
    	String arguments,
    	Class<?>[] argumentTypes, 
    	boolean reference
    ) throws ServiceException{
    	if(arguments.length() == 0) {
    		return argumentTypes == COLLECTION ? EMPTY : null;
    	} else {
    		List<Object> values = new ArrayList<Object>();
    		int cursor = 0;
    		for(
    			int comma;
    			(comma = seek(arguments, cursor, ',', true)) >= 0;
    			cursor = comma + 1
    		) {
    			values.add(toValue(arguments.substring(cursor, comma), reference));
    		}
			values.add(toValue(arguments.substring(cursor), reference));
			return argumentTypes == COLLECTION ? new Object[]{values} : values.toArray();
    	}
    }
    
    /**
     * Apply a statement to a given query
     * 
     * @param target
     * @param statement
     * @throws ServiceException 
     */
    private static void applyStatement(
        Query query, 
        String statement
    ) throws ServiceException{
    	int cursor = 0;
    	Method: for(Object target = query;;){
	    	int beginOfArguments = seek(statement, cursor, '(', false);
	    	String methodName = statement.substring(cursor, beginOfArguments);
	    	int endOfArguments = seek(statement, beginOfArguments, ')', false);
	    	Class<?>[] argumentTypes = ARGUMENTS.get(methodName);
	    	Object[] arguments = getArguments(
	    		statement.substring(beginOfArguments + 1, endOfArguments),
	    		argumentTypes, 
	    		isReferencePredicate(target)
	    	);
	        try {
	        	target = target.getClass().getMethod(
	        		methodName, 
	        		argumentTypes
	        	).invoke(
	        		target,
	        		arguments
	        	);
	        } catch (Exception exception) {
	            throw new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_QUERY_CRITERIA,
                    "Unknown method",
                    new BasicException.Parameter(
                        "target", 
                        target.getClass().getName()
                    ),
                    new BasicException.Parameter(
                        "method",
                        methodName
                    )
	            );
	        }
	        for(
	        	cursor = endOfArguments + 1;
	        	cursor < statement.length();
	        ){
	        	char current = statement.charAt(cursor++);
	        	if (current == '{' && target instanceof Query) {
	        		int endOfBlock = seek(statement, cursor, '}', false);
	        		applyStatements((Query)target, statement.substring(cursor, endOfBlock));
	        		cursor = endOfBlock + 1; 
	        		if(cursor != statement.length()) throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_QUERY_CRITERIA,
	                    "End of statement expected after block",
                        new BasicException.Parameter(
                            "target", 
                            target.getClass().getName()
                        ),
                        new BasicException.Parameter(
                            "remaining",
                            statement.substring(cursor)
		                )
		            );
	        	} else if (current == '.') {
	        		continue Method;
	        	} else throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_QUERY_CRITERIA,
                    "Dot expected",
                    new BasicException.Parameter(
                        "target", 
                        target.getClass().getName()
                    ),
                    new BasicException.Parameter(
                        "remaining",
                        statement.substring(cursor)
	                )
	            );
	        }
	        break Method;
    	}
    }
    
    /**
     * Apply a statement sequence to a query
     * 
     * @param target
     * @param statements a semicolon separated sequence of statements
     * 
     * @throws ServiceException 
     */
    public static void applyStatements(
        Query target, 
        String statements
    ) throws ServiceException{
		for(
			int cursor = 0, endOfStatement;
			cursor < statements.length();
			cursor = endOfStatement + 1
		) {
			endOfStatement = seek(statements, cursor, ';', true);
			if(endOfStatement < 0) endOfStatement = statements.length();
			applyStatement(target, statements.substring(cursor, endOfStatement));
		}
    }
        
    static {
    	ARGUMENTS.put("equalTo", OBJECT1);
    	ARGUMENTS.put("notEqualTo", OBJECT1);
    	ARGUMENTS.put("elementOf", COLLECTION);
    	ARGUMENTS.put("notAnElementOf", COLLECTION);
    	ARGUMENTS.put("like", COLLECTION);
    	ARGUMENTS.put("unlike", COLLECTION);
    	ARGUMENTS.put("startsWith", COLLECTION);
    	ARGUMENTS.put("startsWith", COLLECTION);
    	ARGUMENTS.put("endsWith", COLLECTION);
    	ARGUMENTS.put("endsNotWith", COLLECTION);
    	ARGUMENTS.put("between", COMPARABLE2);
    	ARGUMENTS.put("outside", COMPARABLE2);
    	ARGUMENTS.put("lessThan", COMPARABLE1);
    	ARGUMENTS.put("lessThanOrEqualTo", COMPARABLE1);
    	ARGUMENTS.put("greaterThanOrEqualTo", COMPARABLE1);
    	ARGUMENTS.put("greaterThan", COMPARABLE1);
    }

}
