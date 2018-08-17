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

package org.jlab.coda.afecs.ui.rcgui.util;

import org.jlab.coda.afecs.ui.rcgui.CodaRcGui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * <p>
 *       Runs user application definition UI
 * </p>
 *
 * @author gurjyan
 *         Date: 11/21/14 Time: 1:51 PM
 * @version 2
 */

public class UserAppDefGui extends JFrame {
    private CodaRcGui owner;
    private CUserApp nap;

    public UserAppDefGui(CodaRcGui owner, CUserApp app) {
        initComponents();
        this.owner = owner;
        nap = app;
        if(nap!=null){
            deleteButton.setEnabled(true);
            nameTextField.setText(nap.getName());
            StringBuilder sb = new StringBuilder();
            for(String s:nap.getCommand()){
                sb.append(s).append(" ");
            }
            pathTextField.setText(sb.toString().trim());
            descriptionTextArea.setText(nap.getDescription());
        } else {
            nap = new CUserApp();
            deleteButton.setEnabled(false);
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        label1 = new JLabel();
        nameTextField = new JTextField();
        label2 = new JLabel();
        pathTextField = new JTextField();
        label3 = new JLabel();
        scrollPane1 = new JScrollPane();
        descriptionTextArea = new JTextArea();
        buttonBar = new JPanel();
        okButton = new JButton();
        deleteButton = new JButton();
        cancelButton = new JButton();
        action1 = new OkAction();
        action2 = new CancelAction();
        action3 = new DeleteAction();

        //======== this ========
        setTitle("User Application");
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {

                //---- label1 ----
                label1.setText("Name");

                //---- label2 ----
                label2.setText("Command");

                //---- label3 ----
                label3.setText("Description");

                //======== scrollPane1 ========
                {
                    scrollPane1.setViewportView(descriptionTextArea);
                }

                GroupLayout contentPanelLayout = new GroupLayout(contentPanel);
                contentPanel.setLayout(contentPanelLayout);
                contentPanelLayout.setHorizontalGroup(
                    contentPanelLayout.createParallelGroup()
                        .addGroup(contentPanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(contentPanelLayout.createParallelGroup()
                                .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                                .addComponent(label3)
                                .addGroup(contentPanelLayout.createSequentialGroup()
                                    .addGroup(contentPanelLayout.createParallelGroup()
                                        .addComponent(label2)
                                        .addComponent(label1))
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addGroup(contentPanelLayout.createParallelGroup()
                                        .addComponent(nameTextField, GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
                                        .addComponent(pathTextField, GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE))))
                            .addContainerGap())
                );
                contentPanelLayout.setVerticalGroup(
                    contentPanelLayout.createParallelGroup()
                        .addGroup(contentPanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(label1)
                                .addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addGap(18, 18, 18)
                            .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(label2)
                                .addComponent(pathTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addGap(18, 18, 18)
                            .addComponent(label3)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                            .addContainerGap())
                );
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 85, 0, 80};
                ((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 0.0};

                //---- okButton ----
                okButton.setAction(action1);
                buttonBar.add(okButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- deleteButton ----
                deleteButton.setAction(action3);
                buttonBar.add(deleteButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- cancelButton ----
                cancelButton.setAction(action2);
                buttonBar.add(cancelButton, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JLabel label1;
    private JTextField nameTextField;
    private JLabel label2;
    private JTextField pathTextField;
    private JLabel label3;
    private JScrollPane scrollPane1;
    private JTextArea descriptionTextArea;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton deleteButton;
    private JButton cancelButton;
    private OkAction action1;
    private CancelAction action2;
    private DeleteAction action3;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private class OkAction extends AbstractAction {
        private OkAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "OK");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            boolean exist = false;
            nap.setName(nameTextField.getText());
            StringTokenizer stk = new StringTokenizer(pathTextField.getText().trim());
            ArrayList<String> cml = new ArrayList<>();
            while(stk.hasMoreTokens()){
                cml.add(stk.nextToken());
            }
            nap.setCommand(cml);
            nap.setDescription(descriptionTextArea.getText());
            for(CUserApp tmp:owner.getUserAppManager().getUserApps()){
                if(tmp.getName().equals(nap.getName())) {
                    exist = true;
                    break;
                }
            }
            if(exist){
                owner.getUserAppManager().removeApp(nap.getName());
                owner.getUserAppManager().addApp(nap);
                owner.getUserAppManager().writeFile();
            } else {
                owner.getUserAppManager().addApp(nap);
                owner.getUserAppManager().writeFile();
                owner.addNewUserAppMenuItem(nap);
                owner.addNewUserAppEditMenuItem(nap);

            }
            dispose();
        }
    }

    private class CancelAction extends AbstractAction {
        private CancelAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Cancel");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    }

    private class DeleteAction extends AbstractAction {
        private DeleteAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Delete");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            String message = "  Are you sure to remove defined external process?";
            String[] buttons = {"Yes", "Cancel"};
            int answ = owner.popupQuestionDialog(message, buttons);
            if( answ == JOptionPane.YES_OPTION){

                // remove defined application
                owner.getUserAppManager().removeApp(nameTextField.getText());
                owner.getUserAppManager().writeFile();

                // recreate menu
                owner.recreateUserAppMenu();
            }
            dispose();
        }
    }
}
