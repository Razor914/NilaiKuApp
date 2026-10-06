package raztech.dev.nilaisiswaapp.admin

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import raztech.dev.nilaisiswaapp.R
import raztech.dev.nilaisiswaapp.adapter.NilaiAdapter
import raztech.dev.nilaisiswaapp.model.Nilai
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class LaporanNilaiActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    private lateinit var rvLaporanNilai: RecyclerView
    private lateinit var txtRingkasanLaporan: TextView
    private lateinit var txtInfoLaporan: TextView

    private lateinit var edtCariNilaiAdmin: EditText
    private val semuaNilai = mutableListOf<Nilai>()

    private lateinit var swipeRefreshLaporanNilai: SwipeRefreshLayout

    private lateinit var nilaiAdapter: NilaiAdapter
    private val listNilai = mutableListOf<Nilai>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laporan_nilai)

        edtCariNilaiAdmin = findViewById(R.id.edtCariNilaiAdmin)

        edtCariNilaiAdmin.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterDataNilaiAdmin(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        firestore = FirebaseFirestore.getInstance()

        rvLaporanNilai = findViewById(R.id.rvLaporanNilai)
        txtRingkasanLaporan = findViewById(R.id.txtRingkasanLaporan)
        txtInfoLaporan = findViewById(R.id.txtInfoLaporan)

        swipeRefreshLaporanNilai = findViewById(R.id.swipeRefreshLaporanNilai)

        swipeRefreshLaporanNilai.setOnRefreshListener {
            loadDataNilai()
        }

        setupRecyclerView()
        loadDataNilai()
    }

    private fun setupRecyclerView() {
        nilaiAdapter = NilaiAdapter(listNilai)

        rvLaporanNilai.layoutManager = LinearLayoutManager(this)
        rvLaporanNilai.adapter = nilaiAdapter
    }

    private fun loadDataNilai() {
        txtRingkasanLaporan.text = "Memuat laporan nilai..."
        txtInfoLaporan.text = "Mohon tunggu sebentar"
        firestore.collection("nilai")
            .get()
            .addOnSuccessListener { result ->
                val dataNilai = mutableListOf<Nilai>()

                for (document in result) {
                    val nilai = document.toObject(Nilai::class.java)
                    dataNilai.add(nilai)
                }

                semuaNilai.clear()
                semuaNilai.addAll(dataNilai)

                filterDataNilaiAdmin(edtCariNilaiAdmin.text.toString())

                val totalData = dataNilai.size
                val totalLulus = dataNilai.count { it.statusKelulusan == "Lulus" }
                val totalTidakLulus = dataNilai.count { it.statusKelulusan == "Tidak Lulus" }

                txtRingkasanLaporan.text = "$totalData data nilai ditemukan"
                txtInfoLaporan.text = "Lulus: $totalLulus • Tidak Lulus: $totalTidakLulus"

                if (dataNilai.isEmpty()) {
                    txtRingkasanLaporan.text = "Belum ada data nilai"
                    txtInfoLaporan.text = "Data akan muncul setelah guru menginput nilai"
                }
                swipeRefreshLaporanNilai.isRefreshing = false
            }
            .addOnFailureListener { error ->
                txtRingkasanLaporan.text = "Gagal memuat laporan"
                txtInfoLaporan.text = "Terjadi kesalahan saat mengambil data"
                swipeRefreshLaporanNilai.isRefreshing = false
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun filterDataNilaiAdmin(keyword: String) {
        val keywordLower = keyword.lowercase().trim()

        val hasilFilter = if (keywordLower.isEmpty()) {
            semuaNilai
        } else {
            semuaNilai.filter { nilai ->
                val statusLower = nilai.statusKelulusan.lowercase().trim()

                val cocokStatus = when (keywordLower) {
                    "lulus" -> statusLower == "lulus"
                    "tidak lulus" -> statusLower == "tidak lulus"
                    "tidak" -> statusLower == "tidak lulus"
                    else -> false
                }

                nilai.nis.lowercase().contains(keywordLower) ||
                        nilai.namaSiswa.lowercase().contains(keywordLower) ||
                        nilai.kelas.lowercase().contains(keywordLower) ||
                        nilai.namaGuru.lowercase().contains(keywordLower) ||
                        nilai.mataPelajaran.lowercase().contains(keywordLower) ||
                        cocokStatus
            }
        }

        nilaiAdapter.updateData(hasilFilter)

        val totalData = hasilFilter.size
        val totalLulus = hasilFilter.count { it.statusKelulusan == "Lulus" }
        val totalTidakLulus = hasilFilter.count { it.statusKelulusan == "Tidak Lulus" }

        txtRingkasanLaporan.text = if (hasilFilter.isEmpty()) {
            "Data nilai tidak ditemukan"
        } else {
            "$totalData data nilai ditemukan"
        }

        txtInfoLaporan.text = "Lulus: $totalLulus • Tidak Lulus: $totalTidakLulus"
    }

}