# SpringScrollLinearLayout
继承自linearlayout实现的阻尼滚动ScrollView <br>


# 项目配置

```
  allprojects {
      repositories {
          ...
          maven { url 'https://jitpack.io' }  //添加jitpack仓库
      }
  }
  
  dependencies {
	  implementation 'com.github.bigdongdong:SpringScrollLinearLayout:7.0' //添加依赖
  }
```

# 嵌套RecyclerView

```xml
<RelativeLayout
    android:descendantFocusability="blocksDescendants"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
    <android.support.v7.widget.RecyclerView
	android:layout_width="match_parent"
	android:layout_height="wrap_content"/>
</RelativeLayout>
```
