package raztech.dev.nilaisiswaapp.siswa

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import raztech.dev.nilaisiswaapp.R
import raztech.dev.nilaisiswaapp.auth.LoginActivity

class DashboardSiswaActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var txtWelcomeSiswa: TextView
    private lateinit var txtInfoSiswa: TextView
    private lateinit var cardLihatNilai: CardView
    private lateinit var cardLogoutSiswa: CardView

    private var nisLogin = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_siswa)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        txtWelcomeSiswa = findViewById(R.id.txtWelcomeSiswa)
        txtInfoSiswa = findViewById(R.id.txtInfoSiswa)
        cardLihatNilai = findViewById(R.id.cardLihatNilai)
        cardLogoutSiswa = findViewById(R.id.cardLogoutSiswa)

        loadDataSiswaLogin()
        setupMenu()
    }

    private fun loadDataSiswaLogin() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val nama = document.getString("nama") ?: "Siswa"
                nisLogin = document.getString("nis") ?: ""

                txtWelcomeSiswa.text = "Halo, $nama 👋"

                if (nisLogin.isNotEmpty()) {
                    txtInfoSiswa.text = "NIS: $nisLogin"
                } else {
                    txtInfoSiswa.text = "NIS belum terhubung dengan akun"
                }
            }
    }

    private fun setupMenu() {
        cardLihatNilai.setOnClickListener {
            val intent = Intent(this, NilaiPribadiActivity::class.java)
            startActivity(intent)
        }

        cardLogoutSiswa.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}