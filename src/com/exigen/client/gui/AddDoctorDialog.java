package com.exigen.client.gui;

import com.alee.laf.WebLookAndFeel;
import com.exigen.client.Client;
import com.exigen.entity.Doctor;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import static com.exigen.util.ProtocolCodes.*;

public class AddDoctorDialog extends JDialog implements Runnable{

    private JTextField name;
    private JTextField surname;
    private JFormattedTextField room;
    private JTextField specialization;

    private Client client;
    private JLabel statusLabel;
    private MainForm form;

    public AddDoctorDialog(MainForm form, JLabel statusLabel) {
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

        name = new JTextField();
        surname = new JTextField();
        MaskFormatter f = null;
        //formatter for room text field input
        try {
            f = new MaskFormatter("##");
            f.setValidCharacters("0123456789");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        room = new JFormattedTextField(f);
        specialization = new JTextField();

        JLabel nameLabel = new JLabel("Имя");
        JLabel surnameLabel = new JLabel("Фамилия");
        JLabel roomLabel = new JLabel("Кабинет");
        JLabel specializationLabel = new JLabel("Специальность");

        JButton okButton = new JButton("Принять");
        JButton cancelButton = new JButton("Отмена");

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(6, 2, 10, 10));
        panel.add(name);
        panel.add(nameLabel);
        panel.add(surname);
        panel.add(surnameLabel);
        panel.add(room);
        panel.add(roomLabel);
        panel.add(specialization);
        panel.add(specializationLabel);
        panel.add(new JLabel());
        panel.add(new JLabel());
        panel.add(okButton);
        panel.add(cancelButton);

        pane.add(panel, BorderLayout.CENTER);
        JLabel label = new JLabel("Добавить доктора");
        label.setHorizontalAlignment(JLabel.CENTER);
        pane.add(label, BorderLayout.NORTH);
        pane.add(new JLabel(), BorderLayout.SOUTH);
        pane.add(new JLabel(), BorderLayout.WEST);
        pane.add(new JLabel(), BorderLayout.EAST);

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Doctor doctor;
                String name = AddDoctorDialog.this.name.getText();
                String surname = AddDoctorDialog.this.surname.getText();
                int room = Integer.parseInt(AddDoctorDialog.this.room.getText());
                String specialization = AddDoctorDialog.this.specialization.getText();
                if (name.length() != 0 && surname.length() != 0 &&
                        AddDoctorDialog.this.room.getText().length() != 0 &&
                        specialization.length() != 0) {
                    doctor = new Doctor(name, surname, room, specialization);
                    if ((Integer) client.sendRequest(REQUEST_ADD_DOCTOR, doctor) == OK) {
                        statusLabel.setText("Доктор был успешно добавлен");
                        statusLabel.repaint();
                    } else {
                        statusLabel.setText("Доктор не был добавлен");
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
