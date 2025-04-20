package com.barterbay.app

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
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

class CategoryAdapter(private var categoryList: List<CategoryModel>, private val listener: OnCategorySelectedListener) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>(), Filterable {

    private var filteredList: List<CategoryModel> = categoryList
    private val firebaseStorageRef = Firebase.storage.reference
    private var selectedPosition: Int = RecyclerView.NO_POSITION // Track selected item
    // Add this getter method (modify your CategoryAdapter to expose the selected position)
    fun getSelectedPosition(): Int = selectedPosition

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.categoryName)
        val lottieAnimation: LottieAnimationView = itemView.findViewById(R.id.categoryAnimation)
        val cardView: CardView = itemView.findViewById(R.id.cardViewOfItemCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = filteredList[position]
        holder.categoryName.text = category.name

        // Load Lottie animation with Firebase caching logic
        loadLottieAnimation(holder.lottieAnimation, category.lottieFile, holder.itemView.context)

        // Check if the current position is selected, set color accordingly
        val isSelected = position == selectedPosition
        val startColor = if (isSelected)
            ContextCompat.getColor(holder.itemView.context, R.color.light_blue_inapp)
        else
            ContextCompat.getColor(holder.itemView.context, R.color.more_light_blue_inapp)

        holder.cardView.setCardBackgroundColor(startColor)

        // **Apply bottom margin only for the last row**
        val layoutParams = holder.cardView.layoutParams as ViewGroup.MarginLayoutParams
        if (position == filteredList.size - 1 || position == filteredList.size - 2 || position == filteredList.size - 3) {
            layoutParams.bottomMargin = 150 // Set 40dp margin for last row
        } else {
            layoutParams.bottomMargin = 0 // Reset margin for other items
        }
        holder.cardView.layoutParams = layoutParams

        holder.cardView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = if (selectedPosition == position) {
                RecyclerView.NO_POSITION
            } else {
                position
            }

            // Animate color change
            animateColorChange(holder.cardView, startColor,
                if (selectedPosition == position) {
                    if(position == categoryList.size - 1) {
                        showCustomCategoryDialog(holder.itemView.context, position, holder)
                    }
                    ContextCompat.getColor(holder.itemView.context, R.color.light_blue_inapp)
                } else {
                    ContextCompat.getColor(holder.itemView.context, R.color.more_light_blue_inapp)
                }
            )

            // Notify fragment
            listener.onCategorySelected(
                selectedPosition != RecyclerView.NO_POSITION,
                if (selectedPosition != RecyclerView.NO_POSITION) category.name else "", category.lottieFile
            )

            // Reset previous selection if needed
            if (previousPosition != RecyclerView.NO_POSITION && previousPosition != selectedPosition) {
                notifyItemChanged(previousPosition)
            }
        }

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

    // Function to smoothly animate color transition
    private fun animateColorChange(view: CardView, fromColor: Int, toColor: Int) {
        val animator = ObjectAnimator.ofArgb(view, "cardBackgroundColor", fromColor, toColor)
        animator.duration = 200
        animator.start()
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

        }
        else {  // this will rarely(never) execute now
            try {
            context.toast("We are in ELSE")
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

    fun setSelectedPosition(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        if (previousPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousPosition)
        }
        if (position != RecyclerView.NO_POSITION) {
            notifyItemChanged(position)
        }
    }

     // Create an extension function for toast:
    fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT){
        Toast.makeText(this, message, duration).show()
    }

    interface OnCategorySelectedListener {
        fun onCategorySelected(isSelected: Boolean, categoryName: String, categoryLottie: String)  // Notify Fragment when selection changes
    }

    private fun showCustomCategoryDialog(context: Context, position: Int, holder: CategoryViewHolder) {
        var categoryNameClass : String? = null
        var categoryLottieClass : String? = ""  // IT SHOULD NOT BE NULL
        val bottomSheet = CustomCategoryBottomSheet { categoryName ->
            // Handle the submitted category name
            // context.toast("Category Submitted: $categoryName")
            categoryNameClass = categoryName
        }

        bottomSheet.onDismissListener = {
            Log.d("DialogEvent", "BottomSheet dismissed in RecyclerView!")
            Toast.makeText(context, "BottomSheet dismissed!", Toast.LENGTH_SHORT).show()
            // Notify fragment about selection change

            if(categoryNameClass==null) {
                // To toggle colors and set the toggle-logic intact/consistent for future toggles, if we don't this, it will cause toggle problems (reverse problems) in the future toggle operations, hence it was necessary to update selectedPosition for CORRECT future toggle operations.
                selectedPosition =
                    if (selectedPosition == position) RecyclerView.NO_POSITION else position // Toggle selection

                // Animate color change for the clicked item
                animateColorChange(
                    holder.cardView,
                    ContextCompat.getColor(holder.itemView.context, R.color.light_blue_inapp),
                    if (selectedPosition == position) {
                        ContextCompat.getColor(holder.itemView.context, R.color.light_blue_inapp)
                    } else {
                        ContextCompat.getColor(holder.itemView.context, R.color.light_blue_inapp)
                    }
                )

                // To dismiss button
                listener.onCategorySelected(false, "", "")
            }
            else{
                listener.onCategorySelected(selectedPosition != RecyclerView.NO_POSITION, categoryNameClass!!, categoryLottieClass!!)

            }

        }

        bottomSheet.show((context as AppCompatActivity).supportFragmentManager, "CustomCategoryBottomSheet")
    }



}
