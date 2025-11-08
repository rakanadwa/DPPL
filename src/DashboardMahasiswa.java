
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class DashboardMahasiswa extends JFrame {
    private String nama;

    public DashboardMahasiswa(String nama) {
        this.nama = nama;
        setTitle("Dashboard Mahasiswa - " + nama);
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel sidebar = new JPanel(new GridLayout(5, 1, 10, 10));
        sidebar.setBackground(new Color(0, 51, 102));

        JButton btnDashboard = new JButton("Dashboard");
        JButton btnBuat = new JButton("Buat Pengaduan");
        JButton btnRiwayat = new JButton("Riwayat Pengaduan");
        JButton btnLogout = new JButton("Logout");

        sidebar.add(btnDashboard);
        sidebar.add(btnBuat);
        sidebar.add(btnRiwayat);
        sidebar.add(new JLabel(""));
        sidebar.add(btnLogout);

        String[] kolom = {"Judul", "Jenis", "Tanggal", "Status"};
        Object[][] data = getDataAduanMahasiswa();
        JTable table = new JTable(data, kolom);
        JScrollPane scroll = new JScrollPane(table);

        add(sidebar, BorderLayout.WEST);
        add(scroll, BorderLayout.CENTER);

        btnBuat.addActionListener(e -> {
            new BuatPengaduan(nama).setVisible(true);
            dispose();
        });
        btnLogout.addActionListener(e -> {
            new LoginPage().setVisible(true);
            dispose();
        });
    }

    private Object[][] getDataAduanMahasiswa() {
        ArrayList<Pengaduan> semua = DataStore.daftarPengaduan;
        ArrayList<Pengaduan> milik = new ArrayList<>();
        for (Pengaduan p : semua)
            if (p.getNamaMahasiswa().equalsIgnoreCase(nama)) milik.add(p);

        Object[][] data = new Object[milik.size()][4];
        for (int i = 0; i < milik.size(); i++) {
            Pengaduan p = milik.get(i);
            data[i][0] = p.getJudul();
            data[i][1] = p.getJenis();
            data[i][2] = p.getTanggal();
            data[i][3] = p.getStatus();
        }
        return data;
    }
}
