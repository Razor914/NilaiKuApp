package raztech.dev.nilaisiswaapp.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import raztech.dev.nilaisiswaapp.R

class RegisterSiswaActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var edtNisRegister: EditText
    private lateinit var edtKodeDaftar: EditText
    private lateinit var edtEmailRegister: EditText
    private lateinit var edtPasswordRegister: EditText
    private lateinit var edtKonfirmasiPassword: EditText
    private lateinit var btnRegisterSiswa: Button
    private lateinit var txtKembaliLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_siswa)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        edtNisRegister = findViewById(R.id.edtNisRegister)
        edtKodeDaftar = findViewById(R.id.edtKodeDaftar)
        edtEmailRegister = findViewById(R.id.edtEmailRegister)
        edtPasswordRegister = findViewById(R.id.edtPasswordRegister)
        edtKonfirmasiPassword = findViewById(R.id.edtKonfirmasiPassword)
        btnRegisterSiswa = findViewById(R.id.btnRegisterSiswa)
        txtKembaliLogin = findViewById(R.id.txtKembaliLogin)

        btnRegisterSiswa.setOnClickListener {
            prosesDaftarSiswa()
        }

        txtKembaliLogin.setOnClickListener {
            finish()
        }
    }

    private fun prosesDaftarSiswa() {
        val nis = edtNisRegister.text.toString().trim()
        val kodeDaftar = edtKodeDaftar.text.toString().trim()
        val email = edtEmailRegister.text.toString().trim()
        val password = edtPasswordRegister.text.toString().trim()
        val konfirmasiPassword = edtKonfirmasiPassword.text.toString().trim()

        if (nis.isEmpty() || kodeDaftar.isEmpty() || email.isEmpty() ||
            password.isEmpty() || konfirmasiPassword.isEmpty()
        ) {
            Toast.makeText(this, "Semua data wajib diisi", Toast.LENGTH_SHORT).show()
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

        cekNisDanKodeDaftar(nis, kodeDaftar, email, password)
    }

    private fun cekNisDanKodeDaftar(
        nis: String,
        kodeInput: String,
        email: String,
        password: String
    ) {
        firestore.collection("siswa")
            .document(nis)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    Toast.makeText(
                        this,
                        "NIS belum terdaftar. Hubungi admin.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }

                val kodeFirestore = document.getString("kodeDaftar") ?: ""
                val userId = document.getString("userId") ?: ""
                val namaSiswa = document.getString("namaSiswa") ?: ""

                if (userId.isNotEmpty()) {
                    Toast.makeText(
                        this,
                        "NIS ini sudah memiliki akun.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }

                if (kodeInput != kodeFirestore) {
                    Toast.makeText(
                        this,
                        "Kode daftar salah.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }

                buatAkunSiswa(
                    nis = nis,
                    namaSiswa = namaSiswa,
                    email = email,
                    password = password
                )
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    "Gagal cek data siswa: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun buatAkunSiswa(
        nis: String,
        namaSiswa: String,
        email: String,
        password: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = auth.currentUser?.uid

                if (uid == null) {
                    Toast.makeText(this, "UID tidak ditemukan", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val userData = hashMapOf(
                    "nama" to namaSiswa,
                    "email" to email,
                    "role" to "siswa",
                    "nis" to nis,
                    "idGuru" to ""
                )

                firestore.collection("users")
                    .document(uid)
                    .set(userData)
                    .addOnSuccessListener {
                        updateUserIdSiswa(nis, uid)
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(
                            this,
                            "Gagal menyimpan user: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    "Gagal membuat akun: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun updateUserIdSiswa(nis: String, uid: String) {
        firestore.collection("siswa")
            .document(nis)
            .update("userId", uid)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Akun siswa berhasil dibuat. Silakan login.",
                    Toast.LENGTH_LONG
                ).show()

                auth.signOut()

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    "Akun dibuat, tetapi gagal update data siswa: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}