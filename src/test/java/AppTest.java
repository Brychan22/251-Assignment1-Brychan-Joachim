
import org.junit.jupiter.api.*;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;

import java.io.File;


/**
 * Unit test for simple App.
 */
class AppTest {
    static String expected = "A test string to load";
    static byte[] expectedBytes = new byte[] {120, 125, 3, 5, 7, 9};
    static byte[] expectedODT = new byte[] {};

    static File testFile = new File("test.txt");
    static File testFileBinary = new File("test.bin");
    static File testODT = new File("test.odt");
    @BeforeAll
    static void createFiles() {
        assertDoesNotThrow(() -> App.saveFile(testFile, expected), "Failed to create the test file");
        assertDoesNotThrow(() -> App.saveFile(testFileBinary, expectedBytes), "Failed to create the test file");
    }
    @AfterAll
    static void removeFiles() {
        testFile.delete();
        testFileBinary.delete();
        testODT.delete();
    }

    @Test
    void testNewWindowSpeed(){
        assertTimeout(ofMillis(5000), () -> {
            App.createNewWindow(null);
        });
    }

    @Test
    void testTextSaveLoad() {
        assertDoesNotThrow(() -> {
            assertEquals(expected, App.loadFileString(testFile));
        }, "Failed loading the text test file");
    }

    @Test
    void testBinarySaveLoad() {
        assertDoesNotThrow(() -> {
            for (int i=0; i < expectedBytes.length; i++){
                assertEquals(expectedBytes[i], App.loadFileBytes(testFileBinary)[i]);
            }            
        }, "Failed loading the binary test file");
    }
}
