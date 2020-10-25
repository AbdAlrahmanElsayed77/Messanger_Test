package com.example.contact.Model

data class User(val name: String, val password: String, val email: String,val profileImage:String){

    constructor():this("","","","")
}