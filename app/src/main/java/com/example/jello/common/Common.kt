package com.example.jello.common

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.jello.R
import com.mmstq.progressbargifdialog.ProgressBarGIFDialog
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.experimental.and


class Common {
    companion object{

        fun showDialog(activity: Activity,loadingGif:Int,doneGif:Int,titleLoading:String,titleDone:String):ProgressBarGIFDialog.Builder{
            val progressDialog = ProgressBarGIFDialog.Builder(activity)
            progressDialog.setCancelable(false)
                .setTitleColor(R.color.black)
                .setLoadingGif(loadingGif)
                .setDoneGif(doneGif)
                .setDoneTitle(titleDone)
                .setLoadingTitle(titleLoading)
            return progressDialog
        }

        fun dismissDialog(progressBarGIFDialog: ProgressBarGIFDialog.Builder){
            progressBarGIFDialog.clear()
        }

        fun validItem(editText: EditText, textViewError: TextView, textError:String, condition:Boolean):Boolean{
            return if (editText.text.isEmpty() || !condition){
                textViewError.visibility = View.VISIBLE
                textViewError.text = textError
                editText.setBackgroundResource(R.drawable.text_field_error)
                false
            }else{
                textViewError.visibility = View.GONE
                editText.setBackgroundResource(R.drawable.text_field)
                true
            }
        }

        fun validItem(editText: EditText,txtError: TextView,textError: String):Boolean{
            return if (editText.text.isEmpty()){
                txtError.visibility = View.VISIBLE
                txtError.text = textError
                editText.setBackgroundResource(R.drawable.text_field_error)
                false
            }else{
                txtError.visibility = View.GONE
                editText.setBackgroundResource(R.drawable.text_field)
                true
            }
        }

        fun encryptPassword(password:String):String{
            var encryptedPassword = ""
            try {
                val m: MessageDigest = MessageDigest.getInstance("MD5")
                m.update(password.toByteArray())
                val bytes: ByteArray = m.digest()
                // The bytes array has bytes in decimal form. Converting it into hexadecimal format.
                val s = StringBuilder()
                for (i in bytes.indices) {
                    s.append(((bytes[i] and 0xff.toByte()) + 0x100).toString(16).substring(1))
                }
                encryptedPassword = s.toString()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
            return encryptedPassword
        }

        fun alertUser(editText: EditText, txtError1:TextView, txt:String){
            txtError1.visibility = View.VISIBLE
            txtError1.text = txt
            editText.setBackgroundResource(R.drawable.text_field_error)
        }

        fun alertUser(editText: EditText):Boolean{
            return if(editText.text.isEmpty()){
                editText.setBackgroundResource(R.drawable.text_field_error)
                false
            }else{
                true
            }
        }

        fun checkNetwork(activity: Activity): Boolean {
            var isConnected = false
            val connectivityManager = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val networkCapabilities = connectivityManager.activeNetwork ?: return false
                val activeNetwork = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
                isConnected = when {
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    else -> false
                }
            } else {
                connectivityManager.run {
                    activeNetworkInfo?.run {
                        isConnected = when (type) {
                            ConnectivityManager.TYPE_WIFI -> true
                            ConnectivityManager.TYPE_MOBILE -> true
                            else -> false
                        }
                    }
                }
            }
            return isConnected
        }

        fun hideSwipeRefresh(swipeRefresh : SwipeRefreshLayout){
            if (swipeRefresh.isRefreshing){
                swipeRefresh.isRefreshing = false
            }
        }

        fun toastErrorNetwork(activity: Activity){
            Toast.makeText(activity, "Make sure the internet is connected", Toast.LENGTH_LONG).show()
        }


    }
}