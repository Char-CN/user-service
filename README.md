# user-service（用户服务系统）
单点登录系统，只需要一个账号即可统一管理各个系统的：用户-角色-权限。
需引用 https://github.com/Char-CN/user-service-core.git

使用Mysql作为数据库
用户-角色-权限 数据缓存于EhCache中，如需分布式部署，交给EhCache配置分布式缓存。

<br>
<br>
发布需要修改：
<br>
1.修改过滤器: web.xml 中的 PermissionsFilter
<br>
2.修改数据源: datasource.properties
