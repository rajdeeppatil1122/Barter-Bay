package com.barterbay.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

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

    }

    private fun loadFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.linearLayoutMainActivity, fragment).commit()
    }
}