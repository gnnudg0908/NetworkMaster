LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

#LOCAL_MODULE_TAGS := optional
LOCAL_PACKAGE_NAME := NetworkControl
LOCAL_CERTIFICATE := platform
#LOCAL_PRIVILEGED_MODULE := true
#LOCAL_SRC_FILES := $(call all-subdir-java-files)
LOCAL_SRC_FILES := \
        $(call all-java-files-under, src) \
#LOCAL_PROGUARD_FLAG_FILES := proguard.flags
include $(BUILD_PACKAGE)