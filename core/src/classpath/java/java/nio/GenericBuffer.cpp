package java.nio;

#include "temp.h"

    /*
     *    Watch it though, this thing is generated from GenericBuffer.cpp
     *    with "C"'s preprocessor !
     *
     *    The C preprocessor needs to define:
     *
     *            TYPE, SIZE, ELT
     *          
     */


public abstract class BUFFER extends Buffer
{ 
    private ByteOrder endian = ByteOrder.BIG_ENDIAN;

   protected ELT [] backing_buffer;
    
    public static BUFFER allocateDirect(int capacity)
    {
	BUFFER b = new gnu.java.nio. BUFFERImpl(capacity, 0, capacity);
	return b;
    }

    public static BUFFER allocate(int capacity)
    {
	BUFFER b = new gnu.java.nio. BUFFERImpl(capacity, 0, capacity);
	return b;
    }

   final public static BUFFER wrap(ELT[] array,
			      int offset,
			      int length)
    {
        gnu.java.nio.BUFFERImpl b = new gnu.java.nio. BUFFERImpl(array, offset, length);
	return b;
    }

  final  public static BUFFER wrap(String a)
    {
#if SIZE == 1
	return wrap(a.getBytes(), 0, a.length());
#else
	int len = a.length();
	ELT[] buffer = new ELT[len];
	for (int i=0;i<len;i++)
	    {
		buffer[i] = (ELT) a.charAt(i);
	    }
	return wrap(buffer, 0, len);
#endif
    }

   final public static BUFFER wrap(ELT[] array)
    {
	return wrap(array, 0, array.length);
    }
    
    final public BUFFER get(ELT[] dst,
			    int offset,
			    int length)
    {

	  for (int i = offset; i < offset + length; i++)
	      {
		  dst[i] = get(); 
	      }
	  return this;
    }

  final  public BUFFER get(ELT[] dst)
    {
	return get(dst, 0, dst.length);
    }

  final  public BUFFER put(BUFFER src)
    {
	while (src.hasRemaining())
	    put(src.get());
	return this;
    }

  final public BUFFER put(ELT[] src,
			  int offset,
			  int length)
    {
	  for (int i = offset; i < offset + length; i++)
	      put(src[i]); 
	  return this;
    }

public final BUFFER put(ELT[] src)
    {
	return put(src, 0, src.length);
    }

public final boolean hasArray()
    {
      return (backing_buffer != null);
    }

public final ELT[] array()
    {
      return backing_buffer;
    }

    public final int arrayOffset()
    {
      return 0;
    }

    public int hashCode()
    {
	return super.hashCode();
    }

    public boolean equals(Object obj)
    {
	if (obj instanceof BUFFER)
	    {
		return compareTo(obj) == 0;
	    }	
	return false;
    }

    public int compareTo(Object ob)
    {
	BUFFER a = (BUFFER) ob;
	if (a.remaining() != remaining())
	    return 1;

	if (! hasArray() ||
	    ! a.hasArray())
	  {
	    return 1;
	  }
	
	int r = remaining();
	int i1 = pos;
	int i2 = a.pos;
	for (int i=0;i<r;i++)
	    {
		int t = (int) (get(i1)- a.get(i2));
		if (t != 0)
		    {
			return (int) t;
		    }
	    }
	return 0;
    }

    public final ByteOrder order()
    {
	return endian;
    }

    public final BUFFER order(ByteOrder bo)
    {
	endian = bo;
	return this;
    }


    // abstract functions:
  
    public abstract  ELT get();    
    public abstract  java.nio. BUFFER put(ELT  b);
    public abstract  ELT get(int index);
    public abstract  java.nio. BUFFER put(int index, ELT  b);
    public abstract BUFFER compact();
    public abstract boolean isDirect();
    public abstract BUFFER slice();
    public abstract BUFFER duplicate();
    public abstract BUFFER asReadOnlyBuffer();
    public abstract ShortBuffer asShortBuffer();
    public abstract CharBuffer asCharBuffer();
    public abstract IntBuffer asIntBuffer();
    public abstract LongBuffer asLongBuffer();
    public abstract FloatBuffer asFloatBuffer();
    public abstract DoubleBuffer asDoubleBuffer();

    public abstract char getChar();
    public abstract BUFFER putChar(char value);
    public abstract char getChar(int index);
    public abstract BUFFER putChar(int index, char value);
    public abstract short getShort();
    public abstract BUFFER putShort(short value);
    public abstract short getShort(int index);
    public abstract BUFFER putShort(int index, short value);
    public abstract int getInt();
    public abstract BUFFER putInt(int value);
    public abstract int getInt(int index);
    public abstract BUFFER putInt(int index, int value);
    public abstract long getLong();
    public abstract BUFFER putLong(long value);
    public abstract long getLong(int index);
    public abstract BUFFER putLong(int index, long value);
    public abstract float getFloat();
    public abstract BUFFER putFloat(float value);
    public abstract float getFloat(int index);
    public abstract BUFFER putFloat(int index, float value);
    public abstract double getDouble();
    public abstract BUFFER putDouble(double value);
    public abstract double getDouble(int index);
    public abstract BUFFER putDouble(int index, double value);
}
    
