import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;

import org.apache.tika.exception.TikaException;

import NotepadIO.NotepadIO;
import Syntax.JavaHighlighter;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.print.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
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
    private Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    Random PRNG;
    PrinterJob printerJob = PrinterJob.getPrinterJob();
    
    EditorWindow(int id, File sourceFile, String content){
        this.id = id;
        this.sourceFile = sourceFile;
        if(content != null){
            textContent = content;
        }
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
            fileChooser.setFileFilter(App.textFileFilter);
            fileChooser.addChoosableFileFilter(App.ODFFileFilter);
            fileChooser.addChoosableFileFilter(App.JavaFileFilter);
            int returnVal = fileChooser.showOpenDialog((JMenuItem)x.getSource());
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                System.out.println("Selected file:" + fileChooser.getSelectedFile().getName());
                if (textContent == null || textArea.getText().equals(textContent)){
                    try{
                        if (fileChooser.getFileFilter() == App.ODFFileFilter){
                            // Using Apache Tika for now. Not ideal, as it doesn't parse all text features effectively
                            String t_result = NotepadIO.loadMiscViaTika(fileChooser.getSelectedFile());
                            if (t_result != null){
                                textArea.setContentType("text/html");
                                textArea.setText(t_result);
                            }
                        }
                        else if (fileChooser.getFileFilter() == App.JavaFileFilter){
                            String t_result = NotepadIO.loadFileString(fileChooser.getSelectedFile());
                            if (t_result != null){
                                textArea.setText(t_result);
                            }
                            StyledDocument doc = textArea.getStyledDocument();
                            JavaHighlighter jh = new JavaHighlighter(t_result);
                            jh.highlightSymbols(doc);
                        }
                        else{
                            String t_result = NotepadIO.loadFileString(fileChooser.getSelectedFile());
                            if (t_result != null){
                                textArea.setText(t_result);
                            }
                        }
                    }
                    catch (Exception e){
                        drawPopupAlert("Error", "Failed to open the file:\n\n" + e.getLocalizedMessage());
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
            doSave();
        });
        JMenuItem saveAsMenuItem = new JMenuItem("Save As");
        saveAsMenuItem.addActionListener((x) -> {
            sourceFile = showSaveDialog();
            doSave();
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
        cutMenuItem.addActionListener((x) -> {
       	 if (textArea.getSelectedText() != null) { // See if they selected something 
                String string = textArea.getSelectedText();
                StringSelection selection = new StringSelection(string);
                clipboard.setContents(selection, selection);
                textArea.replaceSelection("");
            } else {
            	JFrame warningFrame = warningWindow("No string is selected!");
            	warningFrame.setVisible(true);
            }
        });
        
        
        JMenuItem copyMenuItem = new JMenuItem("Copy");
        copyMenuItem.addActionListener((x) -> {
        	 if (textArea.getSelectedText() != null) { // See if they selected something 
                 String string = textArea.getSelectedText();
                 StringSelection selection = new StringSelection(string);
                 clipboard.setContents(selection, selection);
             } else {
            	 JFrame warningFrame = warningWindow("No string is selected!");
            	 warningFrame.setVisible(true);
             }
        });
        
        JMenuItem pasteMenuItem = new JMenuItem("Paste");
        pasteMenuItem.addActionListener((x) -> {
        	Transferable content = clipboard.getContents(this);
            String clipboardString;
            try {
            	clipboardString = (String) content.getTransferData(DataFlavor.stringFlavor);
              	textArea.getDocument().insertString(textArea.getCaretPosition(), clipboardString, null);
            } catch (Exception e) {
            	e.printStackTrace();
              	JFrame warningFrame = warningWindow("Clipboard does not contain string!");
     	 		warningFrame.setVisible(true);
            }
       });
       
        JMenuItem dateAndTimeMenuItem = new JMenuItem("Time/Date");
        dateAndTimeMenuItem.addActionListener((x) -> {
        	String date = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL).format(ZonedDateTime.now());
        	try {
				textArea.getDocument().insertString(textArea.getCaretPosition(), date, null);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
        });

        editMenu.add(cutMenuItem);
        editMenu.add(copyMenuItem);
        editMenu.add(pasteMenuItem);
        editMenu.add(dateAndTimeMenuItem);
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
        
        /* scrollbar */        
        JScrollPane scroll = new JScrollPane (textArea,
     		   JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        mainFrame.add(scroll);

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
     * Creates a popup window if copy is pressed but nothing is selected
     */
    JFrame warningWindow(String warningMessage){
        JFrame noSelectionFrame = new JFrame(warningMessage);
        JLabel warning;

        warning = new JLabel(warningMessage);
        warning.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));

        noSelectionFrame.setTitle(warningMessage);
        noSelectionFrame.add(warning);
        
        noSelectionFrame.setSize(300, 100);
        noSelectionFrame.setLayout(new BoxLayout(noSelectionFrame.getContentPane(), BoxLayout.PAGE_AXIS));
        return noSelectionFrame;
    }

    
    
    /**
     * Shows the file save dialogue box
     * @return the selected save file
     */
    File showSaveDialog(){
        File targetFile = null;
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a file to save");
        fileChooser.setAcceptAllFileFilterUsed(false); 
        fileChooser.addChoosableFileFilter(App.textFileFilter);
        
        
        int userSelection = fileChooser.showSaveDialog(thisWindow);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            boolean validExt = false;
            for (String extension : ((FileNameExtensionFilter)fileChooser.getFileFilter()).getExtensions()) {
                if(fileChooser.getSelectedFile().getName().toLowerCase().endsWith(extension.toLowerCase())){
                    validExt = true;
                    break;
                }
            }
            if(validExt){
                targetFile = fileChooser.getSelectedFile();
            }
            else{
                targetFile = new File(fileChooser.getSelectedFile().getName() + "." + ((FileNameExtensionFilter)fileChooser.getFileFilter()).getExtensions()[0]);
            }
            
            System.out.println("Save as file: " + targetFile.getAbsolutePath());
        }
        return targetFile;
    }

    void doSave(){
        try{
            if(NotepadIO.saveFile(sourceFile, textArea.getText()) && thisWindow.getTitle() != sourceFile.getName() + " - Document"){
                thisWindow.setTitle(sourceFile.getName() + " - Document");
            }
        }
        catch (SecurityException e){
            drawPopupAlert("Error", "Access to the file was denied:\n\n" + e.getLocalizedMessage());
        }
        catch (FileNotFoundException e){
            drawPopupAlert("Error", "Failed to write to the file as it could not be opened:\n\n" + e.getLocalizedMessage());
        }
        catch (IOException e){
            drawPopupAlert("Error", "An IO Error occurred:\n\n" + e.getLocalizedMessage());
        }  
    }

    void drawPopupAlert(String title, String message){
        JFrame alertFrame = new JFrame(title);
        int size = 300;
        alertFrame.setBounds(thisWindow.getX() + thisWindow.getWidth() / 2 - size/2, thisWindow.getY() + thisWindow.getHeight() / 2-size/2, size, size);
        // Replace newlines with html breaks
        message = message.replace("\r\n", "\n");
        message = message.replace("\n", "<br>");
        JLabel alertText = new JLabel("<html>" + message + "</html>");
        alertFrame.add(alertText);
        alertFrame.setVisible(true);
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