/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Operation Parameter Class
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
package org.openmdx.base.accessor.jmi.spi;

import java.util.Arrays;
import java.util.List;
import javax.jmi.model.DirectionKind;
import javax.jmi.model.DirectionKindEnum;

/**
 * Operation parameter Class
 */
class OperationParameter {

    /**
     * Tells whether the sruct shall be unboxed
     */
    final boolean unbox;

    /**
     * The fully qualified type name
     */
    final String parameterType;

    private OperationParameter(boolean unbox, String parameterType) {
        this.unbox = unbox;
        this.parameterType = parameterType;
    }

    /**
     * Create a modelled parameter
     *
     * @param qualifiedParameterName the parameter's fully qualified name
     * @return a modelled parameter
     */
    static OperationParameter createExplicitParameter(
        String qualifiedParameterName
    ) {
        return new OperationParameter(false, qualifiedParameterName);
    }

    /**
     * Create a boxing parameter
     *
     * @param direction tells whether wie are craeting an in or return parameter
     * @param qualifiedOperationName the operation's fully qualified name
     * @return a boxing parameter
     */
    static OperationParameter createBoxingParameter(
        DirectionKind direction,
        String qualifiedOperationName
    ) {
        List<String> components = Arrays.asList(qualifiedOperationName.split(":"));
        StringBuilder parameterType = new StringBuilder();
        int i = 0;
        for (String component : components) {
            if (i++ > 0) parameterType.append(":");
            if (i < components.size()) {
                parameterType.append(component);
            } else {
                parameterType.append(component.substring(0, 1).toUpperCase()).append(component.substring(1));
            }
        }
        if( direction == DirectionKindEnum.IN_DIR) {
            parameterType.append("Params");
        } else if (direction == DirectionKindEnum.OUT_DIR) {
            parameterType.append("Result");
        } else {
            throw new IllegalArgumentException("Unsupported direction: " + direction);
        }
        return new OperationParameter(true, parameterType.toString());
    }

}
