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

/*
 * Created by JFormDesigner on Fri Feb 14 11:05:52 EST 2014
 */

package org.jlab.coda.afecs.ui.rcgui.util;

import org.jlab.coda.afecs.ui.rcgui.CodaRcGui;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author Vardan Gyurjyan
 */
public class RtvTable extends JFrame {

    private CodaRcGui owner;
    private RtvTableModel _model;

    public RtvTable(CodaRcGui owner, Object[][] data) {
        this.owner = owner;
        _model = new RtvTableModel(data);
        initComponents();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        scrollPane1 = new JScrollPane();
        rtvTable = new JTable();
        button1 = new JButton();
        button2 = new JButton();
        action1 = new CancelAction();
        action2 = new OkAction();

        //======== this ========
        setTitle("CODA RTV Settings");
        Container contentPane = getContentPane();

        //======== scrollPane1 ========
        {

            //---- rtvTable ----
            rtvTable.setModel(_model);
            TableColumnModel cm = rtvTable.getColumnModel();
            cm.getColumn(0).setMinWidth(10);
            cm.getColumn(0).setMaxWidth(200);
            cm.getColumn(0).setPreferredWidth(100);
            scrollPane1.setViewportView(rtvTable);
        }

        //---- button1 ----
        button1.setAction(action2);

        //---- button2 ----
        button2.setAction(action1);

        GroupLayout contentPaneLayout = new GroupLayout(contentPane);
        contentPane.setLayout(contentPaneLayout);
        contentPaneLayout.setHorizontalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(contentPaneLayout.createParallelGroup()
                                        .addComponent(scrollPane1, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 446, Short.MAX_VALUE)
                                        .addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
                                                .addGap(0, 317, Short.MAX_VALUE)
                                                .addComponent(button1)
                                                .addGap(18, 18, 18)
                                                .addComponent(button2)))
                                .addContainerGap())
        );
        contentPaneLayout.setVerticalGroup(
                contentPaneLayout.createParallelGroup()
                        .addGroup(contentPaneLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(scrollPane1, GroupLayout.PREFERRED_SIZE, 491, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(contentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(button2)
                                        .addComponent(button1))
                                .addContainerGap(5, Short.MAX_VALUE))
        );
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    public void update(String rtv, String value) {
        try {
            for (int i = 0; i < rtvTable.getRowCount(); i++) {
                if (rtvTable.getValueAt(i, 0).equals(rtv)) {
                    rtvTable.setValueAt(value, i, 1);
                    break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
//            System.out.println("DDD ArrayIndexOutOfBoundsException " + e.getMessage());
        }

    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner non-commercial license
    private JScrollPane scrollPane1;
    private JTable rtvTable;
    private JButton button1;
    private JButton button2;
    private CancelAction action1;
    private OkAction action2;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private class CancelAction extends AbstractAction {
        private CancelAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "Cancel");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            setVisible(false);
            dispose();
        }
    }

    private class OkAction extends AbstractAction {
        private OkAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "  Ok  ");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            try {
                for (int i = 0; i < rtvTable.getRowCount(); i++) {
                    owner.addRTV((String) rtvTable.getValueAt(i, 0), (String) rtvTable.getValueAt(i, 1));
                }
            } catch (ArrayIndexOutOfBoundsException ee) {
//                System.out.println("DDD ArrayIndexOutOfBoundsException " + ee.getMessage());
            }

            setVisible(false);
            dispose();
        }
    }
}
