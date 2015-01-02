<br/>

<br/>
本库两种使用方式:
	第一种，直接new一个PlayerView或者在布局文件的xml里面嵌入view。
	第二种，跳转到一个写好的播放页面PlayerActivity
<br/>
//使用步骤
//第一步 ：通过findViewById或者new PlayerView()得到mPlayerView对象
//mPlayerView= new PlayerView(PlayerActivity.this);
mPlayerView = (PlayerView) findViewById(R.id.pv_video);

//第二步：设置参数，毫秒为单位
//mPlayerView.setNetWorkCache(20000);

//第三步:初始化播放器
mPlayerView.initPlayer(mUrl);

//第四步:设置事件监听，监听缓冲进度等
mPlayerView.setOnChangeListener(this);

//第五步：开始播放
mPlayerView.start();

<br/>
![preview](https://github.com/xiaomo/AndroidPlayerLibrary/blob/master/screenshot/device-2015-01-03-020624.png)
<br/>