@echo off
for /f %%a in ('lein classpath') do @set project_classpath=%%a
java -cp %project_classpath% clojure.main -i .\src\foreclojure\mongo.clj -e "(use 'foreclojure.mongo) (prepare-mongo) (shutdown-agents)"
