package com.exigen.client.gui;

import com.exigen.client.ClientConfig;
import com.exigen.entity.Doctor;
import com.exigen.entity.DoctorSpecialization;
import com.exigen.entity.Patient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class MainForm extends JFrame {

    public MainForm() {
        super("Поликлиника v0.1");
    }

    //setting up tabbed pane with 3 tabs: patients, doctors and records
    private static void setupTabbedPane(Container parent) {
        Font font = new Font("Verdana", Font.PLAIN, 10);
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(font);

        JPanel patientsPanel = new JPanel();
        JPanel doctorsPanel = new JPanel();
        JPanel recordsPanel = new JPanel();

        setupPatientsTab(patientsPanel);
        setupDoctorsTab(doctorsPanel);

        tabbedPane.addTab("Пациенты", patientsPanel);
        tabbedPane.addTab("Врачи", doctorsPanel);
        tabbedPane.addTab("Регистратура", recordsPanel);
        parent.add(tabbedPane);
    }

    //setting up main menu
    private static void setupMenuPanel(JFrame frame) {
        JMenuBar menuBar = new JMenuBar();
        //File menu and its items
        JMenu fileMenu = new JMenu("Файл");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        JMenuItem connectItem = new JMenuItem("Подключиться");
        fileMenu.add(connectItem);
        //Help menu and its items
        JMenu helpMenu = new JMenu("Справка");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(helpMenu);

        JMenuItem helpItem = new JMenuItem("Справка");
        helpMenu.add(helpItem);

        frame.setJMenuBar(menuBar);
    }

    private static void setupDoctorsTab(Container parent) {
        final ArrayList<Doctor> doctorstest = new ArrayList<Doctor>();
        doctorstest.add(new Doctor("Юрий", "Попов", 12, DoctorSpecialization.PROCTOLOGIST));
        doctorstest.add(new Doctor("Елена", "Малышева", 21, DoctorSpecialization.THERAPIST));
        doctorstest.add(new Doctor("Генадий", "Малахов", 24, DoctorSpecialization.SURGEON));

        DoctorsTableModel model = new DoctorsTableModel(doctorstest);

        JPanel buttonsPanel = new JPanel();
        final JTable doctorsTable = new JTable(model);
        JButton addDoctorButton = new JButton("Добавить врача");
        JButton deleteDoctorButton = new JButton("Удалить врача");
        JButton searchButtonButton = new JButton("Поиск по врачам");

        buttonsPanel.add(addDoctorButton);
        buttonsPanel.add(deleteDoctorButton);
        buttonsPanel.add(searchButtonButton);

        final JScrollPane scrollPane = new JScrollPane(doctorsTable);

        parent.setLayout(new BorderLayout());
        parent.add(buttonsPanel, BorderLayout.NORTH);
        parent.add(scrollPane, BorderLayout.CENTER);

        deleteDoctorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < doctorstest.size(); i++) {
                    if (doctorsTable.isRowSelected(i)) {
                        doctorstest.remove(i);
                        scrollPane.repaint();
                    }
                }
            }
        });
    }

    private static void setupPatientsTab(Container parent) {
        final ArrayList<Patient> patientstest = new ArrayList<Patient>();
        patientstest.add(new Patient("Андрей", "Пупкин", "Выборгский", "Понос"));
        patientstest.add(new Patient("Вова", "Галимов", "Центральный", "Открытый перелом уха"));
        patientstest.add(new Patient("Вася", "Печенькин", "Калининский", "Запор"));
        patientstest.add(new Patient("Вахтанг", "Джигитов", "Невский", "Головная боль"));
        patientstest.add(new Patient("Самуил", "Картошкин", "Выборгский", "Вывих носа"));

        PatientsTableModel model = new PatientsTableModel(patientstest);

        JPanel buttonsPanel = new JPanel();
        final JTable patientsTable = new JTable(model);
        JButton addPatientButton = new JButton("Добавить пациента");
        JButton deletePatientButton = new JButton("Удалить пациента");
        JButton searchPatientButton = new JButton("Поиск пациента");

        buttonsPanel.add(addPatientButton);
        buttonsPanel.add(deletePatientButton);
        buttonsPanel.add(searchPatientButton);

        final JScrollPane scrollPane = new JScrollPane(patientsTable);

        parent.setLayout(new BorderLayout());
        parent.add(buttonsPanel, BorderLayout.NORTH);
        parent.add(scrollPane, BorderLayout.CENTER);

        deletePatientButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < patientstest.size(); i++) {
                    if (patientsTable.isRowSelected(i)) {
                        patientstest.remove(i);
                        scrollPane.repaint();
                    }
                }
            }
        });
    }

    public static void setupAndShowGUI() {
        JFrame frame = new MainForm();
        ClientConfig cfg = ClientConfig.getInstance();
        int frameWidth = cfg.getMainFrameDefaultWidth();
        int frameHeight = cfg.getMainFrameDefaultHeight();
        frame.setPreferredSize(new Dimension(frameWidth, frameHeight));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(
                (int) ((screenSize.getWidth() - frameWidth) / 2),
                (int) ((screenSize.getHeight() - frameHeight) / 2));
        setupTabbedPane(frame.getContentPane());
        setupMenuPanel(frame);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        setupAndShowGUI();
    }
}