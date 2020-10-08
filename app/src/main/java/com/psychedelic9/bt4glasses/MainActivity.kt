package com.psychedelic9.bt4glasses

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.psychedelic9.bt4glasses.ui.fragment.BTScanDialogFragment


class MainActivity : AppCompatActivity() {
    companion object{
        const val TAG = "MainActivity"
        const val START_BT_SETTING = 0
    }
    private lateinit var mBluetoothAdapter:BluetoothAdapter
    private val mContext = this
    private var mIsBTOpen = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mBluetoothAdapter = getBtAdapter()!!
        openBT()
        startBTScan()
    }

    private fun startBTScan(){
        BTScanDialogFragment(mBluetoothAdapter).show(supportFragmentManager,"")

    }

    private fun startBLEScan(){
        //TODO:
    }

    private fun openBT(){
        if (!mBluetoothAdapter.isEnabled){
            val enable = mBluetoothAdapter.enable()
            if (!enable){
                mIsBTOpen = false
                Toast.makeText(mContext,"自动开启蓝牙失败，请手动打开！",Toast.LENGTH_LONG).show()
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(intent, START_BT_SETTING)

            }else{
                Toast.makeText(mContext,"已自动开启蓝牙！",Toast.LENGTH_LONG).show()
                mIsBTOpen = true
            }
        }else{
            mIsBTOpen = true
        }
    }


    private fun getBtAdapter(): BluetoothAdapter? {
        return BluetoothAdapter.getDefaultAdapter()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(resultCode){
            START_BT_SETTING->{
                when(resultCode){
                    Activity.RESULT_OK->{

                    }
                    Activity.RESULT_CANCELED->{

                    }
                }
            }
        }
    }



}