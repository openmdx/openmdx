/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Identifier 
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2013, OMEX AG, Switzerland
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmdx.base.text.Case;

/**
 * Identifier
 * <p>
 * Identifier naming is an important issue for automatically generated Java interfaces, especially
 * when the interfaces are intended to be used by applications written by human programmers. The
 * mapping has to reach a balance between conflicting requirements:<ul>
 * <li>Syntactic correctness &mdash; all identifiers in the mapped Java must conform to the Java syntax.
 * <li>User friendliness &mdash; identifiers should convey as much information as possible without being
 *     overly long.
 * <li>Conformance to existing conventions &mdash; identifiers should conform to existing stylistic
 *     conventions.
 * <li>Conflict avoidance &mdash; identifiers must not conflict with keywords, literals and
 *     <code>java.lang.Object</code>'s methods.
 * </ul>
 */
public enum Identifier {

    /**
     * The identifier consists of lower-case alphabetic characters only.
     */
    PACKAGE_NAME (
        Case.LOWER_CASE, // first
        "", // separator
        Case.LOWER_CASE, // others
        ReservedWords.keywordsAndLiterals // reserved
    ),
    
    /**
     * The identifier consists of lower-case alphabetic characters with the
     * following exceptions.The first letter of the identifier is capitalized. If the
     * identifier consists of multiple words, the first letter of each word in the
     * identifier is capitalized.
     */
    CLASS_PROXY_NAME (
        Case.TITLE_CASE, // first
        "", // separator
        Case.TITLE_CASE, // others
        ReservedWords.keywordsAndLiterals // reserved
    ),
    
    /**
     * The identifier consists of lower-case alphabetic characters with the
     * following exception. If the identifier consists of multiple words, the first
     * letter of each word except the first word, is capitalized.
     */
    OPERATION_NAME (
        Case.LOWER_CASE, // first
        "", // separator
        Case.TITLE_CASE, // others
        ReservedWords.reservedMethodNames // reserved
    ),
    
    /**
     * The identifier consists of lower-case alphabetic characters with the
     * following exception. If the identifier consists of multiple words, the first
     * letter of each word except the first word, is capitalized.
     */
    ATTRIBUTE_NAME (
        Case.LOWER_CASE, // first
        "", // separator
        Case.TITLE_CASE, // others
        ReservedWords.keywordsAndLiterals // reserved
    ),
    
    /**
     * The identifier consists of all upper-case alphabetic characters and the 
     * &ldquo;_&rdquo; character (used to separate words).
     */
    CONSTANT (
        Case.UPPER_CASE, // first
        "_", // separator
        Case.UPPER_CASE, // others
        ReservedWords.NONE // reserved
    ),
    
    /**
     * The identifier consists of all upper-case alphabetic characters and the 
     * &ldquo;_&rdquo; character (used to separate words).
     */
    ENUMERATION_LITERAL (
        Case.UPPER_CASE, // first
        "_", // separator
        Case.UPPER_CASE, // others
        ReservedWords.NONE // reserved
    );
    
    
    /**
     * Constructor 
     *
     * @param first
     * @param others
     * @param separator
     * @param reservation
     */
    private Identifier(
        final Case first, 
        final String separator, 
        final Case others, 
        Set<String> reserved
    ) {
        this.first = first;
        this.others = others;
        this.separator = separator;
        this.reserved = reserved;
    }

    /**
     * Retrieve the identifier for a given model element
     * 
     * @param modelElementName
     * 
     * @return the identifier for a given model element
     */
    public final String toIdentifier(
        String modelElementName
    ){
        return toIdentifier(modelElementName, null, null, null, null);        
    }

    /**
     * Retrieve the identifier for a given model element
     * 
     * @param modelElementName the model element name to be converted to an identifier
     * @param removablePrefix if the <code>modelElementName</code> starts with the word 
     * <code>removablePrefix</code>, then it is removed unless it is <code>null</code>.
     * @param prependablePrefix the <code>prependablePrefix</code> is prepended as first
     * word unless it is <code>null</code>.
     * @param removableSuffix if the <code>modelElementName</code> ends with the word 
     * <code>removablePrefix</code>, then it is removed unless it is <code>null</code>
     * @param appendableSuffix the <code>appendableSuffix</code> is appended as last
     * word unless it is <code>null</code>.
     * @return the identifier for the given model element name
     */
    public final String toIdentifier(
        String modelElementName,
        String removablePrefix,
        String prependablePrefix, 
        String removableSuffix, 
        String appendableSuffix
    ){
        return toIdentifier(
            modelElementName,
            this.first,
            this.separator,
            this.others, 
            this.reserved, 
            removablePrefix, 
            prependablePrefix, 
            removableSuffix, 
            appendableSuffix        
        );
    }

