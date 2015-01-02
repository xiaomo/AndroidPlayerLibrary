说明
=================
基于vlc-android项目的视频播放器<br/>

功能
=================
1 支持更频繁的时间上报<br/>
2 显示缓冲进度<br/>

使用方式
=================
本库两种使用方式:<br/>
	第一种，直接new一个PlayerView或者在布局文件的xml里面嵌入view。<br/>
	第二种，跳转到一个写好的播放页面PlayerActivity<br/>
<br/>
<br/>
使用步骤
=================
//第一步 ：通过findViewById或者new PlayerView()得到mPlayerView对象<br/>
//mPlayerView= new PlayerView(PlayerActivity.this);<br/>
mPlayerView = (PlayerView) findViewById(R.id.pv_video);<br/>
<br/>
//第二步：设置参数，毫秒为单位<br/>
//mPlayerView.setNetWorkCache(20000);<br/>
<br/>
//第三步:初始化播放器<br/>
mPlayerView.initPlayer(mUrl);<br/>
<br/>
//第四步:设置事件监听，监听缓冲进度等<br/>
mPlayerView.setOnChangeListener(this);<br/>
<br/>
//第五步：开始播放<br/>
mPlayerView.start();<br/>

<br/>
使用截图
=================
<br/>
![preview](https://github.com/xiaomo/AndroidPlayerLibrary/blob/master/screenshot/device-2015-01-03-020624.png)
<br/>
