package com.example.contact

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.contact.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_sign__up_.*

class Sign_Up_Activity : AppCompatActivity() ,TextWatcher {

    private val mAuth:FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val firestoreInstance:FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    private val currentusersDocRef:DocumentReference
    get() = firestoreInstance.document("users/${mAuth.currentUser?.uid.toString()}")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign__up_)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            window.decorView.systemUiVisibility=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }else{
            window.statusBarColor = Color.WHITE
        }

        edeitText_Name_sign_up.addTextChangedListener(this)
        edeitText_Email_sign_up.addTextChangedListener(this)
        edeitText_Password_sign_up.addTextChangedListener(this)

        btn_sign_up.setOnClickListener {
            val name = edeitText_Name_sign_up.text.toString().trim()
            val email = edeitText_Email_sign_up.text.toString().trim()
            val password = edeitText_Password_sign_up.text.toString().trim()

            if (name.isEmpty()){
                edeitText_Name_sign_up.error="Name Required"
                edeitText_Name_sign_up.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty()){
                edeitText_Email_sign_up.error="Email Required"
                edeitText_Email_sign_up.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                edeitText_Email_sign_up.error="Please Enter a Valid Email"
                edeitText_Email_sign_up.requestFocus()
                return@setOnClickListener
            }

            if (password.length < 6){
                edeitText_Password_sign_up.error="6 Char Required"
                edeitText_Password_sign_up.requestFocus()
                return@setOnClickListener
            }
            creatNewAccount(name,email,password)
        }

    }

    private fun creatNewAccount(name: String, email: String, password: String) {

        progress_sign_up.visibility= View.VISIBLE

        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->

            val newUser=User(name,password,email,"")
            currentusersDocRef.set(newUser)

            if (task.isSuccessful){

                progress_sign_up.visibility= View.INVISIBLE

                val intentMainActivity = Intent(this,MainActivity::class.java)
                intentMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intentMainActivity)
            }else{

                progress_sign_up.visibility= View.INVISIBLE

                Toast.makeText(this,task.exception?.message,Toast.LENGTH_SHORT).show()
            }

        }
    }




    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        btn_sign_up.isEnabled=edeitText_Name_sign_up.text.trim().isNotEmpty()
                && edeitText_Email_sign_up.text.trim().isNotEmpty()
                && edeitText_Password_sign_up.text.trim().isNotEmpty()
    }
}