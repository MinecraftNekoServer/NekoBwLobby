# NekoBwLobby 插件

这是一个为起床战争游戏设计的服务器大厅插件，提供游戏数据查看和悬浮字显示功能。

## 功能

1. **游戏数据查看** - 使用 `/gameinfo` 命令查看起床战争游戏数据
2. **悬浮字显示** - 显示排行榜和其他信息的悬浮字
3. **动态悬浮字创建** - 具有权限的用户可以创建自定义悬浮字

## 指令

- `/gameinfo` - 查看起床战争游戏数据
- `/hologram <文本...>` - 创建自定义悬浮字 (需要 `nekobwlobby.hologram` 权限)

## 配置

### 数据库配置

```yaml
database:
  host: "localhost"        # 数据库主机地址
  port: 3306              # 数据库端口
  name: "nekobedwars"     # 数据库名称
  username: "root"        # 数据库用户名
  password: "wcjs123"     # 数据库密码
  useSSL: false           # 是否使用SSL连接
```

### 悬浮字配置

```yaml
holograms:
  # 排行榜悬浮字配置
  leaderboard:
    enabled: true                    # 是否启用排行榜悬浮字
    location:                        # 悬浮字位置
      world: "world"
      x: 0.0
      y: 0.0
      z: 0.0
    lines:                          # 悬浮字内容 (支持颜色代码 &)
      - "&6起床战争排行榜 &eTOP 10"
      - "&e第一名: &f%player1%"
      - "&7第二名: &f%player2%"
      - "&c第三名: &f%player3%"
      - "&f第四名: &f%player4%"
      - "&f第五名: &f%player5%"
      - "&f第六名: &f%player6%"
      - "&f第七名: &f%player7%"
      - "&f第八名: &f%player8%"
      - "&f第九名: &f%player9%"
      - "&f第十名: &f%player10%"
  # 额外的悬浮字配置
  additional:
    - name: "welcome"                # 悬浮字名称
      enabled: true                  # 是否启用
      location:                     # 悬浮字位置
        world: "world"
        x: 5.0
        y: 10.0
        z: 5.0
      lines:                        # 悬浮字内容 (支持颜色代码 &)
        - "&6欢迎来到起床战争大厅!"
        - "&e玩得开心!"
    - name: "stats"
      enabled: true
      location:
        world: "world"
        x: -5.0
        y: 10.0
        z: 5.0
      lines:
        - "&a服务器统计信息"
        - "&b在线玩家: %players%"
        - "&b游戏模式: 起床战争"
```

## 权限

- `nekobwlobby.hologram` - 使用 `/hologram` 命令创建悬浮字的权限

## 依赖

- HolographicDisplays - 用于显示悬浮字
- MySQL - 用于存储游戏数据