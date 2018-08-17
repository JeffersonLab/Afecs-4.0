
package org.jlab.coda.afecs.ui.rcgui.util;

import org.jlab.coda.afecs.ui.rcgui.CodaRcGui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Vardan Gyurjyan
 */
public class ADialogList extends JDialog {
    private String InfoTitle;
    private String GuiTitle;
    private CodaRcGui owner;

    /**
     * A return status code - returned if Cancel button has been pressed
     */
    public static final int RET_CANCEL = 0;
    /**
     * A return status code - returned if OK button has been pressed
     */
    public static final int RET_OK = 1;

    /**
     * list of objects used to furnish the list
     */
    public String[] myList;

    /**
     * list of objects used to color the list element. 1 = red
     */
    public Integer[] myColor;

    /**
     * selected session
     */
    private String selectedListElement;

    public AtomicInteger status = new AtomicInteger(0);

    public ADialogList(Frame owner) {
        super(owner);
        initComponents();
    }

    public ADialogList(Dialog owner) {
        super(owner);
        initComponents();
    }

    /**
     * @param parent       gui container
     * @param ss           array of strings to be shown in the list
     * @param title        title of the list
     * @param g_title      title of the dialog gui
     * @param selectedItem preselect the list element name
     * @throws HeadlessException exception thrown in the case environment does not support event
     */
    public ADialogList(CodaRcGui parent,
                       String[] ss,
                       Integer[] cl,
                       String title,
                       String g_title,
                       String selectedItem)
            throws HeadlessException {
        super(parent);

        myList = ss;
        myColor = cl;
        InfoTitle = title;
        GuiTitle = g_title;
        owner = parent;

        int tmpi = myColor[0];
        for (int i = 0; i < myColor.length; i++) {
            if (myColor[i] == 11) {
                String tmp = myList[0];
                myList[0] = myList[i];
                myList[i] = tmp;
                myColor[0] = 11;
                myColor[i] = tmpi;
            }
        }

        initComponents();
        guiTitle.setText(GuiTitle);
        infoTitle.setText(InfoTitle);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
            }
        });

        setLocationRelativeTo(parent);

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                doClose();
            }
        });


        if (selectedItem != null) selectRunType(selectedItem);
        setVisible(true);

    }

    /**
     * @return the string of the selected session
     */
    public String getSelectedListElement() {
        return selectedListElement;
    }

    /**
     * select the Jlist element
     *
     * @param runtype to be selected, actually this is the previously selected runtype
     */
    public void selectRunType(String runtype) {

        for (int i = 0; i < myList.length; i++) {
            if (myList[i].equalsIgnoreCase(runtype)) {
                sessionsList.setSelectedIndex(i);
                return;
            }
        }
    }

    private void SessionsListMouseClicked(MouseEvent evt) {
        selectedListElement = (String) sessionsList.getSelectedValue();
    }

    private void doClose() {
        setVisible(false);
        dispose();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner non-commercial license
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        guiTitle = new JLabel();
        infoTitle = new JLabel();
        scrollPane1 = new JScrollPane();
        sessionsList = new JList();
        buttonBar = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();
        action1 = new OkAction();
        action2 = new CancelAction();

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {

                //---- guiTitle ----
                guiTitle.setText("guiTitle");
                guiTitle.setHorizontalAlignment(SwingConstants.CENTER);
                guiTitle.setBackground(new Color(0, 51, 51));
                guiTitle.setFont(new Font("Lucida Grande", Font.PLAIN, 16));
                guiTitle.setForeground(new Color(0, 51, 51));

                //---- infoTitle ----
                infoTitle.setText("infoTitile");
                infoTitle.setHorizontalAlignment(SwingConstants.CENTER);
                infoTitle.setForeground(new Color(51, 0, 0));

                //======== scrollPane1 ========
                {

                    //---- sessionsList ----
                    sessionsList.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
                    sessionsList.setCellRenderer(new MyCellRenderer());
                    sessionsList.setModel(new javax.swing.AbstractListModel() {
                        String[] strings = myList;

                        public int getSize() {
                            return strings.length;
                        }

                        public Object getElementAt(int i) {
                            return strings[i];
                        }
                    });

                    sessionsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
                    sessionsList.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                            SessionsListMouseClicked(evt);
                        }
                    });
                    scrollPane1.setViewportView(sessionsList);
                }

                GroupLayout contentPanelLayout = new GroupLayout(contentPanel);
                contentPanel.setLayout(contentPanelLayout);
                contentPanelLayout.setHorizontalGroup(
                        contentPanelLayout.createParallelGroup()
                                .addGroup(contentPanelLayout.createSequentialGroup()
                                        .addGroup(contentPanelLayout.createParallelGroup()
                                                .addComponent(infoTitle, GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
                                                .addComponent(guiTitle, GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
                                                .addGroup(contentPanelLayout.createSequentialGroup()
                                                        .addContainerGap()
                                                        .addComponent(scrollPane1, GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)))
                                        .addContainerGap())
                );
                contentPanelLayout.setVerticalGroup(
                        contentPanelLayout.createParallelGroup()
                                .addGroup(contentPanelLayout.createSequentialGroup()
                                        .addComponent(guiTitle)
                                        .addGap(18, 18, 18)
                                        .addComponent(infoTitle)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(scrollPane1)
                                        .addContainerGap())
                );
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout) buttonBar.getLayout()).columnWidths = new int[]{0, 85, 80};
                ((GridBagLayout) buttonBar.getLayout()).columnWeights = new double[]{1.0, 0.0, 0.0};

                //---- okButton ----
                okButton.setAction(action1);
                buttonBar.add(okButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 5), 0, 0));

                //---- cancelButton ----
                cancelButton.setAction(action2);
                buttonBar.add(cancelButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
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
    private JLabel guiTitle;
    private JLabel infoTitle;
    private JScrollPane scrollPane1;
    private JList sessionsList;
    private JPanel buttonBar;
    private JButton okButton;
    private JButton cancelButton;
    private OkAction action1;
    private CancelAction action2;
    // JFormDesigner - End of variables declaration  //GEN-END:variables

    private class OkAction extends AbstractAction {
        private OkAction() {
            // JFormDesigner - Action initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
            // Generated using JFormDesigner non-commercial license
            putValue(NAME, "OK");
            // JFormDesigner - End of action initialization  //GEN-END:initComponents
        }

        public void actionPerformed(ActionEvent e) {
            selectedListElement = (String) sessionsList.getSelectedValue();
            if (selectedListElement != null) {
                if (GuiTitle.equals("Cool RunTypes")) {
                    owner.selectRunType(selectedListElement.trim());
                } else if (GuiTitle.equals("MB Subscribe")) {
                    owner.showMBSRunType(selectedListElement.trim());
                }
                status.set(RET_OK);
                doClose();
            } else {
                status.set(RET_CANCEL);
                doClose();
            }
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
            status.set(RET_CANCEL);
            doClose();
        }
    }

    /**
     * Inner class for cell rendering, namely coloring list element
     * red if the myColor int array index is set to 1
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
            if (myColor != null) {
                if (myColor[index] > 0) {
                    setForeground(Color.red);
                } else {
                    setForeground(Color.black);
                }
            }
            if (isSelected) {
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
