package com.psychedelic9.bt4glasses.ui.adapter

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.psychedelic9.bt4glasses.R
import com.psychedelic9.bt4glasses.data.entity.BTResultEntity
import com.psychedelic9.bt4glasses.databinding.BtScanItemBinding
import com.psychedelic9.bt4glasses.util.ListUtils

class BTScanAdapter(context:Context,btAdapter: BluetoothAdapter):RecyclerView.Adapter<BTScanAdapter.ViewHolder>(){
    private val mContext = context
    private val mBTAdapter = btAdapter
    private var mList:ArrayList<BTResultEntity> = ArrayList()
    fun submitList(list: ArrayList<BTResultEntity>) {
        ListUtils.removeListDuplicate1(list)
        mList = list
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: BtScanItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mContext),
            R.layout.bt_scan_item,
            parent,
            false
        )
        val viewHolder = ViewHolder(binding.root)
        viewHolder.setBinding(binding)
        return viewHolder
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.getBinding().entity = mList[position]
        if (mList[position].iconRes!=null){
            holder.getBinding().btScanItemIvTypeIcon.setImageResource(mList[position].iconRes!!)
        }
        holder.getBinding().executePendingBindings()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var binding: BtScanItemBinding? = null
        fun getBinding(): BtScanItemBinding {
            return binding!!
        }

        fun setBinding(binding: BtScanItemBinding) {
            this.binding = binding
        }
    }
}