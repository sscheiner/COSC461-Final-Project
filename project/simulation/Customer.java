
package grocerystorecheckout;

import static grocerystorecheckout.GroceryStore.FutureEventList;
import static grocerystorecheckout.GroceryStore.MeanStoreArrivalTime;
import static grocerystorecheckout.GroceryStore.exponential;
import static grocerystorecheckout.GroceryStore.standInLine;
import static grocerystorecheckout.GroceryStore.stream;
import java.util.ArrayList;

/**
 *
 * @author dcarro8
 */
public class Customer {
    
    private long serviceTime;
    
    public static ArrayList customersInStore = new ArrayList<Customer>();

    public Customer() {
        enterStore(); // new customer enters store
    }
    
    public void enterStore() {
        customersInStore.add(this);
        //selectGoods(); // customer selects goods from shelves
    }
    
    public void selectGoods() {
      //standInLine();  
    }
     
    public void standInLine() {
    }
    
    public void payBill() {
    }
    
    public void departStore() {
       customersInStore.remove(this); 
    }
    
    public long getServiceTime() {
        return serviceTime;
    }
    
    public void setServiceTime(long serviceTime) {
       this.serviceTime = serviceTime; 
    }
    
    // returns the number of customers inside the store
    public static int getCustomersInStore() {
        return customersInStore.size();
    }
   
  
}
