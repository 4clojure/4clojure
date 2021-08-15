#
# To use:
#
#   $ docker build -t 4clojure .
#   $ docker run -d -p 3000:80 4clojure
#
# server accessible at localhost:3000
#

# Pull base image.
FROM dockerfile/ubuntu

# Install Java.
RUN \
  echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
  echo debconf shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections && \
  add-apt-repository -y ppa:webupd8team/java && \
  apt-get update && \
  apt-get install -y oracle-java7-installer

# Install Leiningen.
RUN curl -s https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > \
    /usr/local/bin/lein && \
    chmod 0755 /usr/local/bin/lein

# Install MongoDB.
RUN \
  apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10 && \
  echo 'deb http://downloads-distro.mongodb.org/repo/ubuntu-upstart dist 10gen' | tee /etc/apt/sources.list.d/mongodb.list && \
  apt-get update && \
  apt-get install -y mongodb-org && \
  mkdir -p /data/db

# Install Supervisor.
RUN \
  apt-get install -y supervisor && \
  sed -i 's/^\(\[supervisord\]\)$/\1\nnodaemon=true/' /etc/supervisor/supervisord.conf

# Setup 4clojure.
RUN echo 'grant { permission java.security.AllPermission; };' > ~/.java.policy
RUN git clone https://github.com/4clojure/4clojure.git /opt/4clojure
WORKDIR /opt/4clojure
ENV LEIN_ROOT 1
RUN lein deps
RUN mongod & ./load-data.sh
RUN echo '#!/bin/bash\ncd $(dirname $0) && lein ring server-headless 80' > start && chmod +x start

# Define working directory.
WORKDIR /etc/supervisor/conf.d

# Add Supervisor confs for MongoDB and 4clojure.
RUN echo '[program:mongodb]\ncommand=mongod\nautostart=true\nautorestart=true\n' > mongodb.conf
RUN echo '[program:4clojure]\ncommand=/opt/4clojure/start\nautostart=true\nautorestart=true\n' > 4clojure.conf

# Expose ring server port.
EXPOSE 80

# Define default command.
CMD ["supervisord", "-c", "/etc/supervisor/supervisord.conf"]
