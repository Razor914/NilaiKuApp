package raztech.dev.nilaisiswaapp.siswa

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import raztech.dev.nilaisiswaapp.R
import raztech.dev.nilaisiswaapp.adapter.NilaiAdapter
import raztech.dev.nilaisiswaapp.model.Nilai
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class NilaiPribadiActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var txtHeaderNilaiPribadi: TextView
    private lateinit var txtRingkasanNilaiPribadi: TextView
    private lateinit var txtInfoNilaiPribadi: TextView
    private lateinit var rvNilaiPribadi: RecyclerView
    private lateinit var swipeRefreshNilaiPribadi: SwipeRefreshLayout

    private lateinit var nilaiAdapter: NilaiAdapter
    private val listNilai = mutableListOf<Nilai>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nilai_pribadi)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        txtHeaderNilaiPribadi = findViewById(R.id.txtHeaderNilaiPribadi)
        txtRingkasanNilaiPribadi = findViewById(R.id.txtRingkasanNilaiPribadi)
        txtInfoNilaiPribadi = findViewById(R.id.txtInfoNilaiPribadi)
        rvNilaiPribadi = findViewById(R.id.rvNilaiPribadi)

        swipeRefreshNilaiPribadi = findViewById(R.id.swipeRefreshNilaiPribadi)

        swipeRefreshNilaiPribadi.setOnRefreshListener {
            loadNisSiswaLogin()
        }

        setupRecyclerView()
        loadNisSiswaLogin()
    }

    private fun setupRecyclerView() {
        nilaiAdapter = NilaiAdapter(listNilai)

        rvNilaiPribadi.layoutManager = LinearLayoutManager(this)
        rvNilaiPribadi.adapter = nilaiAdapter
    }

    private fun loadNisSiswaLogin() {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val nama = document.getString("nama") ?: "Siswa"
                val nis = document.getString("nis") ?: ""

                txtHeaderNilaiPribadi.text = "Nilai milik $nama"

                if (nis.isEmpty()) {
                    txtRingkasanNilaiPribadi.text = "NIS tidak ditemukan"
                    txtInfoNilaiPribadi.text = "Akun siswa belum terhubung dengan data NIS"
                    return@addOnSuccessListener
                }

                loadNilaiByNis(nis)
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Gagal membaca data user: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadNilaiByNis(nis: String) {
        txtRingkasanNilaiPribadi.text = "Memuat nilai..."
        txtInfoNilaiPribadi.text = "Mohon tunggu sebentar"

        firestore.collection("nilai")
            .whereEqualTo("nis", nis)
            .get()
            .addOnSuccessListener { result ->
                val dataNilai = mutableListOf<Nilai>()

                for (document in result) {
                    val nilai = document.toObject(Nilai::class.java)
                    dataNilai.add(nilai)
                }

                nilaiAdapter.updateData(dataNilai)

                if (dataNilai.isEmpty()) {
                    txtRingkasanNilaiPribadi.text = "Belum ada nilai"
                    txtInfoNilaiPribadi.text = "Nilai akan muncul setelah guru menginput data"
                } else {
                    val totalLulus = dataNilai.count { it.statusKelulusan == "Lulus" }
                    val totalTidakLulus = dataNilai.count { it.statusKelulusan == "Tidak Lulus" }

                    txtRingkasanNilaiPribadi.text = "${dataNilai.size} data nilai ditemukan"
                    txtInfoNilaiPribadi.text = "Lulus: $totalLulus • Tidak Lulus: $totalTidakLulus"
                }
                swipeRefreshNilaiPribadi.isRefreshing = false
            }
            .addOnFailureListener { error ->
                txtRingkasanNilaiPribadi.text = "Gagal memuat nilai"
                txtInfoNilaiPribadi.text = "Terjadi kesalahan saat mengambil data"
                swipeRefreshNilaiPribadi.isRefreshing = false
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }
}