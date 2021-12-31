/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Persistence Manager Test
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2014-2021, OMEX AG, Switzerland
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
package test.openmdx.generic1;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openmdx.generic1.jmi1.Property;
import org.openmdx.generic1.jmi1.StringProperty;
import org.openmdx.junit5.JDOExtension;
import org.openmdx.junit5.OpenmdxTestCoreStandardExtension;
import org.w3c.cci2.Container;

import test.openmdx.app1.cci2.GenericAddress;

/**
 * Persistence Manager Test
 */
@ExtendWith(OpenmdxTestCoreStandardExtension.class)
public class PersistenceManagerTest {

	@RegisterExtension
	static JDOExtension jdoExtension = JDOExtension.withEntityManagerFactoryName("test-Main-EntityManagerFactory");
    
    @Test
    public void whenOneChildIsAddedToTransientContainerThenItsSizeIs1(){
    	// Arrange
    	final GenericAddress parent = jdoExtension.getEntityManager().newInstance(GenericAddress.class);
    	final Container<Property> testee = parent.<Property>getProperty();
    	final StringProperty child = jdoExtension.getEntityManager().newInstance(StringProperty.class);
    	// Act
    	testee.add(child);
    	// Assert
    	Assertions.assertEquals(1, testee.size());
    }

    @Test
    public void whenOneChildIsAddedToTransientContainerThenItIsReturnedByIterator(){
    	// Arrange
    	final GenericAddress parent = jdoExtension.getEntityManager().newInstance(GenericAddress.class);
    	final Container<Property> testee = parent.<Property>getProperty();
    	final StringProperty child = jdoExtension.getEntityManager().newInstance(StringProperty.class);
    	testee.add(child);
    	// Act
    	Property found = null;
    	for(Property property : testee) {
    		found = property;
    	}
    	// Assert
    	Assertions.assertSame(child,found);
    }
    
}
