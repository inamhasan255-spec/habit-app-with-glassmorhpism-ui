import subprocess
try:
    subprocess.check_call(["gradle", "compileDebugKotlin"])
    print("Success")
except subprocess.CalledProcessError as e:
    print("Failed")
