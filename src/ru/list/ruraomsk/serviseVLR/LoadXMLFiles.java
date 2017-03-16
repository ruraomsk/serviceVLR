/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.list.ruraomsk.serviseVLR;

import com.tibbo.aggregate.common.context.ContextException;
import com.tibbo.aggregate.common.datatable.DataRecord;
import com.tibbo.aggregate.common.datatable.DataTable;
import com.tibbo.aggregate.common.datatable.EncodingUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;
import ruraomsk.list.ru.strongsql.ParamSQL;
import ruraomsk.list.ru.vlrmanager.VLRXMLManager;

/**
 *
 * @author Yury Rusinov <ruraomsl@list.ru at Automatics E>
 */
public class LoadXMLFiles {

    DataTable Table;

    public LoadXMLFiles(PropertiesManager prop) {
        ParamSQL param = prop.getParamSQL();
        String startPath;
        ServiseVLR.appendMessage("Начинаем создавать....");
        param.myDB = prop.getVlrs();
        VLRXMLManager vlrManager = new VLRXMLManager(param);
        Table = VLRXMLManager.emptyTable();
        startPath = prop.getCurrent().getString("work");
        File start = new File(startPath + File.separator+"XML"+File.separator);
        loadData(start);
        ServiseVLR.appendMessage("Главная таблица создана");
    }

    public DataTable getTable() {
        return Table;
    }

    private void loadData(File start) {
        String files[] = start.list();
        for (int i = 0; i < files.length; i++) {
            String dataname = "vars.xml";
            String DATANAME = "VARS.xml";

            File datax = new File(start.getAbsolutePath() + File.separator + files[i] + File.separator + dataname);
            File DATAX = new File(start.getAbsolutePath() + File.separator + files[i] + File.separator + DATANAME);
            File id = new File(files[i]);
            if (!datax.exists() & !DATAX.exists()) {
                ServiseVLR.appendMessage(id.getName() + " " + datax.getPath() + " отсутствует");
                continue;
            }
            if (!datax.exists()) {
                datax = DATAX;
            }
            ServiseVLR.appendMessage(id.getName() + " " + dataname);
            DataTable result;
            try {
                String buffer = new String(Files.readAllBytes(datax.toPath()));
                result = EncodingUtils.decodeFromXML(buffer);
            } catch (IOException | ContextException | DOMException | IllegalArgumentException | SAXException | ParserConfigurationException ex) {
                ServiseVLR.appendMessage("Файл " + datax.getPath() + " " + ex.getMessage());
                continue;
            }
            if (result == null) {
                ServiseVLR.appendMessage(datax.toPath() + " ошибка расшифровки");
                continue;
            }
            DataRecord rec = Table.addRecord();
            rec.setValue("idvlr", id.getName());
            rec.setValue("idfile",1);
            rec.setValue("variables", result);

        }

    }
}
