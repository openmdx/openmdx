/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Description: Format 
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

package org.openmdx.application.mof.mapping.java;

import org.openmdx.application.mof.externalizer.spi.ExternalizationConfiguration;
import org.openmdx.application.mof.mapping.cci.Mapper_1_0;
import org.openmdx.application.mof.mapping.java.mof.ModelNameConstantsMapper;

/**
 * Java Export Formats
 */
public enum JavaExportFormat {
    
    CCI2,
    SPI2,
    JMI1,
    JPA3,
    MOF1 {

        @Override
        public Mapper_1_0 createMapper(ExternalizationConfiguration configuration){
            return new ModelNameConstantsMapper(configuration);
        }

    };

    public boolean isCCI2(){
        return this == CCI2;
    }
    public boolean isSPI2(){
        return this == SPI2;
    }
    public boolean isJMI1(){
        return this == JMI1;
    }
    public boolean isJPA3(){
        return this == JPA3;
    }
    public boolean isMOF1(){
        return this == MOF1;
    }

    public Mapper_1_0 createMapper(ExternalizationConfiguration configuration){
        return new Mapper_1(configuration, this);
    }

    /**
     * Determine the name of the subpackage to be used for the given mapping
     *
     * @return The name of the subpackage to be used for the given mapping
     */
    public String getPackageSuffix(){
        return name().toLowerCase();
    }

    /**
     * Determine the canonical name of the format
     *
     * @return the canonical name of the format
     */
    public String getId(){
        return name().toLowerCase();
    }

    public static JavaExportFormat fromId(String id) {
        for(JavaExportFormat format : JavaExportFormat.values()) {
            if (format.getId().equals(id)) {
                return format;
            }
        }
        throw new IllegalArgumentException("Unknown format: " + id);
    }

    public static boolean supports(String id) {
        for(JavaExportFormat format : JavaExportFormat.values()) {
            if (format.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

}
