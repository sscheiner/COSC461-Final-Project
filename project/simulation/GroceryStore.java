
package grocerystorecheckout;
import java.util.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.imageio.*;

/**
 *
 * @author dcarro8
 * @author Ryan Oechsler
 */

public class GroceryStore extends JPanel implements ActionListener {

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
public static Queue CheckoutLine;
public static Random stream;
public final static long DURATION_IN_MINUTES = 60;
public static double An; // nth customer inter arrival time (n = 5)
public static double Sn; // nth customer service time (n = 5) 
public static Map<Double,Integer> customersAtTime = new HashMap();
public static Map<Double,Integer> queueAtTime = new HashMap();
public static Double TotalQueueTime = 0.0;
public static Double endSimulationTime = 0.0;
//Graphics-related objects
static boolean paintArrivingCustomer;
static boolean paintDepartingCustomer;
static BufferedImage StoreLayout = null;
static BufferedImage GreenHuman  = null;
static BufferedImage BlueHuman   = null;
static BufferedImage RedHuman    = null;
public static  int  custArrivals = 0;
public static int custDepartures = 0;
public static JPanel groceryStorePanel;
public static JFrame groceryStoreFrame;

    // Create a timer controlling how long each event persists on screen (in milliseconds e.g. 1000 = 1 second)
    Timer SimTimer = new Timer(150,this);

    //Constructor
    GroceryStore() { 
      // start the timer that controls the graphics update rate
      SimTimer.start();
    }

    public static void main(String argv[]) {
    
	  MeanStoreArrivalTime = 1.0; // arrival rate per customer mean
	  MeanSelectGoodsTime = 7.5;    
	  MeanCheckoutWaitTime = 3.2;
	  MeanCheckoutArrivalTime = 1.0;
	  MeanPayBillTime = 2.0;
	  MeanServiceTime = .75;
	  SIGMA = 0.6;
	  TotalCustomersServiced = 0;
	  long seed = 10000
			  ; 
	  stream = new Random(seed); // initialize rng stream
	  FutureEventList = new EventList();
	  CheckoutLine = new Queue(); // holds customers in checkout line
	  Initialization();
      InitGraphics();
    }
           
    // Override of the JPanel's paint event
    @Override
    public void paint(Graphics g) { 
    	
	  int i, numHumans;
	  
      // draw the grocery store floor plan
      Graphics2D g2d = (Graphics2D) g;
      g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            
      // draw the grocery store floor plan
      g2d.drawImage(StoreLayout, 0, 0, null);
    
      
      if (paintArrivingCustomer) {
    	  g2d.drawImage(BlueHuman, 175, 420, null);
    	  paintArrivingCustomer = false;
    	  custArrivals++;
      }
      
      if (paintDepartingCustomer) {
    	  g2d.drawImage(BlueHuman, 720, 420, null);
    	  paintDepartingCustomer = false;
    	  custDepartures++;
      }
      // draw the customers who are waiting in line to check out in red
      numHumans = CheckoutLine.getCustomerCount();
      for (i = 0; i < numHumans; i++)
        g2d.drawImage(RedHuman, 720 - 45*i, 160, null);

      // draw the customers who are shopping in green 
      numHumans = custArrivals - custDepartures - i; 
      for (i = 0; i < numHumans; i+=5)  { 
  	    g2d.drawImage(GreenHuman, 80, 150 + 15*i, null);
        if (i+1 < numHumans) g2d.drawImage(GreenHuman, 120, 150 + 15*i, null);   
        if (i+2 < numHumans) g2d.drawImage(GreenHuman, 160, 150 + 15*i, null);   
        if (i+3 < numHumans) g2d.drawImage(GreenHuman, 200, 150 + 15*i, null);
        if (i+4 < numHumans) g2d.drawImage(GreenHuman, 240, 150 + 15*i, null);   
      }     
      g2d.drawString("Time = " + Double.toString(Clock), 300, 50);
      g2d.drawString("Number of Arrivals = " + custArrivals, 325, 440);
      g2d.drawString("Number of Departures = " + custDepartures, 325, 465);
    }
   
    // each time the simulation timer times out we execute one simulation event and update graphics
    @Override
    public void actionPerformed(ActionEvent arg0) {
	   
      Event evt = (Event)FutureEventList.getMin(); // get imminent event
      Clock = evt.getTime();     // advance simulation time
      //System.out.println("Entered timer. clock is: " + Clock);
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
            }
      	      
      //Clock++;    // increment clock one minute 
      repaint();  // update the graphics
      
      //System.out.println("Updating graphics at time" + Clock);
      if (Clock >= DURATION_IN_MINUTES) {
    	endSimulationTime = evt.getTime(); // track end time of simulation
        ReportGeneration();
    	SimTimer.stop();
      }
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
      //Event evt = new Event(enterStore, exponential(stream,MeanStoreArrivalTime), new Customer());
      
