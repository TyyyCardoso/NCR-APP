package ipt.lei.dam.ncrapp.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
        val eventInitDate: TextView = view.findViewById(R.id.eventInitDate)
        val eventEndDate: TextView = view.findViewById(R.id.eventEndDate)
        val layoutData: LinearLayout = view.findViewById(R.id.data)
        val layoutDataInicio : LinearLayout = view.findViewById(R.id.dataInicio)
        val layoutDataFim : LinearLayout = view.findViewById(R.id.dataFim)
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

            Glide.with(context)
                .load(url)
                .fitCenter()
                .centerInside()
                .error(R.drawable.default_event_img)
                .into(holder.eventImage)
        } else {
            holder.eventImage.setImageResource(R.drawable.default_event_img)
        }

        //Verificar se evento ja passou ou nãp
        var eventDate : String
        //Verificar se evento ja passou ou nãp
        var eventEndDate : String
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val today = Date()
        var isEventAvailable = true

        //Atualiza a UI com os valores do Evento
        holder.eventName.text = event.name
        holder.eventDescription.text = event.description
        if(event.endDate.equals(null)){
            holder.layoutData.visibility = View.VISIBLE
            holder.layoutDataInicio.visibility = View.GONE
            holder.layoutDataFim.visibility = View.GONE
            holder.eventDate.text = convertDateTime(event.initDate.toString())
            eventDate = event.initDate.toString().substring(0,10)
            var date : Date =  dateFormat.parse(eventDate);
            if(today.before(date)) {
                if (null != event.subscribed) {
                    if (event.subscribed!!) {
                        holder.eventSubscribeBtn.text = "Cancelar"
                        holder.eventSubscribeBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.buttonRedColor)) // Exemplo para mudar a cor de fundo do botão
                    }
                }
            }else if(today.after(date)){
                holder.eventSubscribeBtn.text = "Evento terminado"
                holder.eventSubscribeBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey)) // Exemplo para mudar a cor de fundo do botão
                isEventAvailable = false
            }else{
                holder.eventSubscribeBtn.text = "Evento já começou"
                holder.eventSubscribeBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey)) // Exemplo para mudar a cor de fundo do botão
                isEventAvailable = false
            }

        }else{
            holder.layoutData.visibility = View.GONE
            holder.layoutDataInicio.visibility = View.VISIBLE
            holder.layoutDataFim.visibility = View.VISIBLE
            holder.eventInitDate.text = convertDateTime(event.initDate.toString())
            holder.eventEndDate.text = convertDateTime(event.endDate.toString())
            eventDate = event.initDate.toString().substring(0,10)
            eventEndDate = event.endDate.toString().substring(0,10)
            var initDate : Date =  dateFormat.parse(eventDate);
            var endDate : Date =  dateFormat.parse(eventEndDate);

            if(today.after(initDate) && today.before(endDate)){
                if (null != event.subscribed) {
                    if (event.subscribed!!) {
                        holder.eventSubscribeBtn.text = "Cancelar"
                        holder.eventSubscribeBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.buttonRedColor)) // Exemplo para mudar a cor de fundo do botão
                    }
                }
            }else if(today.before(initDate)){
                holder.eventSubscribeBtn.text = "Ainda não começou"
                holder.eventSubscribeBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey)) // Exemplo para mudar a cor de fundo do botão
                isEventAvailable = false
            }else if(today.after(endDate)){
                holder.eventSubscribeBtn.text = "Ainda já acabou"
                holder.eventSubscribeBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey)) // Exemplo para mudar a cor de fundo do botão
                isEventAvailable = false
            }
        }

        holder.eventDetailsBtn.setOnClickListener {
            onItemClickListener?.invoke(event)
        }



        /*
       //Definir button de subscrever de acordo com data e se esta ou nao inscrito
        if(null!=event.subscribed){
            if(event.subscribed!!){
                holder.eventSubscribeBtn.text = "Cancelar"
                holder.eventSubscribeBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.buttonRedColor)) // Exemplo para mudar a cor de fundo do botão
            }else if(today.after(date)){
                holder.eventSubscribeBtn.text = "Já começou"
                holder.eventSubscribeBtn.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.grey)) // Exemplo para mudar a cor de fundo do botão
            }
        }*/

        //Listener para subcrever
        holder.eventSubscribeBtn.setOnClickListener {
            if(isEventAvailable) {
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
                    toast!!.setText("Este evento não está disponível")
                } else {
                    toast = Toast.makeText(
                        context,
                        "Este evento não está disponível",
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
