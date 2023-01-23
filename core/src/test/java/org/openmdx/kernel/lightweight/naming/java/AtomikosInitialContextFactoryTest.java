/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Atomikos Initial Context Factory Test
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
package org.openmdx.kernel.lightweight.naming.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openmdx.kernel.lightweight.naming.LightweightInitialContextFactoryBuilder;

/**
 * This tests asserts that the {@code AtomikosInitialContextFactory) is the default
 * {@code InitialContextFactory} as soon as the {@code LightweightInitialContextFactoryBuilder}.
 */
public class AtomikosInitialContextFactoryTest {

    @BeforeAll
    static void initialize() throws NamingException{
    	LightweightInitialContextFactoryBuilder.install();
	}

	@Test
	public void foo() throws NamingException{
	    populateNonMangedContext();
	}

    @Test
    public void bar() throws NamingException{
        populateNonMangedContext();
    }

    @Test
    void assertAtomikosTransactionManager() throws NamingException {
	    // Arrange
        Context root = new InitialContext();
    	// Act
        final Object transactionManager = root.lookup("java:comp/TransactionManager");
        // Assert
        Assertions.assertEquals(
        	"com.atomikos.icatch.jta.UserTransactionManager", 
        	transactionManager.getClass().getName(),
        	"Use the (thirparty) Atomikos transaction manager"
        );
    }
    
	private void populateNonMangedContext() throws NamingException{
	    reset();
	    populateInitialContext();
	    populateTopLevelContexts();
	    populateThirdLevelContexts();
	}
    	
	private void reset() throws NamingException{
	    // Arrange
        Context root = new InitialContext();
        Collection<String> names = new ArrayList<String>(); 
        for(
            NamingEnumeration<Binding> bindings = root.listBindings("");
            bindings.hasMoreElements();
        ){
            names.add(bindings.nextElement().getName());
        }
        // Act
        for(String name : names) {
            root.destroySubcontext(name);
        }
		// Assert
		for(
			NamingEnumeration<Binding> bindings = root.listBindings("");
			bindings.hasMoreElements();
		){
			Assertions.fail("Initially there should be no bindings");
		}
		Assertions.assertEquals("", root.getNameInNamespace(), "The initial contex's name");
	}

	private void populateInitialContext() throws NamingException{
		Context root = new InitialContext();
		root.createSubcontext("org");
		root.createSubcontext("ch");
		Set<String> expected = new HashSet<String>(
			Arrays.asList("ch", "org")
		);
		for(
			NamingEnumeration<Binding> bindings = root.listBindings("");
			bindings.hasMoreElements();
		){
			Binding binding = bindings.nextElement();
			if(expected.remove(binding.getName())) {
				Assertions.assertTrue(
						binding.getObject() instanceof Context,
						binding.getName() + " is expected to be a Context" 
				);
			}
		}
		Assertions.assertTrue(expected.isEmpty(), "Top level contexts");
	}

	private void populateTopLevelContexts() throws NamingException{
		Context root = new InitialContext();
		Context org = (Context) root.lookup("org");
		org.createSubcontext("openmdx");
		Context ch = (Context) root.lookup("ch");
		ch.createSubcontext("omex");
		Set<String> expected = new HashSet<String>(
			Arrays.asList("org/openmdx")
		);
		for(
			NamingEnumeration<Binding> bindings = root.listBindings("org");
			bindings.hasMoreElements();
		){
			Binding binding = bindings.nextElement();
			if(expected.remove(binding.getNameInNamespace())) {
				Assertions.assertTrue(
					binding.getObject() instanceof Context, 
					binding.getName() + " is expected to be a Context"
				);
			}
		}
		Assertions.assertTrue(expected.isEmpty(), "Second level contexts");
	}

	private void populateThirdLevelContexts(
    ) throws NamingException{
		Context root = new InitialContext();
		Context openmdx = (Context) root.lookup("org/openmdx");
		openmdx.bind("value", "2011-04-01");
		Set<String> expected = new HashSet<String>(
			Arrays.asList("2011-04-01")
		);
		for(
			NamingEnumeration<Binding> bindings = root.listBindings("org/openmdx");
			bindings.hasMoreElements();
		){
			Binding binding = bindings.nextElement();
			if(expected.remove(binding.getObject())) {
				Assertions.assertEquals(
					String.class.getName(), binding.getClassName(),
					binding.getName() + " is expected to be a String"
				);
			}
		}
		Assertions.assertTrue(expected.isEmpty(), "Third level bindings");
	}
	
}
