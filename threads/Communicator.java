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
    /**
     * Allocate a new communicator.
     */
	 
	private Lock mutex;				//Lock
	private Integer Buffer;			//Buffer
	private Condition2 currListener; 			//condition for listener
	private Condition2 currSpeaker;				//condition for speaker
	private Condition2 transfer;
	
    public Communicator() {
    	
    	this.mutex = new Lock();
    	this.Buffer = null;
    	this.currListener = new Condition2(this.mutex);
    	this.currSpeaker = new Condition2(this.mutex);
    	this.transfer = new Condition2(this.mutex);
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
    	
    	this.mutex.acquire();							//acquiring the lock
    	while(this.Buffer != null) {						//when the buffer is greater than 0 , put other speakers to sleep			
    		this.currSpeaker.sleep();
    	}
    	this.Buffer = word;								//the buffer equals the new value
    	this.currListener.wake();						// wake listener
    	this.transfer.sleep();
    	this.mutex.release();							// lock is released
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
    	
    	int messages; 				//message from the speaker function
    	this.mutex.acquire();		//acquiring the lock
    	
    	while (this.Buffer == null) { //when the buffer is empty, there is no message to listen to so listener sleeps
    		this.currListener.sleep();
    	}
    	
    	messages = this.Buffer.intValue();				//the message from speaker is now buffers value
    	this.Buffer = 0;								//reset the buffer to empty
    	this.currSpeaker.wake();						//the speaker wakes
    	this.transfer.wake();
    	this.mutex.release();							// lock is released
	
    	return messages;
    }
}
