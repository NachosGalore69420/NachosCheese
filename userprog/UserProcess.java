package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import java.util.HashMap;
import java.util.List;
import java.io.EOFException;
import java.util.Random;

/**
 * Encapsulates the state of a user process that is not contained in its
 * user thread (or threads). This includes its address translation state, a
 * file table, and information about the program being executed.
 *
 * <p>
 * This class is extended by other classes to support additional functionality
 * (such as additional syscalls).
 *
 * @see	nachos.vm.VMProcess
 * @see	nachos.network.NetProcess
 */

class child {
	public
		UserProcess child;
		int cStat;
		boolean isNew;
		child() {
			this.cStat = -Integer.MAX_VALUE;
			isNew = true;
		}
		void cStat(int s) {
			cStat = s;
			isNew = false;
		}
}


public class UserProcess {
    /**
     * Allocate a new process.
     */
	private static final int root = 1;
	private static int MathRand = root;
	private child Child;
	//keep track of chilren
	private HashMap<Integer, child> hm;
	private UThread ut;
    public UserProcess() {
    //every process ID must be unique
    pID = ++pIDs;
    //record open/used files
    OFile = new OpenFile[16];
    OFile[0] = UserKernel.console.openForReading();
    OFile[1] = UserKernel.console.openForWriting();
	int numPhysPages = Machine.processor().getNumPhysPages();
	pageTable = new TranslationEntry[numPhysPages];
	for (int i=0; i<numPhysPages; i++)
	    pageTable[i] = new TranslationEntry(i,i, true,false,false,false);
    }
    
    boolean isParent() {
    	if (Child != null)
    		return true;
    	return false;
    }
    int genRand() {
    	Random rand = new Random();
    	int randInt = rand.nextInt(10000);
    	return randInt;
    }
    
    
    /**
     * Allocate and return a new process of the correct class. The class name
     * is specified by the <tt>nachos.conf</tt> key
     * <tt>Kernel.processClassName</tt>.
     *
     * @return	a new process of the correct class.
     */
    public static UserProcess newUserProcess() {
	return (UserProcess)Lib.constructObject(Machine.getProcessClassName());
    }

    /**
     * Execute the specified program with the specified arguments. Attempts to
     * load the program, and then forks a thread to run it.
     *
     * @param	name	the name of the file containing the executable.
     * @param	args	the arguments to pass to the executable.
     * @return	<tt>true</tt> if the program was successfully executed.
     */
    public boolean execute(String name, String[] args) {
	if (!load(name, args))
	    return false;
	
	new UThread(this).setName(name).fork();

	return true;
    }

    /**
     * Save the state of this process in preparation for a context switch.
     * Called by <tt>UThread.saveState()</tt>.
     */
    public void saveState() {
    }

    /**
     * Restore the state of this process after a context switch. Called by
     * <tt>UThread.restoreState()</tt>.
     */
    public void restoreState() {
	Machine.processor().setPageTable(pageTable);
    }

    /**
     * Read a null-terminated string from this process's virtual memory. Read
     * at most <tt>maxLength + 1</tt> bytes from the specified address, search
     * for the null terminator, and convert it to a <tt>java.lang.String</tt>,
     * without including the null terminator. If no null terminator is found,
     * returns <tt>null</tt>.
     *
     * @param	vaddr	the starting virtual address of the null-terminated
     *			string.
     * @param	maxLength	the maximum number of characters in the string,
     *				not including the null terminator.
     * @return	the string read, or <tt>null</tt> if no null terminator was
     *		found.
     */
    public String readVirtualMemoryString(int vaddr, int maxLength) {
	Lib.assertTrue(maxLength >= 0);

	byte[] bytes = new byte[maxLength+1];

	int bytesRead = readVirtualMemory(vaddr, bytes);

	for (int length=0; length<bytesRead; length++) {
	    if (bytes[length] == 0)
		return new String(bytes, 0, length);
	}

	return null;
    }

    /**
     * Transfer data from this process's virtual memory to all of the specified
     * array. Same as <tt>readVirtualMemory(vaddr, data, 0, data.length)</tt>.
     *
     * @param	vaddr	the first byte of virtual memory to read.
     * @param	data	the array where the data will be stored.
     * @return	the number of bytes successfully transferred.
     */
    public int readVirtualMemory(int vaddr, byte[] data) {
	return readVirtualMemory(vaddr, data, 0, data.length);
    }

