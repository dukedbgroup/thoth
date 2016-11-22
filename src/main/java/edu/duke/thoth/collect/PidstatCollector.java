package edu.duke.thoth.collect;

import java.io.*;

/**
 * Created by zbd1023 on 11/22/16.
 */
public class PidstatCollector {
    static class myThread extends Thread{
        private String s;
        private int interval;
        private Thread t;

        myThread(String ss, int i){
            s = ss;
            interval = i;
        }

        public void run(){
            try {
                ProcessBuilder pb = new ProcessBuilder("./bash");
                System.out.println("starting process");
                Process process = pb.start();
                int errCode = process.waitFor();

                File file = new File("./file");
                System.out.println("starting data collection process");
                String content = output(process.getInputStream());
                FileOutputStream fop = new FileOutputStream(file);
                byte[] contentInBytes = content.getBytes();
                fop.write(contentInBytes);
                fop.flush();
                fop.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        public void start () {
            if (t == null) {
                t = new Thread (this, s);
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


//                    if(line.length() > 16)
//                        System.out.println(line.charAt(16));
                    if(line != "" && line.length() > 14 && line.charAt(14) == 'U') {

                        sb.append(line + System.getProperty("line.separator"));
                    }
                    if(line != "" && line.length() > 14 && line.charAt(14) != 'U')
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
        myThread t1 = new myThread("-d", interval);
        t1.start();

    }

}
