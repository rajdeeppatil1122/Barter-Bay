package com.barterbay.app
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.view.animation.AlphaAnimation
import android.widget.Button
import android.widget.EditText
import androidx.cardview.widget.CardView
import com.barterbay.app.R

class CustomCategoryDialog(
    context: Context,
    private val onCategorySubmitted: (String) -> Unit
) : Dialog(context, R.style.TransparentDialog) {  // 🔥 Set transparent theme here

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Remove default dialog window border
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(LayoutInflater.from(context).inflate(R.layout.custom_category_dialog, null))

        // Make sure the background is completely transparent
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val categoryEditText = findViewById<EditText>(R.id.categoryEditText)
        val submitButton = findViewById<Button>(R.id.submitButton)
        val dialogCard = findViewById<CardView>(R.id.cardViewDialogBox)

        // Fade-in animation when dialog appears
        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 500
            fillAfter = true
        }
        dialogCard.startAnimation(fadeIn)

        submitButton.setOnClickListener {
            val categoryText = categoryEditText.text.toString().trim()
            if (categoryText.isNotEmpty()) {
                // Fade-out animation before closing
                val fadeOut = AlphaAnimation(1f, 0f).apply {
                    duration = 500
                    fillAfter = true
                }
                dialogCard.startAnimation(fadeOut)

                // Dismiss after fade out
                dialogCard.postDelayed({
                    dismiss()
                    onCategorySubmitted(categoryText)
                }, 500)
            } 
        }
    }
}
