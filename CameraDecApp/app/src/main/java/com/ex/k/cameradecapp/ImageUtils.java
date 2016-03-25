package com.ex.k.cameradecapp;

import android.util.Log;

/**
 * Created by k on 2016/1/27.
 */
public class ImageUtils {

    private static int YUVToARGB(int y, int u, int v) {
        int r = y + (int) (1.402f * u);
        int g = y - (int) (0.344f * v + 0.714f * u);
        int b = y + (int) (1.772f * v);
        r = r > 255 ? 255 : r < 0 ? 0 : r;
        g = g > 255 ? 255 : g < 0 ? 0 : g;
        b = b > 255 ? 255 : b < 0 ? 0 : b;

//        if (r > 255) {
//            r = 255;
//        } else if (r < 0) {
//            r = 0;
//        }
//
//        if (g > 255) {
//            g = 255;
//        } else if (g < 0) {
//            g = 0;
//        }
//
//        if (b > 255) {
//            b = 255;
//        } else if (b < 0) {
//            b = 0;
//        }

        int p =  0xff000000 | (b << 16) | (g << 8) | r;
//        Log.d("ccmm", Integer.toHexString(p));
        return p;
    }

    public static native int[] nativeNv21ToARGB888(byte[] data, int width, int height);

    public static int[] Nv21ToARGB888(byte[] data, int width, int height) {

        int size = width * height;
        int offset = size;
        int[] pixels = new int[size];

        int u, v, y1, y2, y3, y4;
        for (int i = 0, j = 0; i < size; i += 2, j++) {
            y1 = data[i] & 0xff;
            y2 = data[i + 1] & 0xff;
            y3 = data[width + i] & 0xff;
            y4 = data[width + i + 1] & 0xff;

            v = data[offset + j] & 0xff;
            u = data[offset + j + 1] & 0xff;
            v = v - 128;
            u = u - 128;

            pixels[i] = YUVToARGB(y1, u, v);
            pixels[i + 1] = YUVToARGB(y2, u, v);
            pixels[width + i] = YUVToARGB(y3, u, v);
            pixels[width + i  + 1] = YUVToARGB(y4, u, v);

            if (0 != i && 0 == (i + 2) % width) {
                i += width;
            }
        }
        return pixels;
    }

}
