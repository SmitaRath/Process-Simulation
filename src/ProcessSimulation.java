import java.text.DecimalFormat;
import java.util.*;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.PrintStream;

public class ProcessSimulation {
    //Array list for process control block
    ArrayList<ProcessControlBlock> PCB = new ArrayList<>();
    //Queue for ready queue
    LinkedList<ReadyInputOutputQueue> readyQueue = new LinkedList<>();
    //Queue for IO queue
    LinkedList<ReadyInputOutputQueue> inputOutputQueue = new LinkedList<>();
    //Linked List for events
    LinkedList<EventDetails> eventDetails = new LinkedList<>();
    //Array List for process details
    ArrayList<ProcessDetails> processDetails = new ArrayList<>();
    //Array List for gantt chart for first CPU cycle
    ArrayList<GanttChart> ganttCharts = new ArrayList<>();
    Random rand = new Random();
    //variable for CPU
    char CPU = 'N';
    //variable for input output device
    char inputOutput = 'N';
    //to store the IO burst provided by the user
    double IOburst;
    //to store the mean Inter IO Interval provided by the user
    double meanInterIOInterval;
    //to store the number of process
    int noOfProcess;
    DecimalFormat df = new DecimalFormat("#.#####");
    DecimalFormat df1 = new DecimalFormat("#.##");
    //to store the scheduling algorithm provided by the user
    String schedulingAlgorithm;
    int counter = 1;
    int processCompletedNumber = 1;
    //to store the quantum provide by the user
    double quantum;
    boolean allProcessArrived = false;
    boolean processCompletedFlag = false;


    //constructor for initialization
    public ProcessSimulation(int minExecutionTimeP, int maxExecutionTimeP, int noOfProcess, double IOburst, double meanInterIOInterval, String schedulingAlgorithm, double quantum) {
        this.IOburst = IOburst;
        this.meanInterIOInterval = meanInterIOInterval;
        this.noOfProcess = noOfProcess;
        this.schedulingAlgorithm = schedulingAlgorithm;
        this.quantum = quantum;
        double meanInterArrival;
        int executionTime;

        //Initializing the process details with the no of process provided by the user.
        for (int i = 0; i < noOfProcess; i++) {
            meanInterArrival = this.meanInterIOInterval + (5 * i);
            executionTime = generateUniformDistribution(minExecutionTimeP, maxExecutionTimeP);
            processDetails.add(new ProcessDetails("P" + (i + 1), executionTime, meanInterArrival, executionTime));
        }



        //if the scheduling algorithm is SJF, sorting the process details according to execution time
        if (schedulingAlgorithm == "SJF") {
            Collections.sort(processDetails);
        }

        //Initializing the event details with the process details for ArrivalCPU event.
        for (ProcessDetails process : processDetails) {
            eventDetails.add(new EventDetails(0, process.processName, "ArrivalCPU"));
        }

    }

    //method for uniformly distribution of execution time
    private int generateUniformDistribution(int min, int max) {
        return rand.nextInt((max - min) + 1) + min;
    }

    //method for exponentially generating the next IO cycle.
    private double generateExponentialDistribution(double meanInterInterval) {
        return (-meanInterInterval * (Math.log(((Math.random() * 65536 + 1) / 65536))));
    }

    //method for searching the index of process in Process control block
    private int searchPCB(String processName) {
        ProcessControlBlock pcb;
        for (int i = 0; i < PCB.size(); i++) {
            pcb = PCB.get(i);
            if (pcb.processName.equals(processName))
                return i;

        }
        return -1;
    }

    //method for searching the index of process in Process Details
    private int searchProcessDetails(String processName) {
        ProcessDetails process;
        for (int i = 0; i < processDetails.size(); i++) {
            process = processDetails.get(i);
            if (process.processName.equals(processName))
                return i;

        }
        return -1;
    }

