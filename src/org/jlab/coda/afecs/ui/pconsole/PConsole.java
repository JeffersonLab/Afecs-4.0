
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

package org.jlab.coda.afecs.ui.pconsole;

import org.jlab.coda.afecs.system.ABase;
import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.cMsg.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Random;

/**
 * <p>
 *     Platform console user interface.
 * </p>
 *
 * @author gurjyan
 *         Date: 11/7/14 Time: 2:51 PM
 * @version 4.x
 */
public class PConsole extends JFrame {
    private String platformName;
    private ABase me;
    private cMsgSubscriptionHandle subscriptionObj;

    private String[] agents = {"-"};
    private String[] supervisors = {"-"};
    private String[] sessions = {"-"} ;
    private String[] containers = {"-"};



    public PConsole(String platformUdl, String platformMCUdl, String expid ) {

        platformName = expid;


        initComponents();

        setSize(800,600);

        // connect to the platform
        me = new ABase(platformUdl, platformMCUdl,"Afecs user", expid);
        me.myName = "pCon-"+new Random().nextInt(100);
        if(!me.isPlatformConnected()){

            // Connect to the rc domain multicast
            // server and request platform host name
            cMsgMessage m = me.rcMonitor(5000);
            if(m!=null){
//                String plHost = m.getSenderHost();
                // update platform udl and connect
                cMsgPayloadItem item = m.getPayloadItem("IpAddresses");
                try {
                    String[] plHosts = item.getStringArray();
                    // update platform udl and connect
                    if (plHosts.length >0) {
                        for (String ph:plHosts) {
                            String UIMulticastUDL = me.updateHostUdl(ph, cMsgNetworkConstants.nameServerTcpPort);
                            try {
                                me.myPlatformConnection = me.platformConnect(UIMulticastUDL);
                            } catch (cMsgException e) {
                                System.out.println("Failed to connect to IP address = "+ph);
                                continue;
                            }
                            System.out.println("Connected to IP address = "+ph);
                            break;
                        }
                    }
                } catch (cMsgException e) {
                    e.printStackTrace();
                }

//                if(plHost!=null){
//                    String UIMulticastUDL = me.updateHostUdl(plHost, cMsgNetworkConstants.nameServerTcpPort);
//                    try {
//                        me.myPlatformConnection = me.platformConnect(UIMulticastUDL);
//                    } catch (cMsgException e) {
//                        e.printStackTrace();
//                    }
//                }
                if(!me.isPlatformConnected()){
                    System.out.println("PConsole: Can not connect to the " + me.getPlEXPID()+" platform.");
                    setVisible(false);
                    dispose();
                }
            } else {
                System.out.println("PConsole: Can not find a platform for EXPID = "+ me.getPlEXPID());
                setVisible(false);
                dispose();
            }
        }

//        // if connection is not successful rc multicast to find a platform
//        if(me.isPlatformConnected()){
//
//
//            // Connect to the rc domain multicast
//            // server and request platform host name
//            cMsgMessage m = me.rcMonitor(5000);
//            if(m!=null){
//                String plHost = m.getSenderHost();
//                // update platform udl and connect
//                if(plHost!=null){
//                    String UIMulticastUDL = me.updateHostUdl(plHost, cMsgNetworkConstants.nameServerTcpPort);
//                    try {
//                        me.myPlatformConnection =  me.platformConnect(UIMulticastUDL);
//                    } catch (cMsgException e) {
//                        e.printStackTrace();
//                    }
//                }
//                if(!me.isPlatformConnected()){
//                    JOptionPane.showMessageDialog(this, " Can not connect to the " + me.getPlEXPID() + " platform.");
//                    setVisible(false);
//                    dispose();
//                }
//            } else {
//                JOptionPane.showMessageDialog(this, " Can not find the " + me.getPlEXPID() + " platform.");
//                setVisible(false);
//                dispose();
//            }
//        }

        // subscribe platform console messages
        _subscribe();

    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        menuBar1 = new JMenuBar();
        menu1 = new JMenu();
        menuItem1 = new JMenuItem();
        label1 = new JLabel();
        label2 = new JLabel();
        nameTextField = new JTextField();
        hostTextField = new JTextField();
        startTimeTextField = new JTextField();
        label3 = new JLabel();
        databaseTextField = new JTextField();
        tcpTextField = new JTextField();
        rcUdpTextField = new JTextField();
        udpTextField = new JTextField();
        scrollPane1 = new JScrollPane();
        AgentList = new JList();
        scrollPane2 = new JScrollPane();
        SupervisorList = new JList();
        scrollPane3 = new JScrollPane();
        SessionList = new JList();
        scrollPane4 = new JScrollPane();
        ContainerList = new JList();
        scrollPane5 = new JScrollPane();
        stdioTextArea = new JTextArea();
        label13 = new JLabel();
        label14 = new JLabel();
        label15 = new JLabel();
        label16 = new JLabel();
        label4 = new JLabel();
        action1 = new ExitAction();

        //======== this ========
        setTitle("Platform Inspector");
        Container contentPane = getContentPane();

        //======== menuBar1 ========
        {

            //======== menu1 ========
            {
                menu1.setText("File");

                //---- menuItem1 ----
                menuItem1.setAction(action1);
                menu1.add(menuItem1);
            }
            menuBar1.add(menu1);
        }
        setJMenuBar(menuBar1);

        //---- label1 ----
        label1.setText("Platform Name");
        label1.setForeground(new Color(0, 102, 102));

        //---- label2 ----
        label2.setText("Platform Host");
        label2.setForeground(new Color(0, 102, 102));

        //---- nameTextField ----
        nameTextField.setEditable(false);

        //---- hostTextField ----
        hostTextField.setEditable(false);

        //---- startTimeTextField ----
        startTimeTextField.setEditable(false);

        //---- label3 ----
        label3.setText("Start TIme");
        label3.setForeground(new Color(0, 102, 102));

        //---- databaseTextField ----
        databaseTextField.setEditable(false);

        //---- tcpTextField ----
        tcpTextField.setEditable(false);

        //---- rcUdpTextField ----
        rcUdpTextField.setEditable(false);

        //---- udpTextField ----
        udpTextField.setEditable(false);

        //======== scrollPane1 ========
        {

            //---- AgentList ----
            AgentList.setForeground(new Color(0, 102, 102));
            AgentList.setBorder(new TitledBorder(null, "Agents", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, null, new Color(0, 102, 102)));
            AgentList.setCellRenderer(new ListRender());
            AgentList.setModel(new javax.swing.AbstractListModel() {
            public int getSize() { return agents.length; }
            public Object getElementAt(int i) { return agents[i]; }
            });

            AgentList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            AgentList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
            AgentListMouseClicked(evt);
            }
            });
            scrollPane1.setViewportView(AgentList);
        }

        //======== scrollPane2 ========
        {

            //---- SupervisorList ----
            SupervisorList.setForeground(new Color(0, 102, 102));
            SupervisorList.setBorder(new TitledBorder(null, "Supervisors", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, null, new Color(0, 102, 102)));
            SupervisorList.setCellRenderer(new ListRender());
            SupervisorList.setModel(new javax.swing.AbstractListModel() {
            public int getSize() { return supervisors.length; }
            public Object getElementAt(int i) { return supervisors[i]; }
            });

            SupervisorList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            SupervisorList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
            SupervisorListMouseClicked(evt);
            }
            });
            scrollPane2.setViewportView(SupervisorList);
        }

        //======== scrollPane3 ========
        {

            //---- SessionList ----
            SessionList.setForeground(new Color(0, 102, 102));
            SessionList.setBorder(new TitledBorder(null, "Sessions", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, null, new Color(0, 102, 102)));
            SessionList.setCellRenderer(new ListRender());
            SessionList.setModel(new javax.swing.AbstractListModel() {
            public int getSize() { return sessions.length; }
            public Object getElementAt(int i) { return sessions[i]; }
            });

            SessionList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            SessionList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
            SessionListMouseClicked(evt);
            }
            });
            scrollPane3.setViewportView(SessionList);
        }

        //======== scrollPane4 ========
        {

            //---- ContainerList ----
            ContainerList.setForeground(new Color(0, 102, 102));
            ContainerList.setBorder(new TitledBorder(null, "Containers", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION, null, new Color(0, 102, 102)));
            ContainerList.setCellRenderer(new ListRender());
            ContainerList.setModel(new javax.swing.AbstractListModel() {
            public int getSize() { return containers.length; }
            public Object getElementAt(int i) { return containers[i]; }
            });

            ContainerList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

            scrollPane4.setViewportView(ContainerList);
        }

        //======== scrollPane5 ========
        {

            //---- stdioTextArea ----
            stdioTextArea.setEditable(false);
            scrollPane5.setViewportView(stdioTextArea);
        }

        //---- label13 ----
        label13.setText("Database");
        label13.setForeground(new Color(0, 102, 102));

        //---- label14 ----
        label14.setText("TCP");
        label14.setForeground(new Color(0, 102, 102));

        //---- label15 ----
        label15.setText("UDP");
        label15.setForeground(new Color(0, 102, 102));

        //---- label16 ----
        label16.setText("rcUDP");
        label16.setForeground(new Color(0, 102, 102));

        //---- label4 ----
        label4.setText("Stdio");
        label4.setForeground(new Color(0, 102, 102));

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(contentPaneLayout.createParallelGroup()
                        .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                            .addGroup(contentPaneLayout.createParallelGroup()
                                .addGroup(contentPaneLayout.createSequentialGroup()
                                    .addComponent(label13)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                                    .addComponent(databaseTextField)
                                    .addGap(18, 18, 18)
                                    .addGroup(contentPaneLayout.createParallelGroup()
                                        .addComponent(label14, GroupLayout.PREFERRED_SIZE, 41, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(tcpTextField, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE))
                                    .addGap(18, 18, 18)))
                            .addGroup(contentPaneLayout.createParallelGroup()
                                .addComponent(udpTextField, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE)
                                .addComponent(label15))
                            .addGap(18, 18, 18)
                            .addGroup(contentPaneLayout.createParallelGroup()
                                .addComponent(rcUdpTextField, GroupLayout.PREFERRED_SIZE, 67, GroupLayout.PREFERRED_SIZE)
                                .addComponent(label16)))
                        .addGroup(contentPaneLayout.createSequentialGroup()
                            .addGroup(contentPaneLayout.createParallelGroup()
                                .addComponent(label1, GroupLayout.PREFERRED_SIZE, 118, GroupLayout.PREFERRED_SIZE)
                                .addComponent(nameTextField, GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE))
                            .addGap(18, 18, 18)
                            .addGroup(contentPaneLayout.createParallelGroup()
                                .addComponent(hostTextField, GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE)
                                .addComponent(label2))
                            .addGap(18, 18, 18)
                            .addGroup(contentPaneLayout.createParallelGroup()
                                .addComponent(label3)
                                .addComponent(startTimeTextField, GroupLayout.PREFERRED_SIZE, 182, GroupLayout.PREFERRED_SIZE)))
                        .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                            .addGroup(contentPaneLayout.createParallelGroup()
                                .addGroup(contentPaneLayout.createSequentialGroup()
                                    .addGap(6, 6, 6)
                                    .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                                    .addGap(18, 18, 18)
                                    .addComponent(scrollPane2, GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                                    .addGap(18, 18, 18)
                                    .addComponent(scrollPane3, GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
                                    .addGap(18, 18, 18)
                                    .addComponent(scrollPane4, GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE))
                                .addComponent(scrollPane5, GroupLayout.DEFAULT_SIZE, 599, Short.MAX_VALUE))
                            .addGap(7, 7, 7))
                        .addGroup(contentPaneLayout.createSequentialGroup()
                            .addComponent(label4)
                            .addGap(0, 574, Short.MAX_VALUE)))
                    .addContainerGap())
        );
        contentPaneLayout.setVerticalGroup(
            contentPaneLayout.createParallelGroup()
                .addGroup(contentPaneLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(label1)
                        .addComponent(label2)
                        .addComponent(label3))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(hostTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(startTimeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                        .addComponent(label13, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(label16, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(label15, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(label14, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(rcUdpTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(udpTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(tcpTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(databaseTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGap(38, 38, 38)
                    .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addGroup(contentPaneLayout.createParallelGroup()
                            .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addComponent(scrollPane2, GroupLayout.PREFERRED_SIZE, 152, GroupLayout.PREFERRED_SIZE)
                                .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 151, GroupLayout.PREFERRED_SIZE))
                            .addComponent(scrollPane3, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 152, GroupLayout.PREFERRED_SIZE))
                        .addComponent(scrollPane4, GroupLayout.PREFERRED_SIZE, 152, GroupLayout.PREFERRED_SIZE))
                    .addGap(18, 18, 18)
                    .addComponent(label4)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(scrollPane5, GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                    .addGap(10, 10, 10))
        );
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JMenuBar menuBar1;
    private JMenu menu1;
    private JMenuItem menuItem1;
    private JLabel label1;
    private JLabel label2;
    private JTextField nameTextField;
    private JTextField hostTextField;
    private JTextField startTimeTextField;
    private JLabel label3;
    private JTextField databaseTextField;
    private JTextField tcpTextField;
    private JTextField rcUdpTextField;
    private JTextField udpTextField;
    private JScrollPane scrollPane1;
    private JList AgentList;
    private JScrollPane scrollPane2;
    private JList SupervisorList;
    private JScrollPane scrollPane3;
    private JList SessionList;
    private JScrollPane scrollPane4;
    private JList ContainerList;
    private JScrollPane scrollPane5;
    private JTextArea stdioTextArea;
    private JLabel label13;
    private JLabel label14;
    private JLabel label15;
    private JLabel label16;
    private JLabel label4;
    private ExitAction action1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    /**
     * Subscribes to a platform console messages
     */
    private void _subscribe(){
        _unsubscribe();

//        try {
//            subscriptionObj = me.myPlatformConnection.subscribe(platformName,
//                    AConstants.RcConsoleReport,
//                    new PConCB(), null);
//        } catch (cMsgException e) {
//            if(AConstants.debug.get()) e.printStackTrace();
//        }
    }

    /**
     * Un_subscribes platform console messages
     */
    private void _unsubscribe(){
        try {
            if(me.isPlatformConnected()){
                if(subscriptionObj !=null) me.myPlatformConnection.unsubscribe(subscriptionObj);
            }
        } catch (cMsgException e) {
            if(AConstants.debug.get()) e.printStackTrace();
        }
    }

    /**
     * gracefully exits
     */
    private void localExit(){
        _unsubscribe();
        setVisible(false);
        dispose();
    }

    /**
     * Agent list selection action
     * @param evt mouse event
     */
    private void AgentListMouseClicked(MouseEvent evt) {
        AgentList.getSelectedValue();
    }

    /**
     * Supervisor list selection action
     * @param evt mouse event
     */
    private void SupervisorListMouseClicked(MouseEvent evt) {
        SupervisorList.getSelectedValue();
    }

    /**
     * Session list selection action
     * @param evt mouse event
     */
    private void SessionListMouseClicked(MouseEvent evt) {
        SessionList.getSelectedValue();
    }

    /**
     * <p>
     *    Inner class for list cell rendering.
     *    in case list elements ends with "."
     *    it will draw that element in green.
     * </p>
     */
    class ListRender extends JLabel implements ListCellRenderer {

        // This is the only method defined by ListCellRenderer.
        // We just reconfigure the JLabel each time we're called.
        public Component getListCellRendererComponent(
                JList list,              // the list
                Object value,            // value to display
                int index,               // cell index
                boolean isSelected,      // is the cell selected
                boolean cellHasFocus)    // does the cell have focus
        {
            String s = value.toString();
            if(s.endsWith(".")){
                setForeground(Color.green);
                s = s.substring(0,s.lastIndexOf("."));
            } else if(s.endsWith("_admin")){
                setForeground(Color.blue);
            } else{
                setForeground(Color.black);
            }
            if(isSelected){
                setBackground(Color.yellow);
            } else {
                setBackground(Color.white);
            }
            setText(s);
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
            return this;
        }
    }

    /**
     * <p>
     *      Private inner class for accepting
     *      state messages from containers
     * </p>
     */
    private class PConCB extends cMsgCallbackAdapter {
        public void callback(cMsgMessage msg, Object userObject) {
            if (msg != null) {

                try {

                    if (msg.getText() != null && !msg.getText().equals("")) {
                        stdioTextArea.append(msg.getText() + "\n");
                        stdioTextArea.setCaretPosition(stdioTextArea.getText().length() - 1);
                    }

                    if (msg.getPayloadItem("Platform") != null) {
                        nameTextField.setText(msg.getPayloadItem("Platform").getString());
                    }

                    if (msg.getPayloadItem("HostName") != null) {
                        hostTextField.setText(msg.getPayloadItem("HostName").getString());
                    }

                    if (msg.getPayloadItem("TcpPort") != null) {
                        tcpTextField.setText(Integer.toString(msg.getPayloadItem("TcpPort").getInt()));
                    }

                    if (msg.getPayloadItem("UdpPort") != null) {
                        udpTextField.setText(Integer.toString(msg.getPayloadItem("UdpPort").getInt()));
                    }

                    if (msg.getPayloadItem("RcUdpPort") != null) {
                        rcUdpTextField.setText(Integer.toString(msg.getPayloadItem("RcUdpPort").getInt()));
                    }

                    if (msg.getPayloadItem("Database") != null) {
                        databaseTextField.setText(msg.getPayloadItem("Database").getString());
                    }

                    if (msg.getPayloadItem("StartTime") != null) {
                        startTimeTextField.setText(msg.getPayloadItem("StartTime").getString());
                    }

                    if (msg.getPayloadItem("Containers") != null) {
                        containers = msg.getPayloadItem("Containers").getStringArray();
                        ContainerList.setListData(containers);
                    }

                    if (msg.getPayloadItem("Sessions") != null) {
                        sessions = msg.getPayloadItem("Sessions").getStringArray();
                        SessionList.setListData(sessions);
                    }

                    if (msg.getPayloadItem("Supervisors") != null) {
                        supervisors = msg.getPayloadItem("Supervisors").getStringArray();
                        SupervisorList.setListData(supervisors);
                    }

                    if (msg.getPayloadItem("Agents") != null) {
                        agents = msg.getPayloadItem("Agents").getStringArray();
                        AgentList.setListData(agents);
                    }

                } catch (cMsgException e) {
                    if (AConstants.debug.get()) e.printStackTrace();
                }

            }
        }
    }

    private class ExitAction extends AbstractAction {
        private ExitAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            putValue(NAME, "Exit");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            localExit();
        }
    }
}
