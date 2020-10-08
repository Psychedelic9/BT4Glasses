package com.psychedelic9.bt4glasses.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 *@Author: yiqing
 *@CreateDate: 2020/8/19 9:57
 *@UpdateDate: 2020/8/19 9:57
 *@Description:
 *@ClassName: SpaceItemDecorationVeritcal
 */
class SpaceItemDecorationVertical(space: Int) : RecyclerView.ItemDecoration() {
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
            outRect.top = 0
        }else{
            outRect.top = mSpace
        }

    }

}