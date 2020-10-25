package com.example.contact.recyclerView

import android.content.Context
import android.text.format.DateFormat
import com.example.contact.Model.TextMessege
import com.example.contact.R
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import kotlinx.android.synthetic.main.item_text_message.*

class RecipientTextMessageItem(private val textMessage:TextMessege,private val messageID:String,val context:Context):Item() {
    override fun bind(viewHolder: ViewHolder, position: Int) {

        viewHolder.text_view_message.text=textMessage.text
        viewHolder.text_view_time.text=DateFormat.format("hh:mm a",textMessage.date).toString()
    }

    override fun getLayout():Int{
        return R.layout.recipient_item_text_message
    }
}