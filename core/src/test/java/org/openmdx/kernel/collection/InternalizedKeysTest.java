/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: InternalizedKeysTest 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2013-2021, OMEX AG, Switzerland
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
package org.openmdx.kernel.collection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Internalized Keys Test
 */
public class InternalizedKeysTest {

    @Test
    public void internalizedStrings(){
        for(char v = 'A'; v < 'Z'; v+=5){
            String key = InternalizedKeys.internalize("key" + v);
            for(int i = 0; i < 4; i++) {
                Assertions.assertSame(key,  InternalizedKeys.internalize("key" + v));
            }
        }
    }

    @Test
    public void internalizedShorts(){
        for(short v = -1200; v <= 1200; v+=100){
            Short key = InternalizedKeys.internalize(Short.valueOf(v));
            for(int i = 0; i < 4; i++) {
            	Assertions.assertSame(key,  InternalizedKeys.internalize(Short.valueOf(v)));
            }
        }
    }

    @Test
    public void internalizedIntegers(){
        for(int v = -1200; v <= 1200; v+=100){
            Integer key = InternalizedKeys.internalize(Integer.valueOf(v));
            for(int i = 0; i < 4; i++) {
            	Assertions.assertSame(key,  InternalizedKeys.internalize(Integer.valueOf(v)));
            }
        }
    }
    
    @Test
    public void internalizedLongs(){
        for(long v = -1200; v <= 1200; v+=100){
            Long key = InternalizedKeys.internalize(Long.valueOf(v));
            for(int i = 0; i < 4; i++) {
            	Assertions.assertSame(key,  InternalizedKeys.internalize(Long.valueOf(v)));
            }
        }
    }
    
}
