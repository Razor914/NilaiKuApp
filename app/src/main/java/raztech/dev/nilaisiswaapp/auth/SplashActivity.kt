package raztech.dev.nilaisiswaapp.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import raztech.dev.nilaisiswaapp.R
import raztech.dev.nilaisiswaapp.admin.DashboardAdminActivity
import raztech.dev.nilaisiswaapp.guru.DashboardGuruActivity
import raztech.dev.nilaisiswaapp.siswa.DashboardSiswaActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        cekStatusLogin()
    }

    private fun cekStatusLogin() {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            masukKeLogin()
        } else {
            cekRoleUser(currentUser.uid)
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
                        "Data user tidak ditemukan. Silakan login ulang.",
                        Toast.LENGTH_LONG
                    ).show()

                    auth.signOut()
                    masukKeLogin()
                    return@addOnSuccessListener
                }

                val role = document.getString("role")

                when (role) {
                    "admin" -> {
                        startActivity(Intent(this, DashboardAdminActivity::class.java))
                        finish()
                    }

                    "guru" -> {
                        startActivity(Intent(this, DashboardGuruActivity::class.java))
                        finish()
                    }

                    "siswa" -> {
                        startActivity(Intent(this, DashboardSiswaActivity::class.java))
                        finish()
                    }

                    else -> {
                        Toast.makeText(
                            this,
                            "Role tidak dikenali. Silakan login ulang.",
                            Toast.LENGTH_LONG
                        ).show()

                        auth.signOut()
                        masukKeLogin()
                    }
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(
                    this,
                    "Gagal memeriksa login: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()

                auth.signOut()
                masukKeLogin()
            }
    }

    private fun masukKeLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}