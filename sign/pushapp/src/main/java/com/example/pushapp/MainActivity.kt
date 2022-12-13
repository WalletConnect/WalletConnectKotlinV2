package com.example.pushapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.example.pushapp.databinding.ActivityMainBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*A notification to the device is send via Postman*/

        var token: String? = null

        Log.e("kobe", "INIT activity")

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("kobe", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            token = task.result

            // Log and toast
            Log.d("kobe", "Token: $token")
        })
    }
}

inline fun <T : ViewBinding> AppCompatActivity.viewBinding(crossinline bindingInflater: (LayoutInflater) -> T): Lazy<T> =
    lazy(LazyThreadSafetyMode.NONE) {
        bindingInflater(layoutInflater)
    }