package util.log;

import androidx.annotation.NonNull;

/**
 * Define logging API allowing to print formatted messages according to the template
 * "<PROCESS_TAG>: <CLASS_TAG> message" using a subset of default Android logging API.
 *
 * NOTE: Like Android logging API, this class does not provide method overloads with
 * string templates due to the slowness of String.format:
 * https://redfin.engineering/java-string-concatenation-which-way-is-best-8f590a7d22a8
 * We propose to use default "+" operator for this purpose in Java or (better) use string templates in Kotlin.
 */
public interface Logger {

    void v(@NonNull String msg);

    void d(@NonNull String msg);

    void i(@NonNull String msg);

    void w(@NonNull String msg);

    void e(@NonNull String msg);

    void e(@NonNull String msg, @NonNull Throwable throwable);
}
