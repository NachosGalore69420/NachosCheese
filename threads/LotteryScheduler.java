package nachos.threads;

import nachos.machine.*;

import java.util.TreeSet;
/*
import PriorityScheduler.PriorityQueue;
import PriorityScheduler.ThreadState;
import LotteryScheduler.ThreadState;*/
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
//scp -r LotteryScheduler.java f19-2l-g4@klwin00.ucmerced.edu:./nachos/threads
// ssh f19-2l-g4@klwin00.ucmerced.edu
/**
 * A scheduler that chooses threads using a lottery.
 *
 * <p>
 * A lottery scheduler associates a number of tickets with each thread. When a
 * thread needs to be dequeued, a random lottery is held, among all the tickets
 * of all the threads waiting to be dequeued. The thread that holds the winning
 * ticket is chosen.
 *
 * <p>
 * Note that a lottery scheduler must be able to handle a lot of tickets
 * (sometimes billions), so it is not acceptable to maintain state for every
 * ticket.
 *
 * <p>
 * A lottery scheduler must partially solve the priority inversion problem; in
 * particular, tickets must be transferred through locks, and through joins.
 * Unlike a priority scheduler, these tickets add (as opposed to just taking
 * the maximum).
 */
public class LotteryScheduler extends PriorityScheduler {
    /**
     * Allocate a new lottery scheduler.
     */
    public LotteryScheduler() {
    }
    
    /**
     * Allocate a new lottery thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer tickets from waiting threads
     *					to the owning thread.
     * @return	a new lottery thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
    //Modded to use LotteryQueue
	return new LotteryQueue(transferPriority);//null;//
    }

    //Increase Tickets -----------
    @Override
    public boolean increasePriority(){
    Lib.assertTrue(Machine.interrupt().disabled());
    KThread thread = KThread.currentThread();
    int priority = getPriority(thread);		
    	if(priority == priorityMaximum)
    		return false;
	setPriority(thread, priority + 1);
	return true;
    }
    @Override
    public boolean decreasePriority(){
    Lib.assertTrue(Machine.interrupt().disabled());
    KThread thread = KThread.currentThread();
    int priority = getPriority(thread);	
    	if(priority == priorityMinimum)
    		return false;
	setPriority(thread, priority - 1);
	return true;
    }
    
    @Override
    public void setPriority(KThread thread, int priority) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	Lib.assertTrue(priority >= priorityMinimum &&
		   priority <= priorityMaximum);
	
	getThreadState(thread).setPriority(priority);
    }
    /**
     * The default priority for a new thread. Do not change this value.
     */
    public static final int priorityDefault = 1;
    /**
     * The minimum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMinimum = 1;
    /**
     * The maximum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMaximum = Integer.MAX_VALUE;  
    //Get Thread State Bellow 
    @Override
    protected LotteryThreadState getThreadState(KThread thread) {
	if (thread.schedulingState == null)
	    thread.schedulingState = new LotteryThreadState(thread);

	return (LotteryThreadState) thread.schedulingState;
    }
    
   /*-------___--------------LotteryQueue------------------*/ 
    protected class LotteryQueue extends PriorityQueue{
    //Holds the Thread and its own Ticket
    private java.util.HashMap<LotteryThreadState,Integer> lotteryWaitQueue;	
    boolean transferPriority;
    private int sum;
	boolean sumChange;
    
    	LotteryQueue(boolean transferPriority){
			/*waitQueue = new java.util.HashMap<ThreadState, Integer>();
			this.transferPriority = transferPriority;
			sum = 0;
			sumChange = true;*/
    		super(transferPriority);
    	}
 
    }
    /*---------------------Lottery ThreadState------------------*/ 
    protected class LotteryThreadState extends PriorityScheduler.ThreadState {
    	//Change objects above queue to match Constructer name
		public LotteryThreadState(KThread thread) {
			super(thread);
		}

}
}
