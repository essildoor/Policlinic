package com.exigen.client.gui;

import com.alee.laf.WebLookAndFeel;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.exigen.util.ProtocolCodes.*;


public class MainForm extends JFrame {

    private ArrayList<Patient> patientsList;
    private ArrayList<Doctor> doctorsList;
    private ArrayList<Record> recordsList;
    private JLabel statusLabel = new JLabel();
    private Client client;
    private JTable recordsTable;
    private JTable doctorsTable;
    private JTable patientsTable;
    private ClientConfig cfg;
    private Logger logger;

    private Patient currentPatientSearchMask; //needs to store current table view
    private Doctor currentDoctorSearchMask;   // ^~

    protected void setCurrentPatientSearchMask(Patient currentPatientSearchMask) {
        this.currentPatientSearchMask = currentPatientSearchMask;
    }

    protected void setCurrentDoctorSearchMask(Doctor currentDoctorSearchMask) {
        this.currentDoctorSearchMask = currentDoctorSearchMask;
    }

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
                e.printStackTrace();
            }
        }
    }

    /**
     * updates all tables from server
     */
    protected void tablesUpdate() {
        ArrayList tables = (ArrayList) client.sendRequest(REQUEST_ALL_LISTS, null);
        patientsList = (ArrayList<Patient>) tables.get(0);
        doctorsList = (ArrayList<Doctor>) tables.get(1);
        recordsList = (ArrayList<Record>) tables.get(2);
        patientsTable.setModel(new PatientsTableModel(patientsList));
        doctorsTable.setModel(new DoctorsTableModel(doctorsList));
        recordsTable.setModel(new RecordsTableModel(recordsList));
    }

    /**
     * Updates patients list from server regarding search param
     * selects all list if param is null
     *
     */
    protected void updatePatientsTable() {
        System.out.println(currentPatientSearchMask);
        patientsList = (ArrayList<Patient>) client.sendRequest(REQUEST_PATIENTS_LIST,
                currentPatientSearchMask);
        patientsTable.setModel(new PatientsTableModel(patientsList));
    }

    /**
     * Updates doctors list from server regarding search param
     * selects all list if param is null
     *
     */
    protected void updateDoctorsTable() {
        doctorsList = (ArrayList<Doctor>) client.sendRequest(REQUEST_DOCTORS_LIST,
                currentDoctorSearchMask);
        doctorsTable.setModel(new DoctorsTableModel(doctorsList));
    }

    /**
     * Updates records list from server
     */
    protected void recordsTableUpdate() {
        recordsTable.setModel(
                new RecordsTableModel(
                        (ArrayList<Record>) client.sendRequest(REQUEST_RECORDS_LIST, null)
                )
        );
    }

    public MainForm(Client client) {
        super("Поликлиника v0.8");
        logger = ClientLogger.getInstance().getLogger();
        this.client = client;
        patientsList = new ArrayList<Patient>();
        doctorsList = new ArrayList<Doctor>();
        recordsList = new ArrayList<Record>();
        cfg = ClientConfig.getInstance();
        patientsTable = new JTable(new PatientsTableModel(patientsList));
        doctorsTable = new JTable(new DoctorsTableModel(doctorsList));
        recordsTable = new JTable(new RecordsTableModel(recordsList));
        //shows all table elements element by default
        currentPatientSearchMask = null;
        currentDoctorSearchMask = null;
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
        //setting up tabs
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
        menuBar.add(fileMenu);

        JMenuItem connectItem = new JMenuItem("Подключиться");
        JMenuItem exitItem = new JMenuItem("Выход");
        fileMenu.add(connectItem);
        fileMenu.add(exitItem);

        frame.setJMenuBar(menuBar);
    }

    /**
     * setting up tab with records table and ect
     *
     * @param parent parent container
     */
    private void setupRecordsTab(Container parent) {

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        //sets header click sorting
        patientsTable.setAutoCreateRowSorter(true);
        JScrollPane recordsScrollPane = new JScrollPane(recordsTable);
        JButton addRecordButton = new JButton();
        JButton deleteRecordButton = new JButton();

        ImageIcon addIcon = createIcon("addIcon32.png");
        ImageIcon deleteIcon = createIcon("deleteIcon32.png");

        addRecordButton.setIcon(addIcon);
        deleteRecordButton.setIcon(deleteIcon);

        addRecordButton.setToolTipText("Add new record");
        deleteRecordButton.setToolTipText("Delete record");

        buttonsPanel.add(addRecordButton);
        buttonsPanel.add(deleteRecordButton);

        parent.setLayout(new BorderLayout());
        parent.add(buttonsPanel, BorderLayout.NORTH);
        parent.add(recordsScrollPane, BorderLayout.CENTER);

        addRecordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new AddRecordDialog(MainForm.this, statusLabel));
            }
        });
        deleteRecordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ((Integer) client.sendRequest(REQUEST_DELETE_RECORD,
                        recordsList.get(recordsTable.getSelectedRow())) == OK) {
                    recordsTableUpdate();
                    statusLabelMessage("Запись удалена");
                    logger.log(Level.FINEST, "Record successfully deleted");
                } else {
                    recordsTableUpdate();
                    logger.log(Level.WARNING, "Couldn't delete record");
                    statusLabelMessage("Запись не удалена!");
                }
            }
        });

    }

    protected static ImageIcon createIcon(String path) {
        URL imgURL = MainForm.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {

            return null;
        }
    }

    /**
     * setting up tab with doctors table and ect
     *
     * @param parent parent container
     */
    private void setupDoctorsTab(Container parent) {
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        //sets header click sorting
        patientsTable.setAutoCreateRowSorter(true);
        JScrollPane doctorsScrollPane = new JScrollPane(doctorsTable);
        JButton addDoctorButton = new JButton();
        JButton editDoctorButton = new JButton();
        JButton deleteDoctorButton = new JButton();
        JButton searchDoctorButton = new JButton();
        JButton viewAllButton = new JButton("View all");

        ImageIcon addIcon = createIcon("addIcon32.png");
        ImageIcon editIcon = createIcon("editIcon32.png");
        ImageIcon deleteIcon = createIcon("deleteIcon32.png");
        ImageIcon searchIcon = createIcon("searchIcon32.png");

        addDoctorButton.setIcon(addIcon);
        editDoctorButton.setIcon(editIcon);
        deleteDoctorButton.setIcon(deleteIcon);
        searchDoctorButton.setIcon(searchIcon);

        addDoctorButton.setToolTipText("Add doctor");
        editDoctorButton.setToolTipText("Edit doctor");
        deleteDoctorButton.setToolTipText("Delete doctor");
        searchDoctorButton.setToolTipText("Search doctors");

        buttonsPanel.add(addDoctorButton);
        buttonsPanel.add(editDoctorButton);
        buttonsPanel.add(deleteDoctorButton);
        buttonsPanel.add(searchDoctorButton);
        buttonsPanel.add(viewAllButton);

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
                    //updates table according current search mask
                    updateDoctorsTable();
                    statusLabelMessage("Доктор удален");
                    logger.log(Level.FINEST, "Doctor successfully deleted");
                } else {
                    updateDoctorsTable();
                    logger.log(Level.WARNING, "Couldn't delete doctor");
                    statusLabelMessage("Доктор не удален!");
                }
            }
        });

        viewAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPatientSearchMask = null;
                updateDoctorsTable();
                statusLabel.setText("Настройки поиска сброшены");
                statusLabel.repaint();
            }
        });
    }

    /**
     * setting up patients tab with table and ect
     *
     * @param parent parent container
     */
    private void setupPatientsTab(final Container parent) {
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        //sets header click sorting
        patientsTable.setAutoCreateRowSorter(true);
        JScrollPane patientsScrollPane = new JScrollPane(patientsTable);
        JButton addPatientButton = new JButton();
        JButton editPatientButton = new JButton();
        JButton deletePatientButton = new JButton();
        JButton searchPatientButton = new JButton();
        JButton viewAllButton = new JButton("View all");

        ImageIcon addIcon = createIcon("addIcon32.png");
        ImageIcon editIcon = createIcon("editIcon32.png");
        ImageIcon deleteIcon = createIcon("deleteIcon32.png");
        ImageIcon searchIcon = createIcon("searchIcon32.png");

        addPatientButton.setIcon(addIcon);
        editPatientButton.setIcon(editIcon);
        deletePatientButton.setIcon(deleteIcon);
        searchPatientButton.setIcon(searchIcon);

        addPatientButton.setToolTipText("Add patient");
        editPatientButton.setToolTipText("Edit patient");
        deletePatientButton.setToolTipText("Delete patient");
        searchPatientButton.setToolTipText("Search patient");

        buttonsPanel.add(addPatientButton);
        buttonsPanel.add(editPatientButton);
        buttonsPanel.add(deletePatientButton);
        buttonsPanel.add(searchPatientButton);
        buttonsPanel.add(viewAllButton);

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
                    //updates table according current search mask
                    updatePatientsTable();
                    logger.log(Level.FINEST, "Patient deleted");
                    statusLabelMessage("Пациент удален");
                } else {
                    updatePatientsTable();
                    logger.log(Level.WARNING, "Couldn't delete patient");
                    statusLabelMessage("Ошибка при попытке удаления пациента");
                }
            }
        });

        viewAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPatientSearchMask = null;
                updatePatientsTable();
                statusLabel.setText("Настройки поиска сброшены");
                statusLabel.repaint();
            }
        });

        searchPatientButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new PatientSearchDialog(MainForm.this, statusLabel));
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
        WebLookAndFeel.install();
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
        frame.logger.log(Level.FINEST, "Main form initialization completed");
        frame.pack();
        frame.setVisible(true);
        //starts daemon thread which syncs table data from server
        /*BackgroundTablesSyncDaemon syncDaemon = frame.new BackgroundTablesSyncDaemon();
        syncDaemon.setDaemon(true);
        syncDaemon.start();*/
    }
}