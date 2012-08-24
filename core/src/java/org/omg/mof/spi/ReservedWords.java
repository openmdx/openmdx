/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: Reserved Words 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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

package org.omg.mof.spi;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Reserved Words
 * <p>
 * This class enumerates<ul>
 * <li>Java keywords
 * <li>Java literals
 * <li><code>java.lang.Object</code> methods
 * </ul>
 */
public class ReservedWords {

    /**
     * Java keywords 
     */
    private static final Collection<String> JAVA_KEYWORDS = Arrays.asList(
        "abstract", "continue", "for", "new", "switch", "assert", "default", 
        "if", "package", "synchronized", "boolean", "do", "goto", "private", 
        "this", "break", "double", "implements", "protected", "throw", 
        "byte", "else", "import", "public", "throws", "case", "enum", 
        "instanceof", "return", "transient", "catch", "extends", "int", 
        "short", "try", "char", "final", "interface", "static", "void", 
        "class", "finally", "long", "strictfp", "volatile", "const", "float", 
        "native", "super", "while"
    );

    /**
     * Java literals for <code>boolean</code>s and <code>Object</code>s
     */
    private static final Collection<String> JAVA_LITERALS = Arrays.asList(
        "true", "false", "null"
    );

    /**
     * Methods defined by <code>java.lang.Object</code>
     */
    private static final Collection<String> OBJECT_METHODS = Arrays.asList(
        "clone", "equals", "finalize", "getClass", "hashCode", "notify", 
        "notifyAll", "toString", "wait"
    );

    /**
     * Empty set
     */
    public static final Set<String> NONE = Collections.emptySet();

    /**
     * Union of <code>JAVA_KEYWORDS</code> and <code>JAVA_LITERALS</code>.
     */
    public static final Set<String> keywordsAndLiterals;

    /**
     * Union of <code>JAVA_KEYWORDS</code>, <code>JAVA_LITERALS</code> and 
     * <code>OBJECT_METHODS</code>.
     */
    public static final Set<String> reservedMethodNames;
    
    static {
        keywordsAndLiterals = new HashSet<String>(JAVA_KEYWORDS);
        keywordsAndLiterals.addAll(JAVA_LITERALS);
        reservedMethodNames = new HashSet<String>(keywordsAndLiterals);
        reservedMethodNames.addAll(OBJECT_METHODS);
    }
    
}
