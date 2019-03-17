[TOC]
# SignInAndSingUp
一个使用kotlin基于androidx编写的登陆注册的界面  
- 功能：
  - 支持账号密码登录
  - 集成QQ微信快捷登录
- 使用 
  - QQ登录
    - 需要在[AndroidManifest](./app/src/main/AndroidManifest.xml)修改为你的开放平台获取的APPID
    - 在[SignActivity](./java/com/mml/signinandup/SignActivity.kt)修改APP_ID
  - 微信 
    - 修改[SignActivity](./java/com/mml/signinandup/SignActivity.kt)里的WX_APP_ID
# 界面展示
  - 登录</br>
    <img src="./ScreenShots/550E0653351B921F78FB1A863AACB793.jpg" width = 30% height = 30% /> 
  - 注册</br>
    <img src="./ScreenShots/768564FED6147E74E95B5861C6E3E659.jpg" width = 30% height = 30% />  
  - 动图</br>
    ![动图](./ScreenShots/D8F0F9CB28DBFF450A181C9CF06A3596.gif)

# 试用（demo下载）
  - [demoAPK](https://www.pgyer.com/mrdf)

# bug fix
- 修复在魅族手机系统出现Attempt to invoke virtual method 'int android.text.Layout.getLineForOffset(int)' on a null object reference错误
TextInputEditText改为EditText












