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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.exigen.util.ProtocolCodes.*;


public class MainForm extends JFrame implements Runnable {

    private ArrayList<Patient> patientsList;
    private ArrayList<Doctor> doctorsList;
    private ArrayList<Record> recordsList;
    private JLabel statusLabel;
    private JLabel connectionStatusLabel;
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

    @Override
    public void run() {
        WebLookAndFeel.install();
        setupAndShowGUI();
    }

    /**
     * background thread
     * performs patients, doctors and records lists update with a specified in client config rate
     * by default rate is 30 sec
     */
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
                logger.log(Level.FINE, "main form daemon interrupted");
                e.printStackTrace();
            }
        }
    }

    /**
     * updates all tables from server
     */
    protected void tablesUpdate() {
        if (client != null) {
            updatePatientsTable();
            updateDoctorsTable();
            updateRecordsTable();
        }
        patientsTable.updateUI();
        doctorsTable.updateUI();
        recordsTable.updateUI();
    }

    /**
     * Updates patients list from server regarding search param
     * selects all list if param is null
     */
    protected void updatePatientsTable() {
        if (client != null) {
            patientsList = (ArrayList<Patient>) client.sendRequest(REQUEST_PATIENTS_LIST,
                    currentPatientSearchMask);
            patientsTable.setModel(new PatientsTableModel(patientsList));
        }
    }

    /**
     * Updates doctors list from server regarding search param
     * selects all list if param is null
     */
    protected void updateDoctorsTable() {
        if (client != null) {
            doctorsList = (ArrayList<Doctor>) client.sendRequest(REQUEST_DOCTORS_LIST,
                    currentDoctorSearchMask);
            doctorsTable.setModel(new DoctorsTableModel(doctorsList));
        }
    }

    /**
     * Updates records list from server
     */
    protected void updateRecordsTable() {
        if (client != null) {
            recordsList = (ArrayList<Record>) client.sendRequest(REQUEST_RECORDS_LIST, null);
            recordsTable.setModel(new RecordsTableModel(recordsList));
        }
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
        statusLabel = new JLabel();
        connectionStatusLabel = new JLabel();

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
        connectionStatusLabel.setFont(font);
        if (client != null)
            connectionStatusLabel.setText("connected");
        else
            connectionStatusLabel.setText("disconnected");
        statusBar.setLayout(new FlowLayout(FlowLayout.LEFT));
        statusBar.add(connectionStatusLabel);
        statusBar.add(new JSeparator(SwingConstants.VERTICAL));
        statusBar.add(statusLabel);
        //init tables data before form opening
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
    private void setupMenuPanel(final JFrame frame) {
        JMenuBar menuBar = new JMenuBar();
        //File menu and its items
        JMenu fileMenu = new JMenu("Файл");
        menuBar.add(fileMenu);

        JMenuItem connectItem = new JMenuItem("Подключиться");
        JMenuItem exitItem = new JMenuItem("Выход");
        fileMenu.add(connectItem);
        fileMenu.add(exitItem);

        frame.setJMenuBar(menuBar);

        connectItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (client == null) {
                    try {
                        client = new Client(cfg.getHost(), cfg.getPort());
                        tablesUpdate();
                        connectionStatusLabel.setText("connected");
                        connectionStatusLabel.repaint();
                    } catch (IOException e1) {
                        logger.log(Level.WARNING, "Couldn't connect to server on address: " +
                                cfg.getHost() + ":" + cfg.getPort());
                        client = null;
                    }
                }
            }
        });

        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
                frame.dispose();
            }
        });
    }

    /**
     * loads icon from MainForm.class folder
     *
     * @param path path from main class folder
     * @return ImageIcon instance
     */
    protected ImageIcon createIcon(String path) {
        URL imgURL = MainForm.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {

            return null;
        }
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
        JButton refreshButton = new JButton();

        ImageIcon addIcon = createIcon("addRecord.png");
        ImageIcon deleteIcon = createIcon("delete.png");
        ImageIcon refreshIcon = createIcon("refresh.png");

        addRecordButton.setIcon(addIcon);
        deleteRecordButton.setIcon(deleteIcon);
        refreshButton.setIcon(refreshIcon);

        addRecordButton.setToolTipText("Добавить");
        deleteRecordButton.setToolTipText("Удалить");
        refreshButton.setToolTipText("Обновить данные");

        buttonsPanel.add(addRecordButton);
        buttonsPanel.add(deleteRecordButton);
        buttonsPanel.add(refreshButton);

        parent.setLayout(new BorderLayout());
        parent.add(buttonsPanel, BorderLayout.NORTH);
        parent.add(recordsScrollPane, BorderLayout.CENTER);

        addRecordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (client != null) {
                    SwingUtilities.invokeLater(new AddRecordDialog(MainForm.this, statusLabel));
                }
            }
        });
        deleteRecordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (client != null) {
                    if ((Integer) client.sendRequest(REQUEST_DELETE_RECORD,
                            recordsList.get(recordsTable.getSelectedRow())) == OK) {
                        updateRecordsTable();
                        statusLabelMessage("Запись удалена");
                        logger.log(Level.FINEST, "Record successfully deleted");
                    } else {
                        updateRecordsTable();
                        logger.log(Level.WARNING, "Couldn't delete record");
                        statusLabelMessage("Запись не удалена!");
                    }
                }
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (client != null) {
                    updateRecordsTable();
                }
            }
        });
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
        JButton viewAllButton = new JButton();
        JButton refreshButton = new JButton();

        ImageIcon addIcon = createIcon("addPerson.png");
        ImageIcon editIcon = createIcon("edit.png");
        ImageIcon deleteIcon = createIcon("delete.png");
        ImageIcon searchIcon = createIcon("search.png");
        ImageIcon viewAllIcon = createIcon("viewAll.png");
        ImageIcon refreshIcon = createIcon("refresh.png");

        addDoctorButton.setIcon(addIcon);
        editDoctorButton.setIcon(editIcon);
        deleteDoctorButton.setIcon(deleteIcon);
        searchDoctorButton.setIcon(searchIcon);
        viewAllButton.setIcon(viewAllIcon);
        refreshButton.setIcon(refreshIcon);

        addDoctorButton.setToolTipText("Добавить");
        editDoctorButton.setToolTipText("Редактировать");
        deleteDoctorButton.setToolTipText("Удалить");
        searchDoctorButton.setToolTipText("Поиск");
        viewAllButton.setToolTipText("Показать все");
        refreshButton.setToolTipText("обновить данные");

        buttonsPanel.add(addDoctorButton);
        buttonsPanel.add(editDoctorButton);
        buttonsPanel.add(deleteDoctorButton);
        buttonsPanel.add(searchDoctorButton);
        buttonsPanel.add(viewAllButton);
        buttonsPanel.add(refreshButton);

        parent.setLayout(new BorderLayout());
        parent.add(buttonsPanel, BorderLayout.NORTH);
        parent.add(doctorsScrollPane, BorderLayout.CENTER);

        addDoctorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (client != null) {
                    SwingUtilities.invokeLater(new AddDoctorDialog(MainForm.this, statusLabel));
                }
            }
        });

        deleteDoctorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (client != null) {
                    if ((Integer) client.sendRequest(REQUEST_DELETE_DOCTOR,
                            doctorsList.get(doctorsTable.getSelectedRow())) == OK) {
                        //updates table according current search mask
                        updateDoctorsTable();
                        statusLabelMessage("Врач удален");
                        logger.log(Level.FINEST, "Doctor successfully deleted");
                    } else {
                        updateDoctorsTable();
                        JOptionPane.showMessageDialog(MainForm.this,
                                "Этого врача удалить нельзя");
                        logger.log(Level.WARNING, "Couldn't delete doctor");
                        statusLabelMessage("Врач не удален!");
                    }
                }
            }
        });

        viewAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (client != null) {
                    currentDoctorSearchMask = null;
                    updateDoctorsTable();
                    statusLabel.setText("Настройки поиска сброшены");
                    statusLabel.repaint();
                }
            }
        });

        searchDoctorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (client != null) {
                    SwingUtilities.invokeLater(new DoctorSearchDialog(MainForm.this, statusLabel));
                }
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (client != null) {
                    updateDoctorsTable();
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
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        //sets header click sorting
        patientsTable.setAutoCreateRowSorter(true);
        JScrollPane patientsScrollPane = new JScrollPane(patientsTable);
        JButton addPatientButton = new JButton();
        JButton editPatientButton = new JButton();
        JButton deletePatientButton = new JButton();
        JButton searchPatientButton = new JButton();
        JButton viewAllButton = new JButton();
        JButton refreshButton = new JButton();

        ImageIcon addIcon = createIcon("addPerson.png");
        ImageIcon editIcon = createIcon("edit.png");
        ImageIcon deleteIcon = createIcon("delete.png");
        ImageIcon searchIcon = createIcon("search.png");
        ImageIcon viewAllIcon = createIcon("viewAll.png");
        ImageIcon refreshIcon = createIcon("refresh.png");

        addPatientButton.setIcon(addIcon);
        editPatientButton.setIcon(editIcon);
        deletePatientButton.setIcon(deleteIcon);
        searchPatientButton.setIcon(searchIcon);
        viewAllButton.setIcon(viewAllIcon);
        refreshButton.setIcon(refreshIcon);

        addPatientButton.setToolTipText("Добавить");
        editPatientButton.setToolTipText("Редактировать");
        deletePatientButton.setToolTipText("Удалить");
        searchPatientButton.setToolTipText("Поиск");
        viewAllButton.setToolTipText("Показать все");
        refreshButton.setToolTipText("Обновить данные");

        buttonsPanel.add(addPatientButton);
        buttonsPanel.add(editPatientButton);
        buttonsPanel.add(deletePatientButton);
        buttonsPanel.add(searchPatientButton);
        buttonsPanel.add(viewAllButton);
        buttonsPanel.add(refreshButton);

        parent.setLayout(new BorderLayout());
        parent.add(buttonsPanel, BorderLayout.NORTH);
        parent.add(patientsScrollPane, BorderLayout.CENTER);

        addPatientButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (client != null) {
                    SwingUtilities.invokeLater(new AddPatientDialog(MainForm.this, statusLabel));
                }
            }
        });

        deletePatientButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (client != null) {
                    if ((Integer) client.sendRequest(REQUEST_DELETE_PATIENT,
                            patientsList.get(patientsTable.getSelectedRow())) == OK) {

                        //updates table according current search mask
                        updatePatientsTable();
                        statusLabelMessage("Пациент удален");
                    } else {
                        JOptionPane.showMessageDialog(MainForm.this,
                                "Этого пациента удалить нельзя");
                        updatePatientsTable();
                        statusLabelMessage("Ошибка при попытке удаления пациента");
                    }
                }
            }
        });

        viewAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (client != null) {
                    currentPatientSearchMask = null;
                    updatePatientsTable();
                    statusLabel.setText("Настройки поиска сброшены");
                    statusLabel.repaint();
                }
            }
        });

        searchPatientButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (client != null) {
                    SwingUtilities.invokeLater(new PatientSearchDialog(MainForm.this, statusLabel));
                }
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (client != null) {
                    updatePatientsTable();
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
    public void setupAndShowGUI() {
        //size and position setup
        int frameWidth = cfg.getMainFrameDefaultWidth();
        int frameHeight = cfg.getMainFrameDefaultHeight();
        setPreferredSize(new Dimension(frameWidth, frameHeight));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(
                (int) ((screenSize.getWidth() - frameWidth) / 2),
                (int) ((screenSize.getHeight() - frameHeight) / 2));

        Image im = Toolkit.getDefaultToolkit().getImage("formIcon.png");
        this.setIconImage(im);
        setLayout(new BorderLayout());
        setupTabbedPane(getContentPane());
        setupMenuPanel(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        logger.log(Level.FINEST, "Main form initialization completed");
        pack();
        setVisible(true);
        //starts daemon thread which syncs table data from server
        /*BackgroundTablesSyncDaemon syncDaemon = frame.new BackgroundTablesSyncDaemon();
        syncDaemon.setDaemon(true);
        syncDaemon.start();*/
    }

    public static void main(String[] args) {
        ClientConfig cfg = ClientConfig.getInstance();  //config instance
        Client client = null;
        Logger logger = ClientLogger.getInstance().getLogger();
        try {
            client = new Client(cfg.getHost(), cfg.getPort());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Couldn't connect to server on address: " + cfg.getHost() +
                    ":" + cfg.getPort());
        }
        SwingUtilities.invokeLater(new MainForm(client));
    }
}