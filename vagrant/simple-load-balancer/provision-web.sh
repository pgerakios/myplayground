#!/bin/bash

echo 'Starting Provision: web'$1
sudo apt-get update
sudo apt-get install -y nginx
echo "<h1>Machine: web"$1"</h1>" >> /usr/share/nginx/www/index.html
sudo service nginx start  
echo 'Provision web'$1 'complete'
