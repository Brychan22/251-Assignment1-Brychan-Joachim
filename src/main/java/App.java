
import java.util.Map;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;


public final class App {
    private App() {
    }

    // Log all open windows so that thread-destruction doesn't kill the app. Works
    // in a (superficially) similar way to multiple browser windows/tabs (i.e. Chromium-like browsers)
    static Map<Integer, EditorWindow> editorWindows;
    static int appWindowCount = 0;

    static FileNameExtensionFilter supportedFileTypesFilter = new FileNameExtensionFilter("Text Documents (*.txt)", "txt");
    
    // UTF-8 has a typical byte-order mark
    static final byte[] Utf8_BOM = new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF}; 

    /**
     * Application entry-point
     * @param args
     */
    public static void main(String[] args) {
        // Initialise the empty list of editor windows
        editorWindows = new HashMap<Integer, EditorWindow>();
        // initialise the first window
        createNewWindow(null);
    }

    /**
     * Creates a new application window
     * @param sourceFile <i>(Nullable)</i> the file to load when creating the window
     */
    static void createNewWindow(File sourceFile){
        String fileContent = null;
        if (sourceFile != null){
            loadFile(sourceFile);
        }
        int newID = appWindowCount++;
        EditorWindow newEditorWindow = new EditorWindow(newID, sourceFile, fileContent);
        editorWindows.put(newID, newEditorWindow);
        newEditorWindow.init();
    }

    /**
     * Performs loading the file
     * @param sourceFile the file to load
     * @return null if loading failed, else a String of the contents of the file
     */
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
