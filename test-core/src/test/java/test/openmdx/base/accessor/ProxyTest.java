/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Proxy Test 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010-2021, OMEX AG, Switzerland
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
package test.openmdx.base.accessor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.ParseException;

import org.junit.jupiter.api.Test;

/**
 * Proxy Test
 */
public class ProxyTest {
    
    public interface Cci2Provider {
        public Cci2Authority getAuthority();
    }

    public interface Cci2Authority {     
    }
    
    public interface Jmi1Authority extends Cci2Authority {
    }
    
    public interface Jmi1Provider extends Cci2Provider {
        public Jmi1Authority getAuthority();
    }
    
    public static class DummyInvocationHandler implements InvocationHandler {

        public DummyInvocationHandler(
        ) {            
        }
        
        /* (non-Javadoc)
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
         */
//      @Override
        public Object invoke(
            Object arg0, 
            Method arg1, 
            Object[] arg2
        ) throws Throwable {
            throw new UnsupportedOperationException();
        }
        
    }
    
    @Test
    public void testInstantiation(
    ) throws ParseException{
        
        InvocationHandler handler = new DummyInvocationHandler();
        @SuppressWarnings("unused")
        Object d0 = Proxy.newProxyInstance(
            Thread.currentThread().getContextClassLoader(),
            new Class[]{
                Jmi1Provider.class 
            }, 
            handler
        );
        
    }
    
}
