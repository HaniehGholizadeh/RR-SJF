import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;

public class Main {

    public static void main(String[] args) throws IOException {
        for (int z = 1; z <= 10; z++) {
            String fi = "input" + z + ".txt";
            File file = new File(fi);
            BufferedReader br = new BufferedReader(new FileReader(file));
            int RR = 2;                 //-rr or sjf
            int timeSlice = 0;          //- used if rr is true
            String str;                 //-read lines
            String[] temp;              //fist line in file
            String method = null;
            if ((str = br.readLine()) != null) {
                if (str.startsWith("RR")) {
                    RR = 0;
                    temp = str.split("\\s+");
                    timeSlice = Integer.parseInt(temp[1]);
                    method = "RR " + timeSlice;
                } else if (str.startsWith("SJF")) {
                    RR = 1;
                    method = "SJF";
                } else if (str.startsWith("PR_no")) {
                    RR = 1;
                    method = "PR_noPREMP";
                } else
                    method = "PR_withPREMP";
            }
            int n = 0;
            if ((str = br.readLine()) != null)
                n = Integer.parseInt(str);
            Task[] tasks = new Task[n];
            int i = 0;
            while ((str = br.readLine()) != null) {
                tasks[i] = new Task(str.split("\\s+"));
                i++;
            }
            Task.sort(tasks);
            LinkedList<Task> queue = new LinkedList<>();
            int time = 0;
            Task t;
            Task.add_to_queue(time, tasks, queue, RR);
            ArrayList<int[]> output;
            output = new ArrayList<>();

            if (RR == 0) {
                int counter;        //0-timeslice value
                while (queue.size() > 0) {
                    t = queue.pop();
                    t.list2.add(time);
                    counter = 0;
                    output.add(new int[]{time, t.id});
                    while (counter < timeSlice && t.cpuBurst > 0) {
                        t.execute();
                        time++;
                        counter++;
                        Task.add_to_queue(time, tasks, queue, 0);
                    }
                    if (t.cpuBurst > 0) {
                        Task.add_based_on_pr(t, queue);
                        t.list1.add(time);
                    }
                }
            } else if (RR == 1) {
                while (queue.size() > 0) {
                    t = queue.pop();
                    t.list2.add(time);
                    output.add(new int[]{time, t.id});
                    while (t.cpuBurst > 0) {
                        t.execute();
                        time++;
                        Task.add_to_queue(time, tasks, queue, 1);
                    }
                }
            } else {
                while (queue.size() > 0) {
                    t = queue.pop();
                    t.list2.add(time);
                    output.add(new int[]{time, t.id});
                    while (t.cpuBurst > 0) {
                        t.execute();
                        time++;
                        Task.add_to_queue(time, tasks, queue, 2);
                        if (queue.size() > 0) {
                            if (queue.getFirst().priority <= t.priority) {
                                break;
                            }
                        }
                    }
                    if (t.cpuBurst > 0) {
                        Task.add_based_on_pr(t, queue);
                        t.list1.add(time);
                    }
                }
            }
            float sum = 0;
            for (Task ts : tasks) {
                for (int j = 0; j < ts.list2.size(); j++) {
                    sum += ts.list2.get(j) - ts.list1.get(j);
                }
            }
            DecimalFormat df = new DecimalFormat("#0.00");
            String avg = df.format((sum / n));
            String fo = "output" + z + ".txt";
            FileWriter myWriter = new FileWriter(fo);
            myWriter.write(method);
            for (i = 0; i < output.size(); i++)
                myWriter.write("\n" + output.get(i)[0] + "\t" + output.get(i)[1]);

            myWriter.write("\nAVG Wating Time: " + avg);
            myWriter.close();
        }
    }

    static class Task {
        int id;
        int arrivalTime;
        int cpuBurst;
        int priority;
        ArrayList<Integer> list1 = new ArrayList<>();
        ArrayList<Integer> list2 = new ArrayList<>();

        public Task(String[] array) {
            this.id = Integer.parseInt(array[0]);
            this.arrivalTime = Integer.parseInt(array[1]);
            this.cpuBurst = Integer.parseInt(array[2]);
            this.priority = Integer.parseInt(array[3]);
            list1.add(arrivalTime);
        }

        public void execute() {
            this.cpuBurst--;
        }

        public static void sort(Task[] arr) {
            int n = arr.length;
            for (int i = 0; i < n - 1; i++) {
                for (int j = 0; j < n - i - 1; j++) {
                    if (arr[j].arrivalTime > arr[j + 1].arrivalTime) {
                        Task temp = arr[j];
                        arr[j] = arr[j + 1];
                        arr[j + 1] = temp;
                    }
                }
            }
        }

        public static void add_to_queue(int time, Task[] t, LinkedList<Task> q, int flag) {
            for (Task task : t) {
                if (task.arrivalTime < time)
                    continue;
                else if (task.arrivalTime == time) {
                    add_based_on_pr(task, q);
                    if (flag != 0)
                        sort_based_on_cpu_br(q);
                } else
                    break;
            }
        }

        public static void add_based_on_pr(Task t, LinkedList<Task> q) {
            if (q.size() == 0)
                q.addFirst(t);
            else if (q.getFirst().priority > t.priority)
                q.addFirst(t);
            else if (q.getLast().priority <= t.priority)
                q.addLast(t);
            else {
                for (int j = 1; j < q.size(); j++) {
                    if (q.get(j).priority > t.priority) {
                        q.add(j, t);
                        break;
                    }
                }
            }
        }

        public static void sort_based_on_cpu_br(LinkedList<Task> q) {
            for (int i = 0; i < q.size() - 1; i++) {
                for (int j = 0; j < q.size() - i - 1; j++) {
                    if (q.get(j).priority == q.get(j + 1).priority) {
                        if (q.get(j).cpuBurst > q.get(j + 1).cpuBurst) {
                            Task temp = q.get(j);
                            q.remove(j);
                            q.add(j + 1, temp);
                        }
                    }
                }
            }
        }
    }
}
