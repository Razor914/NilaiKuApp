package raztech.dev.nilaisiswaapp.admin

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import raztech.dev.nilaisiswaapp.R
import raztech.dev.nilaisiswaapp.auth.LoginActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class DashboardAdminActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var txtTotalSiswa: TextView
    private lateinit var txtTotalGuru: TextView
    private lateinit var txtTotalNilai: TextView

    private lateinit var cardKelolaSiswa: CardView
    private lateinit var cardKelolaGuru: CardView
    private lateinit var cardLaporanNilai: CardView
    private lateinit var cardLogout: CardView

    private lateinit var swipeRefreshDashboardAdmin: SwipeRefreshLayout

    private lateinit var cardBuatAkunGuru: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_admin)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        txtTotalSiswa = findViewById(R.id.txtTotalSiswa)
        txtTotalGuru = findViewById(R.id.txtTotalGuru)
        txtTotalNilai = findViewById(R.id.txtTotalNilai)

        cardKelolaSiswa = findViewById(R.id.cardKelolaSiswa)
        cardKelolaGuru = findViewById(R.id.cardKelolaGuru)
        cardLaporanNilai = findViewById(R.id.cardLaporanNilai)
        cardLogout = findViewById(R.id.cardLogout)

        cardBuatAkunGuru = findViewById(R.id.cardBuatAkunGuru)

        swipeRefreshDashboardAdmin = findViewById(R.id.swipeRefreshDashboardAdmin)

        swipeRefreshDashboardAdmin.setOnRefreshListener {
            loadStatistikDashboard()
        }

        loadStatistikDashboard()
        setupMenuClick()
    }

    private fun loadStatistikDashboard() {
        firestore.collection("siswa")
            .get()
            .addOnSuccessListener { result ->
                txtTotalSiswa.text = result.size().toString()
                swipeRefreshDashboardAdmin.isRefreshing = false
            }
            .addOnFailureListener {
                swipeRefreshDashboardAdmin.isRefreshing = false
            }

        firestore.collection("guru")
            .get()
            .addOnSuccessListener { result ->
                txtTotalGuru.text = result.size().toString()
            }

        firestore.collection("nilai")
            .get()
            .addOnSuccessListener { result ->
                txtTotalNilai.text = result.size().toString()
            }
    }

    private fun setupMenuClick() {
        cardKelolaSiswa.setOnClickListener {
            startActivity(Intent(this, KelolaSiswaActivity::class.java))
        }

        cardKelolaGuru.setOnClickListener {
            startActivity(Intent(this, KelolaGuruActivity::class.java))
        }

        cardLaporanNilai.setOnClickListener {
            startActivity(Intent(this, LaporanNilaiActivity::class.java))
        }

        cardLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        cardBuatAkunGuru.setOnClickListener {
            startActivity(Intent(this, BuatAkunGuruActivity::class.java))
        }
    }
}