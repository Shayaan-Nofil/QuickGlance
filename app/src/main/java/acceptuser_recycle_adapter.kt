import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.m_abdullah.quickglance.R

class acceptuser_recycle_adapter(private val items: MutableList<User>): RecyclerView.Adapter<acceptuser_recycle_adapter.ViewHolder>() {
    private lateinit var onAcceptClickListener: OnAcceptClickListener
    private lateinit var onRejectClickListener: OnRejectClickListener

    class ViewHolder (itemview: View): RecyclerView.ViewHolder(itemview){
        val personimg: ImageView = itemView.findViewById(R.id.person_img)
        val personname: TextView = itemView.findViewById(R.id.person_name)
        val personusername: TextView = itemView.findViewById(R.id.person_username)
        val acceptButton: Button = itemView.findViewById(R.id.add_button)
        val rejectButton: Button = itemview.findViewById(R.id.reject_button)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): acceptuser_recycle_adapter.ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.acceptuser_recycle, parent, false)
        return acceptuser_recycle_adapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }
    override fun onBindViewHolder(holder: acceptuser_recycle_adapter.ViewHolder, position: Int) {
        val usr = items[position]

        holder.acceptButton.setOnClickListener {
            onAcceptClickListener.onAcceptClick(position, items[position])
        }

        holder.rejectButton.setOnClickListener {
            onRejectClickListener.onRejectClick(position, items[position])
        }

        holder.personname.text = usr.name
        holder.personusername.text = usr.username

        Glide.with(holder.itemView)
            .load(usr.profilepic)
            .into(holder.personimg)
    }

    interface OnAcceptClickListener {
        fun onAcceptClick(position: Int, model: User)
    }

    interface OnRejectClickListener {
        fun onRejectClick(position: Int, model: User)
    }

    // Functions to bind the click listeners
    fun setOnAcceptClickListener(onAcceptClickListener: OnAcceptClickListener) {
        this.onAcceptClickListener = onAcceptClickListener
    }

    fun setOnRejectClickListener(onItemClickListener: OnRejectClickListener) {
        this.onRejectClickListener = onItemClickListener
    }
}