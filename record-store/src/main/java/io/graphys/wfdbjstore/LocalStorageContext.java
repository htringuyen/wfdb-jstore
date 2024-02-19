package io.graphys.wfdbjstore;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.channels.Channels;

public class LocalStorageContext {
    private DatabaseInfo dbInfo;

    LocalStorageContext(DatabaseInfo dbInfo) {
        this.dbInfo = dbInfo;
    }

    public void requireHeader(String header, RecordInfo recordInfo) {
        var filePath = header.endsWith(".hea") ?
                recordInfo.getFullPath() + header
                : recordInfo.getFullPath() + header + ".hea";
        try (var inChannel = Channels.newChannel(recordInfo.getDbInfo().remoteHome().resolve(filePath).toURL().openStream());
             var fileChannel = new FileOutputStream(recordInfo.getDbInfo().localHome().resolve(filePath).getPath()).getChannel()
        ) {
            fileChannel.transferFrom(inChannel, 0, Long.MAX_VALUE);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }



}
