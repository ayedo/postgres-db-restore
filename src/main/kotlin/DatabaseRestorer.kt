class DatabaseRestorer(
    private val databaseName: String,
    private val databaseCopier: DatabaseCopier,
    private val snapshotNamePostfix: String = "_snapshot"
) {
    constructor(
        databaseName: String,
        host: String,
        port: Int,
        user: String,
        password: String
    ) : this(
        databaseName,
        DatabaseCopier(host, port, user, password)
    )

    fun takeSnapshot() {
        databaseCopier.copy(from = databaseName, to = snapshotName(databaseName))
    }

    fun restore() {
        databaseCopier.copy(from = snapshotName(databaseName), to = databaseName)
    }

    private fun snapshotName(databaseName: String) = databaseName + snapshotNamePostfix

}
