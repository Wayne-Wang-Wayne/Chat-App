package com.example.chatapp.api


import com.example.chatapp.util.AESUtil
import com.set.app.entertainment.api.APIManager



object APIService {

    private var isNeedEncrypt = true




    private fun <T> getApi(domain: String, cls: Class<T>): T {
        return APIManager.getRetrofit(domain)
            .create(cls)
    }

    private fun getEncryptHeader(
        data: String?, key: String?, iv: String?): HashMap<String, String> {
        val headers = HashMap<String, String>()
        headers[APIHeader.CONTENT_TYPE] = APIHeader.APPLICATION_JSON
        headers[APIHeader.ACCEPT] = APIHeader.APPLICATION_JSON
        headers[APIHeader.ACCEPT_CHARSET] = APIHeader.UTF8
        if (isNeedEncrypt) headers["Authorization"] = "Bearer ${
            AESUtil.encrypt(data, key, iv)
        }"
        return headers
    }
}
