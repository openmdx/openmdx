/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TestNamingLocal.java,v 1.4 2008/01/25 01:00:45 hburger Exp $
 * Description: 
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/25 01:00:45 $
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
package org.openmdx.test.kernel.naming;



import junit.framework.Test;
import junit.framework.TestSuite;

import org.openmdx.kernel.application.container.lightweight.LightweightContainer;


/**
 * This software has been published as "Out of Context JNDI" project by
 * ShiftOne (http://www.shiftone.org) and later on been modified and 
 * integrated in the openMDX framework (http://www.openmdx.org). 
 * 
 * @author The original author was <a href="mailto:jeff@shiftone.org">Jeff Drost</a>.
 */
public class TestNamingLocal {

    /**
     * Method suite
     */
    public static Test suite()
    {
    	LightweightContainer.getInstance(
    	    LightweightContainer.Mode.ENTERPRISE_APPLICATION_CONTAINER
    	);
        TestSuite suite = new TestSuite("Local Tests");
        suite.addTestSuite(BindLookupTestCase.class);
        suite.addTestSuite(EnvironmentTestCase.class);
        suite.addTestSuite(ListTestCase.class);
        suite.addTestSuite(NameTestCase.class);
        suite.addTestSuite(ReferenceTestCase.class);
        suite.addTestSuite(SubcontextTestCase.class);
        suite.addTestSuite(ClassloaderContextTestCase.class);
        return suite;
    }

    
}
