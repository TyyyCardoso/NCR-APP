package ipt.lei.dam.ncrapp.activities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ipt.lei.dam.ncrapp.models.EventResponse
import ipt.lei.dam.ncrapp.R

class EventsAdapter(private val eventsList: List<EventResponse>) : RecyclerView.Adapter<EventsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val textViewTitle: TextView = view.findViewById(R.id.textViewTitle)
        val textViewDescription: TextView = view.findViewById(R.id.textViewDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_card_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = eventsList[position]
        // Carregando a imagem com Picasso
        Picasso.get()
            .load(event.imageUrl)
            .placeholder(R.drawable.cotonete) // Uma imagem de placeholder enquanto carrega
            .error(R.drawable.baseline_event_note_24) // Uma imagem de erro caso não consiga carregar
            .fit()
            .centerInside()
            .into(holder.imageView)
        holder.textViewTitle.text = event.title
        holder.textViewDescription.text = event.description
    }

    override fun getItemCount() = eventsList.size
}

data class Event(val title: String, val description: String, val imageUrl: String)
