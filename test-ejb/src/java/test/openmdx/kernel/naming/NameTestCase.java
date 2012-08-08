/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: NameTestCase.java,v 1.1 2009/04/03 15:08:16 hburger Exp $
 * Description: 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/04/03 15:08:16 $
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
import javax.naming.NameParser;
import javax.naming.NamingException;

import junit.framework.TestCase;


/**
 * This software has been published as "Out of Context JNDI" project by
 * ShiftOne (http://www.shiftone.org) and later on been modified and 
 * integrated in the openMDX framework (http://www.openmdx.org). 
 * 
 * @author The original author was <a href="mailto:jeff@shiftone.org">Jeff Drost</a>.
 */
public class NameTestCase extends TestCase
{

//  private static final Logger LOG = Logger.getLogger(NameTestCase.class);

    /**
     * Method testNameParser
     *
     * @throws NamingException
     */
    private void testNameParser(NameParser nameParser) throws NamingException
    {

//      Name name = null;

        assertNotNull(nameParser);
        assertEquals(1, nameParser.parse("xxx").size());
        assertEquals(2, nameParser.parse("a/b").size());
        assertEquals(1, nameParser.parse("a.b").size());
        assertEquals(3, nameParser.parse("a/b/c").size());
        assertEquals(2, nameParser.parse("a/bcc").size());
        assertEquals(9, nameParser.parse("a/b/c/d/e/f/g/h/i").size());

        // yuck
        assertEquals(4, nameParser.parse("a///c").size());
    }

    /**
     * Method testReferenceString
     *
     * @throws NamingException
     */
    public void testNameParser() throws NamingException
    {

        Context ctx = new InitialContext();
        NameParser nameParser = null;

        nameParser = ctx.getNameParser("");

        testNameParser(nameParser);
    }

    /**
     * Method testSubContextNameParser
     *
     * @throws NamingException
     */
    public void testSubContextNameParser() throws NamingException
    {

        Context ctx = new InitialContext();
        Context    sub        = ctx.createSubcontext("NtestSubContextNameParser");
        NameParser nameParser = null;

        nameParser = sub.getNameParser("");

        testNameParser(nameParser);
    }
}
