package nachos.threads;

import java.util.LinkedList;

import nachos.machine.*;
 

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() { 
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
	       
	    //loop through  linkedList
	    for (int i = 0; i < tempqueue.size(); i++) {
	    	Threadlist val = tempqueue.get(i);
	    	 //If waitTime <= currentTime
	    	if (val.time <= Machine.timer().getTime()) {
	       
	    	//Signal thread to wake
	        val.s.V();
	        
	        // remove elements from the list
	        tempqueue.remove(i--); 
	    	}
	    }
	    
	   //Yield thread to other processes
    	KThread.currentThread().yield();
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
	//Set wake time of threads
	long wakeTime = Machine.timer().getTime() + x;
	 
	//Instantiate semaphore for thread
    Semaphore hold = new Semaphore(0);
    //Store thread that take in semaphore and time
	Threadlist var = new Threadlist(hold, wakeTime);
   
	//Add to Linkedlist
    tempqueue.add(var);
     
     //Put thread to sleep when waketime is not reached
	 if (wakeTime > Machine.timer().getTime()) {
		 //Thread sleep
		hold.P();	 		 
	 }  	 
    }
  /**
   * Threadlist: A class that pass in semaphore and time as tuple to  the 
   * linked list 
   */
  public static class Threadlist {
	   Semaphore s;
	   long time;
	  
	//Constructor taking in Semaphore and time
	public Threadlist(Semaphore p, long time) {		   
           this.s = p; 
		   this.time = time; 
		    
	   } 
   }
      
    public static void selfTest() {
        KThread t1 = new KThread(new Runnable() {
            public void run() {
                long time1 = Machine.timer().getTime();
                int waitTime = 10000;
                System.out.println("Thread calling wait at time:" + time1);
                System.out.println("time1 is ......" + time1);
                ThreadedKernel.alarm.waitUntil(waitTime);   
                System.out.println("The wait time is ......" + waitTime);
                System.out.println("Currenttime ......" + Machine.timer().getTime());
                System.out.println("time1NEW is  ......" + time1);
                System.out.println("Thread woken up after:" + (Machine.timer().getTime() - time1));
                Lib.assertTrue((Machine.timer().getTime() - time1) > waitTime, " thread woke up too early.");
                
            }
        });
        t1.setName("T1");
        t1.fork();
        t1.join();
    }
    
   //Variable for time to wait
   private static long x;
   // Create new Threadlist linkedlist
   private LinkedList<Threadlist> tempqueue = new LinkedList<Threadlist>();  
}

 
