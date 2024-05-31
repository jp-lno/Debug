import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {

    @TempDir
    static Path tempDir;  // Folder that JUnit creates and deletes per test case

    @Test
    void testDeleteExistingFile() throws Exception {
        // Arrange
        File fileToDel = tempDir.resolve("testfile.txt").toFile();
        assertTrue(fileToDel.createNewFile()); // Ensure the file we want to delete exists

        // Act
        boolean result = FileUtils.deleteFile(fileToDel);

        // Assert
        assertTrue(result, "Expected successful deletion");
        assertFalse(fileToDel.exists(), "Expected file to be deleted");
    }

    @Test
    void testDeleteNonExistingFile() {
        // Arrange
        File nonExistingFile = tempDir.resolve("nonExistingFile.txt").toFile();

        // Act
        boolean result = FileUtils.deleteFile(nonExistingFile);

        // Assert
        assertFalse(result, "Expected unsuccessful deletion");
    }
}