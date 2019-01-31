package com.allen.kotlinapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.DrawableRes
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View

/**
 * 文 件 名: DividerGridItemDecoration
 * 创 建 人: Allen
 * 创建日期: 2017/6/12 17:16
 * 修改时间：
 * 修改备注：
 */


/**
 * RecyclerView的GridLayoutManager和StaggeredGridLayoutManager的Item分割线
 */
class DividerGridItemDecoration : RecyclerView.ItemDecoration {
    private var mDivider: Drawable? = null

    constructor(context: Context) {
        val typedArray = context.obtainStyledAttributes(ATTRS)
        mDivider = typedArray.getDrawable(0)
        typedArray.recycle()
    }

    constructor(context: Context, @DrawableRes resId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mDivider = context.getDrawable(resId)
        } else {
            mDivider = context.resources.getDrawable(resId)
        }
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        drawHorizontal(canvas, parent)
        drawVertical(canvas, parent)
    }

    private fun getSpanCount(parent: RecyclerView): Int { // 列数
        var spanCount = -1
        val layoutManager = parent.layoutManager
        if (layoutManager is GridLayoutManager) {
            spanCount = layoutManager.spanCount
        } else if (layoutManager is StaggeredGridLayoutManager) {
            spanCount = layoutManager.spanCount
        }
        return spanCount
    }

    private fun drawHorizontal(c: Canvas, parent: RecyclerView) {
        val childCount = parent.childCount
        for (i in 0..childCount - 1) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val left = child.left - params.leftMargin
            val right = child.right + params.rightMargin + mDivider!!.intrinsicWidth
            val top = child.bottom + params.bottomMargin
            val bottom = top + mDivider!!.intrinsicHeight
            mDivider!!.setBounds(left, top, right, bottom)
            mDivider!!.draw(c)
        }
    }

    private fun drawVertical(c: Canvas, parent: RecyclerView) {
        val childCount = parent.childCount
        for (i in 0..childCount - 1) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.top - params.topMargin
            val bottom = child.bottom + params.bottomMargin
            val left = child.right + params.rightMargin
            val right = left + mDivider!!.intrinsicWidth

            mDivider!!.setBounds(left, top, right, bottom)
            mDivider!!.draw(c)
        }
    }

    private fun isLastColum(parent: RecyclerView, pos: Int, spanCount: Int, childCount: Int): Boolean {
        var childCount = childCount
        val layoutManager = parent.layoutManager
        if (layoutManager is GridLayoutManager) {
            if ((pos + 1) % spanCount == 0) { // 如果是最后一列，则不需要绘制右边
                return true
            }
        } else if (layoutManager is StaggeredGridLayoutManager) {
            val orientation = layoutManager.orientation
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                if ((pos + 1) % spanCount == 0) { // 如果是最后一列，则不需要绘制右边
                    return true
                }
            } else {
                childCount = childCount - childCount % spanCount
                if (pos >= childCount)
                // 如果是最后一列，则不需要绘制右边
                    return true
            }
        }
        return false
    }

    private fun isLastRaw(parent: RecyclerView, pos: Int, spanCount: Int, childCount: Int): Boolean {
        var childCount = childCount
        val layoutManager = parent.layoutManager
        if (layoutManager is GridLayoutManager) {
            childCount = childCount - childCount % spanCount
            if (pos >= childCount)
            // 如果是最后一行，则不需要绘制底部
                return true
        } else if (layoutManager is StaggeredGridLayoutManager) {
            val orientation = layoutManager.orientation // StaggeredGridLayoutManager 且纵向滚动
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                childCount = childCount - childCount % spanCount // 如果是最后一行，则不需要绘制底部
                if (pos >= childCount) {
                    return true
                }
            } else { // StaggeredGridLayoutManager 且横向滚动
                if ((pos + 1) % spanCount == 0) { // 如果是最后一行，则不需要绘制底部
                    return true
                }
            }
        }
        return false
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val spanCount = getSpanCount(parent)
        val childCount = parent.adapter?.itemCount?:0
        val itemPosition = (view.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition
        if (isLastRaw(parent, itemPosition, spanCount, childCount)) { // 如果是最后一行，则不需要绘制底部
            outRect.set(0, 0, mDivider!!.intrinsicWidth, 0)
        } else if (isLastColum(parent, itemPosition, spanCount, childCount)) { // 如果是最后一列，则不需要绘制右边
            outRect.set(0, 0, 0, mDivider!!.intrinsicHeight)
        } else {
            outRect.set(0, 0, mDivider!!.intrinsicWidth, mDivider!!.intrinsicHeight)
        }
    }

    companion object {

        private val ATTRS = intArrayOf(android.R.attr.listDivider)
    }

}
