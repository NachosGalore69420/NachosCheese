package nachos.threads;

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
	private TreeMap<Long, KThread> nextThread;
	
    public Alarm() {
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
	nextThread = new TreeMap<Long, KThread>();
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
	//KThread.currentThread().yield();
	boolean threadStatus = Machine.interrupt().setStatus(false);
    	long currentTime = Machine.timer().getTime();
    	
    	while(!nextThread.isEmpty() && nextThread.firstKey() <= currentTime){
    		nextThread.pollFirstEntry().getValue().ready();
    	}
    	Machine.interrupt().setStatus(true);
    	KThread.yield();
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
	// for now, cheat just to get something working (busy waiting is bad)
	long wakeTime = Machine.timer().getTime() + x;
	boolean threadStatus = Machine.interrupt().setStatus(false);
	
	nextThread.put(wakeTime, KThread.currentThread());
	
	//while (wakeTime > Machine.timer().getTime())
	KThread.yield();
    
	Machine.interrupt().setStatus(true);
	/*while (wakeTime > Machine.timer().getTime())
	    KThread.yield();*/
    }
}
