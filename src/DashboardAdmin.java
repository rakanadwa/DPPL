import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DashboardAdmin extends JFrame {
    public DashboardAdmin() {
        setTitle("Dashboard Admin");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        JPanel mainContent = new JPanel(new BorderLayout(10, 10));
        mainContent.setBackground(Color.WHITE);
        mainContent.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel statsPanel = createStatsPanel();
        mainContent.add(statsPanel, BorderLayout.NORTH);

        JPanel tablePanel = new JPanel(new BorderLayout(0, 10));
        tablePanel.setBackground(Color.WHITE);

        JLabel lblDaftar = new JLabel("Daftar Pengaduan");
        lblDaftar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tablePanel.add(lblDaftar, BorderLayout.NORTH);

        String[] kolom = {"Nama", "Judul", "Jenis", "Tanggal", "Status"};
        JTable table = new JTable(getAllAduan(), kolom);
        table.setFillsViewportHeight(true);
        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        JScrollPane scroll = new JScrollPane(table);
        tablePanel.add(scroll, BorderLayout.CENTER);

        mainContent.add(tablePanel, BorderLayout.CENTER);

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

        btnData.addActionListener(e -> { new DataPengaduan().setVisible(true); dispose(); });
        btnRespon.addActionListener(e -> { new ResponPage().setVisible(true); dispose(); });
        btnLaporan.addActionListener(e -> { new LaporanPage().setVisible(true); dispose(); });
        btnLogout.addActionListener(e -> { new LoginPage().setVisible(true); dispose(); });

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

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 15));
        statsPanel.setBackground(Color.WHITE);

        long total = DataStore.daftarPengaduan.size();
        long diproses = DataStore.daftarPengaduan.stream().filter(p -> p.getStatus().equals("Diproses")).count();
        long selesai = DataStore.daftarPengaduan.stream().filter(p -> p.getStatus().equals("Selesai")).count();
        long terkirim = total - diproses - selesai;

        statsPanel.add(createStatCard(String.valueOf(total), "Pengaduan", new Color(220, 240, 255)));
        statsPanel.add(createStatCard(String.valueOf(terkirim), "Terkirim", new Color(255, 245, 220)));
        statsPanel.add(createStatCard(String.valueOf(diproses), "Diproses", new Color(255, 230, 230)));
        statsPanel.add(createStatCard(String.valueOf(selesai), "Selesai", new Color(230, 255, 230)));

        return statsPanel;
    }

    private JPanel createStatCard(String number, String text, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker()),
                new EmptyBorder(15, 20, 15, 20)
        ));

        JLabel lblNumber = new JLabel(number);
        lblNumber.setFont(new Font("Segoe UI", Font.BOLD, 28));
        card.add(lblNumber, BorderLayout.NORTH);

        JLabel lblText = new JLabel(text);
        lblText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        card.add(lblText, BorderLayout.SOUTH);

        return card;
    }

    private Object[][] getAllAduan() {
        Object[][] data = new Object[DataStore.daftarPengaduan.size()][5];
        for (int i = 0; i < DataStore.daftarPengaduan.size(); i++) {
            Pengaduan p = DataStore.daftarPengaduan.get(i);
            data[i][0] = p.getNamaMahasiswa();
            data[i][1] = p.getJudul();
            data[i][2] = p.getJenis();
            data[i][3] = p.getTanggal();
            data[i][4] = p.getStatus();
        }
        return data;
    }
}
