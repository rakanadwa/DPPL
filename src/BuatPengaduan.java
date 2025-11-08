
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class BuatPengaduan extends JFrame {
    public BuatPengaduan(String nama) {
        setTitle("Buat Pengaduan");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JTextField txtJudul = new JTextField();
        JComboBox<String> cmbJenis = new JComboBox<>(new String[]{"Akademik", "Fasilitas", "Administrasi"});
        JTextArea txtDeskripsi = new JTextArea(5, 20);
        JButton btnLampirkan = new JButton("Lampirkan Bukti");
        JButton btnKirim = new JButton("Kirim");
        JButton btnBatal = new JButton("Batal");

        form.add(new JLabel("Judul Pengaduan:")); form.add(txtJudul);
        form.add(new JLabel("Jenis Aduan:")); form.add(cmbJenis);
        form.add(new JLabel("Deskripsi:")); form.add(new JScrollPane(txtDeskripsi));
        form.add(new JLabel("Lampiran:")); form.add(btnLampirkan);
        form.add(btnBatal); form.add(btnKirim);

        btnKirim.addActionListener(e -> {
            String judul = txtJudul.getText().trim();
            String jenis = cmbJenis.getSelectedItem().toString();
            String deskripsi = txtDeskripsi.getText().trim();

            if (judul.isEmpty() || deskripsi.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lengkapi semua data!");
                return;
            }
            Pengaduan baru = new Pengaduan(nama, judul, jenis, deskripsi, "Terkirim", LocalDate.now().toString());
            DataStore.daftarPengaduan.add(baru);
            JOptionPane.showMessageDialog(this, "Pengaduan berhasil dikirim!");
            new DashboardMahasiswa(nama).setVisible(true);
            dispose();
        });

        btnBatal.addActionListener(e -> {
            new DashboardMahasiswa(nama).setVisible(true);
            dispose();
        });

        add(form);
    }
}
