package io.graphys.wfdbjstore;

import io.graphys.wfdbjstore.exception.NoDatabaseInfoException;

public class DatabaseContext {
    public static ScopedValue<DatabaseInfo> DB_INFO = ScopedValue.newInstance();

    public static DatabaseInfo getDB_INFO() {
        if (!DB_INFO.isBound()) {
            throw new NoDatabaseInfoException("Database info is not bound.");
        }
        var dbInfo = DB_INFO.get();
        if (dbInfo == null) {
            throw new NoDatabaseInfoException("Database info cannot be null.");
        }
        return dbInfo;
    }
}
