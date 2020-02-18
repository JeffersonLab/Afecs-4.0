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

package test;

import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.system.AException;
import org.jlab.coda.afecs.system.util.AfecsTool;
import org.jlab.coda.afecs.system.util.StdOutput;

import java.util.StringTokenizer;

/**
 * Describe...
 *
 * @author gurjyan
 *         Date: 1/7/14 Time: 11:03 AM
 * @version 2
 */
public class ExternalProcessTest {

    public static void main(String[] args) {

        String host = args[0];
        String command = args[1];

        try {
            StdOutput o = AfecsTool.fork("ssh  "+host+" "+command+" &", false);
            String io = o.getStdio();
            StringTokenizer st = new StringTokenizer(io);
            st.nextToken();
            int pid = AfecsTool.isNumber(st.nextToken());
            for(int i = 0; i<3; i++){
                System.out.println(pid);
                AfecsTool.sleep(1000);
            }
            AfecsTool.fork("ssh  "+host+" kill -9 "+pid, true);

            System.out.println();
        } catch (AException e) {
            if(AConstants.debug.get()) e.printStackTrace();
        }
    }

}
