/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Macros 
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

import java.util.List;
import java.util.Map;

import org.openmdx.base.naming.Path;

/**
 * Macros
 */
class MacroHolder implements MacroHandler {

    MacroHolder(
        Map<String, List<StringMacro>> stringMacros,
        List<PathMacro> pathMacros
    ) {
        this.stringMacros = stringMacros;
        this.pathMacros = pathMacros;
    }

    /**
     * String macros per column
     */
    private final Map<String, List<StringMacro>> stringMacros;
    
    /**
     * Path macros
     */
    private final List<PathMacro> pathMacros;
    
    
    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.macros.MacroHandler#externalize(java.lang.String, java.lang.String)
     */
    @Override
    public String externalizeString(
        String columnName,
        String internal
    ) {
        final List<StringMacro> macros = this.stringMacros.get(columnName);
        if(macros != null) {
            for(StringMacro macro : macros) {
                if(macro.isExternalizable(internal)) {
                    return macro.externalize(internal);
                }
            }
        }
        return internal;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.macros.MacroHandler#internalize(java.lang.String, java.lang.String)
     */
    @Override
    public String internalizeString(
        String columnName,
        String external
    ) {
        final List<StringMacro> macros = this.stringMacros.get(columnName);
        if(macros != null) {
            for(StringMacro macro : macros) {
                if(macro.isInternalizable(external)) {
                    return macro.internalize(external);
                }
            }
        }
        return external;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.macros.MacroHandler#externalizePath(org.openmdx.base.naming.Path)
     */
    @Override
    public String externalizePath(Path internal) {
        String converted = internal.toXRI();
        boolean modified = false;
        if(this.pathMacros != null) {
            for(PathMacro macro : this.pathMacros) {
                if(macro.isExternalizable(converted)) {
                    converted = macro.externalize(converted);
                    modified = true;
                }
            }
        }
        return modified ? converted : internal.toURI();
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.dataprovider.layer.persistence.jdbc.macros.MacroHandler#internalizePath(java.lang.String)
     */
    @Override
    public Object internalizePath(String external) {
        String converted = external;
        boolean modified = false;
        if(this.pathMacros != null) {
            for(PathMacro macro : this.pathMacros) {
                if(macro.isInternalizable(converted)) {
                    converted = macro.internalize(converted);
                    modified = true;
                }
            }
        }
        final Path convertedPath = new Path(converted);
        return modified
            ? (convertedPath.toXRI().equals(converted)
                ? (Object) convertedPath
                : (Object) converted)
            : (convertedPath.toURI().equals(converted)
                ? (Object) convertedPath
                : (Object) converted);
    }

    /**
     * Retrieve stringMacros.
     *
     * @return Returns the string macros.
     */
    Map<String, List<StringMacro>> getStringMacros() {
        return this.stringMacros;
    }

    
    /**
     * Retrieve pathMacros.
     *
     * @return Returns the path macros.
     */
    List<PathMacro> getPathMacros() {
        return this.pathMacros;
    }

}
