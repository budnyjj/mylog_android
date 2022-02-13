#pragma once

#include <locale>

#include <jni.h>


// TODO: document, forbid copy and heap allocation
struct JniStringGuard {

    using utf16_view = std::u16string_view;

    static_assert(
        sizeof(char16_t) == sizeof(uint16_t),
        "JniStringGuard: require char16_t to store exactly 16 bits");

    explicit JniStringGuard(JNIEnv* j_env, jstring j_string)
     : m_j_env(j_env),
       m_j_string(j_string),
       m_view(
           reinterpret_cast<const char16_t*>(j_env->GetStringChars(j_string, nullptr)),
           j_env->GetStringLength(j_string))
    {}

    const utf16_view u16view() const {
        return m_view;
    }

    const char16_t* u16chars() const {
        return m_view.data();
    }

    const size_t u16size() const {
        return m_view.size();
    }

    const std::string u8str() const {
        return s_utf_converter.to_bytes(m_view.cbegin(), m_view.cend());
    }

    ~JniStringGuard() {
        m_j_env->ReleaseStringChars(
            m_j_string,
            reinterpret_cast<const uint16_t*>(m_view.data()));
    }

 private:
    // Converts UTF-16 to UTF-8 (to_bytes) and vice-versa (from_bytes)
    using utf16_utf8_converter =
        std::wstring_convert<std::codecvt_utf8_utf16<char16_t>, char16_t>;

    static thread_local utf16_utf8_converter s_utf_converter;

    const jstring m_j_string;
    const utf16_view m_view;

    JNIEnv* m_j_env;
};


thread_local JniStringGuard::utf16_utf8_converter JniStringGuard::s_utf_converter;
