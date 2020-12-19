public class Main {
    public static void main(String[] args) {

        /********
         * Min Execution time - 120000 ms
         * Max Execution time - 240000 ms
         * No of process - 10
         * Io Burst - 60
         * mean inter IO interval - 30
         * Scheduling algorithm - FCFS/SJF/RR
         * Quantum - 100
         */
        ProcessSimulation prc = new ProcessSimulation(120000,
                                                      240000,
                                                      10,
                                                       60,
                                                        30,
                                                        "RR",
                                                        100);
        prc.processSimulationProc();

    }
}
