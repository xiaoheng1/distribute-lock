package com.hanlin.distribute.server.store;

import com.hanlin.distribute.server.util.CommandCodec;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * @author shaoyu
 */
public class LockSnapshotFile {
    
    private static final Logger LOG = LoggerFactory.getLogger(LockSnapshotFile.class);
    
    private String path;
    
    public LockSnapshotFile(String path) {
        this.path = path;
    }
    
    public String getPath() {
        return path;
    }
    
    public boolean save(Map<String, LockRecord> localMap) {
        try {
            FileUtils.writeByteArrayToFile(new File(path), CommandCodec.encodeCommand(localMap));
            return true;
        } catch (IOException e) {
            LOG.error("Fail to save snapshot", e);
            return false;
        }
    }
    
    public Map<String, LockRecord> load() throws IOException {
        byte[] bs = FileUtils.readFileToByteArray(new File(path));
        if (bs != null && bs.length > 0) {
            return CommandCodec.decodeCommand(bs, Map.class);
        }
        throw new IOException("Fail to load snapshot from " + path + ",content: " + Arrays.toString(bs));
    }
}
