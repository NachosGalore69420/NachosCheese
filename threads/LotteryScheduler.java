package nachos.threads;

import nachos.machine.*;

import java.util.TreeSet;

import PriorityScheduler.PriorityQueue;
import PriorityScheduler.ThreadState;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

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
  
    public int getPriority(KThread thread) {
	Lib.assertTrue(Machine.interrupt().disabled());     
	return getThreadState(thread).getPriority();
    }

    public int getEffectivePriority(KThread thread) {
	return getThreadState(thread).getEffectivePriority();
    }

    public void setPriority(KThread thread, int priority) {
	Lib.assertTrue(Machine.interrupt().disabled());
		       
	Lib.assertTrue(priority >= priorityMinimum &&
		   priority <= priorityMaximum);
	
	getThreadState(thread).setPriority(priority);
    }
    
    //Increase Tickets ---------------------------------
    public boolean increasePriority(){
    	
    	
    }
    
    public boolean decreasePriority(){
    	
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
    protected ThreadState getThreadState(KThread thread) {
	if (thread.schedulingState == null)
	    thread.schedulingState = new ThreadState(thread);

	return (ThreadState) thread.schedulingState;
    }
   /*-------___--------------LotteryQueue------------------*/ 
    protected class LotteryQueue extends PriorityQueue{
    	LotteryQueue(boolean transferPriority){
    		super(transferPriority);
    	}
    
	public void waitForAccess(KThread thread) {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    getThreadState(thread).waitForAccess(this);
	}
	
	public void acquire(KThread thread) {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    getThreadState(thread).acquire(this);
	}
	
	public KThread nextThread() {
		
	}
	
	protected ThreadState pickNextThread() {
		
	}
	
	public void changedPriority(){
		if(transferPriority == false)
			return;
		priorityChange = true;
	}
	
	public int getPriority(){
		if(transferPriority == false)
			return priorityMinimum;
		
		int tempPriority, effectivePriority = priorityMinimum;
		
		for(int i = 0; i < priorityWaitQueue.size(); i++){
			tempPriority = getThreadState(priorityWaitQueue.get(i)).getEffectivePriority();
			if(tempPriority > effectivePriority) {
				effectivePriority = tempPriority;
			}
		}
	return effectivePriority;
	}
	
	/*public void print() {
	    Lib.assertTrue(Machine.interrupt().disabled());
	    // implement me (if you want)
	}*/
    	
	public boolean transferPriority;
	/** Holds value */
	private KThread tempHolder = null;
	/**Queue of waiting Threads*/
	protected LinkedList<KThread> priorityWaitQueue = new LinkedList<KThread>();
    /**Flag to check if Prioirty of thread is updated*/
	private boolean priorityChange = false;
    }
	
    }
    
  
    /*---------------------Lottery ThreadState------------------*/ 
    protected class LotteryThreadState extends PriorityScheduler.ThreadState {
    	//Change objects above queue to match Constructer name
		public LotteryThreadState(KThread thread) {
			super(thread);
		}
		
	//mod to get this ticket	
	public int getPriority() {
	    return priority;
	}

    //mod to get all ticket	
	public int getEffectivePriority(){
		Int sum = priority;
		Int sumOther = 0;
		from(int i = 0 to donateQueue.size())
			LotteryQueue pq = donateQueue.
			sumOther += *
	}
	
	public void setPriority(int priority) {
		//sets priority
	    this.priority = priority;
	    
	    // implement me
	}
	//mod for lottery queue
	public void waitForAccess(PriorityQueue waitQueue) {
		 Lib.assertTrue(Machine.interrupt().disabled());

		 waitQueue.changedPriority();
		 
			
		 waitQueue.priorityWaitQueue.add(thread);
	    // implement me
	}

	public void acquire(LotteryQueue waitQueue) {
	this.donateQueue.add(waitQueue);
	}	
	
	protected KThread thread;
	/** The priority of the associated thread. */
	protected int priority;
	/**A linked list of PriorityQueues that donate resources to*/
	protected LinkedList<PriorityQueue> donateQueue = new LinkedList<PriorityQueue>();
  
    
}
