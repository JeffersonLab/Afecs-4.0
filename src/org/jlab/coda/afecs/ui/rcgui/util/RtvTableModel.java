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

import javax.swing.table.AbstractTableModel;

/**
 * Describe...
 *
 * @author gurjyan
 *         Date: 2/14/14 Time: 11:32 AM
 * @version 2
 */
public class RtvTableModel extends AbstractTableModel {

    private String[] columnNames = {"RTV", "Value"};
//    private Object[][] data = new Object[][] {
//            {"%(session)", null},
//            {"%(rt)", null},
//            {"%(rn)", null},
//            {"%(config)", null},
//            {"%(dir)", null}
//    };

    private Object[][] data;

    Class<?>[] columnTypes = new Class<?>[] {
            String.class, String.class
    };

    public RtvTableModel(Object[][] data){
        this.data = data;
    }

    private boolean[] columnEditable = new boolean[] {
            false, true
    };

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnTypes[columnIndex];
    }


    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (getValueAt(row,0).equals("%(rn)") ||
                getValueAt(row,0).equals("%(rt)") ||
                getValueAt(row,0).equals("%(session)") ||
                getValueAt(row,0).equals("%(udl)") ||
                getValueAt(row,0).equals("%(config)") ||
                getValueAt(row,0).equals("%(dir)") ||
                getValueAt(row,0).equals("%(ddb)")
                ) {
            return false;
        } else {
            return columnEditable[col];
        }
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }


}
