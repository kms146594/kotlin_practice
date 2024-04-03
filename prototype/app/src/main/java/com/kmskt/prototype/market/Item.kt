package com.kmskt.prototype.market

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Item(
    val Image: Int,
    val Title: String,
    val Price: String,
    val Location: String,
    val tag: String,
    val nickname: String,
    val chatCount: Int,
    val likeCount: Int
): Parcelable
