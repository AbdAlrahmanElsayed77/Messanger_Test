package com.example.contact

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.contact.Model.User
import com.example.contact.glide.GlideApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.ByteArrayOutputStream
import java.util.*

class ProfileActivity : AppCompatActivity() {

    companion object{
        val RC_SELECT_IMAGE = 2
    }

    private val firestoreInstance:FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private lateinit var userName:String

    private val currentUserDocRef:DocumentReference
    get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid.toString()}")

private val storageInstance:FirebaseStorage by lazy {
    FirebaseStorage.getInstance()
}
    val currentUserStorageRef:StorageReference
    get() = storageInstance.reference.child(FirebaseAuth.getInstance().currentUser?.uid.toString())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            window.decorView.systemUiVisibility=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }else{
            window.statusBarColor = Color.WHITE
        }

        // sign out
        btn_sign_out.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this,Sign_in_Activity::class.java))
            finish()
        }



        setSupportActionBar(profile_toolbar)
        supportActionBar?.title="Me"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        getUserInfo {user->
           userName = user.name
            textView_user_name.text=user.name
            if (user.profileImage.isNotEmpty()){
                GlideApp.with(this)
                    .load(storageInstance.
                    getReference(user.profileImage))
                    .placeholder(R.drawable.ic_account_circle)
                    .into(circleImageView_profile_image)
            }
         }

        circleImageView_profile_image.setOnClickListener {
            val myIntentImage=Intent().apply {
                type="image/*"
                action=Intent.ACTION_GET_CONTENT
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg","image/png"))
            }
            startActivityForResult(Intent.createChooser(myIntentImage,"Select Image"),RC_SELECT_IMAGE)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==RC_SELECT_IMAGE && resultCode==Activity.RESULT_OK && data!=null && data.data!=null){
            circleImageView_profile_image.setImageURI(data.data)


             Progress_Profile.visibility=View.VISIBLE

            val selectedImagePath = data.data
            val selectedImageEmp = MediaStore.Images.Media.getBitmap(this.contentResolver,selectedImagePath)
            val outputStream= ByteArrayOutputStream()
            selectedImageEmp.compress(Bitmap.CompressFormat.JPEG,20,outputStream)
            val selectedImageBytes=outputStream.toByteArray()
            uploadpProfileImage(selectedImageBytes){ path ->
                val userFieldMap = mutableMapOf<String,Any>()
                userFieldMap["name"]=userName
                userFieldMap["profileImage"] = path
                currentUserDocRef.update(userFieldMap)
            }

        }
    }

    private fun uploadpProfileImage(selectedImageBytes: ByteArray , onSuccess:(imagePath:String) ->Unit) {
       val ref= currentUserStorageRef.child("ProfilePicture/${UUID.nameUUIDFromBytes(selectedImageBytes)}")
        ref.putBytes(selectedImageBytes).addOnCompleteListener{
            if (it.isSuccessful){
                onSuccess(ref.path)
                Progress_Profile.visibility=View.INVISIBLE
            }else{
                Toast.makeText(this,"Error : ${it.exception?.message.toString()}",Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home ->{
                finish()
                return true
            }
        }
        return false
    }

    private fun getUserInfo(onComplete:(User) -> Unit) {
        currentUserDocRef.get().addOnSuccessListener {
            onComplete(it.toObject(User::class.java)!!)
        }
    }

}