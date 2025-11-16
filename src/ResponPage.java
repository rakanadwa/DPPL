import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ResponPage extends JFrame {
    private JTable table;

    public ResponPage() {
        setTitle("Respon Pengaduan");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        JPanel mainContent = new JPanel(new BorderLayout(10, 10));
        mainContent.setBackground(Color.WHITE);
        mainContent.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("RESPON PENGADUAN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        mainContent.add(lblTitle, BorderLayout.NORTH);

        String[] kolom = {"No", "Nama", "Judul", "Status"};
        Object[][] data = getData();
        table = new JTable(data, kolom);
        table.setFillsViewportHeight(true);
        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        mainContent.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnProses = new JButton("Tandai Diproses");
        JButton btnSelesai = new JButton("Tandai Selesai");

        stylePrimaryButton(btnProses, new Color(255, 193, 7));
        stylePrimaryButton(btnSelesai, new Color(40, 167, 69));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(btnProses);
        btnPanel.add(btnSelesai);
        mainContent.add(btnPanel, BorderLayout.SOUTH);

        add(mainContent, BorderLayout.CENTER);

        btnProses.addActionListener(e -> updateStatus("Diproses"));
        btnSelesai.addActionListener(e -> updateStatus("Selesai"));
    }

    private Object[][] getData() {
        Object[][] data = new Object[DataStore.daftarPengaduan.size()][4];
        for (int i = 0; i < DataStore.daftarPengaduan.size(); i++) {
            Pengaduan p = DataStore.daftarPengaduan.get(i);
            data[i][0] = i + 1;
            data[i][1] = p.getNamaMahasiswa();
            data[i][2] = p.getJudul();
            data[i][3] = p.getStatus();
        }
        return data;
    }

    private void updateStatus(String statusBaru) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            DataStore.daftarPengaduan.get(selectedRow).setStatus(statusBaru);
            JOptionPane.showMessageDialog(this, "Status diubah menjadi: " + statusBaru);
            new ResponPage().setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Pilih pengaduan terlebih dahulu!");
        }
    }

    private void stylePrimaryButton(JButton btn, Color fgColor) {
        btn.setBackground(Color.WHITE);
        btn.setForeground(fgColor);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fgColor, 2),
                new EmptyBorder(8, 18, 8, 18)
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

        btnRespon.setOpaque(true);
        btnRespon.setBackground(new Color(0, 80, 150));

        btnDashboard.addActionListener(e -> { new DashboardAdmin().setVisible(true); dispose(); });
        btnData.addActionListener(e -> { new DataPengaduan().setVisible(true); dispose(); });
        btnRespon.addActionListener(e -> { });
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
