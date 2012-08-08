/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ContextUtil.java,v 1.2 2005/04/08 15:29:16 hburger Exp $
 * Description: 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2005/04/08 15:29:16 $
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
import java.io.PrintStream;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * This software has been published as "Out of Context JNDI" project by
 * ShiftOne (http://www.shiftone.org) and later on been modified and 
 * integrated in the openMDX framework (http://www.openmdx.org). 
 * 
 * @author The original author was <a href="mailto:jeff@shiftone.org">Jeff Drost</a>.
 */
public class ContextUtil
{
	public static void print(Context ctx) throws NamingException
	{
		print(ctx, System.out);
	}
	public static void print(Context ctx, PrintStream out) throws NamingException
	{
		print(ctx, System.out, 0);
	}
	private static void print(Context ctx, PrintStream out, int depth) throws NamingException
	{
		NamingEnumeration enumeration = ctx.list("");
		
		println("[" + ctx.getNameInNamespace() + "]", out, depth);
		
		
		while (enumeration.hasMoreElements())
		{
			Binding binding = (Binding)enumeration.next();
			Object object = binding.getObject();
			println(binding.getName() + " = " + 
				object + " (" + 
				binding.getClassName() + ")", 
				out, depth + 1);
			
			if (object instanceof Context)
			{
				print((Context)object, out, depth + 2);
			}
		}
	}
	private static void println(String text, PrintStream out, int depth)
	{
		for (int i = 0; i < depth; i++)
		{
			out.print("  ");
		}
		out.println(text);
	}
}