import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UI {
	
	
    public int QUIET = 0;
    public int TRACE = 1;
    public int STEP = 2;
    
    private static BufferedReader inputReader;

    public UI() {
        inputReader = new BufferedReader(
                new InputStreamReader(System.in));
    }
    
    
    private static boolean continueRunning(BufferedReader inputReader) {
        String run = "y";
        boolean continueRunning = true;
        System.out.println("Would you like to execute again? (y/n)");
        try {
            run = inputReader.readLine();
        } catch (IOException ioe) {
            System.out.println("Unable to read input, please try again.");
        }
        while (!run.equals("y") && !run.equals("n")) {
            System.out.println(
                    "Invalid input. Would you like to execute again? (y/n)");
            try {
                run = inputReader.readLine();
            } catch (IOException ioe) {
                System.out.println("Unable to read input, please try again.");
            }
        }
        if (run.equals("n")) {
            continueRunning = false;
        }
        return continueRunning;
    }
    
    public static int retrieveStartAddress () {
        String address = "";
        int startAddress = 0;
        System.out.println("Select a start address (0-65535 or 0x0000-0xFFFF)");
        while (true) {
            try {
                address = inputReader.readLine();
            } catch (IOException ioe) {
                System.out.println("Unable to read input, please try again.");
            }
	        	try {
	        		if (address.indexOf('x') >= 0) {
	        			startAddress = Integer.parseInt(address,16);
	        			break;
	        		}
	        		else {
	        			startAddress = Integer.parseInt(address);
	        			break;
	        		}
	        }
	        	catch (Exception e) {
	        		System.out.println("Invalid entry, try again");
	        	}
        }
        return startAddress;
    }
    
    public static String getLinkedFileName () {
        String name = "";
        System.out.println("Enter a linked file name (maximum 6 characters)");
        while (true) {
            try {
                name = inputReader.readLine();
            } catch (IOException ioe) {
                System.out.println("Unable to read input, please try again.");
            }
            if (name.length() > 6)
	        		System.out.println("Invalid entry, try again");
            else {
            		break;
            }
	    }
        return (name + ".o");
    }

    private static int runMode(BufferedReader inputReader) {
        System.out.println("What mode would you like to run in?");
        System.out.println("Please enter quiet(q), trace(t), or step(s)");
        String runModeInput = "";
        int runMode = 0;
        boolean legalValue = false;
        while (!legalValue) {
            legalValue = true;
            try {
                runModeInput = inputReader.readLine();
            } catch (IOException ioe) {
                System.out.println("Error reading input.");
            }
            switch (runModeInput) {
                case "quiet":
                case "q":
                    runMode = 0;
                    break;
                case "trace":
                case "t":
                    runMode = 1;
                    break;
                case "step":
                case "s":
                    runMode = 2;
                    System.out.println("You are about to enter step mode. ");
                    System.out.println(
                            "At any point you may enter (q) to switch to quiet mode or (t) to switch to trace mode.");
                    System.out.println(
                            "You may also enter (m) to print the current value of memory and registers.");
                    System.out.println("Press enter to continue.");
                    try {
                        inputReader.readLine();
                    } catch (IOException ioe) {
                    }
                    break;
                default:
                    legalValue = false;
                    System.out.println(
                            "Incorrect Input. Please enter either quiet(q), trace(t), or step(s)");
            }
        }
        return runMode;
    }

    private static long userTimeout(BufferedReader inputReader) {
        long timeout = 180000;
        System.out.println(
                "The default timeout for execution is 180000ms, would you like to set your own? (y/n)");
        String userInput = "";
        try {
            userInput = inputReader.readLine();
        } catch (IOException ioe) {
            System.out.println("Unable to read input, please try again.");
        }
        while (!userInput.equals("y") && !userInput.equals("n")) {
            System.out.println(
                    "Invalid input. Would you like to set your own? (y/n)");
            try {
                userInput = inputReader.readLine();
            } catch (IOException ioe) {
                System.out.println("Unable to read input, please try again.");
            }
        }
        if (userInput.equals("y")) {
            boolean goodInput = false;
            while (!goodInput) {
                System.out.println("Enter your timeout in milliseconds");
                try {
                    userInput = inputReader.readLine();
                } catch (IOException ioe) {
                    System.out
                            .println("Unable to read input, please try again.");
                }
                timeout = Long.parseLong(userInput);
                if (timeout > 0) {
                    goodInput = true;
                } else {
                    System.out.println("Invalid input, please try again.");
                }
            }
        }

        return timeout;
    }
    public static int maxInstructions(Simulator fileSim,BufferedReader inputReader) {
		
		int max = fileSim.MAX_INSTRUCTIONS;
		System.out.println(
            "The default maximum instructions is "+max+" instructions, would you like to set your own?");
		System.out.println("Enter an integer to set your own or press enter to use the default");
    String userInput = "";
    try {
        userInput = inputReader.readLine();
    } catch (IOException ioe) {
        System.out.println("Unable to read input, please try again.");
    }
    try {
        max = Integer.parseInt(userInput);
    } catch (NumberFormatException e) {
        max = fileSim.MAX_INSTRUCTIONS;
    }
    return max;
}
    public void beginSimulation(String fileName) {
        boolean isRunning = true;
        System.out.println("Welcome to the AU-17 Machine!");
        while (isRunning) {
            Loader fileLoader = new Loader();
            Memory fileMemory = fileLoader.loadFile(fileName);
            if (fileMemory != null) {
            		long timeout = 180000;
                int runMode = runMode(inputReader);
                Simulator fileSimulator = new Simulator(runMode, timeout);
                int maxInstructions = maxInstructions(fileSimulator,inputReader);
                fileSimulator.updateMaxInstructions(maxInstructions);
                fileSimulator.simulateProgram(fileMemory);
                System.out.println("Your program finished executing.");
            }
            isRunning = continueRunning(inputReader);
        }
    }
}