    /**
     * Transfer data from this process's virtual memory to the specified array.
     * This method handles address translation details. This method must
     * <i>not</i> destroy the current process if an error occurs, but instead
     * should return the number of bytes successfully copied (or zero if no
     * data could be copied).
     *
     * @param	vaddr	the first byte of virtual memory to read.
     * @param	data	the array where the data will be stored.
     * @param	offset	the first byte to write in the array.
     * @param	length	the number of bytes to transfer from virtual memory to
     *			the array.
     * @return	the number of bytes successfully transferred.
     */
    // Needs to be modified for Task II (see project prompt)
    public int readVirtualMemory(int vaddr, byte[] data, int offset,
				 int length) {
	Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);

	byte[] memory = Machine.processor().getMemory();
	
	//*********** added code
		int amount = 0;
		while(offset < data.length && length > 0) {
			int addOffset = (vaddr % 1024); // initialize variable for offset of address
			int virPage = (vaddr / 1024);	// initialize variable for virtual page
			
			if(virPage < 0 || virPage >= pageTable.length ) { //if statement checking if virtual page is less than 0 OR greater/equal to pageTable
				break;
			}
			
			TranslationEntry pgTblEntry = pageTable[virPage]; // translation Entry 
			
			if(!pgTblEntry.valid) {		//checks if pgTblEntry is valid
				break;
			}
			//setting  used pgTblEntry to true 
			pgTblEntry.used = true; 		//
			
			int physPage = pgTblEntry.ppn;			//initializing new variable for the physical page
			int physAddress = (physPage * 1024 + addOffset); //initializing new variable for physical address 
			int sendingLength = Math.min(data.length - offset, Math.min(length, 1024 - addOffset)); // new variable sendingLength for the length to send 
			
			
			System.arraycopy(memory, physAddress, data, offset, sendingLength);
			
			
			length -= sendingLength;		// subtract sendingLength w/ length
			vaddr += sendingLength;			// add sendinglength w/ vaddr
			amount += sendingLength;		// add sendinglength w/ amount
			offset += sendingLength;		// add sendinglength w/ offset
		}
		
