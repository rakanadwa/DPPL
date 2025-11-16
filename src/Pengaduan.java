public class Pengaduan {
    private String namaMahasiswa;
    private String judul;
    private String jenis;
    private String deskripsi;
    private String status;
    private String tanggal;
    private String filePath;

    public Pengaduan(String namaMahasiswa, String judul, String jenis,
                     String deskripsi, String status, String tanggal, String filePath) {
        this.namaMahasiswa = namaMahasiswa;
        this.judul = judul;
        this.jenis = jenis;
        this.deskripsi = deskripsi;
        this.status = status;
        this.tanggal = tanggal;
        this.filePath = filePath;
    }

    public String getNamaMahasiswa() { return namaMahasiswa; }
    public String getJudul() { return judul; }
    public String getJenis() { return jenis; }
    public String getDeskripsi() { return deskripsi; }
    public String getStatus() { return status; }
    public String getTanggal() { return tanggal; }
    public String getFilePath() { return filePath; }
    public void setStatus(String status) { this.status = status; }
}
