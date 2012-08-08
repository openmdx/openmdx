/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: BindLookupTestCase.java,v 1.2 2004/07/15 19:06:04 hburger Exp $
 * Description: 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2004/07/15 19:06:04 $
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



//import org.apache.log4j.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import junit.framework.TestCase;


/**
 * bind, rebind, rename, unbind
 * <p>
 * This software has been published as "Out of Context JNDI" project by
 * ShiftOne (http://www.shiftone.org) and later on been modified and 
 * integrated in the openMDX framework (http://www.openmdx.org). 
 * 
 * @author The original author was <a href="mailto:jeff@shiftone.org">Jeff Drost</a>.
 */
public class BindLookupTestCase extends TestCase
{

//  private static final Logger LOG = Logger.getLogger(BindLookupTestCase.class);

    /**
     * Method testBind
     *
     * @throws NamingException
     */
    public void testBind() throws NamingException
    {

        Context ctx = new InitialContext();
        Object  a   = new Integer(1345);
        Object  b;

        ctx.bind("BLtestBind", a);

        b = ctx.lookup("BLtestBind");

        assertEquals(a, b);
    }

    /**
     * Method testBind
     *
     * @throws NamingException
     */
    public void testBindToNonExistantContext() throws NamingException
    {

        Context ctx = new InitialContext();

        try
        {
            ctx.bind("doesnotexist/BLtestContextNotBound", "a");
        }
        catch (NamingException e)
        {
            return;
        }

        assertTrue("NamingException not thrown", false);
    }

    /**
     * Method testBindNewContext
     *
     * @throws NamingException
     */
    public void testContextShared() throws NamingException
    {

        Context ctx1 = new InitialContext();
        Context ctx2 = new InitialContext();
        Object  a    = new Integer(120986);
        Object  b;

        ctx1.bind("BLtestContextShared", a);

        b = ctx2.lookup("BLtestContextShared");

        assertEquals(a, b);
    }

    /**
     * Method testBindTwice
     *
     * @throws NamingException
     */
    public void testBindTwice() throws NamingException
    {

        Context ctx = new InitialContext();
        Object  a   = new Float(123.456);

        ctx.bind("BLtestBindTwice", a);
        assertEquals(a, ctx.lookup("BLtestBindTwice"));

        try
        {
            ctx.bind("BLtestBindTwice", a);
        }
        catch (NameAlreadyBoundException e)
        {
            return;
        }

        assertTrue("NameAlreadyBoundException not thrown", false);
    }

    /**
     * Method testSameContext
     *
     * @throws NamingException
     */
    public void testLookupThisContext() throws NamingException
    {

        Context ctx = new InitialContext();
        Object  a   = new Integer(32457854);

        ctx.bind("BLtestLookupThisContext", a);

        ctx = (Context) ctx.lookup("");

        assertEquals(a, ctx.lookup("BLtestLookupThisContext"));
    }

    /**
     * Method testBindUnbind
     *
     * @throws NamingException
     */
    public void testBindUnbind() throws NamingException
    {

        Context ctx = new InitialContext();
        Object  a   = "test123";

        ctx.bind("BLtestBindUnbind", a);
        assertEquals(a, ctx.lookup("BLtestBindUnbind"));
        ctx.unbind("BLtestBindUnbind");

        try
        {
            ctx.lookup("BLtestBindUnbind");
        }
        catch (NameNotFoundException e)
        {
            return;
        }

        fail("NameNotFoundException not thrown");
    }

    /**
     * Method testBindNull
     *
     * @throws NamingException
     */
    public void testBindNull() throws NamingException
    {

        Context ctx = new InitialContext();
        Character c = null;

        ctx.bind("BLtestBindNull", c);

        assertNull("BLtestBindNull", ctx.lookup("BLtestBindNull"));
    }

}
