/*
 *   Copyright (c) 2017.  Jefferson Lab (JLab). All rights reserved. Permission
 *   to use, copy, modify, and distribute  this software and its documentation for
 *   governmental use, educational, research, and not-for-profit purposes, without
 *   fee and without a signed licensing agreement.
 *
 *   IN NO EVENT SHALL JLAB BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL
 *   INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING
 *   OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF JLAB HAS
 *   BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *   JLAB SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *   THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *   PURPOSE. THE CLARA SOFTWARE AND ACCOMPANYING DOCUMENTATION, IF ANY,
 *   PROVIDED HEREUNDER IS PROVIDED "AS IS". JLAB HAS NO OBLIGATION TO PROVIDE
 *   MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 *   This software was developed under the United States Government license.
 *   For more information contact author at gurjyan@jlab.org
 *   Department of Experimental Nuclear Physics, Jefferson Lab.
 */

package org.jlab.coda.afecs.plugin.ssh2;

import com.jcraft.jsch.*;
import org.jlab.coda.afecs.system.AConstants;

import org.jlab.coda.afecs.system.util.AfecsTool;

import java.io.*;


/**
 * This is a class that launches SSH2 processes.
 *
 * @author gurjyan
 *         Date: 12/9/13 Time: 11:47 AM
 * @version 3
 */
public class JSSH {


    private String  command;
    private Session session;

    public static void main(String[] args) {
        try {
            new JSSH(args[0], args[1], args[2]).execute();
        } catch (IOException e) {
            if(AConstants.debug.get()) e.printStackTrace();
        } catch (JSchException e) {
            if(AConstants.debug.get()) e.printStackTrace();
        }
    }

    /**
     * Constructor of this class
     * @param host remote host name
     * @param user user name
     * @param command command to be executed on a remote host
     */
    public JSSH(String host, String user, String command){
        JSch jsch=new JSch();
        try {
            session=jsch.getSession(user, host, 22);

            // username and password will be given via UserInfo interface.
            UserInfo ui=new JSSHInfo();
            session.setUserInfo(ui);
            session.connect();
        } catch (JSchException e) {
            e.printStackTrace();
        }
        this.command = command;
    }

    /**
     * The actual scp transfer.
     * @throws IOException on i/o errors
     * @throws JSchException on errors detected by scp
     */
    public void execute() throws IOException, JSchException {
        Channel channel = openExecChannel(command);
        try {
            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            BufferedReader r = new BufferedReader(new InputStreamReader(channel.getInputStream()),1);
            String line;
            while((line = r.readLine())!=null){
                System.out.println(line);
            }
            sendAck(out);
        } finally {
            if (channel != null) {
                channel.disconnect();
                session.disconnect();
            }
        }
        System.out.println("done\n");
    }




    /**
     * Open an ssh channel.
     * @param command the command to use
     * @return the channel
     * @throws JSchException on error
     */
    protected Channel openExecChannel(String command) throws JSchException {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);

        return channel;
    }

    /**
     * Send an acknowledgment.
     * @param out the output stream to use
     * @throws IOException on error
     */
    protected void sendAck(OutputStream out) throws IOException {
        byte[] buf = new byte[1];
        buf[0] = 0;
        out.write(buf);
        out.flush();
    }

}

