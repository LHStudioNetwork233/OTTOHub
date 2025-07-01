### 欢迎使用 OTTOHub！

我们拥有的功能

> 1.更方便的操作
> 2.更美丽的界面
> 3.更强大的功能
> 4.支持本地操作

网站来源于 [@天才儿童爱丽丝](https://b23.tv/2Bw77D5 "@天才儿童爱丽丝")

app由 [@Hiro](https://b23.tv/ZrlhtGn "@Hiro") 开发

在线求个关注(

[官方网站](https://m.ottohub.cn/ "官方网站")

[APP内置图床地址](https://img.api.aa1.cn/ "APP内置图床地址")

------------

## 使用须知

[基本使用须知](https://m.ottohub.cn/b/9513 "基本使用须知")

** APP端补充通知 **

1. 禁止以任何形式反编译本软件，任何因反编译本软件造成的崩溃闪退等问题，作者拒绝接受反馈

2. 该APP源代码已在github上开源，但请不要大肆传播该源码

3. 不许以任何形式对该软件进行二改，请尊重劳动成果

4. 本应用鼓励分享，但请不要在无关区域轻易乱发

其他以基本使用须知为准

------------

## 本机存储

** 存档文件地址: **

- Android 10- : /sdcard/OTTOHub/...

- Android 10+ : /sdcard/Android/data/com.losthiro.ottohubclient/files/...

Android 10+可能会读取错误，因为系统限制了用户权限

** 其他文件地址: **

该类文件统一在存档文件目录下

- 应用配置: /config/...

- 视频草稿: /draft/...

- 本地视频: /save/...

- 崩溃日志: /xxx.log

** 配置文件明细 **

- content_cache.json: 存储私信内容，当输入UID时自动读取

- history_search.json: 存储搜索记录

- rng\_danmaku_config.json: 存储默认弹幕以及输入框文本，按随机数提取

** 本地视频格式 **

[重要]视频存储目录一定要以OV开头，否则无法读取

1. 视频配置
    - mainfest.json: 存放视频基本信息，如简介等
    
    - danmaku_config.json: 存放视频弹幕信息
    
2. 视频文件(封面和头像统一以图片形式打开)
    - cover: 封面文件
    
    - user_avatar: 作者头像
    
    - video: 视频文件

------------

## 项目构建

构建使用gradle

_(作者在Android 10上使用了aide构建)_

_(纯手机操作，效果不好请见谅qwq)_

在`build.gradle`中添加所需依赖

```gradle
dependencies {
    implementation 'com.vladsch.flexmark:flexmark-all:0.64.8'
    implementation 'androidx.drawerlayout:drawerlayout:1.0.0'
    implementation 'androidx.recyclerview:recyclerview:1.0.0'
    implementation 'androidx.appcompat:appcompat:1.0.0'
}
```

另外使用的弹幕渲染库

[渲染库地址](https://github.com/bilibili/DanmakuFlameMaster/ "渲染库地址")

------------

## 支持作者

支持本项目就多多星标吧

欢迎提出修改意见

目前还存在一些问题

欢迎推荐给身边的动物园朋友

也可以赞助网站开发

_(APP就算了，开发者是个高中生qwq)_

[捐赠点这里](https://afdian.tv/a/ottohub "捐赠点这里")

[开源地址](https://github.com/LHStudioNetwork233/OTTOHub "开源地址")

[外部网盘下载](https://www.123pan.com/s/fqQojv-ohcJH.html "外部网盘下载")
