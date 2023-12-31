package ipt.lei.dam.ncrapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.models.didyouknow.DidYouKnowResponse

class DidYouKnowAdapter(private val context: Context, private val didyouknowList: List<DidYouKnowResponse>) : RecyclerView.Adapter<DidYouKnowAdapter.ViewHolder>() {
    var toast: Toast? = null
    var onItemClickListener: ((DidYouKnowResponse) -> Unit)? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val didYouKnowTitle: TextView = view.findViewById(R.id.didyouknow_title)
        val didYouKnowDescription: TextView = view.findViewById(R.id.didyouknow_description)
        val didYouKnowReferences: TextView = view.findViewById(R.id.didyouknow_references)
        val didYouKnowDetailsBtn : Button = view.findViewById(R.id.btnDidYouKnowDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.did_you_know_card_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = didyouknowList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val didyouknow = didyouknowList[position]

        holder.didYouKnowTitle.text = didyouknow.title
        holder.didYouKnowDescription.text = didyouknow.text
        holder.didYouKnowReferences.text = didyouknow.references

        holder.didYouKnowDetailsBtn.setOnClickListener {
            onItemClickListener?.invoke(didyouknow)
        }
    }


}