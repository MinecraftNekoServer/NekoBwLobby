# NekoBwLobby 排行榜系统

## 功能介绍

本插件提供两种类型的排行榜：

1. **固定积分排行榜** - 只显示积分排行，不可切换
2. **可切换排行榜** - 可以点击切换显示不同类型的排行（击杀数、挖床数、死亡数），不显示积分排行

## 使用方法

### 创建排行榜

```
/addinteractivetop <名称> [类型]
```

参数说明：
- `<名称>` - 排行榜的唯一标识名称
- `[类型]` - 排行榜类型（可选）
  - `fixed` 或 `score` - 固定积分排行榜
  - `switchable` - 可切换排行榜（默认）

### 示例

创建固定积分排行榜：
```
/addinteractivetop total_score fixed
```

创建可切换排行榜：
```
/addinteractivetop player_stats switchable
```

## 排行榜类型说明

### 固定积分排行榜
- 始终显示玩家积分排行
- 无法通过点击切换类型
- 适合用于显示总体积分排名

### 可切换排行榜
- 默认显示击杀数排行
- 点击提示文字可切换到挖床数、死亡数排行
- 不显示积分排行（符合需求）
- 每个玩家可以独立切换，互不影响

## 技术特点

1. **玩家独立性** - 每个玩家看到的可切换排行榜类型是独立的
2. **数据实时性** - 排行榜数据从数据库实时获取，保持最新
3. **配置持久化** - 排行榜位置和类型配置会自动保存到config.yml，重启后自动加载
4. **显示方式兼容性** - 自动检测HolographicDisplays插件，如果存在则使用全息显示，否则使用盔甲架
5. **实体复用** - 使用盔甲架UUID实现实体复用，避免重复创建，减少服务器负担
6. **资源优化** - 合理管理显示实体，避免内存泄漏
7. **线程安全** - 使用ConcurrentHashMap确保并发安全

## 配置文件

配置文件位于 `config.yml`：
```yaml
leaderboards:
  settings:
    update-interval: 300  # 更新间隔（秒）
    display-limit: 10     # 显示玩家数量
  boards:                 # 排行榜列表（自动保存）
    total_score:              # 排行榜名称
      world: world            # 世界名称
      x: 0.5                  # X坐标
      y: 64.0                 # Y坐标
      z: 0.5                  # Z坐标
      type: SCORE             # 默认类型
      switchable: false       # 是否可切换
```

## 使用HolographicDisplays插件

本插件支持HolographicDisplays插件。如果服务器安装了HolographicDisplays插件，排行榜将自动使用全息显示技术，提供更好的视觉效果。

### 安装步骤：
1. 将HologramAPI_v1.6.2.jar或HolographicDisplaysAPI_v2.1.7.jar放入服务器的plugins文件夹
2. 重启服务器
3. 本插件会自动检测并使用HolographicDisplays API

### 优势：
- 更好的视觉效果
- 更低的服务器资源消耗
- 更流畅的交互体验