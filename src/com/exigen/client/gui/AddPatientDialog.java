package com.exigen.client.gui;

import com.exigen.client.Client;
import com.exigen.entity.Patient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static com.exigen.util.ProtocolCodes.OK;
import static com.exigen.util.ProtocolCodes.REQUEST_ADD_PATIENT;

public class AddPatientDialog extends JDialog implements Runnable {

    private JTextField name;
    private JTextField surname;
    private JTextField district;
    private JTextField diagnosis;

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
        int width = 400;
        int height = 300;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(
                (int) ((screenSize.getWidth() - width) / 2),
                (int) ((screenSize.getHeight() - height) / 2));
        Container pane = getContentPane();
        pane.setLayout(new BorderLayout(10, 10));
        setPreferredSize(new Dimension(width, height));

        name = new JTextField();
        surname = new JTextField();
        district = new JTextField();
        diagnosis = new JTextField();

        JLabel nameLabel = new JLabel("Name");
        JLabel surnameLabel = new JLabel("Surname");
        JLabel districtLabel = new JLabel("District");
        JLabel diagnosisLabel = new JLabel("Diagnosis");

        JButton okButton = new JButton("Ok");
        JButton cancelButton = new JButton("Cancel");

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 2, 10, 10));
        panel.add(name);
        panel.add(nameLabel);
        panel.add(surname);
        panel.add(surnameLabel);
        panel.add(district);
        panel.add(districtLabel);
        panel.add(diagnosis);
        panel.add(diagnosisLabel);
        panel.add(new JLabel());
        panel.add(new JLabel());
        panel.add(okButton);
        panel.add(cancelButton);

        pane.add(panel, BorderLayout.CENTER);
        JLabel label = new JLabel("Add new patient");
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
                if (name.length() != 0 && surname.length() != 0 && district.length() != 0 &&
                        diagnosis.length() != 0) {
                    patient = new Patient(name, surname, district, diagnosis);
                    if ((Integer) client.sendRequest(REQUEST_ADD_PATIENT, patient) == OK) {
                        statusLabel.setText("Patient successfully added");
                        statusLabel.repaint();
                    } else {
                        statusLabel.setText("Patient didn't added, log file updated");
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
