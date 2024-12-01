Android Logger
==============

My own Android logger implementation with clean API written in Java with performance in mind.
It is based on my C++ logger.

# Rationale

During the years of Android development I've defined a list of
requirements that a good Android logger must meet (ordered by importance):
1. Allow to format messages like
   ```
   <date> <pid> <tid> <log-level> <app process tag>: [<class tag>] <message that typically starts from function name>
   ```
2. Be simple enough to do not appear as a new source of mistakes
   like message reordering, etc.
3. Don't affect app performance in Release mode.
4. Work reasonably fast in Debug mode, i.e. don't perform repetitive slow operations.
5. Do not bring any new dependencies or resources into project.

This is my attempt to implement a logger that meets these requirements.

# TODO

 - add native stream output to the android log
 - add unit and instrumentation tests
 - publish to Maven
