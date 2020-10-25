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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_sign_in_.*

class Sign_in_Activity : AppCompatActivity() , TextWatcher{

    private val mAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val firestore:FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in_)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            window.decorView.systemUiVisibility=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }else{
            window.statusBarColor = Color.WHITE
        }

        edeitText_Email_sign_in.addTextChangedListener(this)
        edeitText_Password_sign_in.addTextChangedListener(this)



        btn_sign_in.setOnClickListener {
            val email = edeitText_Email_sign_in.text.toString()
            val password = edeitText_Password_sign_in.text.toString()
            if (email.isEmpty()){
                edeitText_Email_sign_in.error="Email Required"
                edeitText_Email_sign_in.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                edeitText_Email_sign_in.error="Please Enter a Valid Email"
                edeitText_Email_sign_in.requestFocus()
                return@setOnClickListener
            }

            if (password.length < 6){
                edeitText_Password_sign_in.error="6 Char Required"
                edeitText_Password_sign_in.requestFocus()
                return@setOnClickListener
            }
            sign_in(email,password)
        }


        btn_creat_account.setOnClickListener {
            val CreatNewAccount= Intent(this,Sign_Up_Activity::class.java)
            startActivity(CreatNewAccount)

        }
    }

    override fun onStart() {
        super.onStart()
        if (mAuth.currentUser?.uid != null){
            val intentMainActivity = Intent(this,MainActivity::class.java)
            startActivity(intentMainActivity)
        }
    }

    private fun sign_in(email: String, password: String) {

        progress_sign_in.visibility= View.VISIBLE

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->
            if (task.isSuccessful){

                FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { taskTwo ->
                    val token =taskTwo.result?.token
                    firestore.collection("users")
                        .document(FirebaseAuth.getInstance().currentUser!!.uid)
                        .update(mapOf("token" to token))
                }

                progress_sign_in.visibility= View.INVISIBLE


                val intentMainActivity = Intent(this,MainActivity::class.java)
                intentMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intentMainActivity)
            }else{

                progress_sign_in.visibility= View.INVISIBLE

                Toast.makeText(this,task.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun afterTextChanged(s: Editable?) {

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        btn_sign_in.isEnabled=edeitText_Email_sign_in.text.toString().trim().isNotEmpty()
                && edeitText_Password_sign_in.text.toString().trim().isNotEmpty()
    }
}