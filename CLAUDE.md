# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

鬼火论坛（Ghost Fire Forum）后端 — 基于积分经济体系的社区论坛，核心特色包括红包、吹牛打赌、勋章等互动玩法。

## 构建与运行

```bash
# 本地开发（默认 dev profile，需先启动 PostgreSQL 和 Redis）
mvn spring-boot:run

# 运行单元测试
mvn test

# 打包
mvn package -DskipTests

# Docker 一键部署（prod profile，PG + Redis + RabbitMQ + App）
docker-compose up -d
```

- Java 17，Spring Boot 3.2.5，Maven 构建
- 服务端口 8080，PostgreSQL 5432，Redis 6379
- 前端为 Vue.js + Vite（CORS 允许 `http://localhost:5173`）

## 架构概览

```
Controller → Service → Mapper(BaseMapper<T>) → PostgreSQL
    │           │
 Sa-Token    Redis（会话/限流/排行/浏览量缓存）
```

- **ORM**: MyBatis-Plus 3.5.6（非 JPA），Mapper 继承 `BaseMapper<T>`，`@MapperScan` 自动扫描
- **认证**: Sa-Token 1.38.0，token 存 Redis，BCrypt 加密密码
- **限流**: `@RateLimit` 自定义注解 + Redis INCR 拦截器，超限返回 429
- **分层规范**: 事务 `@Transactional` 放 Service 层，Controller 只做参数校验和结果返回
- **响应格式**: `Result<T>` 统一封装，`GlobalExceptionHandler` 用 `@ResponseStatus` 返回正确 HTTP 状态码

## 关键约定

### 积分经济
所有金币变动必须：SQL 原子更新 `user_stat.coin` → 写 `user_wallet_log` 流水，两步在同一事务内。不要用 Java 自增，用 `SET coin = coin + amount`。

### 并发安全
- 计数字段一律用 SQL 原子更新，禁止 Java 层 `getCount()+1` 再 set
- UserStat 可能不存在，操作前先 INSERT 默认记录（忽略冲突），再原子更新
- 防重复：用数据库 UNIQUE 约束，不在应用层判断

### 实体/DTO/VO 分层
- `entity/` — 数据库实体，`@TableName` + Lombok `@Data`
- `dto/` — 请求参数，带 `@Valid` 校验注解
- `vo/` — 返回视图对象，MapStruct 做 entity→vo 转换
- Controller 禁止直接接收实体类作为请求体

### 软删除
帖子和评论用 `status` 字段标记（1=正常，0=删除），不实际删除数据。

## 数据库

- 19 张表，建表脚本在 `sql/init.sql`
- 核心表：`sys_user`、`user_stat`、`post`、`comment`（楼中楼自关联）、`red_packet`、`boast`
- MyBatis-Plus 自动填充 `createTime`/`updateTime`（`MetaObjectHandlerConfig`）
- 分页用 `PaginationInnerInterceptor`（PostgreSQL 方言）
- 帖子 `search_vector` 生成列 + GIN 全文搜索索引

## 环境配置

- `application.yml` — 共享配置（mybatis-plus、sa-token、multipart）
- `application-dev.yml` — 本地开发（localhost），默认激活
- `application-prod.yml` — Docker 生产（`${DB_HOST}` / `${REDIS_HOST}` 等环境变量）

## 项目状态

- 19 个模块 79 个接口，全部实现完毕
- 20 个单元测试通过（5 个测试类）
- 详细文档见 `docs/接口文档.md`
