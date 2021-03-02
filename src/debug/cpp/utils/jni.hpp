#include <jni.h>


using utf16_view = std::u16string_view;


// TODO: document, forbid copy and heap allocation
struct JniStringGuard {

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

    const utf16_view view() const {
        return m_view;
    }

    const char16_t* chars() const {
        return m_view.data();
    }

    const size_t size() const {
        return m_view.size();
    }

    ~JniStringGuard() {
        m_j_env->ReleaseStringChars(
            m_j_string,
            reinterpret_cast<const uint16_t*>(m_view.data()));
    }

 private:
    const jstring m_j_string;
    const utf16_view m_view;

    JNIEnv* m_j_env;
};
