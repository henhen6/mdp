# 项目架构说明

- console-api
  - API层，用于封装跨服务的接口
   为了打造成为一套代码，同时支持单体架构和微服务架构，所以将跨服务的接口封装在console-api中，不同的架构采用不同的实现类
- console-boot-impl
    单体架构API实现类，通常的实现方式就是直接调用 console-service 层的接口
- console-cloud-impl
    微服务架构API实现类，通常的事项方式是封装一个OpenClient接口或者RPC接口，远程调用 console-controller 层的接口

# 简化项目

通常情况下，在实际的业务项目中，不会将一个项目同时部署2套架构，所以在项目前期确定好项目采用什么架构后，完全可以将另一种架构的实现类删除。




# 一、核心模块说明
- console-api（API 统一层）
  
    核心作用是封装跨服务交互接口，作为架构适配的统一入口。为实现 “一套代码兼容单体 / 微服务双架构” 的设计目标，将所有跨服务调用逻辑收敛于此，通过不同实现类适配不同架构，降低架构迁移成本。


- console-boot-impl（单体架构实现）

    单体架构下的 API 实现载体，采用 “本地直连” 方式：直接调用 console-service 层接口完成业务逻辑，避免远程调用开销，适配单体架构的性能需求。

    单体架构下，典型调用链：
    其他服务Controller或者Service 层 → console-api(Interface) → console-boot-impl → console-service


- console-cloud-impl（微服务架构实现）

    微服务架构下的 API 实现载体，采用 “远程调用” 方式：封装 OpenClient 或 RPC 接口，通过调用远程服务的 console-controller 层接口完成跨服务交互，适配微服务的分布式部署场景。

  微服务架构下，典型调用链：
  其他服务Controller或者Service 层 → console-api(Interface) → console-cloud-impl → 远程接口调用 → console-controller  → console-service

# 二、开发和编写接口步骤

当A服务需要调用B服务的接口时，通常的做法是：

1. B服务的 B-api 层定义接口
2. 定义实现类
   - 单体架构：B服务的 B-boot-impl 层实现接口
   - 微服务架构：B服务的 B-cloud-impl 层实现接口
3. A服务在 A-service 或 A-controller 层加入 B-api 层的依赖（在那层调用接口，就在那层引入依赖）
4. 在A服务编写代码，调用B-api层定义的接口
4. 引入 B-api 的实现依赖
   - 单体架构：在 boot-server/pom.xml 引入 B-boot-impl 的依赖
   - 微服务架构：在 A-server/pom.xml 引入 B-cloud-impl 的依赖

# 三、项目简化建议

实际业务场景中，项目架构（单体 / 微服务）在前期即可确定，且通常不会同时部署双架构。因此，架构确定后可直接删除非目标架构的实现类，减少冗余代码，降低编译部署成本。
（如单体架构可删除 console-cloud-impl，微服务架构可删除 console-boot-impl）