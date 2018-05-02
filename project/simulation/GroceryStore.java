
package grocerystorecheckout;
import java.util.*;
/** We assume every customer enters store, shops for goods and then pay for them.
 *  A customer progresses through 6 states in order(enterStore, selectGoods, 
 * checkoutArrival checkoutDeparture, payBill, departStore). 
 *  We do not expect customers to  enter store and depart without purchasing
 *  goods. We assume store is empty to start off.
 * 
 * @author dcarro8
 */
public class GroceryStore {

public static double Clock, MeanStoreArrivalTime, MeanServiceTime,
        MeanCheckoutTime, MeanSelectGoodsTime, MeanDepartStoreTime,
        MeanCheckoutWaitTime, MeanPayBillTime, SIGMA, MeanCheckoutArrivalTime,
LastEventTime,TotalBusy, MaxQueueLength, SumResponseTime;
public static long NumberOfCustomers, QueueLength, NumberInService,
TotalCustomersServiced, NumberOfDepartures;
public final static int enterStore = 1;
public final static int selectGoods = 2;
public final static int checkoutArrival = 3;
public final static int checkoutDeparture = 4;
public final static int payBill = 5;
public final static int departStore = 6;

public static EventList FutureEventList;
public static Queue Customers;
public static Random stream;
public final static long DURATION_IN_MINUTES = 60;
public static double An; // nth customer inter arrival time (n = 5)
public static double Sn; // nth customer service time (n = 5) 
public static Map<Double,Integer> customersAtTime = new HashMap();
public static Map<Double,Integer> queueAtTime = new HashMap();
public static Double TotalQueueTime = 0.0;
public static Double endSimulationTime = 0.0;

public static void main(String argv[]) {
    MeanStoreArrivalTime = 1.0; // arrival rate per customer mean
    MeanSelectGoodsTime = 7.5;    
    MeanCheckoutWaitTime = 3.2;
    MeanCheckoutArrivalTime = 1.0;
    MeanPayBillTime = 2.0;
    MeanServiceTime = 5.0;
    SIGMA = 0.6;
    TotalCustomersServiced = 0;
    long seed = 1000; 
    stream = new Random(seed); // initialize rng stream
    FutureEventList = new EventList();
    Customers = new Queue(); // holds customers in checkout line
    Clock = 0;
    Initialization();
      
    // Loop until 60 minutes have elapsed
    while (Clock < DURATION_IN_MINUTES) {
      Event evt = (Event)FutureEventList.getMin(); // get imminent event
      if (evt != null) {
        FutureEventList.dequeue(); // be rid of it
        if (evt.getType() == enterStore)
          ProcessEnterStore(evt);
         else if (evt.getType() == selectGoods)
          ProcessSelectGoods(evt);
         else if (evt.getType() == checkoutArrival)  
          ProcessCheckoutArrival(evt);
         else if (evt.getType() == checkoutDeparture)
           ProcessCheckoutDeparture(evt);
          else if (evt.getType() == payBill)
           ProcessPayBill(evt);
          else   
           ProcessDepartStore(evt);
          
          endSimulationTime = evt.getTime(); // track end time of simulation
          }
      //System.out.println("Customers at time(" + Clock + ") minute: " + Customer.getCustomersInStore());
      customersAtTime.put(Clock,Customer.getCustomersInStore());
      queueAtTime.put(Clock,Customers.getCustomerCount());
      Clock++; // increment clock one minute every program iteration
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
      
    // schedule first store arrival event
    Event evt = new Event(enterStore, exponential(stream,MeanStoreArrivalTime), new Customer());
    FutureEventList.enqueue(evt);
   }
   
   // Process a new customer arrival and transition them to select goods state
   // Also schedule next customer arrival
   public static void ProcessEnterStore(Event evt) {
     evt.getCustomer().enterStore(evt);
     //System.out.println("A new customer has entered the store");
     Event customerSelectGoods = new Event(selectGoods,Clock + exponential(stream, MeanSelectGoodsTime), evt.getCustomer());
     FutureEventList.enqueue(customerSelectGoods);
    }
   
    // Process customer select goods and transition them to stand in line state
    public static void ProcessSelectGoods(Event evt) {
     evt.getCustomer().selectGoods();
     //System.out.println("A customer has selected goods");
     
     Event customerStandInLine = new Event(checkoutArrival,Clock + exponential(stream,MeanCheckoutArrivalTime), evt.getCustomer());
     FutureEventList.enqueue(customerStandInLine);
     }  
    
    // Process customer wait in line and transition them to pay bill
    public static void ProcessCheckoutArrival(Event evt) {
      evt.getCustomer().standInLine(evt);
      //System.out.println("A customer has arrived at checkout line");
            
      Customers.enqueue(evt);
      QueueLength++;
    
      // if the server is idle, fetch the event, do statistics
      // and put into service
     if(NumberInService == 0) 
        ScheduleCheckoutDeparture(evt.getCustomer());
     else TotalBusy += (Clock - LastEventTime);
     // server is busy
      // adjust max queue length statistics
     if(MaxQueueLength < QueueLength)
        MaxQueueLength = QueueLength;
          
     // we add a new customer to store once each iteration (1 per minute)
     double simulatedInterArrivalTime = exponential(stream, MeanStoreArrivalTime);
     evt = new Event(enterStore, simulatedInterArrivalTime, new Customer());
     if (evt.getCustomer().getId() == 5) // record 5th customer inter arrival time
          An = simulatedInterArrivalTime;
          
     FutureEventList.enqueue(evt);
     LastEventTime = Clock;
    }
    
    // Process customer pay bill and transition them to depart store
    public static void ProcessPayBill(Event evt) {
     evt.getCustomer().payBill();
     //System.out.println("A customer has paid their bill");
     Event customerDepartStore = new Event(departStore,Clock + exponential(stream, MeanDepartStoreTime), evt.getCustomer());
     FutureEventList.enqueue(customerDepartStore);
   }

    public static void ScheduleCheckoutDeparture(Customer customer) {
      double serviceTime;
      // get the job at the head of the queue
      while (( serviceTime = normal(stream, MeanServiceTime, SIGMA)) < 0 );
      Event depart = new Event(checkoutDeparture,Clock+serviceTime, customer);
       if (customer.getId() == 5) // record 5th customer inter arrival time
          Sn = serviceTime;
      
      FutureEventList.enqueue(depart);
      NumberInService = 1;
      QueueLength--;
    }

    public static void ProcessCheckoutDeparture(Event evt) {
      // get the customer description
      Event finished =(Event)Customers.dequeue();
      // if there are customers in the queue then schedule
      // the departure of the next one
      if(QueueLength > 0)
        ScheduleCheckoutDeparture(evt.getCustomer());
      else 
        NumberInService = 0;
      
      // measure the response time and add to the sum
      double response = (Clock - finished.getTime());
      SumResponseTime += response;
       
      TotalBusy += (Clock - LastEventTime );
      NumberOfDepartures++;
      Event customerPayBill = new Event(payBill,Clock + exponential(stream, MeanPayBillTime), evt.getCustomer());
      FutureEventList.enqueue(customerPayBill);
      LastEventTime = Clock;
      
      evt.getCustomer().setDepartQueueTime(evt.getTime());
     }
        
    public static void ProcessDepartStore(Event evt) {
     evt.getCustomer().departStore(evt);
    // System.out.println("A customer has departed store");
    }
       

    public static void ReportGeneration() {
    double serverUtilization = TotalBusy/Clock;
    double AVGR = SumResponseTime/Customer.getCustomersPaidBill();
    System.out.println("SINGLE SERVER QUEUE SIMULATION - GROCERY STORE CHECKOUT COUNTER ");
    System.out.println("\tMEAN STORE ARRIVAL RATE (λ): " + MeanStoreArrivalTime  + " CUSTOMER PER MINUTE");
    System.out.println("\tNth(N=5) SIMULATED CUSTOMER STORE INTERARRIVAL TIME (An): " + An + " MINUTES");
    //System.out.println("\tMEAN CHECKOUT LINE WAIT TIME: " + MeanCheckoutWaitTime + " MINUTES");
    System.out.println("\tAVG INTER ARRIVAL TIME TO CHECKOUT LINE: " + MeanCheckoutArrivalTime + " MINUTES");
    System.out.println("\tSTANDARD DEVIATION OF SERVICE TIMES: " + SIGMA );
    System.out.println("\tNth(N=5) CUSTOMER SERVICE TIME (Sn): " + Sn + " MINUTES");
    System.out.println("\tMEAN CASHIER (SERVER) RATE (μ): " + MeanServiceTime + " MINUTES PER CUSTOMER");
    System.out.println("\tCUSTOMERS IN STORE AT TIME t L(t):");
    
    // Calculate time-average number of customers in store
    Double LTotal = 0.0;
    for (Double clock : customersAtTime.keySet()) {
      System.out.print(customersAtTime.get(clock));
      if (customersAtTime.get(clock)  > 0)
        LTotal += customersAtTime.get(clock);
      System.out.print(" " );
    }
    System.out.println("\n");
    // Calculate time-average number of customers in Checkout Queue
    System.out.println("\tCUSTOMERS IN QUEUE AT TIME t LQ(t):");
    Double LQTotal = 0.0;
    for (Double clock : queueAtTime.keySet()) {
      System.out.print(queueAtTime.get(clock));
      if (queueAtTime.get(clock) > 0)
       LQTotal += (queueAtTime.get(clock) - 1); //See Chpt 6 pg 16
      System.out.print(" " );
    }
    System.out.println();
    System.out.println("\tLONG-RUN TIME-AVERAGE NUMBER OF CUSTOMERS IN SYSTEM (L): " + LTotal/DURATION_IN_MINUTES);
    System.out.println("\tLONG-RUN TIME-AVERAGE NUMBER OF CUSTOMERS IN QUEUE (LQ): " + LQTotal/DURATION_IN_MINUTES + " CUSTOMERS PER MINUTE");
    Double TotalCustomersTimeInStore = 0.0;
    for (Customer cust : Customer.getEnteredStore()) {
        if (cust.getDepartTime() > 0)
          TotalCustomersTimeInStore += (cust.getDepartTime() - cust.getEnterTime());
        else 
         TotalCustomersTimeInStore += (DURATION_IN_MINUTES - cust.getEnterTime());
    }
    System.out.println("\tLONG-RUN AVERAGE_TIME SPENT IN STORE PER CUSTOMER (w): " + TotalCustomersTimeInStore/Customer.getEnteredStore().size() + " MINUTES");
    Double TotalCustomersTimeInQueue = 0.0;
    for (Customer cust : Customer.getEnteredCheckout()) {
        if (cust.getDepartQueueTime() > 0)
          TotalCustomersTimeInQueue +=  (cust.getDepartQueueTime() - cust.getEnterQueueTime());
        else
           TotalCustomersTimeInQueue +=   (DURATION_IN_MINUTES - cust.getEnterQueueTime());
    }
    System.out.println("\tLONG-RUN AVERAGE_TIME SPENT IN QUEUE PER CUSTOMER (wQ): " + TotalCustomersTimeInQueue/Customer.getEnteredStore().size() + " MINUTES");
  
    System.out.println("\tSERVER UTILIZATION (ρ): " + serverUtilization );
    System.out.println("\tMAXIMUM CHECKOUT LINE LENGTH: " + MaxQueueLength + " CUSTOMERS");
    System.out.println("\tAVERAGE RESPONSE TIME: " + AVGR + " MINUTES" );
    System.out.println("\tSIMULATION RUNLENGTH: " + Clock + " MINUTES");
    System.out.println("\tNUMBER OF CUSTOMERS ENTERED STORE: " + Customer.getCustomersEnteredStore());
    System.out.println("\tNUMBER OF CUSTOMERS SELECTED GOODS :" + Customer.getCustomersSelectedGoods());
    System.out.println("\tNUMBER OF CUSTOMERS ARRIVED AT CHECKOUT LINE: " + Customer.getCustomersEnteredCheckoutLine());
    System.out.println("\tNUMBER OF CUSTOMERS PAID BILL: " + Customer.getCustomersPaidBill());
    System.out.println("\tNUMBER OF CUSTOMERS DEPARTED STORE: " + Customer.getCustomersDepartedStore());
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