		return amount; 
		//**************
    
    }

    /**
     * Transfer all data from the specified array to this process's virtual
     * memory.
     * Same as <tt>writeVirtualMemory(vaddr, data, 0, data.length)</tt>.
     *
     * @param	vaddr	the first byte of virtual memory to write.
     * @param	data	the array containing the data to transfer.
     * @return	the number of bytes successfully transferred.
     */
    public int writeVirtualMemory(int vaddr, byte[] data) {
	return writeVirtualMemory(vaddr, data, 0, data.length);
    }

    /**
     * Transfer data from the specified array to this process's virtual memory.
     * This method handles address translation details. This method must
     * <i>not</i> destroy the current process if an error occurs, but instead
     * should return the number of bytes successfully copied (or zero if no
     * data could be copied).
     *
     * @param	vaddr	the first byte of virtual memory to write.
     * @param	data	the array containing the data to transfer.
     * @param	offset	the first byte to transfer from the array.
     * @param	length	the number of bytes to transfer from the array to
     *			virtual memory.
     * @return	the number of bytes successfully transferred.
     */
    // Also needs to be modified for Task II
    public int writeVirtualMemory(int vaddr, byte[] data, int offset,
				  int length) {
	Lib.assertTrue(offset >= 0 && length >= 0 && offset+length <= data.length);

	byte[] memory = Machine.processor().getMemory();
	
	//*********** added code
	int amount = 0;
	while(offset < data.length && length > 0) {
		int addOffset = (vaddr % 1024); // initialize variable for offset of address
		int virPage = (vaddr / 1024);	// initialize variable for virtual page
		
		if(virPage < 0 || virPage >= pageTable.length ) { //if statement checking if virtual page is less than 0 OR greater/equal to pageTable
			break;
		}
		
		TranslationEntry pgTblEntry = pageTable[virPage]; // translation Entry 
		
		if(!pgTblEntry.valid) {		//checks if pgTblEntry is valid
			break;
		}
		//setting dirty and used pgTblEntry to true 
		pgTblEntry.dirty = true;		//
		pgTblEntry.used = true; 		//
		
		int physPage = pgTblEntry.ppn;			//initializing new variable for the physical page
		int physAddress = (physPage * 1024 + addOffset); //initializing new variable for physical address 
		int sendingLength = Math.min(data.length - offset, Math.min(length, 1024 - addOffset)); // new variable sendingLength for the length to send 
		
		
		System.arraycopy(data, offset, memory, physAddress, sendingLength);
		
		
		length -= sendingLength;		// subtract sendingLength w/ length
		vaddr += sendingLength;			// add sendinglength w/ vaddr
		amount += sendingLength;		// add sendinglength w/ amount
		offset += sendingLength;		// add sendinglength w/ offset
	}
	
	return amount; 
	//**************
    }

    /**
     * Load the executable with the specified name into this process, and
     * prepare to pass it the specified arguments. Opens the executable, reads
     * its header information, and copies sections and arguments into this
     * process's virtual memory.
     *
     * @param	name	the name of the file containing the executable.
     * @param	args	the arguments to pass to the executable.
     * @return	<tt>true</tt> if the executable was successfully loaded.
     */
    private boolean load(String name, String[] args) {
	Lib.debug(dbgProcess, "UserProcess.load(\"" + name + "\")");
	
	OpenFile executable = ThreadedKernel.fileSystem.open(name, false);
	if (executable == null) {
	    Lib.debug(dbgProcess, "\topen failed");
	    return false;
	}

	try {
	    coff = new Coff(executable);
	}
	catch (EOFException e) {
	    executable.close();
	    Lib.debug(dbgProcess, "\tcoff load failed");
	    return false;
	}

	// make sure the sections are contiguous and start at page 0
	numPages = 0;
	for (int s=0; s<coff.getNumSections(); s++) {
	    CoffSection section = coff.getSection(s);
	    if (section.getFirstVPN() != numPages) {
		coff.close();
		Lib.debug(dbgProcess, "\tfragmented executable");
		return false;
	    }
	    numPages += section.getLength();
	}

	// make sure the argv array will fit in one page
	byte[][] argv = new byte[args.length][];
	int argsSize = 0;
	for (int i=0; i<args.length; i++) {
	    argv[i] = args[i].getBytes();
	    // 4 bytes for argv[] pointer; then string plus one for null byte
	    argsSize += 4 + argv[i].length + 1;
	}
	if (argsSize > pageSize) {
	    coff.close();
	    Lib.debug(dbgProcess, "\targuments too long");
	    return false;
	}

	// program counter initially points at the program entry point
	initialPC = coff.getEntryPoint();	

	// next comes the stack; stack pointer initially points to top of it
	numPages += stackPages;
	initialSP = numPages*pageSize;

	// and finally reserve 1 page for arguments
	numPages++;

	if (!loadSections())
	    return false;

	// store arguments in last page
	int entryOffset = (numPages-1)*pageSize;
	int stringOffset = entryOffset + args.length*4;

	this.argc = args.length;
	this.argv = entryOffset;
	
	for (int i=0; i<argv.length; i++) {
	    byte[] stringOffsetBytes = Lib.bytesFromInt(stringOffset);
	    Lib.assertTrue(writeVirtualMemory(entryOffset,stringOffsetBytes) == 4);
	    entryOffset += 4;
	    Lib.assertTrue(writeVirtualMemory(stringOffset, argv[i]) ==
		       argv[i].length);
	    stringOffset += argv[i].length;
	    Lib.assertTrue(writeVirtualMemory(stringOffset,new byte[] { 0 }) == 1);
	    stringOffset += 1;
	}

	return true;
    }

    /**
     * Allocates memory for this process, and loads the COFF sections into
     * memory. If this returns successfully, the process will definitely be
     * run (this is the last step in process initialization that can fail).
     *
     * @return	<tt>true</tt> if the sections were successfully loaded.
     */
    protected boolean loadSections() {
	if (numPages > Machine.processor().getNumPhysPages()) {
	    coff.close();
	    Lib.debug(dbgProcess, "\tinsufficient physical memory");
	    return false;
	}

	//Collect Pages for process
	pageTable = new TranslationEntry[numPages];
	
	for(int i = 0; i < numPages; i++){
		//Obtain a Free Page from Free Pages Stack
		int physPage = UserKernel.allocatePage();
	
		if(physPage < 0){//If Kernal Returns -1; there is no free pages 
		Lib.debug(dbgProcess, "\tinsufficient physical memory");
			for(int j = 0; j < i; j++){
				if(pageTable[j].valid){
					//Undo Grab Page
					UserKernel.deallocatePage(pageTable[j].ppn);
					pageTable[j].valid = false;
				}
			}
		coff.close();
		return false;
		}
		//Add Page to User Process to Use
		pageTable[i] = new TranslationEntry(i,physPage, true,false,false,false);
	}
	
	
	// load sections
	for (int s=0; s<coff.getNumSections(); s++) {
	    CoffSection section = coff.getSection(s);
	    
	    Lib.debug(dbgProcess, "\tinitializing " + section.getName()
		      + " section (" + section.getLength() + " pages)");

	    for (int i=0; i<section.getLength(); i++) {
			int vpn = section.getFirstVPN()+i;

			//Modded to use physical address connected to virtual
			section.loadPage(i, pageTable[vpn].ppn);
			
			if(section.isReadOnly()){
				pageTable[vpn].readOnly = true;
				}
	    }
	}
	// close
	coff.close();
	return true;
    }

    /**
     * Release any resources allocated by <tt>loadSections()</tt>.
     */
    protected void unloadSections() {
    	//For all pages
    	for (int i=0; i<pageTable.length; i++) {
    		//table is 
    		if (pageTable[i].valid) {
    			UserKernel.deallocatePage(pageTable[i].ppn);
    			pageTable[i].valid = false;
    		}
    	}
    }    

    /**
     * Initialize the processor's registers in preparation for running the
     * program loaded into this process. Set the PC register to point at the
     * start function, set the stack pointer register to point at the top of
     * the stack, set the A0 and A1 registers to argc and argv, respectively,
     * and initialize all other registers to 0.
     */
    public void initRegisters() {
	Processor processor = Machine.processor();

	// by default, everything's 0
	for (int i=0; i<processor.numUserRegisters; i++)
	    processor.writeRegister(i, 0);

	// initialize PC and SP according
	processor.writeRegister(Processor.regPC, initialPC);
	processor.writeRegister(Processor.regSP, initialSP);

	// initialize the first two argument registers to argc and argv
	processor.writeRegister(Processor.regA0, argc);
	processor.writeRegister(Processor.regA1, argv);
    }

    /**
     * Handle the halt() system call. 
     */
    private int handleHalt() {
    	// We must check if the process calling halt is the "root," or the first process created.
    	// ADD CODE HERE (use our established variable MathRand, and return 0 if not root)
    	if (this.MathRand != root)
    		// Non-root can't halt the system. Therefore, ignore request and return 0.
    		return 0;

	Machine.halt();
	
	Lib.assertNotReached("Machine.halt() did not halt machine!");
	return 0;
    }


    private static final int
        syscallHalt = 0,
	syscallExit = 1,
	syscallExec = 2,
	syscallJoin = 3,
	syscallCreate = 4,
	syscallOpen = 5,
	syscallRead = 6,
	syscallWrite = 7,
	syscallClose = 8,
	syscallUnlink = 9;

    /**
     * Handle a syscall exception. Called by <tt>handleException()</tt>. The
     * <i>syscall</i> argument identifies which syscall the user executed:
     *
     * <table>
     * <tr><td>syscall#</td><td>syscall prototype</td></tr>
     * <tr><td>0</td><td><tt>void halt();</tt></td></tr>
     * <tr><td>1</td><td><tt>void exit(int status);</tt></td></tr>
     * <tr><td>2</td><td><tt>int  exec(char *name, int argc, char **argv);
     * 								</tt></td></tr>
     * <tr><td>3</td><td><tt>int  join(int pid, int *status);</tt></td></tr>
     * <tr><td>4</td><td><tt>int  creat(char *name);</tt></td></tr>
     * <tr><td>5</td><td><tt>int  open(char *name);</tt></td></tr>
     * <tr><td>6</td><td><tt>int  read(int fd, char *buffer, int size);
     *								</tt></td></tr>
     * <tr><td>7</td><td><tt>int  write(int fd, char *buffer, int size);
     *								</tt></td></tr>
     * <tr><td>8</td><td><tt>int  close(int fd);</tt></td></tr>
     * <tr><td>9</td><td><tt>int  unlink(char *name);</tt></td></tr>
     * </table>
     * 
     * @param	syscall	the syscall number.
     * @param	a0	the first syscall argument.
     * @param	a1	the second syscall argument.
     * @param	a2	the third syscall argument.
     * @param	a3	the fourth syscall argument.
     * @return	the value to be returned to the user.
     */
	//Task 3 Start
    public int handleSyscall(int syscall, int a0, int a1, int a2, int a3) {
	switch (syscall) {
	case syscallHalt:
	    return handleHalt();
	case syscallExit:
		handleExit(a0);
	case syscallExec:
		return handleExec(a0, a1, a2);
	case syscallJoin:
		return handleJoin(a0, a1);
	case syscallClose:
		return handleClose(a0);
	case syscallUnlink:
		return handleUnlink(a0);//Temp
	case syscallCreate:
		return handleCreate(a0);
	case syscallOpen:
		return handleOpen(a0);
	case syscallRead:
		return handleRead(a0, a1, a2);
	case syscallWrite:
		return handleWrite(a0, a1, a2);

	default:
	    Lib.debug(dbgProcess, "Unknown syscall " + syscall);
	    Lib.assertNotReached("Unknown system call!");
	}
	return 0;
    }
    

    /**
     * Handle a user exception. Called by
     * <tt>UserKernel.exceptionHandler()</tt>. The
     * <i>cause</i> argument identifies which exception occurred; see the
     * <tt>Processor.exceptionZZZ</tt> constants.
     *
     * @param	cause	the user exception that occurred.
     */
	
	 private void handleExit(int status) { 
    	//check for a child process
    	if(isParent()) 
    		this.Child.cStat = status;
    	//bc we have 16 file locations in opened files
    	int i = 0;
    	while(i < 16) {
    		handleClose(i);
    		i++;}
    	//unload all sections
    	this.unloadSections();
    	if(root == this.MathRand)
    		Kernel.kernel.terminate();
    	else 
    		KThread.finish(); Lib.assertNotReached();
    }

  private int handleExec(int namePtr, int argc, int argv) {
    	if(namePtr < 0 || argc < 0 || argv < 0 || readVirtualMemoryString(namePtr, 256) == null) 
    		return -1;
    	String ff = readVirtualMemoryString(namePtr, 256);
    	int i = 0; int bytez; byte bytezz[]; String strings[];
    	bytezz = new byte[4]; //this is for 
    	strings = new String[argc]; //argc specifies the number of arguments to be passed to child
    	while(i < argc) {
    		bytez = readVirtualMemory(argv + i*4, bytezz);
    		if(bytez != 4) return -1;
    		//get address of next file
    		if(readVirtualMemoryString(Lib.bytesToInt(bytezz, 0), 256) == null) return -1;
    		strings[i] = readVirtualMemoryString(Lib.bytesToInt(bytezz, 0), 256);
    		if(i+1 == argc) {
    		    child chile = new child();
    		    chile.child = UserProcess.newUserProcess();
    		    if(chile.child.execute(ff, strings)) {
    		    	hm.put(chile.child.MathRand, chile);
    		    	return chile.child.MathRand;
    		    }
    		}
    		i++;
    	}
    	return -1;
    }
    //join will take the arguments of child process ID and the address of that status
    private int handleJoin(int procID, int addr) {
    	//as before we'll virtually pass bytes
    	if(procID < 0 || addr < 0 || !hm.containsKey(procID))
    		return -1;
    	int bytez; byte barr[]; child newC = new child();
    	newC = hm.get(procID);
    	//now join
    	newC.child.ut.join(); hm.remove(procID);
    	if(!newC.isNew) return 0;
    	barr = new byte[4];
    	barr = Lib.bytesFromInt(newC.cStat);
    	bytez = writeVirtualMemory(addr, barr);
    	if(bytez != 4)
    		return 0;
    	return 1;
    }
	
    public void handleException(int cause) {
    	Processor processor = Machine.processor();

    	switch (cause) {
    	case Processor.exceptionSyscall:
    	    int result = handleSyscall(processor.readRegister(Processor.regV0),
    				       processor.readRegister(Processor.regA0),
    				       processor.readRegister(Processor.regA1),
    				       processor.readRegister(Processor.regA2),
    				       processor.readRegister(Processor.regA3)
    				       );
    	    processor.writeRegister(Processor.regV0, result);
    	    processor.advancePC();
    	    break;				       
    				       
    	default:
    	    Lib.debug(dbgProcess, "Unexpected exception: " +
    		      Processor.exceptionNames[cause]);
    	    Lib.assertNotReached("Unexpected exception");
    	}
    }
    //Task 3 end
    /* Task I function implementation:
     * Our functions are private so that it can only be called on by handleSyscall (can't be accessed directly by user)
     * If any error should occur, always return -1.
     */
    private int handleCreate(int p) {
    	// First we use the char pointer argument and readVirtualMemory to obtain the name of the file
    	// Note: We use 256 for the max length of string, for if it should be any longer than 256 it could cause errors.
    	String filename = readVirtualMemoryString(p, 256);
    	// Check if the filename is valid to catch any errors
    	if (filename == null) 
    		return -1;
    	// We use our array OFile to check for an available open file. Max amount of files in our system is 16.
    	int fileDescriptor = -1;
    	for (int i = 0; i < 16; i++) {
    		if (OFile[i] == null) {
    			fileDescriptor = i;
    			break;
    		}
    	}
    	// If no available file (nothing assigned to variable) return -1.
    	if (fileDescriptor == -1)
    		return -1;
    	// Create the file. Second argument should be set to true so that it creates when file does not exist.
    	OpenFile newFile = ThreadedKernel.fileSystem.open(filename, true);
    	if (newFile == null)
    	    return -1;
    	// Assign newFile to our available slot.
    	OFile[fileDescriptor] = newFile;
    	// Return file descriptor at end
    	return fileDescriptor;
    }

    private int handleOpen(int p) {
    	// This function checks errors very similarly to our create function.
    	// Main difference between this and create is that a file is not created, as implied by name.
    	String filename = readVirtualMemoryString(p, 256);
    	if (filename == null)
    		return -1;
    	int fileDescriptor = -1;
    	// Again, loop to check that there are used files in the system.
    	for (int i = 0; i < 16; i++) {
    		if (OFile[i] == null) {
    			fileDescriptor = i;
    			break;
    		}
    	}
    	if (fileDescriptor == -1)
    		return -1;
    	// This time, the second argument for open() is passed as false, as we are not creating the file.
    	OpenFile newFile = ThreadedKernel.fileSystem.open(filename, false);
    	// If doesn't exist, return -1.
    	if (newFile == null)
    		return -1;
    	OFile[fileDescriptor] = newFile;
    	return fileDescriptor;
    }
    

    private int handleClose(int fileDescriptor) {
    	// Validate the file descriptor by making sure it is within the range of files. (in this case, range is 0 to 15 because of 16 files)
    	if (fileDescriptor < 0 || fileDescriptor > 15)
    		// Error, outside range.
    		return -1;
    	// Then check that file at file descriptor is not null
    	if (OFile[fileDescriptor] == null)
    		return -1;
    	// If everything is ok to close, run close.
    	// Other functions should ensure that any write data from a file is successfully flushed before closing.
    	OFile[fileDescriptor].close();
    	// Set file slot to null so that it may be reused.
    	OFile[fileDescriptor] = null;
    	return 0;
    }
	
