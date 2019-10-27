package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;
    // We must initialize more variables as static here:
    static Condition2 adultQueue;
    static Condition2 childQueue;
    static int aOahu, cOahu;
    static boolean location = false;
    
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
	// Must initialize a lock and conditional variables for the boat
	Lock lock = new Lock();
	adultQueue = new Condition2(lock);
	childQueue = new Condition2(lock);
	
	// Create threads here. See section 3.4 of the Nachos for Java
	// Walkthrough linked from the projects page.
	
	// Sample runnable and thread creation:
	Runnable r = new Runnable() {
	    public void run() {
                SampleItinerary();
        }
    };
    KThread t = new KThread(r);
    t.setName("Sample Boat Thread");
    t.fork();
    
    // Create two runnables to initialize location and run AdultItinerary/ChildItinerary
    Runnable rA = new Runnable() {
    	public void run() {
        	// If false->Oahu, if true->Molokai
        	location = false;
        	AdultItinerary();
    	}
    };
    Runnable rC = new Runnable() {
    	public void run() {
    		location = false;
    		ChildItinerary();
    	}
    };
    
    // Two for loops to initialize our adult and child threads, then to fork() them to run.
	for (int i = 0; i < adults; i++) {
		// Creates a new adult thread by passing our corresponding runnable.
		KThread adultT = new KThread(rA);
		// Names for threads are set for debugging purposes.
		adultT.setName("Adult thread " + (i+1));
		// Counter increments to remember how many adults are initially on Oahu.
		aOahu++;
		// Fork beings the runnable
		adultT.fork();
	}
	// Same for loop to create threads for each child now.
	for (int i = 0; i < children; i++) {
		KThread childT = new KThread(rC);
		childT.setName("Child thread " + (i+1));
		// Another counter to help the threads 'remember' the number of initial people.
		cOahu++;
		childT.fork();
	}
	// After all threads are created and running, wake only one of each to begin the simulation.
	// Note: try wakeAll() if code does not work
	adultQueue.wake();
	childQueue.wake();
    }

    /* The goal of AdultItinerary is to obtain the boat when possible. (>0 children at Molokai, for instance)
     *  If not possible to obtain the boat, then sleep using a conditional variable.
     *  The thread returns (nothing more to do) when it obtains the boat and travels to Molokai. (finished)
     *  Busy waiting shall be avoiding by sleeping and only being woken when the trip is possible.
     */
    static void AdultItinerary()
    {
	/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
	*/
    	/* Thread should first sleep until woken again by begin()
    	 * This is to ensure that all adult and children are initialized before the boat moves.
    	 */
    	adultQueue.sleep();
    	while (cOahu > 0) 
    		adultQueue.sleep();
    	
    }

    /* My strategy for the ChildItinerary function is to have many different cases of people remaining on Oahu.
     *  A child will return back to Oahu only if they remember if there are more children or adults left to transport.
     *  When a child transports another child to Molokai, they will be marked as complete, leaving the initial child to remain driving the boat.
     *  Much more complex than AdultItinerary due to more cases needing to be covered.
     */
    static void ChildItinerary()
    {
    	// Just like AdultItinerary, sleep until the entire system is set-up.
    	childQueue.sleep();
    	while(location == false) {
    		
    	}
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
