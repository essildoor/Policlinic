package com.exigen.client.gui;

import com.exigen.client.Client;
import com.exigen.entity.Doctor;
import com.exigen.entity.Patient;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;

@SuppressWarnings("Unchecked")
public class AddRecordDialog extends JDialog implements Runnable {

    private ArrayList<Patient> patients;
    private ArrayList<Doctor> doctors;

    private Client client;
    private MainForm form;
    private JLabel statusLabel;

    //user choice
    private Patient patient;
    private Doctor doctor;
    private String doctorSpecialization;
    private Date date;

    public AddRecordDialog(/*MainForm form, JLabel statusLabel*/) {
        /*this.form = form;
        this.statusLabel = statusLabel;
        client = form.getClient();
        patients = (ArrayList<Patient>) client.sendRequest(REQUEST_PATIENTS_LIST, null);*/
    }

    @Override
    public void run() {
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

        //patient section setup
        JPanel dataPanel1 = new JPanel(new GridLayout(4, 2, 10, 10));
        JPanel buttonPanel1 = new JPanel(new FlowLayout());
        JButton specifyPatientButton = new JButton("Pick a patient");
        JLabel patientName = new JLabel();
        JLabel patientSurname = new JLabel();
        JLabel patientDistrict = new JLabel();
        JLabel patientDiagnosis = new JLabel();
        JLabel patientNameLabel = new JLabel("Name:");
        JLabel patientSurnameLabel = new JLabel("Surname:");
        JLabel patientDistrictLabel = new JLabel("District:");
        JLabel patientDiagnosisLabel = new JLabel("Diagnosis:");

        dataPanel1.add(patientNameLabel);
        dataPanel1.add(patientName);
        dataPanel1.add(patientSurnameLabel);
        dataPanel1.add(patientSurname);
        dataPanel1.add(patientDistrictLabel);
        dataPanel1.add(patientDistrict);
        dataPanel1.add(patientDiagnosisLabel);
        dataPanel1.add(patientDiagnosis);

        buttonPanel1.add(specifyPatientButton);
        specifyPatientButton.setFocusPainted(false);
        specifyPatientButton.setPreferredSize(new Dimension(150, 30));
        buttonPanel1.setAlignmentX(CENTER_ALIGNMENT);
        dataPanel1.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Patient details"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        JPanel viewPanel1 = new JPanel(new GridLayout(1, 1));
        viewPanel1.add(dataPanel1);
        patientPanel.add(viewPanel1);
        patientPanel.add(buttonPanel1);

        //date section setup
        JPanel dataPanel2 = new JPanel(new GridLayout(1, 2, 10, 10));
        JPanel buttonPanel2 = new JPanel(new FlowLayout());
        JButton specifyDateButton = new JButton("Specify date");
        JLabel date = new JLabel();
        JLabel dateLabel = new JLabel("Date:");

        buttonPanel2.add(specifyDateButton);
        specifyDateButton.setPreferredSize(new Dimension(150, 30));
        specifyDateButton.setFocusPainted(false);

        dataPanel2.add(dateLabel);
        dataPanel2.add(date);
        dataPanel2.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Date details"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        buttonPanel2.setAlignmentX(CENTER_ALIGNMENT);
        JPanel viewPanel2 = new JPanel(new GridLayout(1, 1));
        viewPanel2.add(dataPanel2);
        datePanel.add(viewPanel2);
        datePanel.add(buttonPanel2);

        //doctor section setup
        JPanel dataPanel3 = new JPanel(new GridLayout(4, 2, 10, 10));
        JPanel buttonPanel3 = new JPanel(new FlowLayout());
        JButton specifyDoctorButton = new JButton("Specify doctor");
        JLabel doctorName = new JLabel();
        JLabel doctorSurname = new JLabel();
        JLabel doctorRoom = new JLabel();
        JLabel doctorSpecialization = new JLabel();
        JLabel doctorNameLabel = new JLabel("name:");
        JLabel doctorSurnameLabel = new JLabel("Surname:");
        JLabel doctorRoomLabel = new JLabel("Room:");
        JLabel doctorSpecializationLabel = new JLabel("Specialization:");

        buttonPanel3.add(specifyDoctorButton);
        specifyDoctorButton.setFocusPainted(false);
        specifyDoctorButton.setPreferredSize(new Dimension(150, 30));

        dataPanel3.add(doctorNameLabel);
        dataPanel3.add(doctorName);
        dataPanel3.add(doctorSurnameLabel);
        dataPanel3.add(doctorSurname);
        dataPanel3.add(doctorRoomLabel);
        dataPanel3.add(doctorRoom);
        dataPanel3.add(doctorSpecializationLabel);
        dataPanel3.add(doctorSpecialization);
        dataPanel3.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Doctor details"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        buttonPanel3.setAlignmentX(CENTER_ALIGNMENT);
        JPanel viewPanel3 = new JPanel(new GridLayout(1, 1));
        viewPanel3.add(dataPanel3);
        doctorPanel.add(viewPanel3);
        doctorPanel.add(buttonPanel3);

        JButton okButton = new JButton("Ok");
        JButton cancelButton = new JButton("Cancel");
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new AddRecordDialog());
    }
}