    //method for ArrivalCPU event
    public void arrivalCPU(double clock, int processNo, String processName, PrintStream fileOut, PrintStream fileout1) {
        try {
            int PCBno;
            double cpuCompletionTime;
            double waitingTime = 0;
            boolean flag = false;
            double remainingCPUBurstTime = 0;

            //retrieving the process details intor variable
            ProcessDetails procDetails = processDetails.get(processNo);

            if (counter <= noOfProcess) {
                //if the process are ariving in the system then PCB entry will be created for all the process
                PCB.add(new ProcessControlBlock(processName, procDetails.totalExecutionTime));
                counter++;

                //process are arriving so they will queued in the ready and status will be ready at time 0
                PCBno = searchPCB(processName);
                //setting the status of the process to ready at time 0
                PCB.get(PCBno).state = "Ready";
                //setting the starting waiting time of the process to clock
                processDetails.get(processNo).waitingTimeStart = clock;
                //generating the next IO cycle exponentially
                cpuCompletionTime = generateExponentialDistribution(processDetails.get(processNo).meanInterIOInterval);
                //setting the CPU burst which is generated
                processDetails.get(processNo).CPUBurst = cpuCompletionTime;
                //adding the generated CPU burst to the ArrayList of all CPU bursts
                processDetails.get(processNo).allCPUBurst.addLast(cpuCompletionTime);

                //Logging to the out file - as process is added to the ready queue
                fileOut.println("At clock --- " + clock);
                fileOut.println("Process added to ready queue --- " + processName);
                readyQueue.offer(new ReadyInputOutputQueue(processName,
                        processDetails.get(processNo).totalExecutionTime,
                        processDetails.get(processNo).remainingExecutionTime,
                        cpuCompletionTime));

                //Logging to the out file - as process is added to the ready queue
                fileOut.println("Ready queue content=============================");
                for (ReadyInputOutputQueue rq : readyQueue) {
                    fileOut.println(rq.toString());
                }

                //when all process arrived in the system at time 0 the first process will be picked up and processed by CPU if the CPU is free
                if (readyQueue.size() == noOfProcess) {
                    //if the scheduling algorithm is SJF, sorting the ready queue according to the CPU burst.
                    if (schedulingAlgorithm == "SJF") {
                        Collections.sort(readyQueue);
                    }
                    //process which is at first picked from the ready queue
                    ReadyInputOutputQueue newProcess = readyQueue.poll();
                    processName = newProcess.processName;
                    //logging the process picked by ready queue to log file
                    fileOut.println("At clock --- " + clock);
                    fileOut.println("Process picked from ready queue --- " + processName);
                    fileOut.println("Ready queue content=============================");
                    for (ReadyInputOutputQueue rq : readyQueue) {
                        fileOut.println(rq.toString());
                    }
                    processNo = searchProcessDetails((newProcess.processName));
                    //setting the flag to ture as all processes are arrived.
                    allProcessArrived = true;
                }
            }
            //if all processes are arrived at time 0 then CPU will start executing them
            if (allProcessArrived) {
                //checking whether CPU is busy or free
                if (CPU == 'N') {
                    PCBno = searchPCB(processName);
                    processDetails.get(processNo).cpuStatus = "Y";
                    //setting the CPU flag to Y to make it busy
                    CPU = 'Y';
                    //setting the state of the process in PCB to running.
                    PCB.get(PCBno).state = "Running";


                    cpuCompletionTime = processDetails.get(processNo).CPUBurst;

                    //if the scheduling algorihtm is Round Robin
                    if (schedulingAlgorithm == "RR") {

                        //checking generated CPU burst time is greater than quantum(time slice)
                        if (cpuCompletionTime > quantum) {
                            //if yes then setting flag to true
                            flag = true;
                            //computing the remaining burst time from the total generated CPU burst
                            remainingCPUBurstTime = cpuCompletionTime - quantum;
                            //updating CPU burst to quantum
                            cpuCompletionTime = quantum;
                            processDetails.get(processNo).CPUBurst = cpuCompletionTime;
                            //updating the CPU burst in ArrayList to the quantum
                            processDetails.get(processNo).allCPUBurst.removeLast();
                            processDetails.get(processNo).allCPUBurst.addLast(cpuCompletionTime);
                            //updating the remaining burst time of the process
                            processDetails.get(processNo).remainingCPUBurstTime = remainingCPUBurstTime;
                        }
                    }

                    //checking if the generated CPU burst is greater than the remaining execution time of the process
                    if (cpuCompletionTime > processDetails.get(processNo).remainingExecutionTime) {
                        //if yes setting the CPU burst to remaining execution time
                        cpuCompletionTime = processDetails.get(processNo).remainingExecutionTime;
                        processDetails.get(processNo).CPUBurst = cpuCompletionTime;
                        processDetails.get(processNo).allCPUBurst.removeLast();
                        processDetails.get(processNo).allCPUBurst.addLast(cpuCompletionTime);
                    }

                    //if this is for first CPU burst cycle collecting the statistics for the gantt chart
                    if (processDetails.get(processNo).firstcycle) {
                        ganttCharts.add(new GanttChart(processName, processDetails.get(processNo).readyQueuetotalWaitingTime, processDetails.get(processNo).CPUBurst, false));
                    }
                    //Creating Gantt chart to out file.
                    fileout1.println(df.format(procDetails.readyQueuetotalWaitingTime) + "-------------------------   ");
                    fileout1.println("      |                    |  ");
                    fileout1.println("      |" + processName + " " + " CPU Burst " + df.format(cpuCompletionTime) + " Total execution time " + procDetails.totalExecutionTime + " Completed Execution Time " + df.format(procDetails.completedExecutionTime) + " Remaining Time " + df.format(procDetails.remainingExecutionTime));
                    fileout1.println("      |                    |  ");

                    //logging to the out file that process is picked by CPU
                    fileOut.println("At clock --- " + clock);
                    fileOut.println("Process executed by CPU --- " + processName + " CPU Burst = " + cpuCompletionTime);

                    //for round robin algorithm checking the flag and CPU burst is less than remaining execution time generating InterruptTimer event at clock plus cpu burst time
                    if (flag && cpuCompletionTime < processDetails.get(processNo).remainingExecutionTime) {
                        eventDetails.add(new EventDetails(clock + cpuCompletionTime,
                                processName,
                                "InterruptTimer"));
                        flag = false;
                    }
                    //else generating CPU completion event at clock plus cpu burst time
                    else {
                        eventDetails.add(new EventDetails(clock + cpuCompletionTime,
                                processName,
                                "CompletionCPU"));
                    }

                }
                //if cpu is busy process will be queued to ready queue
                else {
                    //logging the process queue to ready queue to log file
                    fileOut.println("At clock --- " + clock);
                    fileOut.println("Process picked from ready queue --- " + processName);


                    PCBno = searchPCB(processName);
                    //setting the state of the process in PCB to ready
                    PCB.get(PCBno).state = "Ready";
                    //updating waiting time start , cpu burst and all cpu burst for the process.
                    processDetails.get(processNo).waitingTimeStart = clock;
                    cpuCompletionTime = generateExponentialDistribution(processDetails.get(processNo).meanInterIOInterval);
                    processDetails.get(processNo).CPUBurst = cpuCompletionTime;
                    processDetails.get(processNo).allCPUBurst.addLast(cpuCompletionTime);

                    readyQueue.offer(new ReadyInputOutputQueue(processName,
                            processDetails.get(processNo).totalExecutionTime,
                            processDetails.get(processNo).remainingExecutionTime,
                            cpuCompletionTime));

                    fileOut.println("Ready queue content=============================");
                    for (ReadyInputOutputQueue rq : readyQueue) {
                        fileOut.println(rq.toString());
                    }


                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //method for InterruptTimer Event for Round Robin algorithm
    public void interruptTimer(double clock, int processNo, String processName, PrintStream fileOut) {
        int PCBno;
        PCBno = searchPCB(processName);
        //updating the interrupt process status to ready
        PCB.get(PCBno).state = "Ready";
        double waitingTime;
        //updating the execution time, completed by the process
        processDetails.get(processNo).completedExecutionTime = processDetails.get(processNo).completedExecutionTime + processDetails.get(processNo).CPUBurst;
        //updating the remaining execution time
        processDetails.get(processNo).remainingExecutionTime = processDetails.get(processNo).totalExecutionTime -
                processDetails.get(processNo).completedExecutionTime;
        //updating the remaining execution time of the process in the PCB
        PCB.get(PCBno).remainingTime = processDetails.get(processNo).remainingExecutionTime;
        //setting the waiting time start of the process to clock as it is entering ready queue
        processDetails.get(processNo).waitingTimeStart = clock;
        //setting the next CPU burst to the remaining CPU burst time which was left after the quantum
        processDetails.get(processNo).CPUBurst = processDetails.get(processNo).remainingCPUBurstTime;
        processDetails.get(processNo).allCPUBurst.addLast(processDetails.get(processNo).remainingCPUBurstTime);
        //setting the remaining CPU burst time to zero
        processDetails.get(processNo).remainingCPUBurstTime = 0;
        //marking the CPU free
        CPU = 'N';
        processDetails.get(processNo).cpuStatus = "N";

        //logginf to the out file as process is adding to the ready queue
        fileOut.println("At clock --- " + clock);
        fileOut.println("Process added to ready queue --- " + processName);
        readyQueue.offer(new ReadyInputOutputQueue(processName,
                processDetails.get(processNo).completedExecutionTime,
                processDetails.get(processNo).remainingExecutionTime,
                processDetails.get(processNo).CPUBurst));
        fileOut.println("Ready queue content=============================");
        for (ReadyInputOutputQueue rq : readyQueue) {
            fileOut.println(rq.toString());
        }

        //if scheduling algorithm is SJF, sorting the ready queue accoridng to the CPU burst time
        if (schedulingAlgorithm == "SJF") {
            Collections.sort(readyQueue);
        }
        ReadyInputOutputQueue newProcess = readyQueue.poll();
        //logging to the out file as process is picked from the ready queue
        fileOut.println("At clock --- " + clock);
        fileOut.println("Process picked from ready queue --- " + newProcess.processName);
        fileOut.println("Ready queue content=============================");
        for (ReadyInputOutputQueue rq : readyQueue) {
            fileOut.println(rq.toString());
        }
        int processIndex = searchProcessDetails(newProcess.processName);
        //calculating the waiting time which process spent in the ready queue
        waitingTime = clock - processDetails.get(processIndex).waitingTimeStart;
        processDetails.get(processIndex).waitingTimeStart = 0;
        //updating the ready queue waiting time of the process
        processDetails.get(processIndex).readyQueuetotalWaitingTime = processDetails.get(processIndex).readyQueuetotalWaitingTime + waitingTime;

        //generating ArrivalCPU event at clock for the process picked from the ready queue
        eventDetails.add(new EventDetails(clock,
                newProcess.processName,
                "ArrivalCPU"));

    }

    //method for CompletionCPU event
    public void completionCPU(double clock, int processNo, String processName, PrintStream fileOut) {
        int PCBno;
        double waitingTime;
        //marking the CPU free
        CPU = 'N';
        processDetails.get(processNo).cpuStatus = "N";
        //first cycle is completed for the process so marking it to false
        processDetails.get(processNo).firstcycle = false;
        //updating the total time executed by the process
        processDetails.get(processNo).completedExecutionTime = processDetails.get(processNo).CPUBurst +
                processDetails.get(processNo).completedExecutionTime;

        //updatinng the total remaining exceution time of the process
        processDetails.get(processNo).remainingExecutionTime =
                processDetails.get(processNo).totalExecutionTime -
                        processDetails.get(processNo).completedExecutionTime;

        PCBno = searchPCB(processName);
        //updating the remaining execution time of the process in the PCB
        PCB.get(PCBno).remainingTime = processDetails.get(processNo).remainingExecutionTime;

        ProcessDetails procDetails = processDetails.get(processNo);

        //checking if the process is completed
        if (procDetails.completedExecutionTime == procDetails.totalExecutionTime) {

            //if yes logging to the out file
            fileOut.println("At clock --- " + clock);
            fileOut.println("Process terminated--- " + processName);
            //setting the state of the process to terminated
            PCB.get(PCBno).state = "terminated";
            //setting the process finished flag to true
            processDetails.get(processNo).finished = true;
            //setting the processCompleted flag to true, this flag will be used in processSimulationProc to remove the process from PCB
            processCompletedFlag = true;
            //setting the finish time to clock in the process details
            processDetails.get(processNo).finishTime = clock;

        }
        //if process is not finsihed
        else {
            //checking if the IO device is free or not
            if (inputOutput == 'N') {
                //if the IO device is free generating the ArrivalI/O event at clock
                eventDetails.add(new EventDetails(clock,
                        processName,
                        "ArrivalI/O"));
            }
            //if IO device is not free
            else {
                //logging to the out file as process is adding to the IO queue
                fileOut.println("At clock --- " + clock);
                fileOut.println("Process added to I/O queue --- " + processName);
                PCBno = searchPCB(processName);
                //setting the state of the process to waiting
                PCB.get(PCBno).state = "Waiting";
                //setting the starting io waiting time to clock
                processDetails.get(processNo).ioWaitingTimeStart = clock;
                //adding the process to io queue
                inputOutputQueue.offer(new ReadyInputOutputQueue(processName,
                        procDetails.totalExecutionTime,
                        procDetails.remainingExecutionTime,
                        procDetails.CPUBurst));
                fileOut.println("IO queue content***********************");
                for (ReadyInputOutputQueue ioq : inputOutputQueue) {
                    fileOut.println(ioq.toString());
                }


            }
        }
        //checking if the ready queue is empty or not
        if (!readyQueue.isEmpty()) {
            //if not empty
            //if scheduling algorithm is SJF sorting the ready queue accoridng to CPU burst
            if (schedulingAlgorithm == "SJF") {
                Collections.sort(readyQueue);
            }
            //picking from the ready queue
            ReadyInputOutputQueue newProcess = readyQueue.poll();
            //logging to the out file
            fileOut.println("At clock --- " + clock);
            fileOut.println("Process picked from ready queue --- " + newProcess.processName);
            fileOut.println("Ready queue content=============================");
            for (ReadyInputOutputQueue rq : readyQueue) {
                fileOut.println(rq.toString());
            }
            int processIndex = searchProcessDetails(newProcess.processName);
            //calcualting the waiting time of the process which spent in the ready queue
            waitingTime = clock - processDetails.get(processIndex).waitingTimeStart;
            processDetails.get(processIndex).waitingTimeStart = 0;
            //updating the total waiting time spent in the ready queue by the process
            processDetails.get(processIndex).readyQueuetotalWaitingTime = processDetails.get(processIndex).readyQueuetotalWaitingTime + waitingTime;
            //generating the "ArrivalCPU" event at clcok
            eventDetails.add(new EventDetails(clock,
                    newProcess.processName,
                    "ArrivalCPU"));

        }
        //if ready queue is empty setting the CPU flag to N
        else {
            CPU = 'N';
        }

    }

    //method for "ArrivalI/O event
    public void arrivalInputOutput(double clock, int processNo, String processName, PrintStream fileOut) {
        int PCBno;
        double iowaitingTime = 0;
        PCBno = searchPCB(processName);
        //setting the state of the process to waiting in PCB
        PCB.get(PCBno).state = "Waiting";
        //marking the IO device busy
        inputOutput = 'Y';
        //logging to the out file
        fileOut.println("At clock --- " + clock);
        fileOut.println("Process executing I/O --- " + processName);
        //updating the IOburst which is provided by the user
        processDetails.get(processNo).ioburst = IOburst;
        //calcualting the IO waiting time which process spent in the IO queue
        if (processDetails.get(processNo).ioWaitingTimeStart != 0) {
            iowaitingTime = clock - processDetails.get(processNo).ioWaitingTimeStart;
            processDetails.get(processNo).ioWaitingTimeStart = 0;
        }
        //updating the total io wait time of the process
        processDetails.get(processNo).iowait = processDetails.get(processNo).iowait + iowaitingTime;
        //generating the CompletionI/O event at clock + IOburst time
        eventDetails.add(new EventDetails(clock + IOburst,
                processName,
                "CompletionI/O"));

    }

    //method for CompletionI/O event
    public void completionInputOutput(double clock, int processNo, String processName, PrintStream fileOut) {
        double cpuCompletionTime;
        int no;
        int PCBno;
        //marking the IO device free
        inputOutput = 'N';
        //updating the total IO execution time
        processDetails.get(processNo).ioexecutionTime =
                processDetails.get(processNo).ioexecutionTime +
                        processDetails.get(processNo).ioburst;

        ProcessDetails procDetails = processDetails.get(processNo);
        //generating the next IO cycle exponentially
        cpuCompletionTime = generateExponentialDistribution(processDetails.get(processNo).meanInterIOInterval);
        processDetails.get(processNo).CPUBurst = cpuCompletionTime;
        processDetails.get(processNo).allCPUBurst.addLast(cpuCompletionTime);
        //if CPU is free
        if (CPU == 'N') {
            //generating new event ArrivalCPU at clock
            eventDetails.add(new EventDetails(clock,
                    processName,
                    "ArrivalCPU"));

        }
        //if CPU is not free
        else {
            PCBno = searchPCB(processName);
            //setting the state of the process to ready in process control block
            PCB.get(PCBno).state = "Ready";
            //setting the start of the waiting time to clock
            processDetails.get(processNo).waitingTimeStart = clock;

            //logging ot the out file
            fileOut.println("At clock --- " + clock);
            fileOut.println("Process added to ready queue --- " + processName);


            readyQueue.offer(new ReadyInputOutputQueue(processName,
                    procDetails.totalExecutionTime,
                    procDetails.remainingExecutionTime,
                    cpuCompletionTime));

            fileOut.println("Ready queue content=============================");
            for (ReadyInputOutputQueue rq : readyQueue) {
                fileOut.println(rq.toString());
            }


        }

        //checking if IO queue is empty or not
        if (!inputOutputQueue.isEmpty()) {

            //if not empty
            ReadyInputOutputQueue newProcess = inputOutputQueue.poll();
            //logging to the out file
            fileOut.println("At clock --- " + clock);
            fileOut.println("Process picked from IO queue --- " + newProcess.processName);

            fileOut.println("IO queue content***********************");
            for (ReadyInputOutputQueue ioq : inputOutputQueue) {
                fileOut.println(ioq.toString());
            }
            //generating ArrivalI/O event at clock
            eventDetails.add(new EventDetails(clock,
                    newProcess.processName,
                    "ArrivalI/O"));

        } else {
            //if empty marking IO free
            inputOutput = 'N';
        }
    }

    //method for process simulation
    public void processSimulationProc() {
        try {
            int index = 0;
            String eventType;
            int processNo;
            String processName;
            double clock;
            int i = 1;
            double p = 1;
            double avgWaitingTime = 0;
            double totalWaitingTime = 0;
            double avgTurnAroundTime = 0;
            double totalTurnAroundTime = 0;
            double cputotalUtilisationTime = 0;
            double cpuUtilisation = 0;
            EventDetails eventRecord;
            int pcbSize;
            int clockCounter = 1;
            int ganttChartCounter = 10;
            PrintStream fileOut = new PrintStream("./out.txt");
            PrintStream fileOut1 = new PrintStream("./gantt.txt");
            PrintStream originalOut = System.out;
            BufferedWriter writer = Files.newBufferedWriter(Paths.get("Output_Simulation_File.csv"));

            if(schedulingAlgorithm=="RR"){
                fileOut.println("Quantum = " + quantum);
            }
            fileOut1.println("Ready Queue Waiting Time");

            do {
                //retrieving the first event record from the eventDetails
                eventRecord = eventDetails.poll();
                //retrieving the time of the event
                clock = eventRecord.time;
                //retrieving the event type
                eventType = eventRecord.eventType;
                //retrieving the process name
                processName = eventRecord.processName;
                //getting the index of the process in process details
                processNo = searchProcessDetails(processName);

                //checking for the type of event, depend on the event type specific methods are called
                switch (eventType) {

                    case "ArrivalCPU":
                        arrivalCPU(clock, processNo, processName, fileOut, fileOut1);
                        break;

                    case "CompletionCPU":
                        completionCPU(clock, processNo, processName, fileOut);
                        break;

                    case "ArrivalI/O":
                        arrivalInputOutput(clock, processNo, processName, fileOut);
                        break;

                    case "CompletionI/O":
                        completionInputOutput(clock, processNo, processName, fileOut);
                        break;

                    case "InterruptTimer":
                        interruptTimer(clock, processNo, processName, fileOut);
                        break;
                }
                //sorting the event according to time/clock
                Collections.sort(eventDetails);

                //if the processCompletedFlag is true which was set in CompletionCPU event, removing the process from PCB
                if (processCompletedFlag) {
                    //writing to the excel file
                    writer.newLine();
                    writer.newLine();
                    writer.append("Process Completed = " + processName + "");
                    writer.newLine();
                    writer.append("Process No, State, Execution Time, Remaining Execution Time");
                    //writing ot the log file
                    fileOut.println("Process Completed = " + processName);
                    writer.newLine();
                    for (ProcessControlBlock process : PCB) {
                        writer.append(String.valueOf(process.processName));
                        writer.append(",");
                        writer.append(String.valueOf(process.state));
                        writer.append(",");
                        writer.append(String.valueOf(process.executionTime));
                        writer.append(",");
                        writer.append(String.valueOf(process.remainingTime));
                        writer.append(",");
                        writer.newLine();
                        fileOut.println(process.toString());
                        fileOut.println("");
                        fileOut.println("");
                    }

                    //removing the process from PCB as the process is finished.
                    pcbSize = searchPCB(processName);
                    PCB.remove(pcbSize);
                    //setting the process completed flag to false
                    processCompletedFlag = false;

                }

                //loop condition to check if the processes are present in Process control block
            } while (!PCB.isEmpty());


            //logging the process details in the log file and  excel file.
            for (ProcessDetails process : processDetails) {

                writer.newLine();
                writer.newLine();
                writer.append("Process No, Total Execution Time, Completed Execution Time, Remaining Execution Time, Ready Queue Total Waiting Time, IO queue Waiting Time,IO Execution Time,Finish Time, Turn Around Time");
                writer.newLine();
                writer.append(String.valueOf(process.processName));
                writer.append(",");
                writer.append(String.valueOf(process.totalExecutionTime));
                writer.append(",");
                writer.append(String.valueOf(process.completedExecutionTime));
                writer.append(",");
                writer.append(String.valueOf(process.remainingExecutionTime));
                writer.append(",");
                writer.append(String.valueOf(process.readyQueuetotalWaitingTime));
                writer.append(",");
                writer.append(String.valueOf(process.iowait));
                writer.append(",");
                writer.append(String.valueOf(process.ioexecutionTime));
                writer.append(",");
                writer.append(String.valueOf(process.finishTime));
                writer.append(",");
                writer.append(String.valueOf(process.completedExecutionTime + process.readyQueuetotalWaitingTime + process.ioexecutionTime + process.iowait));
                writer.append(",");
                writer.newLine();
                System.out.println(process.toString());
                fileOut.println(process.toString());
                //calculating the total execution time by the CPU
                cputotalUtilisationTime = cputotalUtilisationTime + process.completedExecutionTime;
                //calculating the toal time spent by all the process in the ready queue
                totalWaitingTime = totalWaitingTime + process.readyQueuetotalWaitingTime;
                //calculating the total turn around time for all the process.
                totalTurnAroundTime = totalTurnAroundTime + (process.completedExecutionTime + process.readyQueuetotalWaitingTime + process.ioexecutionTime + process.iowait);
            }

            //logging all the CPU bursts of all the process to excel file
            writer.append("CPU Bursts for all the processes");
            writer.newLine();
            writer.append("Process No, CPU Burst");
            for (ProcessDetails process : processDetails) {
                writer.newLine();
                writer.append(String.valueOf(process.processName));
                for (Double dd : process.allCPUBurst) {
                    writer.append(",");
                    writer.append(String.valueOf(df.format(dd)));
                }
            }

            //calculating CPU utilisation by total time spent in the CPU dividing by the end time at which the last process ends
            cpuUtilisation = cputotalUtilisationTime / clock;
            //calculating the average turn around time by the process by dividng the total turn around time by no of process
            avgTurnAroundTime = totalTurnAroundTime / noOfProcess;
            //calculating the average waiting time by the process by dividng the total waiting time  by no of process
            avgWaitingTime = totalWaitingTime / noOfProcess;

            //logging the throughput - no of process finsihed per second and other statistics
            System.out.println("Throughput =                " + df.format(noOfProcess / (clock / 1000)) + "/second");
            System.out.println("CPU Utilisation =           " + df.format(cpuUtilisation * 100));
            System.out.println("Average Turnaround Time =   " + df.format(avgTurnAroundTime));
            System.out.println("Average Waiting Time =      " + df.format(avgWaitingTime));

            //logging the gantt chart for the first CPU cycle
            fileOut.println("Gantt Chart for first CPU cycle");
            fileOut.println();
            for (GanttChart gc : ganttCharts) {
                System.out.print("|    " + gc.processName + "(" + df1.format(gc.executionTime) + ")" + " ");
                fileOut.print("|    " + gc.processName + "(" + df1.format(gc.executionTime) + ")" + " ");
            }
            System.out.println();
            fileOut.println();
            for (GanttChart gc : ganttCharts) {
                System.out.print(df1.format(gc.waitingTime) + "          ");
                fileOut.print(df1.format(gc.waitingTime) + "          ");
            }

            fileOut.println();
            fileOut.println();
            writer.newLine();
            writer.newLine();
            writer.newLine();
            writer.newLine();

            //logging the statistics to out file and excel file
            fileOut.println("Throughput =                " + df.format(noOfProcess / (clock / 1000)) + "/second");
            fileOut.println("CPU Utilisation =           " + df.format(cpuUtilisation * 100));
            fileOut.println("Average Turnaround Time =   " + df.format(avgTurnAroundTime));
            fileOut.println("Average Waiting Time =      " + df.format(avgWaitingTime));

            writer.newLine();
            writer.newLine();
            writer.newLine();
            writer.newLine();

            writer.append("Throughput, CPU Utilisation, Average Turnaround Time, Average Waiting Time");
            writer.newLine();
            writer.append(String.valueOf(df.format(noOfProcess / (clock / 1000)) + "/second"));
            writer.append(",");
            writer.append(String.valueOf(df.format(cpuUtilisation * 100)));
            writer.append(",");
            writer.append(String.valueOf(df.format(avgTurnAroundTime)));
            writer.append(",");
            writer.append(String.valueOf(df.format(avgWaitingTime)));
            writer.append(",");
            writer.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
