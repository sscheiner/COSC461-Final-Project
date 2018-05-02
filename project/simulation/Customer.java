
package grocerystorecheckout;

import static grocerystorecheckout.GroceryStore.MeanStoreArrivalTime;
import static grocerystorecheckout.GroceryStore.exponential;
import static grocerystorecheckout.GroceryStore.stream;
import java.util.ArrayList;
import static grocerystorecheckout.GroceryStore.checkoutArrival;

/**
 * We assume every customer that enters store, next selects goods and then enters checkout line.
 * 
 * @author dcarro8
 */
public class Customer {
    
    private long serviceTime;
    // List of customers that entered store
    public static ArrayList customersEnteredStore = new ArrayList<Customer>();
    // List of customers inside store
    public static ArrayList customersInStore = new ArrayList<Customer>();
    // List of customers that selected goods
    public static ArrayList customersSelectedGoods = new ArrayList<Customer>();
    // List of customers that selected entereed checkout line
    public static ArrayList customersEnteredCheckoutLine = new ArrayList<Customer>();
    // List of customers that paid bill
    public static ArrayList customersPaidBill = new ArrayList<Customer>();
     // List of customers that departed store
    public static ArrayList customersDepartedStore = new ArrayList<Customer>();

    private static int customerId = 1; // unique one-up customer number
    private int id;
    private double enterTime; // time customer enters store
    private double departTime;  // time customer leaves store
    private double enterQueueTime; // time customer enters queue
    private double departQueueTime; // time customer departs queue
     
    public Customer() {
       this.id = customerId++; 
       this.enterTime = 0;
       this.departTime = 0;
       this.enterQueueTime = 0;
       this.departQueueTime = 0;
    }
    
    // Record customers entering store
    public void enterStore(Event evt) {
      customersEnteredStore.add(this);
      customersInStore.add(this);
      this.enterTime = evt.getTime();
    }
    
    // Record customers that selected goods
    public void selectGoods() {
      customersSelectedGoods.add(this);
    }
     
    // Record customers that stood in checkout line
    public void standInLine(Event evt) {
      customersEnteredCheckoutLine.add(this); 
      this.setEnterQueueTime(evt.getTime());
    }
    
    // Record customers that paid their bill
    public void payBill() {
      customersPaidBill.add(this);
    }
    
    // Record customers that departed store
    public void departStore(Event evt) {
      customersDepartedStore.add(this);
      customersInStore.remove(this); 
      this.setDepartTime(evt.getTime());
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
    
    // returns the number of customers that entered store
    public static int getCustomersEnteredStore() {
        return customersEnteredStore.size();
    }
    
     // returns the number of customers that selected goods
    public static int getCustomersSelectedGoods() {
        return customersSelectedGoods.size();
    }
    
    // returns the number of customers that entered checkout line
    public static int getCustomersEnteredCheckoutLine() {
        return customersEnteredCheckoutLine.size();
    }
    
     // returns the number of customers that paid their bill
    public static int getCustomersPaidBill() {
        return customersPaidBill.size();
    }
    
    // returns the number of customers that departed store
    public static int getCustomersDepartedStore() {
        return customersDepartedStore.size();
    }
   
    // returns the list of customers that entered store
    public static ArrayList<Customer> getEnteredStore() {
        return customersEnteredStore;
    }
    
    // returns the list of customers that departed store
    public static ArrayList<Customer> getDepartedStore() {
        return customersDepartedStore;
    }
    
    // returns the list of customers that entered checkout line
    public static ArrayList<Customer> getEnteredCheckout() {
        return customersEnteredCheckoutLine;
    }
    
    
    // return customer id
    public int getId() {
        return id;
    }
    
    public double getEnterTime() {
     return enterTime;
    }
    
    public double getDepartTime() {
     return departTime;
    }
    
    // set depart store time
    public void setDepartTime(Double time) {
     this.departTime = time;
    }
    
    public double getEnterQueueTime() {
     return enterQueueTime;
    } 
    
    public double getDepartQueueTime() {
     return departQueueTime;
    } 
     
    public void setEnterQueueTime(double time) {
       this.enterQueueTime = time;
    }
    
    public void setDepartQueueTime(double time) {
       this.departQueueTime = time;
    }
}
