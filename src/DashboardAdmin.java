
import javax.swing.*;
import java.awt.*;

public class DashboardAdmin extends JFrame {
    public DashboardAdmin() {
        setTitle("Dashboard Admin");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel sidebar = new JPanel(new GridLayout(5, 1, 10, 10));
        sidebar.setBackground(new Color(0, 51, 102));

        JButton btnData = new JButton("Data Pengaduan");
        JButton btnRespon = new JButton("Respon");
        JButton btnLaporan = new JButton("Laporan");
        JButton btnLogout = new JButton("Logout");

        sidebar.add(btnData);
        sidebar.add(btnRespon);
        sidebar.add(btnLaporan);
        sidebar.add(new JLabel(""));
        sidebar.add(btnLogout);

        String[] kolom = {"Nama", "Judul", "Jenis", "Tanggal", "Status"};
        JTable table = new JTable(getAllAduan(), kolom);
        JScrollPane scroll = new JScrollPane(table);

        add(sidebar, BorderLayout.WEST);
        add(scroll, BorderLayout.CENTER);

        btnData.addActionListener(e -> { new DataPengaduan().setVisible(true); dispose(); });
        btnRespon.addActionListener(e -> { new ResponPage().setVisible(true); dispose(); });
        btnLaporan.addActionListener(e -> { new LaporanPage().setVisible(true); dispose(); });
        btnLogout.addActionListener(e -> { new LoginPage().setVisible(true); dispose(); });
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
