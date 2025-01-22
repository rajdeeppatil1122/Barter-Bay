package com.barterbay.app

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.barterbay.app.databinding.ActivityTestBinding
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.core.Tag
import com.google.firebase.database.database
import com.google.firebase.database.getValue

class TestActivity : AppCompatActivity() {
    private var binding : ActivityTestBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        // Write a message to the database
        val database = Firebase.database                  // points to the database, not the reference of the DB
        val databaseReference = database.reference        // we get the topmost reference (of the DB)
        val myRef = database.getReference("key2")    // key = key2  // Gets a DatabaseReference for the provided path.
        val myRef2 = database.getReference("key4")

        myRef.setValue("Kishmish")  // value = Kishmish
            .addOnSuccessListener {
                Log.d("Message", "Data written successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Message", "Failed to write data", e)
            }
        Log.d("Message", "I am in post listing activity")

        myRef2.setValue("The Barter Bay")

        // Reading the data
//        myRef.addValueEventListener(object : ValueEventListener{    // this method, continuously keeps reading the value, and when changes, do some actions, for either 1 or all references
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val value = snapshot.value //(value) is from getValue method. ORIGINALLY --> snapshot.getValue<String>()
//                Toast.makeText(this@TestActivity, "$value", Toast.LENGTH_SHORT).show()
//            }
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//        })

        // Let's understand how to fetch values of all nodes.
//        databaseReference.addValueEventListener(object: ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//                for (childSnapshot in snapshot.children){
//                    Toast.makeText(this@TestActivity, childSnapshot.getValue<String>(), Toast.LENGTH_LONG).show()
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//        })

        // Fetching only a specific value of a key, only when something is pressed, or done or triggered. We can write the below code anywhere. If we simply put it outside, it will automatically execute, just like addValueEventChangeListener, but it will not continuously track for the change, it will execute just for once.:
        binding!!.buttonTestActivity.setOnClickListener{
            databaseReference.child("key3").addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(binding!!.textViewTestActivity.text == "Key1 Value is: My Laddu"){
                        binding!!.textViewTestActivity.text = "Are bass na! Ek baar press kiya kaam khatam, aur kitna karoge? Chalg bhag yaha se."
                    }
                    else if(binding!!.textViewTestActivity.text == "Are bass na! Ek baar press kiya kaam khatam, aur kitna karoge? Chalg bhag yaha se."){
                        binding!!.textViewTestActivity.text = "Are Ek baar bola na, samaz nahi ata? Pagal! Jao chup chap"
                    }
                    else if(binding!!.textViewTestActivity.text == "Are Ek baar bola na, samaz nahi ata? Pagal! Jao chup chap"){
                        binding!!.textViewTestActivity.text = "Are Ek baar bola na, samaz nahi ata? Pagal! Jao chup chap"
                    }
                    else {
                        binding!!.textViewTestActivity.text = "Key1 Value is: ${snapshot.getValue<String>()}"   // here, we could have directly concatenated $snapshot, but for a better practice, we are converting the snapshot data into 'String' and then using it.
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }

    }
}