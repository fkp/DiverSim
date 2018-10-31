public class Compartment
{
   /*****************************************************************
   * Class which holds public variables containing tissue data
   *****************************************************************/
   public double pp_nitrogen;
   public boolean bubbles;
   public double halftime;

   /*****************************************************************
   * Constructors for this class. There are two instances where these
   * classes are created, first when the model is kicked off (at
   * which point we assume none of the nitrogen gradients are causing
   * bubbling) and also whenever the program using the model asks for
   * an update of whats in the tissues.
   *****************************************************************/
   public Compartment (double p_pp_nitrogen, double p_ht)
   {
      pp_nitrogen = p_pp_nitrogen;
      halftime = p_ht;
      bubbles = false;
   }

   public Compartment (double p_pp_nitrogen, double p_ht, boolean p_bubb)
   {
      pp_nitrogen = p_pp_nitrogen;
      halftime = p_ht;
      bubbles = p_bubb;
   }
}
