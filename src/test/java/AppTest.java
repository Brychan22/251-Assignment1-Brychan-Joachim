
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
    static File testFile = new File("test.txt");
    @BeforeAll
    static void createFiles() {
        assertDoesNotThrow(() -> App.saveFile(testFile, expected), "Failed to create the test file");
    }
    @AfterAll
    static void removeFiles() {
        testFile.delete();
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
            assertEquals(expected, App.loadFile(testFile));
        }, "Failed loading the test file");
    }
}
