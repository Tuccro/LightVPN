LOCAL_PATH := $(call my-dir)
ROOT_PATH := $(LOCAL_PATH)

# System JNI
include $(CLEAR_VARS)

LOCAL_MODULE := system

LOCAL_SRC_FILES := system.cpp

LOCAL_LDLIBS := -ldl -llog

LOCAL_STATIC_LIBRARIES := cpufeatures

include $(BUILD_SHARED_LIBRARY)

# iptables
LOCAL_PATH := $(ROOT_PATH)
iptables_subdirs := $(addprefix $(LOCAL_PATH)/iptables/,$(addsuffix /Android.mk, \
	iptables \
	extensions \
	libiptc \
	))
include $(iptables_subdirs)

# iproute2
LOCAL_PATH := $(ROOT_PATH)
ip_subdirs := $(addprefix $(LOCAL_PATH)/iproute2/,$(addsuffix /Android.mk, \
	ip \
	lib \
	tc \
	))
include $(ip_subdirs)

# Import cpufeatures
$(call import-module,android/cpufeatures)