private int handleRead(int fd, int buffer, int size)
    {
    	int zero = 0;
    	byte arrayBuffer[] = new byte[size];
    	int readBytes = OFile[fd].read(arrayBuffer, zero, size);//tbl[fd].read(arrayBuffer, zero, size);
    	int writtenBytes = writeVirtualMemory(buffer, arrayBuffer, zero, readBytes);
    	
    	if(OFile[fd] == null)//tbl[fd] == null)
    		return -1;
    	if(fd < 0)
    		return -1;
    	if(fd > 15)
    		return -1;
    	if(readBytes <= 0)
    		return -1;
    	if(writtenBytes != readBytes)
    		return -1;
    	
    	return readBytes;
    	
    }
	
private int handleWrite(int fd, int buffer, int size)
    {
    	int zero = 0;
    	if( OFile[fd] == null)//tbl[fd] == null)
    		return -1;
    	if(fd < 0)
    		return -1;
    	if(fd > 15)
    		return -1;
    	
    	byte arrayBuffer[] = new byte[size];
    	int readBytes = readVirtualMemory(buffer, arrayBuffer);
    	
    	return OFile[fd].write(arrayBuffer, zero, readBytes);//tbl[fd].write(arrayBuffer, zero, readBytes);
    	
    }
	
private int handleUnlink(int fd)
    {
    	String ff = readVirtualMemoryString(fd, 256);
    	int change = ThreadedKernel.fileSystem.remove(ff) ? 0 : -1;
	
	return change;
    }
	
    // End of Task I's functions
    
    
    // Task II function implementation:
    
    
    
    /** The program being run by this process. */
    protected Coff coff;

    /** This process's page table. */
    protected TranslationEntry[] pageTable;
    /** The number of contiguous pages occupied by the program. */
    protected int numPages;

    /** The number of pages in the program's stack. */
    protected final int stackPages = 8;
    
    private int initialPC, initialSP;
    private int argc, argv;
    
    private static final int pageSize = Processor.pageSize;
    private static final char dbgProcess = 'a';
    private int pID;
	private static int pIDs = 0;
	private HashMap<Integer, OpenFile> allFile;
	private List<Integer> desc;
	//public OpenFile[] tbl;
	public OpenFile [] OFile; //record file use
	
}
