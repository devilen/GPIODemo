#include <jni.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <unistd.h>
#include <android/log.h>
#include "gpio-manager.h"

#define TAG "jni_gpio"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__)
#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL,TAG ,__VA_ARGS__)

#define IN              0
#define OUT             1
#define LOW             0
#define HIGH            1

#define BUFFER_MAX    3
#define DIRECTION_MAX 48

/**
 * 文件所有者是root，所以在操作之前修改文件的权限
 */

extern "C"
JNIEXPORT jint JNICALL
Java_com_zhongyi_gpiolibrary_GpioManager_exportGpio(JNIEnv *env, jclass clazz, jint gpio) {
    system("su");
    system("sudo");
    char buffer[BUFFER_MAX];
    int len;
    int fd;

    fd = open("/sys/class/gpio/export", O_WRONLY);
    if (fd < 0) {
        LOGE("Failed to open export for writing!\n");
        return(0);
    }
    LOGE("sizeof(buffer) : %d", sizeof(buffer));
    len = snprintf(buffer, BUFFER_MAX, "%d", gpio);
    LOGE("gpio : %d , len : %d, buffer : %s , sizeof(buffer) : %d", gpio, len, buffer, sizeof(buffer));
    if (write(fd, buffer, len) < 0) {
        LOGE("Fail to export gpio!\n");
        return 0;
    }

    close(fd);
    return 1;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_zhongyi_gpiolibrary_GpioManager_setGpioDirection(JNIEnv *env, jclass clazz, jint gpio,
                                                        jint direction) {
    static const char dir_str[]  = "in\0out";
    char path[DIRECTION_MAX];
    int fd;

    snprintf(path, DIRECTION_MAX, "/sys/class/gpio/gpio%d/direction", gpio);
    LOGE("path : %s", path);
    fd = open(path, O_WRONLY);
    if (fd < 0) {
        LOGE("failed to open gpio direction for writing!\n");
        return 0;
    }

    if (write(fd, &dir_str[direction == IN ? 0 : 3], direction == IN ? 2 : 3) < 0) {
        LOGE("failed to set direction!\n");
        return 0;
    }

    close(fd);
    return 1;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_zhongyi_gpiolibrary_GpioManager_readGpioStatus(JNIEnv *env, jclass clazz, jint gpio) {
    char path[DIRECTION_MAX];
    char value_str[3];
    int fd;

    snprintf(path, DIRECTION_MAX, "/sys/class/gpio/gpio%d/value", gpio);
    fd = open(path, O_RDONLY);
    if (fd < 0) {
        LOGE("failed to open gpio value for reading!\n");
        return -1;
    }

    if (read(fd, value_str, 3) < 0) {
        LOGE("failed to read value!\n");
        return -1;
    }

    LOGD("fd %d\n", fd);

    close(fd);
    return (atoi(value_str));
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_zhongyi_gpiolibrary_GpioManager_writeGpioStatus(JNIEnv *env, jclass clazz, jint gpio,
                                                       jint value) {
    static const char values_str[] = "01";
    char path[DIRECTION_MAX];
    int fd;

    snprintf(path, DIRECTION_MAX, "/sys/class/gpio/gpio%d/value", gpio);
    LOGE("write path: %s\n", path);
    fd = open(path, O_WRONLY);
    if (fd < 0) {
        LOGE("failed to open gpio value for writing!\n");
        return 0;
    }

    if (write(fd, &values_str[value == LOW ? 0 : 1], 1) < 0) {
        LOGE("failed to write value!\n");
        return 0;
    }

    close(fd);
    return 1;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_zhongyi_gpiolibrary_GpioManager_unexportGpio(JNIEnv *env, jclass clazz, jint gpio) {
    char buffer[BUFFER_MAX];
    int len;
    int fd;

    fd = open("/sys/class/gpio/unexport", O_WRONLY);
    if (fd < 0) {
        LOGE("Failed to open unexport for writing!\n");
        return 0;
    }

    len = snprintf(buffer, BUFFER_MAX, "%d", gpio);
    if (write(fd, buffer, len) < 0) {
        LOGE("Fail to unexport gpio!");
        return 0;
    }

    close(fd);
    return 1;
}