    /**
     * Default accessor for JUnit Tests
     * 
     * @param modelElementName
     * 
     * @return the words the <code>modelElementName</code> consists of
     */
    public static final List<String> toWords(
        String modelElementName
    ){
        List<String> words = new ArrayList<String>();
        for(
            Matcher splitter = word.matcher(modelElementName);
            splitter.find();
        ){
            words.add(splitter.group());
        }
        return words;
    }
    
    public static final boolean isIdentifier(
        String modelElementName
    ){
        return mofIdentifier.matcher(modelElementName).matches();
    }
    
    protected static final String toIdentifier(
        String modelElementName,
        Case first,
        String separator,
        Case others, 
        Collection<String> reserved, 
        String removablePrefix, 
        String prependablePrefix, 
        String removableSuffix, 
        String appendableSuffix        
    ){
        List<String> words = toWords(modelElementName);
        if(words == null || words.isEmpty()) {
            return null;
        } else {
            if (
                removablePrefix != null &&
                removablePrefix.equalsIgnoreCase(words.get(0))
            ){
                words.remove(0);
            }
            if (
                removableSuffix != null &&
                removableSuffix.equalsIgnoreCase(words.get(words.size() - 1))
            ){
                words.remove(words.size() - 1);
            }
            if(prependablePrefix != null) {
                words.addAll(0, toWords(prependablePrefix));
            }
            if(appendableSuffix != null) {
                words.addAll(toWords(appendableSuffix));
            }
            StringBuilder appendable = new StringBuilder( 
                first.toCase(words.get(0))
            );
            for(String word : words.subList(1, words.size())) {
                appendable.append(
                    separator
                ).append(
                    others.toCase(word)
                );
            }
            String identifier = appendable.toString();
            return reserved.contains(identifier) ?
                appendable.append(ESCAPE_SUFFIX).toString() :
                identifier;
        }
    } 
    
    /**
     * Tells whether the standard mapping should be used or a strictly JMI 1 compliant one
     */
    public static final boolean STRICTLY_JMI_1_COMPLIANT = Boolean.FALSE.booleanValue(); // to avoid dead code warning
    
    /**
     * The first word's case
     */
    private final Case first;
    
    /**
     * The other word's case
     */
    private final Case others;
    
    /**
     * The word separator
     */
    private final String separator;
    
    /**
     * The reserved productions
     */
    private final Set<String> reserved; 

    /**
     * Java&trade; Metadata Interface (JMI) Specification's <code>APLHA</code> production
     */
    private static final String UPPER = "\\p{javaUpperCase}";
    
    /**
     * Java&trade; Metadata Interface (JMI) Specification's <code>alpha</code> production
     */
    private static final String LOWER = "\\p{javaLowerCase}";
    
    /**
     * Java&trade; Metadata Interface (JMI) Specification's <code>num</code> production
     */
    private static final String NUM = "\\p{Digit}";
    
    /**
     * Derived from Java&trade; Metadata Interface (JMI) Specification's <code>word</code> production.
     * <p>
     * <em>Note:<br>
     * The original production allows for empty words which doesn't lead to the expected results.
     * </em>
     */
    private static final String MOF_WORD = "(?:(?:" + UPPER + '|' + NUM + ")+|" + LOWER + ")(?:" + LOWER + '|' + NUM + ")*"; 

    /**
     * Each upper case letter starts a new word.
     */
    private static final String STANDARD_WORD = "(?:(?:" + UPPER + "|" + LOWER  + ")(?:[$_]|" + LOWER + "|" + NUM + ")*)";
    
    /**
     * Java&trade; Metadata Interface (JMI) Specification's <code>non-sig</code> production
     */
    private static final String NON_SIG = "[ \\r\\n\\t\\ck\\-_]*";

    /**
     * The pattern to find the words in MOF identifiers.
     */
    private static final Pattern word = Pattern.compile(
        STRICTLY_JMI_1_COMPLIANT ? MOF_WORD : STANDARD_WORD
    );
    
    /**
     * The pattern to check for Java&trade; Metadata Interface (JMI) Specification's valid MOF identifiers.
     */
    private static final Pattern mofIdentifier = Pattern.compile("\\A" + NON_SIG + MOF_WORD + "(?:" + NON_SIG + MOF_WORD + ")*" + NON_SIG + "\\z");
    
    /**
     * The escape suffix is appended to the identifier to avoid conflicts
     * with keywords, literals and methods.
     */
    private static final String ESCAPE_SUFFIX = "_";

}
