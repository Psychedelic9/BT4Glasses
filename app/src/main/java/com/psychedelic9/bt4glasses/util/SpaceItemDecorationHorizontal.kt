package com.psychedelic9.bt4glasses.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration


/**
 *@Author: yiqing
 *@CreateDate: 2020/8/6 10:21
 *@UpdateDate: 2020/8/6 10:21
 *@Description:
 *@ClassName: SpaceItemDecoration
 */
class SpaceItemDecorationHorizontal(space: Int) : ItemDecoration() {
    private var mSpace = space

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        //获得当前item的位置
        val position = parent.getChildAdapterPosition(view)
        if (position == 0){
            outRect.right = mSpace
        }else{
            outRect.left = mSpace
            outRect.right = mSpace
        }

    }

}