package com.exigen.client.gui;

import com.exigen.entity.Doctor;
import com.exigen.entity.DoctorSpecialization;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DoctorsTableModel implements TableModel {

    private Set<TableModelListener> listeners = new HashSet<TableModelListener>();
    private List<Doctor> doctorsList;

    public DoctorsTableModel(List<Doctor> doctorsList) {
        this.doctorsList = doctorsList;
    }

    @Override
    public int getRowCount() {
        return doctorsList.size();
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
                return "Кабинет";
            case 3:
                return "Специальность";
        }
        return "";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 3)
            return DoctorSpecialization.class;
        if (columnIndex == 2)
            return Integer.class;
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Doctor d = doctorsList.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return d.getSurname();
            case 1:
                return d.getName();
            case 2:
                return d.getRoom();
            case 3:
                return d.getSpecialization();
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
