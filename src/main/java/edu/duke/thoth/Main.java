package edu.duke.thoth;
import com.jcraft.jsch.*;

import java.io.IOException;

/**
 * Created by zbd1023 on 11/22/16.
 */
public class Main {
    private static void runCommand(String nodes[], String command){
        try {
            JSch jsch = new JSch();

            jsch.setConfig("StrictHostKeyChecking", "no");
            jsch.addIdentity("/Users/zbd1023/.ssh/EC2.pem");
            for (String s : nodes) {
                Session session = jsch.getSession("ubuntu", s, 22);
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
        String[] nodes = {"ec2-35-164-116-64.us-west-2.compute.amazonaws.com"};
        runCommand(nodes, "java collector");
        System.out.println(System.currentTimeMillis());
        //START SPARK JOB
//        File file = new File("bash");
        ProcessBuilder pb = new ProcessBuilder("./bash");
        Process process = pb.start();
        int errCode = process.waitFor();
        runCommand(nodes, "killall pidstat");
    }


}
