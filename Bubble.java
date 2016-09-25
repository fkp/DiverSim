public class Bubble
{
   /*****************************************************************
   * Fraser Kirkpatrick 06/02/2004
   * Class which holds a bubble to display on the diversim gui
   *****************************************************************/
   public int x_coord;
   public int y_coord;
   public int size;

   /*****************************************************************
   * Constructors for this class.
   *****************************************************************/
   public Bubble (int start_x, int start_y)
   {
      x_coord = start_x;
      y_coord = start_y;
      size = 3;
   }

   /*****************************************************************
   * Every time the gui ticks, move the bubble up and to the left
   * and increase its size as it gets to the surface
   *****************************************************************/
   public void Tick()
   {
      x_coord-=10;
      y_coord-=15;

      /*****************************************************************
      * As we are incrementing the y coord by an odd number, then only
      * increase the size of the bubble every second increment.
      *****************************************************************/
      if (y_coord%2 == 0)
      {
         size++;
      }
   }
}
