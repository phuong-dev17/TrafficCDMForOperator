package org.deplide.application.android.trafficcdmforoperator.submission.submissionoverview

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import org.deplide.application.android.trafficcdmforoperator.R


/*
 * Credit:
 * The swiping left and right effect are inspired from below code:
 * https://gist.github.com/keinix/b1aa2417dbea9311a1207eddf8b9d47b
 * I understand the idea and functionality of each function.
 * However, I don't understand and don't care how the boundaries for icons and backgrounds
 * are calculated. I copied them blindly from the source code.
 */
class SubmissionOverviewSwipeCallback(
    private val adapter: SubmissionDetailRecyclerViewAdapter,
    context: Context) : ItemTouchHelper.Callback() {
    private val iconUndo = ContextCompat.getDrawable(context, R.drawable.undo_24dp)!!
    private val iconModify = ContextCompat.getDrawable(context, R.drawable.edit_24dp)!!
    private val backgroundUndo = GradientDrawable()
    private val backgroundModify = GradientDrawable()
    private val backgroundCornerOffset = 20 //so background is behind the rounded corners of itemView
    private val backgroundCornerRadius = 20.0f

    init {
        DrawableCompat.setTint(iconUndo, ContextCompat.getColor(context, R.color.white))
        DrawableCompat.setTint(iconModify, ContextCompat.getColor(context, R.color.white))

        backgroundUndo.cornerRadius = backgroundCornerRadius
        backgroundUndo.setColor(ContextCompat.getColor(context, R.color.warning))

        backgroundModify.cornerRadius = backgroundCornerRadius
        backgroundModify.setColor(ContextCompat.getColor(context, R.color.primary_dark))
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(
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

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

        val itemView = viewHolder.itemView

        val iconMargin: Int = (itemView.height - iconUndo.intrinsicHeight) / 2
        val iconTop: Int = itemView.top + (itemView.height - iconUndo.intrinsicHeight) / 2
        val iconBottom: Int = iconTop + iconUndo.intrinsicHeight

        if (dX > 0) { // Swiping to the right
            val iconLeft: Int = itemView.left + iconMargin
            val iconRight = itemView.left + iconMargin + iconModify.intrinsicWidth
            iconModify.setBounds(iconLeft, iconTop, iconRight, iconBottom)

            backgroundModify.setBounds(
                itemView.left, itemView.top,
                itemView.left + (dX.toInt()) - backgroundCornerOffset, itemView.bottom
            )

            backgroundModify.draw(c)
            iconModify.draw(c)
        } else if (dX < 0) { // Swiping to the left
            val iconLeft: Int = itemView.right - iconMargin - iconUndo.intrinsicWidth
            val iconRight = itemView.right - iconMargin
            iconUndo.setBounds(iconLeft, iconTop, iconRight, iconBottom)

            backgroundUndo.setBounds(
                itemView.right + (dX.toInt()) + backgroundCornerOffset,
                itemView.top, itemView.right, itemView.bottom
            )

            backgroundUndo.draw(c)
            iconUndo.draw(c)
        } else { // view is unSwiped
            iconModify.setBounds(0, 0, 0, 0)
            backgroundModify.setBounds(0, 0, 0, 0)
            iconUndo.setBounds(0, 0, 0, 0)
            backgroundUndo.setBounds(0, 0, 0, 0)
        }
    }
}