# IOSSwitchView
高仿IOS风格的UISwitch控件

效果图：

![uiswitch](https://github.com/hanhailong/IOSStudyResource/blob/master/screenshot/uiswitch.gif?raw=true)

使用方法：

第一步：布局xml

	<com.hhl.library.IOSSwitchView
	            android:id="@+id/switch_view"
	            android:layout_width="55dp"
	            android:layout_height="35dp"
	            android:layout_gravity="center"
	            app:thumbTintColor="#fff"
	            app:tintColor="#00ff00" />
	            
第二步：代码

	mSwitchView = (IOSSwitchView) findViewById(R.id.switch_view);
	        mStatusTv = (TextView) findViewById(R.id.tv_status);
	
	        mSwitchView.setOnSwitchStateChangeListener(new IOSSwitchView.OnSwitchStateChangeListener() {
	            @Override
	            public void onStateSwitched(boolean isOn) {
	                if (isOn) {
	                    mStatusTv.setText("状态：开");
	                } else {
	                    mStatusTv.setText("状态：关");
	                }
	            }
	        });
