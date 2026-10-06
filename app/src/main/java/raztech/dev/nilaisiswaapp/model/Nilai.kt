package raztech.dev.nilaisiswaapp.model

data class Nilai(
    val idNilai: String = "",
    val nis: String = "",
    val namaSiswa: String = "",
    val kelas: String = "",
    val idGuru: String = "",
    val namaGuru: String = "",
    val mataPelajaran: String = "",
    val nilaiTugas: Int = 0,
    val nilaiUts: Int = 0,
    val nilaiUas: Int = 0,
    val nilaiAkhir: Double = 0.0,
    val statusKelulusan: String = ""
)