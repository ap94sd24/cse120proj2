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
	     
	     //waitQueue = new LinkedList<ThreadKernel>(); 
    }
    

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
    	boolean intStatus = Machine.interrupt().disable();
    
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	
	//initialize wait value
    //waitQueue.add(wait);
	
	conditionLock.release();

	if (value == 0) {
	    waitQueue.waitForAccess(KThread.currentThread());
	    KThread.sleep();
	}
	else {
	    value--;
	}

	Machine.interrupt().restore(intStatus);	

	conditionLock.acquire();
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
    	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
    	
    	boolean intStatus = Machine.interrupt().disable();

    	KThread thread = waitQueue.nextThread();
    	if (thread != null) {
    	    thread.ready();
    	}
    	else {
    	    value++;
    	}
    	
    	Machine.interrupt().restore(intStatus);
	
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
    	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	 	wake(); 
    }
    
    private int value; 
    private Lock conditionLock;
    private ThreadQueue waitQueue =
    		ThreadedKernel.scheduler.newThreadQueue(false);
	public void call() {
		// TODO Auto-generated method stub
	}
}
