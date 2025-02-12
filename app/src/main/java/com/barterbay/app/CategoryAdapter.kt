package com.barterbay.app

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.time.Duration

class CategoryAdapter(private var categoryList: List<CategoryModel>) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>(), Filterable {

    private var filteredList: List<CategoryModel> = categoryList
    private val firebaseStorageRef = Firebase.storage.reference

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.categoryName)
        val lottieAnimation: LottieAnimationView = itemView.findViewById(R.id.categoryAnimation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = filteredList[position]
        holder.categoryName.text = category.name

        // Load Lottie animation with Firebase caching logic
        loadLottieAnimation(holder.lottieAnimation, category.lottieFile, holder.itemView.context)
    }

    override fun getItemCount(): Int = filteredList.size

    // Search filter logic
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase() ?: ""

                filteredList = if (query.isEmpty()) {
                    categoryList
                } else {
                    categoryList.filter { it.name.lowercase().contains(query) }
                }

                val results = FilterResults()
                results.values = filteredList
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as List<CategoryModel>
                notifyDataSetChanged()
            }
        }
    }

    // Function to check if the Lottie file exists locally; if not, download it
    private fun loadLottieAnimation(lottieView: LottieAnimationView, lottieFile: String, context: Context) {
        val directory = File(context.filesDir, "ProductCategory")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val localFile = File(directory, lottieFile)

        if (localFile.exists()) {
            try {
                // Load animation from local storage
                val jsonString = localFile.readText()
                lottieView.setAnimationFromJson(jsonString)
                lottieView.playAnimation()
//            context.toast("We are in IF")
            }catch (e: Exception){
                context.toast(e.toString())
            }

        } else {
            try {
//            context.toast("We are in ELSE")
                // Download from Firebase if not available locally
                val lottieAnimationRef = firebaseStorageRef.child("ProductCategory/$lottieFile")

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        lottieAnimationRef.getFile(localFile).await()
                        val jsonString = localFile.readText()

                        withContext(Dispatchers.Main) {
                            lottieView.setAnimationFromJson(jsonString)
                            lottieView.playAnimation()
                        }
                    } catch (e: Exception) {
                        Log.e("LottieDownload", "Failed to download: ${e.message}")
                    }
                }
            }catch (e: Exception){
                context.toast(e.toString())
            }
        }
    }

     // Create an extension function for toast:
    fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, message, duration).show()
    }
}
