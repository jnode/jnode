/* Copyright (C) 2000, 2002  Free Software Foundation

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package java.awt.image;

import java.awt.color.*;
import java.awt.Point;
import gnu.java.awt.Buffers;

public class ComponentColorModel extends ColorModel
{
  private static int sum(int[] values)
  {
    int sum = 0;
    for (int i=0; i<values.length; i++)
      sum += values[i];
    return sum;
  }

  public ComponentColorModel(ColorSpace colorSpace, int[] bits,
			     boolean hasAlpha,
			     boolean isAlphaPremultiplied,
			     int transparency, int transferType)
  {
    super(sum(bits), bits, colorSpace, hasAlpha, isAlphaPremultiplied,
	  transparency, transferType);
  }

  public int getRed(int pixel)
  {
    if (getNumComponents()>1) throw new IllegalArgumentException();
    return (int) getRGBFloat(pixel)[0];
  }

  public int getGreen(int pixel)
  {
    if (getNumComponents()>1) throw new IllegalArgumentException();
    return (int) getRGBFloat(pixel)[0];
  }
  
  public int getBlue(int pixel)
  {
    if (getNumComponents()>1) throw new IllegalArgumentException();
    return (int) getRGBFloat(pixel)[0];
  }

  public int getAlpha(int pixel)
  {
    if (getNumComponents()>1) throw new IllegalArgumentException();
    int shift = 8 - getComponentSize(getNumColorComponents());
    if (shift >= 0) return pixel << shift;
    return pixel >> (-shift);
  }
   
  public int getRGB(int pixel)
  {
    float[] rgb = getRGBFloat(pixel);
    int ret = getRGB(rgb);
    if (hasAlpha()) ret |= getAlpha(pixel) << 24;
    return ret;
  }


  /* FIXME: Is the values returned from toRGB() in the [0.0, 1.0] or the
     [0.0, 256) range? 
     
     we assume it is in the [0.0, 1.0] range along with the
     other color spaces. */
  
  /* Note, it's OK to pass a to large array to toRGB(). Extra
     elements are ignored. */
  
  private float[] getRGBFloat(int pixel)
  {
    float[] data = { pixel };
    return cspace.toRGB(data);
  }

  private float[] getRGBFloat(Object inData)
  {
    DataBuffer buffer =
    Buffers.createBufferFromData(transferType, inData,
				 getNumComponents());
    int colors = getNumColorComponents();
    float[] data = new float[colors];
    
    // FIXME: unpremultiply data that is premultiplied
    for (int i=0; i<colors; i++)
      {
	float maxValue = (1<<getComponentSize(i))-1;
	data[i] = buffer.getElemFloat(i)/maxValue; 
      }
    float[] rgb = cspace.toRGB(data);
    return rgb;
  }
  
  public int getRed(Object inData)
  {
    return (int) getRGBFloat(inData)[0]*255;
  }

  public int getGreen(Object inData)
  {
    return (int) getRGBFloat(inData)[1]*255;
  }

  public int getBlue(Object inData)
  {
    return (int) getRGBFloat(inData)[2]*255;
  }

  public int getAlpha(Object inData)
  {
    DataBuffer buffer =
      Buffers.createBufferFromData(transferType, inData,
				   getNumComponents());
    int shift = 8 - getComponentSize(getNumColorComponents());
    int alpha = buffer.getElem(getNumColorComponents());
    if (shift >= 0) return alpha << shift;
    return alpha >> (-shift);
  }

  private int getRGB(float[] rgb)
  {
    /* NOTE: We could cast to byte instead of int here. This would
       avoid bits spilling over from one bit field to
       another. But, if we assume that floats are in the [0.0,
       1.0] range, this will never happen anyway. */
    
    /* Remember to multiply BEFORE casting to int, otherwise, decimal
       point data will be lost. */
    int ret =
      (((int) (rgb[0]*255F)) << 16) |
      (((int) (rgb[1]*255F)) <<  8) |
      (((int) (rgb[2]*255F)) <<  0);
    return ret;
  }

  /**
   * @param inData pixel data of transferType, as returned by the
   * getDataElements method in SampleModel.
   */
  public int getRGB(Object inData)
  {
    float[] rgb = getRGBFloat(inData);
    int ret = getRGB(rgb);
    if (hasAlpha()) ret |= getAlpha(inData) << 24;
    return ret;
  }

  public Object getDataElements(int rgb, Object pixel)
  {
    // Convert rgb to [0.0, 1.0] sRGB values.
    float[] rgbFloats = {
      ((rgb >> 16)&0xff)/255.0F,
      ((rgb >>  8)&0xff)/255.0F,
      ((rgb >>  0)&0xff)/255.0F
    };

    // Convert from rgb to color space components.
    float[] data = cspace.fromRGB(rgbFloats);
    DataBuffer buffer = Buffers.createBuffer(transferType, pixel,
					     getNumComponents());
    int numColors = getNumColorComponents();
    
    if (hasAlpha())
      {
	float alpha = ((rgb >> 24)&0xff)/255.0F;
	
	/* If color model has alpha and should be premultiplied, multiply
	   color space components with alpha value. */
	if (isAlphaPremultiplied()) {
	  for (int i=0; i<numColors; i++)
	    data[i] *= alpha;
	}
	// Scale the alpha sample to the correct number of bits.
	alpha *= (1<<(bits[numColors]-1));
	// Arrange the alpha sample in the output array.
	buffer.setElemFloat(numColors, alpha);
      }
    for (int i=0; i<numColors; i++)
      {
	// Scale the color samples to the correct number of bits.
	float value = data[i]*(1<<(bits[i]-1));
	// Arrange the color samples in the output array.
	buffer.setElemFloat(i, value);
      }
    return Buffers.getData(buffer);
  }

  public int[] getComponents(int pixel, int[] components, int offset)
  {
    if (getNumComponents()>1) throw new IllegalArgumentException();
    if (components == null)
    components = new int[getNumComponents() + offset];
    components[offset] = pixel;
    return components;
  }

  public int[] getComponents(Object pixel, int[] components, int offset)
  {
    DataBuffer buffer = Buffers.createBuffer(transferType, pixel,
					     getNumComponents());
    int numComponents = getNumComponents();

    if (components == null)
      components = new int[numComponents + offset];

    for (int i=0; i<numComponents; i++)
      components[offset++] = buffer.getElem(i);

    return components;
  }

  public int getDataElement(int[] components, int offset)
  {
    if (getNumComponents()>1) throw new IllegalArgumentException();
    return components[offset];
  }

  public Object getDataElements(int[] components, int offset, Object obj)
  {
    DataBuffer buffer = Buffers.createBuffer(transferType, obj,
					     getNumComponents());
    int numComponents = getNumComponents();

    for (int i=0; i<numComponents; i++)
      buffer.setElem(i, components[offset++]);

    return Buffers.getData(buffer);
  }

  public ColorModel coerceData(WritableRaster raster,
			       boolean isAlphaPremultiplied) {
    if (this.isAlphaPremultiplied == isAlphaPremultiplied)
      return this;

    /* TODO: provide better implementation based on the
       assumptions we can make due to the specific type of the
       color model. */
    super.coerceData(raster, isAlphaPremultiplied);
    
    return new ComponentColorModel(cspace, bits, hasAlpha(),
				   isAlphaPremultiplied, // argument
				   transparency, transferType);
  }

  public boolean isCompatibleRaster(Raster raster)
  {
    return super.isCompatibleRaster(raster);
    // FIXME: Should we test something more here? (Why override?)
  }

  public WritableRaster createCompatibleWritableRaster(int w, int h)
  {
    SampleModel sm = createCompatibleSampleModel(w, h);
    Point origin = new Point(0, 0);
    return Raster.createWritableRaster(sm, origin);
  }

  public SampleModel createCompatibleSampleModel(int w, int h)
  {
    int pixelStride = getNumComponents();
    
    /* TODO: Maybe we don't need to create a new offset array each
       time, but rather use the same array every time. */
    int[] bandOffsets = new int[pixelStride];
    for (int i=0; i<pixelStride; i++) bandOffsets[i] = i;
    return new ComponentSampleModel(transferType, w, h,
				    pixelStride, pixelStride*w,
				    bandOffsets);
  }

  public boolean isCompatibleSampleModel(SampleModel sm)
  {
    return 
      (sm instanceof ComponentSampleModel) &&
      super.isCompatibleSampleModel(sm);
  }

  public WritableRaster getAlphaRaster(WritableRaster raster)
  {
    if (!hasAlpha()) return null;
    
    SampleModel sm = raster.getSampleModel();
    int[] alphaBand = { sm.getNumBands() - 1 };
    SampleModel alphaModel = sm.createSubsetSampleModel(alphaBand);
    DataBuffer buffer = raster.getDataBuffer();
    Point origin = new Point(0, 0);
    return Raster.createWritableRaster(alphaModel, buffer, origin);
  }
    
  public boolean equals(Object obj)
  {
    if (!(obj instanceof ComponentColorModel)) return false;
    return super.equals(obj);
  }
}
