package raztech.dev.nilaisiswaapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import raztech.dev.nilaisiswaapp.R
import raztech.dev.nilaisiswaapp.model.Guru
import android.graphics.Color

class GuruAdapter(
    private val listGuru: MutableList<Guru>,
    private val onEditClick: (Guru) -> Unit,
    private val onDeleteClick: (Guru) -> Unit
) : RecyclerView.Adapter<GuruAdapter.GuruViewHolder>() {

    class GuruViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtAvatarGuru: TextView = itemView.findViewById(R.id.txtAvatarGuru)
        val txtNamaGuru: TextView = itemView.findViewById(R.id.txtNamaGuru)
        val txtDetailGuru: TextView = itemView.findViewById(R.id.txtDetailGuru)
        val txtStatusAkunGuru: TextView = itemView.findViewById(R.id.txtStatusAkunGuru)

        val btnEditGuru: View = itemView.findViewById(R.id.btnEditGuru)
        val btnHapusGuru: View = itemView.findViewById(R.id.btnHapusGuru)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuruViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_guru, parent, false)

        return GuruViewHolder(view)
    }

    override fun onBindViewHolder(holder: GuruViewHolder, position: Int) {
        val guru = listGuru[position]

        holder.txtAvatarGuru.text = guru.namaGuru.firstOrNull()?.uppercase() ?: "G"
        holder.txtNamaGuru.text = guru.namaGuru
        holder.txtDetailGuru.text = "ID: ${guru.idGuru} • ${guru.mataPelajaran}"

        if (guru.userId.isEmpty()) {
            holder.txtStatusAkunGuru.text = "Status Akun: Belum Terhubung"
            holder.txtStatusAkunGuru.setTextColor(Color.parseColor("#2563EB"))
        } else {
            holder.txtStatusAkunGuru.text = "Status Akun: Sudah Terhubung"
            holder.txtStatusAkunGuru.setTextColor(Color.parseColor("#10B981"))
        }

        holder.btnEditGuru.setOnClickListener {
            onEditClick(guru)
        }

        holder.btnHapusGuru.setOnClickListener {
            onDeleteClick(guru)
        }
    }

    override fun getItemCount(): Int {
        return listGuru.size
    }

    fun updateData(newList: List<Guru>) {
        listGuru.clear()
        listGuru.addAll(newList)
        notifyDataSetChanged()
    }
}