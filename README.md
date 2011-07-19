# 4Clojure

An interactive problem website for Clojure beginners
[https://www.4clojure.com](https://www.4clojure.com).

## State of the Art

This site is in a very early stage of development, so there is not a
lot of polish yet. Anyone interested in contributing should check out
the [Issues](https://github.com/dbyrne/4clojure/issues) page for ideas
on what to work on.

## Setup instructions for running locally

* Download and install [leiningen](https://github.com/technomancy/leiningen).
* Download and install [mongodb](http://www.mongodb.org/).
* The project uses
[clojail](https://github.com/cognitivedissonance/clojail), which
requires a security policy setup in your home directory (because
Clojure's `eval` is unsafe if used improperly).  Set up a file called
`.java.policy` in your home directory.  The contents should look
vaguely like this:

        grant { permission java.security.AllPermission; };

    but see the readme of that project for more details.

* cd to the 4clojure project directory and run `lein deps`.
* Start up your mongodb, if you don't have autostart:

        mongod
* For the first time use, you will need to load the problem data. Run the script `load-data.sh`:

        ./load-data.sh
* Run `lein run` and then open the brower to http://localhost:8080/

        lein run

## Contributors

* [David Byrne](https://github.com/dbyrne) (dbyrne)
* [Alan Malloy](https://github.com/amalloy) (amalloy)
* [Anthony Grimes](https://github.com/Raynes) (raynes)
* [Carin Meier](https://github.com/gigasquid) (cmeier)
* [Clint Harrison](https://github.com/Clinteger) (clinteger)
* [David Davis](https://github.com/daviddavis) (daviddavis)
* [Devin Walters](https://github.com/devn) (devn)
* [Michael Kohl](https://github.com/citizen428) (citizen428)
* [Martin Sander](https://github.com/marvinthepa) (0x89)


Problem sources:

 * Aaron Bedra's [Clojure Koans](https://github.com/functional-koans/clojure-koans)
 * [Ninety-Nine Lisp Problems](http://www.ic.unicamp.br/~meidanis/courses/mc336/2006s2/funcional/L-99_Ninety-Nine_Lisp_Problems.html)
 * [Project Euler](http://www.projecteuler.net)

## License

The source code for 4clojure is available under the Eclipse Public License v 1.0.  For more information, see LICENSE.html.
