#!/bin/bash
# Set up Google Services JSON from environment variables or .env file

# Get the script's directory
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Run the python setup script
if command -v python3 &>/dev/null; then
    python3 "$DIR/setup_google_services.py"
elif command -v python &>/dev/null; then
    python "$DIR/setup_google_services.py"
else
    echo "Error: Python is required to run the google-services configuration setup."
    exit 1
fi
