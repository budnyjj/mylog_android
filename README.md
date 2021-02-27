Android Logger
==============

My own Android logger implementation with clean API
written in Java with performance in mind.

# Rationale

During the years of Android development I've defined a list of
requirements that a good Android logger must meet:
 - allow to format messages like
   ```
<app process tag>: [<class tag>] <message that typically starts from function name>
   ```
 - be as fast as possible, i.e. don't make slow operations like
   default Date formatting for each log record or use global locks
