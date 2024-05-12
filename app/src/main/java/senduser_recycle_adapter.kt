import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.m_abdullah.quickglance.R

class senduser_recycle_adapter(private val items: MutableList<User>, private val sendarray: MutableList<String>): RecyclerView.Adapter<senduser_recycle_adapter.ViewHolder>() {

    class ViewHolder (itemview: View): RecyclerView.ViewHolder(itemview){
        val personimg: ImageView = itemView.findViewById(R.id.person_img)
        val personname: TextView = itemView.findViewById(R.id.person_name)
        val personusername: TextView = itemView.findViewById(R.id.person_username)
        val acceptButton: Button = itemView.findViewById(R.id.add_button)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): senduser_recycle_adapter.ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.senduser_recycle, parent, false)
        return senduser_recycle_adapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }
    override fun onBindViewHolder(holder: senduser_recycle_adapter.ViewHolder, position: Int) {
        val usr = items[position]

        holder.itemView.setOnClickListener {
            if (sendarray.contains(usr.id)){
                sendarray.remove(usr.id)
                holder.acceptButton.text = "Send"
                holder.acceptButton.setBackgroundResource(R.drawable.grey_rounded_box)
            }
            else{
                sendarray.add(usr.id)
                holder.acceptButton.text = "Selected"
                holder.acceptButton.setBackgroundResource(R.drawable.blue_rounded_box)
            }
        }

        holder.personname.text = usr.name
        holder.personusername.text = usr.username

        Glide.with(holder.itemView)
            .load(usr.profilepic)
            .into(holder.personimg)
    }

    fun getsendarray(): MutableList<String> {
        return sendarray
    }

}