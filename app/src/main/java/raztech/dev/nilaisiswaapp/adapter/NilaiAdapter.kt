package raztech.dev.nilaisiswaapp.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import raztech.dev.nilaisiswaapp.R
import raztech.dev.nilaisiswaapp.model.Nilai

class NilaiAdapter(
    private val listNilai: MutableList<Nilai>
) : RecyclerView.Adapter<NilaiAdapter.NilaiViewHolder>() {

    class NilaiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNamaSiswaNilai: TextView = itemView.findViewById(R.id.txtNamaSiswaNilai)
        val txtDetailSiswaNilai: TextView = itemView.findViewById(R.id.txtDetailSiswaNilai)
        val txtStatusKelulusan: TextView = itemView.findViewById(R.id.txtStatusKelulusan)
        val txtMapelGuru: TextView = itemView.findViewById(R.id.txtMapelGuru)
        val txtNilaiTugas: TextView = itemView.findViewById(R.id.txtNilaiTugas)
        val txtNilaiUts: TextView = itemView.findViewById(R.id.txtNilaiUts)
        val txtNilaiUas: TextView = itemView.findViewById(R.id.txtNilaiUas)
        val txtNilaiAkhir: TextView = itemView.findViewById(R.id.txtNilaiAkhir)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NilaiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nilai, parent, false)

        return NilaiViewHolder(view)
    }

    override fun onBindViewHolder(holder: NilaiViewHolder, position: Int) {
        val nilai = listNilai[position]

        holder.txtNamaSiswaNilai.text = nilai.namaSiswa
        holder.txtDetailSiswaNilai.text = "NIS: ${nilai.nis} • Kelas: ${nilai.kelas}"
        holder.txtMapelGuru.text = "${nilai.mataPelajaran} • ${nilai.namaGuru}"

        holder.txtNilaiTugas.text = nilai.nilaiTugas.toString()
        holder.txtNilaiUts.text = nilai.nilaiUts.toString()
        holder.txtNilaiUas.text = nilai.nilaiUas.toString()
        holder.txtNilaiAkhir.text = String.format("%.1f", nilai.nilaiAkhir)

        holder.txtStatusKelulusan.text = nilai.statusKelulusan

        if (nilai.statusKelulusan == "Lulus") {
            holder.txtStatusKelulusan.setBackgroundColor(Color.parseColor("#10B981"))
        } else {
            holder.txtStatusKelulusan.setBackgroundColor(Color.parseColor("#EF4444"))
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