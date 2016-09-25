public class fktemp
{

   public static void main(String [] args)
   {
      int x, y;

      x=6;
      y=3;
 
      for (int z=0; z<(y*3); z++)
      {
         System.out.println("x="+x);
         x=(x+1)%y;
      }
   }
}
