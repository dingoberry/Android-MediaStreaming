//
// Created by k on 2016/1/27.
//
#include <jni.h>
#include <sys/inotify.h>

#define NUM(array, length) {length = (sizeof(array) / sizeof(array[0]));}

static jint YUVToARGB(jint y, jint u, jint v) {
    jint r = y + (jint) (1.402f * u);
    jint g = y - (jint) (0.344f * v + 0.714f * u);
    jint b = y + (jint) (1.772f * v);

    if (r > 255) {
        r = 255;
    } else if (r < 0) {
        r = 0;
    }

    if (g > 255) {
        g = 255;
    } else if (g < 0) {
        g = 0;
    }

    if (b > 255) {
        b = 255;
    } else if (b < 0) {
        b = 0;
    }

    return 0xff000000 | (b << 16) | (g << 8) | r;
}

static jintArray Native_Nv21ToARGB888(JNIEnv *env, jclass clz, jbyteArray data, jint width,
                                      jint height) {

    jint size = width * height;
    jint offset = size;
    jbyte *bData = env->GetByteArrayElements(data, false);
    jintArray result = env->NewIntArray(size);
    jint * pixels = env->GetIntArrayElements(result, false);

    jint u, v, y1, y2, y3, y4;
    for (jint i = 0, j = 0; i < size; i += 2, j++) {
        y1 = bData[i] & 0xff;
        y2 = bData[i + 1] & 0xff;
        y3 = bData[width + i] & 0xff;
        y4 = bData[width + i + 1] & 0xff;

        v = bData[offset + j] & 0xff;
        u = bData[offset + j + 1] & 0xff;
        v = v - 128;
        u = u - 128;

        pixels[i] = YUVToARGB(y1, u, v);
        pixels[i + 1] = YUVToARGB(y2, u, v);
        pixels[width] = YUVToARGB(y3, u, v);
        pixels[width + 1] = YUVToARGB(y4, u, v);

        if (0 != i && 0 == (i + 2) % width) {
            i += width;
        }
    }
    env->ReleaseByteArrayElements(data, bData, 0);
    env->ReleaseIntArrayElements(result, pixels, 0);
//    env->SetIntArrayRegion(result, 0, size, pixels);
    return result;
}

static JNINativeMethod nativeMethods[] = {
        {
                "nativeNv21ToARGB888",
                "([BII)[I",
                (void *) Native_Nv21ToARGB888
        }
};

static int registerNativeMethods(JNIEnv *env) {
    int length;
    NUM(nativeMethods, length);
    return env->RegisterNatives(env->FindClass("com/ex/k/cameradecapp/ImageUtils"), nativeMethods,
                                length);
}

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    jint result = -1;
    jint version;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) == JNI_OK) {
        version = JNI_VERSION_1_4;
    } else {
        goto bail;
    }

    if (registerNativeMethods(env) < 0) {
        goto bail;
    }
    result = version;
    bail:
    return result;
}
