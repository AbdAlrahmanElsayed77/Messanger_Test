package com.example.contact

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.contact.Model.User
import com.example.contact.fragments.ChatFragment
import com.example.contact.fragments.DiscoverFragment
import com.example.contact.fragments.PeopleFragment
import com.example.contact.glide.GlideApp
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() , BottomNavigationView.OnNavigationItemSelectedListener{

    private val mAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    var storageInstance:FirebaseStorage?=null

    private val firestoreInstance:FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val mChatFragment = ChatFragment()
    private val mPeopleFragment = PeopleFragment()
    private val mDiscoverFragment = DiscoverFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        storageInstance= FirebaseStorage.getInstance()

        firestoreInstance.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .get()
            .addOnSuccessListener {
                val user = it.toObject(User::class.java)
                if (user!!.profileImage.isNotEmpty()){
                    GlideApp.with(this)
                        .load(
                            storageInstance!!.
                            getReference(user.profileImage))
                        .into(circleImageView_profile_image)
                }else{
                    circleImageView_profile_image.setImageResource(R.drawable.ic_account_circle)
                }
            }


        setSupportActionBar(toolbar_main)
        supportActionBar?.title=""


        // status Bar Color

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            window.decorView.systemUiVisibility=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }else{
            window.statusBarColor = Color.WHITE
        }


        bottomNavigationView_main.setOnNavigationItemSelectedListener(this)
        setFragment(mChatFragment)




    }

    override fun onNavigationItemSelected(item: MenuItem): kotlin.Boolean {
        when(item.itemId) {
            R.id.navigation_chat_item ->{
                setFragment(mChatFragment)
                return true
            }
            R.id.navigation_people_item ->{
                setFragment(mPeopleFragment)
                return true
            }
            R.id.navigation_more_item ->{
                setFragment(mDiscoverFragment)
                return true
            }
            else -> return false
        }
    }

    private fun setFragment(fragment:Fragment){
        val fr = supportFragmentManager.beginTransaction()
        fr.replace(R.id.coordinatorLayout_main_content,fragment)
        fr.commit()
    }


}




