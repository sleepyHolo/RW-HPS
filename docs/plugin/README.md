# RW-HPS

欢迎来到 RW-HPS Plugin 文档。  
记得使用目录  

## Plugin的构成  
```
jar
    └───[class]
    plugin.json        //Plugin的配置文件          
```

### Plugin.json解析
```
{
  "name": "Plugin的名字",
  "author": "Plugin的作者",
  "main": "Plugin的主Main",
  "description": "Plugin的介绍",
  "version": "Plugin的版本"，
  (可选)"import": "你要在谁的后面进行加载"
}
```

#### 正常的例子
```
{
"name": "NetConnectProtocol",
"author": "Dr",
"main": "dr.rwhps.plugin.netconnectprotocol.Main",
"description": "RustedwarfareServer 1.14 NetConnectProtocol",
"version": "1.14 - 1.2.0.1 +"
}
```

#### 依赖加载的例子
请注意  递归依赖只会递归一次 不会进行多次,这是为了防止CPU高占用  
```
{
"name": "NetConnectProtocol-EX",
"author": "Dr",
"main": "dr.rwhps.plugin.netconnectprotocol.Main",
"description": "RustedwarfareServer 1.14 NetConnectProtocol",
"version": "1.14 - 1.2.0.1 +",
"import": "NetConnectProtocol"
}
```

## Plugin数据结构
NOT TAB 请参考RW-HPS的Setting源码  
接口正在调整为更通用版  