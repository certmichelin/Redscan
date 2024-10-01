FROM php:7.4-apache

SHELL ["/bin/bash", "-o", "pipefail", "-c"]

# hadolint ignore=DL3008
RUN apt-get update && apt-get install -y libfreetype6-dev libpng-dev libjpeg-dev libpq-dev libxml2-dev libonig-dev
RUN docker-php-ext-configure gd --with-jpeg --with-freetype
RUN docker-php-ext-install gd mbstring mysqli soap
RUN rm -rf /var/lib/apt/lists/*
RUN a2enmod rewrite

ENV MANTIS_VER 2.27.0
ENV MANTIS_MD5 c7b99d6b46de8e0597713571c570bac5
ENV MANTIS_URL https://sourceforge.net/projects/mantisbt/files/mantis-stable/${MANTIS_VER}/mantisbt-${MANTIS_VER}.tar.gz
ENV MANTIS_FILE mantisbt.tar.gz

# Install MantisBT itself
RUN curl -fSL "${MANTIS_URL}" -o "${MANTIS_FILE}" \
    && md5sum "${MANTIS_FILE}" \
    && echo "${MANTIS_MD5}  ${MANTIS_FILE}" | md5sum -c \
    && tar -xz --strip-components=1 -f "${MANTIS_FILE}" \
    && rm "${MANTIS_FILE}" \
    && rm -r doc \
    && chown -R www-data:www-data . \
    # Apply PHP and config fixes
    # Use the default production configuration
    && mv "$PHP_INI_DIR/php.ini-production" "$PHP_INI_DIR/php.ini" \
    && echo 'mysqli.allow_local_infile = Off' >> "$PHP_INI_DIR/conf.d/mantis.php.ini" \
    && echo 'display_errors = Off ' >> "$PHP_INI_DIR/conf.d/mantis.php.ini" \
    && echo 'log_errors = On ' >> "$PHP_INI_DIR/conf.d/mantis.php.ini" \
    && echo 'error_log = /dev/stderr' >> "$PHP_INI_DIR/conf.d/mantis.php.ini" \
    && echo 'upload_max_filesize = 50M ' >> "$PHP_INI_DIR/conf.d/mantis.php.ini" \
    && echo 'post_max_size = 51M ' >> "$PHP_INI_DIR/conf.d/mantis.php.ini" \
    && echo 'register_argc_argv = Off' >> "$PHP_INI_DIR/conf.d/mantis.php.ini"

COPY config_inc.php /var/www/html/config/config_inc.php
COPY custom_constants_inc.php /var/www/html/config/custom_constants_inc.php
COPY custom_strings_inc.php /var/www/html/config/custom_strings_inc.php

COPY ./mantis-entrypoint /usr/local/bin/mantis-entrypoint
RUN chmod +x /usr/local/bin/mantis-entrypoint

CMD ["mantis-entrypoint"]
