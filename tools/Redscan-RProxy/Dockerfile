# debian:buster-slim@sha256:b137d31f0ebcce71c9dde707975e1d6582afa178e7033ccb341ddba04d807043
FROM httpd:2.4.41@sha256:946c54069130dbf136903fe658fe7d113bd8db8004de31282e20b262a3e106fb

ENV OPENIDC_VERSION 2.4.2.1
ENV OPENIDC_VERSION_DEB_URL https://github.com/zmartzone/mod_auth_openidc/releases/download/v${OPENIDC_VERSION}/libapache2-mod-auth-openidc_${OPENIDC_VERSION}-1.buster+1_amd64.deb
ENV OPENIDC_VERSION_DEB_SHA1 3a23acded026a39c52bda05792efe5a18d65ab09

ENV CJOSE_VERSION 0.6.1.5
ENV CJOSE_DEB_URL https://github.com/zmartzone/mod_auth_openidc/releases/download/v2.4.0/libcjose0_${CJOSE_VERSION}-1.buster+1_amd64.deb
ENV CJOSE_DEB_SHA1 d6ca5569bed04a1450e054b3280702f1edfe2ae7

ENV DEBIAN_FRONTEND noninteractive

RUN depsRuntime=" \
    libcurl4 ca-certificates curl \
    libpcre3 \
    libjansson4 \
    libhiredis0.14 \
  " \
  set -x \
  && apt-get update \
  && apt-get install -y --no-install-recommends $depsRuntime \
  && rm -r /var/lib/apt/lists/* \
  && curl -sLSf "$CJOSE_DEB_URL" -o libcjose.deb \
  && echo "$CJOSE_DEB_SHA1 libcjose.deb" | sha1sum -c - \
  && dpkg -i libcjose.deb \
  && rm libcjose.deb \
  && curl -sLSf "$OPENIDC_VERSION_DEB_URL" -o mod_auth_openidc-$OPENIDC_VERSION.deb \
  && echo "$OPENIDC_VERSION_DEB_SHA1 mod_auth_openidc-$OPENIDC_VERSION.deb" | sha1sum -c - \
  && (dpkg -i mod_auth_openidc-$OPENIDC_VERSION.deb || echo "Accepting missing dependency") \
  && dpkg --force-all -i mod_auth_openidc-$OPENIDC_VERSION.deb \
  && rm mod_auth_openidc-$OPENIDC_VERSION.deb \
  && ln -s /usr/lib/apache2/modules/mod_auth_openidc.so /usr/local/apache2/modules/mod_auth_openidc.so \
  && rm -rf /var/log/dpkg.log /var/log/alternatives.log /var/log/apt

RUN sed -i 's|Listen 80||' conf/httpd.conf
RUN sed -i 's|LoadModule rewrite_module modules/mod_rewrite.so|LoadModule rewrite_module modules/mod_rewrite.so\nLoadModule auth_openidc_module modules/mod_auth_openidc.so|' conf/httpd.conf
RUN sed -i 's|#LoadModule proxy_module|LoadModule proxy_module|' conf/httpd.conf
RUN sed -i 's|#LoadModule proxy_http_module|LoadModule proxy_http_module|' conf/httpd.conf
RUN sed -i 's|#LoadModule ssl_module|LoadModule ssl_module|' conf/httpd.conf


RUN echo "Include conf/redscan.conf" >> conf/httpd.conf

COPY ./htdocs/ /usr/local/apache2/htdocs/

ENTRYPOINT ["httpd-foreground"]
RUN set -e; \
  set -x; \
  sed -i 's|^LogLevel warn|Include conf/loglevels.conf|' conf/httpd.conf; \
       for L in warn info debug; do echo "<IfDefine LOGLEVEL=$L>\n  LogLevel $L\n</IfDefine>" >> conf/loglevels.conf; done

	   
CMD [ "-DLOGLEVEL=info" ]