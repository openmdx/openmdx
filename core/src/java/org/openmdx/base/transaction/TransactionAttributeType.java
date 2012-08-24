/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: REST Format
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * EasyBeans
 * Copyright (C) 2006 Bull S.A.S.
 * Contact: easybeans@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */
package org.openmdx.base.transaction;

/**
 * Available types of transactions.
 * @see <a HREF="http://www.jcp.org/en/jsr/detail?id=220">EJB 3.0 specification</a>
 * @author Florent Benoit
 * @since EJB 3.0 version.
 * <p>
 * Moved from javax.ejb to org.openmdx.base.transaction in order to get rid
 * of openMDX' EJB dependency.
 */
public enum TransactionAttributeType {

    /**
     * Mandatory type.
     */
    MANDATORY,
 
    /**
     * Required type.
     */
    REQUIRED,
 
    /**
     * Requires new type.
     */
     REQUIRES_NEW,

    /**
     * Supports type.
     */
    SUPPORTS,
 
    /**
     * Not supported type.
     */
    NOT_SUPPORTED,
 
    /**
     * Never type.
     */
     NEVER

}
