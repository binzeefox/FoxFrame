# FoxFrame 框架说明

## 概述

FoxFrame 是一个为方便中小型项目开发而遍写的一个开发框架。封装了RxJava、Retrofit和一些工具方法。同时为MVP框架的搭建提供便利。

由于算是学习过程中的积累产物，一不小心就写了不少东西，导致很多工具类没有很好的介绍，甚至根本没有介绍。我是不会承认因为自己懒得写，所以如果有兴趣可以看看源码，若发现问题相当欢迎练习作者进行指正。希望我早日成为Android大佬，日薪过万

QQ:864706521

## 快速接入

[![](https://jitpack.io/v/binzeefox/FoxFrame.svg)](https://jitpack.io/#binzeefox/FoxFrame)

在项目的 *app.gradle* 文件中的 dependencies 代码块中填写以下依赖
```gradle
    implementation 'com.github.binzeefox:FoxFrame:tag';  //Tag为最新版本号
```
并在项目的 *Project.Gradle* 里的 allprojects.repositories 里添加 ```maven { url "https://jitpack.io" }``` 。然后同步项目即可

## 开始使用

直接将 *Manifest* 文件中的 ```<Application>``` 标签 name 属性指向```com.binzeefox.foxframe.core.base.FoxApplication``` 或继承自该类的自定义Application即可

若项目本身有继承其它Application，可以在自定义Application 的```onCreate()```方法中调用```FoxCore.init(this);```即可。

注意!!，若跳过此步骤，大部分工具方法仍可以通过手动输入Context参数来调用，但有可能会导致奇怪的问题发生

## FoxCore

该类为核心类，需要应用使用继承自FoxApplication的Application或者调用```FoxCore.init(Application)```才能生效，该类包含几个工具方法。
通过该类，可以在应用任何位调用```FoxCore.get()```方法获取FoxCore唯一实例。同时可以用```FoxCore.getApplication()```在任何位置获取Application实例。

### 获取应用信息。
通过调用```FoxCore.get().getPackageInfo();```即可快速获取应用信息

### 获取版本名
通过调用```FoxCore.get().getVersionName();```即可获取当前应用的版本名称

### 获取版本号
通过调用```FoxCore.get().getVersionCode();```即可获取long 类型的版本号。

### 全局数据
该类提供了一个简易的全局数据保存方法。\
通过```FoxCore.get().putGlobalData(String key, Object data)``` 方法存入数据。\
该存入的数据可以在任何地方通过```FoxCore.get().getGlobalData(String key, T defaultValue)``` 方法获取\
通过 ```FoxCore.get().removeGlobalData(String key)``` 来删除数据。\
通过```FoxCore.get().submitGlobalData(String key, DataHolder.Callback callback)```订阅全局数据的变化情况\
该数据是由HashMap进行保存的，如果过度使用可能会导致OOM。建议仅用该方法保存较小的数据（例如token之类的常用值）

具体可参考```com.binzeefox.foxframe.core.tools.DataHolder.class```

### Activity 管理
可以通过```FoxCore.get().getTopActivity()```获取当前栈顶实例，或通过```FoxCore.get().popActivity()```进行弹栈并销毁栈顶Activity。

通过```FoxCore.get().killActivity(int count)``` 批量销毁Activity，参数为从栈顶数想销毁的Activity数量。

通过```FoxCore.get().getActivityList()```获取当前存活的所有Activity列表

## FoxActivity与FoxFragment

为方便开发，该框架提供了FoxActivity与FoxFragment作为基类（以下统称基类），并提供了一些常用方法。下面介绍它们的一些方法

### 继承

基类在各自的```onCreate()```与```onCreateView()```内进行了一些部署，并将这两个方法修饰为final。原本在该方法中进行的Layout绑定与部署将分别在三个独立的方法```onSetLayoutResource(int layoutId)```、```onSetLayoutView(View view)```与```create(Bundle savedInstance)```中进行。

layout资源文件请重写```onSetLayoutResource(int)```并返回layout资源id；layout实例请重写```onSetLayoutView(View)```并返回```View```实例。原本的初始化代码请于```create(Bundle)```中进行。

### 双击检测

基类提供了```checkCallAgain(long timeout, int id)```用于检验该方法在规定时间内是否被调用两次。其中第一个参数为两次点击的间隔。第二个参数为判断检测事件的id，默认为-1

示例如下
```java
/**
 * 利用 {@link FoxActivity#checkCallAgain(int)}   实现二次返回键退出功能
 */
@Override
public void onBackPressed() {
    if (checkCallAgain(2000)){
        // 上面的方法，第一次调用时返回false。参数接受一个长整型，代表超时时间，单位ms。
        // 在该时间内调用第二次，则返回true
        // 这里没有填写检测id，所以默认为-1
        System.exit(0);
    } else {
        NoticeUtil.get().showToast("再次点击返回键关闭应用");
    }
}
```
当第一次点击返回键时，会提示“再次点击返回键关闭应用”，第二次点击返回键时将直接关闭应用

### ViewHelper

基类提供了ViewHelper帮助处理页面交互。在继承基类后可通过```getViewHelper()```方法获取实例。

ViewHelper是一个抽象类，其常用方法如下
- ```String getStringById(int id)```: 通过id获取相应控件填充的文字（非TextView子类该方法返回null）
- ```double getDoubleById(int id)```: 通过id获取控件填充的数字（非TextView子类该方法抛出异常）
- ```int getIntegerById(int id)```: 通过id获取控件填充的数字（非TextView子类该方法抛出异常）
- ```void setErrorById(int id, CharSequence error)```: 通过id设置控件异常状态

其它方法请查阅```com.binzeefox.foxframe.core.tools.ViewHelper.java```\
~~才不是因为懒得写~~

### 跳转封装

基类通过```Navigator```类进行跳转。

示例代码：

```java
/**
 * 跳转方式
 */
private void navigateExample(){
    Intent intent;  //跳转intent
    intent = new Intent(this, SecondActivity.class);
    Bundle bundle;  //跳转参数
    bundle = new Bundle();

    // 显式跳转则直接修改intent相关内容
    navigate(intent)
        .setParams(bundle)  //若有参数，则添加参数
        .configIntent(intent1 -> {  //在跳转前对intent进行设置
            intent.putExtra("some extra", "extra value");
        })
//        .commitForResult(0x01)
        //若是请求跳转，则将下面的方法换成该方法
        .commit();  //最终跳转的方法。

    // 若需要上一页面跳转得到的数据，可以用下面的方法
    Bundle income = getDataFromNavigate();
}
```

### 权限请求
基类封装了一个基于RxPermission灵感的异步权限请求工具。

示例代码如下:
```java
    /**
     * 使用基类封装的方法请求权限
     */
    public void checkPermissionExample(){
        requestPermission(Collections.singletonList(Manifest.permission.ACCESS_FINE_LOCATION)
                , failedList -> {
                    //若全部成功，则返回空列表，否则返回失败的权限
                    LogUtil.d(TAG, "onNext: " + failedList.toString());
                });
    }
```


或直接使用PermissionUtil.class:
```java

    /**
     * 请求权限，灵感来自于RxPermission
     */
    public void checkPermissionExample(){
        PermissionUtil.get(Collections.singletonList(Manifest.permission.ACCESS_FINE_LOCATION))
                .request(this).subscribe(new Observer<List<String>>() {
            @Override
            public void onSubscribe(Disposable d) {
                dContainer.add(d);  //将回收器放入容器。每个Activity和Fragment都有一个该容器。在Destroy时进行回收
            }

            @Override
            public void onNext(List<String> failedList) {
                //若全部成功，则返回空列表，否则返回失败的权限
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }
```

## MVP框架

该框架为方便MVP框架搭建，在 com.binzeefox.foxframe.core.mvp包中封装了一个Presenter基类，包含一些常用的初始化及回收方法。在此不做更多介绍

## RxJava

框架为方便回收Observable，在基类中为每一个FoxActivity和FoxFragment的子类提供了一个变量dContainer(CompositeDisposable)。在子类中订阅Observable时请将产生的disposable放入dContainer中，这将使activity或fragment在销毁时一并回收所有放入dContainer中的disposable。

## DataHolder

一个简单的实现了读写锁的数据模型。在com.binzeefox.foxframe.core.tools包中。该类封装了读、写、删除以及订阅变化状态的方法。具体请阅读源码

## 网络请求

### Client.class
该类为抽象类。

实现```getBaseUrl()```方法并返回根路径即可。使用时将定义的api接口作为参数传入```getApi(Class<T> apiClass)```即可生成相应接口。

若需自定义Retrofit实例或OkHttp实例，仅需重写```createRetrofit()```与```createOkHttp()```两个方法即可。

## com.binzeefox.foxframe.tools 包

### ClassUtil
暂时仅封装了两个方法。\
```static Object getFieldValueByName(String fieldName, Object o)```\
```static void setFieldValueByName(String fieldName, Object target, Object value)```\
前者通过数据类与变量名获取该数据类的值，后者则通过变量名与数据量实例为数据量赋值。在处理数据库相关需求时比较实用

### CollectionUtil

提供了三个列表深拷贝方案。

MD5Util
提供了一个将通过字符串获取MD5摘要的方法。在生成token或者加密密码时比较实用

### RxUtil

提供了一些RxJava中的常用工具
- ```static <T> ObservableTransformer<T, T> setThreadIO()```：将当前Observable设置为IO线程，并在主线程中订阅。
- ```static <T> ObservableTransformer<T, T> setThreadComputation()```：将当前Observable设置为计算线程，并在主线程中订阅。
- ```static <T> ObservableTransformer<T, T> setThreadNew()```：在新线程中允许Observable，并在主线程中订阅。

### dev.AssetsUtil
暂时只实现了一个``` String readTxtAssets(String fullName)``` 用于读取txt文件。

### dev.LogUtil
日志工具类，在Application中设置好CURRENT_CLASS的值，则可以控制所有用该方法打印的日志。

### dev.TextTools
字符串工具类。提供方法如下

- ```static boolean isObb(long num)```：数字是否是奇数
- ```static boolean isObb(String numStr)```：同上若非纯数字则返回false
- ```static boolean isInteger(String str)```：是否是纯数字
- ```static boolean isDouble(String str)```：是否是双精度浮点数
- ```static boolean isChinese(char c)```：是否是汉字
- ```static boolean hasChinese(String str)```：判断字符串是否包含汉字（有一个都算）

该工具类同时提供中国大陆身份证格式验证。
通过```static IDCard idCard(String idStr)```获取IDCard类实例。

- ```IDCard.isLegal()```：返回身份证格式是否合法。
- ```IDCard.getCityName()```：获取该身份证的城市信息
- ```IDCard.getBirthDay()```：获取该身份证的生日信息（返回Date实例）
- ```IDCard.isMale()```：返回该身份证是否为男性

### dev.ThreadUtil
线程工具类，实际上就是个线程池

### image.ImageUtil
基于ImageDecoder的一个图片工具类，用于加载图片资源

示例如下：
```java
        //图片工具类, 下面是加载图片文件的例子
        new Thread(() -> {
            //在主线程中运行会报错，因为是耗时操作
            ImageUtil util = ImageUtil.get();
            try {
                Bitmap bitmap = util.decode(imageFile).decodeBitmap();  //加载Bitmap
                Drawable drawable = util.decode(imageFile).decodeDrawable();    //加载Drawable
                //其它操作
                util.decode(imageFile)
                        .roundCorners(8, 8) //圆角化，单位是px
                        .decodeDrawable();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
```
总之就是，先获取ImageUtil实例，然后通过decode方法传入资源文件（文件、resourceId、Uri、ByteBuffer）获取到ImageDecoder实例。具体请阅读源码并查看jetpack内ImageDecoder相关内容

### phone.IntentUtil
一个收集了一些常用弹窗的工具类。通过以下方法可以快速开启一些系统弹窗
- ```showInternetSetting()```：开启网络设置弹窗
- ```showNFCSetting()```：开启NFC设置弹窗
- ```showVolumeSetting()```：开启音量设置弹窗
- ```showWifiSetting()```：开启wifi设置弹窗

### phone.NoticeUtil
提示工具类，用于弹出Toast之类的提醒信息

```showToast()```方法可以弹出一个Toast信息，该方法若连续生成Toast，每生成新的Toast都会注销掉上一个Toast。

可通过以下方法开启震动
```Notice.vibrate()```获取工具实例
具体请查看```com.binzeefox.foxframe.tools.phone.NoticeUtil```的内部类```Vibrator```的注释内容




