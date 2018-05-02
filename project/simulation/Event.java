package grocerystorecheckout;

/**
 *
 * @author dcarro8
 */

  public class Event{
   private double time ;
   private int type;
   private Customer customer;
   public Event ( int _type , double _time, Customer customer ){
     type = _type ;
     time = _time ;
     this.customer = customer;
   }

  public double getTime(){
    return time ;
  }
  
  public int getType (){
     return type;
  }
  
  public Customer getCustomer (){
     return customer;
  }
}  

