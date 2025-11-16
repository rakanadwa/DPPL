import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LaporanPage extends JFrame {
    public LaporanPage() {
        setTitle("Laporan Pengaduan");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        JPanel mainContent = new JPanel(new BorderLayout(10, 10));
        mainContent.setBackground(Color.WHITE);
        mainContent.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("LAPORAN PENGADUAN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        mainContent.add(lblTitle, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 20));
        contentPanel.setBackground(Color.WHITE);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                "Filter Laporan",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 14)
        ));
        filterPanel.setPreferredSize(new Dimension(0, 80));

        filterPanel.add(new JLabel("Pilih Tanggal:"));
        JComboBox<String> cmbTanggal = new JComboBox<>(new String[]{"Semua Tanggal", "Hari Ini", "Bulan Ini"});
        cmbTanggal.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        filterPanel.add(cmbTanggal);

        JButton btnCetak = new JButton("Cetak");
        stylePrimaryButton(btnCetak, new Color(0, 102, 204));
        filterPanel.add(btnCetak);

        contentPanel.add(filterPanel, BorderLayout.NORTH);

        long total = DataStore.daftarPengaduan.size();
        long terkirim = DataStore.daftarPengaduan.stream().filter(p -> p.getStatus().equals("Terkirim")).count();
        long diproses = DataStore.daftarPengaduan.stream().filter(p -> p.getStatus().equals("Diproses")).count();
        long selesai = DataStore.daftarPengaduan.stream().filter(p -> p.getStatus().equals("Selesai")).count();

        String[][] data = {
                {"Total Pengaduan", String.valueOf(total)},
                {"Terkirim", String.valueOf(terkirim)},
                {"Diproses", String.valueOf(diproses)},
                {"Selesai", String.valueOf(selesai)}
        };

        JTable table = new JTable(data, new String[]{"Kategori", "Jumlah"});
        table.setFillsViewportHeight(true);
        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        contentPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        mainContent.add(contentPanel, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);
    }

    private void stylePrimaryButton(JButton btn, Color fgColor) {
        btn.setBackground(Color.WHITE);
        btn.setForeground(fgColor);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fgColor, 2),
                new EmptyBorder(6, 16, 6, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private JPanel createSidebar() {
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
        JButton btnData = new JButton("Data Pengaduan");
        JButton btnRespon = new JButton("Respon");
        JButton btnLaporan = new JButton("Laporan");
        JButton btnLogout = new JButton("Logout");

        styleSidebarButton(btnDashboard);
        styleSidebarButton(btnData);
        styleSidebarButton(btnRespon);
        styleSidebarButton(btnLaporan);
        styleSidebarButton(btnLogout);

        btnLaporan.setOpaque(true);
        btnLaporan.setBackground(new Color(0, 80, 150));

        btnDashboard.addActionListener(e -> { new DashboardAdmin().setVisible(true); dispose(); });
        btnData.addActionListener(e -> { new DataPengaduan().setVisible(true); dispose(); });
        btnRespon.addActionListener(e -> { new ResponPage().setVisible(true); dispose(); });
        btnLaporan.addActionListener(e -> { });
        btnLogout.addActionListener(e -> { new LoginPage().setVisible(true); dispose(); });

        sidebar.add(btnDashboard);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnData);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnRespon);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnLaporan);
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
