
package grocerystorecheckout;
import java.util.*;
/**
 *
 * @author dcarro8
 */
public class GroceryStore {

public static double Clock, MeanStoreArrivalTime, 
        MeanCheckoutTime, MeanSelectGoodsTime,
        MeanServiceTime, SIGMA,
LastEventTime,TotalBusy, MaxQueueLength, SumResponseTime;
public static long NumberOfCustomers, QueueLength, NumberInService,
TotalCustomersServiced, NumberOfDepartures, LongService;
public final static int enterStore = 1;
public final static int selectGoods = 2;
public final static int standInLine = 3;
public final static int payBill = 4;
public final static int departStore = 5;

public static EventList FutureEventList;
public static Queue Customers;
public static Random stream;
public final static long DURATION_IN_MINUTES = 60;
public static long initialTime;

    public static void main(String argv[]) {
    MeanStoreArrivalTime = 4.5;
    MeanSelectGoodsTime = 5.5;    
    //MeanCheckoutTime = 4.5;
    MeanServiceTime = 3.2;
    SIGMA = 0.6;
    TotalCustomersServiced = 0;
    long seed = 1000; 
    stream = new Random(seed); // initialize rng stream
    FutureEventList = new EventList();
    Customers = new Queue(); // holds customers in checkout line
    Initialization();
     
    
    // Loop until 60 minutes have elapsed
    while (Clock < (initialTime + DURATION_IN_MINUTES)) {
      Event evt = (Event)FutureEventList.getMin(); // get imminent event
      FutureEventList.dequeue(); // be rid of it
      Clock = evt.getTime(); // advance simulation time
      if (evt.getType() == enterStore)
          ProcessCustomerArrival(evt);
      else if (evt.getType() == selectGoods)
          ProcessCustomerSelectGoods(evt);
      else if (evt.getType() == standInLine) {
          ProcessQueueArrival(evt);
      } else {  // must be departStore event
         ProcessQueueDeparture(evt);
      }
      Clock++; // increment clock one minute
    } 
    ReportGeneration();
   }

   public static void Initialization() {
    Clock = 0.0;
    QueueLength = 0;
    NumberInService = 0;
    LastEventTime = 0.0;
    TotalBusy = 0;
    MaxQueueLength = 0;
    SumResponseTime = 0;
    NumberOfDepartures = 0;
    LongService = 0;
    
    // create first store arrival event
    Event evt = new Event(enterStore, exponential(stream,MeanStoreArrivalTime), new Customer());
    FutureEventList.enqueue(evt);
      
    // create first customer select goods event
    evt = new Event(selectGoods, exponential(stream,MeanSelectGoodsTime), new Customer());
    FutureEventList.enqueue(evt);
    
    // create first checkout event
    evt = new Event(standInLine, exponential(stream,MeanServiceTime), new Customer());
    FutureEventList.enqueue(evt);
    
    long initialTime = System.currentTimeMillis();
   }
   
   public static void ProcessCustomerArrival(Event evt) {
     evt.getCustomer().enterStore();
     Event customer_arrival = new Event(enterStore,Clock + exponential(stream, MeanStoreArrivalTime), evt.getCustomer());
     FutureEventList.enqueue(customer_arrival);
     LastEventTime = Clock;
   }
   
    public static void ProcessCustomerSelectGoods(Event evt) {
     evt.getCustomer().selectGoods();
     Event customer_selectGoods = new Event(selectGoods,Clock + exponential(stream, MeanSelectGoodsTime), evt.getCustomer());
     FutureEventList.enqueue(customer_selectGoods);
     LastEventTime = Clock;
   }  
    public static void ProcessQueueArrival(Event evt) {
      Customers.enqueue(evt);
      QueueLength++;
    
      // if the server is idle, fetch the event, do statistics
      // and put into service
     if(NumberInService == 0)
        ScheduleDeparture(evt.getCustomer());
     else TotalBusy += (Clock - LastEventTime);
     // server is busy
      // adjust max queue length statistics
     if(MaxQueueLength < QueueLength)
        MaxQueueLength = QueueLength;
     // schedule the next arrival
     Event next_checkout = new Event(standInLine,Clock + exponential(stream, MeanServiceTime), evt.getCustomer());
     FutureEventList.enqueue(next_checkout);
     LastEventTime = Clock;
    }

