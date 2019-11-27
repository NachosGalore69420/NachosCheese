package nachos.threads;

import nachos.machine.*;

import java.util.TreeSet;

import java.util.HashMap;
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
    private java.util.HashMap<LotteryThreadState,Integer> lotteryWaitQueue = new java.util.HashMap<LotteryThreadState, Integer>();	
    boolean transferPriority;
    private int sum = 0;
	boolean sumChange = false;
	public int sumTot = 0;//adds up the whole lottery
	private ThreadState acquire;
    	LotteryQueue(boolean transferPriority){
    		
    		/*waitQueue = new java.util.HashMap<ThreadState, Integer>();
			this.transferPriority = transferPriority;
			sum = 0;
			sumChange = true;*/
    		super(transferPriority);
    		this.transferPriority = transferPriority;
    		//System.out.println(" start ");
    	}

    	public KThread nextThread() {
    		//Lib.assertTrue(Machine.interrupt().disabled());
    		//System.out.println(" nextThread "+lotteryWaitQueue.toString());
    		if (lotteryWaitQueue.isEmpty() || pickNextThread() == null)//if empty
				return null;//exit
    		//pick
    		LotteryThreadState threadState = pickNextThread();

    		this.lotteryWaitQueue.remove(threadState);
    		
			//get
    		acquireThread(threadState.thread);
    		return threadState.thread;
    	}//nextThread
    	
		protected LotteryThreadState pickNextThread() {
			//System.out.println(" pickNextThread "+lotteryWaitQueue.toString());
			if (lotteryWaitQueue.isEmpty()) {
				return null;
			}
			
			LotteryThreadState threadStateResult = null;
			Random rand = new Random();

			int lottery = rand.nextInt(getTicketCurr()) + 1;
			Iterator<LotteryThreadState> Iter = lotteryWaitQueue.keySet().iterator();
			
			while (lottery > sumTot && Iter.hasNext()) {
				threadStateResult = Iter.next();
				sumTot += lotteryWaitQueue.get(threadStateResult).intValue();
			}
			return threadStateResult;
		}//pickNextThread
    	
		private int getTicketCurr(){
			if(sumChange == true)
				return sum;
			else
				return calcTickSum();
		}
		
		private int calcTickSum(){
			return 1;
		}
		
		/*private int threadTickets(LotteryThreadState ts) {
			if (transferPriority) 
				return ts.getEffectivePriority();//Lottry Thread
			else 
				return ts.getPriority();//From Priority
			}*/
		
		
		public void acquireThread(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			getThreadState(thread).acquire(this);
		}

    }
    /*---------------------Lottery ThreadState------------------*/ 
    protected class LotteryThreadState extends PriorityScheduler.ThreadState {
    	//Change objects above queue to match Constructer name
		public LotteryThreadState(KThread thread) {
			super(thread);
			//System.out.println(" start ");
		}
		@Override
		public void setPriority(int priority) {
			this.priority = priority;
		}
		
		@Override
		public int getEffectivePriority() {
		    // Count the Sum
			
		return priority;
		}//Get Effective
		
		//@Override
		public void waitForAccess(LotteryQueue LotteryThreadState) {
			 Lib.assertTrue(Machine.interrupt().disabled());
			 //  LotteryThreadState.put(thread ,Priority ,Gen a ticket); 
		    
		}
		
    }//LotteryThread End
	/** The thread with which this object is associated. */	   
	protected KThread thread;
	/** The priority of the associated thread. */
	protected int priority = priorityDefault;
	/**A linked list of PriorityQueues that donate resources to*/
	protected LinkedList<PriorityQueue> donateQueue = new LinkedList<PriorityQueue>();
   
}
