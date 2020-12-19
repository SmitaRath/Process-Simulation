//This class will serve the purpose for process control block
public class ProcessControlBlock {
    //it will store the name of the process
    String processName;
    //for storing the state of the process - ready, running, waiting, terminated;
    String state;
    //for storing the total execution time;
    double executionTime;
    //for storing the remaining execution time;
    double remainingTime;

    public ProcessControlBlock(String processName,double executionTime) {
        this.processName = processName;
        this.state = "";
        this.executionTime=executionTime;
        this.remainingTime=executionTime;
    }

    //overriding toString method to display output
    @Override
    public String toString() {
        return "ProcessControlBlock{" +
                "processNo=" + processName +
                ", state='" + state  +
                ", executionTime=" + executionTime +
                ", remainingTime=" + remainingTime +
                '}';
    }
}

