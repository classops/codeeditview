## CodeEditView

自定义验证码输入控件

## 使用方法

### 添加依赖

1. 添加仓库到根build.gradle文件
``` gradle
    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
```
2. 添加库依赖
``` gradle
    dependencies {
        implementation 'com.github.wangmingshuo:codeeditview:0.1.3'
    }
````


- 使用数字验证码View

```
<com.hanter.android.codeeditview.CodeEditView
	android:id="@+id/civTest"
	android:layout_width="match_parent"
	android:layout_height="48dp"
	android:background="#EEEEEE"
	android:paddingLeft="20dp"
	android:paddingRight="20dp"
	android:paddingTop="4dp"
	android:paddingBottom="4dp"
	android:focusable="true"
	android:layout_marginTop="20dp"
	app:cev_code_length="4"
	app:cev_code_dividerWidth="8dp"
	app:cev_code_itemWidth="0dp"
	app:cev_code_drawable="@drawable/custom_code_box"
	app:cev_code_text=""
	app:cev_code_textColor="#33b5e5"
	app:cev_code_textSize="24sp"/>
```
- 设置输入完成回调接口

```
civTest.setOnCodeCompleteListener(new CodeEditView.OnCodeCompleteListener() {
            override fun onCodeComplete(code: String) {
                Toast.makeText(this@MainActivity, code, Toast.LENGTH_SHORT).show()
            }
        });
```
