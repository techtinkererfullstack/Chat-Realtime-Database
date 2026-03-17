package com.example.chatapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var chatAdapter: ChatAdapter
    private val messagesList = mutableListOf<Message>()
    
    private var receiverUid: String? = null
    private var senderUid: String? = null
    private var chatRoomId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime()) // 1. Get keyboard insets

            // 2. Use Math.max to ensure we use whichever is larger (usually the keyboard)
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                Math.max(systemBars.bottom, ime.bottom)
            )

            insets
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        
        val name = intent.getStringExtra("name")
        receiverUid = intent.getStringExtra("uid")
        senderUid = auth.currentUser?.uid
        
        if (senderUid == null || receiverUid == null) {
            Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.tvName.text = name

        // Generate a unique chat room ID for these two users
        chatRoomId = if (senderUid!! < receiverUid!!) {
            senderUid + receiverUid
        } else {
            receiverUid + senderUid
        }

        chatAdapter = ChatAdapter(senderUid!!)
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }

        binding.btnSend.setOnClickListener {
            val msgText = binding.etMessage.text.toString().trim()
            if (msgText.isNotEmpty()) {
                sendMessage(msgText)
            }
        }

        listenForMessages()
        
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun sendMessage(text: String) {
        val messageId = database.child("chats").child(chatRoomId!!).child("messages").push().key
        val message = Message(
            senderId = senderUid,
            message = text,
            timestamp = System.currentTimeMillis()
        )
        
        if (messageId != null) {
            database.child("chats").child(chatRoomId!!).child("messages").child(messageId).setValue(message)
                .addOnSuccessListener {
                    binding.etMessage.text.clear()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun listenForMessages() {
        database.child("chats").child(chatRoomId!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messagesList.clear()
                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Message::class.java)
                        if (message != null) {
                            messagesList.add(message)
                        }
                    }
                    chatAdapter.setMessages(messagesList)
                    if (messagesList.isNotEmpty()) {
                        binding.rvMessages.smoothScrollToPosition(messagesList.size - 1)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChatActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}