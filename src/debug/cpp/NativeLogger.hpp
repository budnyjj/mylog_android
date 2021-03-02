#pragma once

#include <stdlib.h>
#include <unistd.h>
#include <sys/time.h>
#include <sys/types.h>

#include <algorithm>
#include <array>
#include <charconv>
#include <codecvt>
#include <cstdio>
#include <cstring>
#include <locale>
#include <memory>
#include <mutex>
#include <string>
#include <string_view>
#include <system_error>
#include <tuple>
#include <vector>

#include <android/log.h>


using std::lock_guard;
using std::size_t;

// Converts UTF-16 to UTF-8 (to_bytes) and vice-versa (from_bytes)
using utf16_utf8_converter =
    std::wstring_convert<std::codecvt_utf8_utf16<char16_t>, char16_t>;
using utf16_view = std::u16string_view;


struct NativeLogger {

    /**
     * Performs static logger initialization.
     *
     * NOTE: this method should be invoked very first once per process.
     *
     * @param |process_tag| tag of the process to log
     * @param |log_file_descriptor| descriptor of additional file to log into or
     *                              a negative value if there is no one
     */
    static void init(
        const utf16_view process_tag,
        const int32_t log_file_descriptor
    ) {
        // set process tag first as it is used by |logcatFatal|
        s_process_tag = s_utf_converter.to_bytes(
            process_tag.cbegin(), process_tag.cend());
        // open additional file
        if (log_file_descriptor < 0) {
            s_log_file = nullptr;
        } else if ((s_log_file = fdopen(log_file_descriptor, "w")) == nullptr) {
            logcatFatal(
                "[NativeLogger] init: failed to open file descriptor %d: %s",
                log_file_descriptor,
                std::strerror(errno)
            );
            std::abort();
        }
    }

    explicit NativeLogger(const utf16_view braced_class_tag)
     : m_braced_class_tag(s_utf_converter.to_bytes(
             braced_class_tag.cbegin(), braced_class_tag.cend())),
       m_write_mtx()
    {}

    void d(const utf16_view msg_utf16) const {
        const auto msg_utf8 =
            s_utf_converter.to_bytes(msg_utf16.cbegin(), msg_utf16.cend());
        write(ANDROID_LOG_DEBUG, 'D', msg_utf8);
    }

 private:
    /** PID/TID field size reserved by LogCat. */
    static constexpr size_t SZ_ID = 5;
    /** Time field size reserved by LogCat. */
    static constexpr size_t SZ_TM = 2;
    /** Milliseconds field size reserved by LogCat. */
    static constexpr size_t SZ_MS = 3;
    /** Log message prefix size. */
    static constexpr size_t SZ_THREAD_INFO = SZ_TM * 5 + SZ_MS + SZ_ID * 2 + 10;

    /** Minimum length of place reserved by LogCat for process tag. */
    static constexpr size_t SZ_LOGCAT_TAG = 8;

    static thread_local utf16_utf8_converter s_utf_converter;
    static thread_local std::vector<char> s_msg_buffer;

    static std::string s_process_tag;
    static std::FILE* s_log_file;

    const std::string m_braced_class_tag;

    mutable std::mutex m_write_mtx;

    // TODO: document
    static void formatLastDigits(
        int src_number,
        const int num_digits,
        char* dst_buffer
    ) {
        for (auto i = num_digits - 1; i >= 0; --i) {
            dst_buffer[i] = (src_number % 10) + '0';
            src_number /= 10;
        }
    }

    /**
     * Reports fatal errors using to Android LogCat.
     *
     * Assumes that |s_process_tag| is set.
     */
    static void logcatFatal(const char* fmt, ...) {
        va_list ap;
        va_start(ap, fmt);
        __android_log_vprint(ANDROID_LOG_FATAL, s_process_tag.c_str(), fmt, ap);
        va_end(ap);
    }

