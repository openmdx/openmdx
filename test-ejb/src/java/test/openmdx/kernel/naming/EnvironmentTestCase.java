/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: EnvironmentTestCase.java,v 1.2 2011/08/20 20:20:08 hburger Exp $
 * Description: 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/08/20 20:20:08 $
 * ====================================================================
 *
 * This software is published under the GNU Lesser General Public
 * as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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



//import org.apache.log4j.Logger;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.TestCase;


/**
 * This software has been published as "Out of Context JNDI" project by
 * ShiftOne (http://www.shiftone.org) and later on been modified and 
 * integrated in the openMDX framework (http://www.openmdx.org). 
 * 
 * @author The original author was <a href="mailto:jeff@shiftone.org">Jeff Drost</a>.
 */
public class EnvironmentTestCase extends TestCase
{

//  private static final Logger LOG = Logger.getLogger(EnvironmentTestCase.class);

    /**
     * Method testBind
     *
     * @throws NamingException
     */
    @SuppressWarnings("unchecked")
	public void testReturnedClone() throws NamingException
    {

        Context ctx = new InitialContext();
        
        @SuppressWarnings("rawtypes")
        Hashtable env = null;
        int       size1, size2;

        env   = ctx.getEnvironment();
        size1 = env.size();

        env.put("EtestReturnedClone", "bla bla bla");

        env   = ctx.getEnvironment();
        size2 = env.size();

        assertEquals(size1, size2);
    }

    /**
     * Method testAddToEnvironment
     *
     * @throws NamingException
     */
    public void testAddToEnvironment() throws NamingException
    {

        Context ctx = new InitialContext();
        Object    value = new Integer(12354);
        Hashtable<?,?> env   = null;

        ctx.addToEnvironment("EtestAddToEnvironment", value);

        env = ctx.getEnvironment();

        assertEquals(value, env.get("EtestAddToEnvironment"));
    }

    /**
     * Method testRemoveFromEnvironment
     *
     * @throws NamingException
     */
    public void testRemoveFromEnvironment() throws NamingException
    {

        Context ctx = new InitialContext();
        Object    value = new Integer(12098754);
        Hashtable<?,?> env   = null;

        ctx.addToEnvironment("EtestRemoveFromEnvironment", value);

        env = ctx.getEnvironment();

        assertEquals(value, env.get("EtestRemoveFromEnvironment"));
        ctx.removeFromEnvironment("EtestRemoveFromEnvironment");
        assertEquals(value, env.get("EtestRemoveFromEnvironment"));

        env = ctx.getEnvironment();

        assertFalse(env.containsKey("EtestRemoveFromEnvironment"));
    }
}
