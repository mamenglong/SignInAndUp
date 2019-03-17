package com.mml.signinandup.wxapi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.mml.lib.network.ServiceCreator
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.net.URLEncoder


/**
 * 项目名称：SignInAndUp
 * Created by Long on 2019/3/17.
 * 修改时间：2019/3/17 15:22
 */
class WXEntryActivity : Activity(), IWXAPIEventHandler {
    private var api: IWXAPI? = null
    private val resp: BaseResp? = null
    private val WX_APP_ID = "创建应用后得到的APP_ID"
    // 获取第一步的code后，请求以下链接获取access_token
    private var GetCodeRequest =
        "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code"
    // 获取用户个人信息
    private var GetUserInfo = "https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID"
    private val WX_APP_SECRET = "创建应用后得到的APP_SECRET"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        api = WXAPIFactory.createWXAPI(this, WX_APP_ID, false)
        api!!.handleIntent(intent, this)
    }

    // 微信发送请求到第三方应用时，会回调到该方法
    override fun onReq(req: BaseReq) {
        finish()
    }

    // 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
    override fun onResp(resp: BaseResp?) {
        var resp = resp
        var result = ""
        if (resp != null) {
            resp = resp
        }
        when (resp!!.errCode) {
            BaseResp.ErrCode.ERR_OK -> {
                result = "发送成功"
                Toast.makeText(this, result, Toast.LENGTH_LONG).show()
                val code = (resp as SendAuth.Resp).code

                /*
             * 将你前面得到的AppID、AppSecret、code，拼接成URL 获取access_token等等的信息(微信)
             */
                val get_access_token = getCodeRequest(code)
                ServiceCreator
                    .setBaseURL(get_access_token)
                    .setIsUseLoggingInterceptor(true)
                    .create(AccessTakenService::class.java)
                    .getAccessTaken().enqueue(object : Callback<Any>{
                        override fun onFailure(call: Call<Any>, t: Throwable) {
                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                        override fun onResponse(call: Call<Any>, response: Response<Any>) {
                            if (response.body().toString()!="") {
                               response as JSONObject
                                val access_token = response
                                    .getString("access_token")
                                val openid = response.getString("openid")
                                val get_user_info_url = getUserInfo(
                                    access_token, openid
                                )
                                getUserInfo(get_user_info_url)
                            }
                            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                        }

                    })

                finish()
            }
            BaseResp.ErrCode.ERR_USER_CANCEL -> {
                result = "发送取消"
                Toast.makeText(this, result, Toast.LENGTH_LONG).show()
                finish()
            }
            BaseResp.ErrCode.ERR_AUTH_DENIED -> {
                result = "发送被拒绝"
                Toast.makeText(this, result, Toast.LENGTH_LONG).show()
                finish()
            }
            else -> {
                result = "发送返回"
                Toast.makeText(this, result, Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    /**
     * 通过拼接的用户信息url获取用户信息
     *
     * @param user_info_url
     */
    private fun getUserInfo(user_info_url: String) {
        ServiceCreator
            .setBaseURL(user_info_url)
            .setIsUseLoggingInterceptor(true)
            .create(UserInfoService::class.java)
            .getUserInfo().enqueue(object : Callback<Any>{
                override fun onFailure(call: Call<Any>, t: Throwable) {
                    println("获取用户信息:$t")
                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onResponse(call: Call<Any>, response: Response<Any>) {
                    println("获取用户信息:$response")
                    if(response.body().toString()!="") {
                        response as JSONObject
                        val openid = response.getString("openid")
                        val nickname = response.getString("nickname")
                        val headimgurl = response.getString("headimgurl")
                    }
                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

            })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        api!!.handleIntent(intent, this)
        finish()
    }

    /**
     * 获取access_token的URL（微信）
     *
     * @param code
     * 授权时，微信回调给的
     * @return URL
     */
    private fun getCodeRequest(code: String): String {
        var result: String? = null
        GetCodeRequest = GetCodeRequest.replace(
            "APPID",
            urlEnodeUTF8(WX_APP_ID)
        )
        GetCodeRequest = GetCodeRequest.replace(
            "SECRET",
            urlEnodeUTF8(WX_APP_SECRET)
        )
        GetCodeRequest = GetCodeRequest.replace("CODE", urlEnodeUTF8(code))
        result = GetCodeRequest
        return result
    }

    /**
     * 获取用户个人信息的URL（微信）
     *
     * @param access_token
     * 获取access_token时给的
     * @param openid
     * 获取access_token时给的
     * @return URL
     */
    private fun getUserInfo(access_token: String, openid: String): String {
        var result: String? = null
        GetUserInfo = GetUserInfo.replace(
            "ACCESS_TOKEN",
            urlEnodeUTF8(access_token)
        )
        GetUserInfo = GetUserInfo.replace("OPENID", urlEnodeUTF8(openid))
        result = GetUserInfo
        return result
    }

    private fun urlEnodeUTF8(str: String): String {
        var result = str
        try {
            result = URLEncoder.encode(str, "UTF-8")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return result
    }
    interface UserInfoService {
        @GET
        fun getUserInfo(): Call<Any>
    }
    interface AccessTakenService{
        @POST
        fun getAccessTaken():Call<Any>
    }
}
