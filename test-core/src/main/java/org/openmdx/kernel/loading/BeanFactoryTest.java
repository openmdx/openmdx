/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Bean Factory Test 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2014, OMEX AG, Switzerland
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
package org.openmdx.kernel.loading;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openmdx.kernel.configuration.Configurations;
import org.openmdx.kernel.configuration.cci.Configuration;

/**
 * Bean Factory Test
 */
public class BeanFactoryTest {

	@Test
	public void whenDefaultConstructorThenCreateJavaBean(){
		// Arrange
		final Map<String,String> properties = new HashMap<String,String>();
		properties.put("class", AJavaBean.class.getName());
		properties.put("shorts[1]", "-10");
		properties.put("shorts[3]", "-1000");
		properties.put("integer", "4711");
		properties.put("longs[1]", "10");
		properties.put("longs[3]", "1000");
		final Configuration configuration = Configurations.getBeanConfiguration(properties);
		// Act
		final Factory<AnInterface> factory = BeanFactory.newInstance(
			AnInterface.class,
			configuration
		);
		final AnInterface instance = factory.instantiate();	
		// Assert
		final Short[] shorts = instance.getShorts();
		Assert.assertEquals(4, shorts.length);
		Assert.assertNull(shorts[0]);
		Assert.assertEquals(Short.valueOf((short)-10), shorts[1]);
		Assert.assertNull(shorts[2]);
		Assert.assertEquals(Short.valueOf((short)-1000), shorts[3]);
		Assert.assertEquals(Integer.valueOf(4711), instance.getInteger());
		final long[] longs = instance.getLongs();
		Assert.assertEquals(4, longs.length);
		Assert.assertEquals(0l, longs[0]);
		Assert.assertEquals(10l, longs[1]);
		Assert.assertEquals(0l,longs[2]);
		Assert.assertEquals(1000l, longs[3]);
	}
	
}
