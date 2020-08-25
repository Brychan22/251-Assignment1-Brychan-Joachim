package ODTLibrary;

import NotepadIO.NotepadIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.*;

/**
 * 
 */
public class ODTParser {
    /*
     * Preface: ODT (OpenDocument Text format) is a zip container format
     * encapsulating several xml files
     */
    // Store references to the files contained within the zip file (Name, ZipEntry)
    Map<String, ZipEntry> zippedFiles;
    // Store the information from metadata. TODO: Verify character counts vs. metadata counts
    List<MlElement> metadata;

    /**
     * Opens an ODT file, exposing all
     * @param sourceFile
     * @throws ZipException
     * @throws IOException
     */
    public void OpenODT(File sourceFile) throws ZipException, IOException {
        zippedFiles = new HashMap<String, ZipEntry>();
        ZipFile zf = new ZipFile(sourceFile);
        Enumeration<? extends ZipEntry> subFiles = zf.entries();
        // Change this to parse each file sequentially, once loading is working
        while(subFiles.hasMoreElements()){
            ZipEntry entry = subFiles.nextElement();
            if (entry.isDirectory()){
                continue;
            }
            zippedFiles.put(entry.getName(), entry);
        }
        // Ensure ODT format
        assert(zippedFiles.containsKey("mimetype"));
        try (InputStream stream = zf.getInputStream(zippedFiles.get("mimetype"))){
            assert("application/vnd.oasis.opendocument.text".equals(NotepadIO.loadBytesAsString(NotepadIO.loadBytes(stream))));
        }
        // If we're here, the file has been optimistically verified as an ODT document. continue loading.
        // The remaining files need to be opened and streamed.
        // Start with styles, so we can dynamically convert content.xml
    }
    
}

