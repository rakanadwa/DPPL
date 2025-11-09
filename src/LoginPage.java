
import javax.swing.*;
import java.awt.*;

public class LoginPage extends JFrame {
    private JTextField txtID;
    private JPasswordField txtPassword;

    public LoginPage() {
        setTitle("Sistem Layanan Pengaduan Mahasiswa");
        setSize(400, 350);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel lblTitle = new JLabel("Sistem Layanan Pengaduan Mahasiswa", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        JPanel header = new JPanel();
        header.setBackground(new Color(0, 51, 102));
        header.add(lblTitle);

        JPanel form = new JPanel(new GridLayout(5, 1, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        txtID = new JTextField();
        txtPassword = new JPasswordField();
        JCheckBox chkRemember = new JCheckBox("Ingat Saya");
        JButton btnLogin = new JButton("Masuk");

        form.add(new JLabel("ID Mahasiswa / Admin:"));
        form.add(txtID);
        form.add(new JLabel("Password:"));
        form.add(txtPassword);
        form.add(chkRemember);
        form.add(btnLogin);

        btnLogin.addActionListener(e -> {
            String id = txtID.getText().trim();
            String pass = new String(txtPassword.getPassword());
            if (id.equalsIgnoreCase("admin") && pass.equals("123")) {
                new DashboardAdmin().setVisible(true);
                dispose();
            } else if (!id.isEmpty() && !pass.isEmpty()) {
                new DashboardMahasiswa(id).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "ID atau Password salah!");
            }
        });

        add(header, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
    }
}
