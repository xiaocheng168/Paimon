package cc.mcyx.paimon.database

import cn.hutool.db.Db
import cn.hutool.db.ds.simple.SimpleDataSource

/**
 * 快速连接数据
 * @param databaseName 数据库名
 * @param url 数据库地址
 * @param databaseType 数据库类型
 * @param user 数据库用户名
 * @param pass 数据库密码
 */
class PaimonDatabase(
    val databaseType: DatabaseType,
    val url: String,
    val databaseName: String = "",
    val user: String = "",
    val pass: String = ""
) {

    //数据源
    val simpleDataSource: SimpleDataSource

    //选中库
    val db: Db

    //数据原初始化
    init {
        when (databaseType) {
            DatabaseType.SQLITE -> {
                this.simpleDataSource = SimpleDataSource(String.format("jdbc:sqlite:%s", this.url), "", "")
                this.db = Db.use(this.simpleDataSource)
            }

            DatabaseType.MYSQL -> {
                this.simpleDataSource =
                    SimpleDataSource(String.format("jdbc:mysql:%s/%s", this.url, this.databaseName), user, pass)
                this.db = Db.use(this.simpleDataSource)
            }
        }
    }


    companion object {
        /**
         * 获得一个数据存储连接
         * @param url 存储位置
         * @return 返回连接对象
         */
        fun getDatabaseSqlite(url: String): PaimonDatabase {
            return PaimonDatabase(DatabaseType.SQLITE, url)
        }
    }

    enum class DatabaseType(val string: String) {
        MYSQL("mysql"), SQLITE("sqlite")
    }
}

