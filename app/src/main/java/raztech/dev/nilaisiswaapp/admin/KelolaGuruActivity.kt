package raztech.dev.nilaisiswaapp.admin

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import raztech.dev.nilaisiswaapp.R
import raztech.dev.nilaisiswaapp.adapter.GuruAdapter
import raztech.dev.nilaisiswaapp.model.Guru
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.text.Editable
import android.text.TextWatcher

class KelolaGuruActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    private lateinit var edtIdGuru: EditText
    private lateinit var edtNamaGuru: EditText
    private lateinit var edtMataPelajaran: EditText
    private lateinit var btnSimpanGuru: Button
    private lateinit var btnBatalEditGuru: Button
    private lateinit var rvGuru: RecyclerView
    private lateinit var txtInfoGuru: TextView

    private lateinit var edtCariGuru: EditText
    private val semuaGuru = mutableListOf<Guru>()

    private lateinit var guruAdapter: GuruAdapter

    private lateinit var swipeRefreshGuru: SwipeRefreshLayout

    private val listGuru = mutableListOf<Guru>()

    private var isEditMode = false
    private var idGuruSedangDiedit = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kelola_guru)

        firestore = FirebaseFirestore.getInstance()

        edtIdGuru = findViewById(R.id.edtIdGuru)
        edtNamaGuru = findViewById(R.id.edtNamaGuru)
        edtMataPelajaran = findViewById(R.id.edtMataPelajaran)
        btnSimpanGuru = findViewById(R.id.btnSimpanGuru)
        btnBatalEditGuru = findViewById(R.id.btnBatalEditGuru)
        rvGuru = findViewById(R.id.rvGuru)
        rvGuru.isNestedScrollingEnabled = false
        txtInfoGuru = findViewById(R.id.txtInfoGuru)
        edtCariGuru = findViewById(R.id.edtCariGuru)

        swipeRefreshGuru = findViewById(R.id.swipeRefreshGuru)

        swipeRefreshGuru.setOnRefreshListener {
            loadDataGuru()
        }

        setupRecyclerView()
        loadDataGuru()

        btnSimpanGuru.setOnClickListener {
            if (isEditMode) {
                updateDataGuru()
            } else {
                simpanDataGuru()
            }
        }

        btnBatalEditGuru.setOnClickListener {
            keluarDariModeEdit()
        }
    }

    private fun setupRecyclerView() {
        guruAdapter = GuruAdapter(
            listGuru = listGuru,
            onEditClick = { guru ->
                masukModeEdit(guru)
            },
            onDeleteClick = { guru ->
                konfirmasiHapusGuru(guru)
            }
        )

        rvGuru.layoutManager = LinearLayoutManager(this)
        rvGuru.adapter = guruAdapter
        rvGuru.isNestedScrollingEnabled = false
    }

    private fun simpanDataGuru() {
        val idGuru = edtIdGuru.text.toString().trim()
        val namaGuru = edtNamaGuru.text.toString().trim()
        val mataPelajaran = edtMataPelajaran.text.toString().trim()

        if (idGuru.isEmpty() || namaGuru.isEmpty() || mataPelajaran.isEmpty()) {
            Toast.makeText(this, "Semua data wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val kodeDaftar = (100000..999999).random().toString()

        val guru = Guru(
            idGuru = idGuru,
            namaGuru = namaGuru,
            mataPelajaran = mataPelajaran,
            userId = "",

        )

        firestore.collection("guru")
            .document(idGuru)
            .set(guru)
            .addOnSuccessListener {
                Toast.makeText(this, "Data guru berhasil disimpan", Toast.LENGTH_SHORT).show()
                clearForm()
                loadDataGuru()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Gagal menyimpan data: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadDataGuru() {
        txtInfoGuru.text = "Memuat data guru..."

        firestore.collection("guru")
            .get()
            .addOnSuccessListener { result ->
                val dataGuru = mutableListOf<Guru>()

                for (document in result) {
                    val guru = document.toObject(Guru::class.java)
                    dataGuru.add(guru)
                }

                semuaGuru.clear()
                semuaGuru.addAll(dataGuru)

                filterDataGuru(edtCariGuru.text.toString())

                txtInfoGuru.text = if (dataGuru.isEmpty()) {
                    "Belum ada data guru"
                } else {
                    "${dataGuru.size} data guru ditemukan"
                }

                swipeRefreshGuru.isRefreshing = false
            }
            .addOnFailureListener { error ->
                txtInfoGuru.text = "Gagal memuat data guru"
                swipeRefreshGuru.isRefreshing = false

                Toast.makeText(
                    this,
                    "Error: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun masukModeEdit(guru: Guru) {
        isEditMode = true
        idGuruSedangDiedit = guru.idGuru

        edtIdGuru.setText(guru.idGuru)
        edtNamaGuru.setText(guru.namaGuru)
        edtMataPelajaran.setText(guru.mataPelajaran)

        edtIdGuru.isEnabled = false
        btnSimpanGuru.text = "Update Guru"
        btnBatalEditGuru.visibility = View.VISIBLE

        Toast.makeText(this, "Mode edit data ${guru.namaGuru}", Toast.LENGTH_SHORT).show()
    }

    private fun updateDataGuru() {
        val namaGuru = edtNamaGuru.text.toString().trim()
        val mataPelajaran = edtMataPelajaran.text.toString().trim()

        if (namaGuru.isEmpty() || mataPelajaran.isEmpty()) {
            Toast.makeText(this, "Nama guru dan mata pelajaran wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val updateData = mapOf(
            "namaGuru" to namaGuru,
            "mataPelajaran" to mataPelajaran
        )

        firestore.collection("guru")
            .document(idGuruSedangDiedit)
            .update(updateData)
            .addOnSuccessListener {
                Toast.makeText(this, "Data guru berhasil diperbarui", Toast.LENGTH_SHORT).show()
                keluarDariModeEdit()
                loadDataGuru()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Gagal update data: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun filterDataGuru(keyword: String) {
        val keywordLower = keyword.lowercase().trim()

        val hasilFilter = if (keywordLower.isEmpty()) {
            semuaGuru
        } else {
            semuaGuru.filter { guru ->
                guru.idGuru.lowercase().contains(keywordLower) ||
                        guru.namaGuru.lowercase().contains(keywordLower) ||
                        guru.mataPelajaran.lowercase().contains(keywordLower)
            }
        }

        guruAdapter.updateData(hasilFilter)

        txtInfoGuru.text = if (hasilFilter.isEmpty()) {
            "Data guru tidak ditemukan"
        } else {
            "${hasilFilter.size} data guru ditemukan"
        }
    }

    private fun keluarDariModeEdit() {
        isEditMode = false
        idGuruSedangDiedit = ""

        edtIdGuru.isEnabled = true
        btnSimpanGuru.text = "Simpan Guru"
        btnBatalEditGuru.visibility = View.GONE

        clearForm()
    }

    private fun konfirmasiHapusGuru(guru: Guru) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Data Guru")
            .setMessage("Apakah kamu yakin ingin menghapus data ${guru.namaGuru}?")
            .setPositiveButton("Hapus") { _, _ ->
                hapusDataGuru(guru)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun hapusDataGuru(guru: Guru) {
        firestore.collection("guru")
            .document(guru.idGuru)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Data guru berhasil dihapus", Toast.LENGTH_SHORT).show()

                if (isEditMode && idGuruSedangDiedit == guru.idGuru) {
                    keluarDariModeEdit()
                }

                loadDataGuru()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Gagal menghapus data: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun clearForm() {
        edtIdGuru.text.clear()
        edtNamaGuru.text.clear()
        edtMataPelajaran.text.clear()
    }
}