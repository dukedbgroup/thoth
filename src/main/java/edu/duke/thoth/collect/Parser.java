package edu.duke.thoth.collect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by zbd1023 on 11/22/16.
 */
public class Parser {
        String content;
        FileOutputStream fop;
        ArrayList<StatRow> rows = new ArrayList<StatRow>();
        String header;
        Map<Integer,List<StatRow>> pidMap;

        Parser(String com, String con){
            content = con;
        }
        public void parse() throws IOException {
            String[] arr = content.split(System.getProperty("line.separator"));
            for(String s: arr){
                System.out.println(s);
            }
            header = arr[2];
            for(int i = 3; i < arr.length; i++){
                if(arr[i].length() < 8 || arr[i].charAt(0) == '#')
                    continue;
                rows.add(new StatRow(makeLine(arr[i])));
            }
            parseByPid();
        }

        public void writeRawRowsToFile() throws IOException {
            File file = new File("./raw-pidstat.csv");
            try {
                fop = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //write header
            fop.write(makeLine(header).getBytes());
            fop.flush();
            for(StatRow r: rows) {
                fop.write(r.stat.getBytes());
                fop.flush();
            }
            fop.close();
            System.out.println("File: " + "raw-pidstat"+ ".csv " + "written");
        }

        public void writePidFiles() throws IOException {
            for(Map.Entry<Integer,List<StatRow>> entry: pidMap.entrySet()) {
                List<StatRow> statList = entry.getValue();
                Integer pid = entry.getKey();
                File file = new File("./"+pid+"-pidstat.csv");
                try {
                    fop = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                //write header
                fop.write(makeLine(header).getBytes());
                fop.flush();
                for(StatRow r: statList) {
                    fop.write(r.stat.getBytes());
                    fop.flush();
                }
                fop.close();
                System.out.println("File: " +pid+ "raw-pidstat"+ ".csv " + "written");
            }
        }

        public void parseByPid(){
            if(rows.size()==0) {
                System.err.println("no input data.");
                System.exit(1);
            }
            pidMap = new HashMap<Integer,List<StatRow>>();

            // add to hash map <pid,array[stats]>
            for(StatRow r:rows) {
                if(pidMap.containsKey(r.pid)) {
                    pidMap.get(r.pid).add(r);
                } else {
                    List<StatRow> stats = new ArrayList<StatRow>();
                    stats.add(r);
                    pidMap.put(r.pid,stats);
                }
            }
            insertMissingTimestamps();
        }

        public void insertMissingTimestamps() {
            // last and first time stamp of benchmark
            Long lastTimestamp = rows.get(rows.size()-1).time;
            Long firstTimestamp = rows.get(0).time;
            // insert missing entries for each pid and write file
            for(Map.Entry<Integer,List<StatRow>> entry: pidMap.entrySet()) {
                List<StatRow> rowList = entry.getValue();
                // skip if no missing entries
                if(rowList.size()>=lastTimestamp-firstTimestamp+1) {
                    continue;
                }
                Integer pid = entry.getKey();
                // append entries until last timestamp of the benchmark
                Long pidLastEntryTime = rowList.get(rowList.size()-1).time;
                if(pidLastEntryTime < lastTimestamp) {
                    for(Long i=pidLastEntryTime+1; i<=lastTimestamp;i++) {
                        // copy last row stats
                        StatRow row = new StatRow(rowList.get(rowList.size()-1).stat);
                        // increment time
                        row.setTime(i);
                        rowList.add(row);
                    }
                }
                // skip if no missing entires
                if(rowList.size()>=lastTimestamp-firstTimestamp+1) {
                    continue;
                }
                // append missing entries in the middle
                for(int i=1;i<rowList.size();i++) {
                    Long timeFast = rowList.get(i).time;
                    Long timeSlow = rowList.get(i-1).time;
                    for(Long j=timeFast-1; j>=timeSlow+1; j--) {
                        StatRow nextRow = new StatRow(rowList.get(i-1).stat);
                        nextRow.setTime(j);
                        rowList.add(i,nextRow);
                    }
                }
            }
        }

        public String makeLine(String line){
            String[] cur = line.split("\\s+");
            StringBuilder sb = new StringBuilder();
            sb.append(cur[1]);
            for(int i = 2; i < cur.length; i++){
                if(!cur[i].equals("")) {
                    sb.append(",");
                    sb.append(cur[i]);
                }
            }
            sb.append(System.getProperty("line.separator"));
            return sb.toString();
        }

}
