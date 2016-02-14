#!/bin/bash

echo 'Starting Provision: lb1'
sudo apt-get update
sudo apt-get install -y nginx
sudo service nginx stop
sudo rm -rf /etc/nginx/sites-enabled/default
sudo touch /etc/nginx/sites-enabled/default
echo "upstream testapp {
        server 10.0.0.11;
        server 10.0.0.12;
}

server {
          listen 80;
          #default_server;
          #listen [::]:80 default_server ipv6only=on;

          #cache static content
          location ~* \.(css|js|gif|jpe?g|png)$ {
              expires 168h;
    
          }

          # REST caching
          location /api {
            expires 2m;
          }

          root /usr/share/nginx/www;
          index index.html index.htm;

          # Make site accessible from http://localhost/
          server_name localhost;

          location / {
                proxy_pass http://testapp;
          }

}" >> /etc/nginx/sites-enabled/default
sudo service nginx start
echo "Machine: lb1" >> /usr/share/nginx/www/index.html
echo 'Provision lb1 complete'
