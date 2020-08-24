import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.print.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;
import java.awt.event.*;

/**
 * Actual editor window class
 * Provides a unified way to create editor windows. Ties in with App to create editor windows in a single environment.
 * The app will not exit until all windows are closed
 */
public class EditorWindow {
    private String textContent;
    private int id; // Used to maintain a temporary file in case it is needed (Document recovery etc)
    private File sourceFile;
    private JTextPane textArea;
    private JFrame thisWindow;
    Random PRNG;
    PrinterJob printerJob = PrinterJob.getPrinterJob();
    
    EditorWindow(int id, File sourceFile, String content){
        this.id = id;
        this.sourceFile = sourceFile;
        if(content != null){
            textContent = content;
        }
    }

    /**
     * Initialise a new context of Editor Window
     */
    public void init(){
        PRNG = new Random();
        // Create the frame somewhere inwards from the top-right.
        // Would be better to actually detect screen size though that requires a more 
        // in-depth implementation. Realistically, the minimum screen real-estate for
        // modern Windows is 1024*768, so at least part of the window is guaranteed to
        // be on-screen with a limit of 500 px
        thisWindow = prepMainFrame();
        thisWindow.setBounds(PRNG.nextInt(500), PRNG.nextInt(500), 600, 600);
        if(textContent != null){
            textArea.setText(textContent);
        }
        if (sourceFile != null){
            thisWindow.setTitle(sourceFile.getName() + " - Notepad");
        }
        thisWindow.setVisible(true);
    }

