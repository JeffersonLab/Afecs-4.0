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
 * Created by JFormDesigner on Fri Oct 14 10:33:28 EDT 2011
 */

package org.jlab.coda.afecs.ui.rcgui.util;

import org.jlab.coda.afecs.system.AConstants;
import org.jlab.coda.afecs.ui.rcgui.CodaRcGui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Vardan Gyurjyan
 */
public class AutoModeForm extends JFrame {
    private CodaRcGui owner;
    public AutoModeForm(CodaRcGui owner) {
        this.owner = owner;
        initComponents();

        // set number of runs on the slider gui
        NRTextField.setText(Integer.toString(this.owner._nScheduledRuns));
        if(this.owner._nScheduledRuns >=NRSlider.getMaximum()){
            NRTextField.setText(Integer.toString(NRSlider.getMaximum()));
            NRSlider.setValue(NRSlider.getMaximum());
        } else {
            NRSlider.setValue(this.owner._nScheduledRuns);
        }

        // set event limit on the slider gui
        ELTextField.setText(Integer.toString(this.owner._eventLimit));
        if(this.owner._eventLimit>=ELSlider.getMaximum()){
            ELTextField.setText(Integer.toString(ELSlider.getMaximum()));
            ELSlider.setValue(ELSlider.getMaximum());
        } else {
            ELSlider.setValue(this.owner._eventLimit);
        }

        // set data limit on the slider gui
        DLTextField.setText(Long.toString(this.owner._dataLimit*4L/1000000L));
        if(this.owner._dataLimit*4L/1000000L >= DLSlider.getMaximum()){
            DLTextField.setText(Long.toString(DLSlider.getMaximum()));
            DLSlider.setValue(DLSlider.getMaximum());
        } else {
            DLSlider.setValue((int)(long)(this.owner._dataLimit*4L/1000000L));
        }

        // set time limit on the slider gui
        int tl = (int)this.owner._timeLimit/60000;
        TLTextField.setText(Integer.toString(tl));
        if(tl>=TLSlider.getMaximum()){
            TLTextField.setText(Integer.toString(TLSlider.getMaximum()));
            TLSlider.setValue(TLSlider.getMaximum());
        } else {
            TLSlider.setValue(tl);
        }

    }


