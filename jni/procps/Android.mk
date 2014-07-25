LOCAL_PATH := $(call my-dir)

# Generic
PROCPS_C_INCLUDES := \
					$(LOCAL_PATH)/proc \
					$(LOCAL_PATH)/include

VERSION      := 3
SUBVERSION   := 2
MINORVERSION := 8
TARVERSION   := $(VERSION).$(SUBVERSION).$(MINORVERSION)

# proc
include $(CLEAR_VARS)

LOCAL_MODULE := proc

LOCAL_C_INCLUDES := $(PROCPS_C_INCLUDES)

LOCAL_CFLAGS := -Wno-error=format-security -DVERSION=$(VERSION) -DSUBVERSION=$(SUBVERSION) -DMINORVERSION=$(MINORVERSION)

LOCAL_SRC_FILES := $(addprefix proc/, \
					alloc.c devname.c escape.c ksym.c pwcache.c readproc.c \
					sig.c slab.c sysinfo.c version.c whattime.c)

include $(BUILD_STATIC_LIBRARY)

# pgrep
include $(CLEAR_VARS)

LOCAL_MODULE := pgrep

LOCAL_C_INCLUDES := $(PROCPS_C_INCLUDES)

LOCAL_CFLAGS := -Wno-error=format-security 

LOCAL_SRC_FILES := pgrep.c

LOCAL_STATIC_LIBRARIES := proc

LOCAL_LDLIBS := -lm -lc

include $(BUILD_EXECUTABLE)
