import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DataPengaduan extends JFrame {
    public DataPengaduan() {
        setTitle("Data Pengaduan");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        JPanel mainContent = new JPanel(new BorderLayout(10, 10));
        mainContent.setBackground(Color.WHITE);
        mainContent.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("DATA PENGADUAN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        mainContent.add(lblTitle, BorderLayout.NORTH);

        String[] kolom = {"Nama Mahasiswa", "Judul", "Jenis", "Tanggal", "Status"};
        Object[][] data = new Object[DataStore.daftarPengaduan.size()][5];
        for (int i = 0; i < DataStore.daftarPengaduan.size(); i++) {
            Pengaduan p = DataStore.daftarPengaduan.get(i);
            data[i][0] = p.getNamaMahasiswa();
            data[i][1] = p.getJudul();
            data[i][2] = p.getJenis();
            data[i][3] = p.getTanggal();
            data[i][4] = p.getStatus();
        }

        JTable table = new JTable(data, kolom);
        table.setFillsViewportHeight(true);
        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        JScrollPane scroll = new JScrollPane(table);

        mainContent.add(scroll, BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);
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

        JButton btnData = new JButton("Data Pengaduan");
        JButton btnRespon = new JButton("Respon");
        JButton btnLaporan = new JButton("Laporan");
        JButton btnLogout = new JButton("Logout");

        styleSidebarButton(btnData);
        styleSidebarButton(btnRespon);
        styleSidebarButton(btnLaporan);
        styleSidebarButton(btnLogout);

        btnData.setOpaque(true);
        btnData.setBackground(new Color(0, 80, 150));

        JButton btnDashboard = new JButton("Dashboard");
        styleSidebarButton(btnDashboard);
        btnDashboard.addActionListener(e -> { new DashboardAdmin().setVisible(true); dispose(); });

        btnData.addActionListener(e -> { });
        btnRespon.addActionListener(e -> { new ResponPage().setVisible(true); dispose(); });
        btnLaporan.addActionListener(e -> { new LaporanPage().setVisible(true); dispose(); });
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
