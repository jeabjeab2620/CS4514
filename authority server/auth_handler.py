import time, jwt
from typing import Dict
from decouple import config
from fastapi import Request, HTTPException
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials

JWT_SECRET = config("secret")
JWT_ALGORITHM = config("algorithm")

ACCESS_TOKEN = "access_token"
REFRESH_TOKEN = "refresh_token"

def token_response(token: str):
    return {
        "access_token": token
    }
def token_refresh_response(token:str):
    return {
        "refresh_token": token
    }
def create_token(uid: str) -> Dict[str, str]:
    payload = {
        "uid": uid,
        "expires": time.time() + 300,
        "token_type": ACCESS_TOKEN
    }
    token = jwt.encode(payload, JWT_SECRET, algorithm=JWT_ALGORITHM)

    return token_response(token)

def create_refresh_token(uid: str) -> Dict[str, str]:
    payload = {
        "uid": uid,
        "expires": time.time() + 60*60*24*365, #set the expiration date to 365 days after now,
        "token_type": REFRESH_TOKEN
    }
    token = jwt.encode(payload, JWT_SECRET, algorithm=JWT_ALGORITHM)

    return token_refresh_response(token)

def decode_token(token: str) -> dict:
    try:
        decoded_token = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
        return decoded_token if decoded_token["expires"] >= time.time() else None
    except:
        return {}

class JWTAccessBearer(HTTPBearer):
    def __init__(self, auto_error: bool = True):
        super(JWTAccessBearer, self).__init__(auto_error=auto_error)

    async def __call__(self, request: Request):
        credentials: HTTPAuthorizationCredentials = await super(JWTAccessBearer, self).__call__(request)
        if credentials:
            if not credentials.scheme == "Bearer":
                raise HTTPException(status_code=403, detail="Invalid authentication scheme.")
            if not self.verify_jwt(credentials.credentials):
                raise HTTPException(status_code=403, detail="Invalid token or expired token.")
            return credentials.credentials
        else:
            raise HTTPException(status_code=403, detail="Invalid authorization code.")

    def verify_jwt(self, jwtoken: str) -> bool:
        isTokenValid: bool = False

        try:
            payload = decode_token(jwtoken)
        except:
            payload = None
        if payload and payload['token_type'] == ACCESS_TOKEN:
            isTokenValid = True
        return isTokenValid

class JWTRefreshBearer(HTTPBearer):
    def __init__(self, auto_error: bool = True):
        super(JWTRefreshBearer, self).__init__(auto_error=auto_error)

    async def __call__(self, request: Request):
        credentials: HTTPAuthorizationCredentials = await super(JWTRefreshBearer, self).__call__(request)
        if credentials:
            if not credentials.scheme == "Bearer":
                raise HTTPException(status_code=403, detail="Invalid authentication scheme.")
            if not self.verify_jwt(credentials.credentials):
                raise HTTPException(status_code=403, detail="Invalid token or expired token.")
            return credentials.credentials
        else:
            raise HTTPException(status_code=403, detail="Invalid authorization code.")

    def verify_jwt(self, jwtoken: str) -> bool:
        isTokenValid: bool = False

        try:
            payload = decode_token(jwtoken)
        except:
            payload = None
        if payload and payload['token_type'] == REFRESH_TOKEN:
            isTokenValid = True
        return isTokenValid

if __name__ == "__main__":
    pass