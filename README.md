# SimpleTownyProduct

基于TownyAPI的资源区块玩法插件

## 玩法
1. 提供自定义资源区块配置，可配置地皮名称、公共私有类型、产出数量、执行时命令和互斥类型。
2. 偷窃区块资源玩法：每个城镇中的玩家都可以偷窃可收获资源的区块中部分的资源，假如玩家在偷窃活动中死亡，将会被收监。

## 依赖 dependency
[Spigot 1.20]<br>
[Towny, PlaceholderAPI, Vault]

## 命令 command

|                  | 作用               |
|------------------|------------------|
| t product info   | 查看目前可用的资源区块      |
| t product gain   | 一键收获城镇中所有资源区块的产出 |
| t product reload | 重加载插件配置+语言文件     |
| t product help   | 查看指令帮助           |
| t product steal  | 偷窃脚下私有资源区块的部分产出  |

## 权限 permission
|                          | 作用             |
|--------------------------|----------------|
| towny.product.publicgain | 从公共资源区块获取产出的权限 |
| towny.product.gain       | 从私有资源区块获取产出的权限 |
| towny.product.steal      | 偷窃权限           |

## 安装 install

1. 需要按照依赖中的插件到你的服务器。
2. 在Towny-settings-townperms.yml下`default`组加入towny.product.publicgain、towny.product.steal<br>
`mayor`组加入towny.product.gain
3. 下载本项目jar包放入plugins文件夹
4. 启动服务器

## 展示截图

待制作


