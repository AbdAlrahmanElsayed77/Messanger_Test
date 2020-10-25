package com.example.contact

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.contact.Model.*
import com.example.contact.glide.GlideApp
import com.example.contact.recyclerView.RecipientImagMessageItem
import com.example.contact.recyclerView.RecipientTextMessageItem
import com.example.contact.recyclerView.SenderImageMessageItem
import com.example.contact.recyclerView.SenderTextMessageItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.activity_chat.*
import java.io.ByteArrayOutputStream
import java.util.*

class ChatActivity : AppCompatActivity() {


    private lateinit var mCurrentChatChannelId: String
    private val storageInstance:FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    private val firestoreInstance :FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    private val currentUserDocRef:DocumentReference
    get() = firestoreInstance.document(
        "users/${FirebaseAuth.getInstance().currentUser?.uid}"
    )

    private val currentImageRef:StorageReference
    get() = storageInstance.reference

    private val chatChannelsCollectionRef=firestoreInstance.collection("chatChannels")
    private var mRecipientId= ""
    private var mCurrentUserId = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var currentUser:User
    private val messageAdapter by lazy { GroupAdapter<ViewHolder>() }






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        getUserInfo {user->
            currentUser=user
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            window.statusBarColor = Color.WHITE
        }

        val userName = intent.getStringExtra("user_name")
        val profileImage = intent.getStringExtra("profile_image")
        mRecipientId= intent.getStringExtra("other_id")!!
        textView_user_name.text = userName


        fab_send_image.setOnClickListener {
            val myIntentImage=Intent().apply {
                type="image/*"
                action=Intent.ACTION_GET_CONTENT
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg","image/png"))
            }
            startActivityForResult(Intent.createChooser(myIntentImage,"Select Image"),2)
        }


        creatChatChannel{ channelId ->

            mCurrentChatChannelId=channelId

            getMessage(channelId)

            imageView_send.setOnClickListener {
                val text=editText_messege.text.toString()
                if (text.isNotEmpty()){
                    val messageSend =TextMessege(
                        text,
                        mCurrentUserId,
                        mRecipientId,
                        currentUser.name,
                        "",
                        Calendar.getInstance().time
                    )
                    sendMessage(channelId,messageSend,editText_messege.text.toString())
                    editText_messege.setText("")
                }else{
                    Toast.makeText(this,"Empte",Toast.LENGTH_LONG).show()
                }
            }
        }

        chat_recyclerView.apply {
            adapter = messageAdapter
        }



        if (profileImage!!.isNotEmpty()) {
            GlideApp.with(this)
                .load(storageInstance.getReference(profileImage))
                .into(circle_imageView_profile_picture)
        } else {
            circle_imageView_profile_picture.setImageResource(R.drawable.ic_account_circle)
        }



        imageView_back.setOnClickListener {
            finish()
        }

    }

    private fun sendMessage(channelId: String,message: Message,text:String) {

        val contentMessage= mutableMapOf<String,Any>()
        contentMessage["text"]=text
        contentMessage["senderId"]=message.senderId
        contentMessage["recipientId"]=message.recipientId
        contentMessage["senderName"]=message.senderName
        contentMessage["recipientName"]=message.recipientName
        contentMessage["date"]=message.date
        contentMessage["type"]=message.type
        chatChannelsCollectionRef.document(channelId).collection("messages").add(message)

        firestoreInstance.collection("users")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection("sharedChat")
            .document(mRecipientId)
            .update(contentMessage)

        firestoreInstance.collection("users")
            .document(mRecipientId)
            .collection("sharedChat")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .update(contentMessage)

    }


    private fun creatChatChannel(onComplete:(channelId:String)->Unit){
        firestoreInstance.collection("users")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection("sharedChat")
            .document(mRecipientId)
            .get()
            .addOnSuccessListener {document ->
                if (document.exists()) {
                    onComplete(document["ChannelId"] as @kotlin.ParameterName(name = "channelId") String)
                    return@addOnSuccessListener
                }

                val newChatChannel = firestoreInstance.collection("users").document()
                firestoreInstance.collection("users")
                    .document(mRecipientId)
                    .collection("sharedChat")
                    .document(mCurrentUserId)
                    .set(mapOf("ChannelId" to newChatChannel.id))

                firestoreInstance.collection("users")
                    .document(mCurrentUserId)
                    .collection("sharedChat")
                    .document(mRecipientId)
                    .set(mapOf("ChannelId" to newChatChannel.id))

                onComplete(newChatChannel.id)
            }
    }

    private fun getMessage(channelId: String){
        val query = chatChannelsCollectionRef.document(channelId).collection("messages")
            .orderBy("date",Query.Direction.DESCENDING)
        query.addSnapshotListener { querySnapshot, firebaseFirestoreExeption ->
            messageAdapter.clear()
            querySnapshot!!.documents.forEach {document ->

                if (document["type"] == MessageType.Text){
                    val textMessage=document.toObject(TextMessege::class.java)
                    if (textMessage?.senderId == mCurrentUserId){
                        messageAdapter.add(SenderTextMessageItem(document.toObject(TextMessege::class.java)!!,document.id,this))
                    }else{
                        messageAdapter.add(RecipientTextMessageItem(document.toObject(TextMessege::class.java)!!,document.id,this))
                    }
                }else{
                    val imageMessage=document.toObject(ImageMessage::class.java)
                    if (imageMessage?.senderId==mCurrentUserId){
                        messageAdapter.add(SenderImageMessageItem(document.toObject(ImageMessage::class.java)!!,document.id,this))
                    }else{
                        messageAdapter.add(RecipientImagMessageItem(document.toObject(ImageMessage::class.java)!!,document.id,this))
                    }

                }

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode==2 && resultCode==Activity.RESULT_OK && data!=null && data.data!=null){
            val selectedImagePath=data.data
            val selectedImageBmp=MediaStore.Images.Media.getBitmap(this.contentResolver,selectedImagePath)
            val outputStream=ByteArrayOutputStream()
            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG,25,outputStream)
            val selectedImageByts=outputStream.toByteArray()

            uploadImage(selectedImageByts){path->
                val imageMessage=ImageMessage(path,mCurrentUserId,mRecipientId,currentUser.name,"",Calendar.getInstance().time)

               // chatChannelsCollectionRef.document(mCurrentChatChannelId).collection("messages").add(imageMessage)

                sendMessage(mCurrentChatChannelId,imageMessage,"PHOTO")
            }

        }
    }

    private fun uploadImage(selectedImageByts: ByteArray,onSuccess:(imagePath:String)->Unit) {
        val ref=currentImageRef.child("${FirebaseAuth.getInstance().currentUser!!.uid}/images/${UUID.nameUUIDFromBytes(selectedImageByts)}")
            ref.putBytes(selectedImageByts)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    onSuccess(ref.path)
                    Toast.makeText(this,"Done",Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(this,"Error",Toast.LENGTH_LONG).show()

                }
            }
    }

    private fun getUserInfo(onComplete:(User) ->Unit){
        currentUserDocRef.get().addOnSuccessListener {
            onComplete(it.toObject(User::class.java)!!)
        }
    }

}