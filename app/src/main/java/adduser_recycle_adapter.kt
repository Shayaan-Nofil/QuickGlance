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

class adduser_recycle_adapter(private val items: MutableList<User>): RecyclerView.Adapter<adduser_recycle_adapter.ViewHolder>() {
    private lateinit var onAcceptClickListener: OnAcceptClickListener
    private lateinit var onItemClickListener: OnItemClickListener

    class ViewHolder (itemview: View): RecyclerView.ViewHolder(itemview){
        val personimg: ImageView = itemView.findViewById(R.id.person_img)
        val personname: TextView = itemView.findViewById(R.id.person_name)
        val addedtext: TextView = itemView.findViewById(R.id.added_text)
        val personusername: TextView = itemView.findViewById(R.id.person_username)
        val acceptButton: Button = itemView.findViewById(R.id.add_button)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): adduser_recycle_adapter.ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.adduser_recycle, parent, false)
        return adduser_recycle_adapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }
    override fun onBindViewHolder(holder: adduser_recycle_adapter.ViewHolder, position: Int) {
        val usr = items[position]
        holder.addedtext.visibility = View.GONE

        val mAuth = Firebase.auth
        FirebaseDatabase.getInstance().getReference("Requests").addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    var sent = false
                    for (data in snapshot.children){
                        val myclass = data.getValue(Requests::class.java)
                        if (myclass != null) {
                            if (myclass.senderid == mAuth.uid.toString() && myclass.recieverid == usr.id){
                                sent = true
                                Log.w("TAG", "sent is true")
                            }
                        }
                    }
                    if (sent){
                        Log.w("TAG", "Already sent")
                        holder.addedtext.visibility = View.VISIBLE
                        holder.acceptButton.visibility = View.GONE
                        holder.acceptButton.isClickable = false
                    }
                    else{
                        Log.w("TAG", "Request Sent")
                        holder.addedtext.visibility = View.GONE
                        holder.acceptButton.visibility = View.VISIBLE
                        holder.acceptButton.isClickable = true
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("TAG", "Failed to read value.", error.toException())
            }
        })

        holder.acceptButton.setOnClickListener {
            onAcceptClickListener.onAcceptClick(position, items[position])
        }

        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(position, items[position])
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

    interface OnItemClickListener {
        fun onItemClick(position: Int, model: User)
    }

    // Functions to bind the click listeners
    fun setOnAcceptClickListener(onAcceptClickListener: OnAcceptClickListener) {
        this.onAcceptClickListener = onAcceptClickListener
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }
}