FROM java
MAINTAINER ATSD Developers <dev-atsd@axibase.com>
#metadata
LABEL com.axibase.vendor="Axibase Corporation" \
    com.axibase.product="Energinet Grabber"

#install maven java and phantomjs
RUN apt-get update && apt-get install --no-install-recommends -y git-core maven wget \
    && mkdir /start \
    && rm -rf /var/lib/apt/lists/*

ADD files/ /start

# add install scripts

ADD entrypoint.sh /start
RUN chmod +x /start/*
RUN /start/install_phantomjs.sh
RUN /start/install_energinet_grabber.sh
ENTRYPOINT ["/bin/bash","/start/entrypoint.sh"]