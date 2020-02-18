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
import java.awt.*;
import java.awt.event.*;


/**
 * <p>
 *     Slider dialog
 * </p>
 */
public class ASliderDialog extends JComponent
        implements Accessible {

    public JDialog dialog;
    private String InfoTitle;
    private String GuiTitle;
    private int SliderValue;

    // A return status code if Cancel
    // button has been pressed
    public static final int RET_CANCEL = 0;

    // A return status code if OK
    // button has been pressed
    public static final int RET_OK = 1;

    // selected session
    private int RepInterval;
    public AScrollSlider imSlider;
    private int returnStatus = RET_CANCEL;
    private JLabel sliderCurrentValue = new JLabel();
    // max value of the slider
    private int maxValue;

    public int status = 0;

    public ASliderDialog(int maxValue){
        super();
        this.maxValue = maxValue;
    }

    /**
     * <p>
     *     Creates a dialog
     * </p>
     * @param  parent gui container
     * @return  JDialog object
     * @throws HeadlessException
     */
    protected JDialog createDialog(Component parent) throws HeadlessException {

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
     * <p>
     *     Show the dialog
     * </p>
     * @param parent gui container
     * @param title title of the list
     * @param aiv initial value of the slider
     * @param g_title title of the dialog gui
     */
    public void showDialog(Component parent,
                           int aiv,
                           String title,
                           String g_title)
            throws HeadlessException {

        SliderValue = aiv;
        InfoTitle = title;
        GuiTitle = g_title;
        dialog = createDialog(parent);
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                returnStatus = RET_CANCEL;
            }
        });
        dialog.setVisible(true);
    }

    private void initComponents() {
        JButton okButton = new JButton();
        JButton cancelButton = new JButton();
        JComponent jPanel1 = new JPanel();

        JLabel jLabel1 = new JLabel();

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

        sliderCurrentValue.setFont(new java.awt.Font("Lucida Bright", 1, 12));
        sliderCurrentValue.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        sliderCurrentValue.setText(InfoTitle+" = "+SliderValue);

        imSlider = new AScrollSlider(Scrollbar.HORIZONTAL, SliderValue, 1, 0, maxValue, maxValue);
        imSlider.addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent ae) {
                RepInterval =  imSlider.getValue();
                sliderCurrentValue.setText(InfoTitle+" = " + RepInterval);

            }
        });



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
                                .addContainerGap())
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(imSlider, GroupLayout.PREFERRED_SIZE, 160 , GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(sliderCurrentValue, GroupLayout.PREFERRED_SIZE, 140, GroupLayout.PREFERRED_SIZE))
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
                        .addContainerGap()
                        .addComponent(imSlider)
                        .addComponent(sliderCurrentValue, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
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


    /**
     * @return the return status of this dialog
     *         one of RET_OK or RET_CANCEL
     * */
    public int getReturnStatus() {
        return returnStatus;
    }

    /**
     * @return the float value of the slider.
     *         N.B. selected values of the slider
     *         are divided by 10 */
    public String getSliderValue() {
        if (RepInterval==0) RepInterval=1;
        return String.valueOf(RepInterval);
    }


    private void okButtonActionPerformed(ActionEvent evt) {
            status = 1;
            doClose(RET_OK);
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        status = 0;
        doClose(RET_CANCEL);
    }

    /** Closes the dialog */
    private void closeDialog(WindowEvent evt) {
        doClose(RET_CANCEL);
    }

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dialog.dispose();
    }

}
