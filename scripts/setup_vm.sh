#!/bin/bash

# Install Java
sudo apt install openjdk-11-jre-headless

# Install Maven
sudo apt-get install software-properties-common
sudo apt-add-repository universe
sudo apt-get update
sudo apt install maven

# Create log dir
mkdir -p log/dev/membership
