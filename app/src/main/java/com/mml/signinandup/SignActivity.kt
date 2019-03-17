package com.mml.signinandup

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.percentlayout.widget.PercentRelativeLayout
import com.coder.zzq.smartshow.dialog.SmartDialog
import com.coder.zzq.smartshow.dialog.creator.type.impl.DialogCreatorFactory
import com.coder.zzq.smartshow.toast.SmartToast
import com.google.gson.JsonObject
import com.tencent.connect.UserInfo
import com.tencent.connect.auth.QQToken
import com.tencent.connect.common.Constants
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import kotlinx.android.synthetic.main.activity_sign.*
import kotlinx.android.synthetic.main.layout_signin.*
import kotlinx.android.synthetic.main.layout_signup.*
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory


class SignActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        const val TYPE_SIGNUP: Int = 1
        const val TYPE_SIGNIN: Int = 2
        const val TAG = "SignActivity.Kt"
    }

    private var isSignInScreen = true//默认登陆界面
    private var mAuthTask: UserTask? = null
    private var mSingInDialog: SmartDialog? = null
    //QQ
    private val QQ_APP_ID = "你的APPID"
    // 微信登录
    private lateinit var WXapi: IWXAPI
    private val WX_APP_ID = "创建应用后得到的APP_ID"
    private lateinit var mTencent: Tencent
    private lateinit var mUserInfo: UserInfo
    private lateinit var mQQIUiListener: QQIUiListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign)
        //传入参数APPID和全局Context上下文
        mTencent = Tencent.createInstance(QQ_APP_ID, this.applicationContext)
        initView()
        showSignInForm()
        SmartToast.info("welcome")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            Constants.REQUEST_LOGIN -> {
                Tencent.onActivityResultData(requestCode, resultCode, data, mQQIUiListener)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun initView() {
        llSignIn!!.setOnClickListener(this)
        llSignUp!!.setOnClickListener(this)
        tvSignUpInvoker!!.setOnClickListener {
            isSignInScreen = false
            showSignUpForm()
        }

        tvSignInInvoker!!.setOnClickListener {
            isSignInScreen = true
            showSignInForm()
        }

        btnSignUp!!.setOnClickListener {
            val clockwise = AnimationUtils.loadAnimation(applicationContext, R.anim.login_ui_rotate_right_to_left)
            if (!isSignInScreen) {
                btnSignUp!!.startAnimation(clockwise)
                attemptSignUp()
            }
        }
        btnSignIn!!.setOnClickListener {
            attemptSignIn()
        }
        iv_sign_in_by_qq.setOnClickListener {
            SmartToast.info("正在拉起QQ...")
            mQQIUiListener = QQIUiListener()
            mTencent.login(this, "all", mQQIUiListener)
        }
        iv_sign_in_by_wechat.setOnClickListener {
            SmartToast.info("正在拉起微信...")
            WXapi = WXAPIFactory.createWXAPI(this, WX_APP_ID, true)
            if(WXapi.isWXAppInstalled) {
                WXapi.registerApp(WX_APP_ID)
                val req: SendAuth.Req = SendAuth.Req()
                req.scope = "snsapi_userinfo"//req.scope = "snsapi_userinfo,snsapi_friend,snsapi_message,snsapi_contact"

                req.state = "wechat_sdk_demo"
                WXapi.sendReq(req)
            }else{
                SmartToast.fail("未安装微信客服端！")
            }
        }
    }

    private fun attemptSignIn() {
        // Reset errors.
        et_signin_username.error = null
        et_signin_password.error = null

        // Store values at the time of the login attempt.
        val username = et_signin_username.text.toString()
        val password = et_signin_password.text.toString()
        if (BuildConfig.DEBUG) {
            SmartToast.info("$username:$password")
        }
        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            et_signin_password.error = getString(R.string.error_invalid_password)
            focusView = et_signin_password
            cancel = true
        }

        // Check for a valid username address.
        if (TextUtils.isEmpty(username)) {
            et_signin_username.error = getString(R.string.error_empty_username)
            focusView = et_signin_username
            cancel = true
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            // showProgress(true)
            if (mSingInDialog == null) {
                mSingInDialog = SmartDialog.newInstance(
                    DialogCreatorFactory
                        .loading()
                        .large()
                        .message("登录中...")
                ).reuse(true)
            }
            mSingInDialog!!.show(this)
            mAuthTask = UserTask(username, password)
            mAuthTask!!.execute(TYPE_SIGNIN)
        }

    }

    private fun attemptSignUp() {
        // Reset errors.
        et_signup_username.error = null
        et_signup_password.error = null

        // Store values at the time of the login attempt.
        val username = et_signup_password.text.toString()
        val password = et_signup_password.text.toString()
        var mobile = et_signup_mobile.text.toString()
        if (BuildConfig.DEBUG) {
            SmartToast.info("$username:$password")
        }
        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            et_signin_password.error = getString(R.string.error_invalid_password)
            focusView = et_signin_password
            cancel = true
        }

        // Check for a valid username address.
        if (TextUtils.isEmpty(username)) {
            et_signup_username.error = getString(R.string.error_empty_username)
            focusView = et_signup_username
            cancel = true
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            // showProgress(true)
            if (mSingInDialog == null) {
                mSingInDialog = SmartDialog.newInstance(
                    DialogCreatorFactory
                        .loading()
                        .large()
                        .message("注册中...")
                ).reuse(true)
            }
            mSingInDialog!!.show(this)
            mAuthTask = UserTask(username, password, mobile)
            mAuthTask!!.execute(TYPE_SIGNUP)
        }
    }

    private fun showSignUpForm() {
        val paramsLogin = llSignIn!!.layoutParams as PercentRelativeLayout.LayoutParams
        val infoLogin = paramsLogin.percentLayoutInfo
        infoLogin.widthPercent = 0.15f
        llSignIn!!.requestLayout()
        et_signup_username.error = null
        et_signup_password.error = null
        llSignUp.requestFocus()
        var va = llcBottomView!!.layoutParams as PercentRelativeLayout.LayoutParams
        va.percentLayoutInfo.leftMarginPercent = 0.15f
        va.percentLayoutInfo.rightMarginPercent = 0f
        val paramsSignUp = llSignUp!!.layoutParams as PercentRelativeLayout.LayoutParams
        val infoSignUp = paramsSignUp.percentLayoutInfo
        infoSignUp.widthPercent = 0.85f
        llSignUp!!.requestLayout()

        tvSignUpInvoker!!.visibility = View.GONE
        tvSignInInvoker!!.visibility = View.VISIBLE
        val translate = AnimationUtils.loadAnimation(applicationContext, R.anim.login_ui_translate_right_to_left)
        llSignUp!!.startAnimation(translate)
        tvSignUpInvoker!!.visibility = View.GONE
        tvSignInInvoker!!.visibility = View.VISIBLE
        var llcat = AnimationUtils.loadAnimation(applicationContext, R.anim.login_ui_translate_left_right)
        llcBottomView.startAnimation(llcat)
        val clockwise = AnimationUtils.loadAnimation(applicationContext, R.anim.login_ui_rotate_right_to_left)
        btnSignUp!!.startAnimation(clockwise)

    }

    private fun showSignInForm() {
        val paramsLogin = llSignIn!!.layoutParams as PercentRelativeLayout.LayoutParams
        val infoLogin = paramsLogin.percentLayoutInfo
        infoLogin.widthPercent = 0.85f
        llSignIn!!.requestLayout()
        et_signin_username.error = null
        et_signin_password.error = null
        llSignIn.requestFocus()
        var va = llcBottomView!!.layoutParams as PercentRelativeLayout.LayoutParams
        va.percentLayoutInfo.rightMarginPercent = 0.15f
        va.percentLayoutInfo.leftMarginPercent = 0f
        val paramsSignUp = llSignUp!!.layoutParams as PercentRelativeLayout.LayoutParams
        val infoSignUp = paramsSignUp.percentLayoutInfo
        infoSignUp.widthPercent = 0.15f
        llSignUp!!.requestLayout()

        val translate = AnimationUtils.loadAnimation(applicationContext, R.anim.login_ui_translate_left_to_right)
        llSignIn!!.startAnimation(translate)

        tvSignUpInvoker!!.visibility = View.VISIBLE
        tvSignInInvoker!!.visibility = View.GONE
        var llcat = AnimationUtils.loadAnimation(applicationContext, R.anim.login_ui_translate_right_left)
        llcBottomView.startAnimation(llcat)
        val clockwise = AnimationUtils.loadAnimation(applicationContext, R.anim.login_ui_rotate_left_to_right)
        btnSignIn!!.startAnimation(clockwise)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.llSignIn || v.id == R.id.llSignUp) {
            val methodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            methodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)

        }

    }

    inner class UserTask constructor(private var mUserName: String, private var mPassword: String) :
        AsyncTask<Int, Int, Int>() {
        private var mMobile: String = ""

        //        注册构造函数
        constructor(mUserName: String, mPassword: String, mMobile: String) : this(mUserName, mPassword) {
            this.mMobile = mMobile
        }

        override fun doInBackground(vararg params: Int?): Int? {
            // TODO: attempt authentication against a network service.
            // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            //params是一个Int数组
            var result = 0
            when (params[0]) {
                TYPE_SIGNIN -> {
                    Thread.sleep(1000)
                    result = 1
                }
                TYPE_SIGNUP -> {
                    Thread.sleep(1000)
                    result = 2
                }
            }
            return result
        }

        override fun onPostExecute(result: Int?) {
//            super.onPostExecute(result)
            when (result) {
                1 -> {
                    mSingInDialog!!.dismiss(this@SignActivity)
                    SmartToast.success("登陆成功！")
                }
                2 -> {
                    mSingInDialog!!.dismiss(this@SignActivity)
                    SmartToast.success("注册成功！")
                }
            }
        }

        override fun onCancelled() {
            mAuthTask = null
            mSingInDialog!!.dismiss(this@SignActivity)
            SmartToast.fail("取消登陆！")
//            super.onCancelled()
        }
    }

    inner class QQIUiListener : IUiListener {
        override fun onComplete(response: Any?) {
            SmartToast.success("授权成功！")
            Log.i(TAG, response.toString())
            val jObject = response as JsonObject
            val openID = jObject.get("openid") as String
            val accessToken = jObject.get("access_token") as String
            val expires = jObject.get("expires_in") as String
            mTencent.openId = openID
            mTencent.setAccessToken(accessToken, expires)
            val qqToken: QQToken = mTencent.qqToken
            mUserInfo = UserInfo(application, qqToken)
            mUserInfo.getUserInfo(object : IUiListener {
                override fun onComplete(p0: Any?) {
                    Log.i(TAG, "登录成功" + p0.toString())
                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onCancel() {
                    Log.i(TAG, "登录取消")
                    // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onError(p0: UiError?) {
                    Log.i(TAG, "登录失败" + p0.toString())
                    // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

            })

        }

        override fun onCancel() {
            SmartToast.fail("授权取消")
        }

        override fun onError(p0: UiError?) {
            SmartToast.error("授权失败，${p0.toString()}")
            //To change body of created functions use File | Settings | File Templates.
        }

    }
}
