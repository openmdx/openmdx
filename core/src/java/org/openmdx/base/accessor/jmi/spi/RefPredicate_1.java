/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: RefPredicate_1.java,v 1.11 2009/01/06 10:21:20 wfro Exp $
 * Description: RefFilter_1 based AnyTypePredicate implementation
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/06 10:21:20 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.base.accessor.jmi.spi;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.openmdx.application.dataprovider.cci.AttributeSpecifier;
import org.openmdx.base.accessor.jmi.cci.JmiServiceException;
import org.openmdx.base.accessor.jmi.cci.RefFilter_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.query.FilterOperators;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.base.query.Quantors;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.AnyTypePredicate;

/**
 * RefFilter_1 based AnyTypePredicate implementation
 */
public class RefPredicate_1
    extends RefQuery_1
    implements AnyTypePredicate
{

    /**
     * Constructor 
     *
     * @param refPackage
     * @param filterType
     * @param filterProperties
     * @param attributeSpecifiers
     * @param delegateFilter
     * @param delegateQuantor
     * @param delegateName
     */
    protected RefPredicate_1(
        RefPackage_1_0 refPackage,
        String filterType,
        FilterProperty[] filterProperties,
        AttributeSpecifier[] attributeSpecifiers,
        RefFilter_1_0 delegateFilter,
        Short delegateQuantor,
        String delegateName
    ) {
        super(
            refPackage,
            filterType,
            filterProperties,
            attributeSpecifiers
        );
        this.filter = delegateFilter;
        this.quantor = delegateQuantor;
        this.name = delegateName;
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -5668618898901781191L;

    /**
     * 
     */
    protected final RefFilter_1_0 filter;

    /**
     * 
     */
    protected final Short quantor;

    /**
     * 
     */
    protected final String name;

    /**
     * Adding value to the delegate filter
     * 
     * @param operator
     * @param operand
     */
    public void refAddValue(
        short operator,
        Collection<?> operand
    ){
        try {
            this.filter.refAddValue(
                this.name,
                this.quantor.shortValue(),
                operator,
                operand
            );
        } catch (NullPointerException exception) {
            throw new JmiServiceException(
                new ServiceException(
                    exception,
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    "Either 'null operand' or 'candidate collection support not implemented for openMDX 1 compatibility mode'",
                    new BasicException.Parameter(
                        "quantor", 
                        quantor == null ? "null" : Quantors.toString(this.quantor)
                    ),
                    new BasicException.Parameter(
                        "name", 
                        this.name
                    ),
                    new BasicException.Parameter(
                        "operator",
                        FilterOperators.toString(operator)
                    ),
                    operand == null ? new BasicException.Parameter(
                        "operand", 
                        "null"
                    ) : new BasicException.Parameter(
                        "operand.size()", 
                        operand.size()
                    )
                )
            );
        }
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#equalTo(V)
     */
    public void equalTo(
        Object operand
    ) {
        elementOf(
            Collections.singleton(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#elementOf(V...)
     */
    public void elementOf(
        Object... operand
    ) {
        elementOf(
            Arrays.asList(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#elementOf(Collection<V>)
     */
    public void elementOf(
        Collection<?> operand
    ) {
        refAddValue(
            FilterOperators.IS_IN,
            operand
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#notEqual(V)
     */
    public void notEqualTo(
        Object operand
    ) {
        notAnElementOf(
            Collections.singleton(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#notAnElementOf(V...)
     */
    public void notAnElementOf(
        Object... operand
    ) {
        notAnElementOf(
            Arrays.asList(operand)
        );
    }

    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#notAnElementOf(Collection<V>)
     */
    public void notAnElementOf(
        Collection<?> operand
    ) {
        refAddValue(
            FilterOperators.IS_NOT_IN,
            operand
        );
    }

}
