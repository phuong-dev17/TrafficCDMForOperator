package org.deplide.application.android.trafficcdmforoperator.submission.submissionoverview

import android.content.Context
import android.graphics.drawable.ColorDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import org.deplide.application.android.trafficcdmforoperator.R

class SubmissionOverviewSwipeCallback(
    private val adapter: SubmissionDetailRecyclerViewAdapter,
    private val context: Context) : ItemTouchHelper.Callback() {
    private val iconUndo = ContextCompat.getDrawable(context, R.drawable.undo_24dp)
    private val iconModify = ContextCompat.getDrawable(context, R.drawable.edit_24dp)
    private val backgroundUndo = ColorDrawable(ContextCompat.getColor(context, R.color.warning))
    private val backgroundModify = ColorDrawable(ContextCompat.getColor(context, R.color.primary_dark))

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return ItemTouchHelper.Callback.makeMovementFlags(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        )
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        if (direction == ItemTouchHelper.LEFT) {
            adapter.itemSwipedLeft(position)
        } else if (direction == ItemTouchHelper.RIGHT) {
            adapter.itemSwipedRight(position)
        }

    }

}