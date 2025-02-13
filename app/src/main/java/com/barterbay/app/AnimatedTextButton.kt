package com.barterbay.app

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat

class AnimatedTextButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = android.R.attr.buttonStyle
) : AppCompatButton(context, attrs, defStyleAttr) {

    init {
        // Set default appearance
        gravity = Gravity.CENTER
        setPadding(20, 0, 20, 0)
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.black)) // Change as needed
        setTextColor(ContextCompat.getColor(context, android.R.color.white))
        textSize = 20f
        text = "Select Category"
    }

    // Method to change text with fade animation
    fun updateTextWithAnimation(newText: String) {
        if (text.toString() == newText) return // Prevent redundant animations

        val fadeOut = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f)
        fadeOut.duration = 100
        fadeOut.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                text = newText
                ObjectAnimator.ofFloat(this@AnimatedTextButton, "alpha", 0f, 1f).apply {
                    duration = 200
                    start()
                }
            }
        })
        fadeOut.start()
    }
}
