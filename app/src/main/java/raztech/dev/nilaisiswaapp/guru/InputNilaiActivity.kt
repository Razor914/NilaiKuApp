package raztech.dev.nilaisiswaapp.guru

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import raztech.dev.nilaisiswaapp.R
import raztech.dev.nilaisiswaapp.model.Nilai
import raztech.dev.nilaisiswaapp.model.Siswa
import raztech.dev.nilaisiswaapp.utils.NilaiUtils

class InputNilaiActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var edtNisInput: EditText
    private lateinit var btnCariSiswa: Button
    private lateinit var txtDataSiswa: TextView

    private lateinit var edtNilaiTugas: EditText
    private lateinit var edtNilaiUts: EditText
    private lateinit var edtNilaiUas: EditText
    private lateinit var txtHasilNilai: TextView
    private lateinit var btnSimpanNilai: Button

    private var siswaDipilih: Siswa? = null
    private var idGuruLogin = ""
    private var namaGuruLogin = ""
    private var mataPelajaranLogin = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_nilai)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        edtNisInput = findViewById(R.id.edtNisInput)
        btnCariSiswa = findViewById(R.id.btnCariSiswa)
        txtDataSiswa = findViewById(R.id.txtDataSiswa)

        edtNilaiTugas = findViewById(R.id.edtNilaiTugas)
        edtNilaiUts = findViewById(R.id.edtNilaiUts)
        edtNilaiUas = findViewById(R.id.edtNilaiUas)
        txtHasilNilai = findViewById(R.id.txtHasilNilai)
        btnSimpanNilai = findViewById(R.id.btnSimpanNilai)

        loadDataGuruLogin()

        btnCariSiswa.setOnClickListener {
            cariSiswa()
        }

        btnSimpanNilai.setOnClickListener {
            simpanNilai()
        }
    }

    private fun loadDataGuruLogin() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { userDoc ->
                idGuruLogin = userDoc.getString("idGuru") ?: ""

                if (idGuruLogin.isNotEmpty()) {
                    firestore.collection("guru")
                        .document(idGuruLogin)
                        .get()
                        .addOnSuccessListener { guruDoc ->
                            namaGuruLogin = guruDoc.getString("namaGuru") ?: ""
                            mataPelajaranLogin = guruDoc.getString("mataPelajaran") ?: ""
                        }
                }
            }
    }

    private fun cariSiswa() {
        val nis = edtNisInput.text.toString().trim()

        if (nis.isEmpty()) {
            Toast.makeText(this, "Masukkan NIS siswa terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("siswa")
            .document(nis)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val siswa = document.toObject(Siswa::class.java)
                    siswaDipilih = siswa

                    txtDataSiswa.text = """
                        Nama  : ${siswa?.namaSiswa}
                        NIS   : ${siswa?.nis}
                        Kelas : ${siswa?.kelas}
                    """.trimIndent()

                    Toast.makeText(this, "Data siswa ditemukan", Toast.LENGTH_SHORT).show()
                } else {
                    siswaDipilih = null
                    txtDataSiswa.text = "Data siswa tidak ditemukan"
                    Toast.makeText(this, "Siswa dengan NIS tersebut tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Gagal mencari siswa: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun simpanNilai() {
        val siswa = siswaDipilih

        if (siswa == null) {
            Toast.makeText(this, "Cari dan pilih siswa terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val tugasText = edtNilaiTugas.text.toString().trim()
        val utsText = edtNilaiUts.text.toString().trim()
        val uasText = edtNilaiUas.text.toString().trim()

        if (tugasText.isEmpty() || utsText.isEmpty() || uasText.isEmpty()) {
            Toast.makeText(this, "Nilai tugas, UTS, dan UAS wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val tugas = tugasText.toInt()
        val uts = utsText.toInt()
        val uas = uasText.toInt()

        if (!NilaiUtils.validasiNilai(tugas) ||
            !NilaiUtils.validasiNilai(uts) ||
            !NilaiUtils.validasiNilai(uas)
        ) {
            Toast.makeText(this, "Nilai harus berada pada rentang 0 sampai 100", Toast.LENGTH_SHORT).show()
            return
        }

        val nilaiAkhir = NilaiUtils.hitungNilaiAkhir(tugas, uts, uas)
        val status = NilaiUtils.tentukanStatusKelulusan(nilaiAkhir)

        val idNilai = "${siswa.nis}_$idGuruLogin"

        val dataNilai = Nilai(
            idNilai = idNilai,
            nis = siswa.nis,
            namaSiswa = siswa.namaSiswa,
            kelas = siswa.kelas,
            idGuru = idGuruLogin,
            namaGuru = namaGuruLogin,
            mataPelajaran = mataPelajaranLogin,
            nilaiTugas = tugas,
            nilaiUts = uts,
            nilaiUas = uas,
            nilaiAkhir = nilaiAkhir,
            statusKelulusan = status
        )

        firestore.collection("nilai")
            .document(idNilai)
            .set(dataNilai)
            .addOnSuccessListener {
                txtHasilNilai.text = "Nilai Akhir: $nilaiAkhir\nStatus: $status"
                Toast.makeText(this, "Nilai berhasil disimpan", Toast.LENGTH_SHORT).show()
                clearNilaiForm()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Gagal menyimpan nilai: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun clearNilaiForm() {
        edtNilaiTugas.text.clear()
        edtNilaiUts.text.clear()
        edtNilaiUas.text.clear()
    }
}