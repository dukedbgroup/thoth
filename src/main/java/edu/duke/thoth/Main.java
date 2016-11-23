package edu.duke.thoth;
import com.jcraft.jsch.*;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Wilson Zhang on 11/22/16.
 */
public class Main {
    private static void runCommand(ArrayList<String> nodes, String command, String name, String path){
        try {
            JSch jsch = new JSch();

            jsch.setConfig("StrictHostKeyChecking", "no");

            jsch.addIdentity(path);
            for (String s : nodes) {
                Session session = jsch.getSession(name, s, 22);
                session.connect();
                Channel channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand(command);
                channel.connect();
                channel.disconnect();
                session.disconnect();
            }
        }catch (JSchException e){
            System.out.print(e);
        }
    }

    public static void main(String[] arg) throws IOException,InterruptedException {
        ConfigParser parser = new ConfigParser();
        runCommand(parser.slaves, "java PidstatCollector", parser.name, parser.pathOfPem);
        ProcessBuilder pb = new ProcessBuilder("/Users/zbd1023/Applications/spark-2.0.1-bin-hadoop2.6/bin/spark-submit", "run-example", "SparkTC");
        Process process = pb.start();
        int errCode = process.waitFor();
        runCommand(parser.slaves, "killall pidstat",parser.name, parser.pathOfPem);
    }


}
