package com.barterbay.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity4 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // bindings
        val bottomNavigationView : BottomNavigationView = findViewById(R.id.bottomNavBar)

        loadFragment(HomeFragment1())

        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.home) {
                loadFragment(HomeFragment1())
                true
            }
            else if (menuItem.itemId == R.id.chat) {
                loadFragment(ChatActivityListFragment2())
                true
            }
            else if (menuItem.itemId == R.id.notification) {
                loadFragment(NotificationFragment3())
                true
            }
            else if (menuItem.itemId == R.id.profile){
                loadFragment(ProfileFragment4())
                true
            }
            else if (menuItem.itemId == R.id.postListing){
                val intent = Intent(this, PostListingActivity7::class.java)
                startActivity(intent)
//                    Intent i = new Intent(getApplicationContext(), ActivityTwo.class);  (in java)
                true
            }
            else{
                false
            }
        }

        // load all the lotttie .json files
        // Download JSON files when the app starts
        downloadAllLottieFiles(this)

    }

    private fun loadFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.linearLayoutMainActivity, fragment).commit()
    }

    fun downloadAllLottieFiles(context: Context) {
        val storageRef = Firebase.storage.reference.child("ProductCategory")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // List all files in Firebase Storage under "ProductCategory"
                val result = storageRef.listAll().await()

                for (fileRef in result.items) {
                    val lottieFileName = fileRef.name // e.g., "electronics.json"
                    val directory = File(context.filesDir, "ProductCategory")
                    if (!directory.exists()) {
                        directory.mkdirs()
                    }

                    val localFile = File(directory, lottieFileName)

                    if (!localFile.exists()) {  // Only download if not already present
                        fileRef.getFile(localFile).await()
                        Log.d("LottieDownload", "Downloaded: $lottieFileName")
                    } else {
                        Log.d("LottieDownload", "Already exists: $lottieFileName")
                    }
                }

                withContext(Dispatchers.Main) {
                    Log.d("LottieDownload", "All Lottie animations downloaded!")
                }
            } catch (e: Exception) {
                context.toast("Error downloading files: ${e.message}")
            }
        }
    }

    // Create an extension function for toast:
    fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, message, duration).show()
    }

}