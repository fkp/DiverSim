public class Fish
{
   /*****************************************************************
   * Fraser Kirkpatrick 23/02/2004
   * Class which holds a fish to display on the diversim gui
   *****************************************************************/
   public int x_coord;
   public int y_coord;
   public String type;
   public int frame;
   public int fish_frames;

   /*****************************************************************
   * Constructor for this class.
   *****************************************************************/
   public Fish (int start_x, int start_y, String start_type, int frames)
   {
      x_coord = start_x;
      y_coord = start_y;
      type = start_type;
      fish_frames = frames;
      frame = 0;
   }

   /*****************************************************************
   * Every time the gui ticks, move the fish to the left
   *****************************************************************/
   public void Tick()
   {
      x_coord-=(int) (Math.random()*10)/3;
      frame = (frame+1) % fish_frames;
   }
}
