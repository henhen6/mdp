# 接口对应的sdk

sdk提供给ISV使用，可在此基础上进行封装，具体使用方式参见单元测试。

mdp-sdk 是完全独立的一个jar，是需要提供给第三方开发者使用的，所以mdp-sdk的依赖项尽可能的少，千万不要让mdp-sdk依赖mdp-column-max、mdp-datasource-max、lmap-util-max等模块了。

mdp-sdk 使用http工具，封装mdp-openapi模块已经开发好的 @Open 接口，用于提供给第三方调用，第三方通过mdp-sdk进行调用时，请求路径为： sop-gateway-server -> mdp-openapi，
sop-gateway-server负责对请求进行鉴权，mdp-openapi负责接口的业务逻辑。

- mdp-sdk-core
  sdk 基础代码

- mdp-simple-sdk
  提供给第三方的sdk