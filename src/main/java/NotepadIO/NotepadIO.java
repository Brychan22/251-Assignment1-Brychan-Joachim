package NotepadIO;

import java.io.*;
import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.awt.*;

import org.apache.tika.sax.*;
import org.apache.tika.sax.ToXMLContentHandler;
import org.apache.tika.parser.*;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NotepadIO {
    // UTF-8 has a typical byte-order mark
    static final byte[] Utf8_BOM = new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF}; 
    
    public static byte[] loadFileBytes(File sourceFile) throws IOException {
        // initialise the buffer with a default 32k
        try (FileInputStream fs = new FileInputStream(sourceFile)){
            return loadBytes(fs);
        }
    }

    public static byte[] loadBytes(InputStream stream) throws IOException {
                // initialise the buffer with a default 32k
                byte[] readBytes = new byte[32768];
                int length = 0;
                byte[] bufferBytes = new byte[16384];
                while (stream.available() > 0){
                    int bufferSize = stream.read(bufferBytes);
                    // Resize the buffer if the data won't fit
                    if (readBytes.length - length <= bufferSize){
                        byte[] newReadBytes = new byte[readBytes.length*2]; // Similar implementation to a list
                        for (int i = 0; i<length; i++){
                            newReadBytes[i] = readBytes[i];
                        }
                        readBytes = newReadBytes;
                    }
                    // Add the buffered bytes
                    for (int i = length; i < length + bufferSize; i++){
                        readBytes[i] = bufferBytes[i-length];
                    }
                    length += bufferSize;
                }
                byte[] finalBytes = new byte[length];
                for(int i=0; i < length; i++){
                    finalBytes[i] = readBytes[i];
                }
                return finalBytes;
    }

    public static String loadFileString(File sourceFile, String charset, byte[] filterBOM) throws IOException, UnsupportedEncodingException {
        return loadBytesAsString(loadFileBytes(sourceFile), charset, filterBOM);
        
    }

    public static String loadFileString(File sourceFile) throws IOException, UnsupportedEncodingException {
        return loadFileString(sourceFile, "UTF-8", Utf8_BOM);
    }

    public static String loadBytesAsString(byte[] bytes) throws IOException, UnsupportedEncodingException {
        return loadBytesAsString(bytes, "UTF-8", Utf8_BOM);
    }

    public static String loadBytesAsString(byte[] bytes, String charset, byte[] filterBOM) throws IOException, UnsupportedEncodingException {
        boolean BomPresent = true;
        for (int i=0; i<filterBOM.length; i++) {
            if (bytes[i] != filterBOM[i]){
                BomPresent = false;
                break;
            }
        }
        // Consume the BOM
        if(BomPresent){
            return new String(bytes, filterBOM.length, bytes.length - filterBOM.length, charset);
        }
        else{
            return new String(bytes, charset);
        }
    }

    public static String loadMiscViaTika(File file) throws IOException, SAXException, TikaException{
        ContentHandler handler = new ToXMLContentHandler();
 
        AutoDetectParser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();
        try (InputStream stream = new FileInputStream(file)) {
            parser.parse(stream, handler, metadata);
            return handler.toString();
        }
    }

    /**
     * Performs loading the file
     * @param sourceFile the file to load
     * @return null if loading failed, else a String of the contents of the file
     */
    @Deprecated
    static String loadFile(File sourceFile){
        try {
            StringBuilder resultStringBuilder = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile)))) {
                String line;
                while ((line = br.readLine()) != null) {
                    resultStringBuilder.append(line).append("\n");
                    }
            }
            return resultStringBuilder.toString();
        }
        catch (IOException e){
            return null;
        }
    }

        /**
     * Saves the specified byte data to the file, showing an error window if an issue occured
     * @param file the File to save to
     * @param saveData the array of byte data to save
     */
    public static boolean saveFile(File file, byte[] saveData) throws SecurityException, FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(file);
        if (!file.exists()){
            file.createNewFile();
        }
        fos.write(saveData);
        fos.close();
        return true;     
    }

    /**
     * Saves the specified text in the specified format to a file, showing an error message if an issue occurred.
     * @param file the File to save to
     * @param saveText the text to save
     * @param format the <b>java.nio.charset.Charset</b> format to save to
     */
    public static boolean saveFile(File file, String saveText, String format) throws SecurityException, FileNotFoundException, IOException {
        byte[] textBytes = saveText.getBytes(Charset.availableCharsets().get(format));
        if (format == "UTF-8"){
            byte[] t_textBytes = new byte[textBytes.length + 3];
            for(int i = 0; i < Utf8_BOM.length; i++){
				t_textBytes[i] = Utf8_BOM[i];
            }
            for(int i = 0; i < textBytes.length; i++){
				t_textBytes[i+3] = textBytes[i];
            }
            textBytes = t_textBytes;
        }
        return saveFile(file, textBytes);
    }

    /**
     * Saves the specified text in UTF-8 format to a file, showing an error message if an issue occurred.
     * @param file the File to save to
     * @param saveText the text to save
     */
    public static boolean saveFile(File file, String saveText) throws SecurityException, FileNotFoundException, IOException {
        return saveFile(file, saveText, "UTF-8");
    }
}