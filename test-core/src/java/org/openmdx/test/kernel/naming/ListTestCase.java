/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ListTestCase.java,v 1.3 2008/01/09 16:00:05 hburger Exp $
 * Description: 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/01/09 16:00:05 $
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

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import junit.framework.TestCase;


/**
 * This software has been published as "Out of Context JNDI" project by
 * ShiftOne (http://www.shiftone.org) and later on been modified and 
 * integrated in the openMDX framework (http://www.openmdx.org). 
 * 
 * @author The original author was <a href="mailto:jeff@shiftone.org">Jeff Drost</a>.
 */
public class ListTestCase extends TestCase
{

//  private static final Logger LOG = Logger.getLogger(ListTestCase.class);

    /**
     * Method testReturnedClone
     *
     * @throws NamingException
     */
    public void testListBindings() throws NamingException
    {

        Context ctx = new InitialContext();
        {
            Context           sub   = ctx.createSubcontext("ListTestCase");
    
            sub.bind("one", new Integer(1));
            sub.bind("two", "2");
            sub.bind("three", new Integer(3));
            sub.bind("four", "four");
            sub.bind("five", new Integer(5));
        }
        {
            int               count = 0;
            NamingEnumeration<NameClassPair> enumeration = ctx.list("ListTestCase");
    
            while (enumeration.hasMore())
            {
                Object obj = enumeration.next();
    
                count++;
    
                assertTrue(obj.getClass().getName(), obj instanceof NameClassPair);
            }
    
            assertEquals(count, 5);
        }
        {
            int count = 0;
            NamingEnumeration<Binding> enumeration  = ctx.listBindings("ListTestCase");
    
            while (enumeration.hasMore())
            {
                Object obj = enumeration.next();
    
                count++;
    
                assertTrue(obj.getClass().getName(), obj instanceof Binding);
            }
    
            assertEquals(count, 5);
            
        }
    }
}
