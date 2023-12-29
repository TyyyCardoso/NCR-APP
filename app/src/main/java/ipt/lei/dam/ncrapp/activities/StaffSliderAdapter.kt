package ipt.lei.dam.ncrapp.activities

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ipt.lei.dam.ncrapp.R
import ipt.lei.dam.ncrapp.models.StaffMemberResponse


class StaffSliderAdapter(private val context: Context, private var staffMembers: List<StaffMemberResponse>) : RecyclerView.Adapter<StaffSliderAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.staff_member_item, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val staffMemberImage: ImageView = view.findViewById(R.id.staffMemberImage)
        val staffMemberName: TextView = view.findViewById(R.id.staffMemberName)
        val staffMemberPosition: TextView = view.findViewById(R.id.staffMemberPosition)
        val staffMemberEmail: TextView = view.findViewById(R.id.staffMemberEmail)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val member = staffMembers[position]

        //holder.staffMemberImage. = member.image
        holder.staffMemberName.text = member.name
        holder.staffMemberPosition.text = member.cargo
        holder.staffMemberEmail.text = member.email

        if(!member.image.isNullOrEmpty()){
            val decodedString: ByteArray = Base64.decode(member.image, Base64.DEFAULT)

            // Converter array de bytes em Bitmap
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

            holder.staffMemberImage.setImageBitmap(decodedByte)
        }

    }

    fun updateData(newStaffMembers: List<StaffMemberResponse>) {
        staffMembers = newStaffMembers
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = staffMembers.size


}

