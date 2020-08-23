
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import javax.management.openmbean.TabularType;
import javax.swing.*;
import javax.swing.event.MouseInputListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet.FontAttribute;

import java.awt.*;
import java.awt.print.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;

/**
 * Hello world!
 */
public final class App {
    private App() {
    }

    // Log all open windows so that thread-destruction doesn't kill the app. Works
    // in a (superficially) similar way to multiple browser windows/tabs (i.e. Chromium-like browsers)
    static Map<String, EditorWindow> editorWindows;

    static Random PRNG;
    static int appWindowCount = 0;

    // Callback function that takes a string 'x' and removes it from the Map (i.e. provides a callback method for a closing window)
    static Function<String,Void> closeCallback = (x) -> {
        if(editorWindows.containsKey(x)){
            editorWindows.remove(x);
        }
        if(editorWindows.isEmpty()){
            // If that was our last window, close the application
            System.exit(0);
        }
        return null;
    };
    // Callback function that provides a way to call the creation of a new window from the App 
    static Function<File, Void> newWindow = (x) -> {
        createNewWindow(x);
        return null;
    };

    // Callback function that provides a way to call the creation of a new window from the App 
    static Function<File, String> loadFileContent = (x) -> {
        try{
            return loadFile(x);
        }
        catch (IOException e){
            return null;
        }
        
    };

    /**
     * Application entry-point
     * 
     * @param args
     */
    public static void main(String[] args) {
        PRNG = new Random();
        // Initialise the empty list of editor windows
        editorWindows = new HashMap<String, EditorWindow>();
        // initialise the first window
        newWindow.apply(null);
    }

    static void createNewWindow(File sourceFile){
        String fileContent = null;
        if (sourceFile != null){
            try{
                loadFile(sourceFile);
            }
            catch (IOException e){
                sourceFile = null;
            }
        }
        String newID = Integer.toString(appWindowCount++);
        EditorWindow newEditorWindow = new EditorWindow(closeCallback, newWindow, loadFileContent, newID, sourceFile, fileContent);
        editorWindows.put(newID, newEditorWindow);
        newEditorWindow.init();
    }

    static String loadFile(File sourceFile) throws IOException{
        StringBuilder resultStringBuilder = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile)))) {
                String line;
                while ((line = br.readLine()) != null) {
                    resultStringBuilder.append(line).append("\n");
                    }
            }
            return resultStringBuilder.toString();
    }

}
