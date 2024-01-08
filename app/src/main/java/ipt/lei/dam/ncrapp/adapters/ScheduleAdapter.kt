package ipt.lei.dam.ncrapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.models.schedule.ScheduleResponse


class ScheduleAdapter(private val context: Context, private var schedules: List<ScheduleResponse>) : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>()  {

    var onItemClickListener: ((ScheduleResponse) -> Unit)? = null
    var onItemClickTransferListener: ((ScheduleResponse) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.schedule_item, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val scheduleTitle: TextView = view.findViewById(R.id.document_title)
        //val scheduleDescription: TextView = view.findViewById(R.id.document_description)
        val docItem: LinearLayout = view.findViewById(R.id.doc_item)
        val docDeleteItem: ImageView = view.findViewById(R.id.delete_document_icon)
        val documentIcon: ImageView = view.findViewById(R.id.document_icon)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val schedule = schedules[position]

        //holder.staffMemberImage. = member.image
        holder.scheduleTitle.text = schedule.docName
        //holder.scheduleDescription.text = schedule.docDescription
        holder.docItem.setOnClickListener {
            onItemClickTransferListener?.invoke(schedule)
        }

        holder.documentIcon.setOnClickListener {
            onItemClickTransferListener?.invoke(schedule)
             }

        holder.docDeleteItem.setOnClickListener {
            onItemClickListener?.invoke(schedule)
        }

        val sharedPref = context.getSharedPreferences(context.getString(R.string.userInfo), AppCompatActivity.MODE_PRIVATE)
        val clientType = sharedPref.getString(context.getString(R.string.type), context.getString(R.string.member))

        if(clientType.equals(context.getString(R.string.admin))){
            holder.docDeleteItem.visibility = View.VISIBLE
        }
    }

    fun updateData(newSchedules: List<ScheduleResponse>) {
        schedules = newSchedules
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = schedules.size

}
