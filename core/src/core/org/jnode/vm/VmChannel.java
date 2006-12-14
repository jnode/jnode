package org.jnode.vm;

import gnu.classpath.Configuration;
import gnu.java.nio.VMChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.net.*;

/**
 * Native interface to support configuring of channel to run in a non-blocking
 * manner and support scatter/gather io operations.
 *
 * @author
 *
 */
public final class VmChannel
{

  public static int stdin_fd(){return 0;}
  public static int stdout_fd(){return 0;}
  public static int stderr_fd(){return 0;}

  public static void setBlocking(int fd, boolean blocking)
    throws IOException{}

  public static int available(int native_fd) throws IOException{return 0;}

  public static int read(int fd, ByteBuffer dst) throws IOException{return 0;}


  public static int read(int fd) throws IOException{return 0;}
  public static long readScattering(int fd, ByteBuffer[] dsts,
                                            int offset, int length)
    throws IOException{return 0;}


  public static int receive (int fd, ByteBuffer dst, ByteBuffer address)
    throws IOException{return 0;}

  public static int write(int fd, ByteBuffer src) throws IOException{return 0;}

  public static long writeGathering(int fd, ByteBuffer[] srcs,
                                     int offset, int length)
    throws IOException{return 0;}

  // Send to an IPv4 address.
  public static int send(int fd, ByteBuffer src, byte[] addr, int port)
    throws IOException{return 0;}

  // Send to an IPv6 address.
  public static int send6(int fd, ByteBuffer src, byte[] addr, int port)
    throws IOException{return 0;}

  public static void write(int fd, int b) throws IOException{}

  public static void initIDs(){}

  // Network (socket) specific methods.


  /**
   * Create a new socket, returning the native file descriptor.
   *
   * @param stream Set to true for streaming sockets{} false for datagrams.
   * @return The native file descriptor.
   * @throws java.io.IOException If creating the socket fails.
   */
  public static int socket(boolean stream) throws IOException{return 0;}


  public static boolean connect(int fd, byte[] addr, int port, int timeout)
    throws SocketException{return false;}

  public static boolean connect6(int fd, byte[] addr, int port, int timeout)
    throws SocketException{return false;}

  public static void disconnect(int fd) throws IOException{}

  public static int getsockname(int fd, ByteBuffer name)
    throws IOException{return 0;}

  /*
   * The format here is the peer address, followed by the port number.
   * The returned value is the length of the peer address{} thus, there
   * will be LEN + 2 valid bytes put into NAME.
   */
  public static int getpeername(int fd, ByteBuffer name)
    throws IOException{return 0;}

  public static int accept(int native_fd) throws IOException{return 0;}


  public static int open(String path, int mode) throws IOException{return 0;}

  public static long position(int fd) throws IOException{return 0;}

  public static void seek(int fd, long pos) throws IOException{}

  public static void truncate(int fd, long len) throws IOException{}

  public static boolean lock(int fd, long pos, long len,
                                     boolean shared, boolean wait)
    throws IOException{return false;}

  public static void unlock(int fd, long pos, long len) throws IOException{}

  public static long size(int fd) throws IOException{return 0;}

  public static MappedByteBuffer map(int fd, char mode,
                                             long position, int size)
    throws IOException{return null;}

  public static boolean flush(int fd, boolean metadata) throws IOException{return false;}

  public static void close(int native_fd) throws IOException{}
}
