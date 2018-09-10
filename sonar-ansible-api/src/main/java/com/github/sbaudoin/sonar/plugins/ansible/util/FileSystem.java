/**
 * Copyright (c) 2018, Sylvain Baudoin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.sbaudoin.sonar.plugins.ansible.util;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public final class FileSystem implements Closeable {
    private static final Logger LOGGER = Loggers.get(FileSystem.class);

    private boolean defaultFileSystem = false;
    private java.nio.file.FileSystem fs;


    /**
     * Initializes a new file system connected to the passed resource
     */
    public FileSystem(URI uri) {
        try {
            fs = FileSystems.newFileSystem(uri, Collections.emptyMap(), null);
        } catch (IOException|IllegalArgumentException e) {
            LOGGER.warn("Using default FS because of an error: {}", e.getMessage());
            fs = FileSystems.getDefault();
            defaultFileSystem = true;
        } catch (FileSystemAlreadyExistsException e) {
            LOGGER.warn("FS already exists for URI: {}", uri.toString());
            fs = FileSystems.getFileSystem(uri);
            defaultFileSystem = fs == FileSystems.getDefault();
        }
    }


    @Override
    public void close() throws IOException {
        // The default file system is actually not closeable, see https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html
        if (!defaultFileSystem) {
            fs.close();
        }
    }

    /**
     * Reads and returns the 1st-level content of the passed directory described as a URI. This URI must be compatible
     * with the file system.
     *
     * @param uri an URI to a directory
     * @return
     * @throws IOException
     * @throws NotDirectoryException if the file could not otherwise be opened because it is not a directory (optional specific exception)
     * @throws SecurityException In the case the default provider is used, and a security manager is installed, the checkRead
     *                           method is invoked to check read access to the directory.
     */
    public Stream<Path> readDirectory(URI uri) throws IOException {
        List<Path> paths = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(fs.provider().getPath(uri))) {
            for (Path entry: stream) {
                paths.add(entry);
            }
            return paths.stream();
        }
    }
}
