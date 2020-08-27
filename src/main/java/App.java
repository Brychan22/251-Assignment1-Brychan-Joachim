
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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.HashMap;

import NotepadIO.NotepadIO;


public final class App {
    private App() {
    }

    // Log all open windows so that thread-destruction doesn't kill the app. Works
    // in a (superficially) similar way to multiple browser windows/tabs (i.e. Chromium-like browsers)
    static Map<Integer, EditorWindow> editorWindows = new HashMap<Integer, EditorWindow>();
    static int appWindowCount = 0;

    static FileNameExtensionFilter textFileFilter = new FileNameExtensionFilter("Text Documents (*.txt)", "txt");
    static FileNameExtensionFilter ODFFileFilter = new FileNameExtensionFilter("Open-Office Document (*.odt)", "odt");
    static FileNameExtensionFilter JavaFileFilter = new FileNameExtensionFilter("Java Source File (*.java)", "java");
    
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
    	String date = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL).format(ZonedDateTime.now());
        String fileContent = date + '\n';
        if (sourceFile != null) {
            try{
                NotepadIO.loadFileString(sourceFile);
            }
            catch (Exception e){

            }
            
        }
        int newID = appWindowCount++;
        
        EditorWindow newEditorWindow = new EditorWindow(newID, sourceFile, fileContent);
        editorWindows.put(newID, newEditorWindow);
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
