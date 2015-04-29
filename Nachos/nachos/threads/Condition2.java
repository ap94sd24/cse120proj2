package nachos.threads;

import nachos.machine.*;

import java.util.LinkedList;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
	     this.conditionLock = conditionLock; 
	    //Use to store threads for sleep/wake methods
	     waitQ = new LinkedList<KThread>(); 
    }
    

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
    	//Thread status
    	boolean intStatus = Machine.interrupt().disable();
    //Debug for current thread lock
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	
	//Release associated lock
	conditionLock.release();
	// Add current thread to the linkedList
	waitQ.add(KThread.currentThread());
	//Put thread to sleep
	KThread.sleep();
	
	// Restore the status
	Machine.interrupt().restore(intStatus);	
	
	//Reacquire the lock
	conditionLock.acquire();
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
    	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
    	//Thread status
    	boolean intStatus = Machine.interrupt().disable();
    	      	
    	//Put thread to ready state by removing from the linkedList
    	if (!waitQ.isEmpty()) {
    		waitQ.remove().ready(); 	    
    	}
    	 
    	//Restore thread status
    	Machine.interrupt().restore(intStatus);
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
    	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
    	//Call wake method to wake all threads
        while (!waitQ.isEmpty()) {
	 	    wake(); 
      }
    }
    
    public static void selfTest(){
    	int[] item = new int[1];
    	item[0] = 0;
    	Lock lock = new Lock();
    	Condition2 con = new Condition2(lock);
    	KThread producer = new KThread(new Producer(item, lock, con));
    	KThread producer1 = new KThread(new Producer(item, lock, con));
    	KThread consumer1 = new KThread(new Consumer(item, lock, con));
    	KThread consumer2 = new KThread(new Consumer(item, lock, con));
    	System.out.println("\n----------------------------------------------\nTEST CONDITION2\n");
    	 
    	producer1.fork(); 
    	consumer1.fork();
    	consumer2.fork();
    	producer.fork();
    	
    	producer.join();
    	producer1.join();
    	consumer1.join();
    	consumer2.join();
    	 
    }
    
    private static class Consumer implements Runnable{
    	private int[] item;
    	private Lock lock;
    	private Condition2 sleepingConsumers;
    	public Consumer(int[] iteM, Lock lock, Condition2 con_var){
    		item = iteM;
    		this.lock = lock;
    		sleepingConsumers = con_var;
    	}
    	public void run(){
    		lock.acquire();
    		while(item[0] < 1){
    			System.out.println("Consumer: no item, so I sleep.");
    			sleepingConsumers.sleep();
    		}
    		item[0] -= 1;
    		System.out.println("Consumer: I use 1 item.");
    		lock.release();
    	}
    }
    
    private static class Producer implements Runnable{
    	private int[] item;
    	private Lock lock;
    	private Condition2 sleepingConsumers;
    	public Producer(int[] iteM, Lock lock, Condition2 con_var){
    		item = iteM;
    		this.lock = lock;
    		sleepingConsumers = con_var;
    	}
    	public void run(){
    		lock.acquire();
    		item[0] += 1;
    		System.out.println("Producer: I make one item.");
    		sleepingConsumers.wakeAll();
    		lock.release();
    	}
    }
    
    
    private Lock conditionLock;
    //Create LinkedList to store threads
    private LinkedList<KThread> waitQ;  
}
