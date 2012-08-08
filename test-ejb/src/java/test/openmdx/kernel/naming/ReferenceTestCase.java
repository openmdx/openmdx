/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ReferenceTestCase.java,v 1.1 2009/04/03 15:08:16 hburger Exp $
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


import java.util.Date;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;

import junit.framework.TestCase;

import org.openmdx.kernel.log.SysLog;


/**
 * This software has been published as "Out of Context JNDI" project by
 * ShiftOne (http://www.shiftone.org) and later on been modified and 
 * integrated in the openMDX framework (http://www.openmdx.org). 
 * 
 * @author The original author was <a href="mailto:jeff@shiftone.org">Jeff Drost</a>.
 */
public class ReferenceTestCase extends TestCase
{

//  private static final Logger LOG = Logger.getLogger(ReferenceTestCase.class);

    /**
     * Method testReferenceString
     *
     * @throws NamingException
     */
    public void testReferenceString() throws NamingException
    {

        Context   ctx = new InitialContext();
        Reference ref = new Reference(Date.class.getName(), ReferencedObjectFactory.class.getName(), null);

        ctx.bind("testReferenceString", ref);

        Object o = null;

        o = ctx.lookup("testReferenceString");
        
        System.out.println(o.getClass().getName() + ": " + o);
        assertTrue("Referenced Object Class", o instanceof Date);

//      LOG.debug(o.getClass().getName() + "=" + o);
        SysLog.trace(o.getClass().getName(), o);

        o = ctx.lookup("testReferenceString");

//      LOG.debug(o.getClass().getName() + "=" + o);
        SysLog.trace(o.getClass().getName(), o);
    }
}
