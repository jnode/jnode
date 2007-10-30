package org.jnode.fs.nfs.nfs2.rpc.nfs;

/**
 * @author Andrei Dore
 */
public enum ResultCode {

    NFS_OK(0), NFS_ERROR_NO_ENTRY(2), NFS_ERROR_ACCESS(13), NFS_ERROR_DIRECTORY_NOT_EMPTY(66);

    private int code;

    ResultCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ResultCode getResultCode(int code) {

        switch (code) {
            case 0:
                return NFS_OK;
            case 2:
                return NFS_ERROR_NO_ENTRY;
            case 13:
                return NFS_ERROR_ACCESS;
            case 66:
                return NFS_ERROR_DIRECTORY_NOT_EMPTY;
            default:
                throw new IllegalArgumentException("Code  unlnown : " + code);
        }

    }

}
