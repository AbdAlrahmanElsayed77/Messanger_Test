package com.example.contact.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contact.ChatActivity
import com.example.contact.Model.TextMessege
import com.example.contact.Model.User
import com.example.contact.ProfileActivity
import com.example.contact.R
import com.example.contact.SearchActivity
import com.example.contact.recyclerView.chatItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.ViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_chat.view.*

class ChatFragment : Fragment() {

    private val firestoreInstance:FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    private lateinit var chatSection:Section


    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view =inflater.inflate(R.layout.fragment_chat, container, false)
        view.serch_edeitText.setOnClickListener {
            activity!!.startActivity(Intent(activity, SearchActivity::class.java))
        }

        val textviewTitle=activity?.findViewById(R.id.title_toolbaar_textView) as TextView
        textviewTitle.text= " Chats"

        val CircleImageViewProfileImage = activity?.findViewById(R.id.circleImageView_profile_image) as ImageView
        CircleImageViewProfileImage.setOnClickListener {
            startActivity(Intent(activity,ProfileActivity::class.java))
            activity!!.finish()
        }

        addChatLisener(::initRecyclerView)

        return view
    }

    private fun addChatLisener(onListen:(List<Item>) ->Unit):ListenerRegistration {


        return firestoreInstance.collection("users")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection("sharedChat")
            .orderBy("date",Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, firebaseFirestoreExeption ->
            if (firebaseFirestoreExeption != null){
                return@addSnapshotListener
            }
            val items= mutableListOf<Item>()

            querySnapshot!!.documents.forEach {document ->

                if (document.exists()){
                    items.add(chatItems(document.id,document.toObject(User::class.java)!!,document.toObject(TextMessege::class.java)!!,activity!!))
                }

            }

            onListen(items)
        }
    }

    private fun initRecyclerView(item:List<Item>){
        chat_recyclerView.apply {
            layoutManager=LinearLayoutManager(activity)
            adapter= GroupAdapter<ViewHolder>().apply {
                chatSection=Section(item)
                add(chatSection)
                setOnItemClickListener(onItemClick)
            }
        }
    }

    val onItemClick= OnItemClickListener{ item, view ->

        if (item is chatItems){
            val intentChatActivity = Intent(activity, ChatActivity::class.java)
            intentChatActivity.putExtra("user_name",item.user.name)
            intentChatActivity.putExtra("profile_image",item.user.profileImage)
            intentChatActivity.putExtra("other_id",item.uid)
            activity!!.startActivity(intentChatActivity)
        }

    }

}