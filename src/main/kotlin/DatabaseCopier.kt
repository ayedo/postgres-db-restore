import org.jooq.DSLContext
import org.jooq.impl.DSL

class DatabaseCopier(
    private val host: String,
    private val port: Int,
    private val user: String,
    private val password: String
) {

    fun copy(from: String, to: String) {

        synchronized(this) {

            val url = jdbcUrl(host, port, from)

            DSL.using(url, user, password).use({ db: DSLContext ->

                withExclusiveConnectionTo(db, to, from) {
                    db.execute("drop database if exists $to;")
                    db.execute("create database $to template $from;")
                }

            })
        }
    }

    private fun jdbcUrl(host: String, port: Int, database: String) =
        "jdbc:postgresql://$host:$port/$database?ApplicationName=database-copier"

    private fun withExclusiveConnectionTo(db: DSLContext, vararg databaseNames: String, fn: () -> Unit) {

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
