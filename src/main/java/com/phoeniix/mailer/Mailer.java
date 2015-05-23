/* Java Website Monitor (JWebMonitor) v0.9.1
 * The Java Website Monitor is a java program that tries to detect if a website 
 * is up and if not send an SMTP email to a list of valid email addresses.  It 
 * can be run with or without SMTP authentication and with or without SSL 
 * provided that the proper site certificate is installed in the keystore. 
 * 
 * Copyright (C) 2004  Marc Schroeder
 * http://forum.phoeniix.com/
 * 
 * Requires:
 * /lib/mail.jar				// JavaMail API
 * /lib/activation.jar                  	// Java Activation Framework
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.phoeniix.mailer;


/**
 * Imports
 */

import java.util.*;
import javax.mail.*;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.*;
import java.security.Security;


/**
 * @author phoeniix
 */
public class Mailer {

    /**
     * Creates a new instance of Mailer
     */
    public Mailer() {
    }

    // Send an alert to the recipient

    public static void sendMessage(String emailmethod, String emailsmtpauth, String servername,
                                   String emailsmtpport, String emailsmtpssl, final String username,
                                   final String userpw, String fromaddress, ArrayList toaddress,
                                   ArrayList ccaddress, String subject, String themessage,
                                   String theerror) {
        try {
            // Set the properties
            Properties properties = new Properties();
            properties.put("mail.smtp.host", servername);
            properties.put("mail.smtp.port", emailsmtpport);
            if (emailsmtpssl.equals("true")) {
                properties.put("mail.smtp.ssl", emailsmtpssl);
                properties.put("mail.smtp.socketFactory.port", emailsmtpport);
                properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                properties.put("mail.smtp.socketFactory.fallback", "false");
            }

            Session session;

            if (emailsmtpauth.equals("true")) {
                properties.put("mail.smtp.auth", "true");

                // Create an SMTP Authenticator class
                final class SMTPAuthenticator extends javax.mail.Authenticator {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, userpw);
                    }
                }

                // Create session with authentication
                session = Session.getDefaultInstance(properties, new SMTPAuthenticator());
            } else {
                // Create session without authentication
                session = Session.getDefaultInstance(properties, null);
            }

            // Create the message object
            MimeMessage message = new MimeMessage(session);

            String encoding = "iso-8859-1";                                //Set Encoding

            // Create the message
            String encodedText = MimeUtility.encodeText(themessage, encoding, null);
            message.setText(encodedText);

            // Create the subject
            String encodedSubject = MimeUtility.encodeText(subject, encoding, null);
            message.setSubject(encodedSubject);

            // Create the from addresses
            String encodednamefrom = MimeUtility.encodeText(fromaddress, encoding, null);
            Address from = new InternetAddress(fromaddress, encodednamefrom);
            message.setFrom(from);

            // Create the to addresses
            int sizeTo = toaddress.size();
            InternetAddress[] to = new InternetAddress[sizeTo];
            for (int i = 0; i < sizeTo; i++) {
                String encodednameto = MimeUtility.encodeText(toaddress.get(i).toString(), encoding, null);
                to[i] = new InternetAddress(toaddress.get(i).toString(), encodednameto);
            }
            message.addRecipients(Message.RecipientType.TO, to);

            // build list of cc's
            int sizeCc = ccaddress.size();
            InternetAddress[] cc = new InternetAddress[sizeCc];
            for (int i = 0; i < sizeCc; i++) {
                String encodednamecc = MimeUtility.encodeText(ccaddress.get(i).toString(), encoding, null);
                cc[i] = new InternetAddress(ccaddress.get(i).toString(), encodednamecc);
            }
            message.addRecipients(Message.RecipientType.CC, cc);


            // Send alert
            System.out.println("!!! Website Down - " + theerror + " is not responding.");
            System.out.println(">>> Sending alerts to: " + toaddress + " " + ccaddress);

            Transport tr = session.getTransport("smtp");
            tr.connect(servername, username, userpw);
            message.saveChanges(); // don't forget this
            tr.sendMessage(message, message.getAllRecipients());
            tr.close();
            System.out.println("*** Message Successfully Sent.\n");
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("!!! Message Failed to Send\n");
        }
        finally {
            //tr.close();
        }

    }

}
