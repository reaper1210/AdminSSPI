package com.swamisamarthpet.adminsspi

import android.app.Activity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class FCMNotificationSender(
    private var userFcmToken: String?,
    var title: String?,
    var body: String?,
    var mActivity: Activity?
) {
    private val postUrl = "https://fcm.googleapis.com/fcm/send"
    private var fcmServerKey = ""
    val db = FirebaseFirestore.getInstance()

    fun sendNotifications() {
        db.collection("admin").document("key").get().addOnSuccessListener {
            fcmServerKey = it["key"].toString()
        }
        val requestQueue = Volley.newRequestQueue(mActivity)
        val mainObj = JSONObject()
        try {
            mainObj.put("to", userFcmToken)
            val notiObject = JSONObject()
            notiObject.put("title", title)
            notiObject.put("body", body)
            notiObject.put("icon", "ic_launcher.webp")
            mainObj.put("notification", notiObject)

            val request: JsonObjectRequest = object : JsonObjectRequest(
                Request.Method.POST, postUrl,
                mainObj,
                Response.Listener{},
                Response.ErrorListener {}){

                override fun getHeaders(): MutableMap<String, String> {

                    val header: MutableMap<String, String> = HashMap()
                    header["content-type"] = "application/json"
                    header["authorization"] = "key=$fcmServerKey"
                    return header

                }

            }
            requestQueue.add(request)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}