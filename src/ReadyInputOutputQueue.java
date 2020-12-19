//This class is created to server the purpose of ready queue and I/O queue
public class ReadyInputOutputQueue implements Comparable<ReadyInputOutputQueue>//, Comparator<ReadyInputOutputQueue>
{
    //to store the process name
    String processName;
    //to store the total execution time of the process
    double completionTime;
    //to store the total remaining time of the process
    double remainingTime;
    //to store the generated CPU burst for a process
    double CPUBurst;


    public ReadyInputOutputQueue(String processName, double completionTime, double remainingTime,double CPUBurst) {
        this.processName = processName;
        this.completionTime = completionTime;
        this.remainingTime = remainingTime;
        this.CPUBurst = CPUBurst;
    }

    @Override
    public String toString() {
        return "processName='" + processName + '\'' +
                ", completionTime=" + completionTime +
                ", remainingTime=" + remainingTime +
                ", CPUBurst=" + CPUBurst;
    }

    // Overriding compareTo method to sort the ready queue based on cpu burst time.
    @Override
    public int compareTo(ReadyInputOutputQueue o) {
        return this.CPUBurst > o.CPUBurst ? 1 : this.CPUBurst < o.CPUBurst ? -1 : 0;
    }

}
