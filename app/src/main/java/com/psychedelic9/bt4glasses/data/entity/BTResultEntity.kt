package com.psychedelic9.bt4glasses.data.entity

import android.os.ParcelUuid

data class BTResultEntity(
    var iconRes: Int? = null,
    var deviceName: String? = null,
    var deviceContent: String? = null,
    var deviceMac: String? = null,
    var UUID: Array<ParcelUuid>?=null
)