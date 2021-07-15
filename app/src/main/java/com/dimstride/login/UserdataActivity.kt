package com.dimstride.login

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.dimstride.login.databinding.ActivityUserdataBinding
import com.dimstride.login.models.UserDataFirebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserdataActivity : AppCompatActivity() {

    private val TAG = "APK1-UserdataActivity"
    private lateinit var v: ActivityUserdataBinding
    var userData: UserDataFirebase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        v = ActivityUserdataBinding.inflate(layoutInflater)
        setContentView(v.root)

        userData = intent.extras?.getSerializable("userData") as? UserDataFirebase ?: UserDataFirebase()

        initViews()
    }

    private fun initViews() {
        v.editNameEt.setText(userData?.name)
        v.editLastnameEt.setText(userData?.lastname)
        v.editProfessionEt.setText(userData?.profession)
        userData?.age?.let { v.editAgeEt.setText(userData?.age.toString()) }

        v.editAcceptBt.setOnClickListener {
            if (v.editNameEt.text.isNotEmpty()) {
                userData?.name = v.editNameEt.text.toString()
            }
            if (v.editLastnameEt.text.isNotEmpty()) {
                userData?.lastname = v.editLastnameEt.text.toString()
            }
            if (v.editProfessionEt.text.isNotEmpty()) {
                userData?.profession = v.editProfessionEt.text.toString()
            }
            if (v.editAgeEt.text.isNotEmpty()) {
                userData?.age = v.editAgeEt.text.toString().toInt()
            }
            editFirestore(userData)
            finish()
        }
    }

    private fun editFirestore(userData: UserDataFirebase?) {
        val db = Firebase.firestore
        val id = Firebase.auth.currentUser?.uid
        if (userData != null && id != null) {
            db.collection("users").document(id)
                .set(userData)
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
        }
    }
}
