/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package com.phoeniix.JWebMonitor;

import com.google.appengine.repackaged.com.google.common.io.CharStreams;
import com.phoeniix.mailer.Mailer;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

/**
 *
 * @author phoeniix
 */
public class JWebMonitor extends Application {

    private static Locale locale;;
    private static ResourceBundle messages;
            
    static ArrayList sitelist = new ArrayList();
    static String emailmethod = new String();
    static String emailfrom = new String();
    static ArrayList emailto = new ArrayList();
    static ArrayList emailcc = new ArrayList();
    static String emailsubject = new String();
    static String emailmessage = new String();
    static String emailserver = new String();
    static String emailsmtpport = new String();
    static String emailsmtpssl = new String();
    static String emailsmtpauth = new String();
    static String emailuser = new String();
    static String emailpw = new String();
    
    private static Button btn;
    private static Text text;
    private static StringProperty output = new SimpleStringProperty();
//    private static TextField textField;

        
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        output.set("Log");
                
        output.set(output.get() + "\n" + "" +
            "=====================================================\n" +
            "=== Java Web Monitor (JWebMonitor)\n" +
            "=== Copyright (C) 2015 - Marc Schroeder\n" +
            "=== http://jwebmonitor.phoeniix.com/\n" +
            "====================================================="
        );
                  
        // setup the language support
        locale = Locale.ENGLISH;
        messages = ResourceBundle.getBundle("MessagesBundle", locale);
        

        // Get values from the config file
        try {
            File file = new File("config.xml");
            
            DocumentBuilder builder = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder();
            Document doc = builder.parse(file);

            // site info
            output.set(output.get() + "\n" + "\n[Adding Sites...]");
            sitelist = createArray(doc, "data", "site");                    // get list of websites

            // Type of notification
            output.set(output.get() + "\n" + "\n[Adding email data...]");
            emailmethod = createString(doc, "data", "emailmethod");
            emailsmtpauth = createString(doc, "data", "emailsmtpauth");

            // email info
            emailfrom = createString(doc, "data", "emailfrom");                // Get emailfrom addresses
            emailto = createArray(doc, "data", "emailto");                // Get emailto addresses
            emailcc = createArray(doc, "data", "emailcc");                // Get emailcc addresses
            emailsubject = createString(doc, "data", "emailsubject");            // Get emailsubject addresses
            emailmessage = createString(doc, "data", "emailmsg");                // Get emailmsg addresses

            // email server info
            if (emailsmtpauth.equals("false")) {
                emailserver = createString(doc, "data", "emailsmtpserver");        // Get sendmail server
            } else {
                emailserver = createString(doc, "data", "emailsmtpserver");        // Get smtp server
                emailuser = createString(doc, "data", "emailsmtpuser");            // Get stmp user
                emailpw = createString(doc, "data", "emailsmtppw");            // Get smtp pw
            }

            emailsmtpport = createString(doc, "data", "emailsmtpport");            // Get smtp port
            emailsmtpssl = createString(doc, "data", "emailsmtpssl");            // Get smtp use ssl?
        }
        catch (Exception e) {
            output.set(output.get() + "\n" + messages.getString("cantFindConfig"));
            return;
        }
        
        
        // Launch the application
        Application.launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("JWebMonitor");
                
        Group root = new Group();
        
        btn = new Button();
        btn.setLayoutX(0);
        btn.setLayoutY(0);
        btn.setText("Start Scanning...");
        btn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                btn.setText("Scanning...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(JWebMonitor.class.getName()).log(Level.SEVERE, null, ex);
                }
                startScanning();
            }
        });
        root.getChildren().add(btn);
        
//        Group g = new Group(gg -> {
//            gg.getChildren().addAll(new Button(b -> {
//                b.setText("Hello");
//            });
//        });
        
	text = TextBuilder
            .create()
//            .text("Log")
            .font(new Font(10))
            .stroke(Color.BLACK)
            .fill(Color.WHEAT)
            .font(Font.font("Ubuntu", 41))
