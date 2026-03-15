package com.example.chatapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView

class UserAdapter(
    private val users: List<User>,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_conversation, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.tvUserName.text = user.name
        holder.tvLastMessage.text = user.email // Just showing email as a placeholder for last message
        
        // Placeholder for profile image
        // holder.ivUserAvatar.setImageResource(R.drawable.sample_avatar)

        holder.itemView.setOnClickListener {
            onUserClick(user)
        }
    }

    override fun getItemCount(): Int = users.size

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivUserAvatar: ShapeableImageView = itemView.findViewById(R.id.ivUserAvatar)
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
    }
}