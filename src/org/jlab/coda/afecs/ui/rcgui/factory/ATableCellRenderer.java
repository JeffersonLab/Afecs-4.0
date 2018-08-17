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

//import org.jlab.coda.afecs.system.AConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;

/**
 * <p>
 *     RcGUI table cell rendering class
 * </p>
 * @author gurjyan
 *         Date: 11/18/14 Time: 2:51 PM
 * @version 3.x
 */
public class ATableCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int column) {
        Component cell = super.getTableCellRendererComponent(table, value,
                isSelected, hasFocus, row, column);
        if(value!=null){
            Color c;
            if (row % 2 == 0 && !table.isCellSelected(row, column)) {
                c=new Color(240,240,240);
                cell.setBackground(c);
            }
            else {
                cell.setBackground(Color.white);
                c = Color.white;
            }

            TableModel model = table.getModel();

            String columnName = model.getColumnName(column+1);
            if(columnName.equals("SeverityId")){
                String vv = (String)model.getValueAt(row,column+1);
                int v = Integer.parseInt(vv);
                if (v >0 && v <= 4){
                    c=Color.WHITE;
                }
                else if (v >= 5 && v <= 8){
                    c=Color.YELLOW;
                }
                else if (v >= 9 && v <= 12){
                    c=Color.RED;
                }
                else if (v >= 13 && v <= 14){
                    c=Color.MAGENTA;
                }
                else if (v == 15){
                    c=Color.getHSBColor(195,25,90);
                }
            }

            cell.setBackground(c);

//            if (value instanceof String ) {
//                String sever = ((String) value);
//                if(sever.contains(AConstants.WARN) ||
//                        sever.contains(AConstants.disabled) ||
//                        sever.contains("warn") ||
//                        sever.equals(AConstants.disabled) ||
//                        sever.equals("WARNING") ||
//                        sever.equals("warning") ||
//                        sever.equals(AConstants.udf) ||
//                        sever.equals(AConstants.checking)
//                        ){
//                    cell.setBackground(Color.YELLOW);
//                } else if (
//                        sever.equals(AConstants.SERROR) ||
//                        sever.equals(AConstants.ERROR) ||
//                        sever.equals("error") ||
//                        sever.equals(AConstants.removed) ||
//                        sever.equals(AConstants.disconnected) ||
//                        sever.equals(AConstants.failed)
//                        ){
//                    cell.setBackground(Color.RED);
//                } else {
//                    cell.setBackground(c);
//                }
//            }
        }
        return cell;
    }
}
