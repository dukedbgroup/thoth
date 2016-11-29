package edu.duke.thoth;

import java.io.*;

/**
 * Created by Wilson Zhang on 11/22/16.
 */
public class PidstatCollector {
    static class myThread extends Thread{
        private String command;
        private int interval;
        private Thread t;

        myThread(String ss, int i){
            command = ss;
            interval = i;
        }

        public void run(){
            try {
                String[] cmd = {"/bin/sh",
                        "-c",
                        "jps | grep CoarseGrainedExecutorBackend | cut -d \" \" -f1"};
                String line = null;
                while(line == null) {
                    ProcessBuilder pb0 = new ProcessBuilder(cmd);
                    Process p = pb0.start();
                    BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    line = br.readLine();
                }

                ProcessBuilder pb = new ProcessBuilder("/usr/bin/pidstat", command, "-h", "-p", line, String.valueOf(interval));
                System.out.println("starting process");
                Process process = pb.start();
                int errCode = process.waitFor();
                System.out.println("starting data collection process");
                String content = output(process.getInputStream());
                Parser parser = new Parser(command, content);
                parser.parse();
                parser.writePidFiles();
                parser.writeRawRowsToFile();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        public void start () {
            if (t == null) {
                t = new Thread (this, command);
                t.start ();
            }
        }
        private static String output(InputStream inputStream)throws IOException{
            StringBuilder sb = new StringBuilder();
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(inputStream));
                String line = null;
                boolean check = false;
                while ((line = br.readLine()) != null) {
                    //todo make parser in a new file, keep old value if nothing changed
                    sb.append(line + System.getProperty("line.separator"));
                }
            } finally {
                br.close();
            }
            return sb.toString();
        }

    }

    public static void main(String[] arg){
        int interval = 1;
        myThread t1 = new myThread("-urd" , interval);
        t1.start();

    }

}

