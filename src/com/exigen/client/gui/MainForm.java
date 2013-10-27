package com.exigen.client.gui;

import com.exigen.client.Client;
import com.exigen.client.ClientConfig;
import com.exigen.client.ClientLogger;
import com.exigen.entity.Doctor;
import com.exigen.entity.Patient;
import com.exigen.entity.Record;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.exigen.util.ProtocolCodes.*;


public class MainForm extends JFrame {

    private final String ADD_BUTTON_LABEL = "Add";
    private final String DELETE_BUTTON_LABEL = "Remove";
    private final String SEARCH_BUTTON_LABEL = "Search";
    private ArrayList<Patient> patientsList;
    private ArrayList<Doctor> doctorsList;
    private ArrayList<Record> recordsList;
    private JLabel statusLabel = new JLabel();
    private Logger logger;
    private Client client;
    private JTable recordsTable;
    private JTable doctorsTable;
    private JTable patientsTable;
    private ClientConfig cfg;

    protected Client getClient() {
        return this.client;
    }

    /**
     * background thread
     * performs patients, doctors and records lists update with a specified in client config rate
     * by default rate is 30 sec
     */
    @SuppressWarnings("Unchecked")
    class BackgroundTablesSyncDaemon extends Thread {

        private int syncRate;

        @Override
        public void run() {
            syncRate = cfg.getMainFrameTablesSyncRateMs();
            try {
                while (!isInterrupted()) {
                    Thread.sleep(syncRate);
                    tablesUpdate();
                }
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "main form daemon interrupted");
                System.out.println("main form daemon interrupted");
                e.printStackTrace();
            }
        }
    }

    protected void tablesUpdate() {
        ArrayList tables = (ArrayList) client.sendRequest(REQUEST_ALL_LISTS, null);
        patientsList = (ArrayList<Patient>) tables.get(0);
        doctorsList = (ArrayList<Doctor>) tables.get(1);
        recordsList = (ArrayList<Record>) tables.get(2);
        patientsTable.setModel(new PatientsTableModel(patientsList));
        doctorsTable.setModel(new DoctorsTableModel(doctorsList));
        recordsTable.setModel(new RecordsTableModel(recordsList));
    }

    public MainForm(Client client) {
        super("Поликлиника v0.1");
        logger = ClientLogger.getInstance().getLogger();
        this.client = client;
        patientsList = new ArrayList<Patient>();
        doctorsList = new ArrayList<Doctor>();
        recordsList = new ArrayList<Record>();
        cfg = ClientConfig.getInstance();
        patientsTable = new JTable(new PatientsTableModel(patientsList));
        doctorsTable = new JTable(new DoctorsTableModel(doctorsList));
        recordsTable = new JTable(new RecordsTableModel(recordsList));

    }

    /**
     * setting up tabbed pane with 3 tabs: patients, doctors and records
     *
     * @param parent parent container
     */
    public void setupTabbedPane(Container parent) {
        Font font = new Font("Verdana", Font.PLAIN, 10);
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(font);

        JPanel patientsPanel = new JPanel();
        JPanel doctorsPanel = new JPanel();
        JPanel recordsPanel = new JPanel();
        JStatusBar statusBar = new JStatusBar();
        statusLabel.setFont(font);
        statusBar.add(statusLabel);
        //inits tables data before form opening
        tablesUpdate();
        //seting up tabs
        setupPatientsTab(patientsPanel);
        setupDoctorsTab(doctorsPanel);
        setupRecordsTab(recordsPanel);
        //adding tabs to the parent pane
        tabbedPane.addTab("Пациенты", patientsPanel);
        tabbedPane.addTab("Врачи", doctorsPanel);
        tabbedPane.addTab("Регистратура", recordsPanel);
        parent.add(tabbedPane, BorderLayout.CENTER);
        parent.add(statusBar, BorderLayout.SOUTH);
    }

    /**
     * setting up main menu bar
     *
     * @param frame main frame reference
     */
    private void setupMenuPanel(JFrame frame) {
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

    /**
     * setting up tab with records table and ect
     *
     * @param parent parent container
     */
    private void setupRecordsTab(Container parent) {

        JPanel buttonsPanel = new JPanel();

        JScrollPane recordsScrollPane = new JScrollPane(recordsTable);
        JButton addRecordButton = new JButton(ADD_BUTTON_LABEL);
        JButton deleteRecordButton = new JButton(DELETE_BUTTON_LABEL);
        JButton searchButtonButton = new JButton(SEARCH_BUTTON_LABEL);

        buttonsPanel.add(addRecordButton);
        buttonsPanel.add(deleteRecordButton);
        buttonsPanel.add(searchButtonButton);

        parent.setLayout(new BorderLayout());
        parent.add(buttonsPanel, BorderLayout.NORTH);
        parent.add(recordsScrollPane, BorderLayout.CENTER);

        deleteRecordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }

    /**
     * setting up tab with doctors table and ect
     *
     * @param parent parent container
     */
    private void setupDoctorsTab(Container parent) {
        JPanel buttonsPanel = new JPanel();
        JScrollPane doctorsScrollPane = new JScrollPane(doctorsTable);
        JButton addDoctorButton = new JButton(ADD_BUTTON_LABEL);
        JButton deleteDoctorButton = new JButton(DELETE_BUTTON_LABEL);
        JButton searchButtonButton = new JButton(SEARCH_BUTTON_LABEL);

        buttonsPanel.add(addDoctorButton);
        buttonsPanel.add(deleteDoctorButton);
        buttonsPanel.add(searchButtonButton);

        parent.setLayout(new BorderLayout());
        parent.add(buttonsPanel, BorderLayout.NORTH);
        parent.add(doctorsScrollPane, BorderLayout.CENTER);

        addDoctorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new AddDoctorDialog(MainForm.this, statusLabel));
            }
        });

        deleteDoctorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ((Integer) client.sendRequest(REQUEST_DELETE_DOCTOR,
                        doctorsList.get(doctorsTable.getSelectedRow())) == OK) {
                    tablesUpdate();
                    statusLabelMessage("Doctor deleted");
                } else {
                    tablesUpdate();
                    statusLabelMessage("error");
                }
            }
        });
    }

    /**
     * setting up patients tab with table and ect
     *
     * @param parent parent container
     */
    private void setupPatientsTab(final Container parent) {
        JPanel buttonsPanel = new JPanel();
        JScrollPane patientsScrollPane = new JScrollPane(patientsTable);
        JButton addPatientButton = new JButton(ADD_BUTTON_LABEL);
        JButton deletePatientButton = new JButton(DELETE_BUTTON_LABEL);
        JButton searchPatientButton = new JButton(SEARCH_BUTTON_LABEL);

        buttonsPanel.add(addPatientButton);
        buttonsPanel.add(deletePatientButton);
        buttonsPanel.add(searchPatientButton);

        parent.setLayout(new BorderLayout());
        parent.add(buttonsPanel, BorderLayout.NORTH);
        parent.add(patientsScrollPane, BorderLayout.CENTER);

        addPatientButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new AddPatientDialog(MainForm.this, statusLabel));
            }
        });

        deletePatientButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ((Integer) client.sendRequest(REQUEST_DELETE_PATIENT,
                        patientsList.get(patientsTable.getSelectedRow())) == OK) {
                    tablesUpdate();
                    statusLabelMessage("Patient deleted");
                } else {
                    tablesUpdate();
                    statusLabelMessage("error");
                }
            }
        });
    }

    private void statusLabelMessage(String message) {
        statusLabel.setText(message);
        statusLabel.repaint();
    }

    /**
     * setting up all components to main frame
     */
    public static void setupAndShowGUI(Client client) {
        final MainForm frame = new MainForm(client);
        ClientConfig cfg = ClientConfig.getInstance();
        int frameWidth = cfg.getMainFrameDefaultWidth();
        int frameHeight = cfg.getMainFrameDefaultHeight();
        frame.setPreferredSize(new Dimension(frameWidth, frameHeight));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(
                (int) ((screenSize.getWidth() - frameWidth) / 2),
                (int) ((screenSize.getHeight() - frameHeight) / 2));
        frame.setLayout(new BorderLayout());
        frame.setupTabbedPane(frame.getContentPane());
        frame.setupMenuPanel(frame);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        //starts daemon thread which syncs table data from server
        /*BackgroundTablesSyncDaemon syncDaemon = frame.new BackgroundTablesSyncDaemon();
        syncDaemon.setDaemon(true);
        syncDaemon.start();*/
    }
}