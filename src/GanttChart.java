//This class is created to save the statistics for Gantt chart
public class GanttChart{
    //to store the process name
    String processName;
    //to store the waiting time for a process
    double waitingTime;
    //to store the turn around time for 10 minute interval
    double executionTime;
    //to store the state of the process finsihed or not finished
    boolean finished;

    public GanttChart(String processName, double waitingTime, double executionTime,boolean finished) {
        this.processName = processName;
        this.waitingTime = waitingTime;
        this.executionTime = executionTime;
        this.finished=finished;

    }

    @Override
    public String toString() {
        return "GanttChart{" +
                "processName='" + processName + '\'' +
                ", waitingTime=" + waitingTime +
                ", executionTime=" + executionTime +
                '}';
    }

}
