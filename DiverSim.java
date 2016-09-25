import java.awt.*;
import java.applet.*;
import java.awt.event.*;

public class DiverSim extends Applet implements Runnable
{
   /*****************************************************************
   * Fraser Kirkpatrick 01/02/2004
   * Main applet class which displays the graphical decompression
   * model
   *****************************************************************/

   // Variables used to pass to the Decompression Model
   private double dive_depth;
   private double dive_time;

   // Various parameter values
   private double max_depth;
   private double min_depth;
   private double surface_pp_nitrogen;
   private double units_depth_per_atmosphere;
   private double gas_nitrogen_frac;
   private double starting_amb_press;
   private short compartments;

   // The instance of the Decompression model and the data it passes back
   private Model decomodel;
   private Compartment[] data;

   // Various graphics variables
   private Graphics buffer_screen;
   private Graphics screen;
   private Image offscreen;
   private Font myfont;
   private Image[] diver;
   private Dimension appdim;
   private int Xsize;
   private int Ysize;
   private int diver_frames;
   private int diverframe;
   private int bordersize;
   private int Xdiver_origin;
   private int Ydiver_origin;
   private int Xgraph_origin;
   private int Ygraph_origin;
   private int Xht_text_origin;
   private int Yht_text_origin;
   private int Xdiver_size;
   private int Ydiver_size;
   private int Xgraph_size;
   private int Ygraph_size;
   private int comp_width;
   private double comp_max_height;
   private double comp_min_height;
   private int comp_x_origin[];
   private Bubble bubble[];
   private double comp_y_factor;
   private double diver_y_factor;
   private int ht_text_height;
   private long sleep_time;
   private Thread diver_thread;
   private int ticks_per_bubble;
   private int total_ticks;
   private int no_bubbles;
   private boolean show_intro;

   public void init()
   {
      int x;

      // The decompression model parameters
      surface_pp_nitrogen = 0.79;
      units_depth_per_atmosphere = 10;
      gas_nitrogen_frac = .79;
      compartments = 5;
      starting_amb_press = 1;
      max_depth = 40;
      min_depth = 0;
      double halftime [] = {5,10,20,40,75};

      // The data from the model and the actual model instance itself
      data = new Compartment[compartments];
      decomodel = new Model(surface_pp_nitrogen
                           ,units_depth_per_atmosphere
                           ,compartments
                           ,halftime
                           ,starting_amb_press
                           ,gas_nitrogen_frac
                           ,false);

      // Various graphics stuff
      myfont = new Font("Monospaced",Font.PLAIN,10);
      appdim = getSize();
      Xsize = appdim.width;
      Ysize = appdim.height;
      bordersize = 10;
      ht_text_height = 50;

      // How long the thread will sleep for before refreshing the screen
      // (milliseconds)
      sleep_time = 500;

      System.out.println("Xsize:"+Xsize+", Ysize:"+Ysize);

      Xdiver_size = Xsize - (2*bordersize);
      Ydiver_size = (Ysize - (3*bordersize) - ht_text_height)/2;
      Xgraph_size = Xdiver_size;
      Ygraph_size = Ydiver_size;
      Xdiver_origin = 0+bordersize;
      Ydiver_origin = 0+bordersize;
      Xgraph_origin = Xdiver_origin;
      Ygraph_origin = Ydiver_origin + Ydiver_size + bordersize;
      Xht_text_origin = Xdiver_origin;
      Yht_text_origin = Ygraph_origin + Ygraph_size + (ht_text_height/4);

      System.out.println("Xgraph_size:"+Xgraph_size);
      System.out.println("Ygraph_size:"+Ygraph_size);
      System.out.println("Xdiver_size:"+Xdiver_size);
      System.out.println("Ydiver_size:"+Ydiver_size);

      // How much space to give each compartment we want to display
      comp_width=(Xgraph_size-(bordersize*(compartments-1)))/compartments;
      System.out.println("comp_width:"+comp_width);

      System.out.println("max_depth:"+max_depth);
      System.out.println("units_depth_per_atmosphere:"+units_depth_per_atmosphere);
      System.out.println("gas_nitrogen_frac:"+gas_nitrogen_frac);
      // What the max/min size of the compartments will be
      comp_max_height = ((max_depth/units_depth_per_atmosphere)+1)*gas_nitrogen_frac;
      comp_min_height = gas_nitrogen_frac;

      System.out.println("max/min_height:"+comp_max_height+" "+comp_min_height);

      // Create an offscreen image to support double buffering
      offscreen = createImage(Xsize,Ysize);
      buffer_screen = offscreen.getGraphics();
      buffer_screen.setFont(myfont);

      // Initialise the screen variable so the draw methods can paint onto it
      screen = getGraphics();

      // What the factor to convert a nitrogen pp to y space in graph is
      comp_y_factor = Ygraph_size / (comp_max_height - comp_min_height);
      System.out.println("comp_y_factor:"+comp_y_factor);

      // What the compartment origins will be
      comp_x_origin = new int[compartments];
      for (x=0; x<compartments; x++)
      {
         comp_x_origin[x]=Xgraph_origin+(comp_width*x)+(bordersize*x);
      }

      // Total number of ticks. This number increments every screen draw
      // therefore you can create objects on the screen based on the tick
      // you are currently at
      total_ticks = 1000;

      // Factor between depth and how far the diver is down from origin
      diver_y_factor = (Ydiver_size-30) / max_depth;
      System.out.println("diver_y_factor:"+diver_y_factor);

      // Images to use for the diver frames
      diver_frames=8;
      diverframe = 0;
      diver = new Image[diver_frames];
      for(x=0; x<diver_frames; x++)
      {
         diver[x]=getImage(getCodeBase(),"images/diver"+x+".gif");
      }

      // Bubbles to move to the surface from the diver
      ticks_per_bubble = 30;
      no_bubbles = 3;
      bubble = new Bubble[no_bubbles];

      // To start with show the intro screen
      show_intro = true;

      // Enable events and then ask for keyboard focus
      this.enableEvents(AWTEvent.MOUSE_EVENT_MASK
                       |AWTEvent.KEY_EVENT_MASK);
      this.requestFocus();  
   }

