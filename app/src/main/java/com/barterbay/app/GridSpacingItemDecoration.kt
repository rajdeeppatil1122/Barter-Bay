package com.barterbay.app

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSpacingItemDecoration(private val verticalSpacing: Int, private val horizontalSpacing: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.left = horizontalSpacing
        outRect.right = horizontalSpacing
        outRect.top = verticalSpacing
        if (parent.getChildAdapterPosition(view) == state.itemCount - 1 || parent.getChildAdapterPosition(view) == state.itemCount - 2) {
            outRect.bottom = 0 // Here we say 0 because we have already given them the 150 bottom margin in categoryadapter
        } else {
            outRect.bottom = 0
        }
    }
}