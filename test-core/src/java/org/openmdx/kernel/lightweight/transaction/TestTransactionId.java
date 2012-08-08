/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestTransactionId.java,v 1.2 2009/09/15 15:38:47 hburger Exp $
 * Description: Test Transaction Id
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/09/15 15:38:47 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.kernel.lightweight.transaction;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;

import javax.transaction.xa.Xid;

import org.junit.Before;
import org.junit.Test;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.text.format.HexadecimalFormatter;

/**
 * Test Transaction Id
 */
public class TestTransactionId {
    
    @Before
    public void setUp(
    ) throws Exception {
        this.generator = new TransactionIdFactory();
    }

    @Test
    public void testTransactionId(
    ) throws ServiceException, UnsupportedEncodingException {
        Xid transactionId = this.generator.createTransactionId();
        System.out.println("Transaction: " + transactionId);
        assertEquals(
            "TransactionId toString()",
            "Xid:" + new HexadecimalFormatter("LC".getBytes("ASCII"))+ '-' + new HexadecimalFormatter(transactionId.getGlobalTransactionId()),
            transactionId.toString()
        );
        Xid transactionBranchId = this.generator.createTransactionBranchId(transactionId, 0x1234);
        System.out.println("Branch:      " + transactionBranchId);
        assertEquals(
            "TransactionBranchId toString()",
            transactionId.toString() + "-00001234",
            transactionBranchId.toString()
        );
    }

    private TransactionIdFactory generator;
      
}
