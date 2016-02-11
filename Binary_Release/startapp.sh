#!/bin/bash

# Start Keypad Keys
/root/keys &

# Path for the images
export IMAGE_PATH="/data/USB_500"

java -jar /root/usbtool.jar $IMAGE_PATH

