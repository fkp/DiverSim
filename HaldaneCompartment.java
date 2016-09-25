public class HaldaneCompartment
{
   /*****************************************************************
   * Fraser Kirkpatrick 30/01/2004
   * Class which contains information about a tissue compartment
   *****************************************************************/

   /*****************************************************************
   * Private variables
   *****************************************************************/
   private Compartment Compart;
   private boolean debug_mode;

   /*****************************************************************
   * Constructor for this class
   *****************************************************************/
   public HaldaneCompartment (double p_pp_nitrogen_absorbed
                             ,double p_halftime
                             ,boolean p_debug_mode)
   {
      Compart = new Compartment(p_pp_nitrogen_absorbed,p_halftime);
      debug_mode = p_debug_mode;
   }

   /*****************************************************************
   * Method to update the nitrogen pp for this compartment and return
   * the new value
   *****************************************************************/
   public boolean Update(double nitro_pp, double time)
   {
      /*****************************************************************
      * Haldane's formula to calculate the tissue nitrogen content goes
      * like this:
      *    pp(t) = pp(t-1) + (ppat-pp(t-1))*(1-2^(-t/Ht))
      *
      * where pp(t) = nitrogen partial pressure at time t
      *       pp(t-1) = nitrogen pp at time t-1
      *       ppat = nitrogen pp in atmosphere
      *       t = time tissue exposed to ppat
      *       Ht = tissue halftime
      *****************************************************************/
      if (debug_mode)
      {
         System.out.print(toString()+": ht "+Compart.halftime+" pp "+Compart.pp_nitrogen+" -> ");
      }

      Compart.pp_nitrogen = Compart.pp_nitrogen+(nitro_pp-Compart.pp_nitrogen)*
                            (1-Math.pow(2,(-time/Compart.halftime)));

      if (debug_mode)
      {
         System.out.println(Compart.pp_nitrogen);
      }
      /*****************************************************************
      * If the pp nitrogen is more than twice the ambient pp nitrogen
      * then by Haldane's model, nitrogen bubbles will develop in this
      * tissue. Therefore ascertain whether or not this applies to this
      * tissue.
      *****************************************************************/
      if (Compart.pp_nitrogen > (nitro_pp * 2))
      {
         Compart.bubbles = true;
      }
      else
      {
         Compart.bubbles = false;
      }

      /*****************************************************************
      * Return that the update was successful
      *****************************************************************/
      return true;
   }

   /*****************************************************************
   * Method to return a pointer to the compartment class
   *****************************************************************/
   public Compartment GetData()
   {
      return Compart;
   }
}

