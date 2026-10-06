package raztech.dev.nilaisiswaapp.admin

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import raztech.dev.nilaisiswaapp.R

class BuatAkunGuruActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore

    private lateinit var edtIdGuruAkun: EditText
    private lateinit var btnCekGuru: Button
    private lateinit var txtDataGuruAkun: TextView
    private lateinit var edtEmailGuruAkun: EditText
    private lateinit var edtPasswordGuruAkun: EditText
    private lateinit var edtKonfirmasiPasswordGuruAkun: EditText
    private lateinit var btnBuatAkunGuru: Button

    private var idGuruValid = ""
    private var namaGuruValid = ""
    private var mataPelajaranValid = ""
    private var userIdGuru = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buat_akun_guru)

        firestore = FirebaseFirestore.getInstance()

        edtIdGuruAkun = findViewById(R.id.edtIdGuruAkun)
        btnCekGuru = findViewById(R.id.btnCekGuru)
        txtDataGuruAkun = findViewById(R.id.txtDataGuruAkun)
        edtEmailGuruAkun = findViewById(R.id.edtEmailGuruAkun)
        edtPasswordGuruAkun = findViewById(R.id.edtPasswordGuruAkun)
        edtKonfirmasiPasswordGuruAkun = findViewById(R.id.edtKonfirmasiPasswordGuruAkun)
        btnBuatAkunGuru = findViewById(R.id.btnBuatAkunGuru)

        // Tombol buat akun dimatikan dulu sampai data guru berhasil dicek
        btnBuatAkunGuru.isEnabled = false
        btnBuatAkunGuru.alpha = 0.5f

        btnCekGuru.setOnClickListener {
            cekDataGuru()
        }

        btnBuatAkunGuru.setOnClickListener {
            prosesBuatAkunGuru()
        }
    }

    private fun cekDataGuru() {
        val idGuruInput = edtIdGuruAkun.text.toString().trim()

        if (idGuruInput.isEmpty()) {
            Toast.makeText(this, "Masukkan ID Guru terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("guru")
            .document(idGuruInput)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    resetDataGuru()
                    txtDataGuruAkun.text = "Data guru tidak ditemukan"
                    Toast.makeText(this, "ID Guru belum terdaftar", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                idGuruValid = document.getString("idGuru") ?: idGuruInput
                namaGuruValid = document.getString("namaGuru") ?: ""
                mataPelajaranValid = document.getString("mataPelajaran") ?: ""
                userIdGuru = document.getString("userId") ?: ""

                if (namaGuruValid.isEmpty() || mataPelajaranValid.isEmpty()) {
                    resetDataGuru()
                    txtDataGuruAkun.text = "Data guru tidak lengkap. Cek kembali data guru."
                    Toast.makeText(this, "Data guru tidak lengkap", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                txtDataGuruAkun.text = """
                    Nama Guru      : $namaGuruValid
                    ID Guru        : $idGuruValid
                    Mata Pelajaran : $mataPelajaranValid
                    Status Akun    : ${if (userIdGuru.isEmpty()) "Belum punya akun" else "Sudah punya akun"}
                """.trimIndent()

                if (userIdGuru.isNotEmpty()) {
                    btnBuatAkunGuru.isEnabled = false
                    btnBuatAkunGuru.alpha = 0.5f
                    Toast.makeText(this, "Guru ini sudah memiliki akun", Toast.LENGTH_LONG).show()
                } else {
                    btnBuatAkunGuru.isEnabled = true
                    btnBuatAkunGuru.alpha = 1.0f
                    Toast.makeText(this, "Data guru berhasil dicek", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { error ->
                resetDataGuru()
                Toast.makeText(this, "Gagal cek data guru: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun prosesBuatAkunGuru() {
        if (idGuruValid.isEmpty()) {
            Toast.makeText(this, "Cek data guru terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        if (userIdGuru.isNotEmpty()) {
            Toast.makeText(this, "Guru ini sudah memiliki akun", Toast.LENGTH_LONG).show()
            return
        }

        val email = edtEmailGuruAkun.text.toString().trim()
        val password = edtPasswordGuruAkun.text.toString().trim()
        val konfirmasiPassword = edtKonfirmasiPasswordGuruAkun.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || konfirmasiPassword.isEmpty()) {
            Toast.makeText(this, "Email dan password wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != konfirmasiPassword) {
            Toast.makeText(this, "Konfirmasi password tidak sama", Toast.LENGTH_SHORT).show()
            return
        }

        buatAkunGuruDenganSecondaryAuth(email, password)
    }

    private fun buatAkunGuruDenganSecondaryAuth(
        email: String,
        password: String
    ) {
        val secondaryAppName = "SecondaryAuthApp"

        val secondaryApp = try {
            FirebaseApp.getInstance(secondaryAppName)
        } catch (e: IllegalStateException) {
            FirebaseApp.initializeApp(
                this,
                FirebaseApp.getInstance().options,
                secondaryAppName
            )
        }

        if (secondaryApp == null) {
            Toast.makeText(this, "Gagal membuat secondary Firebase App", Toast.LENGTH_LONG).show()
            return
        }

        val secondaryAuth = FirebaseAuth.getInstance(secondaryApp)

        secondaryAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uidGuru = result.user?.uid

                if (uidGuru == null) {
                    Toast.makeText(this, "UID guru tidak ditemukan", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                simpanRoleGuruKeFirestore(
                    uidGuru = uidGuru,
                    email = email,
                    secondaryAuth = secondaryAuth
                )
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Gagal membuat akun guru: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun simpanRoleGuruKeFirestore(
        uidGuru: String,
        email: String,
        secondaryAuth: FirebaseAuth
    ) {
        val userData = hashMapOf(
            "nama" to namaGuruValid,
            "email" to email,
            "role" to "guru",
            "nis" to "",
            "idGuru" to idGuruValid
        )

        firestore.collection("users")
            .document(uidGuru)
            .set(userData)
            .addOnSuccessListener {
                updateUserIdGuru(
                    uidGuru = uidGuru,
                    secondaryAuth = secondaryAuth
                )
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    "Akun dibuat, tetapi gagal menyimpan role: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun updateUserIdGuru(
        uidGuru: String,
        secondaryAuth: FirebaseAuth
    ) {
        firestore.collection("guru")
            .document(idGuruValid)
            .update("userId", uidGuru)
            .addOnSuccessListener {
                secondaryAuth.signOut()

                Toast.makeText(
                    this,
                    "Akun guru berhasil dibuat",
                    Toast.LENGTH_LONG
                ).show()

                clearForm()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    "Akun dibuat, tetapi gagal update data guru: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun resetDataGuru() {
        idGuruValid = ""
        namaGuruValid = ""
        mataPelajaranValid = ""
        userIdGuru = ""

        btnBuatAkunGuru.isEnabled = false
        btnBuatAkunGuru.alpha = 0.5f
    }

    private fun clearForm() {
        edtIdGuruAkun.text.clear()
        edtEmailGuruAkun.text.clear()
        edtPasswordGuruAkun.text.clear()
        edtKonfirmasiPasswordGuruAkun.text.clear()

        txtDataGuruAkun.text = "Data guru belum dicek"

        resetDataGuru()
    }
}