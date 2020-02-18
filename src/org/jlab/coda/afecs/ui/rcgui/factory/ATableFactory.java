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

package org.jlab.coda.afecs.ui.rcgui.factory;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * RcGUI table factory
 * </p>
 *
 * @author gurjyan
 *         Date: 11/18/14 Time: 2:51 PM
 * @version 4.x
 */
public class ATableFactory {
    public DefaultTableModel mmm;
    public JTable myTable;

    /**
     * <p>
     * Creates and returns a JTable object
     * </p>
     *
     * @param name    of the table
     * @param al      ArrayList of column names
     * @param useRend boolean indicates to use Coda
     *                run-control specific renderer
     *                (colored cell )
     * @return JTable object
     */
    public JTable createTable(String name,
                              ArrayList<String> al,
                              boolean useRend) {
        myTable = new JTable();
        Object[][] oa = new Object[0][al.size()];
        String[] sa = new String[al.size()];
        final Class[] types = new Class[al.size()];
        final boolean[] canEdit = new boolean[al.size()];
        int i = 0;
        for (String s : al) {
            sa[i] = s;
            types[i] = java.lang.String.class;
            canEdit[i] = false;
            i++;
        }
        mmm = new javax.swing.table.DefaultTableModel(oa, sa) {

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        };

        myTable.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        myTable.setFont(Font.getFont("Luxi Sans-Bold-13"));
        myTable.setModel(mmm);
        myTable.setColumnSelectionAllowed(true);
        TableColumnModel tcm = myTable.getColumnModel();

        if (useRend) {
            TableCellRenderer renderer;
            if (name.contains("data")) {
                renderer = new ATableCellRendererS();
            } else {
                renderer = new ATableCellRenderer();
            }
            try {
                myTable.setDefaultRenderer(Class.forName("java.lang.String"), renderer);
            } catch (ClassNotFoundException ex) {
                System.exit(0);
            }
            for (String s : al) {
                if (s.equalsIgnoreCase("Name")) {
                    int index = tcm.getColumnIndex(s);
                    tcm.getColumn(index).setPreferredWidth(150);
                } else if (s.equalsIgnoreCase("State")) {
                    int index = tcm.getColumnIndex(s);
                    tcm.getColumn(index).setPreferredWidth(100);
                } else if (s.equalsIgnoreCase("Message")) {
                    int index = tcm.getColumnIndex(s);
                    tcm.getColumn(index).setPreferredWidth(570);
                } else if (s.equalsIgnoreCase("Time")) {
                    int index = tcm.getColumnIndex(s);
                    tcm.getColumn(index).setPreferredWidth(90);
                } else if (s.equalsIgnoreCase("SeverityId")) {
                    int index = tcm.getColumnIndex(s);
                    TableColumn column = tcm.getColumn(index);
                    tcm.removeColumn(column);
                }

            }
            myTable.getTableHeader().setFont(new Font("Serif", Font.BOLD, 12));
        } else {
            myTable.setBackground(Color.WHITE);
            myTable.setFont(new Font("Serif", Font.PLAIN, 10));
            myTable.getTableHeader().setFont(new Font("Serif", Font.BOLD, 10));
        }
        myTable.getTableHeader().setBackground(Color.orange);
        myTable.getTableHeader().setForeground(new Color(0, 77, 77));
        myTable.setName(name);
        return myTable;
    }

    public JTable getTable() {
        return myTable;
    }

    public DefaultTableModel getTableModel() {
        return mmm;
    }

    public Object getSelectedName() {
        int r = myTable.getSelectionModel().getLeadSelectionIndex();
        if (r >= 0 && mmm.getRowCount() > 0) {
            Object o = null;
            try {
                o = myTable.getValueAt(r, 0);
            } catch (Exception e) {
//                System.out.println("DDD "+e.getMessage());
            }
            return o;
        } else {
            return null;
        }
    }

    /**
     * Creates table with daLog browser specific column structure
     *
     * @param name of the table
     * @return JTable object
     */
    public JTable createDaLogTable(String name) {
        ArrayList<String> al = new ArrayList<>();
        al.add("Name");
        al.add("Message");
        al.add("Time");
        al.add("Severity");
        al.add("SeverityId");
        return createTable(name, al, true);
    }

    /**
     * Creates table with rcGui data table specific column structure
     *
     * @param name of the table
     * @return JTable object
     */
    public JTable createDataTable(String name, ListSelectionListener listener) {
        ArrayList<String> al = new ArrayList<>();
        al.add("Name");
        al.add("State");
        al.add("EvtRate");
        al.add("DataRate");
        al.add("IntEvtRate");
        al.add("IntDataRate");
        JTable table = createTable(name, al, true);
        table.getSelectionModel().addListSelectionListener(listener);
        table.getColumnModel().getSelectionModel()
                .addListSelectionListener(listener);
        return table;
    }

