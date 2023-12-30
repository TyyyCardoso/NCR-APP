package ipt.lei.dam.ncrapp.activities

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.models.EventResponse
import ipt.lei.dam.ncrapp.network.RetrofitClient
import java.text.SimpleDateFormat


class EventsAdapter(private val context: Context, private val eventsList: List<EventResponse>) : RecyclerView.Adapter<EventsAdapter.ViewHolder>() {

    var onItemClickListener: ((EventResponse) -> Unit)? = null
    var onItemClickSubscribeListener: ((EventResponse, Int) -> Unit)? = null
    var toast: Toast? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventImage: ImageView = view.findViewById(R.id.event_image)
        val eventName: TextView = view.findViewById(R.id.event_name)
        val eventDescription: TextView = view.findViewById(R.id.event_description)
        val eventDate: TextView = view.findViewById(R.id.event_date)
        val eventSubscribeBtn : Button = view.findViewById(R.id.btnEventSubscribe)
        val eventDetailsBtn : Button = view.findViewById(R.id.btnEventDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_card_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = eventsList[position]

        val sharedPreferences = context.getSharedPreferences("UserInfo", Context.MODE_PRIVATE)
        val clientType = sharedPreferences.getString("clientType", "student")


        if (!event.image.isNullOrBlank()){
            val url = "" + RetrofitClient.BASE_URL + "event/images/" + event.image
            println("Event: " + event.name + "getting image from: " + url)
            Picasso.get()
                .load(url)
                .fit()
                .centerInside()
                //.placeholder(R.drawable.default_event_img)
                .error(R.drawable.default_event_img)
                .into(holder.eventImage)
        }

        holder.eventName.text = event.name
        holder.eventDescription.text = event.description


        holder.eventDate.text = convertDateTime(event.date.toString())

        holder.eventDetailsBtn.setOnClickListener {
            onItemClickListener?.invoke(event)
        }

        if(null!=event.subscribed){
            if(event.subscribed!!){
                holder.eventSubscribeBtn.setText("Cancelar")
                holder.eventSubscribeBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.buttonRedColor)) // Exemplo para mudar a cor de fundo do botão
            }
        }


        holder.eventSubscribeBtn.setOnClickListener {
            if(clientType.equals("student")){
                if (toast != null) {
                    toast!!.setText("É necessário fazer login para se inscrever nos eventos")
                } else {
                    toast = Toast.makeText(context, "É necessário fazer login para se inscrever nos eventos", Toast.LENGTH_SHORT)
                }
                toast!!.show()
            }else{
                onItemClickSubscribeListener?.invoke(event, position)
            }
        }

        if(clientType.equals("student")){
            holder.eventSubscribeBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey)) // Exemplo para mudar a cor de fundo do botão
        }
    }

    override fun getItemCount() = eventsList.size

    fun convertDateTime(dateTimeStr: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm")
        val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm")

        val date = inputFormat.parse(dateTimeStr)
        return outputFormat.format(date)
    }

}

data class Event(val title: String, val description: String, val imageUrl: String)
