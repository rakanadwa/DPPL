import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;

public class BuatPengaduan extends JFrame {

    private JLabel lblFilePath;
    private String selectedFilePath = "";

    public BuatPengaduan(String nama) {
        setTitle("Buat Pengaduan");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel sidebar = createSidebar(nama);
        add(sidebar, BorderLayout.WEST);

        JPanel mainContent = new JPanel(new BorderLayout(10, 10));
        mainContent.setBackground(Color.WHITE);
        mainContent.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("BUAT PENGADUAN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        mainContent.add(lblTitle, BorderLayout.NORTH);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);

        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        JTextField txtJudul = new JTextField();
        JComboBox<String> cmbJenis = new JComboBox<>(new String[]{"Akademik", "Fasilitas", "Administrasi"});
        JTextArea txtDeskripsi = new JTextArea(8, 20);
        JButton btnKirim = new JButton("Kirim");
        JButton btnBatal = new JButton("Batal");

        JButton btnLampirkan = new JButton("Pilih File...");
        lblFilePath = new JLabel("Tidak ada file dipilih.");
        lblFilePath.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblFilePath.setForeground(Color.DARK_GRAY);

        txtJudul.setFont(fieldFont);
        txtJudul.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        cmbJenis.setFont(fieldFont);
        cmbJenis.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        txtDeskripsi.setFont(fieldFont);
        txtDeskripsi.setLineWrap(true);
        txtDeskripsi.setWrapStyleWord(true);
        JScrollPane deskripsiScroll = new JScrollPane(txtDeskripsi);
        deskripsiScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        styleDefaultButton(btnLampirkan);
        JPanel uploadFieldPanel = new JPanel(new BorderLayout(10, 0));
        uploadFieldPanel.setBackground(Color.WHITE);
        uploadFieldPanel.add(btnLampirkan, BorderLayout.WEST);
        uploadFieldPanel.add(lblFilePath, BorderLayout.CENTER);
        uploadFieldPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionPanel.setBackground(Color.WHITE);
        stylePrimaryButton(btnKirim);
        styleDefaultButton(btnBatal);
        actionPanel.add(btnKirim);
        actionPanel.add(btnBatal);
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        formPanel.add(createFormField("Judul Pengaduan:", txtJudul, labelFont));
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(createFormField("Jenis Aduan:", cmbJenis, labelFont));
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(createFormField("Deskripsi:", deskripsiScroll, labelFont));
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(createFormField("Lampiran (Opsional):", uploadFieldPanel, labelFont));
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(actionPanel);

        mainContent.add(formPanel, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);

        btnLampirkan.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Pilih File Bukti (Opsional)");
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                selectedFilePath = selectedFile.getAbsolutePath();
                lblFilePath.setText(selectedFile.getName());
                lblFilePath.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                lblFilePath.setForeground(Color.BLACK);
            }
        });

        btnKirim.addActionListener(e -> {
            String judul = txtJudul.getText().trim();
            String jenis = cmbJenis.getSelectedItem().toString();
            String deskripsi = txtDeskripsi.getText().trim();

            if (judul.isEmpty() || deskripsi.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Judul dan Deskripsi tidak boleh kosong!");
                return;
            }

            Pengaduan baru = new Pengaduan(
                    nama,
                    judul,
                    jenis,
                    deskripsi,
                    "Terkirim",
                    LocalDate.now().toString(),
                    selectedFilePath
            );

            DataStore.daftarPengaduan.add(baru);
            JOptionPane.showMessageDialog(this, "Pengaduan berhasil dikirim!");
            new DashboardMahasiswa(nama).setVisible(true);
            dispose();
        });

        btnBatal.addActionListener(e -> {
            new DashboardMahasiswa(nama).setVisible(true);
            dispose();
        });
    }

    private JPanel createFormField(String labelText, Component field, Font labelFont) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel(labelText);
        label.setFont(labelFont);
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        int height = (field instanceof JScrollPane) ? 180 : field.getPreferredSize().height + 30;
        if (field instanceof JPanel) height = 65;
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        return panel;
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setBackground(new Color(0, 51, 102));
        btn.setForeground(Color.BLUE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    private void styleDefaultButton(JButton btn) {
        btn.setBackground(new Color(224, 224, 224));
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private JPanel createSidebar(String nama) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(0, 51, 102));
        sidebar.setPreferredSize(new Dimension(220, 600));
        sidebar.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblTitle = new JLabel("SISTEM LAYANAN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTitle.setBorder(new EmptyBorder(10, 5, 20, 5));
        sidebar.add(lblTitle);

        JButton btnDashboard = new JButton("Dashboard");
        JButton btnBuat = new JButton("Buat Pengaduan");
        JButton btnLogout = new JButton("Logout");

        styleSidebarButton(btnDashboard);
        styleSidebarButton(btnBuat);
        styleSidebarButton(btnLogout);

        btnBuat.setOpaque(true);
        btnBuat.setBackground(new Color(0, 80, 150));

        btnDashboard.addActionListener(e -> { new DashboardMahasiswa(nama).setVisible(true); dispose(); });
        btnBuat.addActionListener(e -> { });
        btnLogout.addActionListener(e -> { new LoginPage().setVisible(true); dispose(); });

        sidebar.add(btnDashboard);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnBuat);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(btnLogout);

        return sidebar;
    }

    private void styleSidebarButton(JButton btn) {
        btn.setBackground(new Color(0, 51, 102));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(new EmptyBorder(10, 15, 10, 15));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, btn.getPreferredSize().height));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
}
