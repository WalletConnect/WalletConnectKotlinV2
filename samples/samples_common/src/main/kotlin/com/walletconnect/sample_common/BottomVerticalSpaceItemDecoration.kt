package com.walletconnect.sample_common

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class BottomVerticalSpaceItemDecoration(private val marginBottom: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.getChildAdapterPosition(view) != parent.adapter?.itemCount?.minus(1)) {
            outRect.bottom = marginBottom
        }
    }
}