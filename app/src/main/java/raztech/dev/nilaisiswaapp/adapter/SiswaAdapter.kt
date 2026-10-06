package raztech.dev.nilaisiswaapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import raztech.dev.nilaisiswaapp.R
import raztech.dev.nilaisiswaapp.model.Siswa

class SiswaAdapter(
    private val listSiswa: MutableList<Siswa>,
    private val onEditClick: (Siswa) -> Unit,
    private val onDeleteClick: (Siswa) -> Unit
) : RecyclerView.Adapter<SiswaAdapter.SiswaViewHolder>() {

    class SiswaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtAvatar: TextView = itemView.findViewById(R.id.txtAvatar)
        val txtNamaSiswa: TextView = itemView.findViewById(R.id.txtNamaSiswa)
        val txtDetailSiswa: TextView = itemView.findViewById(R.id.txtDetailSiswa)
        val btnEditSiswa: ImageButton = itemView.findViewById(R.id.btnEditSiswa)
        val btnHapusSiswa: ImageButton = itemView.findViewById(R.id.btnHapusSiswa)
        val txtKodeSiswa: TextView = itemView.findViewById(R.id.txtKodeSiswa)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SiswaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_siswa, parent, false)

        return SiswaViewHolder(view)
    }

    override fun onBindViewHolder(holder: SiswaViewHolder, position: Int) {
        val siswa = listSiswa[position]

        holder.txtAvatar.text = siswa.namaSiswa.firstOrNull()?.uppercase() ?: "-"
        holder.txtNamaSiswa.text = siswa.namaSiswa
        holder.txtDetailSiswa.text = "NIS: ${siswa.nis} • Kelas: ${siswa.kelas}"
        holder.txtKodeSiswa.text = if (siswa.userId.isEmpty()) {
            "Kode daftar: ${siswa.kodeDaftar}"
        } else {
            "Akun siswa sudah terhubung"
        }

        holder.btnEditSiswa.setOnClickListener {
            onEditClick(siswa)
        }

        holder.btnHapusSiswa.setOnClickListener {
            onDeleteClick(siswa)
        }
    }

    override fun getItemCount(): Int {
        return listSiswa.size
    }

    fun updateData(newList: List<Siswa>) {
        listSiswa.clear()
        listSiswa.addAll(newList)
        notifyDataSetChanged()
    }
}