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

import static com.exigen.util.ProtocolCodes.OK;
import static com.exigen.util.ProtocolCodes.REQUEST_ADD_PATIENT;

public class AddPatientDialog extends JDialog implements Runnable {

    private JTextField name;
    private JTextField surname;
    private JTextField district;
    private JTextField diagnosis;
    private JFormattedTextField insuranceId;

    private Client client;
    private MainForm form;
    private JLabel statusLabel;

    public AddPatientDialog(MainForm form, JLabel statusLabel) {
        super();
        this.form = form;
        client = form.getClient();
        this.statusLabel = statusLabel;
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
        Container pane = getContentPane();
        pane.setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(width, height));
        MaskFormatter f = null;
        try {
            f = new MaskFormatter("##########");
            f.setValidCharacters("0123456789");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        name = new JTextField();
        surname = new JTextField();
        district = new JTextField();
        diagnosis = new JTextField();
        insuranceId = new JFormattedTextField(f);

        JLabel nameLabel = new JLabel("Имя");
        JLabel surnameLabel = new JLabel("Фамилия");
        JLabel districtLabel = new JLabel("Участок");
        JLabel diagnosisLabel = new JLabel("Диагноз");
        JLabel insuranceIdLabel = new JLabel("Номер полиса");

        JButton okButton = new JButton("Принять");
        JButton cancelButton = new JButton("Отмена");

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(7, 2, 10, 10));
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
        panel.add(okButton);
        panel.add(cancelButton);

        pane.add(panel, BorderLayout.CENTER);
        JLabel label = new JLabel("Добавить пациента");
        label.setHorizontalAlignment(JLabel.CENTER);
        pane.add(label, BorderLayout.NORTH);
        pane.add(new JLabel(), BorderLayout.SOUTH);
        pane.add(new JLabel(), BorderLayout.WEST);
        pane.add(new JLabel(), BorderLayout.EAST);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Patient patient;
                String name = AddPatientDialog.this.name.getText();
                String surname = AddPatientDialog.this.surname.getText();
                String district = AddPatientDialog.this.district.getText();
                String diagnosis = AddPatientDialog.this.diagnosis.getText();
                int insuranceId = Integer.parseInt(AddPatientDialog.this.insuranceId.getText());
                if (name.length() != 0 && surname.length() != 0 && district.length() != 0 &&
                        diagnosis.length() != 0) {
                    patient = new Patient(name, surname, district, diagnosis, insuranceId);
                    if ((Integer) client.sendRequest(REQUEST_ADD_PATIENT, patient) == OK) {
                        statusLabel.setText("Пациент успешно добавлен");
                        statusLabel.repaint();
                    } else {
                        statusLabel.setText("Пациент не был добавлен");
                        statusLabel.repaint();
                    }
                    form.tablesUpdate();
                    setVisible(false);
                    dispose();
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
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setVisible(true);
    }
}
