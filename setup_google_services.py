#!/usr/bin/env python3
import os
import json

def load_env(env_path=".env"):
    env_vars = {}
    if os.path.exists(env_path):
        with open(env_path, "r") as f:
            for line in f:
                line = line.strip()
                if not line or line.startswith("#"):
                    continue
                if "=" in line:
                    parts = line.split("=", 1)
                    key = parts[0].strip()
                    val = parts[1].strip().strip('"').strip("'")
                    env_vars[key] = val
    return env_vars

def main():
    # Load env from .env file
    env = load_env()
    
    # Also fetch from system environment variables as fallbacks
    project_id = env.get("FIREBASE_PROJECT_ID") or os.environ.get("FIREBASE_PROJECT_ID") or "focus-cube"
    project_number = env.get("FIREBASE_MESSAGING_SENDER_ID") or env.get("FIREBASE_PROJECT_NUMBER") or os.environ.get("FIREBASE_MESSAGING_SENDER_ID") or os.environ.get("FIREBASE_PROJECT_NUMBER") or "602410048349"
    app_id = env.get("FIREBASE_APP_ID") or os.environ.get("FIREBASE_APP_ID") or f"1:{project_number}:android:a5224898116c7294f526b4"
    api_key = env.get("FIREBASE_API_KEY") or os.environ.get("FIREBASE_API_KEY") or "AIzaSyDDDeGZ-MFrLDG0mDpKcc2Xw5Io0m1T1M8"
    package_name = env.get("APPLICATION_ID") or env.get("PACKAGE_NAME") or os.environ.get("APPLICATION_ID") or os.environ.get("PACKAGE_NAME") or "focus.cube"
    google_web_client_id = env.get("GOOGLE_WEB_CLIENT_ID") or os.environ.get("GOOGLE_WEB_CLIENT_ID")

    storage_bucket = f"{project_id}.firebasestorage.app"

    # Construct the oauth client configuration if Google Web Client ID is defined
    oauth_clients = []
    if google_web_client_id:
        oauth_clients.append({
            "client_id": google_web_client_id,
            "client_type": 3
        })

    google_services_json = {
        "project_info": {
            "project_number": project_number,
            "project_id": project_id,
            "storage_bucket": storage_bucket
        },
        "client": [
            {
                "client_info": {
                    "mobilesdk_app_id": app_id,
                    "android_client_info": {
                        "package_name": package_name
                    }
                },
                "oauth_client": oauth_clients,
                "api_key": [
                    {
                        "current_key": api_key
                    }
                ],
                "services": {
                    "appinvite_service": {
                        "other_platform_oauth_client": []
                    }
                }
            }
        ],
        "configuration_version": "1"
    }

    # Ensure app directory exists
    os.makedirs("app", exist_ok=True)
    
    # Write to app/google-services.json
    output_path = "app/google-services.json"
    with open(output_path, "w") as f:
        json.dump(google_services_json, f, indent=2)
        f.write("\n")
        
    print(f"Successfully generated {output_path}:")
    print(f"  Project ID: {project_id}")
    print(f"  Project Number: {project_number}")
    print(f"  App ID: {app_id}")
    print(f"  Package Name: {package_name}")
    if google_web_client_id:
        print(f"  Web Client ID: {google_web_client_id}")

if __name__ == "__main__":
    main()
