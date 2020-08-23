
import java.util.Map;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;


public final class App {
    private App() {
    }

    // Log all open windows so that thread-destruction doesn't kill the app. Works
    // in a (superficially) similar way to multiple browser windows/tabs (i.e. Chromium-like browsers)
    static Map<Integer, EditorWindow> editorWindows;
    static int appWindowCount = 0;

    static FileNameExtensionFilter supportedFileTypesFilter = new FileNameExtensionFilter("Text Documents (*.txt)", "txt");

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
