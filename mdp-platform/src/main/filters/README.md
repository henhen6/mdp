# 注意

这里的配置信息用于在编译期间替换其他文件中的同名配置，如yml中写了如下配置
```yml
mdp:
  nacos:
    ip: @config.nacos.ip@
    port: @config.nacos.port@
    namespace: @config.nacos.namespace@
    username: @config.nacos.username@
    password: @config.nacos.password@
```
通过 `mvn clean package` 等命令进行编译后，将替换为
```yml
mdp:
  nacos:
    ip: 123456
    port: 123456
    namespace: 123456
    username: 123456
    password: 123456
```


