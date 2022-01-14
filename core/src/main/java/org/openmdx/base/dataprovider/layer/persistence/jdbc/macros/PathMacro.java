/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Path Macro 
 * Owner:       the original authors.
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.base.dataprovider.layer.persistence.jdbc.macros;

import java.util.regex.Pattern;

/**
 * Path Macro
 */
public class PathMacro {
    
    private final String name;
    private final String value;
    final String firstName;
    final String otherName;
    private final Pattern firstNamePattern;
    private final Pattern otherNamePattern;
    private final Pattern valuePattern;
    
    PathMacro(
        String name,
        String value
    ) {
        this.name = name;
        this.value = value;
        this.firstName = "xri:*" + name;
        this.firstNamePattern = Pattern.compile(firstName, Pattern.LITERAL);
        this.otherName = "(*" + name + ")";
        this.otherNamePattern = Pattern.compile(otherName, Pattern.LITERAL);
        this.valuePattern = Pattern.compile(value, Pattern.LITERAL);
    }

    boolean isInternalizable(String external) {
        return external.indexOf(name) >= 0;
    }

    boolean isExternalizable(String xri) {
        return xri.indexOf(value) >= 0;
    }

    /**
     * No validation for isExternalizable() necessary as it is a package local class
     * 
     * @param internal the original value
     * @return a string with the leading macro value replaced by the macro name
     */
    String externalize(String xri) {
        return valuePattern.matcher(
            valuePattern.matcher(xri).replaceFirst(firstName)
        ).replaceAll(otherName);
    }
    
    /**
     * No validation for isInternalizable() necessary as it is a package local class
     * 
     * @param the macro specific value
     * @return a strong with the leading macro name replaced by the macro value
     */
    String internalize(String external) {
        return otherNamePattern.matcher(
            firstNamePattern.matcher(external).replaceFirst(value)
        ).replaceAll(value);
    }

    public String getName() {
        return this.name;
    }
    
    public String getValue() {
        return this.value;
    }

}