    /**
     * Creates a 'main' window frame.
     * @return a frame representing the main content of the app
     */
    JFrame prepMainFrame(){
        JFrame mainFrame = new JFrame("New Document - Notepad");
        JMenuBar menuBar = new JMenuBar();
        /* ---- Menus ---- */
        //#region File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem newMenuItem = new JMenuItem("New");
        JMenuItem newWindowMenuItem = new JMenuItem("New Window");
        newWindowMenuItem.addActionListener((x) -> {
            App.createNewWindow(null);
        });
        JMenuItem openMenuItem = new JMenuItem("Open");
        openMenuItem.addActionListener((x) -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(App.supportedFileTypesFilter);
            int returnVal = fileChooser.showOpenDialog((JMenuItem)x.getSource());
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                System.out.println("Selected file:" + fileChooser.getSelectedFile().getName());
                if (textContent == null || textArea.getText().equals(textContent)){
                        String t_result = App.loadFile(fileChooser.getSelectedFile());
                        if (t_result != null){
                            textArea.setText(t_result);
                            textContent = t_result;
                        }
                    }
                else{
                    App.createNewWindow(fileChooser.getSelectedFile());
                }
            }
        });
        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.addActionListener((x) -> {
            if(sourceFile == null){
                sourceFile = showSaveDialog();
            }
            saveFile(sourceFile, textArea.getText());
        });
        JMenuItem saveAsMenuItem = new JMenuItem("Save As");
        saveAsMenuItem.addActionListener((x) -> {
            sourceFile = showSaveDialog();
            saveFile(sourceFile, textArea.getText());
        });
        JMenuItem printMenuItem = new JMenuItem("Print");
        printMenuItem.addActionListener((x) -> {
            if (printerJob.printDialog()) {
                try {printerJob.print();}
                catch (PrinterException exc) {
                    System.out.println(exc);
                 }
             } 
        });
        JMenuItem pageSetupMenuItem = new JMenuItem("Page Setup...");
        pageSetupMenuItem.addActionListener((x) -> {
            printerJob.pageDialog(printerJob.defaultPage());
        });
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener((x) -> {
            App.windowClosed(id);
            thisWindow.dispatchEvent(new WindowEvent(thisWindow, WindowEvent.WINDOW_CLOSING));
        });
        fileMenu.add(newMenuItem);
        fileMenu.add(newWindowMenuItem);
        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(saveAsMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(printMenuItem);
        fileMenu.add(pageSetupMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        //#endregion
        //#region Edit Menu
        JMenu editMenu = new JMenu("Edit");
        JMenuItem cutMenuItem = new JMenuItem("Cut");
        JMenuItem copyMenuItem = new JMenuItem("Copy");
        JMenuItem pasteMenuItem = new JMenuItem("Paste");

        editMenu.add(cutMenuItem);
        editMenu.add(copyMenuItem);
        editMenu.add(pasteMenuItem);
        //#endregion
        //#region Help Menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.addActionListener((x) -> {
            JFrame aboutFrame = aboutWindow();
            aboutFrame.setVisible(true);
        });

        helpMenu.add(aboutMenuItem);
        //#endregion

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(helpMenu);
        mainFrame.setJMenuBar(menuBar);
        /* End of Menus */

        /* Text Area */
        textArea = new JTextPane();

        mainFrame.add(textArea);
        return mainFrame;
    }
    
    /**
     * Creates the 'about' window frame
     * @return a frame representing the about content
     */
    JFrame aboutWindow(){
        JFrame aboutFrame = new JFrame("About");
        JLabel about_title, about_contributor, about_licence, about_contributor_names;

        about_title = new JLabel("Text Editor");
        about_title.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));

        about_contributor = new JLabel("Contributors:");
        about_contributor.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));

        // TODO add contributor names properly
        about_contributor_names = new JLabel("<html>Brychan Dempsey<br></html>"); 

        // TODO add licence details
        about_licence = new JLabel("<HTML><U>add licence name here</U></HTML>");
        about_licence.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        about_licence.setForeground(Color.BLUE);
        about_licence.addMouseListener(new ClickOnlyListener());

        aboutFrame.setTitle("About");
        aboutFrame.add(about_title);
        aboutFrame.add(about_contributor);
        aboutFrame.add(about_contributor_names);
        aboutFrame.add(about_licence);

        aboutFrame.setSize(300, 300);
        aboutFrame.setLayout(new BoxLayout(aboutFrame.getContentPane(), BoxLayout.PAGE_AXIS));
        return aboutFrame;
    }

    /**
     * Shows the file save dialogue box
     * @return the selected save file
     */
    File showSaveDialog(){
        File targetFile = null;
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a file to save");
        fileChooser.setFileFilter(App.supportedFileTypesFilter);
        int userSelection = fileChooser.showSaveDialog(thisWindow);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            targetFile = fileChooser.getSelectedFile();
            System.out.println("Save as file: " + targetFile.getAbsolutePath());
        }
        return targetFile;
    }

    /**
     * Saves the specified byte data to the file, showing an error window if an issue occured
     * @param file the File to save to
     * @param saveData the array of byte data to save
     */
    void saveFile(File file, byte[] saveData) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            if (!file.exists()){
                file.createNewFile();
            }
            fos.write(saveData);
        }
        catch (SecurityException e){
            drawPopupAlert("Error", "Access to the file was denied:\n\n" + e.getStackTrace());
        }
        catch (FileNotFoundException e){
            drawPopupAlert("Error", "Failed to write to the file as it could not be opened:\n\n" + e.getStackTrace());
        }
        catch (IOException e){
            drawPopupAlert("Error", "An IO Error occurred:\n\n" + e.getStackTrace());
        }       
    }

    /**
     * Saves the specified text in the specified format to a file, showing an error message if an issue occurred.
     * @param file the File to save to
     * @param saveText the text to save
     * @param format the <b>java.nio.charset.Charset</b> format to save to
     */
    void saveFile(File file, String saveText, String format){
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
        saveFile(file, textBytes);
    }

    /**
     * Saves the specified text in UTF-8 format to a file, showing an error message if an issue occurred.
     * @param file the File to save to
     * @param saveText the text to save
     */
    void saveFile(File file, String saveText){
        saveFile(file, saveText, "UTF-8");
    }

    void drawPopupAlert(String title, String message){

    }
}



/**
 * Litener that will only respond to a mouseClicked event.
 * Used to allow a JLabel to be clickable
 */
class ClickOnlyListener implements MouseListener{

    @Override
    public void mouseClicked(MouseEvent e) {
        JFrame licence = new JFrame("Licence Details");
        licence.setSize(300, 300);
        licence.setVisible(true);
        //File licenceFile = new File("LICENCE");
        // TODO Load licence here
    }

    @Override
    public void mousePressed(MouseEvent e) {
        return;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        return;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        return;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        return;
    }

}