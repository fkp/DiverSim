import java.lang.reflect.Array;

public class test
{

   public static void main(String [] args)
   {
      Model diver;
      double surface_pp_nitrogen = 0.79;
      double units_depth_per_atmosphere = 10;
      double gas_nitrogen_fraction = .79;
      short compartments = 5;
      double halftime[] = new double[compartments];
      halftime[0] = 5;
      halftime[1] = 10;
      halftime[2] = 20;
      halftime[3] = 40;
      halftime[4] = 75;

      double starting_amb_press = 1;
      Compartment [][] data = new Compartment[21][];

      System.out.println(Array.getLength(halftime)+" vs "+compartments);

      diver = new Model(surface_pp_nitrogen,units_depth_per_atmosphere,compartments,halftime,starting_amb_press,gas_nitrogen_fraction,true);

      // this dive makes the 20 min halftime compartment form bubbles
      diver.ProcessSample(10,5);
      data[0] = diver.GetCompartments();
      diver.ProcessSample(20,6);
      data[1] = diver.GetCompartments();
      diver.ProcessSample(30,7);
      data[2] = diver.GetCompartments();
      diver.ProcessSample(40,8);
      data[3] = diver.GetCompartments();
      diver.ProcessSample(40,25);
      data[4] = diver.GetCompartments();
      diver.ProcessSample(40,30);
      data[5] = diver.GetCompartments();
      diver.ProcessSample(40,35);
      data[6] = diver.GetCompartments();
      diver.ProcessSample(40,40);
      data[7] = diver.GetCompartments();
      diver.ProcessSample(40,45);
      data[8] = diver.GetCompartments();
      diver.ProcessSample(40,50);
      data[9] = diver.GetCompartments();
      diver.ProcessSample(40,55);
      data[10] = diver.GetCompartments();
      diver.ProcessSample(40,60);
      data[11] = diver.GetCompartments();
      diver.ProcessSample(40,65);
      data[12] = diver.GetCompartments();
      diver.ProcessSample(40,70);
      data[13] = diver.GetCompartments();
      diver.ProcessSample(40,80);
      data[14] = diver.GetCompartments();
      diver.ProcessSample(40,85);
      data[15] = diver.GetCompartments();
      diver.ProcessSample(40,90);
      data[16] = diver.GetCompartments();
      diver.ProcessSample(40,100);
      data[17] = diver.GetCompartments();
      diver.ProcessSample(40,105);
      data[18] = diver.GetCompartments();
      diver.ProcessSample(10,130);
      data[19] = diver.GetCompartments();
      diver.ProcessSample(10,131);
      data[20] = diver.GetCompartments();

      System.out.println("********** Results **********");
      print_info(data);
   }

   public static void print_info(Compartment [][] data)
   {
      // loop round the results
      for (int res = 0; res < Array.getLength(data); res++)
      {
         System.out.println("Sample #"+res);

         // loop round the compartments in each result
         for (int num = 0; num < Array.getLength(data[res]); num++)
         {
            System.out.print(" Cp:"+num+" Ht:"+data[res][num].halftime+" Pp:"+data[res][num].pp_nitrogen);
            if (data[res][num].bubbles == true)
            {
               System.out.println("  <- BUBBLING!");
            }
            else
            {
               System.out.println("");
            }
         }
      }
   }
}
