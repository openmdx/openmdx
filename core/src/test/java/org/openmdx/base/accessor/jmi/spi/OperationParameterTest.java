package org.openmdx.base.accessor.jmi.spi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import javax.jmi.model.DirectionKindEnum;

class OperationParameterTest {

    @Test
    void testCreateExplicitParameter() {
        String qualifiedName = "test:openmdx:clock1:Time";
        OperationParameter param = OperationParameter.createExplicitParameter(qualifiedName);
        Assertions.assertFalse(param.unbox);
        Assertions.assertEquals(qualifiedName, param.parameterType);
    }

    @Test
    void testCreateBoxingParameterIn() {
        String qualifiedOpName = "test:openmdx:app1:EmailAddress:sendMessageTemplate";
        String qualifiedName = "test:openmdx:app1:EmailAddressSendMessageTemplateParams";
        OperationParameter param = OperationParameter.createBoxingParameter(DirectionKindEnum.IN_DIR, qualifiedOpName);
        Assertions.assertTrue(param.unbox);
        Assertions.assertEquals(qualifiedName, param.parameterType);
    }

    @Test
    void testCreateBoxingParameterOut() {
        String qualifiedOpName = "test:openmdx:app1:EmailAddress:sendMessageTemplate";
        String qualifiedName = "test:openmdx:app1:EmailAddressSendMessageTemplateResult";
        OperationParameter param = OperationParameter.createBoxingParameter(DirectionKindEnum.OUT_DIR, qualifiedOpName);
        Assertions.assertTrue(param.unbox);
        Assertions.assertEquals(qualifiedName, param.parameterType);
    }

    @Test
    void testCreateBoxingParameterUnsupportedDirection() {
        String qualifiedOpName = "org.openmdx.test:TestOp";
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            OperationParameter.createBoxingParameter(null, qualifiedOpName);
        });
    }
}