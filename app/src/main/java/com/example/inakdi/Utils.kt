package com.example.inakdi

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateFormat
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.util.*

object Utils {

    val categories = arrayOf(
        "Tidak Ramah",
        "Sedang",
        "Ramah"
    )

    val categoryIcons = arrayOf(
        R.drawable.sad,
        R.drawable.flat,
        R.drawable.smile
    )

    val categoryColors = arrayOf(
        R.color.red600,
        R.color.white700,
        R.color.green600
    )

    fun toast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun getTimestamp(): Long {
        return System.currentTimeMillis()
    }

    fun formatTimestampDate(timestamp: Long): String {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp
        return DateFormat.format("dd/MM/yyyy", calendar).toString()
    }

    fun getCategoryIcon(category: String): Int {
        val index = categories.indexOf(category)
        return if (index != -1) categoryIcons[index] else R.drawable.gambar
    }

    fun getCategoryColor(context: Context, category: String): Int {
        val index = categories.indexOf(category)
        return if (index != -1) ContextCompat.getColor(context, categoryColors[index]) else ContextCompat.getColor(context, R.color.white)
    }

    fun callIntent(context: Context, phone: String){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("tel: " + Uri.encode(phone)))
        context.startActivity(intent)
    }

    fun smsIntent(context: Context, phone: String){
        val intent = Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", Uri.encode(phone), null))
        context.startActivity(intent)
    }

    fun waIntent(context: Context, phone: String){
        val url = "https://api.whatsapp.com/send?phone=$phone"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        context.startActivity(intent)
    }

}
