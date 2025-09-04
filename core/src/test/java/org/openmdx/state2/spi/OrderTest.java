package org.openmdx.state2.spi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.spi2.Datatypes;

public class OrderTest {

    @Test
    void when_newYearsEve_then_nextDayIsNewYearsDay() {
        // Arrange
        Object newYearsEve = Datatypes.create(Datatypes.DATE_CLASS, "2023-12-31");
        Object newYearsDay = Datatypes.create(Datatypes.DATE_CLASS, "2024-01-01");
        // Act
        Object nextDay = Order.successor(Datatypes.DATE_CLASS.cast(newYearsEve));
        // Assert
        Assertions.assertEquals(newYearsDay, nextDay);
    }

    @Test
    void when_newYearsDay_then_previousDayIsNewYearsEve() {
        // Arrange
        Object newYearsEve = Datatypes.create(Datatypes.DATE_CLASS, "2023-12-31");
        Object newYearsDay = Datatypes.create(Datatypes.DATE_CLASS, "2024-01-01");
        // Act
        Object previousDay = Order.predecessor(Datatypes.DATE_CLASS.cast(newYearsDay));
        // Assert
        Assertions.assertEquals(newYearsEve, previousDay);
    }

    @Test
    void when_validFromEqualsInvalidFrom_then_failure(){
        // Arrange
        Object validFrom = Datatypes.create(Datatypes.DATE_TIME_CLASS, "20000401T000000.000Z");
        Object invalidFrom = Datatypes.create(Datatypes.DATE_TIME_CLASS, "20000401T000000.000Z");
        // Act & Assert
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> Order.assertTimeRange(Datatypes.DATE_TIME_CLASS.cast(validFrom), Datatypes.DATE_TIME_CLASS.cast(invalidFrom))
        );
    }

    @Test
    void when_validFromGreaterThanInvalidFrom_then_failure(){
        // Arrange
        Object validFrom = Datatypes.create(Datatypes.DATE_TIME_CLASS, "20000401T000000.001Z");
        Object invalidFrom = Datatypes.create(Datatypes.DATE_TIME_CLASS, "20000401T000000.000Z");
        // Act & Assert
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> Order.assertTimeRange(Datatypes.DATE_TIME_CLASS.cast(validFrom), Datatypes.DATE_TIME_CLASS.cast(invalidFrom))
        );
    }

    @Test
    void when_validFromIsLessThanInvalidFrom_then_ok(){
        // Arrange
        Object validFrom = Datatypes.create(Datatypes.DATE_TIME_CLASS, "20000401T000000.000Z");
        Object invalidFrom = Datatypes.create(Datatypes.DATE_TIME_CLASS, "20000401T000000.001Z");
        // Act & Assert
        Assertions.assertDoesNotThrow(
            () -> Order.assertTimeRange(Datatypes.DATE_TIME_CLASS.cast(validFrom), Datatypes.DATE_TIME_CLASS.cast(invalidFrom))
        );
    }

    @Test
    void when_validFromAndValityToAreInfinity_then_ok(){
        // Arrange
        Object validFrom = null;
        Object invalidFrom = null;
        // Act & Assert
        Assertions.assertDoesNotThrow(
            () -> Order.assertTimeRange(Datatypes.DATE_TIME_CLASS.cast(validFrom), Datatypes.DATE_TIME_CLASS.cast(invalidFrom))
        );
    }

    @Test
    void when_validFromIsInfinity_then_ok(){
        // Arrange
        Object validFrom = null;
        Object invalidFrom = Datatypes.create(Datatypes.DATE_TIME_CLASS, "20000401T000000.000Z");
        // Act & Assert
        Assertions.assertDoesNotThrow(
            () -> Order.assertTimeRange(Datatypes.DATE_TIME_CLASS.cast(validFrom), Datatypes.DATE_TIME_CLASS.cast(invalidFrom))
        );
    }

    @Test
    void when_validToIsInfinity_then_ok(){
        // Arrange
        Object validFrom = Datatypes.create(Datatypes.DATE_TIME_CLASS, "20000401T000000.000Z");
        Object invalidFrom = null;
        // Act & Assert
        Assertions.assertDoesNotThrow(
            () -> Order.assertTimeRange(Datatypes.DATE_TIME_CLASS.cast(validFrom), Datatypes.DATE_TIME_CLASS.cast(invalidFrom))
        );
    }

}
