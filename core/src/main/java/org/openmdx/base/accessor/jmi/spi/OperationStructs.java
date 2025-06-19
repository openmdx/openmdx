/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Operation Structs Class
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

import java.util.Collection;
import javax.jmi.model.DirectionKind;
import javax.jmi.model.DirectionKindEnum;
import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Multiplicity;
import org.openmdx.base.rest.cci.VoidRecord;
import org.openmdx.kernel.exception.BasicException;

import static javax.jmi.model.DirectionKindEnum.IN_DIR;
import static javax.jmi.model.DirectionKindEnum.RETURN_DIR;

/**
 * Operation Structs Class
 */
class OperationStructs {

    private OperationStructs(OperationParameter inDirection, OperationParameter returnDirection) {
        this.inDirection = inDirection;
        this.returnDirection = returnDirection;
    }

    private static final String CLASSIC_IN_DIRECTION_PARAMETER_NAME = "in";
    private static final String CLASSIC_RETURN_DIRECTION_PARAMETER_NAME = "result";
    private static final String CLASSIC_VOID_PARAMETER_NAME = "void";

    final OperationParameter inDirection;
    final OperationParameter returnDirection;

    public static OperationStructs determineOperationStructs(
        ModelElement_1_0 operationDef
    ) throws ServiceException {
        assertOperation(operationDef);
        int inCount = 0;
        int returnCount = 0;
        OperationParameter inParameter = null;
        OperationParameter returnParameter = null;
        Collection<?> elements = operationDef.objGetList("content");
        for (final Object element : elements) {
            final ModelElement_1_0 candidate = operationDef.getModel().getElement(element);
            if (candidate.isParameterType()) {
                DirectionKind direction = DirectionKindEnum.forName((String) candidate.objGetValue("direction"));
                if (direction == IN_DIR) {
                    if(isClassicParameter(direction, candidate)){
                        inParameter = OperationParameter.createExplicitParameter(
                            candidate.getType().getLastSegment().toClassicRepresentation()
                        );
                    }
                    if(++inCount > 1){
                        inParameter = null;
                    }
                } else if (direction == DirectionKindEnum.RETURN_DIR) {
                    if(isClassicParameter(direction, candidate)){
                        returnParameter = OperationParameter.createExplicitParameter(
                            candidate.getType().getLastSegment().toClassicRepresentation()
                        );
                    }
                    if(++returnCount > 1){
                        returnParameter = null;
                    }
                }
            }
        }
        if(inParameter == null){
            if(inCount == 0){
                inParameter = OperationParameter.createExplicitParameter(VoidRecord.NAME);
            } else {
                #if CLASSIC_CHRONO_TYPES
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "no parameter with name \"" + CLASSIC_IN_DIRECTION_PARAMETER_NAME + "\" defined for operation",
                    new BasicException.Parameter("operation", operationDef.getQualifiedName())
                );
                #else
                    inParameter = OperationParameter.createBoxingParameter(IN_DIR, operationDef.getQualifiedName());
                #endif
            }
        }
        if(returnParameter == null){
            if(returnCount == 0){
                returnParameter = OperationParameter.createExplicitParameter(VoidRecord.NAME);
            } else {
                #if CLASSIC_CHRONO_TYPES
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.ASSERTION_FAILURE,
                    "no parameter with name \"" + CLASSIC_RETURN_DIRECTION_PARAMETER_NAME + "\" defined for operation",
                    new BasicException.Parameter("operation", operationDef.getQualifiedName())
                );
                #else
                returnParameter = OperationParameter.createBoxingParameter(RETURN_DIR, operationDef.getQualifiedName());
                #endif
            }
        }
        return new OperationStructs(inParameter, returnParameter);
    }

    private static boolean isClassicParameter(DirectionKind direction, ModelElement_1_0 candidate) throws ServiceException {
        return OperationStructs.isClassicParameterName(direction, candidate.getName()) &&
            Multiplicity.SINGLE_VALUE == Multiplicity.parse(candidate.getMultiplicity()) &&
            candidate.getModel().getElementType(candidate).isStructureType();
    }

    private static void assertOperation(
        ModelElement_1_0 elementDef
    ) throws ServiceException {
        if (!elementDef.getModel().isOperationType(elementDef)) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ASSERTION_FAILURE,
                "model element not of type " + ModelAttributes.OPERATION,
                new BasicException.Parameter("model element", elementDef.getQualifiedName())
            );
        }
    }

    private static boolean isClassicParameterName(DirectionKind direction, String parameterName) {
        if(CLASSIC_VOID_PARAMETER_NAME.equals(parameterName)){
            return true;
        } else if(direction == DirectionKindEnum.IN_DIR) {
            return CLASSIC_IN_DIRECTION_PARAMETER_NAME.equals(parameterName);
        } else if(direction == DirectionKindEnum.RETURN_DIR) {
            return CLASSIC_RETURN_DIRECTION_PARAMETER_NAME.equals(parameterName);
        } else {
            throw new IllegalArgumentException("Unsupported direction: " + direction);
        }
    }

}
