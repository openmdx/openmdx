/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: SubcontextTestCase.java,v 1.2 2010/06/07 10:10:47 hburger Exp $
 * Description: 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/06/07 10:10:47 $
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
public class SubcontextTestCase extends TestCase
{

//  private static final Logger LOG = Logger.getLogger(SubcontextTestCase.class);

    /**
     * Method testCreateSubcontext
     *
     * @throws NamingException
     */
    public void testCreateSubcontext() throws NamingException
    {

        Context ctx    = new InitialContext();
        Context newCtx = null;

        ctx.createSubcontext("aa");
        ctx.createSubcontext("aa/bb");
        ctx.createSubcontext("aa/bb/cc");
        ctx.createSubcontext("aa/bb/cc/00");
        ctx.createSubcontext("aa/bb/cc/00/11");

        newCtx = ctx.createSubcontext("aa/bb/cc/00/11/22");

        String name = newCtx.getNameInNamespace();

        assertEquals("aa/bb/cc/00/11/22", name);
    }

    /**
     * Method testBindLookup
     *
     * @throws NamingException
     */
    public void testBindLookup() throws NamingException
    {

        Context ctx = new InitialContext();
        Object  a   = new Integer(11111);

        ctx.createSubcontext("created");
        ctx.bind("created/abcdef", a);
        assertEquals(a, ctx.lookup("created/abcdef"));
    }

    /**
     * Method testBindLookupContextLookup
     *
     * @throws NamingException
     */
    public void testBindLookupContextLookup() throws NamingException
    {

        Context ctx = new InitialContext();
        Object  a   = new Integer(2222);

        ctx.createSubcontext("new");
        ctx.bind("new/abcdef", a);

        ctx = (Context) ctx.lookup("new");

        assertEquals(a, ctx.lookup("abcdef"));
    }
}
