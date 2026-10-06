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
import raztech.dev.nilaisiswaapp.adapter.SiswaAdapter
import raztech.dev.nilaisiswaapp.model.Siswa
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.text.Editable
import android.text.TextWatcher

class KelolaSiswaActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    private lateinit var edtNis: EditText
    private lateinit var edtNamaSiswa: EditText
    private lateinit var edtKelas: EditText
    private lateinit var btnSimpanSiswa: Button
    private lateinit var btnBatalEdit: Button
    private lateinit var rvSiswa: RecyclerView
    private lateinit var txtInfoData: TextView
    private lateinit var swipeRefreshSiswa: SwipeRefreshLayout
    private lateinit var edtCariSiswa: EditText
    private val semuaSiswa = mutableListOf<Siswa>()

    private lateinit var siswaAdapter: SiswaAdapter
    private val listSiswa = mutableListOf<Siswa>()

    private var isEditMode = false
    private var nisSedangDiedit = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kelola_siswa)

        firestore = FirebaseFirestore.getInstance()

        swipeRefreshSiswa = findViewById(R.id.swipeRefreshSiswa)

        swipeRefreshSiswa.setOnRefreshListener {
            loadDataSiswa()
        }

        edtNis = findViewById(R.id.edtNis)
        edtNamaSiswa = findViewById(R.id.edtNamaSiswa)
        edtKelas = findViewById(R.id.edtKelas)
        btnSimpanSiswa = findViewById(R.id.btnSimpanSiswa)
        btnBatalEdit = findViewById(R.id.btnBatalEdit)
        rvSiswa = findViewById(R.id.rvSiswa)
        txtInfoData = findViewById(R.id.txtInfoData)

        setupRecyclerView()
        loadDataSiswa()

        btnSimpanSiswa.setOnClickListener {
            if (isEditMode) {
                updateDataSiswa()
            } else {
                simpanDataSiswa()
            }
        }

        btnBatalEdit.setOnClickListener {
            keluarDariModeEdit()
        }

        edtCariSiswa = findViewById(R.id.edtCariSiswa)

        edtCariSiswa.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterDataSiswa(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupRecyclerView() {
        siswaAdapter = SiswaAdapter(
            listSiswa = listSiswa,
            onEditClick = { siswa ->
                masukModeEdit(siswa)
            },
            onDeleteClick = { siswa ->
                konfirmasiHapusSiswa(siswa)
            }
        )

        rvSiswa.layoutManager = LinearLayoutManager(this)
        rvSiswa.adapter = siswaAdapter
        rvSiswa.isNestedScrollingEnabled = false
    }

    private fun simpanDataSiswa() {
        val nis = edtNis.text.toString().trim()
        val namaSiswa = edtNamaSiswa.text.toString().trim()
        val kelas = edtKelas.text.toString().trim()

        if (nis.isEmpty() || namaSiswa.isEmpty() || kelas.isEmpty()) {
            Toast.makeText(this, "Semua data wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("siswa")
            .document(nis)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Toast.makeText(
                        this,
                        "NIS sudah terdaftar. Gunakan NIS lain atau edit data yang sudah ada.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    val kodeDaftar = (100000..999999).random().toString()

                    val siswa = Siswa(
                        nis = nis,
                        namaSiswa = namaSiswa,
                        kelas = kelas,
                        userId = "",
                        kodeDaftar = kodeDaftar
                    )

                    firestore.collection("siswa")
                        .document(nis)
                        .set(siswa)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                "Data siswa berhasil disimpan. Kode daftar: $kodeDaftar",
                                Toast.LENGTH_LONG
                            ).show()

                            clearForm()
                            loadDataSiswa()
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(
                                this,
                                "Gagal menyimpan data: ${error.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    "Gagal mengecek NIS: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun loadDataSiswa() {
        txtInfoData.text = "Memuat data siswa..."

        firestore.collection("siswa")
            .get()
            .addOnSuccessListener { result ->
                val dataSiswa = mutableListOf<Siswa>()

                for (document in result) {
                    val siswa = document.toObject(Siswa::class.java)
                    dataSiswa.add(siswa)
                }

                semuaSiswa.clear()
                semuaSiswa.addAll(dataSiswa)

                filterDataSiswa(edtCariSiswa.text.toString())

                txtInfoData.text = if (dataSiswa.isEmpty()) {
                    "Belum ada data siswa"
                } else {
                    "${dataSiswa.size} data siswa ditemukan"
                }

                swipeRefreshSiswa.isRefreshing = false
            }
            .addOnFailureListener { error ->
                txtInfoData.text = "Gagal memuat data siswa"
                swipeRefreshSiswa.isRefreshing = false

                Toast.makeText(
                    this,
                    "Error: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun filterDataSiswa(keyword: String) {
        val keywordLower = keyword.lowercase().trim()

        val hasilFilter = if (keywordLower.isEmpty()) {
            semuaSiswa
        } else {
            semuaSiswa.filter { siswa ->
                siswa.nis.lowercase().contains(keywordLower) ||
                        siswa.namaSiswa.lowercase().contains(keywordLower) ||
                        siswa.kelas.lowercase().contains(keywordLower)
            }
        }

        siswaAdapter.updateData(hasilFilter)

        txtInfoData.text = if (hasilFilter.isEmpty()) {
            "Data siswa tidak ditemukan"
        } else {
            "${hasilFilter.size} data siswa ditemukan"
        }
    }

    private fun masukModeEdit(siswa: Siswa) {
        isEditMode = true
        nisSedangDiedit = siswa.nis

        edtNis.setText(siswa.nis)
        edtNamaSiswa.setText(siswa.namaSiswa)
        edtKelas.setText(siswa.kelas)

        edtNis.isEnabled = false
        btnSimpanSiswa.text = "Update Data Siswa"
        btnBatalEdit.visibility = View.VISIBLE

        Toast.makeText(this, "Mode edit data ${siswa.namaSiswa}", Toast.LENGTH_SHORT).show()
    }

    private fun updateDataSiswa() {
        val namaSiswa = edtNamaSiswa.text.toString().trim()
        val kelas = edtKelas.text.toString().trim()

        if (namaSiswa.isEmpty() || kelas.isEmpty()) {
            Toast.makeText(this, "Nama dan kelas wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val updateData = mapOf(
            "namaSiswa" to namaSiswa,
            "kelas" to kelas
        )

        firestore.collection("siswa")
            .document(nisSedangDiedit)
            .update(updateData)
            .addOnSuccessListener {
                Toast.makeText(this, "Data siswa berhasil diperbarui", Toast.LENGTH_SHORT).show()
                keluarDariModeEdit()
                loadDataSiswa()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Gagal update data: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun keluarDariModeEdit() {
        isEditMode = false
        nisSedangDiedit = ""

        edtNis.isEnabled = true
        btnSimpanSiswa.text = "Simpan Data Siswa"
        btnBatalEdit.visibility = View.GONE

        clearForm()
    }

    private fun konfirmasiHapusSiswa(siswa: Siswa) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Data Siswa")
            .setMessage("Apakah kamu yakin ingin menghapus data ${siswa.namaSiswa}?")
            .setPositiveButton("Hapus") { _, _ ->
                hapusDataSiswa(siswa)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun hapusDataSiswa(siswa: Siswa) {
        firestore.collection("siswa")
            .document(siswa.nis)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Data siswa berhasil dihapus", Toast.LENGTH_SHORT).show()

                if (isEditMode && nisSedangDiedit == siswa.nis) {
                    keluarDariModeEdit()
                }

                loadDataSiswa()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Gagal menghapus data: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun clearForm() {
        edtNis.text.clear()
        edtNamaSiswa.text.clear()
        edtKelas.text.clear()
    }
}