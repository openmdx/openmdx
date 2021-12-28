/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Importer Test 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2021, OMEX AG, Switzerland
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

package org.openmdx.application.xml;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * TestImporter
 */
public class TestImporter extends Importer {

    private static final Pattern p0 = newPattern(null);
    private static final Pattern p1 = newPattern("*.xml");
    private static final Pattern p2 = newPattern("A/*/*.xml");
    private static final Pattern p3 = newPattern("A/**/d*.xml");
    
    @Test
    public void testArchiveEntryPattern(){
        Assertions.assertNull(p0, "null pattern");
        Assertions.assertTrue(p1.matcher("xxx.xml").matches(), "xxx.xml matches " + p1.pattern());
        Assertions.assertFalse(p1.matcher("xxx.xml.old").matches(), "xxx.xml.old does not match " + p1.pattern());
        Assertions.assertFalse(p1.matcher("X/xx.xml").matches(), "X/xx.xml does not match " + p1.pattern());
        Assertions.assertTrue(p2.matcher("A/B/ccc.xml").matches(), "A/B/ccc.xml matches " + p2.pattern());
        Assertions.assertFalse(p2.matcher("A/B/C/ddd.xml").matches(), "A/B/C/ddd.xml does not match " + p2.pattern());
        Assertions.assertTrue(p3.matcher("A/B/C/ddd.xml").matches(), "A/B/C/ddd.xml matches " + p3.pattern());
    }
    
}
