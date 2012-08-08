/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: StackedExceptions.java,v 1.7 2008/03/21 18:30:08 hburger Exp $
 * Description: SPICE Exceptions: Stacked Exceptions Helper 
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:30:08 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.base.exception;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.openmdx.kernel.exception.BasicException;

/**
 * @deprecated 
 * @see org.openmdx.kernel.exception.BasicException
 */
@SuppressWarnings("unchecked")
public final class StackedExceptions {

    /**
     * Avoid instantiation
     */
    private StackedExceptions(
    ){
        super();
    }

    /**
     *
     */
    static private BasicException.Parameter[] exceptionSource = null;

    /**
     *
     */
    static private BasicException.Parameter[] NO_PARAMETER = new BasicException.Parameter[]{};

    /**
     * @deprecated in favour of
     * {@link org.openmdx.kernel.exception.BasicException#setSource(java.lang.Object)
     * BasicException.setSource(exceptionSource)}
     */
    static public void setExceptionSource(
        String[] exceptionSource
    ){
    	String value = Arrays.asList(exceptionSource).toString(); 
        StackedExceptions.exceptionSource = new BasicException.Parameter[]{
            new BasicException.Parameter(
                BasicException.Parameter.EXCEPTION_SOURCE, 
                value
            )
        };
        BasicException.setSource(value);
    }

    /**
     * @deprecated without replacement
     * 
     * @param parameters
     * @return
     */
    static public BasicException.Parameter[] prependExceptionSource(
        BasicException.Parameter[] parameters
    ){
        return BasicException.Parameter.add(
            StackedExceptions.exceptionSource,
            parameters
        );
    }

    /**
     * @deprecated in favour of
     * {@link org.openmdx.kernel.exception.BasicException.toStackedException(java.lang.Throwable,java.lang.Throwable)
     * BasicException.toStackedException(cause,wrapper)}
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
            NO_PARAMETER,
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
     * @deprecated without replacement
     * 
     * @param that
     * @param source
     * @return
     */
    public static BasicException.Parameter[] parameters(
        Throwable that,
        BasicException.Parameter[] source
    ){
        List target = new ArrayList();
        if(source != null)target.addAll(Arrays.asList(source));

        // Cleanup
        for(
            Iterator i=target.iterator();
            i.hasNext();
        ){
            String name = ((BasicException.Parameter)i.next()).getName();
            if (
        		BasicException.Parameter.EXCEPTION_CLASS.equals(name) ||
        		BasicException.Parameter.EXCEPTION_SOURCE.equals(name)
            ) i.remove();
        }   

        target.add(
            0,
            new BasicException.Parameter(
        		BasicException.Parameter.EXCEPTION_CLASS, 
                that.getClass().getName()
            )
        );
        if(StackedExceptions.exceptionSource!=null)target.addAll(
            0,
            Arrays.asList(StackedExceptions.exceptionSource)
        );

        return (BasicException.Parameter[])target.toArray(
            new BasicException.Parameter[target.size()]
        );  

    }

}

