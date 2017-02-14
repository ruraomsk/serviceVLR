/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.list.ruraomsk.serviseVLR;

import com.tibbo.aggregate.common.Log;
import com.tibbo.aggregate.common.context.ContextException;
import com.tibbo.aggregate.common.datatable.DataRecord;
import com.tibbo.aggregate.common.datatable.DataTable;
import com.tibbo.aggregate.common.datatable.EncodingUtils;
import com.tibbo.aggregate.common.datatable.FieldFormat;
import com.tibbo.aggregate.common.datatable.TableFormat;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;
import ruraomsk.list.ru.strongsql.ParamSQL;

/**
 *
 * @author Yury Rusinov <ruraomsl@list.ru at Automatics E>
 */
public class PropertiesManager {

    private DataTable properties;
    private String datafiles;
    private String vlrs="vlr";
    private DataRecord current;

    /**
     * Возвращает имя базы данных с файлами описания переменных
     *
     * @return
     */
    public String getVlrs() {
        return vlrs;
    }

    /**
     * Загружает таблицу настроек серверов
     */
    public void loadProperties() {
        try {
            String content = new String(Files.readAllBytes(Paths.get("properties.xml")));
            properties = EncodingUtils.decodeFromXML(content);
            current = getProperties().rec();
            return;
        } catch (IOException | ParserConfigurationException | ContextException | DOMException | IllegalArgumentException | SAXException ex) {
        }
        TableFormat tf = new TableFormat();
        tf.addField(FieldFormat.create("<name><S><A=ruraWin><D=Имя сервера>"));
        tf.addField(FieldFormat.create("<url><S><A=jdbc:postgresql://192.168.1.30:5432/vlrbase><D=URL сервера>"));
        tf.addField(FieldFormat.create("<user><S><A=postgres><D=Пользователь>"));
        tf.addField(FieldFormat.create("<password><S><A=162747><D=Пароль>"));
        tf.addField(FieldFormat.create("<work><S><A=rura><D=Рабочая директория>"));
        properties = new DataTable(tf);
        current = properties.addRecord();
    }
    public void setProrerties(DataTable properties){
        this.properties=properties;
    }
    /**
     * Сохраняет настройки
     */
    public void saveProperties() {
        String content = null;
        try {
            content = EncodingUtils.encodeToXML(properties);
        } catch (ParserConfigurationException | IOException | ContextException | DOMException ex) {
            Log.DATABASE.info("Error encoding property table " + ex.getMessage());
        }

        try (PrintWriter writer = new PrintWriter("properties.xml", "UTF-8")) {
            writer.print(content);
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            Log.DATABASE.info("Error for write property table " + ex.getMessage());
        }
    }

    /**
     * @return the properties
     */
    public DataTable getProperties() {
        return properties;
    }

    /**
     * @return the current
     */
    public DataRecord getCurrent() {
        return current;
    }
    public void setCurrent(DataRecord cur){
        current=cur;
    }
    /**
     * Устанавливает новую текущую запись из настроек
     *
     * @param idx
     * @return
     */
    public DataRecord changeCurrent(int idx) {
        if(idx>=properties.getRecordCount()) return null;
        DataRecord cur = properties.getRecord(idx);
        current = cur;
        return current;
    }

    /**
     * читает из текущей настройки и настраивает таблицу параметров для SQL
     *
     * @return
     */
    public ParamSQL getParamSQL() {
        ParamSQL par = new ParamSQL();
        par.myDB=getVlrs();
        par.JDBCDriver = "org.postgresql.Driver";
        par.url = current.getString("url");
        par.user = current.getString("user");
        par.password = current.getString("password");
        return par;
    }
    public String getNameServer(){
        return current.getString("name");
    }
    public int getDataLen(){
        return properties.getRecordCount();
    }
}
