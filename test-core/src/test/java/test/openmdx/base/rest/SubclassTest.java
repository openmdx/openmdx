/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Subclass Test
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2021, OMEX AG, Switzerland
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
package test.openmdx.base.rest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Subclass Test
 */
public class SubclassTest {
    
    @Test
    public void testX() {
        XInterface x = new XImplementation(System.currentTimeMillis());
        java.util.Date dX = x.getList().get(0); 
        Assertions.assertEquals(java.util.Date.class, dX.getClass(), "dX");
        dX = x.list().get(0);
        Assertions.assertEquals(java.util.Date.class, dX.getClass(), "dX");
    }

    @Test
    public void testY() {
        YInterface y = new YImplementation(System.currentTimeMillis());
        java.sql.Date dY = y.getList().get(0);
        Assertions.assertEquals(java.sql.Date.class, dY.getClass(), "dY");
        java.util.Date dX = y.getList().get(0); 
        Assertions.assertEquals(java.sql.Date.class, dX.getClass(), "dX");
        java.util.List<java.sql.Date> l = y.list();
        dY = l.get(0);
        Assertions.assertEquals(java.sql.Date.class, dY.getClass(), "dY");
        dX = l.get(0); 
        Assertions.assertEquals(java.sql.Date.class, dX.getClass(), "dX");
    }
    
}
