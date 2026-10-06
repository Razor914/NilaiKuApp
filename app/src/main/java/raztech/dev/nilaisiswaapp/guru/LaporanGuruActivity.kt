package raztech.dev.nilaisiswaapp.guru

import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import raztech.dev.nilaisiswaapp.R
import raztech.dev.nilaisiswaapp.adapter.NilaiGuruAdapter
import raztech.dev.nilaisiswaapp.model.Nilai
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.text.InputType
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import raztech.dev.nilaisiswaapp.utils.NilaiUtils

class LaporanGuruActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var txtInfoGuruLaporan: TextView
    private lateinit var txtRingkasanLaporanGuru: TextView
    private lateinit var txtInfoLaporanGuru: TextView
    private lateinit var btnCetakPdfGuru: Button
    private lateinit var rvLaporanGuru: RecyclerView

    private lateinit var nilaiAdapter: NilaiGuruAdapter

    private lateinit var edtCariNilaiGuru: EditText
    private val semuaNilaiGuru = mutableListOf<Nilai>()

    private val listNilai = mutableListOf<Nilai>()

    private var idGuruLogin = ""
    private var namaGuruLogin = ""
    private var mataPelajaranLogin = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laporan_guru)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        txtInfoGuruLaporan = findViewById(R.id.txtInfoGuruLaporan)
        txtRingkasanLaporanGuru = findViewById(R.id.txtRingkasanLaporanGuru)
        txtInfoLaporanGuru = findViewById(R.id.txtInfoLaporanGuru)
        btnCetakPdfGuru = findViewById(R.id.btnCetakPdfGuru)
        rvLaporanGuru = findViewById(R.id.rvLaporanGuru)

        setupRecyclerView()
        loadDataGuruLogin()

        btnCetakPdfGuru.setOnClickListener {
            if (listNilai.isEmpty()) {
                Toast.makeText(this, "Belum ada data nilai untuk dicetak", Toast.LENGTH_SHORT).show()
            } else {
                buatPdfLaporanGuru()
            }
        }

        edtCariNilaiGuru = findViewById(R.id.edtCariNilaiGuru)

        edtCariNilaiGuru.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterDataNilaiGuru(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

    }

    private fun setupRecyclerView() {
        nilaiAdapter = NilaiGuruAdapter(
            listNilai = listNilai,
            onEditClick = { nilai ->
                tampilkanDialogEditNilai(nilai)
            },
            onDeleteClick = { nilai ->
                konfirmasiHapusNilai(nilai)
            }
        )

        rvLaporanGuru.layoutManager = LinearLayoutManager(this)
        rvLaporanGuru.adapter = nilaiAdapter
    }

    private fun hapusNilaiFirestore(nilai: Nilai) {
        val idDokumen = if (nilai.idNilai.isNotEmpty()) {
            nilai.idNilai
        } else {
            "${nilai.nis}_$idGuruLogin"
        }

        firestore.collection("nilai")
            .document(idDokumen)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Nilai berhasil dihapus", Toast.LENGTH_SHORT).show()
                loadLaporanNilaiGuru()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Gagal hapus nilai: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun konfirmasiHapusNilai(nilai: Nilai) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Nilai")
            .setMessage("Apakah kamu yakin ingin menghapus nilai ${nilai.namaSiswa}?")
            .setPositiveButton("Hapus") { _, _ ->
                hapusNilaiFirestore(nilai)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun tampilkanDialogEditNilai(nilai: Nilai) {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 20, 40, 10)

        val edtTugas = EditText(this)
        edtTugas.hint = "Nilai Tugas"
        edtTugas.inputType = InputType.TYPE_CLASS_NUMBER
        edtTugas.setText(nilai.nilaiTugas.toString())

        val edtUts = EditText(this)
        edtUts.hint = "Nilai UTS"
        edtUts.inputType = InputType.TYPE_CLASS_NUMBER
        edtUts.setText(nilai.nilaiUts.toString())

        val edtUas = EditText(this)
        edtUas.hint = "Nilai UAS"
        edtUas.inputType = InputType.TYPE_CLASS_NUMBER
        edtUas.setText(nilai.nilaiUas.toString())

        layout.addView(edtTugas)
        layout.addView(edtUts)
        layout.addView(edtUas)

        AlertDialog.Builder(this)
            .setTitle("Edit Nilai ${nilai.namaSiswa}")
            .setMessage("Ubah nilai tugas, UTS, dan UAS.")
            .setView(layout)
            .setPositiveButton("Update") { _, _ ->
                val tugasText = edtTugas.text.toString().trim()
                val utsText = edtUts.text.toString().trim()
                val uasText = edtUas.text.toString().trim()

                if (tugasText.isEmpty() || utsText.isEmpty() || uasText.isEmpty()) {
                    Toast.makeText(this, "Semua nilai wajib diisi", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val tugas = tugasText.toInt()
                val uts = utsText.toInt()
                val uas = uasText.toInt()

                if (!NilaiUtils.validasiNilai(tugas) ||
                    !NilaiUtils.validasiNilai(uts) ||
                    !NilaiUtils.validasiNilai(uas)
                ) {
                    Toast.makeText(this, "Nilai harus berada pada rentang 0 sampai 100", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                updateNilaiFirestore(nilai, tugas, uts, uas)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateNilaiFirestore(
        nilai: Nilai,
        tugas: Int,
        uts: Int,
        uas: Int
    ) {
        val nilaiAkhir = NilaiUtils.hitungNilaiAkhir(tugas, uts, uas)
        val status = NilaiUtils.tentukanStatusKelulusan(nilaiAkhir)

        val idDokumen = if (nilai.idNilai.isNotEmpty()) {
            nilai.idNilai
        } else {
            "${nilai.nis}_$idGuruLogin"
        }

        val updateData = mapOf(
            "nilaiTugas" to tugas,
            "nilaiUts" to uts,
            "nilaiUas" to uas,
            "nilaiAkhir" to nilaiAkhir,
            "statusKelulusan" to status
        )

        firestore.collection("nilai")
            .document(idDokumen)
            .update(updateData)
            .addOnSuccessListener {
                Toast.makeText(this, "Nilai berhasil diperbarui", Toast.LENGTH_SHORT).show()
                loadLaporanNilaiGuru()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Gagal update nilai: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadDataGuruLogin() {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            Toast.makeText(this, "User belum login", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { userDoc ->
                idGuruLogin = userDoc.getString("idGuru") ?: ""

                if (idGuruLogin.isEmpty()) {
                    txtInfoGuruLaporan.text = "ID Guru tidak ditemukan"
                    return@addOnSuccessListener
                }

                firestore.collection("guru")
                    .document(idGuruLogin)
                    .get()
                    .addOnSuccessListener { guruDoc ->
                        namaGuruLogin = guruDoc.getString("namaGuru") ?: "Guru"
                        mataPelajaranLogin = guruDoc.getString("mataPelajaran") ?: "-"

                        txtInfoGuruLaporan.text = "$namaGuruLogin • $mataPelajaranLogin"

                        loadLaporanNilaiGuru()
                    }
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Gagal membaca data guru: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadLaporanNilaiGuru() {
        txtRingkasanLaporanGuru.text = "Memuat laporan..."
        txtInfoLaporanGuru.text = "Mohon tunggu sebentar"

        firestore.collection("nilai")
            .whereEqualTo("idGuru", idGuruLogin)
            .get()
            .addOnSuccessListener { result ->
                val dataNilai = mutableListOf<Nilai>()

                for (document in result) {
                    val nilai = document.toObject(Nilai::class.java)
                    dataNilai.add(nilai)
                }

                semuaNilaiGuru.clear()
                semuaNilaiGuru.addAll(dataNilai)

                filterDataNilaiGuru(edtCariNilaiGuru.text.toString())

                val totalData = dataNilai.size
                val totalLulus = dataNilai.count { it.statusKelulusan == "Lulus" }
                val totalTidakLulus = dataNilai.count { it.statusKelulusan == "Tidak Lulus" }

                txtRingkasanLaporanGuru.text = "$totalData data nilai ditemukan"
                txtInfoLaporanGuru.text = "Lulus: $totalLulus • Tidak Lulus: $totalTidakLulus"

                if (dataNilai.isEmpty()) {
                    txtRingkasanLaporanGuru.text = "Belum ada data nilai"
                    txtInfoLaporanGuru.text = "Data akan muncul setelah guru menginput nilai"
                }
            }
            .addOnFailureListener { error ->
                txtRingkasanLaporanGuru.text = "Gagal memuat laporan"
                txtInfoLaporanGuru.text = "Terjadi kesalahan saat mengambil data"
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun filterDataNilaiGuru(keyword: String) {
        val keywordLower = keyword.lowercase().trim()

        val hasilFilter = if (keywordLower.isEmpty()) {
            semuaNilaiGuru
        } else {
            semuaNilaiGuru.filter { nilai ->
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
                        cocokStatus
            }
        }

        nilaiAdapter.updateData(hasilFilter)

        val totalData = hasilFilter.size
        val totalLulus = hasilFilter.count { it.statusKelulusan == "Lulus" }
        val totalTidakLulus = hasilFilter.count { it.statusKelulusan == "Tidak Lulus" }

        txtRingkasanLaporanGuru.text = if (hasilFilter.isEmpty()) {
            "Data nilai tidak ditemukan"
        } else {
            "$totalData data nilai ditemukan"
        }

        txtInfoLaporanGuru.text = "Lulus: $totalLulus • Tidak Lulus: $totalTidakLulus"
    }

    private fun buatPdfLaporanGuru() {
        val pdfDocument = PdfDocument()

        val pageWidth = 595
        val pageHeight = 842
        var pageNumber = 1

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val paint = Paint()
        val titlePaint = Paint()
        val headerPaint = Paint()

        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 18f

        headerPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        headerPaint.textSize = 12f

        paint.textSize = 10f

        var y = 50

        fun tulisHeaderHalaman() {
            canvas.drawText("Laporan Nilai Siswa", 40f, y.toFloat(), titlePaint)
            y += 25

            canvas.drawText("Nama Guru: $namaGuruLogin", 40f, y.toFloat(), paint)
            y += 16

            canvas.drawText("Mata Pelajaran: $mataPelajaranLogin", 40f, y.toFloat(), paint)
            y += 16

            val tanggal = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")).format(Date())
            canvas.drawText("Tanggal Cetak: $tanggal", 40f, y.toFloat(), paint)
            y += 28

            canvas.drawText("No", 40f, y.toFloat(), headerPaint)
            canvas.drawText("Nama Siswa", 70f, y.toFloat(), headerPaint)
            canvas.drawText("Kelas", 210f, y.toFloat(), headerPaint)
            canvas.drawText("Tugas", 270f, y.toFloat(), headerPaint)
            canvas.drawText("UTS", 320f, y.toFloat(), headerPaint)
            canvas.drawText("UAS", 365f, y.toFloat(), headerPaint)
            canvas.drawText("Akhir", 410f, y.toFloat(), headerPaint)
            canvas.drawText("Status", 465f, y.toFloat(), headerPaint)

            y += 12
            canvas.drawLine(40f, y.toFloat(), 555f, y.toFloat(), paint)
            y += 18
        }

        fun halamanBaru() {
            pdfDocument.finishPage(page)

            pageNumber++
            y = 50

            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas

            tulisHeaderHalaman()
        }

        tulisHeaderHalaman()

        listNilai.forEachIndexed { index, nilai ->
            if (y > 790) {
                halamanBaru()
            }

            val namaPendek = if (nilai.namaSiswa.length > 22) {
                nilai.namaSiswa.take(22) + "..."
            } else {
                nilai.namaSiswa
            }

            canvas.drawText((index + 1).toString(), 40f, y.toFloat(), paint)
            canvas.drawText(namaPendek, 70f, y.toFloat(), paint)
            canvas.drawText(nilai.kelas, 210f, y.toFloat(), paint)
            canvas.drawText(nilai.nilaiTugas.toString(), 270f, y.toFloat(), paint)
            canvas.drawText(nilai.nilaiUts.toString(), 320f, y.toFloat(), paint)
            canvas.drawText(nilai.nilaiUas.toString(), 365f, y.toFloat(), paint)
            canvas.drawText(String.format("%.1f", nilai.nilaiAkhir), 410f, y.toFloat(), paint)
            canvas.drawText(nilai.statusKelulusan, 465f, y.toFloat(), paint)

            y += 20
        }

        y += 25
        if (y > 790) {
            halamanBaru()
        }

        val totalLulus = listNilai.count { it.statusKelulusan == "Lulus" }
        val totalTidakLulus = listNilai.count { it.statusKelulusan == "Tidak Lulus" }

        canvas.drawLine(40f, y.toFloat(), 555f, y.toFloat(), paint)
        y += 20
        canvas.drawText("Total Data: ${listNilai.size}", 40f, y.toFloat(), headerPaint)
        y += 18
        canvas.drawText("Lulus: $totalLulus", 40f, y.toFloat(), headerPaint)
        y += 18
        canvas.drawText("Tidak Lulus: $totalTidakLulus", 40f, y.toFloat(), headerPaint)

        pdfDocument.finishPage(page)

        try {
            val folder = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)

            if (folder != null && !folder.exists()) {
                folder.mkdirs()
            }

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "Laporan_Nilai_${idGuruLogin}_$timeStamp.pdf"
            val file = File(folder, fileName)

            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            Toast.makeText(this, "PDF berhasil dibuat: $fileName", Toast.LENGTH_LONG).show()
            bukaPdf(file)

        } catch (e: Exception) {
            pdfDocument.close()
            Toast.makeText(this, "Gagal membuat PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun bukaPdf(file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "PDF dibuat, tetapi tidak ada aplikasi pembuka PDF", Toast.LENGTH_LONG).show()
        }
    }
}