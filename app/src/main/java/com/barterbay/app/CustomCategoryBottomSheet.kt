package com.barterbay.app

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CustomCategoryBottomSheet(private val onCategorySubmitted: (String) -> Unit) : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_custom_category, null)
        dialog.setContentView(view)

        val categoryEditText = view.findViewById<EditText>(R.id.categoryEditText)
        val submitButton = view.findViewById<Button>(R.id.submitButton)

        submitButton.setOnClickListener {
            val categoryName = categoryEditText.text.toString().trim()
            if (categoryName.isNotEmpty()) {
                onCategorySubmitted(categoryName)
                dismiss() // Close the bottom sheet
            } else {
                categoryEditText.error = "Please enter a category"
            }
        }

        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setWindowAnimations(R.style.DialogAnimation)
    }

    var onDismissListener: (() -> Unit)? = null  // Custom listener

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()  // Notify dismissal
    }

}
