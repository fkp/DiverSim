import java.applet.*;
import java.awt.*;

public class RunSim extends DiverSim {
    public static void main(String args[]) {
        Applet applet = new DiverSim();
        Frame frame = new AppletFrame("DiverSim", applet, 300, 300);
    }
}

class AppletFrame extends Frame {
    public AppletFrame(String title, Applet applet, int width, int height) { 
        // create the Frame with the specified title.
        super(title); 
        
        // Add a menubar, with a File menu, with a Quit button.
        MenuBar menubar = new MenuBar();
        Menu file = new Menu("File", true);
        menubar.add(file);
        file.add("Quit");
        this.setMenuBar(menubar);
        
        // Add the applet to the window.  Set the window size.  Pop it up.
        this.add("Center", applet);
        this.resize(width, height);
        this.show();
        
        // Start the applet.
        applet.init();
        applet.start();
    }
    
    // Handle the Quit menu button.
    public boolean action(Event e, Object arg)
    {
        if (e.target instanceof MenuItem) {
            String label = (String) arg;
            if (label.equals("Quit")) System.exit(0);
        }
        return false;
    }
}
