package nachos.threads;
import nachos.ag.BoatGrader;
import nachos.machine.Machine;

// Needed for assertions?
// import nachos.machine.*;

public class Boat
{
    static BoatGrader bg;
    // We must initialize more variables as static here:
    static Condition2 adultQueue;
    static Condition2 childQueue;
    static Condition2 childQueueM;
    static Condition2 toMolokai;
    static Condition2 waitUntilDone;
    static int aOahu, cOahu;
    static Lock lock;
    // Note: may need to initialize a bool for boat location and if there is a child on the boat
    static boolean bLocation;
    // A boolean to communicate if there is a child waiting for a passenger to ride with.
    static boolean cOnBoat;
    static boolean finished;
    
    public static void selfTest()
    {
	BoatGrader b = new BoatGrader();
	
	System.out.println("\n ***Testing Boats with only 2 children***");
	begin(0, 2, b);

//	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
//  	begin(1, 2, b);

//  	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
//  	begin(3, 3, b);
    }

    public static void begin( int adults, int children, BoatGrader b )
    {
	// Store the externally generated autograder in a class
	// variable to be accessible by children.
	bg = b;

	// Instantiate global variables here
	aOahu = 0;		// # of adults on Oahu
	cOahu = 0;		// # of children on Oahu
	bLocation = false;
	cOnBoat = false;
	// Must initialize a lock and conditional variables for the boat
	lock = new Lock();
	adultQueue = new Condition2(lock);
	childQueue = new Condition2(lock);
	// We will initialize another conditional variable for children on Molokai specifically. This is to avoid busy-waiting.
	childQueueM = new Condition2(lock);
	// Another conditional variable will be used when a child is waiting on another passenger before going to Molokai.
	toMolokai = new Condition2(lock);
	waitUntilDone = new Condition2(lock);
	finished = false;
	
	// Create threads here. See section 3.4 of the Nachos for Java
	// Walkthrough linked from the projects page.
	
	/* Sample runnable and thread creation:
	Runnable r = new Runnable() {
	    public void run() {
                SampleItinerary();
        }
    };
    KThread t = new KThread(r);
    t.setName("Sample Boat Thread");
    t.fork();
    */
    
    // Create two runnables to initialize location and run AdultItinerary/ChildItinerary
    Runnable rA = new Runnable() {
    	public void run() {
        	// If false->Oahu, if true->Molokai
        	boolean location = false;
        	// Functions were modified to take in argument "location" so that each thread can track where they are.
        	AdultItinerary(location);
    	}
    };
    Runnable rC = new Runnable() {
    	public void run() {
    		boolean location = false;
    		ChildItinerary(location);
    	}
    };
    
    aOahu = adults;
    cOahu = children;
    
    //Potentially have to disable interrupts while system is setup?
    Machine.interrupt().disable();
    
    // Two for loops to initialize our adult and child threads, then to fork() them to run.
	for (int i = 0; i < children; i++) {
		KThread childT = new KThread(rC);
		childT.setName("Child thread " + (i+1));
		// Another counter to help the threads 'remember' the number of initial people.
		// cOahu++;
		childT.fork();
	}
    for (int i = 0; i < adults; i++) {
		// Creates a new adult thread by passing our corresponding runnable.
		KThread adultT = new KThread(rA);
		// Names for threads are set for debugging purposes.
		adultT.setName("Adult thread " + (i+1));
		// Counter increments to remember how many adults are initially on Oahu.
		// aOahu++;
		// Fork beings the runnable
		adultT.fork();
	}
    Machine.interrupt().enable();
	// Perhaps use a while loop that waits until simulation is done?
    lock.acquire();
    while(!finished) {
    	childQueue.wakeAll();
    	waitUntilDone.sleep();
    }
    lock.release();
    System.out.println("DEBUG: Finishing begin()");
    return;
    }

