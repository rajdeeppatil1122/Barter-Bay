package com.barterbay.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.barterbay.app.databinding.ActivityPostListing7Binding
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.google.firebase.storage.storage

class PostListingActivity7 : AppCompatActivity() {
    private var binding: ActivityPostListing7Binding? = null
    private var selectedUri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostListing7Binding.inflate(layoutInflater)
        setContentView(binding?.root)

        val firebaseDB = Firebase.database  // this is Database
        val firebaseStorage = Firebase.storage  // this is Storage
        val firebaseDBReference = firebaseDB.getReference()   // this is Database Reference
        val firebaseStorageReference = firebaseStorage.getReference()   // this is Storage Reference
        binding!!.activityPostListingButton.setOnClickListener {
            val intent = Intent()
            intent.setType("image/*")
            intent.setAction(Intent.ACTION_GET_CONTENT)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

            startActivityForResult(Intent.createChooser(intent, "Select Pictures"), 101)

        }

        val productFirebaseRef = firebaseDB.getReference("productsByCategory")

        // Create a new product
        val productID = productFirebaseRef.push().key
        Toast.makeText(this, productID.toString(), Toast.LENGTH_SHORT).show()
        val newProduct = mapOf(
            "name" to "Math Textbook",
            "description" to "A detailed algebra guide.",
            "condition" to "Like New",
            "price" to 300,
            "barterValue" to "Laptop or ₹300",
            "ownerId" to "user123",
            "location" to "XYZ College",
            "dateListed" to System.currentTimeMillis(),
            "imageUrls" to listOf("https://.../image1.jpg", "https://.../image2.jpg")
        )

        binding!!.acativityPostListingSaveButton.setOnClickListener{
//            // Save product
//            newProduct?.let {       // Cleaner Code: if(newProduct != null)  --> It is just for tackling with the NullPointerException, that's it.
//                productFirebaseRef.child("books").child("$productID").setValue(newProduct)
//            }

            // save to storage
            val natureRef = firebaseStorageReference.child("nature")    // saves single file in root level
            val natureImageRef = firebaseStorageReference.child("images/nature.jpg")    // saves single file in folder called 'images', name is same as of previous.
            Toast.makeText(this, "$selectedUri", Toast.LENGTH_SHORT).show()
            selectedUri?.let { it1 ->
                natureRef.putFile(it1)

                    .addOnSuccessListener {
                    Toast.makeText(this, "Uploaded", Toast.LENGTH_SHORT).show()
                    }

                    .addOnFailureListener { exception ->
                        android.util.Log.e("Firebase Upload", "Error uploading file", exception)
                        Toast.makeText(this, "Upload Failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }


    }

    @Deprecated(
        "This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.",
        ReplaceWith(
            "super.onActivityResult(requestCode, resultCode, data)",
            "androidx.appcompat.app.AppCompatActivity"
        )
    )
    @Override
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val selectedImageUri: Uri? = data?.data    //(from getData()) It works even if you don't mention the type 'Uri?'
        val clipData = data?.clipData

        if (resultCode == RESULT_OK && requestCode == 101) {
            if (selectedImageUri != null) {   // if it contains only 1 file
//                selectedUri = selectedImageUri
                Toast.makeText(this, "$selectedImageUri", Toast.LENGTH_SHORT).show()
                Log.d("URI MESSAGE", selectedImageUri.toString())
                binding!!.activityPostListingImageView.setImageURI(selectedImageUri)
            }
            else if (clipData != null) {  // if it contains multiple files
                for (i in 0 until clipData.itemCount) {     // i in 0 means, i starts from 0
                    var uri = clipData.getItemAt(i).uri

                    if (i == 0) {
                        binding!!.activityPostListingImageView.setImageURI(uri)
                        Toast.makeText(this, "$uri", Toast.LENGTH_SHORT).show()
                        selectedUri = uri
                    }
                    if (i == 1) {
                        binding!!.activityPostListingImageView2.setImageURI(uri)
                    }
                }
            }
        }
    }
}