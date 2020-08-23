import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.print.*;
import java.util.Random;
import java.util.function.Function;
import java.awt.event.*;

/**
 * Parent class
 */
public class EditorWindow implements ActionListener {
    private String textContent;
    private String id; // Used to maintain a temporary file in case it is needed (Document recovery etc)
    private String filePath;

    private Function<String, Void> onClose;
    private Function<String, Void> newWindow;

    private JFrame thisWindow;
    Random PRNG;
    PrinterJob printerJob = PrinterJob.getPrinterJob();
    
    EditorWindow(Function<String, Void> closeCallback, Function<String, Void> newWindow, String id, String filePath, String content){
        this.onClose = closeCallback;
        this.newWindow = newWindow;
        this.id = id;
        this.filePath = filePath;
        if(content != null){
            textContent = content;
        }
    }

    public void init(){
        PRNG = new Random();
        // Create the frame somewhere inwards from the top-right.
        // Would be better to actually detect screen size though that requires a more 
        // in-depth implementation. Realistically, the minimum screen real-estate for
        // modern Windows is 1024*768, so at least part of the window is guaranteed to
        // be on-screen with a limit of 500 px
        thisWindow = prepMainFrame();
        thisWindow.setBounds(PRNG.nextInt(500), PRNG.nextInt(500), 600, 600);
        thisWindow.setVisible(true);
    }

    /**
     * Creates a 'main' window frame
     * @return a frame representing the main content of the app
     */
    JFrame prepMainFrame(){
        JFrame mainFrame = new JFrame("Notepad");
        JMenuBar menuBar = new JMenuBar();
        /* ---- Menus ---- */
        //#region File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem newMenuItem = new JMenuItem("New");
        JMenuItem newWindowMenuItem = new JMenuItem("New Window");
        newWindowMenuItem.addActionListener((x) -> {
            newWindow.apply(null);
        });
        JMenuItem openMenuItem = new JMenuItem("Open");
        openMenuItem.addActionListener((x) -> {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Documents (*.txt)", "txt");
            fileChooser.setFileFilter(filter);
            int returnVal = fileChooser.showOpenDialog((JMenuItem)x.getSource());
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                System.out.println("Selected file:" + fileChooser.getSelectedFile().getName());
                /* Do file load in here */
            }
        });
        JMenuItem saveMenuItem = new JMenuItem("Save");
        JMenuItem saveAsMenuItem = new JMenuItem("Save As");
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
            onClose.apply(id);
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
        return mainFrame;
    }

    JFrame aboutWindow(){
        JFrame aboutFrame = new JFrame("About");
        JLabel about_title, about_contributor, about_licence, about_contributor_names;
        about_title = new JLabel("Text Editor");
        about_title.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        about_contributor = new JLabel("Contributors:");
        about_contributor.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        // TODO add contributor names properly
        about_contributor_names = new JLabel("<html>Brychan Dempsey<br>Lachlan</html>"); 
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

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub

    }
}

class ClickOnlyListener implements MouseListener{

    @Override
    public void mouseClicked(MouseEvent e) {
        JFrame licence = new JFrame("Licence Details");
        licence.setSize(300, 300);
        licence.setVisible(true);
        // TODO add licence details
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