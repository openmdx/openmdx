/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RelocatableEnumeration.java,v 1.1 2009/01/12 12:49:24 wfro Exp $
 * Description: Relocatable Enumeration
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/12 12:49:24 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.kernel.naming.spi.rmi;

import java.io.Serializable;
import java.util.List;
import java.util.NoSuchElementException;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * Relocatable Enumeration
 */
public class RelocatableEnumeration<T> 
	implements Serializable, NamingEnumeration<T> 
{

	/**
     * 
     */
    private static final long serialVersionUID = 3545800978994901808L;

    /**
	 * @serial
	 */
	private List<T> list;

	/**
	 * @serial
	 */
	private NamingException exception;

	/**
	 * Constructor for naming enumerations terminating abnormally
	 */
	public RelocatableEnumeration(
		List<T> list,
		NamingException exception
	){
		this.list = list;
		this.exception = exception;
	}

	/**
	 * Constructor for enumerations terminating normally
	 */
	public RelocatableEnumeration(
		List<T> list
	){
		this(list,null);
	}

	/* (non-Javadoc)
	 * @see javax.naming.NamingEnumeration#next()
	 */
	public T next() throws NamingException {
		return nextElement();
	}

	/* (non-Javadoc)
	 * @see javax.naming.NamingEnumeration#hasMore()
	 */
	public boolean hasMore() throws NamingException {
		if(hasMoreElements()) {
		    return true;
		} else if(this.exception == null) {
		    return false;
		} else {
		    throw this.exception;
		}
	}

	/* (non-Javadoc)
	 * @see javax.naming.NamingEnumeration#close()
	 */
	public void close() throws NamingException {
		this.list = null;
	}

	/* (non-Javadoc)
	 * @see java.util.Enumeration#hasMoreElements()
	 */
	public boolean hasMoreElements() {
		return !this.list.isEmpty();
	}

	/* (non-Javadoc)
	 * @see java.util.Enumeration#nextElement()
	 */
	public T nextElement() {
		try {
			return this.list.remove(0);
		} catch (IndexOutOfBoundsException exception){
		 	throw new NoSuchElementException();
		 }
	}

}