      //Seed future event list with customer arrival events
      double arrivalTime = 0.0;
      while(arrivalTime < DURATION_IN_MINUTES)
      {
    	  Event evt = new Event(enterStore, arrivalTime, new Customer());
    	  FutureEventList.enqueue(evt);
    	  arrivalTime = arrivalTime + exponential(stream,MeanStoreArrivalTime);
      } 
      //System.out.println(FutureEventList.size() + " customers have entered the store");
    }

    private static void InitGraphics() {

      // Import the store floor plan and customer images from files
      try {
  	    StoreLayout = ImageIO.read(new File("C:\\temp\\groceryStore.jpg"));
        GreenHuman  = ImageIO.read(new File("C:\\temp\\GreenHuman.jpg"));    
        BlueHuman   = ImageIO.read(new File("C:\\temp\\BlueHuman.jpg"));
        RedHuman    = ImageIO.read(new File("C:\\temp\\RedHuman.jpg"));
      } 
      catch (IOException e) {
        e.printStackTrace();
      }

      // Create and configure the container to hold the grocery store graphics
      JFrame groceryStoreFrame = new JFrame("Grocery Store Simulator"); 
      JPanel groceryStorePanel = new GroceryStore();
      groceryStoreFrame.setContentPane(groceryStorePanel);      
      groceryStoreFrame.setSize(StoreLayout.getWidth(),StoreLayout.getHeight()); 
      groceryStoreFrame.setVisible(true);
      groceryStorePanel.setVisible(true);

     }
   
   // Process a new customer arrival and transition them to select goods state
   // Also schedule next customer arrival
   public static void ProcessEnterStore(Event evt) {
     evt.getCustomer().enterStore(evt);
     //System.out.println("A new customer has entered the store at time" + Clock);
     Event customerSelectGoods = new Event(selectGoods,Clock + exponential(stream, MeanSelectGoodsTime), evt.getCustomer());
     FutureEventList.enqueue(customerSelectGoods);
     paintArrivingCustomer = true;
    }
   
    // Process customer select goods and transition them to stand in line state
    public static void ProcessSelectGoods(Event evt) {
     evt.getCustomer().selectGoods();
     //System.out.println("A customer has selected goods at time" + Clock);
     
     Event customerStandInLine = new Event(checkoutArrival,Clock + exponential(stream,MeanCheckoutArrivalTime), evt.getCustomer());
     FutureEventList.enqueue(customerStandInLine);
     }  
    
    // Process customer wait in line and transition them to pay bill
    public static void ProcessCheckoutArrival(Event evt) {
      evt.getCustomer().standInLine(evt);
      //System.out.println("A customer has arrived at checkout line at time" + Clock);
            
      CheckoutLine.enqueue(evt);
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
     /*double simulatedInterArrivalTime = exponential(stream, MeanStoreArrivalTime);
     evt = new Event(enterStore, simulatedInterArrivalTime, new Customer());
     if (evt.getCustomer().getId() == 5) // record 5th customer inter arrival time
          An = simulatedInterArrivalTime;
          
     FutureEventList.enqueue(evt);*/
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
      Event finished =(Event)CheckoutLine.dequeue();
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
    //System.out.println("A customer has departed store at time" + Clock);
    paintDepartingCustomer = true;
    }
       

    public static void ReportGeneration() {
    double serverUtilization = TotalBusy/Clock;
    double AVGR = SumResponseTime/Customer.getCustomersPaidBill();
    System.out.println("SINGLE SERVER QUEUE SIMULATION - GROCERY STORE CHECKOUT COUNTER ");
    System.out.println("\tMEAN STORE ARRIVAL RATE (lambda): " + MeanStoreArrivalTime  + " CUSTOMER PER MINUTE");
    System.out.println("\tNth(N=5) SIMULATED CUSTOMER STORE INTERARRIVAL TIME (An): " + An + " MINUTES");
    //System.out.println("\tMEAN CHECKOUT LINE WAIT TIME: " + MeanCheckoutWaitTime + " MINUTES");
    System.out.println("\tAVG INTER ARRIVAL TIME TO CHECKOUT LINE: " + MeanCheckoutArrivalTime + " MINUTES");
    System.out.println("\tSTANDARD DEVIATION OF SERVICE TIMES: " + SIGMA );
    System.out.println("\tNth(N=5) CUSTOMER SERVICE TIME (Sn): " + Sn + " MINUTES");
    System.out.println("\tMEAN CASHIER (SERVER) RATE (mu): " + MeanServiceTime + " MINUTES PER CUSTOMER");
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
  
    System.out.println("\tSERVER UTILIZATION (phi): " + serverUtilization );
    System.out.println("\tMAXIMUM CHECKOUT LINE LENGTH: " + MaxQueueLength + " CUSTOMERS");
    System.out.println("\tAVERAGE RESPONSE TIME: " + AVGR + " MINUTES" );
    System.out.println("\tSIMULATION RUNLENGTH: " + Clock + " MINUTES");
    System.out.println("\tNUMBER OF CUSTOMERS ENTERED STORE: " + Customer.getCustomersEnteredStore());
    System.out.println("\tNUMBER OF CUSTOMERS SELECTED GOODS :" + Customer.getCustomersSelectedGoods());
    System.out.println("\tNUMBER OF CUSTOMERS ARRIVED AT CHECKOUT LINE: " + Customer.getCustomersEnteredCheckoutLine());
    System.out.println("\tNUMBER OF CUSTOMERS PAID BILL: " + Customer.getCustomersPaidBill());
    System.out.println("\tNUMBER OF CUSTOMERS DEPARTED STORE: " + Customer.getCustomersDepartedStore());
    System.out.println("\tNUMBER OF CUSTOMERS IN STORE: " + Customer.customersInStore.size());
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
    



