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

import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@Deprecated
public class AListDDialog extends JComponent implements Accessible {
    public JDialog dialog;
    private String InfoTitle;
    private String GuiTitle;

    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;

    /** list of objects used to furnish the list*/
    public String[] myList;

    /** list of objects used to color the list element. 1 = red*/
    public Integer[] myColor;

    /** selected session*/
    private String selectedListElement;
    private javax.swing.JList sessionsList;

    public int status = 0;

    public AListDDialog(){
        super();
    }

    /**
     *
     * @param  parent               gui container
     * @return                      JDialog object
     * @throws HeadlessException    exception thrown in the case environment does not support event
     */
    protected JDialog createDialog(Component parent)
            throws HeadlessException {

        dialog = new JDialog((Frame)parent, GuiTitle, true);
        dialog.setComponentOrientation(this.getComponentOrientation());

        Container contentPane = dialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(this, BorderLayout.CENTER);

        if (JDialog.isDefaultLookAndFeelDecorated()) {
            boolean supportsWindowDecorations =
                    UIManager.getLookAndFeel().getSupportsWindowDecorations();
            if (supportsWindowDecorations) {
                dialog.getRootPane().setWindowDecorationStyle(JRootPane.FILE_CHOOSER_DIALOG);
            }
        }

        initComponents();

        dialog.pack();

        dialog.setLocationRelativeTo(parent);

        return dialog;
    }

    /**
     *
     * @param parent                   gui container
     * @param ss                       array of strings to be shown in the list
     * @param title                    title of the list
     * @param g_title                 title of the dialog gui
     * @param selectedItem             preselect the list element name
     * @throws HeadlessException       exception thrown in the case environment does not support event
     */
    public void showDialog(Component parent,
                           String[] ss,
                           Integer[] cl,
                           String title,
                           String g_title,
                           String selectedItem)
            throws HeadlessException {

        myList = ss;
        myColor = cl;
        InfoTitle = title;
        GuiTitle = g_title;

        int tmpi = myColor[0];
        for(int i=0; i<myColor.length;i++){
            if(myColor[i]==11){
              String tmp = myList[0];
                myList[0] = myList[i];
                myList[i] = tmp;
                myColor[0] = 11;
                myColor[i] = tmpi;
            }
        }

        dialog = createDialog(parent);
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
            }
        });
        if(selectedItem!=null)selectRunType(selectedItem);
        dialog.setVisible(true);

    }


    /** @return the string of the selected session */
    public String getSelectedListElement() {
        return selectedListElement;
    }

    /** select the Jlist element
     * @param runtype to be selected, actually this is the previously selected runtype*/
    public void selectRunType(String runtype) {

        for(int i=0;i<myList.length;i++){
            if(myList[i].equalsIgnoreCase(runtype)) {
                sessionsList.setSelectedIndex(i);
                return;
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
        JButton okButton = new JButton();
        JButton cancelButton = new JButton();

        JComponent jPanel1 = new JPanel();

        JLabel jLabel1 = new JLabel();
        JLabel jLabel2 = new JLabel();
        JScrollPane jScrollPane1 = new JScrollPane();
        sessionsList = new javax.swing.JList();

        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Lucida Bright", 1, 14));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText(InfoTitle);

        jLabel2.setFont(new java.awt.Font("Lucida Bright", 1, 14));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        sessionsList.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
        sessionsList.setCellRenderer(new MyCellRenderer());
        sessionsList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = myList;
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });

        sessionsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        sessionsList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                SessionsListMouseClicked(evt);
            }
        });

        jScrollPane1.setViewportView(sessionsList);

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);

        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup()
                        .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup()
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 140 , GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap())
                        .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(okButton, GroupLayout.PREFERRED_SIZE, 67, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap()
                        .addComponent(cancelButton)
                        .addContainerGap())

                ))
        );

        jPanel1Layout.linkSize(cancelButton, okButton);

        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup()
                        .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap()
                        .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 179, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup()
                                .addComponent(cancelButton)
                                .addComponent(okButton))
                        .addContainerGap(23, Short.MAX_VALUE))
        );



        GroupLayout layout = new GroupLayout(dialog.getContentPane());
        dialog.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup()
                        .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                )
        );


        layout.setVerticalGroup(
                layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                )
        );
    }

    private void SessionsListMouseClicked(MouseEvent evt) {
        selectedListElement = (String)sessionsList.getSelectedValue();
    }

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
        selectedListElement = (String)sessionsList.getSelectedValue();
        if(selectedListElement !=null) {
            status = 1;
            doClose(RET_OK);
        }
        else  {
            status = 0;
            doClose(RET_CANCEL);
        }

    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        status = 0;
        doClose(RET_CANCEL);
    }

    private void closeDialog(java.awt.event.WindowEvent evt) {
        doClose(RET_CANCEL);
    }

    private void doClose(int retStatus) {
        setVisible(false);
        dialog.dispose();
    }


    /**
     *  Inner class for cell rendering, namely coloring list element
     *  red if the myColor int array index is set to 1
     */
    class MyCellRenderer extends JLabel implements ListCellRenderer {

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
            setText(s);
            if(myColor!=null){
                if (myColor[index]>0) {
                    setForeground(Color.red);
                } else{
                    setForeground(Color.black);
                }
            }
            if(isSelected){
                setBackground(Color.yellow);
            } else {
                setBackground(Color.white);
            }
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
            return this;
        }
    }

}
