package raztech.dev.nilaisiswaapp.guru

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import raztech.dev.nilaisiswaapp.R
import raztech.dev.nilaisiswaapp.auth.LoginActivity

class DashboardGuruActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var txtWelcomeGuru: TextView
    private lateinit var txtInfoGuru: TextView
    private lateinit var cardInputNilai: CardView
    private lateinit var cardLogoutGuru: CardView

    private lateinit var cardLaporanGuru: CardView

    private var idGuruLogin = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_guru)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        txtWelcomeGuru = findViewById(R.id.txtWelcomeGuru)
        txtInfoGuru = findViewById(R.id.txtInfoGuru)
        cardInputNilai = findViewById(R.id.cardInputNilai)
        cardLogoutGuru = findViewById(R.id.cardLogoutGuru)
        cardLaporanGuru = findViewById(R.id.cardLaporanGuru)

        loadDataGuruLogin()
        setupMenu()
    }

    private fun loadDataGuruLogin() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val nama = document.getString("nama") ?: "Guru"
                idGuruLogin = document.getString("idGuru") ?: ""

                txtWelcomeGuru.text = "Halo, $nama 👋"

                if (idGuruLogin.isNotEmpty()) {
                    firestore.collection("guru")
                        .document(idGuruLogin)
                        .get()
                        .addOnSuccessListener { guruDoc ->
                            val mapel = guruDoc.getString("mataPelajaran") ?: "-"
                            txtInfoGuru.text = "Mata pelajaran: $mapel"
                        }
                }
            }
    }

    private fun setupMenu() {
        cardInputNilai.setOnClickListener {
            val intent = Intent(this, InputNilaiActivity::class.java)
            startActivity(intent)
        }

        cardLaporanGuru.setOnClickListener {
            startActivity(Intent(this, LaporanGuruActivity::class.java))
        }

        cardLogoutGuru.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}