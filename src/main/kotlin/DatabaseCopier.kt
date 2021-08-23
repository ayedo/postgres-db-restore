import java.sql.Connection
import java.sql.DriverManager

class DatabaseCopier(
    private val host: String,
    private val port: Int,
    private val user: String,
    private val password: String
) {
    private fun <R> withConnection(url: String, user: String, password: String, block: (Connection) -> R) {
        DriverManager.getConnection(url, user, password).use {
            block(it)
        }
    }

    private fun Connection.execute(query: String) {
        this.createStatement().use {
            it.execute(query)
        }
    }

    fun copy(from: String, to: String) {

        synchronized(this) {

            val url = jdbcUrl(host, port, from)

            withConnection(url, user, password) { db ->
                withExclusiveConnectionTo(db, to, from) {
                    db.execute("drop database if exists $to;")
                    db.execute("create database $to template $from;")
                }
            }
        }
    }


    private fun jdbcUrl(host: String, port: Int, database: String) =
        "jdbc:postgresql://$host:$port/$database?ApplicationName=database-copier"

    private fun withExclusiveConnectionTo(db: Connection, vararg databaseNames: String, fn: () -> Unit) {

        val whereDatNames = databaseNames.asSequence().map({ "datname = '$it'" }).joinToString(" or ")

        // make sure no new connection can be made to the provided databases
        db.execute("update pg_database set datallowconn = false where $whereDatNames;")

        // disconnect everybody else
        db.execute(
            """SELECT pg_terminate_backend(pg_stat_activity.pid)
                FROM pg_stat_activity
                WHERE $whereDatNames
                AND pid <> pg_backend_pid();"""
        )

        fn()

        // allow new connections again
        db.execute("update pg_database set datallowconn = true where $whereDatNames;")

    }

}
