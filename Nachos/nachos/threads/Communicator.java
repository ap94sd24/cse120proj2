package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
	
	Lock lock;
	Condition speakerCon; //speaker condition
	Condition listenerCon; //listener condition
	
	//variable to keep track on listen and spoken
	//0 = do nothing
	//1 = listen
	//2 = speak
	private int toDo; 
	//stored communicator value
    private int comWord;
	/**
     * Allocate a new communicator.
     */
    public Communicator() {
    	lock = new Lock();
    	speakerCon = new Condition(lock);
    	listenerCon = new Condition(lock);
    	toDo = 0;
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
    	//System.out.println("SPEAK");
    	lock.acquire(); //only speak runs

    	//System.out.println("isFull value: " + isFull);
    	//wait until someone has listened before speaking
    	while (toDo == 2){
    	    listenerCon.wake();		
    	    speakerCon.sleep();
    	    //System.out.println("can't speak yet");
    	}
    	this.comWord = word;
    	toDo = 2; //speaker has been done, now listen can run
    	listenerCon.wakeAll();
    	
    	lock.release(); //listen can now run
    	//System.out.println("AFTERMATH: " + this.comWord);
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
    	//System.out.println("LISTEN");
    	lock.acquire();  //only listen runs
    	
    	//haven't found a speaker who already spoke
    	while(toDo == 1){
    		//System.out.println("waiting for speaker to speak");
    	    listenerCon.sleep();
    	} 
    	toDo = 1;
    	speakerCon.wakeAll();
    	lock.release();  //speaker can now run
    	//System.out.println("AFTERMATH LISTEN: " + this.comWord);
    	return this.comWord;
    }
    
    public static void selfTest(){
       final Communicator com = new Communicator();
       final long times[] = new long[4];
       final int words[] = new int[2];
        KThread speaker1 = new KThread( new Runnable () {
            public void run() {
                com.speak(4);
                times[0] = Machine.timer().getTime();
            }
        });
        speaker1.setName("S1");
        KThread speaker2 = new KThread( new Runnable () {
            public void run() {
                com.speak(7);
                times[1] = Machine.timer().getTime();
            }
        });
        speaker1.setName("S2");
        KThread listener1 = new KThread( new Runnable () {
            public void run() {
                words[0] = com.listen();
                times[2] = Machine.timer().getTime();
            }
        });
        listener1.setName("L1");
        KThread listener2 = new KThread( new Runnable () {
            public void run() {
                words[1] = com.listen();
                times[3] = Machine.timer().getTime();
            }
        });
        listener2.setName("L2");
        
        speaker1.fork(); speaker2.fork(); listener1.fork(); listener2.fork();
        speaker1.join(); speaker2.join(); listener1.join(); listener2.join();
       
        System.out.println("words  0: " + words[0]);
        System.out.println("words 1: " + words[1]);
        System.out.println("times : "  + times[0]);
        System.out.println("times : "  + times[1]);
        System.out.println("times : "  + times[2]);
        System.out.println("times : "  + times[3]);
        
        
        Lib.assertTrue(words[0] == 4, "Didn't listen back spoken word."); 
        Lib.assertTrue(words[1] == 7, "Didn't listen back spoken word.");
        Lib.assertTrue(times[0] < times[2], "speak returned before listen.");
        Lib.assertTrue(times[1] < times[3], "speak returned before listen.");
        
        //System.out.println("works");
    }
}
