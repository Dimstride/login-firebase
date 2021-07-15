package com.dimstride.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.dimstride.login.models.UserDataFirebase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private val TAG = "APK1-MainActivity"
    private val RC_SIGN_IN = 76254782
    private lateinit var auth: FirebaseAuth
    private var userData: UserDataFirebase? = null
    private var googleSignInClient: GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Testing Git
        initGoogle()
        initViews()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.editData -> {
                startUserdataActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initViews() {
        val mainLoginGoogle = findViewById<Button>(R.id.mainLoginGoogle)
        val mainLogoutGoogle = findViewById<Button>(R.id.mainLogoutGoogle)
        val mainRegisterBt = findViewById<Button>(R.id.mainRegisterBt)
        val loginBt = findViewById<Button>(R.id.loginBt)
        val signupBt = findViewById<Button>(R.id.signupBt)
        val backToLoginBt = findViewById<Button>(R.id.backToLoginBt)

        val loginLayout = findViewById<LinearLayout>(R.id.loginLayout)
        val loginEmail = findViewById<EditText>(R.id.loginEmail)
        val loginPass = findViewById<EditText>(R.id.loginPass)

        val signupLayout = findViewById<LinearLayout>(R.id.signupLayout)
        val signupEmail = findViewById<EditText>(R.id.signupEmail)
        val signupPass1 = findViewById<EditText>(R.id.signupPass1)
        val signupPass2 = findViewById<EditText>(R.id.signupPass2)

        mainLoginGoogle.setOnClickListener {
            signIn()
        }

        mainLogoutGoogle.setOnClickListener {
            Firebase.auth.signOut()
            updateUI(auth.currentUser)
        }

        mainRegisterBt.setOnClickListener {
            loginLayout.isVisible = false
            signupLayout.isVisible = true
            mainRegisterBt.isVisible = false
        }

        backToLoginBt.setOnClickListener {
            loginLayout.isVisible = true
            signupLayout.isVisible = false
            mainRegisterBt.isVisible = true
        }

        loginBt.setOnClickListener {
            if (loginEmail.text.isNotEmpty() && loginPass.text.count() >= 6) {
                Log.d(TAG, "Loging In")
                logInWithEmail(loginEmail.text.toString(), loginPass.text.toString())
            } else {
                Log.d(TAG, "Wrong credentials")
            }
        }

        signupBt.setOnClickListener {
            if (signupEmail.text.isNotEmpty() && signupPass1.text.isNotEmpty() && signupPass2.text.isNotEmpty()) {
                if (signupPass1.text.toString() == signupPass2.text.toString()) {
                    Log.d(TAG, "Signing Up")
                    signUpWithEmail(signupEmail.text.toString(), signupPass1.text.toString())

                } else {
                    Log.d(TAG, "Password doesn't mach")
                }
            } else {
                Log.d(TAG, "Wrong credentials")
            }
        }
    }

    private fun startUserdataActivity() {
        startActivity(Intent(this, UserdataActivity::class.java).putExtra("userData", userData))
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun initGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        auth = Firebase.auth
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        val mainLoginGoogle = findViewById<Button>(R.id.mainLoginGoogle)
        val mainLogoutGoogle = findViewById<Button>(R.id.mainLogoutGoogle)
        val mainRegisterBt = findViewById<Button>(R.id.mainRegisterBt)
        val mainWelcome = findViewById<TextView>(R.id.mainWelcome)
        val mainDataTv = findViewById<TextView>(R.id.mainDataTv)
        val loginLayout = findViewById<LinearLayout>(R.id.loginLayout)
        val signupLayout = findViewById<LinearLayout>(R.id.signupLayout)

        Log.i(TAG, "email: " + user?.email.toString())
        Log.i(TAG, "displayName: " + user?.displayName.toString())

        readUserData()

        if (user == null) {
            loginLayout.visibility = View.VISIBLE
            signupLayout.visibility = View.GONE
            mainLoginGoogle.visibility = View.VISIBLE
            mainLogoutGoogle.visibility = View.GONE
            mainRegisterBt.visibility = View.VISIBLE
            mainDataTv.visibility = View.GONE
            mainWelcome.visibility = View.GONE
        } else {
            loginLayout.visibility = View.GONE
            signupLayout.visibility = View.GONE
            mainLoginGoogle.visibility = View.GONE
            mainLogoutGoogle.visibility = View.VISIBLE
            mainRegisterBt.visibility = View.GONE
            mainDataTv.visibility = View.VISIBLE
            mainWelcome.visibility = View.VISIBLE
            mainWelcome.text = user.email
        }
    }

    private fun signUpWithEmail(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                }
            }
    }

    private fun logInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                }
            }
    }

    private fun readUserData() {
        val db = Firebase.firestore

        auth.currentUser?.let {
            db.collection("users").document(it.uid)
                .get()
                .addOnSuccessListener { result ->
                    Log.d(TAG, "${result.id} => ${result.data}")
                    userData = result.toObject(UserDataFirebase::class.java)
                    fillUserData(userData)
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents.", exception)
                }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fillUserData(userData: UserDataFirebase?) {
        val mainDataTv = findViewById<TextView>(R.id.mainDataTv)

        if (userData == null) {
            mainDataTv.text = ""
            startUserdataActivity()
        } else {
            mainDataTv.text = "${userData.name ?: "Username not found"} ${userData.lastname ?: ""}\n" +
                    "Profession: ${userData.profession ?: "Unknown"}\n" +
                    "Age: ${userData.age ?: "Unknown"}"
        }
    }
}