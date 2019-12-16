# README

由仝曦文（aka. 狐彻）创建的框架基础包

包含了常用的工具类和可供继承的Activity与Application抽象类

和一个简易的MVP框架基类

有些使用的比较狗的设计模式，会一点点的改掉（又菜又懒的我）

想了解使用方式，可以直接联系本人

@qq: 864706521 (标明来意)
@wechat: bnzeefox

使用愉快

## 依赖

[![](https://jitpack.io/v/binzeefox/AndroidTemplete.svg)](https://jitpack.io/#binzeefox/AndroidTemplete)

implementation 'com.github.binzeefox:AndroidTemplete:Tag'

记得在Project.Gradle 里的 ```allprojects.repositories```里添加 ```maven { url "https://jitpack.io" }```


## 使用

继承并实现FoxApplication并设置为项目application，或者调用```FoxCore.init(Application)```进行初始化。
即使不初始化，也能使用部分功能，而初始化后框架内所有方法都不需要提供Context