    private void NRSliderStateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        NRTextField.setText(Integer.toString(source.getValue()));
    }

    private void ELSliderStateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        ELTextField.setText(Integer.toString(source.getValue()));
    }

    private void DLSliderStateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        DLTextField.setText(Integer.toString(source.getValue()));
    }

    private void TLSliderStateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        TLTextField.setText(Integer.toString(source.getValue()));
    }


    private void NRTextFieldActionPerformed(ActionEvent e) {
        try {
            int v = Integer.parseInt(NRTextField.getText());
            if(v>=NRSlider.getMaximum()){
                NRTextField.setText(Integer.toString(NRSlider.getMaximum()));
                NRSlider.setValue(NRSlider.getMaximum());
            } else {
                NRSlider.setValue(v);
            }
        } catch (NumberFormatException e1) {
            NRTextField.setText("");
        }
    }

    private void ELTextFieldActionPerformed(ActionEvent e) {
        try {
            int v = Integer.parseInt(ELTextField.getText());
            if(v>=ELSlider.getMaximum()){
                ELSlider.setValue(ELSlider.getMaximum());
            } else {
                ELSlider.setValue(v);

            }
        } catch (NumberFormatException e1) {
            ELTextField.setText("");
        }
    }

    private void DLTextFieldActionPerformed(ActionEvent e) {
        try {
            int v = Integer.parseInt(DLTextField.getText());
            if(v >= DLSlider.getMaximum()){
                DLSlider.setValue(DLSlider.getMaximum());
            } else {
                DLSlider.setValue(v);

            }
        } catch (NumberFormatException e1) {
            DLTextField.setText("");
        }
    }

    private void TLTextFieldActionPerformed(ActionEvent e) {
        try {
            int v = Integer.parseInt(TLTextField.getText());
            if(v>=TLSlider.getMaximum()){
                TLTextField.setText(Integer.toString(TLSlider.getMaximum()));
                TLSlider.setValue(TLSlider.getMaximum());
            } else {
                TLSlider.setValue(v);
            }
        } catch (NumberFormatException e1) {
            TLTextField.setText("");
        }
    }

    private void cancelButtonMouseClicked(MouseEvent e) {
        this.dispose();
    }

    private void startButtonMouseClicked(MouseEvent e) {
        try {
            int nr = Integer.parseInt(NRTextField.getText());
            int el = Integer.parseInt(ELTextField.getText());
            int dl = Integer.parseInt(DLTextField.getText());
            int tl = Integer.parseInt(TLTextField.getText());
            owner.setAutoMode(nr,el,dl,tl);
        } catch (NumberFormatException e1) {
            if(AConstants.debug.get()) e1.printStackTrace();
        }
        this.dispose();
    }

    private void stopButtonMouseClicked(MouseEvent e) {
        owner.setAutoMode(0, 0, 0, 0);
        this.dispose();
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        JPanel dialogPane = new JPanel();
        JPanel contentPanel = new JPanel();
        NRSlider = new JSlider();
        NRTextField = new JTextField();
        ELSlider = new JSlider();
        ELTextField = new JTextField();
        TLSlider = new JSlider();
        TLTextField = new JTextField();
        DLSlider = new JSlider();
        DLTextField = new JTextField();
        JPanel buttonBar = new JPanel();
        JButton startButton = new JButton();
        JButton stopButton = new JButton();
        JButton cancelButton = new JButton();

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setPreferredSize(new Dimension(333, 305));

                //---- NRSlider ----
                NRSlider.setValue(0);
                NRSlider.setPaintLabels(true);
                NRSlider.setPaintTicks(true);
                NRSlider.setMinorTickSpacing(5);
                NRSlider.setMajorTickSpacing(20);
                NRSlider.setBorder(new TitledBorder("Number of Runs"));
                NRSlider.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        NRSliderStateChanged(e);
                    }
                });

                //---- NRTextField ----
                NRTextField.setText("0");
                NRTextField.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        NRTextFieldActionPerformed(e);
                    }
                });

                //---- ELSlider ----
                ELSlider.setValue(0);
                ELSlider.setMinorTickSpacing(1000000);
                ELSlider.setMajorTickSpacing(5000000);
                ELSlider.setBorder(new TitledBorder("Event Limit"));
                ELSlider.setMaximum(20000000);
                ELSlider.setPaintTicks(true);
                ELSlider.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        ELSliderStateChanged(e);
                    }
                });

                //---- ELTextField ----
                ELTextField.setText("0");
                ELTextField.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ELTextFieldActionPerformed(e);
                    }
                });

                //---- TLSlider ----
                TLSlider.setValue(0);
                TLSlider.setPaintLabels(true);
                TLSlider.setPaintTicks(true);
                TLSlider.setMinorTickSpacing(100);
                TLSlider.setMajorTickSpacing(200);
                TLSlider.setBorder(new TitledBorder("Time Limit (min.)"));
                TLSlider.setMaximum(600);
                TLSlider.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        TLSliderStateChanged(e);
                    }
                });

                //---- TLTextField ----
                TLTextField.setText("0");
                TLTextField.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        TLTextFieldActionPerformed(e);
                    }
                });

                //---- DLSlider ----
                DLSlider.setValue(0);
                DLSlider.setMinorTickSpacing(500);
                DLSlider.setMajorTickSpacing(1000);
                DLSlider.setBorder(new TitledBorder("Data Limit (MByte)"));
                DLSlider.setMaximum(10000);
                DLSlider.setPaintTicks(true);
                DLSlider.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        DLSliderStateChanged(e);
                    }
                });

                //---- DLTextField ----
                DLTextField.setText("0");
                DLTextField.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ELTextFieldActionPerformed(e);
                        DLTextFieldActionPerformed(e);
                    }
                });

                GroupLayout contentPanelLayout = new GroupLayout(contentPanel);
                contentPanel.setLayout(contentPanelLayout);
                contentPanelLayout.setHorizontalGroup(
                    contentPanelLayout.createParallelGroup()
                        .addGroup(contentPanelLayout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(contentPanelLayout.createParallelGroup()
                                .addGroup(contentPanelLayout.createSequentialGroup()
                                    .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(NRSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(ELSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .addGap(18, 18, 18)
                                    .addGroup(contentPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(ELTextField, GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)
                                        .addComponent(NRTextField, GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)))
                                .addGroup(contentPanelLayout.createSequentialGroup()
                                    .addComponent(TLSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(TLTextField, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE))
                                .addGroup(contentPanelLayout.createSequentialGroup()
                                    .addComponent(DLSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(DLTextField, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)))
                            .addContainerGap(38, Short.MAX_VALUE))
                );
                contentPanelLayout.setVerticalGroup(
                    contentPanelLayout.createParallelGroup()
                        .addGroup(contentPanelLayout.createSequentialGroup()
                            .addGroup(contentPanelLayout.createParallelGroup()
                                .addGroup(contentPanelLayout.createSequentialGroup()
                                    .addContainerGap()
                                    .addComponent(NRSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGroup(contentPanelLayout.createSequentialGroup()
                                    .addGap(15, 15, 15)
                                    .addComponent(NRTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(contentPanelLayout.createParallelGroup()
                                .addGroup(contentPanelLayout.createSequentialGroup()
                                    .addGap(9, 9, 9)
                                    .addComponent(ELTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addComponent(ELSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addGroup(contentPanelLayout.createParallelGroup()
                                .addGroup(contentPanelLayout.createSequentialGroup()
                                    .addGap(18, 18, 18)
                                    .addComponent(DLSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGroup(contentPanelLayout.createSequentialGroup()
                                    .addGap(26, 26, 26)
                                    .addComponent(DLTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                            .addGroup(contentPanelLayout.createParallelGroup()
                                .addGroup(contentPanelLayout.createSequentialGroup()
                                    .addGap(18, 18, 18)
                                    .addComponent(TLSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGroup(contentPanelLayout.createSequentialGroup()
                                    .addGap(27, 27, 27)
                                    .addComponent(TLTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                            .addContainerGap(22, Short.MAX_VALUE))
                );
            }
            dialogPane.add(contentPanel, BorderLayout.WEST);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout) buttonBar.getLayout()).columnWidths = new int[] {0, 85, 0, 80};
                ((GridBagLayout) buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 0.0};

                //---- startButton ----
                startButton.setText("Set");
                startButton.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        startButtonMouseClicked(e);
                    }
                });
                buttonBar.add(startButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- stopButton ----
                stopButton.setText("Reset");
                stopButton.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        stopButtonMouseClicked(e);
                    }
                });
                buttonBar.add(stopButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- cancelButton ----
                cancelButton.setText("Cancel");
                cancelButton.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        cancelButtonMouseClicked(e);
                    }
                });
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

    private JSlider NRSlider;
    private JTextField NRTextField;
    private JSlider ELSlider;
    private JTextField ELTextField;
    private JSlider TLSlider;
    private JTextField TLTextField;
    private JSlider DLSlider;
    private JTextField DLTextField;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

}
