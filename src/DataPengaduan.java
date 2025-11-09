import javax.swing.*;
import java.awt.*;

public class DataPengaduan extends JFrame {
    public DataPengaduan() {
        setTitle("Data Pengaduan");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

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
        JButton btnBack = new JButton("Kembali");
        btnBack.addActionListener(e -> {
            new DashboardAdmin().setVisible(true);
            dispose();
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(btnBack, BorderLayout.SOUTH);
    }
}