    /**
     * Creates table with rcGui individual
     * component table specific column structure
     *
     * @param name of the table
     * @return JTable object
     */
    public JTable createIndividualCompTable(String name) {
        ArrayList<String> al = new ArrayList<>();
        al.add("Payload");
        al.add("Value");
        return createTable(name, al, true);
    }

    public void addRowsIndCTable() {
        String[] n = {"codaName",
                "codaClass",
                "objectType",
                "state",
                "eventCount",
                "eventRate (Hz)",
                "dataRate (kB/s)",
                "dataCount",
                "liveTime",
                "fileName",
                "minEventSize (bytes)",
                "maxEventSize (bytes)",
                "avgEventSize (bytes)",
                "minSingleEvtBuildTime (ns)",
                "maxSingleEvtBuildTime (ns)",
                "avgSingleEvtBuildTime (ns)",
                "chunk_x_etBuffer (bytes)",
        };
        for (String s : n) {
            String r[] = new String[2];
            r[0] = s;
            r[1] = "";
            getTableModel().addRow(r);
        }
    }

    /**
     * Clears created table. N.B. Table default
     * model is null if createTable is not called.
     */
    public void clearTable() {
        try {
            if (mmm != null) {
                while (mmm.getRowCount() > 0) {
                    mmm.removeRow(0);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
//            System.out.println("DDD ArrayIndexOutOfBoundsException " + e.getMessage());
        }

    }

    public void addRcDataTable(String[] data) {
        boolean found = false;
        try {
            for (int i = 0; i < myTable.getRowCount(); i++) {
                if (myTable.getValueAt(i, 0).equals(data[0])) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                getTableModel().addRow(data);
            } else {
                updateRcDataTable(data);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
//            System.out.println("DDD ArrayIndexOutOfBoundsException " + e.getMessage());
        }
//         myTable.scrollRectToVisible(myTable.getCellRect(getTableModel().getRowCount() - 1, 0, true));

    }


    public void updateRcDataTable(String[] data) {
        try {
            for (int i = 0; i < myTable.getRowCount(); i++) {
                if (myTable.getValueAt(i, 0).equals(data[0])) {
                    myTable.setValueAt(data[1], i, 1);
                    myTable.setValueAt(data[2], i, 2);
                    myTable.setValueAt(data[3], i, 3);
                    myTable.setValueAt(data[4], i, 4);
                    myTable.setValueAt(data[5], i, 5);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
//            System.out.println("DDD ArrayIndexOutOfBoundsException " + e.getMessage());
        }
    }

    public void updateIndividualCompTable(String[] v) {
        for (int i = 0; i < v.length; i++) {
            myTable.setValueAt(v[i], i, 1);
        }
    }

    private void clearIOBufferRaws() {
        try {
            for (int i = 17; i < myTable.getRowCount(); i++) {
                getTableModel().removeRow(i);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
//            System.out.println("DDD ArrayIndexOutOfBoundsException " + e.getMessage());
        }

    }

    private int getRawId(String rawName) {
        int j = -1;
        try {
            for (int i = 0; i < myTable.getRowCount(); i++) {
                if (myTable.getModel().getValueAt(i, 0).equals(rawName)) return i;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
//            System.out.println("DDD ArrayIndexOutOfBoundsException " + e.getMessage());
        }

        return j;
    }


    public void updateIndComTInputBuffers(String compName, Map<String, Double> data) {
        if (data != null && !data.isEmpty()) {

            for (String s : data.keySet()) {
                if (s.contains(compName)) {
                    int rid = getRawId("in:" + s);
                    if (rid < 0) {
                        String r[] = new String[2];
                        r[0] = "in:" + s;
                        r[1] = data.get(s).toString();
                        getTableModel().addRow(r);
                    } else {
                        myTable.setValueAt(data.get(s), rid, 1);
                    }
                }
            }
        }
    }

    public void updateIndComTOutputBuffers(String compName, Map<String, Double> data) {
        if (data != null && !data.isEmpty()) {

            for (String s : data.keySet()) {
                if (s.contains(compName)) {
                    int rid = getRawId("out:" + s);
                    if (rid < 0) {
                        String r[] = new String[2];
                        r[0] = "out:" + s;
                        r[1] = data.get(s).toString();
                        getTableModel().addRow(r);
                    } else {
                        myTable.setValueAt(data.get(s), rid, 1);
                    }
                }
            }
        }
    }
}
