package com.exigen.client.gui;

import com.alee.laf.WebLookAndFeel;
import com.exigen.client.Client;
import com.exigen.entity.Doctor;
import com.exigen.entity.Patient;
import com.exigen.entity.Record;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import static com.exigen.util.ProtocolCodes.*;

@SuppressWarnings("Unchecked")
public class AddRecordDialog extends JDialog implements Runnable {

    private Client client;
    private JLabel statusLabel;
    private ArrayList<Doctor> doctorsList;
    private MainForm form;


    public AddRecordDialog(MainForm form, JLabel statusLabel) {
        this.statusLabel = statusLabel;
        this.form = form;
        client = form.getClient();
    }

    private Vector<String> getDoctorsListAsVector(String specialization) {
        System.out.println("getDoctorsListAsVector: entering");
        if (specialization.equals("")) {
            doctorsList = (ArrayList<Doctor>) client.sendRequest(REQUEST_DOCTORS_LIST, null);
        } else {
            //search mask
            Doctor param = new Doctor(null, null, 0, specialization);
            doctorsList =
                    (ArrayList<Doctor>) client.sendRequest(REQUEST_DOCTORS_LIST, param);
        }
        System.out.println("getDoctorsListAsVector: received list from server");
        int i = doctorsList.size();
        Vector<String> doctorsModifiedList = new Vector<String>(i);
        for (Doctor d : doctorsList) {
            doctorsModifiedList.add(
                    String.format(
                            "%s %s. ,кабинет %d", d.getSurname(), d.getName().charAt(0), d.getRoom()
                    )
            );
        }
        System.out.println("getDoctorsListAsVector: vector constructed, leaving");
        return doctorsModifiedList;
    }

    private Vector<String> getDoctorSpecializationListAsVector() {
        return new Vector<String>(
                (ArrayList<String>) client.sendRequest(REQUEST_DOCTOR_SPECIALIZATION_LIST, null)
        );
    }

    @Override
    public void run() {
        WebLookAndFeel.install();
        int width = 350;
        int height = 520;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(
                (int) ((screenSize.getWidth() - width) / 2),
                (int) ((screenSize.getHeight() - height) / 2));
        Container pane = getContentPane();
        pane.setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(width, height));

        JPanel patientPanel = new JPanel();
        JPanel datePanel = new JPanel();
        JPanel doctorPanel = new JPanel();
        patientPanel.setLayout(new BoxLayout(patientPanel, BoxLayout.Y_AXIS));
        datePanel.setLayout(new BoxLayout(datePanel, BoxLayout.Y_AXIS));
        doctorPanel.setLayout(new BoxLayout(doctorPanel, BoxLayout.Y_AXIS));

        MaskFormatter f = null;
        try {
            f = new MaskFormatter("#####");
            f.setValidCharacters("0123456789");
        } catch (ParseException e) {
            e.printStackTrace();
        }


        //patient section setup
        JPanel dataPanel1 = new JPanel(new GridLayout(5, 2, 10, 10));
        final JTextField patientName = new JTextField();
        final JTextField patientSurname = new JTextField();
        final JTextField patientDistrict = new JTextField();
        final JTextField patientDiagnosis = new JTextField();
        final JFormattedTextField patientInsuranceId = new JFormattedTextField(f);
        JLabel patientNameLabel = new JLabel("Имя:");
        JLabel patientSurnameLabel = new JLabel("Фамилия:");
        JLabel patientDistrictLabel = new JLabel("Участок:");
        JLabel patientDiagnosisLabel = new JLabel("Диагноз:");
        JLabel patientInsuranceIdLabel = new JLabel("Номер страхового полиса:");

        dataPanel1.add(patientNameLabel);
        dataPanel1.add(patientName);
        dataPanel1.add(patientSurnameLabel);
        dataPanel1.add(patientSurname);
        dataPanel1.add(patientDistrictLabel);
        dataPanel1.add(patientDistrict);
        dataPanel1.add(patientDiagnosisLabel);
        dataPanel1.add(patientDiagnosis);
        dataPanel1.add(patientInsuranceIdLabel);
        dataPanel1.add(patientInsuranceId);

        dataPanel1.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Пациент"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        JPanel viewPanel1 = new JPanel(new GridLayout(1, 1));
        viewPanel1.add(dataPanel1);
        patientPanel.add(viewPanel1);