//            .effect(new Glo())
            .build();
        text.setLayoutX(0);
        text.setLayoutY(60);
//        TextField textField = neww TextField();
//        textField.setText("Neon Sign");
        text.textProperty().bind(output);
        root.getChildren().add(text);
        
        Scene scene = new Scene(root, 1024, 768);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
        
        output.set("hey");
    }
    
    

    public void startScanning(){
        // Quit app if no websites listed
        if (sitelist.isEmpty()) {
            output.set(output.get() + "\n" + messages.getString("addWebsites"));
            btn.setText(messages.getString("startScanning"));
            return;
        }

                        
        // connect to website and if problem (exception) then run notifier else do nothing
        output.set(output.get() + "\n" + messages.getString("testingWebsites"));
        for (int i = 0; i < sitelist.size(); i++) {
            try {
                URL url = new URL(sitelist.get(i).toString());

                output.set(output.get() + "\n" + "*** Testing " + url);
                long begin = System.currentTimeMillis();            // Record start time
                URLConnection uc = url.openConnection();            // Open connection to website
                uc.setConnectTimeout(5000);
                uc.setReadTimeout(10000);
                InputStream ic = uc.getInputStream();                // Get input stream to try and cause exception
                String response = CharStreams.toString(new InputStreamReader(ic));

                long end = System.currentTimeMillis();                // Record end time
                float timeToLoad = (((float) (end - begin)) / 1000);    // Convert to floating number
                output.set(output.get() + "\n" + ">>> " + url + " is responding! Time: " + timeToLoad + " seconds.\n");

//                output.set(output.get() + "\n" + "Response: " + response);
                if (!response.contains("<title>"))
                    throw new Exception("Expected string not found.");
            }
            catch (Exception e) {
                String emailmsg = emailmessage + " " + sitelist.get(i) + "\n\nDetected Problem:\n" + e;
                String theError = "" + e;

                Mailer.sendMessage(emailmethod, emailsmtpauth, emailserver, emailsmtpport, emailsmtpssl, emailuser, emailpw, emailfrom, emailto, emailcc, emailsubject, emailmsg, theError);
            }
        }
        
        // Reset the button text
        btn.setText(messages.getString("startScanning"));
    }



    // return the string of the requested child

    public static String createString(Document doc, String roottag, String childsection) {
        String thestring = new String();
        NodeList nodes = doc.getElementsByTagName(roottag);

        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);

            // Process the lines
            NodeList lines = element.getElementsByTagName(childsection);

            Element line = (Element) lines.item(0);

            // Collect the text from the <Line> element
            StringBuffer sb = new StringBuffer();
            Node child = line.getFirstChild();
            if (child instanceof CharacterData && child != null) {
                CharacterData cd = (CharacterData) child;
                sb.append(cd.getData());
            }

            String text = sb.toString().trim();
            thestring = text;
            output.set(output.get() + "\n" + ">>> Added " + childsection + ": " + text);
        }
        return thestring;
    }// End createString method


    // return an array of similar child nodes

    public static ArrayList createArray(Document doc, String roottag, String childsection) {
        ArrayList thearray = new ArrayList();
        NodeList nodes = doc.getElementsByTagName(roottag);

        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);

            // Process the lines
            NodeList lines = element.getElementsByTagName(childsection);

            for (int j = 0; j < lines.getLength(); j++) {
                Element line = (Element) lines.item(j);

                // Collect the text from the <Line> element
                StringBuffer sb = new StringBuffer();
                for (Node child = line.getFirstChild(); child != null; child = child.getNextSibling()) {
                    if (child instanceof CharacterData) {
                        CharacterData cd = (CharacterData) child;
                        sb.append(cd.getData());
                    }
                }

                String text = sb.toString().trim();
                thearray.add(text);
                output.set(output.get() + "\n" + ">>> Added " + childsection + ": " + text);
            }
        }
        return thearray;
    }// End createArray method
}
