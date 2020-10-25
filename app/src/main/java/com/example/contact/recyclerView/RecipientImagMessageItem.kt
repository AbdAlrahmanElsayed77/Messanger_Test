package com.example.contact.recyclerView

import android.content.Context
import android.text.format.DateFormat
import com.example.contact.Model.ImageMessage
import com.example.contact.R
import com.example.contact.glide.GlideApp
import com.google.firebase.storage.FirebaseStorage
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.sender_item_image_message.*

class RecipientImagMessageItem(private val imageMessage:ImageMessage,
                             private val messageID:String,
                             val context:Context):Item() {

    private val storageInstance:FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.textView_image_time.text=DateFormat.format("hh:mm a",imageMessage.date).toString()

        if (imageMessage.imagePath.isNotEmpty()){
            GlideApp.with(context)
                .load(storageInstance.getReference(imageMessage.imagePath))
                .placeholder(R.drawable.image)
                .into(viewHolder.imageView_message_image)
        }
    }

    override fun getLayout():Int{
        return R.layout.recipient_item_image_message
    }
}