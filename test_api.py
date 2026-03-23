import urllib.request
import json

base_url = "http://localhost:5000"

def make_request(url, method="GET", data=None, token=None):
    headers = {'Content-Type': 'application/json'}
    if token:
        headers['Authorization'] = f"Bearer {token}"
        
    req_data = json.dumps(data).encode('utf-8') if data else None
    req = urllib.request.Request(f"{base_url}{url}", data=req_data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as response:
            return response.status, response.read().decode('utf-8')
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode('utf-8')
    except Exception as e:
        return 0, str(e)

# 1. Register a test user
status, body = make_request("/register", "POST", {
    "name": "Test User",
    "email": "test@test.com",
    "password": "password"
})
print("Register:", status, body)

if status == 409:
    # Already exists, try login
    status, body = make_request("/login", "POST", {
        "email": "test@test.com",
        "password": "password"
    })
    print("Login:", status, body)

try:
    token = json.loads(body).get("token")
except:
    token = None
print("Token:", token)

# 2. Add a subject
if token:
    status, body = make_request("/subjects", "POST", {"name": "Math2"}, token)
    print("Add Subject:", status, body)

# 3. Get subjects
if token:
    status, body = make_request("/subjects", "GET", token=token)
    print("Get Subjects:", status, body)
