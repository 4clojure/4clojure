# 4Clojure

An interactive problem website for Clojure beginners
[https://www.4clojure.com](https://www.4clojure.com).

## Setup instructions for running locally

* Download and install [leiningen](https://github.com/technomancy/leiningen).
* Download and install [mongodb](http://www.mongodb.org/).
* The project uses [clojail](https://github.com/Raynes/clojail)  That
requires a security policy setup in your home directory.  Setup a file
called .java.policy in your home directory.  The contents should
look like this
       grant {
  permission java.security.AllPermission;
             };

* CD in the project and run "lein deps".  
* Start up your mongodb, if you don't have autostart
        mongod
*  For the first time use, you will need to load the problem data run
* the script load-data.sh
      ./load-data.sh
* Run lein ring server and the browser should open for you.
      lein ring server