        //date section setup
        JPanel dataPanel2 = new JPanel(new GridLayout(1, 2, 10, 10));
        SimpleDateFormat f1 = new SimpleDateFormat("dd/MM/yyyy");
        final JFormattedTextField date = new JFormattedTextField(f1);
        JLabel dateLabel = new JLabel("Дата:");

        dataPanel2.add(dateLabel);
        dataPanel2.add(date);
        dataPanel2.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Дата"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        JPanel viewPanel2 = new JPanel(new GridLayout(1, 1));
        viewPanel2.add(dataPanel2);
        datePanel.add(viewPanel2);

        //doctor section setup
        JPanel dataPanel3 = new JPanel(new GridLayout(2, 2, 10, 10));
        final JComboBox doctorSpecialization = new JComboBox(getDoctorSpecializationListAsVector());
        final JComboBox doctors = new JComboBox();
        doctors.setVisible(false);
        doctorSpecialization.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String specializationPicked = doctorSpecialization.getSelectedItem().toString();
                doctors.setModel(
                        new DefaultComboBoxModel(getDoctorsListAsVector(specializationPicked))
                );
                doctors.setVisible(true);
            }
        });

        JLabel doctorSpecializationLabel = new JLabel("Специальность:");
        JLabel doctorName = new JLabel("Врач:");

        dataPanel3.add(doctorSpecializationLabel);
        dataPanel3.add(doctorName);
        dataPanel3.add(doctorSpecialization);
        dataPanel3.add(doctors);
        dataPanel3.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Врач"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        JPanel viewPanel3 = new JPanel(new GridLayout(1, 1));
        viewPanel3.add(dataPanel3);
        doctorPanel.add(viewPanel3);

        JButton okButton = new JButton("Принять");
        JButton cancelButton = new JButton("Отмена");
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //get doctor
                if (doctorsList == null) {
                    statusLabel.setText("Неверный ввод");
                    statusLabel.repaint();
                    setVisible(false);
                    dispose();
                } else {
                    Doctor doctor = doctorsList.get(doctors.getSelectedIndex());
                    //get patient
                    String name = patientName.getText();
                    String surname = patientSurname.getText();
                    String district = patientDistrict.getText();
                    String diagnosis = patientDiagnosis.getText();
                    String insuranceId = patientInsuranceId.getText();
                    //prepare search mask
                    Patient patient;
                    if (!insuranceId.equals("") && !insuranceId.equals("     ")) {
                        patient = new Patient(null, null, null, null, Integer.parseInt(insuranceId));
                    } else {
                        if (!name.equals("")) {
                            patient = new Patient(name, null, null, null, 0);
                        } else if (!surname.equals("")) {
                            patient = new Patient(null, surname, null, null, 0);
                        } else if (!district.equals("")) {
                            patient = new Patient(null, null, district, null, 0);
                        } else if (!diagnosis.equals("")) {
                            patient = new Patient(null, null, null, diagnosis, 0);
                        } else {
                            patient = null;
                        }
                    }
                    //search by mask
                    ArrayList<Patient> searchResult =
                            (ArrayList<Patient>) client.sendRequest(REQUEST_SEARCH_PATIENT, patient);
                    patient = null;
                    if (searchResult == null || searchResult.size() == 0 || searchResult.size() > 1) {
                        System.out.println("search result size=" + searchResult.size());
                        JOptionPane.showMessageDialog(AddRecordDialog.this,
                                "Неверный запрос по пациенту");
                    } else {
                        patient = searchResult.get(0);
                    }
                    //get date
                    SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");
                    try {
                        Date date1 = f.parse(date.getText());
                        if (patient != null && doctor != null && date1 != null) {
                            Record record = new Record(doctor, patient, date1);
                            if ((Integer) client.sendRequest(REQUEST_ADD_RECORD, record) == OK) {
                                statusLabel.setText("Регистрационная запись успешно добавлена");
                                statusLabel.repaint();
                                form.recordsTableUpdate();
                                setVisible(false);
                                dispose();
                            } else {
                                JOptionPane.showMessageDialog(AddRecordDialog.this,
                                        "Ошибка при попытке записи в базу");
                            }
                        }
                    } catch (ParseException e1) {
                        JOptionPane.showMessageDialog(AddRecordDialog.this,
                                "Неверный ввод даты");
                        //e1.printStackTrace();

                    }
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });

        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.add(patientPanel);
        pane.add(datePanel);
        pane.add(doctorPanel);
        pane.add(new JLabel());
        pane.add(new JSeparator(JSeparator.HORIZONTAL));
        pane.add(buttonsPanel);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        pack();
        setVisible(true);
    }
}
