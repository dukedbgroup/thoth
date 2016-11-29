package edu.duke.thoth;

/**
 * Created by Wilson Zhang on 11/23/16.
 */
public class StatRow {
    Long time;
    int pid;
    String stat;
    StatRow(String s){
        String[] arr = s.split(",");
        time = Long.parseLong(arr[0]);

        pid = Integer.valueOf(arr[1]);
        stat = s;
    }
    public void setTime(Long t) {
        time = t;
        String[] arr = stat.split(",");
        arr[0] = t+"";
        StringBuilder sb = new StringBuilder();
        for(String str:arr) {
            sb.append(","+str);
        }
        stat = sb.toString().substring(1);
    }
}
