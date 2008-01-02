package org.jnode.net.nfs.nfs2;

public class NFS2Exception extends Exception {

    private ResultCode resultCode;

    public NFS2Exception(String message, Throwable e) {
        super(message, e);
    }

    public NFS2Exception(ResultCode resultCode) {
        super(getNFSMessage(resultCode));
        this.resultCode = resultCode;

    }

    public NFS2Exception(String message) {
        super(message);
    }

    public static String getNFSMessage(ResultCode resultCode) {

        String text;

        if (resultCode == ResultCode.NFS_ERROR_NO_PERM) {
            text = "Not owner.  The caller does not have correct ownership to perform the requested operation.";
        } else if (resultCode == ResultCode.NFS_ERROR_NO_ENTRY) {
            text = " No such file or directory.  The file or directory specified does not exist.";
        } else if (resultCode == ResultCode.NFS_ERROR_IO) {
            text = "Some sort of hard error occurred when the operation was in progress.  This could be a disk error, for example.";
        } else if (resultCode == ResultCode.NFS_ERROR_NXIO) {
            text = "No such device or address.";
        } else if (resultCode == ResultCode.NFS_ERROR_ACCESS) {
            text = "Permission denied.  The caller does not have the correct permission to perform the requested operation.";
        } else if (resultCode == ResultCode.NFS_ERROR_FILE_ALREADY_EXIST) {
            text = " File exists.  The file specified already exists.";
        } else if (resultCode == ResultCode.NFS_ERROR_NO_DEVICE) {
            text = " No such device.";
        } else if (resultCode == ResultCode.NFS_ERROR_NOT_DIRECTORY) {
            text = "   Not a directory.  The caller specified a non-directory in a directory operation.";
        } else if (resultCode == ResultCode.NFS_ERROR_IS_DIRECTORY) {
            text = "  Is a directory.  The caller specified a directory in a non-directory operation.";
        } else if (resultCode == ResultCode.NFS_ERROR_FILE_TOO_BIG) {
            text = " File too large.  The operation caused a file to grow beyond the server's limit.";
        } else if (resultCode == ResultCode.NFS_ERROR_NO_SPACE) {
            text = "  No space left on device.  The operation caused the server's filesystem to reach its limit.";
        } else if (resultCode == ResultCode.NFS_ERROR_READ_ONLY_FS) {
            text = " Read-only filesystem.  Write attempted on a read-only filesystem.";
        } else if (resultCode == ResultCode.NFS_ERROR_NAME_TOO_LONG) {
            text = "  File name too long.  The file name in an operation was too long.";
        } else if (resultCode == ResultCode.NFS_ERROR_DIRECTORY_NOT_EMPTY) {
            text = "Directory not empty.  Attempted to remove a directory that was not empty.";
        } else if (resultCode == ResultCode.NFS_ERROR_DISK_QUOTA) {
            text = " Disk quota exceeded.  The client's disk quota on the server has been exceeded.";
        } else if (resultCode == ResultCode.NFS_ERROR_STALE) {
            text = "  The 'fhandle' given in the arguments was invalid.  That is, the file referred to by that file handle no longer exists, or access to it has been revoked.";
        } else if (resultCode == ResultCode.NFS_ERROR_WRITE_FLUSH) {
            text = "  The server's write cache used in the WRITECACHE call got flushedto disk. ";

        } else {
            text = "";
        }

        return text + " Error code:" + resultCode.getCode();

    }

    public ResultCode getResultCode() {
        return resultCode;
    }

}