    /**
     * Sets pointers to the buffer of required size with
     * prefilled thread and message producer information.
     *
     * @param msg_size message size
     * @param buffer_ptr pointer to the beginning of buffer
     * @param pt_pegin_ptr pointer to the beginning of process tag
     * @param pt_end_ptr pointer to the next char after the end of process tag
     * @param ct_ptr pointer to the beginning of the class tag
     * @param msg_ptr pointer to the beginning of the message
     */
    void getMsgData(
        const size_t msg_size,
        char*& buffer_ptr,
        char*& pt_begin_ptr,
        char*& pt_end_ptr,
        char*& ct_ptr,
        char*& msg_ptr
    ) const {
        // process tag size
        const auto pt_size = s_process_tag.size();
        // process tag field size
        const auto ptf_size = std::max<size_t>(pt_size, SZ_LOGCAT_TAG);
        // size of thread info with process tag field and terminal delimiters
        const auto tiptf_size = SZ_THREAD_INFO + ptf_size + 2;
        // size of thread info, delimited process tag and braced class tag
        const auto tiptfct_size = tiptf_size + m_braced_class_tag.size();
        const auto min_msg_buffer_size = tiptfct_size + msg_size + 1;
        const auto prev_msg_buffer_size = s_msg_buffer.size();
        if (prev_msg_buffer_size >= min_msg_buffer_size) {
            buffer_ptr = s_msg_buffer.data();
            pt_begin_ptr = buffer_ptr + SZ_THREAD_INFO;
            pt_end_ptr = pt_begin_ptr + ptf_size;
            ct_ptr = buffer_ptr + tiptf_size;
            msg_ptr = buffer_ptr + tiptfct_size;
            return;
        }
        // assign pointers after resize
        s_msg_buffer.resize(min_msg_buffer_size);
        buffer_ptr = s_msg_buffer.data();
        pt_begin_ptr = buffer_ptr + SZ_THREAD_INFO;
        ct_ptr = buffer_ptr + tiptf_size;
        msg_ptr = buffer_ptr + tiptfct_size;
        if (prev_msg_buffer_size != 0) {
            pt_end_ptr = pt_begin_ptr + ptf_size;
            return;
        }
        // fill message template
        // delimiters
        buffer_ptr[2] = '-';
        buffer_ptr[5] = ' ';
        buffer_ptr[8] = ':';
        buffer_ptr[11] = ':';
        buffer_ptr[14] = '.';
        buffer_ptr[18] = ' ';
        buffer_ptr[24] = ' ';
        buffer_ptr[30] = ' ';
        buffer_ptr[32] = ' ';
        // process id
        auto* pid_ptr = buffer_ptr + 19;
        formatLastDigits(getpid(), SZ_ID, pid_ptr);
        while (*pid_ptr == '0') {
            *pid_ptr++ = ' ';
        }
        // thread id
        auto* tid_ptr = buffer_ptr + 25;
        formatLastDigits(gettid(), SZ_ID, tid_ptr);
        while (*tid_ptr == '0') {
            *tid_ptr++ = ' ';
        }
        // process tag
        std::copy(s_process_tag.cbegin(), s_process_tag.cend(), pt_begin_ptr);
        pt_end_ptr = pt_begin_ptr + pt_size;
        while (pt_end_ptr < pt_begin_ptr + SZ_LOGCAT_TAG) {
            *pt_end_ptr++ = ' ';
        }
        pt_end_ptr[1] = ' ';
        // class tag
        std::copy(
            m_braced_class_tag.cbegin(),
            m_braced_class_tag.cend(),
            ct_ptr
        );
    }

    /**
     * Logs messages to both Android LogCat and log file.
     *
     * Assumes that |s_log_file != nullptr|.
     *
     * @param prio Android log priority
     * @param msg_buffer pointer to the beginning of buffer prefilled with
     *                   class tag and null-terminated message
     * @param msg_buffer_size size of |msg_buffer|
     * @param pt_size size of process tag
     * @param tipt_size size of thread information with process tag and
     *                  terminal delimiters
     */
    void write(
        const int prio,
        const char prio_char,
        const std::string& msg
    ) const {
        const auto msg_size = msg.size();
        char* buffer_ptr;
        char* ct_ptr;
        char* pt_begin_ptr;
        char* pt_end_ptr;
        char* msg_ptr;
        getMsgData(msg_size, buffer_ptr, pt_begin_ptr, pt_end_ptr, ct_ptr, msg_ptr);
        auto* msg_end_ptr = msg_ptr + msg_size;
        // fill null-terminated message
        std::copy(msg.cbegin(), msg.cend(), msg_ptr);
        *pt_end_ptr = '\0';
        *msg_end_ptr = '\0';
        if (s_log_file == nullptr) {
            __android_log_write(prio, pt_begin_ptr, ct_ptr);
            return;
        }
        // fill log level
        buffer_ptr[31] = prio_char;
        {
            // write under lock so as to keep messages synchronized across sinks
            lock_guard lock(m_write_mtx);
            // obtain date inside lock to make sure that
            // it won't differ much from printed by LogCat
            timeval tval;
            if (gettimeofday(&tval, nullptr) != 0) {
                logcatFatal(
                    "[NativeLogger] writeMixed: failed to get current time: %s",
                    std::strerror(errno)
                );
            }
            const auto* bt = localtime(&tval.tv_sec);
            // fill date components
            formatLastDigits(bt->tm_mon + 1, SZ_TM, buffer_ptr);
            formatLastDigits(bt->tm_mday, SZ_TM, buffer_ptr + 3);
            formatLastDigits(bt->tm_hour, SZ_TM, buffer_ptr + 6);
            formatLastDigits(bt->tm_min, SZ_TM, buffer_ptr + 9);
            formatLastDigits(bt->tm_sec, SZ_TM, buffer_ptr + 12);
            formatLastDigits(tval.tv_usec / 1000, SZ_MS, buffer_ptr + 15);
            // write to logcat
            __android_log_write(prio, s_process_tag.c_str(), ct_ptr);
            // replace null-terminators with colon and newline character
            *pt_end_ptr = ':';
            *msg_end_ptr = '\n';
            // write to file
            fwrite(buffer_ptr, 1, msg_end_ptr - buffer_ptr + 1, s_log_file);
            fflush(s_log_file);
        }
    }
};

thread_local utf16_utf8_converter NativeLogger::s_utf_converter;
thread_local std::vector<char> NativeLogger::s_msg_buffer;

std::string NativeLogger::s_process_tag;
std::FILE* NativeLogger::s_log_file;
