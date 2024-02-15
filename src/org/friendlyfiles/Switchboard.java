package org.friendlyfiles;

// TODO: Am I abstracting too early?  Do we really need this?

// Handles all coordination between the ui, backend, and file system.
class Switchboard {
    private Backend backend;
    // TODO: Change this to an array so we can have multiple file sources at once?
    private FileSource fileSource;

    public Switchboard(Backend backend, FileSource fileSource) {
        this.backend = backend;
        this.fileSource = fileSource;
    }

    
}
