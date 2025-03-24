package test.flavour;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.spi2.Datatypes;

import java.io.*;
import java.util.Date;

public class ChronoFlavourTest {

    @Test
    public void testDeserializeDate() {

        //
        final String filePath = "src/test/resources/date.ser";
        serializeData(
                #if CLASSIC_CHRONO_TYPES new java.util.Date() #else java.time.Instant.now() #endif
        );

        //
        try (final FileInputStream fileInputStream = new FileInputStream(filePath);
             final ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {

            //
            Assertions.assertInstanceOf(Datatypes.DATE_TIME_CLASS, Datatypes.DATE_TIME_CLASS.cast(objectInputStream.readObject()));

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void serializeData(
            #if CLASSIC_CHRONO_TYPES java.util.Date #else java.time.Instant #endif object
    ) {

        //
        final String filePath = "src/test/resources/date.ser";

        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {

            objectOutputStream.writeObject(object);
            System.out.println("Serialized data is saved in " + filePath);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final Date date = new Date();

    }

}
