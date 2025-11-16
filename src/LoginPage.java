import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginPage extends JFrame {
    private JTextField txtID;
    private JPasswordField txtPassword;

    public LoginPage() {
        setTitle("Sistem Layanan Pengaduan Mahasiswa");
        setSize(400, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel lblTitle = new JLabel("Sistem Layanan Pengaduan Mahasiswa", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBorder(new EmptyBorder(20, 10, 20, 10));
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 51, 102));
        header.add(lblTitle, BorderLayout.CENTER);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        txtID = new JTextField();
        txtPassword = new JPasswordField();
        JCheckBox chkRemember = new JCheckBox("Ingat Saya");
        JButton btnLogin = new JButton("Masuk");

        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);

        JLabel lblID = new JLabel("ID Mahasiswa / Admin:");
        lblID.setFont(labelFont);
        lblID.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtID.setFont(fieldFont);
        txtID.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtID.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(labelFont);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword.setFont(fieldFont);
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        chkRemember.setBackground(Color.WHITE);
        chkRemember.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chkRemember.setAlignmentX(Component.LEFT_ALIGNMENT);

        stylePrimaryButton(btnLogin);
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(lblID);
        form.add(Box.createRigidArea(new Dimension(0, 5)));
        form.add(txtID);
        form.add(Box.createRigidArea(new Dimension(0, 15)));
        form.add(lblPass);
        form.add(Box.createRigidArea(new Dimension(0, 5)));
        form.add(txtPassword);
        form.add(Box.createRigidArea(new Dimension(0, 10)));
        form.add(chkRemember);
        form.add(Box.createRigidArea(new Dimension(0, 20)));
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

    private void stylePrimaryButton(JButton btn) {
        btn.setBackground(new Color(0, 102, 204));
        btn.setForeground(Color.BLUE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 15, 10, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    }
}
