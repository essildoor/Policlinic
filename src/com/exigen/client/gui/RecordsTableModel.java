package com.exigen.client.gui;

import com.exigen.entity.Record;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecordsTableModel implements TableModel {

    private Set<TableModelListener> listeners = new HashSet<TableModelListener>();
    private List<Record> recordsList;

    public RecordsTableModel(List<Record> recordsList) {
        this.recordsList = recordsList;
    }

    @Override
    public int getRowCount() {
        return recordsList.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Врач";
            case 1:
                return "Пациент";
            case 2:
                return "Дата";
        }
        return "";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Record r = recordsList.get(rowIndex);
        SimpleDateFormat format = new SimpleDateFormat("dd / MM / yyyy");
        switch (columnIndex) {
            case 0:
                return r.getDoctor().getSurname() + " " + r.getDoctor().getName().charAt(0) + ".";
            case 1:
                return r.getPatient().getSurname() + " " + r.getPatient().getName().charAt(0) + ".";
            case 2:
                return format.format(r.getDate());
        }
        return "";
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }
}
