# Check if python is available, if not, do it with sed or whatever
python3 -c '
import json
with open("metadata.json", "r+") as f:
    data = json.load(f)
    data["name"] = "FocusCube"
    f.seek(0)
    json.dump(data, f, indent=4)
    f.truncate()
'
