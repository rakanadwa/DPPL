
import javax.swing.*;
import java.awt.*;

public class ResponPage extends JFrame {
    private JTable table;

    public ResponPage() {
        setTitle("Respon Pengaduan");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        String[] kolom = {"No", "Nama", "Judul", "Status"};
        Object[][] data = getData();
        table = new JTable(data, kolom);

        JButton btnProses = new JButton("Tandai Diproses");
        JButton btnSelesai = new JButton("Tandai Selesai");
        JButton btnKembali = new JButton("Kembali");

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnProses); btnPanel.add(btnSelesai); btnPanel.add(btnKembali);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        btnProses.addActionListener(e -> updateStatus("Diproses"));
        btnSelesai.addActionListener(e -> updateStatus("Selesai"));
        btnKembali.addActionListener(e -> { new DashboardAdmin().setVisible(true); dispose(); });
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
}
