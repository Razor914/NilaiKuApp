package raztech.dev.nilaisiswaapp.utils

object NilaiUtils {

    fun validasiNilai(nilai: Int): Boolean {
        return nilai in 0..100
    }

    fun hitungNilaiAkhir(nilaiTugas: Int, nilaiUts: Int, nilaiUas: Int): Double {
        return (0.3 * nilaiTugas) + (0.3 * nilaiUts) + (0.4 * nilaiUas)
    }

    fun tentukanStatusKelulusan(nilaiAkhir: Double): String {
        return if (nilaiAkhir >= 70) {
            "Lulus"
        } else {
            "Tidak Lulus"
        }
    }
}