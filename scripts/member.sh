#!/bin/bash

# Get the directory of the script
script_dir=$( cd "$(dirname "${BASH_SOURCE[0]}")" ; pwd -P )
cd "$script_dir/.."

java -cp target/iDunno-1.0-SNAPSHOT.jar com.ryan.membership.MainServer $1 $2 $3
