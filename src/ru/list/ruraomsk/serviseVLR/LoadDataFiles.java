/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.list.ruraomsk.serviseVLR;

import com.tibbo.aggregate.common.datatable.DataRecord;
import com.tibbo.aggregate.common.datatable.DataTable;
import java.io.File;
import ruraomsk.list.ru.strongsql.ParamSQL;
import ruraomsk.list.ru.vlrmanager.VLRDataTableManager;
import ruraomsk.list.ru.vlrmanager.VLRXMLManager;

/**
 *
 * @author Yury Rusinov <ruraomsl@list.ru at Automatics E>
 */
public class LoadDataFiles {

    DataTable Table;
    public LoadDataFiles( PropertiesManager prop) {
        ParamSQL param = prop.getParamSQL();
        String startPath;
        ServiseVLR.appendMessage("Начинаем создавать....");
        param.myDB = prop.getVlrs();
        VLRXMLManager vlrManager = new VLRXMLManager(param);
        Table = VLRXMLManager.emptyTable();
        startPath=prop.getCurrent().getString("work");
        File start = new File(startPath + File.separator + "PPO" + File.separator);
        loadData(start, true);
        start = new File(startPath + File.separator + "SPO" + File.separator);
        loadData(start, false);
        ServiseVLR.appendMessage("Главная таблица создана");
    }
    public DataTable getTable(){
        return Table;
    }
    private void loadData(File start, boolean ppo) {
        DataTable emptyTable = VLRDataTableManager.emptyTable();
        String files[] = start.list();
        for (int i = 0; i < files.length; i++) {
            String dataname = ppo ? "data110" : "data10";
            String DATANAME = ppo ? "DATA110" : "DATA10";

            File datax = new File(start.getAbsolutePath()+File.separator +files[i] + File.separator + "eeprom" + File.separator + dataname);
            File DATAX = new File(start.getAbsolutePath()+File.separator +files[i] + File.separator + "eeprom" + File.separator + DATANAME);
            File id = new File(files[i]);
            DataRecord rec = Table.addRecord();
            rec.setValue("idvlr", id.getName());

            if (!datax.exists()&!DATAX.exists()) {
                ServiseVLR.appendMessage(id.getName() + " " + datax.getPath() + " отсутствует");
                rec.setValue("variables", emptyTable);
                if (ppo) {
                    rec.setValue("idfile", 2);
                    Table.addRecord(rec);
                    rec.setValue("idfile", 3);
                } else {
                    rec.setValue("idfile", 1);
                }
                continue;
            }
            if(!datax.exists()) datax=DATAX;
            ServiseVLR.appendMessage(id.getName()+" "+dataname);
            
            byte[] buffer;
            if (ppo) {
                rec.setValue("idfile", 2);
                rec.setValue("variables", emptyTable);
                buffer = VLRDataTableManager.loadZipFile(datax.getPath(), true);
                if (buffer == null) {
                    ServiseVLR.appendMessage(id.getName() + " " + dataname + " ошибка в zip переменных");
                    continue;
                }
                DataTable dataTable = VLRDataTableManager.loadVariables(buffer);
                if (dataTable == null) {
                    ServiseVLR.appendMessage(id.getName() + " " + dataname + " ошибка расшифровки");
                    continue;
                }
                rec.setValue("variables", dataTable);
                rec = Table.addRecord();
                rec.setValue("idvlr", id.getName());
                rec.setValue("idfile", 3);
                rec.setValue("variables", emptyTable);

                buffer = VLRDataTableManager.loadZipFile(datax.getPath(), false);
                if (buffer == null) {
                    ServiseVLR.appendMessage(id.getName() + " " + dataname + " ошибка в zip переменных");
                    continue;
                }
                dataTable = VLRDataTableManager.loadConstants(buffer);
                if (dataTable == null) {
                    ServiseVLR.appendMessage(id.getName() + " " + dataname + " ошибка расшифровки");
                    continue;
                }
                rec.setValue("variables", dataTable);
            } else{
                rec.setValue("idfile", 1);
                rec.setValue("variables", emptyTable);
                buffer = VLRDataTableManager.loadZipFile(datax.getPath(), true);
                if (buffer == null) {
                    ServiseVLR.appendMessage(id.getName() + " " + dataname + " ошибка в zip переменных");
                    continue;
                }
                DataTable dataTable = VLRDataTableManager.loadVariables(buffer);
                if (dataTable == null) {
                    ServiseVLR.appendMessage(id.getName() + " " + dataname + " ошибка расшифровки");
                    continue;
                }
                rec.setValue("variables", dataTable);
            }

        }
    }

}
