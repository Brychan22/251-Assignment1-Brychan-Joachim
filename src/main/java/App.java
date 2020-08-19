
import javax.swing.*;

/**
 * Hello world!
 */
public final class App {
    private App() {
    }

    /**
     * Application entry-point
     * 
     * @param args
     */
    public static void main(String[] args) {
        // Not a fan of this UI mess, but unfamiliar with Swing components
        JMenuBar menuBar = new JMenuBar();

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.addActionListener((x) -> {
            JLabel about_title, about_contributor, about_licence;
            JFrame aboutFrame = new JFrame();
            about_title = new JLabel("Text Editor");
            about_contributor = new JLabel("Contributors:");
            about_licence = new JLabel("");

            aboutFrame.setTitle("About");
            aboutFrame.add(about_title);
            aboutFrame.add(about_contributor);
            aboutFrame.add(about_licence);

            aboutFrame.setSize(300, 300);
            aboutFrame.setLayout(new BoxLayout(aboutFrame.getContentPane(), BoxLayout.PAGE_AXIS));
            aboutFrame.setVisible(true);
        });
        helpMenu.add(aboutMenuItem);
        menuBar.add(helpMenu);
        JFrame appFrame = new JFrame();
        appFrame.setJMenuBar(menuBar);
        appFrame.setSize(500,400);
        appFrame.setLayout(null);
        appFrame.setVisible(true);
    }
}
