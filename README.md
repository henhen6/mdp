# 主数据平台

[![Language](https://img.shields.io/badge/语言-Java17%20%7C%20SpringCloud%20%7C%20Vue3%20%7C%20...-red?style=flat-square&color=42b883)](https://github.com/henhen6/mdp)
[![License](https://img.shields.io/github/license/henhen6/mdp?color=42b883&style=flat-square)](https://github.com/henhen6/mdp/blob/master/LICENSE)
[![Author](https://img.shields.io/badge/作者-很很-orange.svg)](https://github.com/henhen6)
[![Star](https://img.shields.io/github/stars/henhen6/mdp?color=42b883&logo=github&style=flat-square)](https://github.com/henhen6/mdp/stargazers)
[![Fork](https://img.shields.io/github/forks/henhen6/mdp?color=42b883&logo=github&style=flat-square)](https://github.com/henhen6/mdp/network/members)
[![Star](https://gitee.com/henhen6/mdp/badge/star.svg?theme=gray)](https://gitee.com/henhen6/mdp/stargazers)
[![Fork](https://gitee.com/henhen6/mdp/badge/fork.svg?theme=gray)](https://gitee.com/henhen6/mdp/members)
![star](https://gitcode.com/henhen6/mdp/star/badge.svg)

## 介绍
主数据平台，英文名master-data-platform，简称MDP。 主要为了解决 用户身份认证、用户访问子应用、子应用接入与审批、基础数据管理等。

MDP支持SSO、Oauth2等多种主流的单点登录协议。

## 子系统

- 工作台

  md-workbench，简称mdw

- 控制台

  md-console，简称mdc

- 开放平台

  md-open，简称mdo


## 技术栈
- 开发方面： 
  - JSON序列化：Jackson 
  - 缓存：Redis 
  - 数据库： MySQL 8.0.x 
  - 定时器：power-job 
  - 登录、权限框架：Sa-Token 
  - 持久层框架： Mybatis-flex
  - 代码生成器：自主开发 
  - 项目构建：Maven 
  - 文件服务器：支持 FastDFS 5.0.5/阿里云OSS/本地存储/MinIO/华为云/七牛云 任意切换
- 监控方面： 
  - 监控： spring-boot-admin 
- 部署方面： 
  - 服务器：CentOS 
  - Nginx 
  - Jenkins 
  - Docker 
  - Kubernetes
- 前端 
  - vue-vben-admin
  - vue3
  - vite
  - vue-router
  - Pinia
  - typescript
  - vxe-table
  - Axios
  - Dayjs 
  - codemirror