package raztech.dev.nilaisiswaapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import raztech.dev.nilaisiswaapp.R
import raztech.dev.nilaisiswaapp.model.Nilai

class NilaiGuruAdapter(
    private val listNilai: MutableList<Nilai>,
    private val onEditClick: (Nilai) -> Unit,
    private val onDeleteClick: (Nilai) -> Unit
) : RecyclerView.Adapter<NilaiGuruAdapter.NilaiGuruViewHolder>() {

    class NilaiGuruViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNamaSiswaNilaiGuru: TextView = itemView.findViewById(R.id.txtNamaSiswaNilaiGuru)
        val txtDetailSiswaNilaiGuru: TextView = itemView.findViewById(R.id.txtDetailSiswaNilaiGuru)
        val txtStatusKelulusanGuru: TextView = itemView.findViewById(R.id.txtStatusKelulusanGuru)
        val txtNilaiTugasGuru: TextView = itemView.findViewById(R.id.txtNilaiTugasGuru)
        val txtNilaiUtsGuru: TextView = itemView.findViewById(R.id.txtNilaiUtsGuru)
        val txtNilaiUasGuru: TextView = itemView.findViewById(R.id.txtNilaiUasGuru)
        val txtNilaiAkhirGuru: TextView = itemView.findViewById(R.id.txtNilaiAkhirGuru)
        val btnEditNilaiGuru: Button = itemView.findViewById(R.id.btnEditNilaiGuru)
        val btnHapusNilaiGuru: Button = itemView.findViewById(R.id.btnHapusNilaiGuru)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NilaiGuruViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nilai_guru, parent, false)

        return NilaiGuruViewHolder(view)
    }

    override fun onBindViewHolder(holder: NilaiGuruViewHolder, position: Int) {
        val nilai = listNilai[position]

        holder.txtNamaSiswaNilaiGuru.text = nilai.namaSiswa
        holder.txtDetailSiswaNilaiGuru.text = "NIS: ${nilai.nis} • Kelas: ${nilai.kelas}"

        holder.txtNilaiTugasGuru.text = nilai.nilaiTugas.toString()
        holder.txtNilaiUtsGuru.text = nilai.nilaiUts.toString()
        holder.txtNilaiUasGuru.text = nilai.nilaiUas.toString()
        holder.txtNilaiAkhirGuru.text = String.format("%.1f", nilai.nilaiAkhir)

        holder.txtStatusKelulusanGuru.text = nilai.statusKelulusan

        if (nilai.statusKelulusan == "Lulus") {
            holder.txtStatusKelulusanGuru.setBackgroundColor(Color.parseColor("#10B981"))
        } else {
            holder.txtStatusKelulusanGuru.setBackgroundColor(Color.parseColor("#EF4444"))
        }

        holder.btnEditNilaiGuru.setOnClickListener {
            onEditClick(nilai)
        }

        holder.btnHapusNilaiGuru.setOnClickListener {
            onDeleteClick(nilai)
        }
    }

    override fun getItemCount(): Int {
        return listNilai.size
    }

    fun updateData(newList: List<Nilai>) {
        listNilai.clear()
        listNilai.addAll(newList)
        notifyDataSetChanged()
    }
}