   public void paint (Graphics g)
   {
      if (show_intro)
      {
         draw_intro();
      }
      else
      {
         draw_sim(-1);
      }
   }

   public void run()
   {
      int tick_no = 1;
      double minutes_per_tick = .25;

      Thread.currentThread().setPriority(Thread.NORM_PRIORITY);

      while(true)
      {
         try
         {
            Thread.sleep(sleep_time);
         }
         catch (InterruptedException e)
         {
            break;
         }

         if (show_intro)
         {
            draw_intro();
         }
         else
         {
            // Increment the tick number (cycles round if gets to max)
            tick_no = (tick_no+1) % total_ticks;

            // Increment the dive time
            dive_time+=minutes_per_tick;

            // Process a sample of the dive and redraw the screen
            decomodel.ProcessSample(dive_depth,dive_time);
            draw_sim(tick_no);
         }
      }
   }

   public void start()
   {
      if (diver_thread == null)
      {
         diver_thread = new Thread(this);
         diver_thread.start();
      }
   }

   public void stop()
   {
      diver_thread = null;
   }

   public void draw_intro()
   {
      // AWT Components
      Button ok;

      ok = new Button("Ok");

      this.add("South",ok);
 //     pack();
 //     show();

/*
      // Blank the whole screen before redrawing
      buffer_screen.setColor(Color.black);
      buffer_screen.fillRect(0,0,Xsize,Ysize);

      // Display the introduction
      buffer_screen.setColor(Color.green);
      buffer_screen.drawString("Simulation of a Haldane decompression model", bordersize,20);
      buffer_screen.drawString("         (c) Fraser Kirkpatrick 2004",bordersize,30);
      buffer_screen.drawString("Use the up and down arrow keys to make the", bordersize,50);
      buffer_screen.drawString("diver ascend or decend. Tissue compartments", bordersize,60);
      buffer_screen.drawString("which have nitrogen bubbles forming are", bordersize,70);
      buffer_screen.drawString("shown in red. NB this model should not be", bordersize,80);
      buffer_screen.drawString("used for real dive planning!", bordersize,90);
      buffer_screen.drawString("Press the mouse over this pane to start", bordersize, 120);

      screen.drawImage(offscreen,0,0,this);
*/
   }

