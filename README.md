# PluginLimiter

## 限制插件功能

功能包括

* 限制插件**生效的位置**(location)
* 限制插件/全局**监听事件**(event)
* 限制插件 **监听器** (listener)
* 限制插件/全局 **命令** (command)

## 描述

插件灵感来源  [PerWorldplugins](https://github.com/TonimatasMCDEV/PerWorldPlugins) 一个让插件只在指定世界生效的插件。

看了它源码之后发现了新大陆，原来插件还可以控制其他插件(非依赖关系)，于是写了这个插件。

主要功能是**限制插件只在某些位置生效或者只在某些位置不生效。**

最简单的例子是让xxx插件只在某些世界/位置生效或者不在某些世界生效，具体看下面怎么配置

对于有**开发经验**的开发者来说，利用本插件可以对**插件之间的不兼容进行热处理**(治标不治本，仅处理报错问题)

比如我这有个报错

![image-20220510231926736](https://pic.imgdb.cn/item/627a828a094754312980ab4a.png)

看到 PluginLimiter字段就是 插件可以处理的报错，对Citizens插件进行限制(本例子仅作演示，不代表这样可以)

~~~ text
Citizens:
    enable: true
    matcher1:
      enable: false
      listeners:
        net.citizensnpcs.EventListen:
        - onPlayerInteractEntity
~~~

以上禁用了Citizens net.citizensnpcs.EventListen 监听器中的 的玩家交互事件 ，不再报错（但是右键npc的功能废了，需要自己取舍）

## 命令&权限

~~~text
/PluginLimiter reload  重载配置
/PluginLimiter version 查看插件版本

PluginLimiter 的缩写为pl、plr、pnl
运行命令需要op 或者 权限 pluginLimiter.admin
~~~

## 插件配置

本插件提供4种条件来控制插件行为
本插件无法限制 任务 (task)

**whitelist** 为全局白名单 当 匹配到某一规则时 将停止 黑名单、插件匹配器的匹配，并放行事件

**blacklist** 为全局黑 当 匹配到某一规则时 将停止、插件匹配器的匹配，并阻止事件

**limits** 键下储存插件匹配器

### 匹配器写法

匹配器分为 开关、反转开关、条件 三部分

**开关**: enable  (设置为true 开启 false 关闭，如果不声明将为false)

**反转开关**: reverse (举例：条件为：位置满足 = world 世界，
设置为true则将匹配所有除了world世界的世界。false 或者不声明将不做变化)

**条件**:
条件分为 events、listeners、commands、locations 4种

在条件后面加上 "-R"后缀表示反转条件 即 events-R、listeners-R、commands-R、locations-R
在条件后面加上 "-R"后缀表示反转条件 即 events-R、listeners-R、commands-R、locations-R
在条件后面加上 "-R"后缀表示反转条件 即 events-R、listeners-R、commands-R、locations-R

==========================================

#### events 表示插件/全局可以监听的事件，

匹配所有事件

~~~ text
events: all
~~~

匹配特定事件，比如 AsyncPlayerPreLoginEvent、PlayerJoinEvent

~~~ text
events:
- org.bukkit.event.player.AsyncPlayerPreLoginEvent
- org.bukkit.event.player.PlayerJoinEvent
~~~

==========================================

#### listeners 表示某个插件特定的监听器，于全局无效，因为每个插件的监听器都是不同的(包名)

匹配所有插件注册的监听器

~~~ text
listeners: all
~~~

匹配特定监听器下的所有监听方法

~~~ text
listeners:
  net.Indyuce.mmoitems.listener.ItemUse: all
~~~

匹配特定监听器下的特定方法
有2种写法，一个是方法名(将匹配第一个符合的方法)，一个是方法名:参数类型

~~~ text
listeners:
   net.Indyuce.mmoitems.listener.DisableInteractions:
    - clickInteractions
    - clickInteractions:org.bukkit.event.player.PlayerInteractEvent
~~~

==========================================

#### commands 匹配插件/全局命令

匹配所有命令

~~~ text
commands: all
~~~

匹配特殊命令(正则表达式)可在这里测试：https://www.bejson.com/othertools/regex/
某些特殊插件的命令无法匹配，因为其命令并不属于该插件，而是生成的 fake plugin 但你可以在Global里试试
比如 WorldEditor //开头的命令，
TrMenu 界面文件注册的自定义命令，

~~~ text
commands:
- "/mmoitems .*"   // 匹配所有 /mmoitems 开头的命令
- "/spawn"         //匹配 /spawn
- "/.? second .*"  //匹配第二个参数是 second 的命令
等等
~~~

==========================================

#### locations 匹配位置

匹配所有，如果不声明也是所有位置

~~~ text
locations: all
~~~

匹配某些世界 world、world_island

~~~ text
locations:
- world
- world_island
~~~

匹配某个矩形区域 像WorldEdit 的2个点一样 格式: 世界名:点一(x,y,z):点二(x,y,z)

~~~ text
locations:
- world:0,0,0:500,255,500
~~~

==========================================

#### 其他:

4种条件可自由组合，但有些条件无法获得位置，比如世界下雨事件，没有具体位置，所以使用的是出生点的位置
commands 与 events、listener 互不影响
当listener中的某个event参数符合events时将优先匹配events
events匹配的是所有监听器，listeners 匹配的是特定监听器

==========================================

匹配器的格式是

~~~ text
名字:  随意起名，不能包含 '\' 符号
  enable:true 必须
  reverse: true 可选
  events ... 可选
  listeners ... 可选
  commands ... 可选
  locations ... 可选
~~~

让插件在 world_nether 与 world 世界停止监听和命令
例子:

~~~ text
limiter1:
     enable: true
     events: all
     commands: all
     locations-R:
     - world_nether
     - world
~~~

==========================================

一个插件可以有多个匹配器，按从上到下的顺序匹配，当满足一个匹配器时将停止继续匹配
例子：
禁止更多弓 插件在 world_nether world 生效

~~~ text
MoarBows:
   enable: true
   limiter1:
     enable: true
     events: all
     commands: all
     locations-R:
     - world_nether
     - world
~~~

匹配器1(matcher1) : 使插件仅在world_rpg世界生效
匹配器2(matcher2) : 在世界world中禁止监听器 net.Indyuce.mmoitems.listener.DisableInteractions 中的 clickInteractions 监听方法

~~~ text
MMOItems:
   enable: true
   matcher1:
     enable: false
     events: all
     commands: all
     locations-R:
     - world_rpg
   matcher2:
     enable: true
     listeners:
       net.Indyuce.mmoitems.listener.DisableInteractions:
       - clickInteractions
     locations:
     - world
~~~

==========================================

所有插件匹配器位于limits键中

## 最后需要注意的

* 插件不支持热重载

* 使用本插件造成的任何后果本人不负责，请自行测试完再用于生产环境

  
