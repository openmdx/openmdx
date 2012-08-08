/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: InvoicePositionImpl.java,v 1.2 2009/02/18 13:00:20 hburger Exp $
 * Description: ProductImpl 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/02/18 13:00:20 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.test.app1.aop2;

import org.openmdx.base.aop2.AbstractObject;
import org.openmdx.test.app1.jmi1.Invoice;
import org.openmdx.test.app1.jmi1.InvoicePosition;
import org.openmdx.test.app1.jmi1.Product;
import org.openmdx.test.app1.jmi1.Segment;

/**
 * ProductImpl
 */
public class InvoicePositionImpl extends AbstractObject<org.openmdx.test.app1.jmi1.InvoicePosition,org.openmdx.test.app1.cci2.InvoicePosition> {

    /**
     * Constructor 
     *
     * @param same
     * @param next
     */
    public InvoicePositionImpl(
        org.openmdx.test.app1.jmi1.InvoicePosition same,
        org.openmdx.test.app1.cci2.InvoicePosition next
    ) {
        super(same, next);
    }

    /**
     * @return
     * @see org.openmdx.test.app1.jmi1.InvoicePosition#getProduct()
     */
    public Product getProduct() {
        InvoicePosition same = sameObject();
        Invoice invoice = same.getInvoice();
        Segment segment = (Segment) invoice.refImmediateComposite();
        return segment.getProductGroup(
            invoice.getProductGroupId()
        ).getProduct(
            same.getProductId()
        );
    }
        
}
