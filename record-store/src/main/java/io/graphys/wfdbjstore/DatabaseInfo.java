package io.graphys.wfdbjstore;

import lombok.Builder;
import java.net.URI;

/**
 * A wrapper containing all information needed for processing of the wfdb database.
 * It is also the signature of the given database.
 */
@Builder
public class DatabaseInfo {
    private final String name;
    private final String version;
    private final URI localHome;
    private final URI remoteHome;

    private DatabaseInfo(String name, String version, URI localHome, URI remoteHome) {
        this.name = name;
        this.version = version;
        this.localHome = localHome;
        this.remoteHome = remoteHome;
    }

    /**
     * Return the name of the database. Database name and version form the id of the database.
     * @return database name
     */
    public String name() {
        return name;
    }

    /**
     * Return the version of the database. Database name and version form the id of the database.
     * @return database version
     */
    public String version() {
        return version;
    }

    /**
     * Return URI that points to local root directory of the database.
     * @return URI that points to local root directory of the database
     */
    public URI localUri() {
        return localHome.resolve(name + "/").resolve( version + "/");
    }

    /**
     * Return URI that points to remote root directory of the database.
     * @return URI that points to remote root directory of the database
     */
    public URI remoteUri() {
        return remoteHome.resolve(name + "/").resolve(version + "/");
    }

    /**
     * Return URI that points to local home directory of all database.
     * @return URI that points to local home directory of all database
     */
    public URI localHome() {
        return localHome;
    }

    /**
     * Return URI that points to remote home directory of all database.
     * @return URI that points to remote home directory of all database
     */
    public URI remoteHome() {
        return remoteHome;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DatabaseInfo other) {
            return this.name.equals(other.name)
                    && this.version.equals(other.version);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (name + version).hashCode();
    }
}
