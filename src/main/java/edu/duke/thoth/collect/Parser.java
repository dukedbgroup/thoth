package edu.duke.thoth.collect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by zbd1023 on 11/22/16.
 */
public class Parser {
        File f;
        String content;
        FileOutputStream fop;
        String command;
        HashMap<String, String> map = new HashMap<String, String>();
        Parser(String com, String con){
            command = com;
            map.put("-u", "cpustat");
            map.put("-r", "memstat");
            map.put("-d","iostat");
            if(map.get(command) == null){
                System.out.print("unsupported command, terminating");
                System.exit(1);
            }
            File file = new File("./" + map.get(command) + ".csv");
            content = con;
            try {
                fop = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        public void parse() throws IOException{
            String[] arr = content.split(System.getProperty("line.separator"));
            //write header
            fop.write(makeLine(arr[2], true));
            fop.flush();
            for(int i = 3; i < arr.length; i++){
                if(arr[i].length() < 15 || arr[i].charAt(14) == 'U')
                    continue;
                fop.write(makeLine(arr[i], false));
                fop.flush();
            }
            fop.close();
            System.out.println("File: " + map.get(command) + ".csv " + "written");
        }
        public byte[] makeLine(String line, boolean header){
            String[] cur = line.split(" ");
            StringBuilder sb = new StringBuilder();
            if(header)
                sb.append("time");
            else
                sb.append(cur[0]);
            for(int i = 2; i < cur.length; i++){
                if(!cur[i].equals("")) {
                    sb.append(",");
                    sb.append(cur[i]);
                }
            }
            sb.append(System.getProperty("line.separator"));
            return sb.toString().getBytes();
        }

}
