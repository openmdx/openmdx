/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestNamingRemote.java,v 1.2 2010/06/04 22:39:41 hburger Exp $
 * Description: 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/04 22:39:41 $
 * ====================================================================
 *
 * This software is published under the GNU Lesser General Public
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package test.openmdx.kernel.naming;



import java.util.Date;

import javax.naming.Context;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.openmdx.kernel.application.container.lightweight.LightweightContainer;
import org.openmdx.kernel.application.process.Subprocess;
import org.openmdx.kernel.naming.Contexts;
import org.openmdx.kernel.naming.initial.ContextFactory;


/**
 * This software has been published as "Out of Context JNDI" project by
 * ShiftOne (http://www.shiftone.org) and later on been modified and 
 * integrated in the openMDX framework (http://www.openmdx.org). 
 * 
 * @author The original author was <a href="mailto:jeff@shiftone.org">Jeff Drost</a>.
 */
public class TestNamingRemote extends TestSuite {

	/**
	 * @param name
	 */
	public TestNamingRemote(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.Test#run(junit.framework.TestResult)
	 */
	@Override
	public void run(TestResult result) {
		setUp(result);
		if(!result.shouldStop()) try {
			super.run(result);
		} finally {
			tearDown(result);
		}
	}

	Subprocess lightweightContainer;
	
	protected void setUp(
		TestResult result
	){
		try {
			this.lightweightContainer = LightweightContainer.fork();
			System.out.println("LightweightContainer@" +new Date() + ' ' + (this.lightweightContainer.isAlive() ? " alive" : "died"));
		} catch (Exception e) {
			e.printStackTrace();
			result.stop();
		}
		if(!this.lightweightContainer.isAlive()) result.stop();
	}
	
	protected void tearDown(
		TestResult result
	){
		try {
			this.lightweightContainer.destroy();
		} catch (Exception e) {
			result.stop();
		} finally {
			this.lightweightContainer = null;
		}
	}
	
	static class Watch extends Thread {
		
		public boolean hasTerminated(
		){
			return true;
		}
		
		public boolean getExitValue(
		){
			return true;
		}

	}

	/**
     * Method suite
     */
    public static Test suite()
    {    	
        TestSuite suite = new TestNamingRemote("Remote Tests");
        suite.addTestSuite(BindLookupTestCase.class);
        suite.addTestSuite(EnvironmentTestCase.class);
        suite.addTestSuite(ListTestCase.class);
        suite.addTestSuite(NameTestCase.class);
        suite.addTestSuite(ReferenceTestCase.class);
        suite.addTestSuite(SubcontextTestCase.class);
        return suite;
    }

    static {
    	System.setProperty(
    		Context.PROVIDER_URL,
			"//localhost:" + Contexts.getRegistryPort() + '/' + Contexts.getNamingService()
		);
    	System.setProperty(
    		Context.INITIAL_CONTEXT_FACTORY,
			ContextFactory.class.getName()
		);
    }

}