   public void draw_sim(int tick_no)
   {
      int x;
      int graph_height;
      double exact_graph_height;
      int diver_x;
      int diver_y;

      // Blank the whole screen before redrawing
      buffer_screen.setColor(Color.black);
      buffer_screen.fillRect(0,0,Xsize,Ysize);

      // Make a panel for the diver
      buffer_screen.setColor(Color.blue);
      buffer_screen.fillRect(Xdiver_origin,Ydiver_origin,Xdiver_size,Ydiver_size);
      buffer_screen.setColor(Color.green);
      buffer_screen.drawRect(Xdiver_origin-1,Ydiver_origin-1,Xdiver_size+2,Ydiver_size+2);

      // Get the compartment data from the decompression model
      data = decomodel.GetCompartments();

      // This section draws the diver and associated bubbles etc.
      // Increment the diver image and then draw it
      diverframe = (diverframe+1) % diver_frames;
      diver_x = Xdiver_origin+(Xdiver_size/2)-20;
      diver_y = (int)(Ydiver_origin+(dive_depth*diver_y_factor));
      buffer_screen.drawImage(diver[diverframe],diver_x,diver_y,this);

      // this procedure creates and maintains objects on the screen only do this
      // if this is a genuine tick (ie the dive time has incremented)
      if (tick_no != -1)
      {
         process_objects(tick_no, diver_x, diver_y);
      }

      // move the bubbles then draw them
      for (x=0; x<no_bubbles; x++)
      {
         if(bubble[x] != null)
         {
            if (bubble[x].x_coord>Xdiver_origin && bubble[x].y_coord>Ydiver_origin+15)
            {
               bubble[x].Tick();
               buffer_screen.setColor(Color.white);
               buffer_screen.drawOval(bubble[x].x_coord,bubble[x].y_coord,bubble[x].size,bubble[x].size);
            }
         }
      }

      // This section draws the compartment graphs
      for (x=0; x<compartments; x++)
      {

         buffer_screen.setColor(Color.green);
         buffer_screen.drawString("Ht:"+data[x].halftime,comp_x_origin[x],Yht_text_origin);

         if(data[x].bubbles == true)
         {
            buffer_screen.setColor(Color.red);
         }
         else
         {
            buffer_screen.setColor(Color.yellow);
         }

         exact_graph_height = comp_y_factor * (data[x].pp_nitrogen-comp_min_height);
         graph_height = (int) exact_graph_height;
         buffer_screen.fillRect(comp_x_origin[x],(Ygraph_origin+Ygraph_size-graph_height),comp_width,graph_height);

         // The graph outline
         buffer_screen.setColor(Color.green);
         buffer_screen.drawRect(comp_x_origin[x],Ygraph_origin,comp_width,Ygraph_size);
      }

      // Draw the ambient nitrogen partial pressure over the graphs
      buffer_screen.setColor(Color.green);
      exact_graph_height = comp_y_factor * (decomodel.GetAmbNitPP()-comp_min_height);
      graph_height = (int) exact_graph_height;
      buffer_screen.drawLine(Xgraph_origin,Ygraph_origin+Ygraph_size-graph_height,Xgraph_origin+Xgraph_size,Ygraph_origin+(Ygraph_size-graph_height));

      // Print the dive time and depth at the bottom of the screen
      buffer_screen.drawString("Depth:"+dive_depth+"m", Xgraph_origin,Yht_text_origin+20);
      buffer_screen.drawString("Dive Time:"+(int)dive_time+"mins", Xgraph_origin+(Xgraph_size/2),Yht_text_origin+20);

      screen.drawImage(offscreen,0,0,this);
   }

   private void process_objects(int tick_no, int diver_x, int diver_y)
   {
      int x;

      // if this tick is an appropriate number, then create a bubble
      if (tick_no % ticks_per_bubble < no_bubbles)
      {
         bubble[tick_no % ticks_per_bubble]= new Bubble(diver_x+40,diver_y+10);
      }

   }

   public void processKeyEvent(KeyEvent event)
   {
      int key_code;

      // Using the KEY_TYPED constant here only fires for keys which actually
      // represent letters so to get the arrow  keys we need KEY_PRESSED
      if (event.getID() == KeyEvent.KEY_PRESSED)
      {
         key_code = event.getKeyCode();
                                                                                
         if (key_code == KeyEvent.VK_DOWN && dive_depth < max_depth)
         {
            dive_depth++;
                                                                                
            // Force the applet to repaint at this instant
            draw_sim(-1);
         }
         if (key_code == KeyEvent.VK_UP && dive_depth > min_depth)
         {
            dive_depth--;
                       
            // Force the applet to repaint at this instant
            draw_sim(-1);
         }
      }
      else
      {
         super.processKeyEvent(event);
      }
                       
   }

   public void processMouseEvent(MouseEvent event)
   {
      if (event.getID() == MouseEvent.MOUSE_PRESSED && show_intro)
      {
         show_intro = false;
         draw_sim(-1);
      }
      else
      {
         super.processMouseEvent(event);
      }
   }
}
