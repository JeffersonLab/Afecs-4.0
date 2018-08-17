package org.jlab.coda.afecs.ui.rcgui.factory;

import org.jlab.coda.afecs.system.AConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;

/**
 * Created by gurjyan on 3/6/17.
 */
public class ATableCellRendererS extends DefaultTableCellRenderer {
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

            cell.setBackground(c);

            if (value instanceof String ) {
                String sever = ((String) value);
                if(sever.contains(AConstants.WARN) ||
                        sever.contains(AConstants.disabled) ||
                        sever.contains("warn") ||
                        sever.equals(AConstants.disabled) ||
                        sever.equals("WARNING") ||
                        sever.equals("warning") ||
                        sever.equals(AConstants.udf) ||
                        sever.equals(AConstants.checking)
                        ){
                    cell.setBackground(Color.YELLOW);
                } else if (
                        sever.equals(AConstants.SERROR) ||
                        sever.equals(AConstants.ERROR) ||
                        sever.equals("error") ||
                        sever.equals(AConstants.removed) ||
                        sever.equals(AConstants.disconnected) ||
                        sever.equals(AConstants.failed)
                        ){
                    cell.setBackground(Color.RED);
                } else {
                    cell.setBackground(c);
                }
            }
        }
        return cell;
    }
}
