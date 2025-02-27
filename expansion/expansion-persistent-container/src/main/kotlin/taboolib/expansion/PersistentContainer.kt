package taboolib.expansion

import taboolib.common.io.newFile
import taboolib.common.platform.function.getDataFolder
import taboolib.library.configuration.ConfigurationSection
import java.io.File

/**
 * 创建持久化储存容器
 */
fun persistentContainer(
    type: Any,
    flags: List<String> = emptyList(),
    clearFlags: Boolean = false,
    ssl: String? = null,
    builder: PersistentContainer.() -> Unit
): PersistentContainer {
    return PersistentContainer(type, flags, clearFlags, ssl, builder)
}

/**
 * 创建持久化储存容器
 */
fun persistentContainer(
    host: String,
    port: Int,
    user: String,
    password: String,
    database: String,
    flags: List<String> = emptyList(),
    clearFlags: Boolean = false,
    ssl: String? = null,
    builder: PersistentContainer.() -> Unit,
): PersistentContainer {
    return PersistentContainer(host, port, user, password, database, flags, clearFlags, ssl, builder)
}

class PersistentContainer {

    val container: Container

    /**
     * 设置源
     * - 传入文件类型则为 SQLite 模式
     * - 传入 ConfigurationSection 则读取 SQL 配置
     */
    constructor(
        type: Any,
        flags: List<String> = emptyList(),
        clearFlags: Boolean = false,
        ssl: String? = null,
        builder: PersistentContainer.() -> Unit
    ) {
        this.container = when (type) {
            // SQLite 模式
            is File -> {
                ContainerSQLite(type)
            }
            // SQLite 模式
            is String -> {
                ContainerSQLite(newFile(getDataFolder(), type))
            }
            // SQL 模式
            is ConfigurationSection -> {
                ContainerSQL(
                    type.getString("host", "localhost")!!,
                    type.getInt("port"),
                    type.getString("user", "user")!!,
                    type.getString("password", "user")!!,
                    type.getString("database", "minecraft")!!,
                    flags,
                    clearFlags,
                    ssl
                )
            }
            // 无效类型
            else -> error("Unsupported source type: $type")
        }
        builder(this)
        this.container.init()
    }

    /**
     * 设置 SQL 源
     */
    constructor(
        host: String,
        port: Int,
        user: String,
        password: String,
        database: String,
        flags: List<String> = emptyList(),
        clearFlags: Boolean = false,
        ssl: String? = null,
        builder: PersistentContainer.() -> Unit
    ) {
        this.container = ContainerSQL(host, port, user, password, database, flags, clearFlags, ssl)
        builder(this)
        this.container.init()
    }

    /**
     * 注册标准容器
     */
    fun container(name: String, server: Boolean = false, builder: ContainerBuilder.() -> Unit) {
        container.addTable(name, player = !server, playerKey = true, data = ContainerBuilder(name).also(builder).dataList)
    }

    /**
     * 注册扁平容器
     */
    fun flatContainer(name: String, builder: ContainerBuilder.Flatten.() -> Unit = {}) {
        container.addTable(name, player = true, playerKey = false, data = ContainerBuilder.Flatten(name).also(builder).fixed().dataList)
    }

    /**
     * 获取控制器
     */
    operator fun get(name: String): ContainerOperator {
        return container.operator(name)
    }

    /**
     * 关闭容器
     */
    fun close() {
        container.close()
    }
}

