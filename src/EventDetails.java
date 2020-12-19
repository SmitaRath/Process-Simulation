//this class is created to store the event details
public class EventDetails implements Comparable<EventDetails> {
    //to store the clock time
    double time;
    //to store the process name
    String processName;
    //to store the type of event
    String eventType;

    public EventDetails(double time, String processName, String eventType) {
        this.time = time;
        this.processName = processName;
        this.eventType = eventType;
    }

    @Override
    public String toString() {
        return "EventDetails{" +
                "time=" + time +
                ", processName=" + processName +
                ", eventType='" + eventType + '\'' +
                '}';
    }

    //overriding compareTo method to sort the arrayList based on time.
    @Override
    public int compareTo(EventDetails eventDetailsparam) {

        return this.time > eventDetailsparam.time ? 1 : this.time < eventDetailsparam.time ? -1 : 0;
    }
}
