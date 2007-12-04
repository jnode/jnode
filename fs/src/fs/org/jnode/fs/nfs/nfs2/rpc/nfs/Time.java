package org.jnode.fs.nfs.nfs2.rpc.nfs;

import java.util.Date;

public class Time {

    private int seconds;

    private int microSeconds;

    public Time() {
        this(0, 0);
    }

    public Time(Date date) {
        this((int) (date.getTime() / 1000), 0);
    }

    public Time(int seconds, int microSeconds) {
        this.seconds = seconds;
        this.microSeconds = microSeconds;
    }

    public void setSeconds(int x) {
        this.seconds = x;
    }

    public int getSeconds() {
        return this.seconds;
    }

    public void setMicroSeconds(int x) {
        this.microSeconds = x;
    }

    public int getMicroSeconds() {
        return this.microSeconds;
    }

    @Override
    public String toString() {
        Date date = new Date((long) seconds * 1000);
        return date.toString();
    }

}
