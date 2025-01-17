/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Description: Mapping Types 
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package org.openmdx.application.mof.mapping.cci;

/**
 * Mapping Types
 */
public class MappingTypes {
    
    private MappingTypes() {
        // Avoid instantiation
    }
    
    /**
     * This is a modifier telling that annotations use markdown
     */
    public static final String MARKDOWN = "--markdown-annotations";

    /**
     * This is a modifier telling to use Jakarta 8
     */
    public static final String JAKARTA_8 = "--jakarta-8";

    /**
     * This is a modifier telling to use classic JMI mapping
     */
    public static final String JMI_CLASSIC = "--jmi-classic";
    
    /**
     * openMDX model mapping
     */
    public static final String XMI1 = "xmi1";
    
    /**
     * Non-JMI mapping
     */
    public static final String CCI2 = "cci2";
    
    /**
     * Extends cci2 mapping with JMI's reflective interfaces
     */
    public static final String JMI1 = "jmi1";

    /**
     * Standard JPA mapping
     */
    public static final String JPA3 = "jpa3";

    /**
     * Java interfaces with the model
     */
    public static final String MOF1 = "mof1";

    /**
     * PIM Documentation
     */
    public static final String PIMDOC = "pimdoc";
    
}
