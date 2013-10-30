package com.exigen.client.gui;

import com.alee.laf.WebLookAndFeel;
import com.exigen.client.Client;
import com.exigen.entity.Patient;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.ArrayList;

public class PatientSearchDialog extends JDialog implements Runnable {

    private Client client;
    private JLabel statusLabel;
    private MainForm form;
    private ArrayList<Patient> patientsList;
    private JTextField name;
    private JTextField surname;
    private JTextField district;
    private JTextField diagnosis;
    private JFormattedTextField insuranceId;



    public PatientSearchDialog(MainForm form, JLabel statusLabel) {
        this.statusLabel = statusLabel;
        this.form = form;
        client = form.getClient();
    }

    @Override
    public void run() {
        WebLookAndFeel.install();
        int width = 400;
        int height = 300;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(
                (int) ((screenSize.getWidth() - width) / 2),
                (int) ((screenSize.getHeight() - height) / 2));
        setPreferredSize(new Dimension(width, height));
        JLabel title = new JLabel("Поиск пациентов");
        JLabel nameLabel = new JLabel("Имя");
        JLabel surnameLabel = new JLabel("Фамилия");
        JLabel districtLabel = new JLabel("Участок");
        JLabel diagnosisLabel = new JLabel("Диагноз");
        JLabel insuranceIdLabel = new JLabel("Номер полиса");
        name = new JTextField();
        surname = new JTextField();
        district = new JTextField();
        diagnosis = new JTextField();
        MaskFormatter f = null;
        try {
            f = new MaskFormatter("#####");
            f.setValidCharacters("0123456789");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        insuranceId = new JFormattedTextField(f);

        JButton searchButton = new JButton("Поиск");
        JButton cancelButton = new JButton("Отмена");

        JPanel panel = new JPanel(new GridLayout(7, 2, 10 ,10));

        panel.add(name);
        panel.add(nameLabel);
        panel.add(surname);
        panel.add(surnameLabel);
        panel.add(district);
        panel.add(districtLabel);
        panel.add(diagnosis);
        panel.add(diagnosisLabel);
        panel.add(insuranceId);
        panel.add(insuranceIdLabel);
        panel.add(new JLabel());
        panel.add(new JLabel());
        panel.add(searchButton);
        panel.add(cancelButton);

        Container pane = getContentPane();
        pane.setLayout(new BorderLayout(10, 10));

        pane.add(title, BorderLayout.NORTH);
        pane.add(panel, BorderLayout.CENTER);
        pane.add(new JLabel(), BorderLayout.SOUTH);
        pane.add(new JLabel(), BorderLayout.EAST);
        pane.add(new JLabel(), BorderLayout.WEST);

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = PatientSearchDialog.this.name.getText();
                String surname = PatientSearchDialog.this.surname.getText();
                String district = PatientSearchDialog.this.district.getText();
                String diagnosis = PatientSearchDialog.this.diagnosis.getText();
                int insuranceId = Integer.parseInt(PatientSearchDialog.this.insuranceId.getText());
                Patient searchMask = new Patient(name, surname, district, diagnosis, insuranceId);
                form.setCurrentPatientSearchMask(searchMask);
                form.updatePatientsTable();
                statusLabel.setText("Поиск выполнен");
                statusLabel.repaint();
                setVisible(false);
                dispose();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });

        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setVisible(true);
    }
}
