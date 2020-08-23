
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet.FontAttribute;

import java.awt.*;
import java.awt.print.*;
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

    // Function that takes a string 'x' and removes it from the Map (i.e. provides a callback method for a closing window)
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
    // Function that provides a way to call the creation of a new window from the App 
    static Function<String, Void> newWindow = (x) -> {
        createNewWindow(x);
        return null;
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

    static void createNewWindow(String filePath){
        String fileContent = null;
        if (filePath != null){
            // TODO load file
        }
        String newID = Integer.toString(appWindowCount++);
        EditorWindow newEditorWindow = new EditorWindow(closeCallback, newWindow, newID, filePath, fileContent);
        editorWindows.put(newID, newEditorWindow);
        newEditorWindow.init();
    }

}
