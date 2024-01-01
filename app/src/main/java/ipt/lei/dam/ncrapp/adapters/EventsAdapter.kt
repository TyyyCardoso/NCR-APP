package ipt.lei.dam.ncrapp.adapters

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
import ipt.lei.dam.ncrapp.models.events.EventResponse
import ipt.lei.dam.ncrapp.network.RetrofitClient
import java.text.SimpleDateFormat
import java.util.Date


class EventsAdapter(private val context: Context, private val eventsList: List<EventResponse>) : RecyclerView.Adapter<EventsAdapter.ViewHolder>() {
    //Listener para abrir detalhes do evento
    var onItemClickListener: ((EventResponse) -> Unit)? = null
    //Listener para inscrever no evento
    var onItemClickSubscribeListener: ((EventResponse, Int) -> Unit)? = null
    var toast: Toast? = null

    //Definiçao do viewHolder
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

    //Para cada item (evento)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //Obtem o evento do momento
        val event = eventsList[position]

        //Obtem info do user
        val sharedPreferences = context.getSharedPreferences("UserInfo", Context.MODE_PRIVATE)
        val clientType = sharedPreferences.getString("clientType", "student")

        //Verifica se existe referencia a imagem
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
        } else {
            holder.eventImage.setImageResource(R.drawable.default_event_img)
        }

        //Atualiza a UI com os valores do Evento
        holder.eventName.text = event.name
        holder.eventDescription.text = event.description
        holder.eventDate.text = convertDateTime(event.date.toString())
        holder.eventDetailsBtn.setOnClickListener {
            onItemClickListener?.invoke(event)
        }

        //Verificar se evento ja passou ou nãp
        var eventDate = event.date.toString().substring(0,10)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        var date : Date =  dateFormat.parse(eventDate);
        val today = Date()

        //Definir button de subscrever de acordo com data e se esta ou nao inscrito
        if(null!=event.subscribed){
            if(event.subscribed!!){
                holder.eventSubscribeBtn.text = "Cancelar"
                holder.eventSubscribeBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.buttonRedColor)) // Exemplo para mudar a cor de fundo do botão
            }else if(today.after(date)){
                holder.eventSubscribeBtn.text = "Já começou"
                holder.eventSubscribeBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey)) // Exemplo para mudar a cor de fundo do botão
            }

        }

        //Listener para subcrever
        holder.eventSubscribeBtn.setOnClickListener {
            if(!today.after(date)) {
                if (clientType.equals("student")) {
                    if (toast != null) {
                        toast!!.setText("É necessário fazer login para se inscrever nos eventos")
                    } else {
                        toast = Toast.makeText(
                            context,
                            "É necessário fazer login para se inscrever nos eventos",
                            Toast.LENGTH_SHORT
                        )
                    }
                    toast!!.show()
                } else {
                    onItemClickSubscribeListener?.invoke(event, position)
                }
            }else{
                if (toast != null) {
                    toast!!.setText("Este evento já expirou")
                } else {
                    toast = Toast.makeText(
                        context,
                        "Este evento já expirou",
                        Toast.LENGTH_SHORT
                    )
                }
                toast!!.show()
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
