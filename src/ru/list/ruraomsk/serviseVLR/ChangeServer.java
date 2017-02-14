/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.list.ruraomsk.serviseVLR;

import com.tibbo.aggregate.common.datatable.DataRecord;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author Yury Rusinov <ruraomsl@list.ru at Automatics E>
 */
public class ChangeServer extends JDialog {

    ChangeServer(JFrame frame) {
        super(frame, "Выберите сервер", true);
        DataRecord current = ServiseVLR.prop.getCurrent();
        String[] items = new String[ServiseVLR.prop.getDataLen()];
        int i = 0;
        while (ServiseVLR.prop.changeCurrent(i) != null) {
            items[i] = ServiseVLR.prop.getNameServer();
            i++;
        }
        ServiseVLR.prop.setCurrent(current);
        JComboBox combo = new JComboBox(items);
        combo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox box = (JComboBox) e.getSource();
                ServiseVLR.prop.changeCurrent(box.getSelectedIndex());
                ServiseVLR.appendMessage("Изменен сервер");
            }
        });
        add(combo,BorderLayout.NORTH);
        JButton ok = new JButton("Принять");
        ok.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {
                setVisible(false);
            }
        });

        add(ok, BorderLayout.SOUTH);
        setSize(260, 100);
        setLocation(200, 200);
        setVisible(true);
    }

}
