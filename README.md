# distribute-lock

基于 JRaft 的分布式锁

使用：

1.用不同的配置文件，启动 LockServer 三次
2.启动 LockClient，调用 lock 方法即可加锁
