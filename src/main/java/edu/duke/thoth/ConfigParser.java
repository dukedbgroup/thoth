package edu.duke.thoth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;

/**
 * Created by Wilson Zhang on 11/22/16.
 */
public class ConfigParser {
    ArrayList<String> slaves = new ArrayList<String>();
    String name = "";
    String pathOfPem = "";
     ConfigParser(){
         try {
         File slave = new File("./conf/slaves");
             Scanner scan = new Scanner(slave);
             String s = null;
             if(!scan.hasNextLine()){
                 System.out.print("You need at least 1 slave");
                 System.exit(1);
             }
             while (scan.hasNextLine()){
                 s = scan.nextLine();
                 slaves.add(s);
             }
             File config = new File("./conf/config.xml");
             DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
             Document doc = dBuilder.parse(config);
             doc.getDocumentElement().normalize();
             NodeList nList = doc.getElementsByTagName("property");
             for(int i = 0; i < nList.getLength(); i++){
                Node cur = nList.item(i);
                if(cur.getNodeType() == Node.ELEMENT_NODE){
                    Element e = (Element) cur;
                    String check = e.getElementsByTagName("name").item(0).getTextContent();
                    if(check.equals("thoth.master.slaveName"))
                        name = e.getElementsByTagName("value").item(0).getTextContent();
                    else if(check.equals("thoth.master.pemPath"))
                        pathOfPem = e.getElementsByTagName("value").item(0).getTextContent();
                }
             }
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (ParserConfigurationException e) {
             e.printStackTrace();
         } catch (SAXException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }


     }

}
