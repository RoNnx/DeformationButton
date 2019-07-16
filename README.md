# DeformationButton 

[![](https://www.jitpack.io/v/RoNnx/DeformationButton.svg)](https://www.jitpack.io/#RoNnx/DeformationButton)  ![](https://img.shields.io/badge/language-java-red.svg)  ![](https://img.shields.io/badge/platform-android-green.svg)  [![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)![](https://img.shields.io/badge/API-21+-orange.svg)

一款有酷炫动画按钮（持续更新中！）

* 点击由矩形形变为圆形并有加载动画
* 成功动画
* 失败动画
* 变回矩形的动画
* ········

### 说明

这是我的第一个开源库（按钮动画效果是参考其他库，但都是自己实现的），虽然功能比较简单，代码写的不够好，可能BUG也很多，但是后续会对这个项目慢慢完善并持续进行优化，努力学习

### 演示效果

![](https://github.com/RoNnx/DeformationButton/blob/master/demonstration.gif?raw=true)

### 快速使用

**Step 1.** 添加 JitPack 存储库到 Project 的 build.gradle 中

```groovy
allprojects {
	repositories {
		...
		maven { url 'https://www.jitpack.io' }
	}
}
```

**Step 2.** 添加依赖

```groovy
dependencies {
    implementation 'com.github.RoNnx:DeformationButton:1.0.1'
}
```