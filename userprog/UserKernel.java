package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;

import java.util.*;
/**
 * A kernel that can support multiple user processes.
 */
public class UserKernel extends ThreadedKernel {
    /**
     * Allocate a new user kernel.
     */
	
	
	//******* added code
	public static ArrayList<Boolean>  pageStatus; //new Array List 
	public static LinkedList<Integer> freePage;   //new linked list 
	
	
	
	public static void initializePages() { 		//new method intializePages
		
		pageStatus = new ArrayList<Boolean>(); //initialize the ArrayList
		freePage = new LinkedList<Integer>();  //initialize the linkedList
		
		//for loop for pages
		//adds the free page and then changes the status 
		for(int x = 0; x < Machine.processor().getNumPhysPages(); x++) { 
			freePage.add(x);						
			pageStatus.add(false);					
		}
	}
	
	
	 public static int allocatePage() {
		 Machine.interrupt().disable();				//disable interrupt 
		 
		// if statement that checks the size of the page is less than one 
		// enable interrupt and returns -1 
		 if(freePage.size() < 1) {					
			Machine.interrupt().enable(); 			 
			 return -1;								// return statement 
		 }
		 //else statement 
		 else {
			 int pg = freePage.pop();			//new integer variable pg 
			 Lib.assertTrue(pageStatus.get(pg) == false); //equals false
			 pageStatus.set(pg, true);			//set the status of the page 
			 Machine.interrupt().enable();		//enable 
			 return pg;							//return pg 
		 }
		
	 }
	 
	 
	 public static void deallocatePage(int pg) {
			Machine.interrupt().disable();				//disable interrupt 
			Lib.assertTrue(pageStatus.get(pg) == true);
			pageStatus.set(pg, false);    				// set the page status 
			freePage.push(pg);							// push the page ("pg") to free page
			Machine.interrupt().enable();				//enable
		}
		
	//*********
	 
	 
    public UserKernel() {
	super();
    }

    /**
     * Initialize this kernel. Creates a synchronized console and sets the
     * processor's exception handler.
     */
    public void initialize(String[] args) {
	super.initialize(args);

	console = new SynchConsole(Machine.console());
	
	Machine.processor().setExceptionHandler(new Runnable() {
		public void run() { exceptionHandler(); }
	    });
    }

    /**
     * Test the console device.
     */	
    public void selfTest() {
	super.selfTest();

	System.out.println("Testing the console device. Typed characters");
	System.out.println("will be echoed until q is typed.");

	char c;

	do {
	    c = (char) console.readByte(true);
	    console.writeByte(c);
	}
	while (c != 'q');

	System.out.println("");
    }

    /**
     * Returns the current process.
     *
     * @return	the current process, or <tt>null</tt> if no process is current.
     */
    public static UserProcess currentProcess() {
	if (!(KThread.currentThread() instanceof UThread))
	    return null;
	
	return ((UThread) KThread.currentThread()).process;
    }

    /**
     * The exception handler. This handler is called by the processor whenever
     * a user instruction causes a processor exception.
     *
     * <p>
     * When the exception handler is invoked, interrupts are enabled, and the
     * processor's cause register contains an integer identifying the cause of
     * the exception (see the <tt>exceptionZZZ</tt> constants in the
     * <tt>Processor</tt> class). If the exception involves a bad virtual
     * address (e.g. page fault, TLB miss, read-only, bus error, or address
     * error), the processor's BadVAddr register identifies the virtual address
     * that caused the exception.
     */
    public void exceptionHandler() {
	Lib.assertTrue(KThread.currentThread() instanceof UThread);

	UserProcess process = ((UThread) KThread.currentThread()).process;
	int cause = Machine.processor().readRegister(Processor.regCause);
	process.handleException(cause);
    }

    /**
     * Start running user programs, by creating a process and running a shell
     * program in it. The name of the shell program it must run is returned by
     * <tt>Machine.getShellProgramName()</tt>.
     *
     * @see	nachos.machine.Machine#getShellProgramName
     */
    public void run() {
	super.run();

	UserProcess process = UserProcess.newUserProcess();
	
	String shellProgram = Machine.getShellProgramName();	
	Lib.assertTrue(process.execute(shellProgram, new String[] { }));

	KThread.currentThread().finish();
    }

    /**
     * Terminate this kernel. Never returns.
     */
    public void terminate() {
	super.terminate();
    }

    /** Globally accessible reference to the synchronized console. */
    public static SynchConsole console;

    // dummy variables to make javac smarter
    private static Coff dummy1 = null;
}