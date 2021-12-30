/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: JUnit 5 Extension for JDO
 * Owner:       Datura Informatik + Organisation AG, Switzerland, 
 *              https://www.datura.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2021, OMEX AG, Switzerland
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
package org.openmdx.junit5;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openmdx.kernel.jdo.ReducedJDOHelper;

/**
 * JUnit 5 Standard Extension for JDO
 */
public class JDOExtension implements BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

	private JDOExtension(String entityManagerFactoryName) {
		this.entityManagerFactoryName = entityManagerFactoryName;
	}

	/**
	 * The entity manager factory name
	 */
	private final String entityManagerFactoryName;

	/**
	 * The entity manager factory
	 */
	private PersistenceManagerFactory entityManagerFactory;

	/**
	 * The entity manager
	 */
	private PersistenceManager entityManager;
	
	/**
	 * Create a JDO extension to be registered
	 * 
	 * @see org.junit.jupiter.api.extension.RegisterExtension
	 * 
	 * @param entityManagerFactoryName the entity manager fatory name
	 * 
	 * @return the JDO to be registered
	 */
	public static JDOExtension withEntityManagerFactoryName(String entityManagerFactoryName) {
		return new JDOExtension(entityManagerFactoryName);
	}
	
	public PersistenceManager getEntityManager() {
		return entityManager;
	}

	public void beforeAll(ExtensionContext context) throws Exception {
		entityManagerFactory = ReducedJDOHelper.getPersistenceManagerFactory(
		    null,
		    entityManagerFactoryName
		);
	}

	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		entityManagerFactory.close();
		entityManagerFactory = null;
	}
	
	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		entityManager = entityManagerFactory.getPersistenceManager();
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		entityManager.close();
		entityManager = null;
	}

}
