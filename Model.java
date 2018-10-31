public class Model
{
   /*****************************************************************
   * Class which implements a decompression model.
   *****************************************************************/

   /*****************************************************************
   * Private variables - general configuration params
   *****************************************************************/
   private short no_compartments;
   private double units_depth_per_atmos;
   private double gas_nitrogen_fraction;
   private boolean debug_mode;
   static final double c_atmos_press = 1;

   /*****************************************************************
   * Private variables - specifics of the decompression model
   *****************************************************************/
   private double last_amb_press;
   private double last_dive_time;
   private HaldaneCompartment compart[];

   /*****************************************************************
   * Constructor for this class
   *****************************************************************/
   public Model(double p_surface_pp_nitrogen
               ,double p_units_depth_per_atmos
               ,short p_no_compartments
               ,double [] p_halftime
               ,double p_starting_amb_press
               ,double p_gas_nitrogen_fraction
               ,boolean p_debug_mode)
   {
      /*****************************************************************
      * Initialise the class global variables
      *****************************************************************/
      no_compartments = p_no_compartments;
      units_depth_per_atmos = p_units_depth_per_atmos;
      gas_nitrogen_fraction = p_gas_nitrogen_fraction;
      debug_mode = p_debug_mode;

      /*****************************************************************
      * Initialise the diving specific variables
      *****************************************************************/
      if (debug_mode)
      {
         System.out.println(toString()+": Parameters passed to class are:");
         System.out.println(" surface_pp_nitrogen:"+p_surface_pp_nitrogen);
         System.out.println(" units_depth_per_atmos:"+p_units_depth_per_atmos);
         System.out.println(" compartments:"+p_no_compartments);
         System.out.println(" p_starting_amb_press:"+p_starting_amb_press);
      }

      compart = new HaldaneCompartment[no_compartments];

      for (int num=0; num<p_no_compartments; num++)
      {
         if (debug_mode)
         {
            System.out.println(this.toString()+": Creating compartment "+num+" with params:");
            System.out.println(" p_surface_pp_nitrogen:"+p_surface_pp_nitrogen);
            System.out.println(" p_halftime[]:"+p_halftime[num]);
         }

         compart[num] = new HaldaneCompartment (p_surface_pp_nitrogen
                                               ,p_halftime[num]
                                               ,p_debug_mode);
      }
      last_amb_press = p_starting_amb_press;
      last_dive_time = 0;
   }

   /*****************************************************************
   * Method to process a sample of depth/dive time
   *****************************************************************/
   public boolean ProcessSample (double new_depth, double new_time)
   {
      double new_amb_press;
      double av_amb_press;
      double av_nitro_pp;
      double sample_length;
      boolean failed = false;

      /*****************************************************************
      * First convert the depth into the ambient pressure. Divide the
      * depth by the units of depth per atmospheres and add atmospheric
      * pressure.
      *****************************************************************/
      new_amb_press = new_depth / units_depth_per_atmos + c_atmos_press;

      /*****************************************************************
      * Now work out the average ambient pressure for the period we are
      * sampling. Perhaps if we were being conservative we could simply
      * take deepest of the two.. but I'll settle for an average.
      *****************************************************************/
      av_amb_press = (last_amb_press + new_amb_press) / 2;

      /*****************************************************************
      * Now convert the average ambient pressure to the equivalent
      * nitrogen partial pressure
      *****************************************************************/
      av_nitro_pp = av_amb_press * gas_nitrogen_fraction;

      /*****************************************************************
      * Now work out the length of time for the period we are sampling
      *****************************************************************/
      sample_length = new_time - last_dive_time;

      if (debug_mode)
      {
         System.out.println(toString()+": About to update compts for nitrogen pp:"+av_nitro_pp+", sample length "+sample_length);
      }

      /*****************************************************************
      * We now just need to get the compartments to update their
      * nitrogen levels
      *****************************************************************/
      for (int num = 0; num < no_compartments; num++)
      {
         if (compart[num].Update(av_nitro_pp,sample_length) == false)
         {
            // flag that an update failed
            failed = true;
         }
      }

      /*****************************************************************
      * We're done with the last values so reset them to the current
      * ones for the next time
      *****************************************************************/
      last_amb_press = new_amb_press;
      last_dive_time = new_time;

      if (failed)
      {
         // a compartment failed to update
         return false;
      }
      else
      {
         // the sample was successfully processed
         return true;
      }
   }

   /*****************************************************************
   * This method will return an array of compartment values to the
   * controlling program. Get a compartment class from each
   * HaldaneCompartment instances and then create a duplicate to 
   * return, so that the private variables cannot be altered by any
   * parent classes.
   *****************************************************************/
   public Compartment[] GetCompartments()
   {
      Compartment CompartmentData [] = new Compartment[no_compartments];
      Compartment thiscomp;

      for (int num = 0; num < no_compartments; num ++)
      {
         thiscomp = compart[num].GetData();
         CompartmentData[num] = new Compartment (thiscomp.pp_nitrogen
                                                ,thiscomp.halftime
                                                ,thiscomp.bubbles);
      }

      return CompartmentData;
   }

   /*****************************************************************
   * This method will return a number containing the ambient partial
   * pressure of nitrogen at the current depth (or the depth given
   * by the last dive sample)
   *****************************************************************/
   public double GetAmbNitPP ()
   {
      return last_amb_press * gas_nitrogen_fraction;
   }
}

