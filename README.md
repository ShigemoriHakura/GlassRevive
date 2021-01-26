# GlassRevive
谷歌眼镜XE24的各种技巧（/=-=）/  
Revive Google Glass in 2021.  
不需要安装眼镜端app。  
Just install the apk to your phone~  

# GlassRevive-Android
* 需要解决高并发，虽然我并不觉得有这个需求。（就当锻炼自己了
* 现在需要重新做一个队列，来处理所有的消息，大致的构想是service来处理连接，然后再清理队列
* iOS这边可能暂时不太想做。。。（懒，手头还要完善Shiori项目，实现自己的几个设想

# 状态/Status
* iOS支持我还在研究，可能使用esp32做数据转发
* iOS support is still in progress. maybe I will use ESP32 for data transfer.
* 安卓支持已经完成
* Android support is done

# 多语言/Multi Language
* 简体中文/zh_CN
* English/en_US

# 功能/Features
* 安卓/Android
* [x]NotificationListener
* [x]Google Glass Proto
* [x]Google Glass Bluetooth Support


# 特殊感谢/Special thanks
* https://github.com/ieee8023/JoeGlass/
* https://github.com/xingrz/glass-research
* https://developers.google.com/glass/v1/reference/timeline/
* https://github.com/ShigemoriHakura/Google-Glass-Proto