    public static void ScheduleDeparture(Customer customer) {
    double ServiceTime;
    // get the job at the head of the queue
    while (( ServiceTime = normal(stream, MeanServiceTime, SIGMA)) < 0 );
      Event depart = new Event(departStore,Clock+ServiceTime, customer);
      FutureEventList.enqueue(depart);
      NumberInService = 1;
      QueueLength--;
    }

    public static void ProcessQueueDeparture(Event evt) {
      // get the customer description
      Event finished =(Event)Customers.dequeue();
      // if there are customers in the queue then schedule
      // the departure of the next one
      while(QueueLength > 0)
        ScheduleDeparture(evt.getCustomer());
      NumberInService = 0;
      // measure the response time and add to the sum
      //System.out.println("Clock = " + Clock);
     double response = (Clock - finished.getTime());
      //System.out.println("Response = " + response);
      //System.out.println("Finished = " + finished.get_time());
      SumResponseTime += response;
      if( response > 4.0) LongService++; // record long service
      
      TotalBusy += (Clock - LastEventTime );
      NumberOfDepartures++;
      LastEventTime = Clock;
        
      evt.getCustomer().departStore();  
    }
    
    
   // public int getCustomersQueuCount() {
      //  return Customers.getCustomerCount();
  //  }

    public static void ReportGeneration() {
    double serverUtilization = TotalBusy/Clock;
    double AVGR = SumResponseTime/TotalCustomersServiced;
    double PC4 = ((double)LongService)/TotalCustomersServiced;
    System.out.println("SINGLE SERVER QUEUE SIMULATION - GROCERY STORE CHECKOUT COUNTER ");
    System.out.println("\tMEAN STORE ARRIVAL TIME " + MeanStoreArrivalTime );
    System.out.println("\tMEAN CHECKOUT LINE ARRIVAL TIME " + MeanCheckoutTime );
    System.out.println("\tMEAN SERVICE TIME " + MeanServiceTime );
    System.out.println("\tSTANDARD DEVIATION OF SERVICE TIMES " + SIGMA );
    System.out.println("\tNUMBER OF CUSTOMERS SERVED " + TotalCustomersServiced);
    System.out.println();
    System.out.println("\tSERVER UTILIZATION " + serverUtilization );
    System.out.println("\tMAXIMUM LINE LENGTH " + MaxQueueLength);
    System.out.println("\tAVERAGE RESPONSE TIME " + AVGR + " MINUTES" );
    System.out.println("\tPROPORTION WHO SPEND FOUR MINUTES OR MORE IN SYSTEM " + PC4 );
    System.out.println("\tSIMULATION RUNLENGTH " + Clock + " MINUTES");
    System.out.println("\tNUMBER OF DEPARTURES " + TotalCustomersServiced );
    }

    public static double exponential(Random rng, double mean) {
    return -mean *Math.log(rng.nextDouble());
    }
    public static double SaveNormal;
    public static int NumNormals = 0;
    public static final double PI = 3.1415927 ;

    public static double normal(Random rng, double mean, double sigma) {
    double ReturnNormal; // should we generate two normals?
    if(NumNormals == 0) {
    double r1 = rng.nextDouble();
    double r2 = rng.nextDouble();
    ReturnNormal = Math.sqrt(-2*Math.log(r1))*Math.cos(2*PI*r2);
    SaveNormal= Math.sqrt(-2*Math.log(r1))*Math.sin(2*PI*r2);
    NumNormals=1;
    }
    else {
      NumNormals=0;
      ReturnNormal = SaveNormal;
    }
    return ReturnNormal*sigma + mean ;
    }

}
