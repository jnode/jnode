package org.jnode.fs.nfs.nfs2.rpc.nfs;

public class NFS2Exception extends Exception {

    private ResultCode resultCode;

    public NFS2Exception(String message, Throwable e) {
        super(message, e);
    }

    public NFS2Exception(ResultCode resultCode) {
        super(getNFSMessage(resultCode));
        this.resultCode = resultCode;

    }

    public static String getNFSMessage(ResultCode resultCode) {

        if (resultCode == ResultCode.NFS_ERROR_ACCESS) {
            return "Permission denied.  The caller does not have the correct permission to perform the requested operation.";
        } else if (resultCode == ResultCode.NFS_ERROR_DIRECTORY_NOT_EMPTY) {
            return "Directory not empty.  Attempted to remove a directory that was not empty.";
        } else if (resultCode == ResultCode.NFS_ERROR_NO_ENTRY) {
            return " No such file or directory.  The file or directory specified does not exist.";
        } else {
            return "";
        }

    }

    public ResultCode getResultCode() {
        return resultCode;
    }

}
