FROM httpd:2.4.41@sha256:946c54069130dbf136903fe658fe7d113bd8db8004de31282e20b262a3e106fb

EXPOSE 443

RUN apt-get update && apt-get install -y --no-install-recommends libcurl4 ca-certificates curl libpcre3 libjansson4 libhiredis0.14 libapache2-mod-auth-openidc libcjose-dev 
RUN ln -s /usr/lib/apache2/modules/mod_auth_openidc.so /usr/local/apache2/modules/mod_auth_openidc.so && rm -rf /var/log/dpkg.log /var/log/alternatives.log /var/log/apt

RUN sed -i 's|Listen 80||' conf/httpd.conf
RUN sed -i 's|LoadModule rewrite_module modules/mod_rewrite.so|LoadModule rewrite_module modules/mod_rewrite.so\nLoadModule auth_openidc_module modules/mod_auth_openidc.so|' conf/httpd.conf
RUN sed -i 's|#LoadModule proxy_module|LoadModule proxy_module|' conf/httpd.conf
RUN sed -i 's|#LoadModule proxy_http_module|LoadModule proxy_http_module|' conf/httpd.conf
RUN sed -i 's|#LoadModule ssl_module|LoadModule ssl_module|' conf/httpd.conf
RUN echo "Include conf/redscan.conf" >> conf/httpd.conf

COPY ./conf/redscan.conf /usr/local/apache2/conf/redscan.conf
COPY ./certs/rproxy.crt /usr/local/apache2/conf/certs/rproxy.crt
COPY ./certs/rproxy.key /usr/local/apache2/conf/certs/rproxy.key

COPY ./htdocs/ /usr/local/apache2/htdocs/

ENTRYPOINT ["httpd-foreground"]
RUN set -e; \
  set -x; \
  sed -i 's|^LogLevel warn|Include conf/loglevels.conf|' conf/httpd.conf; \
       for L in warn info debug; do echo "<IfDefine LOGLEVEL=$L>\n  LogLevel $L\n</IfDefine>" >> conf/loglevels.conf; done

CMD [ "-DLOGLEVEL=info" ]