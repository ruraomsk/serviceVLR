/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.list.ruraomsk.serviseVLR;

import com.tibbo.aggregate.common.datatable.DataTable;
import java.awt.BorderLayout;
import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import static java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.min;
import static java.lang.System.exit;
import static java.lang.Thread.sleep;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import static javax.swing.SwingUtilities.invokeLater;
import ru.list.ruraomsk.editDT.EditDT;
import ruraomsk.list.ru.vlrmanager.VLRXMLManager;

/**
 *
 * @author Yury Rusinov <ruraomsl@list.ru at Automatics E>
 */
public class ServiseVLR {

    public static PropertiesManager prop;
    static JFrame frame;
    static public JTextArea Message;
    static JPanel pan;
    public static JPanel central;
    static JMenuBar menu = new JMenuBar();
    static EditDT editprop = null;
    static DataTable Table;
    static EditDT editTable = null;
    static boolean connect = false;
    static VLRXMLManager vlrManager = null;
    static LoadDataFiles ldf = null;
    static LoadXMLFiles ldx = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        // TODO code application logic here
        prop = new PropertiesManager();
        prop.loadProperties();
        frame = new JFrame();
        frame.setTitle(titleFrame());
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setMinimumSize(getScreenSize());
        frame.setLocation(25, 25);
        initScreen();
        initMenu();
        appendMessage("Программа к работе готова");
        frame.add(pan);
        frame.pack();

        invokeLater(new Runnable() {
            @Override
            public void run() {
                frame.setVisible(true);
            }
        });
        while (true) {
            sleep(1000L);
            if (editprop != null) {
                if (editprop.isFinished()) {
                    prop.setProrerties(editprop.getDataTable());
                    prop.saveProperties();
                    appendMessage("Настройки сохранены");
                }
            }
        }

    }

    private static Dimension getScreenSize() {
        GraphicsEnvironment environment = getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = environment.getScreenDevices();
        // Get size of each screen
        int screenWidth = MAX_VALUE;
        int screenHeight = MAX_VALUE;
        for (GraphicsDevice device : devices) {
            DisplayMode dmode = device.getDisplayMode();
            screenWidth = min(screenWidth, dmode.getWidth());
            screenHeight = min(screenHeight, dmode.getHeight());
        }
        return new Dimension(screenWidth - 50, screenHeight - 50);
    }

    public static void appendMessage(String message) {
        Message.append(dateToStr(System.currentTimeMillis()) + "\t" + message + "\n");
    }

    private static String titleFrame() {
        return "Настройка БД ВЛР на сервере " + prop.getNameServer() + (connect ? " Подключен" : " Отключен");
    }

    private static String dateToStr(long date) {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss.S ");
        return df.format(new Date(date));
    }

    private static void initScreen() {
        Message = new JTextArea();
        Message.setColumns(1);
        Message.setRows(14);
        Message.setEditable(false);
        JScrollPane jScrollMessage = new JScrollPane();
        jScrollMessage.setViewportView(Message);
        central = new JPanel();
        pan = new JPanel(new BorderLayout());
        pan.add(jScrollMessage, SOUTH);
        pan.add(central, CENTER);
    }

    private static void initMenu() {
        menu = new JMenuBar();
        JMenu loadMenu = new JMenu("Загрузки");
        JMenuItem loadItem = new JMenuItem("Загрузить data файлы");
        loadItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ldf = new LoadDataFiles(prop);
                ldx = null;
            }
        });
        loadMenu.add(loadItem);
        JMenuItem xmlItem = new JMenuItem("Загрузить xml файлы");
        xmlItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ldx = new LoadXMLFiles(prop);
                ldf = null;
            }
        });
        loadMenu.add(xmlItem);
        JMenuItem makeItem = new JMenuItem("Посмотреть загруженное описание переменных ВЛР");
        makeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                central.removeAll();
                if (ldx != null) {
                    new EditDT(central, ldx.getTable(), false);
                }
                if (ldf != null) {
                    new EditDT(central, ldf.getTable(), false);
                }
                central.revalidate();
            }
        });
        loadMenu.add(makeItem);

        JMenuItem saveItem = new JMenuItem("Записать новые переменные ВЛР");
        saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!connect) {
                    appendMessage("Произведите подключение к БД");
                    return;
                }
                if (ldf != null) {
                    vlrManager.toDB(ldf.getTable());
                }
                if (ldx != null) {
                    vlrManager.toDB(ldx.getTable());
                }

                appendMessage("Сохранено в БД");
            }
        });
        loadMenu.add(saveItem);

        menu.add(loadMenu);

        JMenu setUpMenu = new JMenu("Сервер");
        JMenuItem setUpItem = new JMenuItem("Изменить настройки");
        setUpItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                central.removeAll();
                editprop = new EditDT(central, prop.getProperties(), false);
                central.revalidate();
            }
        });
        setUpMenu.add(setUpItem);

        JMenuItem choiceItem = new JMenuItem("Сменить сервер");
        choiceItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (connect) {
                    vlrManager.close();
                    connect = false;
                }
                new ChangeServer(frame);
                frame.setTitle(titleFrame());
                central.revalidate();
            }
        });
        setUpMenu.add(choiceItem);

        JMenuItem connectItem = new JMenuItem("Подключить сервер");
        connectItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (connect) {
                    vlrManager.close();
                    connect = false;
                }
                vlrManager = new VLRXMLManager(prop.getParamSQL());
                if (!vlrManager.connected) {
                    vlrManager = new VLRXMLManager(prop.getParamSQL(), true);
                }
                connect = vlrManager.connected;
                frame.setTitle(titleFrame());
                central.revalidate();
                appendMessage("Сервер " + prop.getNameServer() + " подключен");
            }
        });
        setUpMenu.add(connectItem);

        JMenuItem viewItem = new JMenuItem("Посмотреть текущее состояние");
        viewItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!connect) {
                    return;
                }
                central.removeAll();
                new EditDT(central, vlrManager.toTable(VLRXMLManager.emptyTable().getFormat()), false);
                central.revalidate();

            }
        });
        setUpMenu.add(viewItem);
        menu.add(setUpMenu);

        JMenu progMenu = new JMenu("Программа");
        JMenuItem exitMenu = new JMenuItem("Выход");
        exitMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (connect) {
                    vlrManager.close();
                }
                prop.saveProperties();
                exit(0);
            }
        });
        progMenu.add(exitMenu);
        menu.add(progMenu);
        frame.setJMenuBar(menu);

    }
}
