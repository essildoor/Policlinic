package com.exigen.client.gui;

import com.exigen.entity.Patient;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.*;

public class PatientsTableModel implements TableModel{

    private Set<TableModelListener> listeners = new HashSet<TableModelListener>();
    private List<Patient> patientList;

    public PatientsTableModel(List<Patient> patientsList) {
        this.patientList = patientsList;
    }

    @Override
    public int getRowCount() {
        return patientList.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Фамилия";
            case 1:
                return "Имя";
            case 2:
                return "Участок";
            case 3:
                return "Диагноз";
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
        Patient p = patientList.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return p.getSurname();
            case 1:
                return p.getName();
            case 2:
                return p.getDistrict();
            case 3:
                return p.getDiagnosis();
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
