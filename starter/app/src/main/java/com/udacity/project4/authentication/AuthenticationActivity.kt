package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity


/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    private var isAuthonticated: Boolean = false
    private lateinit var binding: com.udacity.project4.databinding.ActivityAuthenticationBinding
    private val TAG = "AuthenticationActivity"
    private val viewModel by viewModels<LoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
        binding =
            com.udacity.project4.databinding.ActivityAuthenticationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnLogin.setOnClickListener {
            performAccountAuth()
        }

        viewModel.authenticationState.observe(this, Observer { authenticationState ->
            when (authenticationState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> {

                    isAuthonticated = true
                    FirebaseAuth.getInstance().currentUser?.let {
                        it.displayName?.let { it1 -> Log.e(TAG, "Welcome :$it1") }
                        it.email?.let { it1 -> binding.tvWelcomeLoginMessage.text = it1 }
                    }
                    startActivity(
                        Intent(
                            this@AuthenticationActivity,
                            RemindersActivity::class.java
                        )
                    )
                    finish()
                }
                else -> {
                    isAuthonticated = false
                    binding.tvWelcomeLoginMessage.text =
                        getString(R.string.welcome_to_the_location_reminder_app)
                    Log.e(
                        TAG,
                        "Authentication state that doesn't require any UI change $authenticationState"
                    )
                }
            }
        })

    }

    private fun performAccountAuth() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setLogo(R.drawable.img_location_banner)
            .setAvailableProviders(providers)
            .setTheme(R.style.AppTheme)
            .build()
        signInLauncher.launch(signInIntent)
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result: FirebaseAuthUIAuthenticationResult? ->

    }


}
