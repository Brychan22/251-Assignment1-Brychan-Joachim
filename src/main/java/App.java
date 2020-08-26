
import java.util.Map;
import java.util.stream.Stream;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;


public final class App {
    private App() {
    }

    // Log all open windows so that thread-destruction doesn't kill the app. Works
    // in a (superficially) similar way to multiple browser windows/tabs (i.e. Chromium-like browsers)
    static Map<Integer, EditorWindow> editorWindows = new HashMap<Integer, EditorWindow>();
    static int appWindowCount = 0;

    static FileNameExtensionFilter textFileFilter = new FileNameExtensionFilter("Text Documents (*.txt)", "txt");
    static FileNameExtensionFilter ODFFileFilter = new FileNameExtensionFilter("Open-Office Document (*.odt)", "odt");
    
    // UTF-8 has a typical byte-order mark
    static final byte[] Utf8_BOM = new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF}; 

    /**
     * Application entry-point
     * @param args
     */
    public static void main(String[] args) {
        // initialise the first window
        createNewWindow(null);
    }

    /**
     * Creates a new application window
     * @param sourceFile <i>(Nullable)</i> the file to load when creating the window
     */
    static void createNewWindow(File sourceFile){
        String fileContent = null;
        if (sourceFile != null) {
            try{
                loadFileString(sourceFile);
            }
            catch (Exception e){

            }
            
        }
        int newID = appWindowCount++;
        
        EditorWindow newEditorWindow = new EditorWindow(newID, sourceFile, fileContent);
        editorWindows.put(newID, newEditorWindow);
    }


    static byte[] loadFileBytes(File sourceFile) throws IOException {
        // initialise the buffer with a default 32k
        try (FileInputStream fs = new FileInputStream(sourceFile)){
            return loadBytes(fs);
        }
    }

    static byte[] loadBytes(InputStream stream) throws IOException {
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

    static String loadFileString(File sourceFile, String charset, byte[] filterBOM) throws IOException, UnsupportedEncodingException {
        return loadBytesAsString(loadFileBytes(sourceFile), charset, filterBOM);
        
    }

    static String loadFileString(File sourceFile) throws IOException, UnsupportedEncodingException {
        return loadFileString(sourceFile, "UTF-8", Utf8_BOM);
    }

    static String loadBytesAsString(byte[] bytes) throws IOException, UnsupportedEncodingException {
        return loadBytesAsString(bytes, "UTF-8", Utf8_BOM);
    }

    static String loadBytesAsString(byte[] bytes, String charset, byte[] filterBOM) throws IOException, UnsupportedEncodingException {
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
    static boolean saveFile(File file, byte[] saveData) throws SecurityException, FileNotFoundException, IOException {
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
    static boolean saveFile(File file, String saveText, String format) throws SecurityException, FileNotFoundException, IOException {
        byte[] textBytes = saveText.getBytes(Charset.availableCharsets().get(format));
        if (format == "UTF-8"){
            byte[] t_textBytes = new byte[textBytes.length + 3];
            for(int i = 0; i < App.Utf8_BOM.length; i++){
				t_textBytes[i] = App.Utf8_BOM[i];
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
    static boolean saveFile(File file, String saveText) throws SecurityException, FileNotFoundException, IOException {
        return saveFile(file, saveText, "UTF-8");
    }

    /**
     * Removes the window of the specified ID from the Map of current windows.
     * Exits the application if the last window was closed
     * @param id the window id to remove
     */
    static void windowClosed(int id){
        if(editorWindows.containsKey(id)){
            editorWindows.remove(id);
        }
        if(editorWindows.isEmpty()){
            // If that was our last window, close the application
            System.exit(0);
        }
    }

}
