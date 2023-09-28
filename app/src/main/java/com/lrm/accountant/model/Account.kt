package com.lrm.accountant.model

import com.google.gson.annotations.SerializedName

data class Account(
    @SerializedName("ActID")
    val actId: Int,
    @SerializedName("ActName")
    val actName: String,
    val actmaintype: String,
    val actname1: String,
    val actunder: Int,
    val amount: Double,
    val dsdate: Any,
    val module: Any,
    val scrnname: Any
)