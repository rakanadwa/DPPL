
import javax.swing.*;
import java.awt.*;

public class LaporanPage extends JFrame {
    public LaporanPage() {
        setTitle("Laporan Pengaduan");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        int total = DataStore.daftarPengaduan.size();
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
        JButton btnBack = new JButton("Kembali");
        btnBack.addActionListener(e -> { new DashboardAdmin().setVisible(true); dispose(); });

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(btnBack, BorderLayout.SOUTH);
    }
}
