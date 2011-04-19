project_classpath=`lein classpath`
echo $project_classpath
java -cp $project_classpath  clojure.main ./src/foreclojure/data_set.clj
