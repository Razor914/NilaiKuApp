package raztech.dev.nilaisiswaapp.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import raztech.dev.nilaisiswaapp.R
import raztech.dev.nilaisiswaapp.admin.DashboardAdminActivity
import raztech.dev.nilaisiswaapp.guru.DashboardGuruActivity
import raztech.dev.nilaisiswaapp.siswa.DashboardSiswaActivity
import android.widget.TextView
import raztech.dev.nilaisiswaapp.auth.RegisterSiswaActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var txtDaftarSiswa: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        edtEmail = findViewById(R.id.edtEmail)
        edtPassword = findViewById(R.id.edtPassword)
        btnLogin = findViewById(R.id.btnLogin)

        txtDaftarSiswa = findViewById(R.id.txtDaftarSiswa)

        txtDaftarSiswa.setOnClickListener {
            startActivity(Intent(this, RegisterSiswaActivity::class.java))
        }

        btnLogin.setOnClickListener {
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this,
                    "Email dan password wajib diisi",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                loginUser(email, password)
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = auth.currentUser?.uid

                if (uid == null) {
                    Toast.makeText(
                        this,
                        "UID user tidak ditemukan",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addOnSuccessListener
                }

                cekRoleUser(uid)
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    "Login gagal: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun cekRoleUser(uid: String) {
        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    Toast.makeText(
                        this,
                        "Data user tidak ditemukan di Firestore",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }

                val role = document.getString("role")

                when (role) {
                    "admin" -> {
                        Toast.makeText(
                            this,
                            "Login sebagai Admin",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(this, DashboardAdminActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                    "guru" -> {
                        Toast.makeText(
                            this,
                            "Login sebagai Guru",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(this, DashboardGuruActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                    "siswa" -> {
                        Toast.makeText(
                            this,
                            "Login sebagai Siswa",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(this, DashboardSiswaActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                    else -> {
                        Toast.makeText(
                            this,
                            "Role tidak dikenali: $role",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    "Gagal membaca data user: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}