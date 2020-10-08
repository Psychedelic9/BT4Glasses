package com.psychedelic9.bt4glasses.ui.fragment

import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_FINISHED
import android.bluetooth.BluetoothAdapter.ACTION_DISCOVERY_STARTED
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothClass.Device.Major.*
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.SystemClock
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.psychedelic9.bt4glasses.R
import com.psychedelic9.bt4glasses.data.entity.BTResultEntity
import com.psychedelic9.bt4glasses.databinding.BtScanDialogBinding
import com.psychedelic9.bt4glasses.ui.adapter.BTScanAdapter
import com.psychedelic9.bt4glasses.util.SpaceItemDecorationVertical
import kotlin.concurrent.thread


class BTScanDialogFragment(btAdapter: BluetoothAdapter) : DialogFragment() {
    companion object {
        const val TAG = "BTScanDialogFragment"
    }

    private lateinit var mBinding: BtScanDialogBinding
    private val mBTAdapter = btAdapter
    private lateinit var mBTScanAdapter: BTScanAdapter
    private lateinit var mBTBondedAdapter:BTScanAdapter
    private var discoveryStartTime = 0L
    private val mFoundDeviceList = ArrayList<BTResultEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, 0)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val dm = DisplayMetrics()
            activity!!.windowManager.defaultDisplay.getMetrics(dm)
            val attributes: WindowManager.LayoutParams = dialog.window!!.attributes
            attributes.gravity = Gravity.TOP //对齐方式
            attributes.x = 0
            attributes.y = 90//具体头部距离
            attributes.width = WindowManager.LayoutParams.MATCH_PARENT
            attributes.height = WindowManager.LayoutParams.MATCH_PARENT
            attributes.dimAmount = 0f//背后UI不变暗
            mBinding.root.setBackgroundColor(resources.getColor(R.color.bg_color))
            dialog.window!!.attributes = attributes
            dialog.window!!.setLayout(attributes.width, attributes.height)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.bt_scan_dialog, container, false)

        mBTScanAdapter = BTScanAdapter(requireContext(), mBTAdapter)
        mBTBondedAdapter = BTScanAdapter(requireContext(), mBTAdapter)
        mBinding.btScanRvResult.apply {
            adapter = mBTScanAdapter
            layoutManager = object : LinearLayoutManager(requireContext()) {
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
            addItemDecoration(SpaceItemDecorationVertical(25))
        }
        mBinding.btScanRvBonded.apply {
            adapter = mBTBondedAdapter
            layoutManager = object : LinearLayoutManager(requireContext()) {
                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
            addItemDecoration(SpaceItemDecorationVertical(25))
        }



        mBinding.btScanCancel.setOnClickListener {
            if (mBTAdapter.isDiscovering){
                mBTAdapter.cancelDiscovery()
            }
            dismiss()
        }
        return mBinding.root
    }

    override fun onResume() {
        super.onResume()
        val mIntentFilter = IntentFilter()
        mIntentFilter.addAction("ACTION_FOUND")
        mIntentFilter.addAction("ACTION_DISCOVERY_STARTED")
        mIntentFilter.addAction("ACTION_DISCOVERY_FINISHED")
        mIntentFilter.addAction(BluetoothDevice.ACTION_FOUND)
        mIntentFilter.addAction(ACTION_DISCOVERY_STARTED)
        mIntentFilter.addAction(ACTION_DISCOVERY_FINISHED)
        requireActivity().registerReceiver(mIntentReceiver, mIntentFilter)
        startScan()

    }

    private fun startScan(){
        discoveryStartTime = SystemClock.uptimeMillis()
        mBTAdapter.startDiscovery()
        mBinding.btScanTvTitle.text = "蓝牙搜索中"
        mBinding.btScanPbProgress.visibility = View.VISIBLE


        val pairedDevices: Set<BluetoothDevice> =
            mBTAdapter.bondedDevices

        if (pairedDevices.isNotEmpty()) {
            val bondedDevices = ArrayList<BTResultEntity>()
            for (device in pairedDevices) {
                // 把名字和地址取出来添加到适配器中
                when (device.bluetoothClass.deviceClass) {
                    COMPUTER -> {
                        bondedDevices.add(BTResultEntity(R.drawable.compute_device,device.name,null,device.address,device.uuids))
                    }
                    PHONE -> {
                        bondedDevices.add(BTResultEntity(R.drawable.phone_device,device.name,null,device.address,device.uuids))
                    }
                    AUDIO_VIDEO -> {
                        bondedDevices.add(BTResultEntity(R.drawable.music_device,device.name,null,device.address,device.uuids))
                    }
                    WEARABLE -> {
                        bondedDevices.add(BTResultEntity(R.drawable.wear_device,device.name,null,device.address,device.uuids))
                    }
                    else -> {
                        bondedDevices.add(BTResultEntity(R.drawable.bluetooth_device,device.name,null,device.address,device.uuids))
                    }
                }
            }
            if (bondedDevices.isNotEmpty()){
                mBTBondedAdapter.submitList(bondedDevices)
            }
        }

        thread {
            Thread.sleep(500)
            Log.d(TAG,"isDiscovering ${mBTAdapter.isDiscovering}")
            while (mBTAdapter.isDiscovering){
                Thread.sleep(1000)
            }
            requireActivity().runOnUiThread {
                mBinding.btScanTvTitle.text = "蓝牙搜索完毕"
                mBinding.btScanPbProgress.visibility = View.GONE
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mFoundDeviceList.clear()
        requireActivity().unregisterReceiver(mIntentReceiver)
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private val mIntentReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        // Abstract class
        override fun onReceive(context: Context?, receivedIntent: Intent) {
            Log.v(TAG, "Entered intentReceiver")
            when {
                BluetoothDevice.ACTION_FOUND == receivedIntent.action -> {
                    Log.v(
                        TAG,
                        "Found after=" + (SystemClock.uptimeMillis() - discoveryStartTime)
                    )
                    val foundDevice =
                        receivedIntent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    Log.v(TAG, foundDevice?.name + " " + foundDevice?.address + " was found")
                    val foundDeviceClass: BluetoothClass =
                        receivedIntent.getParcelableExtra(BluetoothDevice.EXTRA_CLASS)!!
                    Log.v(TAG, "BT class: ${foundDeviceClass.majorDeviceClass} $foundDeviceClass")
                    when (foundDeviceClass.deviceClass) {
                        COMPUTER -> {
                            mFoundDeviceList.add(BTResultEntity(R.drawable.compute_device,foundDevice?.name,null,foundDevice?.address,foundDevice?.uuids))
                        }
                        PHONE -> {
                            mFoundDeviceList.add(BTResultEntity(R.drawable.phone_device,foundDevice?.name,null,foundDevice?.address,foundDevice?.uuids))
                        }
                        AUDIO_VIDEO -> {
                            mFoundDeviceList.add(BTResultEntity(R.drawable.music_device,foundDevice?.name,null,foundDevice?.address,foundDevice?.uuids))
                        }
                        WEARABLE -> {
                            mFoundDeviceList.add(BTResultEntity(R.drawable.wear_device,foundDevice?.name,null,foundDevice?.address,foundDevice?.uuids))
                        }
                        else -> {
                            mFoundDeviceList.add(BTResultEntity(R.drawable.bluetooth_device,foundDevice?.name,null,foundDevice?.address,foundDevice?.uuids))
                        }
                    }
                    mBTScanAdapter.submitList(mFoundDeviceList)
                }
                ACTION_DISCOVERY_STARTED == receivedIntent.action -> {
                    discoveryStartTime = SystemClock.uptimeMillis()
                }
                ACTION_DISCOVERY_FINISHED == receivedIntent.action -> {
                    Log.v(
                        TAG,
                        "Discovery lasted: " + ((SystemClock.uptimeMillis() - discoveryStartTime).toString() + "ms start time=" + discoveryStartTime)
                    )
                }
            }
            Log.v(TAG, "intentReceiver finished")
        } //END onReceive
    } //END BroadcastReceiver

}