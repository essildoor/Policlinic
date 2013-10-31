package com.exigen.client.gui;

import com.alee.laf.WebLookAndFeel;
import com.exigen.client.Client;
import com.exigen.entity.Doctor;
import com.exigen.entity.Patient;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;

public class DoctorSearchDialog extends JDialog implements Runnable{

    private Client client;
    private JLabel statusLabel;
    private MainForm form;
    private JTextField name;
    private JTextField surname;
    private JFormattedTextField room;
    private JTextField specialization;

    public DoctorSearchDialog(MainForm form, JLabel statusLabel) {
        this.form = form;
        this.statusLabel = statusLabel;
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
        JLabel title = new JLabel("Поиск врачей");
        title.setHorizontalAlignment(JLabel.CENTER);
        JLabel nameLabel = new JLabel("Имя");
        JLabel surnameLabel = new JLabel("Фамилия");
        JLabel roomLabel = new JLabel("Кабинет");
        JLabel specializationLabel = new JLabel("Специальность");
        MaskFormatter f = null;
        try {
            f = new MaskFormatter("##");
            f.setValidCharacters("0123456789");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        name = new JTextField();
        surname = new JTextField();
        room = new JFormattedTextField(f);
        specialization = new JTextField();

        JButton searchButton = new JButton("Поиск");
        JButton cancelButton = new JButton("Отмена");

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
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
                String name = DoctorSearchDialog.this.name.getText();
                String surname = DoctorSearchDialog.this.surname.getText();
                String tmp = DoctorSearchDialog.this.room.getText();
                String specialization = DoctorSearchDialog.this.specialization.getText();
                int room = 0;
                if (!tmp.equals("") && !tmp.equals("  "))
                    room = Integer.parseInt(tmp);
                Doctor searchMask = new Doctor(name, surname, room, specialization);
                form.setCurrentDoctorSearchMask(searchMask);
                form.updateDoctorsTable();
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
