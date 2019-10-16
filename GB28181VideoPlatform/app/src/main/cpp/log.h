//
// Created by Administrator on 2019/7/23.
//

#ifndef TESTAPP_LOG_H
#define TESTAPP_LOG_H

#include <android/log.h>
#define TAG "in-jni" // 这个是自定义的LOG的标识
#define DEBUG true
#define LOGD(...) if(DEBUG){__android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__);} // 定义LOGD类型
#define LOGI(...) if(DEBUG){__android_log_print(ANDROID_LOG_INFO,TAG ,__VA_ARGS__);} // 定义LOGI类型
#define LOGW(...) if(DEBUG){__android_log_print(ANDROID_LOG_WARN,TAG ,__VA_ARGS__);} // 定义LOGW类型
#define LOGE(...) if(DEBUG){__android_log_print(ANDROID_LOG_ERROR,TAG ,__VA_ARGS__);} // 定义LOGE类型


#endif //TESTAPP_LOG_H
