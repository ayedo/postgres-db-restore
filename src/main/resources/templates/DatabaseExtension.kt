package templates

import DatabaseRestorer
import templates.DatabaseExtension.Companion.DATABASE_RESTORER_KEY
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class DatabaseExtension : BeforeAllCallback, BeforeEachCallback {

    override fun beforeAll(context: ExtensionContext) {

        // TODO: setup your database here. Run migrations etc.

        // TODO: adjust connection info below
        val databaseRestorer = DatabaseRestorer(
            databaseName = "postgres",
            host = "localhost",
            port = 5432,
            user = "postgres",
            password = "postgres"
        )

        databaseRestorer.takeSnapshot()

        context.setDatabaseRestorer(databaseRestorer)
    }


    override fun beforeEach(context: ExtensionContext) {
        val restorer = context.getDatabaseRestorer()
        restorer.restore()
    }

    companion object {
        const val DATABASE_RESTORER_KEY = "databaseRestorer"
    }

}

fun ExtensionContext.getDatabaseRestorer(): DatabaseRestorer =
    this.getStoreOfNamespace(DatabaseRestorer::class.java).get(DATABASE_RESTORER_KEY) as DatabaseRestorer

fun ExtensionContext.setDatabaseRestorer(restorer: DatabaseRestorer) =
    this.getStoreOfNamespace(DatabaseRestorer::class.java).put(
        DATABASE_RESTORER_KEY,
        restorer
    )

fun <T> ExtensionContext.getStoreOfNamespace(clazz: Class<T>): ExtensionContext.Store =
    this.getStore(ExtensionContext.Namespace.create(clazz))!!