    /* The goal of AdultItinerary is to obtain the boat when possible. (>0 children at Molokai, for instance)
     *  If not possible to obtain the boat, then sleep using a conditional variable.
     *  The thread returns (nothing more to do) when it obtains the boat and travels to Molokai. (finished)
     *  Busy waiting shall be avoiding by sleeping and only being woken when the trip is possible.
     *  Also, as mentioned before, this was changed to take in location as a boolean.
     */
    static void AdultItinerary(boolean location)
    {
	/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
	*/
    	// adultQueue.sleep();
    	// Goal: Sleep until there are less than 2 children left on Oahu, which is the only case that an adult should leave for Molokai
    	lock.acquire();
    	// System.out.println("DEBUG: Adult thread start.");
    	while (cOahu > 1 || bLocation == true) {
    		childQueue.wakeAll();
    		adultQueue.sleep();
    	}
    	if (bLocation == false && cOahu == 1) {
    		// When woken and able to row, first acquire the lock before rowing.
    		// lock.acquire();
    		// Send message to boat grader, reduce number of adults on Oahu, and set boat's location to Molokai (T)
    		bg.AdultRowToMolokai();
    		aOahu--;
    		bLocation = true;
    		// Release lock, wake child on Molokai, and do nothing more (finished)
    		// lock.release();
    		childQueueM.wakeAll();
    	}
    	
    	// System.out.println("DEBUG: Adult thread end.");
    	adultQueue.sleep();
    	lock.release();
    }

    /* My strategy for the ChildItinerary function is to have many different cases of people remaining on Oahu.
     *  A child will return back to Oahu only if they remember if there are more children or adults left to transport.
     *  When a child transports another child to Molokai, they will be marked as complete, leaving the initial child to remain driving the boat.
     *  Much more complex than AdultItinerary due to more cases needing to be covered.
     */
    static void ChildItinerary(boolean location)
    {
    	
    	lock.acquire();
    	// System.out.println("DEBUG: Child thread start.");
    	// childQueue.sleep();
    	// while(location == false) {
    	// while(true) should be used so that the same child thread operates the boat when needed.
    	while (true) {
    		if (location == false && bLocation == false) {
    			// If there is a child piloting the boat (at Oahu) that woke up this thread, ride to Molokai.
    			if (cOnBoat == true) {
    				bg.ChildRideToMolokai();
    				cOahu--;
    				location = true;
    				bLocation = true;
    				toMolokai.wakeAll();
    				// Sleep at Molokai until woken up later by an adult later.
    				childQueueM.sleep();
    			}
    			// If 2 children at Oahu, take both to Molokai and set both locations to true (Molokai)
    			else if (cOahu > 1 && cOnBoat == false) {
    				// lock.acquire();
    				cOnBoat = true;
    				bg.ChildRowToMolokai();
    				cOahu--;
    				childQueue.wakeAll();
    				toMolokai.sleep();
    				cOnBoat = false;
    				location = true;
    				bLocation = true;
    				// lock.release();
    			}
    			else if (cOahu < 2 && aOahu > 0) {
    				// lock.acquire();
    				adultQueue.wakeAll();
    				childQueue.sleep();
    				// lock.release();
    			}
    			else if (cOahu == 1) {
    				bg.ChildRowToMolokai();
    				cOahu--;
    				location = true;
    				bLocation = true;
    			}
    		}
    		if (location == true && bLocation == true) {
    			if (cOahu == 0 && aOahu == 0) {
    				finished = true;
    				waitUntilDone.wakeAll();
    				break;
    			}
    			else {
    				// lock.acquire();
    				bg.ChildRowToOahu();
    				cOahu++;
    				location = false;
    				bLocation = false;
    				// lock.release();
    			}
    		}
    		/*
    		else {
    			childQueueM.wakeAll();
    			childQueue.sleep();
    		}
    		*/
    	}
    	// System.out.println("DEBUG: System finished");
    	lock.release();
    	
    }

    static void SampleItinerary()
    {
	// Please note that this isn't a valid solution (you can't fit
	// all of them on the boat). Please also note that you may not
	// have a single thread calculate a solution and then just play
	// it back at the autograder -- you will be caught.
	System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
	bg.AdultRowToMolokai();
	bg.ChildRideToMolokai();
	bg.AdultRideToMolokai();
	bg.ChildRideToMolokai();
    }
    
}
