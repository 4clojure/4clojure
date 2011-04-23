@echo off
for /f %%a in ('lein classpath') do @set project_classpath=%%a
java -cp %project_classpath% clojure.main .\src\foreclojure\data_set.clj