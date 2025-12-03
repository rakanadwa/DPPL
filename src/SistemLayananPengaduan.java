import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SistemLayananPengaduan extends JFrame {

    // --- WARNA TEMA ---
    private static final Color PRIMARY_BLUE = new Color(13, 71, 161);
    private static final Color ACTIVE_BG = Color.WHITE;
    private static final Color ACTIVE_TEXT = PRIMARY_BLUE;
    private static final Color INACTIVE_TEXT = Color.WHITE;
    private static final Color BG_LIGHT = new Color(248, 249, 250);
    private static final Color TEXT_DARK = new Color(33, 33, 33);
    private static final Color TEXT_GRAY = new Color(117, 117, 117);
    private static final Color STATUS_GREEN = new Color(46, 125, 50);
    private static final Color STATUS_YELLOW = new Color(255, 143, 0);
    private static final Color STATUS_RED = new Color(211, 47, 47);
    private static final Color INPUT_BG = new Color(245, 245, 245);

    // --- VARIABEL GLOBAL KHUSUS DOSEN ---
    private Complaint selectedComplaint;
    private JPanel lecturerContentPanel;

    // --- VARIABEL GLOBAL ADMIN ---
    private JPanel adminContentPanel;
    private Complaint selectedAdminComplaint;

    // --- VARIABEL GLOBAL MANAJEMEN ---
    private JPanel mgmtContentPanel;

    // --- DATA MODEL ---
    static class User {
        String id, name, password, role;
        public User(String id, String name, String password, String role) {
            this.id = id; this.name = name; this.password = password; this.role = role;
        }
    }

    static class Complaint {
        String id, title, type, description, status, date;
        String studentId, studentName;
        String lecturerId;
        String lecturerResponse = "";
        String adminResponse = "";
        String studentAttachment = "";
        String lecturerAttachment = "";
        String adminAttachment = "";
    }

    // --- DB HELPER ---
    static class Database {
        private static final String URL = "jdbc:mysql://localhost:3306/pengaduan_db?useSSL=false&serverTimezone=UTC";
        private static final String USER = "root"; // ganti sesuai MySQL
        private static final String PASS = "";     // ganti sesuai MySQL

        public static Connection getConnection() throws SQLException {
            return DriverManager.getConnection(URL, USER, PASS);
        }

        // USERS
        static User findUser(String id, String pass) throws SQLException {
            try (Connection c = getConnection();
                 PreparedStatement ps = c.prepareStatement("SELECT * FROM users WHERE id=? AND password=?")) {
                ps.setString(1, id);
                ps.setString(2, pass);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) return new User(rs.getString("id"), rs.getString("name"), rs.getString("password"), rs.getString("role"));
                return null;
            }
        }

        static ResultSet getUsersByRole(Connection c, String role) throws SQLException {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM users WHERE role=?");
            ps.setString(1, role);
            return ps.executeQuery();
        }

        static ResultSet getAllUsers(Connection c) throws SQLException {
            return c.createStatement().executeQuery("SELECT * FROM users");
        }

        static boolean addUser(User u) throws SQLException {
            try (Connection c = getConnection();
                 PreparedStatement ps = c.prepareStatement("INSERT INTO users(id,name,password,role) VALUES(?,?,?,?)")) {
                ps.setString(1, u.id); ps.setString(2, u.name); ps.setString(3, u.password); ps.setString(4, u.role);
                return ps.executeUpdate() == 1;
            }
        }

        static boolean updateUser(User u) throws SQLException {
            try (Connection c = getConnection();
                 PreparedStatement ps = c.prepareStatement("UPDATE users SET name=?, password=?, role=? WHERE id=?")) {
                ps.setString(1, u.name); ps.setString(2, u.password); ps.setString(3, u.role); ps.setString(4, u.id);
                return ps.executeUpdate() == 1;
            }
        }

        static boolean deleteUser(String id) throws SQLException {
            try (Connection c = getConnection();
                 PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE id=?")) {
                ps.setString(1, id);
                return ps.executeUpdate() == 1;
            }
        }

        // COMPLAINTS
        static boolean insertComplaint(Complaint cp) throws SQLException {
            try (Connection c = getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "INSERT INTO complaints(id,title,type,description,status,date,studentId,studentName,lecturerId,lecturerResponse,adminResponse,studentAttachment) " +
                                 "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)")) { // Tambah 1 parameter
                ps.setString(1, cp.id); ps.setString(2, cp.title); ps.setString(3, cp.type);
                ps.setString(4, cp.description); ps.setString(5, cp.status); ps.setString(6, cp.date);
                ps.setString(7, cp.studentId); ps.setString(8, cp.studentName);
                ps.setString(9, cp.lecturerId); ps.setString(10, cp.lecturerResponse);
                ps.setString(11, cp.adminResponse);
                ps.setString(12, cp.studentAttachment); // Simpan File Mahasiswa
                return ps.executeUpdate() == 1;
            }
        }

        static ResultSet getComplaintsByStudent(Connection c, String studentId) throws SQLException {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM complaints WHERE studentId=? ORDER BY date DESC");
            ps.setString(1, studentId);
            return ps.executeQuery();
        }

        static ResultSet getComplaintsByLecturer(Connection c, String lecturerId) throws SQLException {
            PreparedStatement ps = c.prepareStatement("SELECT * FROM complaints WHERE type='Akademik' AND lecturerId=? ORDER BY date DESC");
            ps.setString(1, lecturerId);
            return ps.executeQuery();
        }

        static ResultSet getAllComplaints(Connection c) throws SQLException {
            return c.createStatement().executeQuery("SELECT * FROM complaints ORDER BY date DESC");
        }

        static boolean updateLecturerResponse(String id, String response, String status, String attachment) throws SQLException {
            try (Connection c = getConnection();
                 PreparedStatement ps = c.prepareStatement("UPDATE complaints SET lecturerResponse=?, status=?, lecturerAttachment=? WHERE id=?")) {
                ps.setString(1, response);
                ps.setString(2, status);
                ps.setString(3, attachment); // Simpan File Dosen
                ps.setString(4, id);
                return ps.executeUpdate() == 1;
            }
        }

        static boolean updateAdminResponse(String id, String response, String status, String attachment) throws SQLException {
            try (Connection c = getConnection();
                 PreparedStatement ps = c.prepareStatement("UPDATE complaints SET adminResponse=?, status=?, adminAttachment=? WHERE id=?")) {
                ps.setString(1, response);
                ps.setString(2, status);
                ps.setString(3, attachment); // Simpan File Admin
                ps.setString(4, id);
                return ps.executeUpdate() == 1;
            }
        }

        static boolean updateStatus(String id, String status) throws SQLException {
            try (Connection c = getConnection();
                 PreparedStatement ps = c.prepareStatement("UPDATE complaints SET status=? WHERE id=?")) {
                ps.setString(1, status); ps.setString(2, id);
                return ps.executeUpdate() == 1;
            }
        }

        static boolean deleteComplaint(String id) throws SQLException {
            try (Connection c = getConnection();
                 PreparedStatement ps = c.prepareStatement("DELETE FROM complaints WHERE id=?")) {
                ps.setString(1, id);
                return ps.executeUpdate() == 1;
            }
        }

        // STATISTICS
        static int countByStatus(Connection c, String status) throws SQLException {
            PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM complaints WHERE status LIKE ?");
            ps.setString(1, "%" + status + "%");
            ResultSet rs = ps.executeQuery();
            rs.next(); return rs.getInt(1);
        }
        static int countByType(Connection c, String type) throws SQLException {
            PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM complaints WHERE type=?");
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();
            rs.next(); return rs.getInt(1);
        }
        static int countUsersByRole(Connection c, String role) throws SQLException {
            PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM users WHERE role=?");
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            rs.next(); return rs.getInt(1);
        }
    }

    // --- KOMPONEN GUI UTAMA ---
    private static User currentUser;
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);
    private JPanel studentDashPanel, lecturerDashPanel, adminDashPanel, mgmtDashPanel;
    private java.util.List<NavButton> sidebarButtons = new java.util.ArrayList<>();

    public SistemLayananPengaduan() {
        setTitle("Sistem Layanan Pengaduan Mahasiswa");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        mainPanel.add(createLoginPanel(), "LOGIN");
        add(mainPanel);
        cardLayout.show(mainPanel, "LOGIN");
    }

    // ==========================================
    // 1. HALAMAN LOGIN
    // ==========================================
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PRIMARY_BLUE);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(40, 40, 40, 40));
        card.setPreferredSize(new Dimension(360, 450));

        JLabel title1 = new JLabel("SISTEM LAYANAN");
        title1.setFont(new Font("SansSerif", Font.BOLD, 16));
        title1.setForeground(TEXT_DARK);
        title1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title2 = new JLabel("PENGADUAN MAHASISWA");
        title2.setFont(new Font("SansSerif", Font.BOLD, 16));
        title2.setForeground(TEXT_DARK);
        title2.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField txtId = new JTextField("ID");
        txtId.setForeground(Color.GRAY);
        txtId.setPreferredSize(new Dimension(100, 40));
        txtId.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtId.setBorder(new RoundedBorder(10));
        txtId.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtId.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) { if(txtId.getText().equals("ID")){txtId.setText(""); txtId.setForeground(Color.BLACK);} }
            public void focusLost(FocusEvent e) { if(txtId.getText().isEmpty()){txtId.setText("ID"); txtId.setForeground(Color.GRAY);} }
        });

        JPasswordField txtPass = new JPasswordField("Password");
        txtPass.setForeground(Color.GRAY);
        txtPass.setEchoChar((char) 0);
        txtPass.setPreferredSize(new Dimension(100, 40));
        txtPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtPass.setBorder(new RoundedBorder(10));
        txtPass.setAlignmentX(Component.CENTER_ALIGNMENT);
        txtPass.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) { String p=new String(txtPass.getPassword()); if(p.equals("Password")){txtPass.setText(""); txtPass.setForeground(Color.BLACK); txtPass.setEchoChar('•');} }
            public void focusLost(FocusEvent e) { String p=new String(txtPass.getPassword()); if(p.isEmpty()){txtPass.setText("Password"); txtPass.setForeground(Color.GRAY); txtPass.setEchoChar((char)0);} }
        });

        JPanel optionPanel = new JPanel(new BorderLayout());
        optionPanel.setBackground(Color.WHITE);
        optionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        optionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JCheckBox chkRemember = new JCheckBox("Ingat saya"); chkRemember.setBackground(Color.WHITE); chkRemember.setFont(new Font("SansSerif", Font.PLAIN, 11)); chkRemember.setForeground(Color.GRAY);
        JLabel lblForgot = new JLabel("Lupa password ?"); lblForgot.setFont(new Font("SansSerif", Font.PLAIN, 11)); lblForgot.setForeground(Color.GRAY); lblForgot.setCursor(new Cursor(Cursor.HAND_CURSOR));
        optionPanel.add(chkRemember, BorderLayout.WEST);
        optionPanel.add(lblForgot, BorderLayout.EAST);

        JButton btnLogin = new JButton("Masuk") {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(66, 133, 244));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnLogin.setContentAreaFilled(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btnLogin.addActionListener(e -> {
            String id = txtId.getText();
            String pass = new String(txtPass.getPassword());
            if (id.equals("ID") || pass.equals("Password")) { JOptionPane.showMessageDialog(this, "Isi ID & Password"); return; }

            try {
                currentUser = Database.findUser(id, pass);
                if (currentUser != null) {
                    txtId.setText("ID"); txtId.setForeground(Color.GRAY);
                    txtPass.setText("Password"); txtPass.setForeground(Color.GRAY); txtPass.setEchoChar((char)0);

                    switch (currentUser.role) {
                        case "MAHASISWA": recreatePanel(createStudentMainPanel(), "STUDENT"); refreshStudentDash(); break;
                        case "DOSEN": recreatePanel(createLecturerMainPanel(), "LECTURER"); refreshLecturerDash(); break;
                        case "ADMIN": recreatePanel(createAdminMainPanel(), "ADMIN"); refreshAdminDash(); break;
                        case "MANAJEMEN": recreatePanel(createManagementMainPanel(), "MANAGEMENT"); refreshMgmtDash(); break;
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Login Gagal");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error koneksi database");
            }
        });

        card.add(Box.createVerticalStrut(30)); card.add(title1); card.add(title2); card.add(Box.createVerticalStrut(50));
        card.add(txtId); card.add(Box.createVerticalStrut(15)); card.add(txtPass); card.add(Box.createVerticalStrut(10));
        card.add(optionPanel); card.add(Box.createVerticalStrut(30)); card.add(btnLogin); card.add(Box.createVerticalGlue());
        panel.add(card);
        return panel;
    }

    private void recreatePanel(JPanel newPanel, String name) {
        mainPanel.removeAll();
        mainPanel.add(createLoginPanel(), "LOGIN");
        mainPanel.add(newPanel, name);
        mainPanel.revalidate();
        mainPanel.repaint();
        cardLayout.show(mainPanel, name);
    }

    // ==========================================
    // 2. MAHASISWA & DASHBOARD
    // ==========================================
    // GANTI METHOD INI
    private JPanel createStudentMainPanel() {
        JPanel container = new JPanel(new BorderLayout());

        // --- SIDEBAR ---
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(PRIMARY_BLUE);
        sidebar.setPreferredSize(new Dimension(280, getHeight()));

        // Bagian Atas: Judul & Menu
        JPanel topSidebar = new JPanel();
        topSidebar.setLayout(new BoxLayout(topSidebar, BoxLayout.Y_AXIS));
        topSidebar.setBackground(PRIMARY_BLUE);
        topSidebar.setBorder(new EmptyBorder(40, 20, 20, 20));

        JLabel titleLabel = new JLabel("<html>SISTEM LAYANAN<br>PENGADUAN<br>MAHASISWA</html>");
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        topSidebar.add(titleLabel);
        topSidebar.add(Box.createVerticalStrut(60));

        sidebarButtons.clear();
        String[] menus = {"Dashboard", "Buat Pengaduan"};

        // Ganti NavButton biasa dengan logika visual baru
        for (String m : menus) {
            NavButton btn = new NavButton(m) {
                @Override
                public void setActive(boolean b) {
                    super.setActive(b);
                    if (b) {
                        setBackground(Color.WHITE);
                        setForeground(PRIMARY_BLUE);
                        // Icon perlu di-update warnanya jika menggunakan TextIcon
                        if(getText().contains("Dashboard")) setIcon(new TextIcon("\uD83C\uDFE0", PRIMARY_BLUE)); // Rumah
                        else setIcon(new TextIcon("\u270F\uFE0F", PRIMARY_BLUE)); // Pensil
                    } else {
                        setBackground(PRIMARY_BLUE);
                        setForeground(Color.WHITE);
                        if(getText().contains("Dashboard")) setIcon(new TextIcon("\uD83C\uDFE0", Color.WHITE));
                        else setIcon(new TextIcon("\u270F\uFE0F", Color.WHITE));
                    }
                }
                @Override
                protected void paintComponent(Graphics g) {
                    if (isSelected() || getForeground().equals(PRIMARY_BLUE)) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(Color.WHITE);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); // Rounded active state
                        g2.dispose();
                    }
                    super.paintComponent(g);
                }
            };
            btn.setOpaque(false);
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btn.setPreferredSize(new Dimension(240, 45));
            btn.setMaximumSize(new Dimension(240, 45));
            sidebarButtons.add(btn);
            topSidebar.add(btn);
            topSidebar.add(Box.createVerticalStrut(10));
        }

        sidebar.add(topSidebar, BorderLayout.CENTER);

        // Bagian Bawah: Profil Abu-abu (Sesuai Gambar Desktop 2)
        JPanel profilePanel = new JPanel(new BorderLayout());
        profilePanel.setBackground(new Color(240, 240, 240)); // Abu-abu terang
        profilePanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Info User
        JPanel userInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        userInfo.setOpaque(false);
        JLabel icon = new JLabel(new TextIcon("\uD83D\uDC64", Color.BLACK)); // Icon User Hitam

        JPanel textInfo = new JPanel(new GridLayout(2, 1));
        textInfo.setOpaque(false);
        JLabel lblName = new JLabel(currentUser != null ? currentUser.name : "Mahasiswa");
        lblName.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel lblId = new JLabel(currentUser != null ? currentUser.id : "NIM");
        lblId.setFont(new Font("SansSerif", Font.PLAIN, 12));
        textInfo.add(lblName); textInfo.add(lblId);

        userInfo.add(icon); userInfo.add(textInfo);

        // Tombol Logout Merah
        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(new Color(211, 47, 47)); // Merah gelap
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setPreferredSize(new Dimension(0, 35));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> { currentUser = null; cardLayout.show(mainPanel, "LOGIN"); });

        profilePanel.add(userInfo, BorderLayout.NORTH);
        profilePanel.add(Box.createVerticalStrut(10), BorderLayout.CENTER);
        profilePanel.add(btnLogout, BorderLayout.SOUTH);

        sidebar.add(profilePanel, BorderLayout.SOUTH);
        container.add(sidebar, BorderLayout.WEST);

        // Content
        JPanel content = new JPanel(new CardLayout());
        content.setBackground(BG_LIGHT);
        studentDashPanel = createStudentDashboardUI();
        content.add(studentDashPanel, "Dashboard");
        content.add(createStudentCreateComplaintUI(), "Buat Pengaduan");
        container.add(content, BorderLayout.CENTER);

        setupSidebarLogic(content);
        // Set default active styling
        sidebarButtons.get(0).setActive(true);

        return container;
    }

    // GANTI METHOD INI
    private JPanel createStudentDashboardUI() {
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(BG_LIGHT);

        // Header Title
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_LIGHT);
        header.setBorder(new EmptyBorder(30, 40, 20, 40));
        JLabel title = new JLabel("DASHBOARD MAHASISWA");
        title.setFont(new Font("SansSerif", Font.PLAIN, 28));
        header.add(title, BorderLayout.CENTER);
        JLabel notif = new JLabel("\uD83D\uDD14"); // Bell Icon
        notif.setFont(new Font("SansSerif", Font.PLAIN, 20));
        header.add(notif, BorderLayout.EAST);
        mainContainer.add(header, BorderLayout.NORTH);

        // Content Body
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(BG_LIGHT);
        body.setBorder(new EmptyBorder(0, 40, 40, 40));

        // 1. Stats Cards Container
        JPanel statsContainer = new JPanel(new GridLayout(1, 3, 20, 0));
        statsContainer.setBackground(BG_LIGHT);
        statsContainer.setMaximumSize(new Dimension(2000, 130)); // Fixed height
        // Placeholder stats, nanti diisi refreshStudentDash
        statsContainer.add(new JLabel());
        statsContainer.add(new JLabel());
        statsContainer.add(new JLabel());
        body.add(statsContainer);
        body.add(Box.createVerticalStrut(30));

        // 2. Table Section
        JLabel tableTitle = new JLabel("Daftar Pengaduan");
        tableTitle.setFont(new Font("SansSerif", Font.PLAIN, 20));
        tableTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(tableTitle);
        body.add(Box.createVerticalStrut(15));

        // Container Tabel (Putih Rounded)
        JPanel tableCard = new JPanel(new BorderLayout());
        tableCard.setBackground(Color.WHITE);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new Color(230, 230, 230), 1, true),
                new EmptyBorder(10, 20, 20, 20)
        ));

        String[] cols = {"Judul", "Tanggal", "Status", "Aksi"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 3; } // Hanya kolom Aksi yg editable (klik)
        };

        JTable table = new JTable(model);
        table.setRowHeight(50);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(245, 245, 245));
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0,0,1,0,Color.LIGHT_GRAY));

        // CUSTOM RENDERER & EDITOR UNTUK KOLOM AKSI
        table.getColumn("Aksi").setCellRenderer(new StudentActionRenderer());
        table.getColumn("Aksi").setCellEditor(new StudentActionEditor());
        table.getColumn("Status").setCellRenderer(new StatusRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        tableCard.add(scroll, BorderLayout.CENTER);
        body.add(tableCard);

        mainContainer.add(body, BorderLayout.CENTER);
        mainContainer.putClientProperty("model", model);
        mainContainer.putClientProperty("stats", statsContainer);
        return mainContainer;
    }

    // Helper untuk membuat Card Stat (Mirip gambar Desktop 2)
    private JPanel createModernStatCard(String number, String label) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new javax.swing.border.LineBorder(new Color(230,230,230), 1, true)); // Border tipis

        JLabel num = new JLabel(number, SwingConstants.CENTER);
        num.setFont(new Font("SansSerif", Font.PLAIN, 36));
        num.setForeground(TEXT_DARK);

        JLabel txt = new JLabel(label, SwingConstants.CENTER);
        txt.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txt.setForeground(TEXT_GRAY);
        txt.setBorder(new EmptyBorder(0,0,15,0));

        p.add(num, BorderLayout.CENTER);
        p.add(txt, BorderLayout.SOUTH);
        return p;
    }

    // GANTI METHOD INI
    private JPanel createStudentCreateComplaintUI() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_LIGHT);

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(BG_LIGHT);
        header.setBorder(new EmptyBorder(30, 40, 10, 40));
        JLabel title = new JLabel("BUAT PENGADUAN");
        title.setFont(new Font("SansSerif", Font.PLAIN, 28));
        header.add(title);
        panel.add(header, BorderLayout.NORTH);

        // Form Container (Vertical Stack)
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBackground(BG_LIGHT);
        formContainer.setBorder(new EmptyBorder(10, 45, 20, 45));

        // Style warna input background
        Color inputBg = new Color(245, 245, 245);

        // 1. Judul
        JLabel lblJudul = new JLabel("Judul"); lblJudul.setFont(new Font("SansSerif", Font.PLAIN, 14)); lblJudul.setAlignmentX(0);
        JTextField txtTitle = new JTextField();
        txtTitle.setBackground(inputBg);
        txtTitle.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        txtTitle.setPreferredSize(new Dimension(0, 40));
        txtTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtTitle.setAlignmentX(0);

        // 2. Jenis Aduan
        JLabel lblJenis = new JLabel("Jenis Aduan"); lblJenis.setFont(new Font("SansSerif", Font.PLAIN, 14)); lblJenis.setAlignmentX(0);
        String[] types = {"Fasilitas", "Akademik", "Lain-lain"};
        JComboBox<String> cmbType = new JComboBox<>(types);
        cmbType.setBackground(inputBg);
        cmbType.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        cmbType.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cmbType.setAlignmentX(0);

        // 3. Dosen (Hidden by default)
        JLabel lblDosen = new JLabel("Dosen Terkait :"); lblDosen.setFont(new Font("SansSerif", Font.PLAIN, 14)); lblDosen.setAlignmentX(0);
        JComboBox<String> cmbDosen = new JComboBox<>();
        cmbDosen.addItem("- Pilih Dosen -");
        try (Connection c = Database.getConnection(); ResultSet rs = Database.getUsersByRole(c, "DOSEN")) {
            while (rs.next()) cmbDosen.addItem(rs.getString("id") + " - " + rs.getString("name"));
        } catch (SQLException ex) { ex.printStackTrace(); }
        cmbDosen.setBackground(inputBg);
        cmbDosen.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        cmbDosen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cmbDosen.setAlignmentX(0);

        JPanel pDosen = new JPanel(); pDosen.setLayout(new BoxLayout(pDosen, BoxLayout.Y_AXIS));
        pDosen.setBackground(BG_LIGHT); pDosen.setAlignmentX(0);
        pDosen.add(lblDosen); pDosen.add(Box.createVerticalStrut(5)); pDosen.add(cmbDosen); pDosen.add(Box.createVerticalStrut(15));
        pDosen.setVisible(false);

        cmbType.addActionListener(e -> {
            pDosen.setVisible(cmbType.getSelectedItem().equals("Akademik"));
            pDosen.revalidate();
        });

        // 4. Deskripsi
        JLabel lblDesc = new JLabel("Deskripsi"); lblDesc.setFont(new Font("SansSerif", Font.PLAIN, 14)); lblDesc.setAlignmentX(0);
        JTextArea txtDesc = new JTextArea(8, 20);
        txtDesc.setBackground(inputBg);
        txtDesc.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        txtDesc.setLineWrap(true);
        JScrollPane scrollDesc = new JScrollPane(txtDesc);
        scrollDesc.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollDesc.setAlignmentX(0);

        // 5. Lampiran
        JLabel lblFileTitle = new JLabel("Lampirkan"); lblFileTitle.setFont(new Font("SansSerif", Font.PLAIN, 14)); lblFileTitle.setAlignmentX(0);
        JButton btnFile = new JButton("Pilih File");
        btnFile.setBackground(new Color(220, 220, 220));
        btnFile.setBorderPainted(false); btnFile.setFocusPainted(false);
        JLabel lblFileName = new JLabel(" Tidak ada file");
        JPanel fileP = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        fileP.setBackground(BG_LIGHT); fileP.setAlignmentX(0);
        fileP.add(btnFile); fileP.add(lblFileName);

        btnFile.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) lblFileName.setText(" " + fc.getSelectedFile().getName());
        });

        // Add components
        formContainer.add(lblJudul); formContainer.add(Box.createVerticalStrut(5)); formContainer.add(txtTitle);
        formContainer.add(Box.createVerticalStrut(15));
        formContainer.add(lblJenis); formContainer.add(Box.createVerticalStrut(5)); formContainer.add(cmbType);
        formContainer.add(Box.createVerticalStrut(15));
        formContainer.add(pDosen);
        formContainer.add(lblDesc); formContainer.add(Box.createVerticalStrut(5)); formContainer.add(scrollDesc);
        formContainer.add(Box.createVerticalStrut(15));
        formContainer.add(lblFileTitle); formContainer.add(Box.createVerticalStrut(5)); formContainer.add(fileP);

        // Tombol Kirim (Bottom Right)
        JButton btnKirim = new JButton("Kirim");
        btnKirim.setBackground(new Color(13, 71, 161));
        btnKirim.setForeground(Color.WHITE);
        btnKirim.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnKirim.setPreferredSize(new Dimension(120, 40));
        btnKirim.setFocusPainted(false); btnKirim.setBorderPainted(false);
        btnKirim.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnKirim.addActionListener(e -> {
            if(txtTitle.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Judul Kosong!"); return; }
            // ... Logic simpan ke DB sama seperti sebelumnya ...
            // Copy logic insertComplaint disini
            String dosenId = "-";
            if(cmbType.getSelectedItem().equals("Akademik") && cmbDosen.getSelectedIndex() > 0)
                dosenId = cmbDosen.getSelectedItem().toString().split(" - ")[0];

            Complaint c = new Complaint();
            c.id = String.valueOf(System.currentTimeMillis());
            c.title = txtTitle.getText();
            c.type = (String)cmbType.getSelectedItem();
            c.description = txtDesc.getText();
            c.status = "Menunggu Verifikasi";
            c.date = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
            c.studentId = currentUser.id;
            c.studentName = currentUser.name;
            c.lecturerId = dosenId;
            String fileName = lblFileName.getText().trim();
            if(fileName.equals("Tidak ada file")) fileName = "";
            c.studentAttachment = fileName;


            try {
                Database.insertComplaint(c);
                JOptionPane.showMessageDialog(this, "Terkirim!");
                txtTitle.setText(""); txtDesc.setText("");
                refreshStudentDash();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Gagal menyimpan ke database");
            }
        });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(BG_LIGHT);
        footer.setBorder(new EmptyBorder(0, 40, 30, 40));
        footer.add(btnKirim);

        panel.add(formContainer, BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    // Helper Style Form
    private JLabel createFormLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 14));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JTextField createStyledTextField() {
        JTextField t = new JTextField();
        t.setBackground(INPUT_BG);
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        t.setPreferredSize(new Dimension(0, 40));
        t.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        return t;
    }

    private void refreshStudentDash() {
        if(studentDashPanel == null || currentUser == null) return;
        DefaultTableModel model = (DefaultTableModel) studentDashPanel.getClientProperty("model");
        JPanel statsCard = (JPanel) studentDashPanel.getClientProperty("stats");
        model.setRowCount(0);
        int sent = 0, process = 0, done = 0;

        try (Connection c = Database.getConnection(); ResultSet rs = Database.getComplaintsByStudent(c, currentUser.id)) {
            while (rs.next()) {
                String status = rs.getString("status");
                model.addRow(new Object[]{rs.getString("title"), rs.getString("date"), status, "Lihat"});
                if(status.contains("Menunggu")) sent++;
                else if(status.contains("Diproses") || status.contains("Ditanggapi")) process++;
                else if(status.equals("Selesai")) done++;
            }
        } catch (SQLException ex) { ex.printStackTrace(); }

        statsCard.removeAll();
        statsCard.add(createModernStatCard(String.valueOf(sent), "Pengaduan Terkirim"));
        statsCard.add(createModernStatCard(String.valueOf(process), "Pengaduan Diproses"));
        statsCard.add(createModernStatCard(String.valueOf(done), "Pengaduan Selesai"));
        statsCard.revalidate(); statsCard.repaint();
    }

    // ==========================================
    // SIDEBAR & STYLING
    // ==========================================
    class NavButton extends JButton {
        private boolean isActive = false;
        public NavButton(String text) {
            super(text);
            setBorderPainted(false); setFocusPainted(false); setContentAreaFilled(false); setOpaque(false);
            setFont(new Font("SansSerif", Font.BOLD, 14)); setHorizontalAlignment(SwingConstants.LEFT);
            setForeground(INACTIVE_TEXT);
            setBorder(new EmptyBorder(10, 25, 10, 10));
            updateIcon();
        }
        public void setActive(boolean b) {
            this.isActive = b;
            setForeground(isActive ? ACTIVE_TEXT : INACTIVE_TEXT);
            updateIcon(); repaint();
        }
        private void updateIcon() {
            Color iconColor = isActive ? ACTIVE_TEXT : INACTIVE_TEXT;
            if(getText().contains("Dashboard")) setIcon(new TextIcon("\uD83C\uDFE0", iconColor));
            else setIcon(new TextIcon("\u270F\uFE0F", iconColor));
        }
        protected void paintComponent(Graphics g) {
            if (isActive) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACTIVE_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    private JPanel createSidebarWithProfile(String title, String[] menus) {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(PRIMARY_BLUE);
        sidebar.setPreferredSize(new Dimension(260, getHeight()));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(PRIMARY_BLUE);
        top.setBorder(new EmptyBorder(30, 0, 20, 0));

        JLabel lblTitle = new JLabel("<html>&nbsp;&nbsp;&nbsp;SISTEM LAYANAN<br>&nbsp;&nbsp;&nbsp;PENGADUAN<br>&nbsp;&nbsp;&nbsp;MAHASISWA</html>");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(lblTitle);
        top.add(Box.createVerticalStrut(40));

        sidebarButtons.clear();
        for (String m : menus) {
            NavButton btn = new NavButton(m);
            btn.setMaximumSize(new Dimension(500, 50));
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            sidebarButtons.add(btn);
            top.add(btn);
            top.add(Box.createVerticalStrut(5));
        }
        if(!sidebarButtons.isEmpty()) sidebarButtons.get(0).setActive(true);
        sidebar.add(top, BorderLayout.CENTER);

        if (currentUser != null) {
            JPanel profilePanel = new JPanel();
            profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
            profilePanel.setBackground(new Color(240, 240, 240));
            profilePanel.setBorder(new EmptyBorder(15, 20, 20, 20));

            JPanel infoBox = new JPanel(new BorderLayout());
            infoBox.setOpaque(false);
            JLabel avatar = new JLabel("\uD83D\uDC64");
            avatar.setFont(new Font("SansSerif", Font.PLAIN, 30));
            JPanel texts = new JPanel(new GridLayout(2,1));
            texts.setOpaque(false);
            JLabel lName = new JLabel(currentUser.name); lName.setFont(new Font("SansSerif", Font.BOLD, 14));
            JLabel lId = new JLabel(currentUser.id); lId.setFont(new Font("SansSerif", Font.PLAIN, 12)); lId.setForeground(Color.GRAY);
            texts.add(lName); texts.add(lId);
            texts.setBorder(new EmptyBorder(0, 10, 0, 0));
            infoBox.add(avatar, BorderLayout.WEST);
            infoBox.add(texts, BorderLayout.CENTER);

            JButton btnLogout = new JButton("Logout");
            btnLogout.setFont(new Font("SansSerif", Font.BOLD, 14));
            btnLogout.setForeground(Color.WHITE);
            btnLogout.setContentAreaFilled(false);
            btnLogout.setFocusPainted(false);
            btnLogout.setBorderPainted(false);
            btnLogout.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
                public void paint(Graphics g, JComponent c) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(STATUS_RED);
                    g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 20, 20);
                    super.paint(g, c);
                    g2.dispose();
                }
            });
            btnLogout.addActionListener(e -> { currentUser = null; cardLayout.show(mainPanel, "LOGIN"); });
            btnLogout.setMaximumSize(new Dimension(300, 40));
            btnLogout.setAlignmentX(Component.CENTER_ALIGNMENT);

            profilePanel.add(infoBox);
            profilePanel.add(Box.createVerticalStrut(15));
            profilePanel.add(btnLogout);
            sidebar.add(profilePanel, BorderLayout.SOUTH);
        }
        return sidebar;
    }

    private void setupSidebarLogic(JPanel contentPanel) {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        for (NavButton btn : sidebarButtons) {
            btn.addActionListener(e -> {
                for (NavButton b : sidebarButtons) b.setActive(false);
                btn.setActive(true);
                String cmd = btn.getText();

                if (contentPanel.getComponent(0) == studentDashPanel) refreshStudentDash();
                else if (contentPanel.getComponent(0) == lecturerDashPanel) refreshLecturerDash();
                else if (contentPanel.getComponent(0) == adminDashPanel) refreshAdminDash();
                else if (contentPanel.getComponent(0) == mgmtDashPanel) refreshMgmtDash();
                cl.show(contentPanel, cmd);
            });
        }
    }

    // ==========================================
    // 2. DOSEN PANEL
    // ==========================================
    private JPanel createLecturerMainPanel() {
        JPanel container = new JPanel(new BorderLayout());
        JPanel sidebar = createSidebarWithProfile("Dosen", new String[]{"Dashboard", "Riwayat Tanggapan"});
        container.add(sidebar, BorderLayout.WEST);

        lecturerContentPanel = new JPanel(new CardLayout());
        lecturerContentPanel.setBackground(BG_LIGHT);

        lecturerDashPanel = createLecturerDashboardUI();
        JPanel historyPanel = createLecturerHistoryUI();
        JPanel responseFormPanel = createLecturerResponseFormUI();

        lecturerContentPanel.add(lecturerDashPanel, "Dashboard");
        lecturerContentPanel.add(historyPanel, "Riwayat Tanggapan");
        lecturerContentPanel.add(responseFormPanel, "FormTanggapan");
        container.add(lecturerContentPanel, BorderLayout.CENTER);

        CardLayout cl = (CardLayout) lecturerContentPanel.getLayout();
        for (NavButton btn : sidebarButtons) {
            btn.addActionListener(e -> {
                for (NavButton b : sidebarButtons) b.setActive(false);
                btn.setActive(true);
                String cmd = btn.getText();
                if (cmd.equals("Dashboard")) {
                    refreshLecturerDash();
                    cl.show(lecturerContentPanel, "Dashboard");
                } else if (cmd.equals("Riwayat Tanggapan")) {
                    refreshLecturerHistory(historyPanel);
                    cl.show(lecturerContentPanel, "Riwayat Tanggapan");
                }
            });
        }
        return container;
    }

    private JPanel createLecturerDashboardUI() {
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(BG_LIGHT);
        mainContainer.setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_LIGHT);
        JLabel title = new JLabel("DASHBOARD DOSEN");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(TEXT_DARK);
        JLabel notif = new JLabel("\uD83D\uDD14");
        notif.setFont(new Font("SansSerif", Font.PLAIN, 24));
        header.add(title, BorderLayout.WEST);
        header.add(notif, BorderLayout.EAST);

        CardPanel statsCard = new CardPanel();
        statsCard.setLayout(new GridLayout(1, 3, 0, 0));
        statsCard.setPreferredSize(new Dimension(0, 120));
        statsCard.add(new JLabel()); statsCard.add(new JLabel()); statsCard.add(new JLabel());

        JPanel statsWrapper = new JPanel(new BorderLayout());
        statsWrapper.setBackground(BG_LIGHT);
        statsWrapper.setBorder(new EmptyBorder(30, 0, 30, 0));
        statsWrapper.add(statsCard, BorderLayout.CENTER);

        JPanel tablePanel = new CardPanel();
        tablePanel.setLayout(new BorderLayout());
        tablePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTable = new JLabel("Daftar Pengaduan");
        lblTable.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTable.setBorder(new EmptyBorder(0, 0, 15, 0));

        String[] cols = {"ID", "Judul", "Tanggal", "Status", "Aksi"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == 4; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(50);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.removeColumn(table.getColumnModel().getColumn(0));
        table.getColumn("Status").setCellRenderer(new StatusRenderer());
        table.getColumn("Aksi").setCellRenderer(new LecturerActionRenderer());
        table.getColumn("Aksi").setCellEditor(new LecturerActionEditor());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        tablePanel.add(lblTable, BorderLayout.NORTH);
        tablePanel.add(scroll, BorderLayout.CENTER);

        mainContainer.add(header, BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout()); body.setBackground(BG_LIGHT);
        body.add(statsWrapper, BorderLayout.NORTH);
        body.add(tablePanel, BorderLayout.CENTER);
        mainContainer.add(body, BorderLayout.CENTER);
        mainContainer.putClientProperty("model", model);
        mainContainer.putClientProperty("stats", statsCard);
        return mainContainer;
    }

    private JPanel createLecturerResponseFormUI() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_LIGHT);

        // --- Header ---
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(BG_LIGHT);
        header.setBorder(new EmptyBorder(30, 40, 10, 40));

        JButton btnBack = new JButton("\u2190 Kembali");
        btnBack.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            CardLayout cl = (CardLayout) lecturerContentPanel.getLayout();
            refreshLecturerDash();
            cl.show(lecturerContentPanel, "Dashboard");
        });

        JLabel title = new JLabel("TANGGAPI PENGADUAN");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));

        header.add(btnBack);
        header.add(Box.createHorizontalStrut(10));
        header.add(title);
        panel.add(header, BorderLayout.NORTH);

        // --- Container Form ---
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBackground(BG_LIGHT);
        formContainer.setBorder(new EmptyBorder(10, 60, 40, 60));

        // Style Input
        Color inputBg = new Color(245, 245, 245);
        Border inputBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);

        // --- BAGIAN 1: DETAIL ADUAN MAHASISWA ---
        JLabel lblDetail = new JLabel("Detail Aduan");
        lblDetail.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblDetail.setAlignmentX(Component.LEFT_ALIGNMENT); // KUNCI RATA KIRI

        JTextArea txtDetail = new JTextArea(5, 20);
        txtDetail.setEditable(false);
        txtDetail.setBackground(new Color(230, 230, 230));
        txtDetail.setBorder(inputBorder);
        txtDetail.setLineWrap(true);
        txtDetail.setWrapStyleWord(true);
        txtDetail.setAlignmentX(Component.LEFT_ALIGNMENT); // KUNCI RATA KIRI

        // LABEL UNTUK MENAMPILKAN FILE MAHASISWA
        JLabel lblStudentFile = new JLabel("Lampiran Mahasiswa: -");
        lblStudentFile.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblStudentFile.setForeground(new Color(13, 71, 161));
        lblStudentFile.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblStudentFile.setAlignmentX(Component.LEFT_ALIGNMENT); // KUNCI RATA KIRI

        lblStudentFile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                String text = lblStudentFile.getText();
                if(!text.contains("-") && !text.contains("Tidak ada")) {
                    JOptionPane.showMessageDialog(panel, "Membuka file: " + text.replace("Lampiran Mahasiswa: ", ""));
                }
            }
        });

        // --- BAGIAN 2: INPUT TANGGAPAN DOSEN ---
        JLabel lblResponse = new JLabel("Tanggapan");
        lblResponse.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblResponse.setAlignmentX(Component.LEFT_ALIGNMENT); // KUNCI RATA KIRI

        JTextArea txtResponse = new JTextArea(8, 20);
        txtResponse.setBackground(inputBg);
        txtResponse.setBorder(inputBorder);
        txtResponse.setLineWrap(true);
        txtResponse.setWrapStyleWord(true);

        // ScrollPane juga harus diset alignment-nya
        JScrollPane scrollResponse = new JScrollPane(txtResponse);
        scrollResponse.setAlignmentX(Component.LEFT_ALIGNMENT); // KUNCI RATA KIRI

        // --- BAGIAN 3: LAMPIRAN DOSEN ---
        JLabel lblLampirkan = new JLabel("Lampirkan");
        lblLampirkan.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblLampirkan.setAlignmentX(Component.LEFT_ALIGNMENT); // KUNCI RATA KIRI

        JButton btnFile = new JButton("Pilih File");
        btnFile.setBackground(new Color(220, 220, 220));
        btnFile.setFocusPainted(false);
        btnFile.setBorderPainted(false);

        JLabel lblLecturerFile = new JLabel(" Tidak ada file");
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filePanel.setBackground(BG_LIGHT);
        filePanel.setAlignmentX(Component.LEFT_ALIGNMENT); // KUNCI RATA KIRI
        filePanel.add(btnFile);
        filePanel.add(lblLecturerFile);

        btnFile.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                lblLecturerFile.setText(" " + fc.getSelectedFile().getName());
            }
        });

        // --- SUSUN KOMPONEN ---
        formContainer.add(lblDetail);
        formContainer.add(Box.createVerticalStrut(5));
        formContainer.add(txtDetail); // Input Detail
        formContainer.add(Box.createVerticalStrut(5));
        formContainer.add(lblStudentFile); // File Mahasiswa
        formContainer.add(Box.createVerticalStrut(20));

        formContainer.add(lblResponse);
        formContainer.add(Box.createVerticalStrut(5));
        formContainer.add(scrollResponse); // Input Tanggapan
        formContainer.add(Box.createVerticalStrut(15));

        formContainer.add(lblLampirkan);
        formContainer.add(Box.createVerticalStrut(5));
        formContainer.add(filePanel); // Tombol File

        // --- FOOTER & LOGIC SIMPAN (Sama seperti sebelumnya) ---
        JButton btnKirim = new JButton("Kirim");
        btnKirim.setBackground(new Color(13, 71, 161));
        btnKirim.setForeground(Color.WHITE);
        btnKirim.setPreferredSize(new Dimension(150, 40));
        btnKirim.setFocusPainted(false);
        btnKirim.setBorderPainted(false);

        btnKirim.addActionListener(e -> {
            if (selectedComplaint != null) {
                if(txtResponse.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Tanggapan harus diisi!");
                    return;
                }
                try {
                    String attachmentName = lblLecturerFile.getText().trim();
                    if(attachmentName.equals("Tidak ada file")) attachmentName = "";

                    // Pastikan method updateLecturerResponse di class Database sudah menerima 4 parameter (id, respon, status, lampiran)
                    Database.updateLecturerResponse(selectedComplaint.id, txtResponse.getText(), "Ditanggapi Dosen", attachmentName);

                    JOptionPane.showMessageDialog(this, "Tanggapan berhasil dikirim!");
                    txtResponse.setText("");
                    lblLecturerFile.setText(" Tidak ada file");
                    refreshLecturerDash();
                    ((CardLayout) lecturerContentPanel.getLayout()).show(lecturerContentPanel, "Dashboard");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Gagal mengirim tanggapan");
                }
            }
        });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(BG_LIGHT);
        footer.setBorder(new EmptyBorder(0, 60, 30, 60));
        footer.add(btnKirim);

        panel.add(formContainer, BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);

        // LOGIC SAAT PANEL DIBUKA
        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                if (selectedComplaint != null) {
                    txtDetail.setText("Judul: " + selectedComplaint.title + "\n\nDeskripsi:\n" + selectedComplaint.description);
                    try (Connection c = Database.getConnection();
                         PreparedStatement ps = c.prepareStatement("SELECT studentAttachment FROM complaints WHERE id=?")) {
                        ps.setString(1, selectedComplaint.id);
                        ResultSet rs = ps.executeQuery();
                        if(rs.next()) {
                            String file = rs.getString("studentAttachment");
                            if(file != null && !file.isEmpty()) {
                                lblStudentFile.setText("Lampiran Mahasiswa: " + file);
                                lblStudentFile.setForeground(new Color(13, 71, 161));
                            } else {
                                lblStudentFile.setText("Lampiran Mahasiswa: -");
                                lblStudentFile.setForeground(Color.GRAY);
                            }
                        }
                    } catch (SQLException ex) { ex.printStackTrace(); }
                } else {
                    txtDetail.setText("- Data tidak ditemukan -");
                }
            }
        });

        return panel;
    }

    private JPanel createLecturerHistoryUI() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_LIGHT);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("RIWAYAT TANGGAPAN");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setBorder(new EmptyBorder(0, 0, 20, 0));

        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        String[] cols = {"Judul Aduan", "Tanggal", "Status", "Jenis Aduan", "Aksi"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int row, int column) { return column == 4; } };
        JTable table = new JTable(model);
        table.setRowHeight(50);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.getColumn("Status").setCellRenderer(new StatusRenderer());
        table.getColumn("Aksi").setCellRenderer(new HistoryDetailRenderer());
        table.getColumn("Aksi").setCellEditor(new HistoryDetailEditor());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        card.add(scroll, BorderLayout.CENTER);
        panel.add(title, BorderLayout.NORTH);
        panel.add(card, BorderLayout.CENTER);
        panel.putClientProperty("model", model);
        return panel;
    }

    private void refreshLecturerDash() {
        if (lecturerDashPanel == null) return;
        DefaultTableModel model = (DefaultTableModel) lecturerDashPanel.getClientProperty("model");
        JPanel statsCard = (JPanel) lecturerDashPanel.getClientProperty("stats");
        model.setRowCount(0);
        int perluRespon = 0, selesai = 0;

        try (Connection c = Database.getConnection(); ResultSet rs = Database.getComplaintsByLecturer(c, currentUser.id)) {
            while (rs.next()) {
                String id = rs.getString("id");
                String title = rs.getString("title");
                String date = rs.getString("date");
                String status = rs.getString("status");
                String lecturerResp = rs.getString("lecturerResponse");
                String btnText = (lecturerResp == null || lecturerResp.isEmpty()) ? "Tanggapi" : "Selesai";
                if (btnText.equals("Tanggapi")) perluRespon++; else selesai++;
                model.addRow(new Object[]{id, title, date, status, btnText});
            }
        } catch (SQLException ex) { ex.printStackTrace(); }

        statsCard.removeAll();
        statsCard.add(createStatItem(String.valueOf(perluRespon), "Perlu Direspon"));
        statsCard.add(createStatItem(String.valueOf(selesai), "Selesai Ditanggapi"));
        statsCard.add(createStatItem(String.valueOf(perluRespon + selesai), "Total Aduan"));
        statsCard.revalidate(); statsCard.repaint();
    }

    private void refreshLecturerHistory(JPanel p) {
        if (p == null) return;
        DefaultTableModel model = (DefaultTableModel) p.getClientProperty("model");
        model.setRowCount(0);
        try (Connection c = Database.getConnection(); ResultSet rs = Database.getComplaintsByLecturer(c, currentUser.id)) {
            while (rs.next()) {
                String resp = rs.getString("lecturerResponse");
                if (resp != null && !resp.isEmpty()) {
                    model.addRow(new Object[]{rs.getString("title"), rs.getString("date"), rs.getString("status"), rs.getString("type"), "Detail"});
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    // ==========================================
    // 3. ADMIN PANEL
    // ==========================================
    private JPanel createAdminMainPanel() {
        JPanel container = new JPanel(new BorderLayout());
        JPanel sidebar = createSidebarWithProfile("Admin", new String[]{"Dashboard", "Data Pengaduan", "Respon"});
        container.add(sidebar, BorderLayout.WEST);

        adminContentPanel = new JPanel(new CardLayout());
        adminContentPanel.setBackground(BG_LIGHT);

        adminDashPanel = createAdminDashboardUI();
        JPanel dataPanel = createAdminDataUI();
        JPanel responListPanel = createAdminResponListUI();
        JPanel responFormPanel = createAdminResponFormUI();

        adminContentPanel.add(adminDashPanel, "Dashboard");
        adminContentPanel.add(dataPanel, "Data Pengaduan");
        adminContentPanel.add(responListPanel, "Respon");
        adminContentPanel.add(responFormPanel, "FormRespon");

        container.add(adminContentPanel, BorderLayout.CENTER);

        CardLayout cl = (CardLayout) adminContentPanel.getLayout();
        for (NavButton btn : sidebarButtons) {
            btn.addActionListener(e -> {
                for (NavButton b : sidebarButtons) b.setActive(false);
                btn.setActive(true);
                String cmd = btn.getText();
                if (cmd.equals("Dashboard")) {
                    refreshAdminDash();
                    cl.show(adminContentPanel, "Dashboard");
                } else if (cmd.equals("Data Pengaduan")) {
                    refreshAdminData(dataPanel);
                    cl.show(adminContentPanel, "Data Pengaduan");
                } else if (cmd.equals("Respon")) {
                    refreshAdminResponList(responListPanel);
                    cl.show(adminContentPanel, "Respon");
                }
            });
        }
        return container;
    }

    private JPanel createAdminDashboardUI() {
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(BG_LIGHT);
        mainContainer.setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_LIGHT);
        JLabel title = new JLabel("DASHBOARD ADMIN");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(TEXT_DARK);
        header.add(title, BorderLayout.WEST);
        header.add(new JLabel(new TextIcon("\uD83D\uDD14", TEXT_DARK)), BorderLayout.EAST);

        CardPanel statsCard = new CardPanel();
        statsCard.setLayout(new GridLayout(1, 3, 0, 0));
        statsCard.setPreferredSize(new Dimension(0, 120));
        statsCard.add(new JLabel()); statsCard.add(new JLabel()); statsCard.add(new JLabel());

        JPanel statsWrapper = new JPanel(new BorderLayout());
        statsWrapper.setBackground(BG_LIGHT);
        statsWrapper.setBorder(new EmptyBorder(30, 0, 30, 0));
        statsWrapper.add(statsCard, BorderLayout.CENTER);

        JPanel tablePanel = new CardPanel();
        tablePanel.setLayout(new BorderLayout());
        tablePanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTable = new JLabel("Daftar Pengaduan");
        lblTable.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTable.setBorder(new EmptyBorder(0, 0, 15, 0));

        String[] cols = {"ID", "Judul", "Tanggal", "Status", "Aksi"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return c == 4; } };
        JTable table = new JTable(model);
        table.setRowHeight(50);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.removeColumn(table.getColumnModel().getColumn(0));
        table.getColumn("Status").setCellRenderer(new StatusRenderer());
        table.getColumn("Aksi").setCellRenderer(new AdminDashboardActionRenderer());
        table.getColumn("Aksi").setCellEditor(new AdminDashboardActionEditor());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        tablePanel.add(lblTable, BorderLayout.NORTH);
        tablePanel.add(scroll, BorderLayout.CENTER);

        mainContainer.add(header, BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout()); body.setBackground(BG_LIGHT);
        body.add(statsWrapper, BorderLayout.NORTH);
        body.add(tablePanel, BorderLayout.CENTER);
        mainContainer.add(body, BorderLayout.CENTER);
        mainContainer.putClientProperty("model", model);
        mainContainer.putClientProperty("stats", statsCard);
        return mainContainer;
    }

    private JPanel createAdminDataUI() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_LIGHT);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("DATA PENGADUAN");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setBorder(new EmptyBorder(0, 0, 20, 0));

        // Card Panel Table
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new Color(230, 230, 230), 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        // UPDATE KOLOM: Tambahkan "ID" di index 0
        String[] cols = {"ID", "Nama Mahasiswa", "Tanggal", "Status", "Jenis Aduan", "", ""};

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 5 || c == 6; } // Tombol Detail & Hapus editable
        };

        JTable table = new JTable(model);
        styleTable(table); // Gunakan style yang sudah ada

        // UPDATE RENDERER & EDITOR
        // Ingat: Index kolom bergeser +1 karena ada ID di depan
        table.getColumn("Status").setCellRenderer(new StatusRenderer());

        // Kolom Detail (Index 5)
        table.getColumnModel().getColumn(5).setCellRenderer(new HistoryDetailRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new AdminDetailEditor()); // Kita update class ini di bawah
        table.getColumnModel().getColumn(5).setMaxWidth(80);

        // Kolom Hapus (Index 6)
        table.getColumnModel().getColumn(6).setCellRenderer(new AdminDeleteRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new AdminDeleteEditor());
        table.getColumnModel().getColumn(6).setMaxWidth(60);

        // SEMBUNYIKAN KOLOM ID (Index 0) AGAR TAMPILAN TETAP BERSIH
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        card.add(scroll, BorderLayout.CENTER);
        panel.add(title, BorderLayout.NORTH);
        panel.add(card, BorderLayout.CENTER);

        panel.putClientProperty("model", model);
        return panel;
    }

    private JPanel createAdminResponListUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG_LIGHT);
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("RESPON");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(BG_LIGHT);

        JScrollPane scroll = new JScrollPane(listContainer);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(BG_LIGHT);

        mainPanel.add(title, BorderLayout.NORTH);
        mainPanel.add(scroll, BorderLayout.CENTER);
        mainPanel.putClientProperty("listContainer", listContainer);
        return mainPanel;
    }

    private JPanel createAdminResponFormUI() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_LIGHT);

        // --- HEADER ---
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(BG_LIGHT);
        header.setBorder(new EmptyBorder(30, 40, 10, 40));

        JButton btnBack = new JButton("\u2190 Kembali");
        btnBack.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            CardLayout cl = (CardLayout) adminContentPanel.getLayout();
            refreshAdminResponList((JPanel)adminContentPanel.getComponent(2)); // Refresh list
            cl.show(adminContentPanel, "Respon");
        });

        JLabel lblPageTitle = new JLabel("RESPON ADUAN");
        lblPageTitle.setFont(new Font("SansSerif", Font.BOLD, 24));

        header.add(btnBack);
        header.add(Box.createHorizontalStrut(10));
        header.add(lblPageTitle);
        panel.add(header, BorderLayout.NORTH);

        // --- FORM CONTAINER ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BG_LIGHT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 10, 0);

        Border inputBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 5)
        );

        // --- 1. DETAIL ADUAN MAHASISWA ---
        formPanel.add(createLabel("Detail Aduan (Mahasiswa)"), gbc); gbc.gridy++;

        JPanel pDetail = new JPanel(new BorderLayout());
        pDetail.setBackground(new Color(248, 248, 248));
        pDetail.setBorder(inputBorder);

        JLabel lblDetailText = new JLabel("Judul Pengaduan...");
        lblDetailText.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JButton btnViewDetail = new JButton("Detail");
        styleGrayButton(btnViewDetail);

        pDetail.add(lblDetailText, BorderLayout.CENTER);
        pDetail.add(btnViewDetail, BorderLayout.EAST);

        // Logic Tombol Detail Mahasiswa (Menampilkan Deskripsi & File)
        btnViewDetail.addActionListener(e -> {
            if(selectedAdminComplaint != null) {
                String msg = "Deskripsi:\n" + selectedAdminComplaint.description +
                        "\n\nLampiran Mahasiswa:\n" +
                        (selectedAdminComplaint.studentAttachment == null || selectedAdminComplaint.studentAttachment.isEmpty() ?
                                "- Tidak ada file -" : selectedAdminComplaint.studentAttachment);

                JTextArea ta = new JTextArea(msg);
                ta.setEditable(false); ta.setLineWrap(true); ta.setWrapStyleWord(true);
                ta.setSize(new Dimension(400, 200));
                JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Detail Aduan Mahasiswa", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        formPanel.add(pDetail, gbc); gbc.gridy++;
        formPanel.add(Box.createVerticalStrut(15), gbc); gbc.gridy++;

        // --- 2. TANGGAPAN DOSEN (Kondisional) ---
        JLabel lblDosenTitle = createLabel("Tanggapan Dosen");
        formPanel.add(lblDosenTitle, gbc); gbc.gridy++;

        JPanel pDosen = new JPanel(new BorderLayout());
        pDosen.setBackground(new Color(248, 248, 248));
        pDosen.setBorder(inputBorder);

        JLabel lblDosenText = new JLabel("Tanggapan Dosen...");
        lblDosenText.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JButton btnViewDosen = new JButton("Detail");
        styleGrayButton(btnViewDosen);

        pDosen.add(lblDosenText, BorderLayout.CENTER);
        pDosen.add(btnViewDosen, BorderLayout.EAST);

        // Logic Tombol Detail Dosen (Menampilkan Respon & File)
        btnViewDosen.addActionListener(e -> {
            if(selectedAdminComplaint != null) {
                String msg = "Tanggapan Dosen:\n" + selectedAdminComplaint.lecturerResponse +
                        "\n\nLampiran Dosen:\n" +
                        (selectedAdminComplaint.lecturerAttachment == null || selectedAdminComplaint.lecturerAttachment.isEmpty() ?
                                "- Tidak ada file -" : selectedAdminComplaint.lecturerAttachment);

                JTextArea ta = new JTextArea(msg);
                ta.setEditable(false); ta.setLineWrap(true); ta.setWrapStyleWord(true);
                ta.setSize(new Dimension(400, 200));
                JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Detail Tanggapan Dosen", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        formPanel.add(pDosen, gbc); gbc.gridy++;
        Component spacerDosen = Box.createVerticalStrut(15);
        formPanel.add(spacerDosen, gbc); gbc.gridy++;

        // --- 3. INPUT RESPON ADMIN ---
        formPanel.add(createLabel("Respon Admin"), gbc); gbc.gridy++;
        JTextArea txtRespon = new JTextArea(8, 20);
        txtRespon.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        txtRespon.setLineWrap(true);
        txtRespon.setWrapStyleWord(true);
        formPanel.add(txtRespon, gbc); gbc.gridy++;
        formPanel.add(Box.createVerticalStrut(15), gbc); gbc.gridy++;

        // --- 4. LAMPIRAN ADMIN (Logic Baru) ---
        formPanel.add(createLabel("Lampirkan"), gbc); gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;

        JButton btnFile = new JButton("Pilih File");
        styleGrayButton(btnFile);
        btnFile.setPreferredSize(new Dimension(100, 35));

        JLabel lblAdminFile = new JLabel(" Tidak ada file");

        JPanel fileWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        fileWrapper.setBackground(BG_LIGHT);
        fileWrapper.add(btnFile);
        fileWrapper.add(lblAdminFile);

        // ActionListener Tombol File Admin
        btnFile.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                lblAdminFile.setText(" " + fc.getSelectedFile().getName());
            }
        });
        formPanel.add(fileWrapper, gbc);

        // --- WRAPPER & SCROLL ---
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setBackground(BG_LIGHT);
        formWrapper.setBorder(new EmptyBorder(0, 60, 0, 60)); // Margin kiri kanan
        formWrapper.add(formPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(formWrapper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(BG_LIGHT);

        // --- FOOTER (TOMBOL KIRIM) ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(BG_LIGHT);
        footer.setBorder(new EmptyBorder(10, 60, 30, 60));

        JButton btnSubmit = new JButton("Respon");
        btnSubmit.setOpaque(true);
        btnSubmit.setBorderPainted(false);
        btnSubmit.setBackground(new Color(13, 71, 161));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setPreferredSize(new Dimension(150, 45));
        btnSubmit.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnSubmit.setFocusPainted(false);
        btnSubmit.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnSubmit.addActionListener(e -> {
            if(selectedAdminComplaint != null) {
                if(txtRespon.getText().isEmpty()){
                    JOptionPane.showMessageDialog(this, "Isi respon terlebih dahulu!"); return;
                }
                try {
                    // Ambil nama file admin
                    String attachmentName = lblAdminFile.getText().trim();
                    if(attachmentName.equals("Tidak ada file")) attachmentName = "";

                    // Update DB dengan 4 parameter (id, respon, status, attachment)
                    Database.updateAdminResponse(selectedAdminComplaint.id, txtRespon.getText(), "Selesai", attachmentName);

                    JOptionPane.showMessageDialog(this, "Respon terkirim!");
                    txtRespon.setText("");
                    lblAdminFile.setText(" Tidak ada file");

                    refreshAdminResponList((JPanel)adminContentPanel.getComponent(2));
                    ((CardLayout)adminContentPanel.getLayout()).show(adminContentPanel, "Respon");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Gagal menyimpan respon");
                }
            }
        });
        footer.add(btnSubmit);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);

        // --- LOGIC DISPLAY SAAT HALAMAN DIBUKA ---
        panel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                if(selectedAdminComplaint != null) {
                    // Query ulang DB untuk memastikan data attachment terbaru (Mahasiswa & Dosen) terambil
                    try (Connection c = Database.getConnection();
                         PreparedStatement ps = c.prepareStatement("SELECT * FROM complaints WHERE id=?")) {
                        ps.setString(1, selectedAdminComplaint.id);
                        ResultSet rs = ps.executeQuery();
                        if(rs.next()){
                            selectedAdminComplaint.description = rs.getString("description");
                            selectedAdminComplaint.studentAttachment = rs.getString("studentAttachment");
                            selectedAdminComplaint.lecturerResponse = rs.getString("lecturerResponse");
                            selectedAdminComplaint.lecturerAttachment = rs.getString("lecturerAttachment");
                            selectedAdminComplaint.type = rs.getString("type");
                            selectedAdminComplaint.title = rs.getString("title");
                        }
                    } catch (SQLException ex) { ex.printStackTrace(); }

                    lblPageTitle.setText("RESPON ADUAN " + selectedAdminComplaint.type.toUpperCase());
                    lblDetailText.setText(selectedAdminComplaint.title); // Tampilkan Judul di box

                    // Cek apakah ada tanggapan dosen (Khusus tipe Akademik)
                    boolean hasDosenResponse = selectedAdminComplaint.type.equals("Akademik");

                    lblDosenTitle.setVisible(hasDosenResponse);
                    pDosen.setVisible(hasDosenResponse);
                    spacerDosen.setVisible(hasDosenResponse);

                    if(hasDosenResponse) {
                        if (selectedAdminComplaint.lecturerResponse != null && !selectedAdminComplaint.lecturerResponse.isEmpty()) {
                            lblDosenText.setText("Tanggapan Dosen tersedia (Klik Detail)");
                            lblDosenText.setForeground(new Color(0, 128, 0));
                        } else {
                            lblDosenText.setText("Belum ada tanggapan dosen");
                            lblDosenText.setForeground(Color.GRAY);
                        }
                    }

                    // Reset scroll ke atas
                    SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
                }
            }
        });
        return panel;
    }

    private void refreshAdminDash() {
        if(adminDashPanel == null) return;
        DefaultTableModel model = (DefaultTableModel) adminDashPanel.getClientProperty("model");
        JPanel statsCard = (JPanel) adminDashPanel.getClientProperty("stats");
        model.setRowCount(0);

        int received = 0, processed = 0, done = 0;
        try (Connection c = Database.getConnection(); ResultSet rs = Database.getAllComplaints(c)) {
            while (rs.next()) {
                String status = rs.getString("status");
                model.addRow(new Object[]{rs.getString("id"), rs.getString("title"), rs.getString("date"), status, "Lihat"});
                if(status.equals("Menunggu Verifikasi")) received++;
                else if(status.contains("Diproses") || status.contains("Ditanggapi")) processed++;
                else if(status.equals("Selesai")) done++;
            }
        } catch (SQLException ex) { ex.printStackTrace(); }

        statsCard.removeAll();
        statsCard.add(createStatItem(String.valueOf(received), "Pengaduan Diterima"));
        statsCard.add(createStatItem(String.valueOf(processed), "Pengaduan Diproses"));
        statsCard.add(createStatItem(String.valueOf(done), "Pengaduan Selesai"));
        statsCard.revalidate(); statsCard.repaint();
    }

    private void refreshAdminData(JPanel panel) {
        DefaultTableModel model = (DefaultTableModel) panel.getClientProperty("model");
        model.setRowCount(0);
        try (Connection c = Database.getConnection(); ResultSet rs = Database.getAllComplaints(c)) {
            while (rs.next()) {
                // Masukkan ID di urutan pertama (Index 0)
                model.addRow(new Object[]{
                        rs.getString("id"), // ID (Tersembunyi)
                        rs.getString("studentName"),
                        rs.getString("date"),
                        rs.getString("status"),
                        rs.getString("type"),
                        "Detail",
                        "Hapus"
                });
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void refreshAdminResponList(JPanel panel) {
        JPanel listContainer = (JPanel) panel.getClientProperty("listContainer");
        listContainer.removeAll();
        try (Connection c = Database.getConnection(); ResultSet rs = Database.getAllComplaints(c)) {
            while (rs.next()) {
                String status = rs.getString("status");
                if(!"Selesai".equals(status)) {
                    Complaint cpl = new Complaint();
                    cpl.id = rs.getString("id");
                    cpl.title = rs.getString("title");
                    cpl.type = rs.getString("type");
                    cpl.description = rs.getString("description");
                    cpl.studentName = rs.getString("studentName");
                    cpl.lecturerResponse = rs.getString("lecturerResponse");
                    cpl.status = status;

                    JPanel card = new JPanel(new BorderLayout());
                    card.setBackground(Color.WHITE);
                    card.setBorder(BorderFactory.createCompoundBorder(new EmptyBorder(0,0,15,0), new javax.swing.border.LineBorder(new Color(230,230,230), 1, true)));
                    card.setMaximumSize(new Dimension(2000, 100));

                    JPanel info = new JPanel(new GridLayout(2, 1));
                    info.setBackground(Color.WHITE);
                    info.setBorder(new EmptyBorder(15, 20, 15, 10));
                    JLabel lblTitle = new JLabel("Pengaduan " + cpl.id + " (" + cpl.type + ")");
                    lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
                    JLabel lblSub = new JLabel(cpl.title); lblSub.setForeground(Color.GRAY);
                    info.add(lblTitle); info.add(lblSub);

                    JPanel action = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                    action.setBackground(Color.WHITE);
                    action.setBorder(new EmptyBorder(25, 0, 0, 20));
                    JLabel lblFrom = new JLabel("Dari : " + cpl.studentName + " ");
                    lblFrom.setFont(new Font("SansSerif", Font.PLAIN, 14));
                    JButton btnRespon = new JButton("Respon");
                    btnRespon.setOpaque(true); btnRespon.setBorderPainted(false);
                    btnRespon.setBackground(new Color(66, 133, 244)); btnRespon.setForeground(Color.WHITE);
                    btnRespon.setFont(new Font("SansSerif", Font.BOLD, 12));
                    btnRespon.setPreferredSize(new Dimension(100, 35));
                    btnRespon.setFocusPainted(false);
                    btnRespon.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    btnRespon.addActionListener(e -> {
                        selectedAdminComplaint = cpl;
                        try {
                            if("Menunggu Verifikasi".equals(cpl.status)) Database.updateStatus(cpl.id, "Diproses");
                        } catch (SQLException ex) { ex.printStackTrace(); }
                        CardLayout cl = (CardLayout) adminContentPanel.getLayout();
                        cl.show(adminContentPanel, "FormRespon");
                        Component[] comps = adminContentPanel.getComponents();
                        for(Component cp : comps) if(cp.isVisible()) cp.dispatchEvent(new java.awt.event.ComponentEvent(cp, java.awt.event.ComponentEvent.COMPONENT_SHOWN));
                    });
                    action.add(lblFrom);
                    action.add(btnRespon);

                    card.add(info, BorderLayout.CENTER);
                    card.add(action, BorderLayout.EAST);
                    listContainer.add(card);
                }
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
        listContainer.revalidate(); listContainer.repaint();
    }

    private JLabel createLabel(String text) { JLabel l = new JLabel(text); l.setFont(new Font("SansSerif", Font.PLAIN, 16)); return l; }

    // ==========================================
    // 4. MANAJEMEN PANEL
    // ==========================================
    private JPanel createManagementMainPanel() {
        JPanel container = new JPanel(new BorderLayout());
        JPanel sidebar = createSidebarWithProfile("Manajemen", new String[]{"Dashboard", "Data Pengguna", "Data Aduan"});
        container.add(sidebar, BorderLayout.WEST);

        mgmtContentPanel = new JPanel(new CardLayout());
        mgmtContentPanel.setBackground(BG_LIGHT);

        mgmtDashPanel = createMgmtDashUI();
        JPanel userPanel = createMgmtUserUI();
        JPanel reportPanel = createMgmtReportUI();

        mgmtContentPanel.add(mgmtDashPanel, "Dashboard");
        mgmtContentPanel.add(userPanel, "Data Pengguna");
        mgmtContentPanel.add(reportPanel, "Data Aduan");
        container.add(mgmtContentPanel, BorderLayout.CENTER);

        CardLayout cl = (CardLayout) mgmtContentPanel.getLayout();
        for (NavButton btn : sidebarButtons) {
            btn.addActionListener(e -> {
                for (NavButton b : sidebarButtons) b.setActive(false);
                btn.setActive(true);
                String cmd = btn.getText();
                if (cmd.equals("Dashboard")) {
                    refreshMgmtDash();
                    cl.show(mgmtContentPanel, "Dashboard");
                } else if (cmd.equals("Data Pengguna")) {
                    refreshMgmtUser(userPanel);
                    cl.show(mgmtContentPanel, "Data Pengguna");
                } else if (cmd.equals("Data Aduan")) {
                    refreshMgmtReport(reportPanel);
                    cl.show(mgmtContentPanel, "Data Aduan");
                }
            });
        }
        return container;
    }

    private JPanel createMgmtDashUI() {
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(BG_LIGHT);
        mainContainer.setBorder(new EmptyBorder(30, 40, 30, 40));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_LIGHT);
        JLabel title = new JLabel("DASHBOARD MANAJEMEN");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(TEXT_DARK);
        header.add(title, BorderLayout.WEST);
        header.add(new JLabel(new TextIcon("\uD83D\uDD14", TEXT_DARK)), BorderLayout.EAST);

        JPanel statsContainer = new JPanel(new GridLayout(1, 3, 25, 0));
        statsContainer.setBackground(BG_LIGHT);
        statsContainer.setPreferredSize(new Dimension(0, 130));

        JPanel statsWrapper = new JPanel(new BorderLayout());
        statsWrapper.setBackground(BG_LIGHT);
        statsWrapper.setBorder(new EmptyBorder(30, 0, 30, 0));
        statsWrapper.add(statsContainer, BorderLayout.CENTER);

        JPanel chartSection = new JPanel(new BorderLayout());
        chartSection.setBackground(BG_LIGHT);
        JLabel lblChart = new JLabel("Grafik Aduan");
        lblChart.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblChart.setForeground(TEXT_DARK);
        lblChart.setBorder(new EmptyBorder(0, 0, 15, 0));

        CardPanel chartCard = new CardPanel();
        chartCard.setLayout(new BorderLayout());
        chartCard.setBackground(Color.WHITE);
        chartCard.setPreferredSize(new Dimension(0, 350));

        SimplePieChartPanel piePanel = new SimplePieChartPanel();
        chartCard.add(piePanel, BorderLayout.CENTER);
        chartSection.add(lblChart, BorderLayout.NORTH);
        chartSection.add(chartCard, BorderLayout.CENTER);

        mainContainer.add(header, BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(BG_LIGHT);
        body.add(statsWrapper, BorderLayout.NORTH);
        body.add(chartSection, BorderLayout.CENTER);
        mainContainer.add(body, BorderLayout.CENTER);
        mainContainer.putClientProperty("statsContainer", statsContainer);
        mainContainer.putClientProperty("chart", piePanel);
        return mainContainer;
    }

    private JPanel createMgmtUserUI() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_LIGHT);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("DATA PENGGUNA");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setBorder(new EmptyBorder(0, 0, 20, 0));

        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        String[] cols = {"ID", "Nama", "Role", "PASSWORD"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = new JTable(model);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        JPanel bottomCtrl = new JPanel(new BorderLayout());
        bottomCtrl.setBackground(BG_LIGHT);
        bottomCtrl.setBorder(new EmptyBorder(20, 0, 0, 0));

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnGroup.setBackground(BG_LIGHT);
        JButton btnAdd = createFlatButton("Tambah", new Color(46, 204, 113));
        JButton btnEdit = createFlatButton("Edit", new Color(255, 193, 7));
        JButton btnDel = createFlatButton("Hapus", STATUS_RED);

        btnAdd.addActionListener(e -> { showUserForm(null, panel); });
        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Pilih user yang ingin diedit!"); return; }
            String id = (String) table.getValueAt(row, 0);
            try (Connection c = Database.getConnection(); ResultSet rs = c.createStatement().executeQuery("SELECT * FROM users WHERE id='"+id+"'")) {
                if (rs.next()) {
                    User u = new User(rs.getString("id"), rs.getString("name"), rs.getString("password"), rs.getString("role"));
                    showUserForm(u, panel);
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        });
        btnDel.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) { JOptionPane.showMessageDialog(this, "Pilih user yang ingin dihapus!"); return; }
            String id = (String) table.getValueAt(row, 0);
            String nama = (String) table.getValueAt(row, 1);
            int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus user: " + nama + "?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    Database.deleteUser(id);
                    refreshMgmtUser(panel);
                    refreshMgmtDash();
                    JOptionPane.showMessageDialog(this, "User berhasil dihapus.");
                } catch (SQLException ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, "Gagal menghapus user."); }
            }
        });
        btnGroup.add(btnAdd); btnGroup.add(btnEdit); btnGroup.add(btnDel);

        JPanel searchGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        searchGroup.setBackground(BG_LIGHT);
        JTextField txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(200, 35));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY), BorderFactory.createEmptyBorder(0, 5, 0, 5)));
        JButton btnCari = createFlatButton("Cari", PRIMARY_BLUE);
        btnCari.setPreferredSize(new Dimension(60, 35));
        btnCari.addActionListener(e -> {
            String keyword = txtSearch.getText().toLowerCase();
            model.setRowCount(0);
            try (Connection c = Database.getConnection(); ResultSet rs = Database.getAllUsers(c)) {
                while (rs.next()) {
                    String nm = rs.getString("name"), id = rs.getString("id");
                    if(nm.toLowerCase().contains(keyword) || id.contains(keyword)) {
                        model.addRow(new Object[]{id, nm, rs.getString("role"), rs.getString("password")});
                    }
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        });
        searchGroup.add(txtSearch); searchGroup.add(Box.createHorizontalStrut(5)); searchGroup.add(btnCari);

        bottomCtrl.add(btnGroup, BorderLayout.WEST);
        bottomCtrl.add(searchGroup, BorderLayout.EAST);

        panel.add(title, BorderLayout.NORTH);
        panel.add(card, BorderLayout.CENTER);
        panel.add(bottomCtrl, BorderLayout.SOUTH);
        panel.putClientProperty("model", model);
        return panel;
    }

    private void showUserForm(User existingUser, JPanel parentPanel) {
        JDialog dialog = new JDialog(this, existingUser == null ? "Tambah User" : "Edit User", true);
        dialog.setSize(400, 400);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(0, 1, 10, 10));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        form.setBackground(Color.WHITE);

        JTextField txtId = new JTextField();
        JTextField txtName = new JTextField();
        JTextField txtPass = new JTextField();
        String[] roles = {"MAHASISWA", "DOSEN", "ADMIN", "MANAJEMEN"};
        JComboBox<String> cmbRole = new JComboBox<>(roles);
        cmbRole.setBackground(Color.WHITE);

        form.add(new JLabel("ID / NIM / NIP")); form.add(txtId);
        form.add(new JLabel("Nama Lengkap")); form.add(txtName);
        form.add(new JLabel("Role")); form.add(cmbRole);
        form.add(new JLabel("Password")); form.add(txtPass);

        if (existingUser != null) {
            txtId.setText(existingUser.id);
            txtId.setEditable(false);
            txtName.setText(existingUser.name);
            txtPass.setText(existingUser.password);
            cmbRole.setSelectedItem(existingUser.role);
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(new Color(245, 245, 245));
        btnPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton btnSave = new JButton("Simpan");
        btnSave.setOpaque(true);
        btnSave.setBorderPainted(false);
        btnSave.setFocusPainted(false);
        btnSave.setBackground(PRIMARY_BLUE);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnSave.setPreferredSize(new Dimension(100, 35));
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnSave.addActionListener(e -> {
            if (txtId.getText().isEmpty() || txtName.getText().isEmpty() || txtPass.getText().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Semua field harus diisi!"); return;
            }
            try {
                if (existingUser == null) {
                    User u = new User(txtId.getText(), txtName.getText(), txtPass.getText(), (String)cmbRole.getSelectedItem());
                    Database.addUser(u);
                    JOptionPane.showMessageDialog(dialog, "User berhasil ditambahkan!");
                } else {
                    existingUser.name = txtName.getText();
                    existingUser.role = (String) cmbRole.getSelectedItem();
                    existingUser.password = txtPass.getText();
                    Database.updateUser(existingUser);
                    JOptionPane.showMessageDialog(dialog, "Data user diperbarui!");
                }
                refreshMgmtUser(parentPanel);
                refreshMgmtDash();
                dialog.dispose();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Gagal menyimpan user");
            }
        });

        btnPanel.add(btnSave);
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel createMgmtReportUI() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_LIGHT);
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        // --- HEADER ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_LIGHT);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("DATA ADUAN");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));

        String[] filters = {"Semua", "Fasilitas", "Akademik", "Lain-lain"};
        JComboBox<String> cmbFilter = new JComboBox<>(filters);
        cmbFilter.setPreferredSize(new Dimension(150, 35));
        cmbFilter.setBackground(Color.WHITE);
        // Refresh table saat filter berubah
        cmbFilter.addActionListener(e -> refreshMgmtReport(panel));

        header.add(title, BorderLayout.WEST);
        header.add(cmbFilter, BorderLayout.EAST);

        // --- TABLE ---
        CardPanel card = new CardPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        String[] cols = {"ID", "Judul", "Tanggal", "Status", ""};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 4; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(50);
        table.setShowVerticalLines(false);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));

        table.getColumn("Status").setCellRenderer(new StatusRenderer());
        table.getColumn("").setCellRenderer(new HistoryDetailRenderer());
        table.getColumn("").setCellEditor(new AdminDetailEditor()); // Pakai editor detail admin agar info lengkap
        table.getColumn("").setMaxWidth(100);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        // --- FOOTER (TOMBOL EKSPOR) ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(BG_LIGHT);
        footer.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton btnEkspor = createFlatButton("Ekspor (.CSV)", PRIMARY_BLUE);
        btnEkspor.setPreferredSize(new Dimension(140, 40));

        // LOGIKA EKSPOR LAPORAN
        btnEkspor.addActionListener(e -> {
            // 1. Pilih Lokasi Penyimpanan
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Simpan Laporan");
            fileChooser.setSelectedFile(new java.io.File("Laporan_Aduan_" + System.currentTimeMillis() + ".csv"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File fileToSave = fileChooser.getSelectedFile();
                // Tambahkan ekstensi .csv jika user lupa
                if (!fileToSave.getAbsolutePath().endsWith(".csv")) {
                    fileToSave = new java.io.File(fileToSave.getAbsolutePath() + ".csv");
                }

                try (java.io.FileWriter fw = new java.io.FileWriter(fileToSave)) {
                    // 2. Tulis Header CSV
                    fw.write("ID,Judul,Tipe,Status,Tanggal,Pelapor,Deskripsi,Respon Dosen,Respon Admin\n");

                    // 3. Ambil Data dari Database berdasarkan Filter yang sedang aktif
                    String filter = (String) cmbFilter.getSelectedItem();
                    String sql = "SELECT * FROM complaints";
                    if (!"Semua".equals(filter)) sql += " WHERE type='" + filter + "'";
                    sql += " ORDER BY date DESC";

                    try (Connection c = Database.getConnection();
                         Statement s = c.createStatement();
                         ResultSet rs = s.executeQuery(sql)) {

                        while (rs.next()) {
                            // 4. Format Baris CSV (Pakai escapeCsv agar koma dalam teks tidak merusak format)
                            String line = String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                                    rs.getString("id"),
                                    escapeCsv(rs.getString("title")),
                                    rs.getString("type"),
                                    rs.getString("status"),
                                    rs.getString("date"),
                                    escapeCsv(rs.getString("studentName")),
                                    escapeCsv(rs.getString("description")),
                                    escapeCsv(rs.getString("lecturerResponse")),
                                    escapeCsv(rs.getString("adminResponse"))
                            );
                            fw.write(line);
                        }
                    }

                    JOptionPane.showMessageDialog(this, "Laporan berhasil diekspor ke:\n" + fileToSave.getAbsolutePath());

                    // Opsional: Buka file otomatis (Desktop integration)
                    try {
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().open(fileToSave);
                        }
                    } catch (Exception ignored) {}

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Gagal mengekspor data: " + ex.getMessage());
                }
            }
        });

        footer.add(btnEkspor);

        panel.add(header, BorderLayout.NORTH);
        panel.add(card, BorderLayout.CENTER);
        panel.add(footer, BorderLayout.SOUTH);

        panel.putClientProperty("model", model);
        panel.putClientProperty("filter", cmbFilter);
        return panel;
    }

    // Helper untuk format CSV (Agar teks yang mengandung koma atau enter tidak error)
    private String escapeCsv(String text) {
        if (text == null) return "-";
        // Ganti tanda petik dua (") menjadi double ("") sesuai standar CSV
        text = text.replace("\"", "\"\"");
        // Hapus enter/baris baru agar rapi dalam satu sel Excel
        text = text.replace("\n", " ").replace("\r", " ");
        return text;
    }

    private void refreshMgmtDash() {
        if (mgmtDashPanel == null) return;
        JPanel statsContainer = (JPanel) mgmtDashPanel.getClientProperty("statsContainer");
        SimplePieChartPanel chart = (SimplePieChartPanel) mgmtDashPanel.getClientProperty("chart");

        int dosen = 0, mhs = 0, adm = 0;
        int fasilitas = 0, akademik = 0, lain = 0;

        try (Connection c = Database.getConnection()) {
            dosen = Database.countUsersByRole(c, "DOSEN");
            mhs = Database.countUsersByRole(c, "MAHASISWA");
            adm = Database.countUsersByRole(c, "ADMIN");
            fasilitas = Database.countByType(c, "Fasilitas");
            akademik = Database.countByType(c, "Akademik");
            lain = Database.countByType(c, "Lain-lain");
        } catch (SQLException ex) { ex.printStackTrace(); }

        statsContainer.removeAll();
        statsContainer.add(new ModernStatCard("Total Dosen", String.valueOf(dosen), "\uD83D\uDC68\u200D\uD83C\uDFEB", new Color(0, 150, 136)));
        statsContainer.add(new ModernStatCard("Total Mahasiswa", String.valueOf(mhs), "\uD83C\uDF93", PRIMARY_BLUE));
        statsContainer.add(new ModernStatCard("Total Admin", String.valueOf(adm), "\uD83D\uDC64", new Color(255, 87, 34)));
        statsContainer.revalidate(); statsContainer.repaint();

        if (chart != null) chart.updateData(fasilitas, akademik, lain);
    }

    private void refreshMgmtUser(JPanel p) {
        if(p == null) return;
        DefaultTableModel m = (DefaultTableModel) p.getClientProperty("model");
        m.setRowCount(0);
        try (Connection c = Database.getConnection(); ResultSet rs = Database.getAllUsers(c)) {
            while (rs.next()) {
                m.addRow(new Object[]{rs.getString("id"), rs.getString("name"), rs.getString("role"), rs.getString("password")});
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void refreshMgmtReport(JPanel p) {
        DefaultTableModel m = (DefaultTableModel) p.getClientProperty("model");
        JComboBox filterBox = (JComboBox) p.getClientProperty("filter");
        String filter = (String) filterBox.getSelectedItem();
        m.setRowCount(0);

        String sql = "SELECT * FROM complaints";
        if (!"Semua".equals(filter)) sql += " WHERE type=?";
        sql += " ORDER BY date DESC";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (!"Semua".equals(filter)) ps.setString(1, filter);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                m.addRow(new Object[]{rs.getString("id"), rs.getString("title"), rs.getString("date"), rs.getString("status"), "Detail"});
            }
        } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private JButton createFlatButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(100, 35));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // --- HELPER CLASSES (UI & TABLE RENDERERS/EDITORS) ---
    class CardPanel extends JPanel { public CardPanel() { setOpaque(false); }
        protected void paintComponent(Graphics g) {
            Graphics2D g2=(Graphics2D)g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(220,220,220)); g2.fillRoundRect(2,2,getWidth()-4,getHeight()-4,20,20);
            g2.setColor(Color.WHITE); g2.fillRoundRect(0,0,getWidth()-4,getHeight()-6,20,20); g2.dispose(); super.paintComponent(g);
        }
    }
    private JPanel createStatItem(String n, String l) { JPanel p=new JPanel(new GridBagLayout()); p.setOpaque(false); GridBagConstraints c=new GridBagConstraints(); c.gridx=0; c.gridy=0; JLabel nL=new JLabel(n); nL.setFont(new Font("SansSerif",Font.BOLD,36)); nL.setForeground(TEXT_DARK); JLabel lL=new JLabel(l); lL.setFont(new Font("SansSerif",Font.PLAIN,14)); lL.setForeground(TEXT_GRAY); p.add(nL,c); c.gridy=1; c.insets=new Insets(5,0,0,0); p.add(lL,c); return p;}
    private static class RoundedBorder implements Border { private int r; RoundedBorder(int r) { this.r=r; } public Insets getBorderInsets(Component c) { return new Insets(r+1,r+1,r+2,r); } public boolean isBorderOpaque() { return true; }
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) { Graphics2D g2=(Graphics2D)g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(Color.LIGHT_GRAY); g2.drawRoundRect(x,y,w-1,h-1,r,r); }
    }
    private static class TextIcon implements Icon { private String s; private Color c; public TextIcon(String s, Color c) { this.s=s; this.c=c; } public void paintIcon(Component cmp, Graphics g, int x, int y) { g.setColor(c); g.drawString(s, x, y+10); } public int getIconWidth() { return 20; } public int getIconHeight() { return 20; } }
    class StatusRenderer extends DefaultTableCellRenderer { public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) { Component cp=super.getTableCellRendererComponent(t,v,s,f,r,c); String st=(String)v; if(st.contains("Selesai"))setForeground(STATUS_GREEN); else if(st.contains("Diproses")||st.contains("Ditanggapi"))setForeground(STATUS_YELLOW); else setForeground(TEXT_GRAY); setFont(new Font("SansSerif",Font.BOLD,14)); return cp; } }
    private void styleGrayButton(JButton btn) { btn.setBackground(new Color(220, 220, 220)); btn.setForeground(TEXT_DARK); btn.setFont(new Font("SansSerif", Font.BOLD, 12)); btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setOpaque(true); btn.setBorder(new EmptyBorder(8, 15, 8, 15)); btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); }

    class HistoryDetailRenderer extends JButton implements TableCellRenderer {
        public HistoryDetailRenderer() { setOpaque(true); setBorderPainted(false); setFocusPainted(false); setFont(new Font("SansSerif", Font.BOLD, 12)); setBackground(new Color(220, 220, 220)); setForeground(TEXT_DARK); setText("Detail"); }
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) { return this; }
    }
    class HistoryDetailEditor extends AbstractCellEditor implements TableCellEditor, java.awt.event.ActionListener {
        private JButton button; private JTable currentTable; private int currentRow;
        public HistoryDetailEditor() { button = new JButton("Detail"); button.setOpaque(true); button.setBorderPainted(false); button.setFocusPainted(false); button.setFont(new Font("SansSerif", Font.BOLD, 12)); button.setBackground(new Color(220, 220, 220)); button.setForeground(TEXT_DARK); button.addActionListener(this); }
        public Object getCellEditorValue() { return "Detail"; }
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) { this.currentTable = table; this.currentRow = row; return button; }
        public void actionPerformed(java.awt.event.ActionEvent e) {
            fireEditingStopped();
            try {
                String judul = (String) currentTable.getValueAt(currentRow, 0);
                String tanggal = (String) currentTable.getValueAt(currentRow, 1);
                String status = (String) currentTable.getValueAt(currentRow, 2);
                JOptionPane.showMessageDialog(null, "Detail Riwayat:\n\n" + "Judul: " + judul + "\n" + "Tanggal: " + tanggal + "\n" + "Status: " + status, "Detail Aduan", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    class AdminDetailEditor extends AbstractCellEditor implements TableCellEditor, java.awt.event.ActionListener {
        JButton b;
        JTable t;
        int r;

        public AdminDetailEditor() {
            b = new JButton("Detail");
            b.setOpaque(true);
            b.setBorderPainted(false);
            b.setBackground(new Color(220, 220, 220));
            b.setFont(new Font("SansSerif", Font.BOLD, 12));
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            b.addActionListener(this);
        }

        public Object getCellEditorValue() { return "Detail"; }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            t = table;
            r = row;
            return b;
        }

        public void actionPerformed(java.awt.event.ActionEvent e) {
            fireEditingStopped();
            try {
                // 1. AMBIL ID DARI TABEL (Kolom index 0 yang kita sembunyikan tadi)
                String idComplaint = (String) t.getModel().getValueAt(r, 0);

                // 2. QUERY LENGKAP KE DATABASE
                String detailText = "";
                try (Connection c = Database.getConnection();
                     PreparedStatement ps = c.prepareStatement("SELECT * FROM complaints WHERE id=?")) {
                    ps.setString(1, idComplaint);
                    ResultSet rs = ps.executeQuery();

                    if (rs.next()) {
                        // Helper untuk handle null string
                        String judul = rs.getString("title");
                        String desc = rs.getString("description");
                        String fileMhs = rs.getString("studentAttachment");

                        String respDosen = rs.getString("lecturerResponse");
                        String fileDosen = rs.getString("lecturerAttachment");

                        String respAdmin = rs.getString("adminResponse");
                        String fileAdmin = rs.getString("adminAttachment");

                        // Format Tampilan Popup
                        StringBuilder sb = new StringBuilder();
                        sb.append("=== DETAIL PENGADUAN ===\n");
                        sb.append("Judul: ").append(judul).append("\n");
                        sb.append("Deskripsi:\n").append(desc).append("\n");
                        sb.append("Lampiran Mahasiswa: ").append(fileMhs != null && !fileMhs.isEmpty() ? fileMhs : "-").append("\n\n");

                        sb.append("--- TANGGAPAN DOSEN ---\n");
                        if (respDosen != null && !respDosen.isEmpty()) {
                            sb.append(respDosen).append("\n");
                            sb.append("Lampiran Dosen: ").append(fileDosen != null && !fileDosen.isEmpty() ? fileDosen : "-").append("\n");
                        } else {
                            sb.append("(Belum ada tanggapan)\n");
                        }
                        sb.append("\n");

                        sb.append("--- RESPON ADMIN ---\n");
                        if (respAdmin != null && !respAdmin.isEmpty()) {
                            sb.append(respAdmin).append("\n");
                            sb.append("Lampiran Admin: ").append(fileAdmin != null && !fileAdmin.isEmpty() ? fileAdmin : "-").append("\n");
                        } else {
                            sb.append("(Belum ada respon)\n");
                        }

                        detailText = sb.toString();
                    }
                }

                // 3. TAMPILKAN DI JOPTIONPANE DENGAN SCROLLPANE
                JTextArea ta = new JTextArea(detailText);
                ta.setEditable(false);
                ta.setLineWrap(true);
                ta.setWrapStyleWord(true);
                ta.setFont(new Font("Monospaced", Font.PLAIN, 13)); // Pakai Monospaced agar rapi

                JScrollPane scroll = new JScrollPane(ta);
                scroll.setPreferredSize(new Dimension(500, 400));

                JOptionPane.showMessageDialog(null, scroll, "Detail Data Pengaduan", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Gagal memuat detail: " + ex.getMessage());
            }
        }
    }

    class AdminDeleteRenderer extends JButton implements TableCellRenderer {
        public AdminDeleteRenderer() { setOpaque(true); setBorderPainted(false); setBackground(Color.WHITE); setIcon(new TextIcon("\uD83D\uDDD1", Color.RED)); setText(""); }
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) { return this; }
    }
    class AdminDeleteEditor extends AbstractCellEditor implements TableCellEditor, java.awt.event.ActionListener {
        JButton b; JTable t; int r;
        public AdminDeleteEditor() { b = new JButton(); b.setOpaque(true); b.setBorderPainted(false); b.setBackground(Color.WHITE); b.setIcon(new TextIcon("\uD83D\uDDD1", Color.RED)); b.addActionListener(this); }
        public Object getCellEditorValue() { return ""; }
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) { t=table; r=row; return b; }
        public void actionPerformed(java.awt.event.ActionEvent e) {
            fireEditingStopped();
            int confirm = JOptionPane.showConfirmDialog(null, "Hapus pengaduan ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if(confirm == JOptionPane.YES_OPTION) {
                try {
                    // Ambil judul di kolom 0 => kita tidak punya ID disini, jadi hapus berdasar posisi bukan aman.
                    // Disarankan: tambahkan kolom hidden ID di model jika ingin delete yang akurat.
                    JOptionPane.showMessageDialog(null, "Silakan hapus melalui halaman Dashboard (Lihat) agar ID akurat.");
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }
    }

    class AdminDashboardActionRenderer extends JButton implements TableCellRenderer {
        public AdminDashboardActionRenderer() { setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setFocusPainted(false); setForeground(Color.WHITE); setFont(new Font("SansSerif", Font.BOLD, 12)); setCursor(new Cursor(Cursor.HAND_CURSOR)); }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(PRIMARY_BLUE);
            g2.fillRoundRect(10, 8, getWidth() - 20, getHeight() - 16, 20, 20);
            g2.dispose();
            super.paintComponent(g);
        }
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) { setText("Lihat"); return this; }
    }
    class AdminDashboardActionEditor extends AbstractCellEditor implements TableCellEditor, java.awt.event.ActionListener {
        private JButton button; private JTable currentTable; private int currentRow;
        public AdminDashboardActionEditor() {
            button = new JButton("Lihat") {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(PRIMARY_BLUE);
                    g2.fillRoundRect(10, 8, getWidth() - 20, getHeight() - 16, 20, 20);
                    g2.dispose(); super.paintComponent(g);
                }
            };
            button.setOpaque(false); button.setContentAreaFilled(false); button.setBorderPainted(false); button.setFocusPainted(false);
            button.setForeground(Color.WHITE); button.setFont(new Font("SansSerif", Font.BOLD, 12)); button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.addActionListener(this);
        }
        public Object getCellEditorValue() { return "Lihat"; }
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) { this.currentTable = table; this.currentRow = row; return button; }
        public void actionPerformed(java.awt.event.ActionEvent e) {
            fireEditingStopped();
            try {
                String idComplaint = (String) currentTable.getModel().getValueAt(currentRow, 0);
                try (Connection c = Database.getConnection();
                     PreparedStatement ps = c.prepareStatement("SELECT * FROM complaints WHERE id=?")) {
                    ps.setString(1, idComplaint);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        selectedAdminComplaint = new Complaint();
                        selectedAdminComplaint.id = rs.getString("id");
                        selectedAdminComplaint.title = rs.getString("title");
                        selectedAdminComplaint.type = rs.getString("type");
                        selectedAdminComplaint.description = rs.getString("description");
                        selectedAdminComplaint.status = rs.getString("status");
                        selectedAdminComplaint.studentName = rs.getString("studentName");
                        selectedAdminComplaint.lecturerResponse = rs.getString("lecturerResponse");

                        if("Menunggu Verifikasi".equals(selectedAdminComplaint.status)) {
                            Database.updateStatus(selectedAdminComplaint.id, "Diproses");
                        }
                        CardLayout cl = (CardLayout) adminContentPanel.getLayout();
                        cl.show(adminContentPanel, "FormRespon");
                        Component[] comps = adminContentPanel.getComponents();
                        for(Component cp : comps) if(cp.isVisible()) cp.dispatchEvent(new java.awt.event.ComponentEvent(cp, java.awt.event.ComponentEvent.COMPONENT_SHOWN));
                    }
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    class LecturerActionRenderer extends JButton implements TableCellRenderer {
        public LecturerActionRenderer() {
            setOpaque(true); setBorderPainted(false); setFocusPainted(false);
            setFont(new Font("SansSerif", Font.BOLD, 12)); setForeground(Color.WHITE);
        }
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            String txt = (String) v; setText(txt);
            setBackground("Tanggapi".equals(txt) ? STATUS_RED : STATUS_GREEN);
            return this;
        }
    }
    class LecturerActionEditor extends AbstractCellEditor implements TableCellEditor, java.awt.event.ActionListener {
        private JButton button; private String label; private JTable currentTable; private int currentRow;
        public LecturerActionEditor() {
            button = new JButton(); button.setOpaque(true); button.setBorderPainted(false); button.setFocusPainted(false);
            button.setFont(new Font("SansSerif", Font.BOLD, 12)); button.setForeground(Color.WHITE); button.addActionListener(this);
        }
        public Object getCellEditorValue() { return label; }
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.currentTable = table; this.currentRow = row; this.label = (String) value; button.setText(label);
            button.setBackground("Tanggapi".equals(label) ? STATUS_RED : STATUS_GREEN);
            return button;
        }
        public void actionPerformed(java.awt.event.ActionEvent e) {
            fireEditingStopped();
            if ("Tanggapi".equals(label)) {
                try {
                    String idComplaint = (String) currentTable.getModel().getValueAt(currentRow, 0);
                    try (Connection c = Database.getConnection();
                         PreparedStatement ps = c.prepareStatement("SELECT * FROM complaints WHERE id=?")) {
                        ps.setString(1, idComplaint);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            selectedComplaint = new Complaint();
                            selectedComplaint.id = rs.getString("id");
                            selectedComplaint.title = rs.getString("title");
                            selectedComplaint.description = rs.getString("description");
                            CardLayout cl = (CardLayout) lecturerContentPanel.getLayout();
                            cl.show(lecturerContentPanel, "FormTanggapan");
                            for (Component comp : lecturerContentPanel.getComponents()) {
                                if (comp.isVisible()) comp.dispatchEvent(new java.awt.event.ComponentEvent(comp, java.awt.event.ComponentEvent.COMPONENT_SHOWN));
                            }
                        }
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }
    }

    class SimplePieChartPanel extends JPanel {
        private int vFasilitas, vAkademik, vLain;
        private final Color COL_FASILITAS = new Color(54, 162, 235);
        private final Color COL_AKADEMIK = new Color(255, 99, 132);
        private final Color COL_LAIN = new Color(255, 206, 86);
        private final Color COL_EMPTY = new Color(229, 229, 229);
        public void updateData(int v1, int v2, int v3) { this.vFasilitas = v1; this.vAkademik = v2; this.vLain = v3; repaint(); }
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int total = vFasilitas + vAkademik + vLain;
            int size = Math.min(getWidth(), getHeight()) - 40; if (size > 280) size = 280;
            int x = 40; int y = (getHeight() - size) / 2;

            if (total == 0) { g2.setColor(COL_EMPTY); g2.fillArc(x, y, size, size, 0, 360); }
            else {
                int startAngle = 90;
                int angle1 = (int) Math.round((double) vFasilitas / total * 360);
                g2.setColor(COL_FASILITAS); g2.fillArc(x, y, size, size, startAngle, angle1); startAngle += angle1;
                int angle2 = (int) Math.round((double) vAkademik / total * 360);
                g2.setColor(COL_AKADEMIK); g2.fillArc(x, y, size, size, startAngle, angle2); startAngle += angle2;
                int angle3 = 360 - angle1 - angle2;
                if(vLain > 0) { g2.setColor(COL_LAIN); g2.fillArc(x, y, size, size, startAngle, angle3); }
            }

            int innerSize = size / 2;
            int innerX = x + (size - innerSize) / 2;
            int innerY = y + (size - innerSize) / 2;
            g2.setColor(Color.WHITE);
            g2.fillOval(innerX, innerY, innerSize, innerSize);

            String totalText = String.valueOf(total);
            String labelText = "Total";
            g2.setColor(TEXT_DARK);
            g2.setFont(new Font("SansSerif", Font.BOLD, 28));
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(totalText);
            g2.drawString(totalText, innerX + (innerSize - tw) / 2, innerY + (innerSize / 2) + 5);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2.setColor(Color.GRAY);
            fm = g2.getFontMetrics();
            int lw = fm.stringWidth(labelText);
            g2.drawString(labelText, innerX + (innerSize - lw) / 2, innerY + (innerSize / 2) + 20);

            int lx = x + size + 40; int ly = y + 40;
            drawLegendItem(g2, lx, ly, COL_FASILITAS, "Fasilitas", vFasilitas, total);
            drawLegendItem(g2, lx, ly + 50, COL_AKADEMIK, "Akademik", vAkademik, total);
            drawLegendItem(g2, lx, ly + 100, COL_LAIN, "Lain-lain", vLain, total);
        }
        private void drawLegendItem(Graphics2D g2, int x, int y, Color c, String label, int val, int total) {
            g2.setColor(c); g2.fillOval(x, y, 15, 15);
            g2.setColor(TEXT_DARK); g2.setFont(new Font("SansSerif", Font.BOLD, 14)); g2.drawString(label, x + 25, y + 12);
            String detail = total > 0 ? (val + " Aduan (" + (int) Math.round((double) val / total * 100) + "%)") : "0 Aduan (0%)";
            g2.setColor(Color.GRAY); g2.setFont(new Font("SansSerif", Font.PLAIN, 12)); g2.drawString(detail, x + 25, y + 30);
        }
    }

    class ModernStatCard extends JPanel {
        public ModernStatCard(String title, String value, String icon, Color accentColor) {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(new EmptyBorder(15, 20, 15, 20));
            JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5)); textPanel.setOpaque(false);
            JLabel lblValue = new JLabel(value); lblValue.setFont(new Font("SansSerif", Font.BOLD, 36)); lblValue.setForeground(TEXT_DARK);
            JLabel lblTitle = new JLabel(title); lblTitle.setFont(new Font("SansSerif", Font.PLAIN, 14)); lblTitle.setForeground(Color.GRAY);
            textPanel.add(lblValue); textPanel.add(lblTitle);
            JLabel lblIcon = new JLabel(icon); lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40)); lblIcon.setForeground(accentColor); lblIcon.setVerticalAlignment(SwingConstants.TOP);
            add(textPanel, BorderLayout.CENTER); add(lblIcon, BorderLayout.EAST);
            putClientProperty("accent", accentColor);
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            g2.setColor(new Color(230, 230, 230)); g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
            Color accent = (Color) getClientProperty("accent");
            if (accent != null) { g2.setColor(accent); g2.fillRoundRect(0, 0, 6, getHeight(), 20, 20); g2.fillRect(4, 0, 5, getHeight()); }
            g2.dispose();
        }
    }

    private void styleTable(JTable table) {
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 40));
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBackground(PRIMARY_BLUE); setForeground(Color.WHITE);
                setFont(new Font("SansSerif", Font.BOLD, 14));
                setBorder(new EmptyBorder(0, 10, 0, 0));
                return this;
            }
        };
        header.setDefaultRenderer(headerRenderer);
        table.setRowHeight(50);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(TEXT_DARK);
        DefaultTableCellRenderer cellPaddingRenderer = new DefaultTableCellRenderer();
        cellPaddingRenderer.setBorder(new EmptyBorder(0, 10, 0, 0));
        for (int i = 0; i < table.getColumnCount(); i++) {
            TableCellRenderer currentRenderer = table.getColumnModel().getColumn(i).getCellRenderer();
            boolean isButtonColumn = (currentRenderer instanceof JButton) || (table.getColumnName(i).equals("Aksi")) || (table.getColumnName(i).equals(""));
            if (!isButtonColumn) table.getColumnModel().getColumn(i).setCellRenderer(cellPaddingRenderer);
        }
    }

    // --- HELPER KHUSUS TABEL MAHASISWA (TOMBOL LIHAT) ---

    // 1. Renderer: Agar sel terlihat seperti tombol
    class StudentActionRenderer extends JButton implements TableCellRenderer {
        public StudentActionRenderer() {
            setOpaque(true);
            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(false); // Transparan agar text saja atau style link
            setFont(new Font("SansSerif", Font.BOLD, 12));
            setForeground(new Color(13, 71, 161)); // Warna Biru
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setText("Lihat");
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    // 2. Editor: Agar tombol bisa diklik dan memunculkan Popup
    class StudentActionEditor extends AbstractCellEditor implements TableCellEditor, java.awt.event.ActionListener {
        private JButton button;
        private JTable table;
        private int row;

        public StudentActionEditor() {
            button = new JButton("Lihat");
            button.setOpaque(true);
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setFont(new Font("SansSerif", Font.BOLD, 12));
            button.setForeground(new Color(13, 71, 161));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.addActionListener(this);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.table = table;
            this.row = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "Lihat";
        }

        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            fireEditingStopped(); // Hentikan edit

            // AMBIL DATA DARI MODEL TABEL
            String judul = (String) table.getValueAt(row, 0);
            String tanggal = (String) table.getValueAt(row, 1);
            String status = (String) table.getValueAt(row, 2);

            // Cari detail lengkap (Deskripsi & Respon) dari Database berdasarkan Judul/Siswa (Idealnya pakai ID hidden)
            // Untuk simulasi visual, kita tampilkan info dasar + query simpel
            String detailMsg = "Judul: " + judul + "\nTanggal: " + tanggal + "\nStatus: " + status + "\n\n";

            try (Connection c = Database.getConnection();
                 PreparedStatement ps = c.prepareStatement("SELECT description, lecturerResponse, adminResponse FROM complaints WHERE title=? AND studentId=?")) {
                ps.setString(1, judul);
                ps.setString(2, currentUser.id);
                ResultSet rs = ps.executeQuery();
                if(rs.next()){
                    detailMsg += "Deskripsi:\n" + rs.getString("description") + "\n\n";
                    String respDosen = rs.getString("lecturerResponse");
                    String respAdmin = rs.getString("adminResponse");

                    if(respDosen != null && !respDosen.isEmpty()) detailMsg += "[Tanggapan Dosen]:\n" + respDosen + "\n\n";
                    if(respAdmin != null && !respAdmin.isEmpty()) detailMsg += "[Respon Admin]:\n" + respAdmin;
                }
            } catch (SQLException ex) { ex.printStackTrace(); }

            // TAMPILKAN POPUP
            JTextArea ta = new JTextArea(detailMsg);
            ta.setEditable(false);
            ta.setLineWrap(true);
            ta.setWrapStyleWord(true);
            ta.setSize(new Dimension(400, 300));
            JScrollPane scroll = new JScrollPane(ta);
            scroll.setPreferredSize(new Dimension(400, 300));

            JOptionPane.showMessageDialog(table, scroll, "Detail Pengaduan", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new SistemLayananPengaduan().setVisible(true));
    }
}