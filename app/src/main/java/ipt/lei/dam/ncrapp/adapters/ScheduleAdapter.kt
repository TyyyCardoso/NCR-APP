package ipt.lei.dam.ncrapp.adapters

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.fragments.schedule.ScheduleFragment
import ipt.lei.dam.ncrapp.models.events.EventResponse
import ipt.lei.dam.ncrapp.models.schedule.ScheduleResponse
import ipt.lei.dam.ncrapp.models.staff.StaffMemberResponse
import ipt.lei.dam.ncrapp.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class ScheduleAdapter(private val context: Context, private var schedules: List<ScheduleResponse>) : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>()  {

    var onItemClickListener: ((ScheduleResponse) -> Unit)? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.schedule_item, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val scheduleTitle: TextView = view.findViewById(R.id.document_title)
        val scheduleDescription: TextView = view.findViewById(R.id.document_description)
        val doc_item: LinearLayout = view.findViewById(R.id.doc_item)
        val doc_delete_item: ImageView = view.findViewById(R.id.delete_document_icon)
        val document_icon: ImageView = view.findViewById(R.id.document_icon)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val schedule = schedules[position]

        val url = "" + RetrofitClient.BASE_URL + "files/docs/" + schedule.docReference

        //holder.staffMemberImage. = member.image
        holder.scheduleTitle.text = schedule.docName
        //holder.scheduleDescription.text = schedule.docDescription
        holder.doc_item.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.dialogAlertTitle))
                .setMessage("Quer transferir o ficheiro \"" + schedule.docName + "\" ?")
                .setNeutralButton("Não", null)
                .setPositiveButton("Sim") { _, _ ->
                    downloadFile(url, schedule.docName)
                }
                .show()
        }

        holder.document_icon.setOnClickListener {
            AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.dialogAlertTitle))
            .setMessage("Quer transferir o ficheiro \"" + schedule.docName + "\" ?")
            .setNeutralButton("Não", null)
            .setPositiveButton("Sim") { _, _ ->
                downloadFile(url, schedule.docName)
            }
            .show() }

        holder.doc_delete_item.setOnClickListener {
            onItemClickListener?.invoke(schedule)
        }

        val sharedPref = context.getSharedPreferences("UserInfo", AppCompatActivity.MODE_PRIVATE)
        val clientType = sharedPref.getString("clientType", "member");

        if(clientType.equals("ADMINISTRADOR")){
            holder.doc_delete_item.visibility = View.VISIBLE;
        }
    }

    fun updateData(newSchedules: List<ScheduleResponse>) {
        schedules = newSchedules
        notifyDataSetChanged()
    }



    override fun getItemCount(): Int = schedules.size

    private fun downloadFile(url: String?, docName: String?) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL(url)
                val connection = url.openConnection()
                val inputStream = connection.getInputStream()

                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

                val fileName = docName

                val file = File(dir, fileName)
                val output = FileOutputStream(file)

                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }
                output.close()
                inputStream.close()
                withContext(Dispatchers.Main) {
                    openFileInDocumentReader(file)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle exceptions or errors
            }
        }
    }

    private fun openFileInDocumentReader(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.applicationContext.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION

        try{
            context.startActivity(intent)
        }catch (e: ActivityNotFoundException) {
            println("Não existe leitor de pdf")
        }

    }
}
