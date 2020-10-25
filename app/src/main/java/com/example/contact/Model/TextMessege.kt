package com.example.contact.Model

import java.util.*

data class TextMessege(val text:String,
                       override val senderId:String,
                       override val recipientId:String,
                       override val senderName: String,
                       override val recipientName: String,
                       override val date:Date,
                       override val type:String=MessageType.Text):Message {

    constructor():this("","","","","",Date())
}