from pydantic import BaseModel, Field, EmailStr

class UserSchema(BaseModel):
    email: EmailStr = Field(...)
    password: str = Field(...)
    fullname: str = Field(...)
    hkid: str = Field(...)
    public_key: str = Field(...)
    
    class Config:
        schema_extra = {
            "example": {
                "email": "abc@gmail.com",
                "password": "strongpassword",
                "fullname": "Joshua Mark",
                "hkid": "A1234567(0)",
                "public_key": "--- Begin Public Key --- ..."
            }
        }

class UserLoginSchema(BaseModel):
    email: EmailStr = Field(...)
    password: str = Field(...)
    
    class Config:
        schema_extra = {
            "example": {
                "email": "abc@gmail.com",
                "password": "strongpassword",
            }
        }

class ReceivePaymentSchema(BaseModel):
    sender: str = Field(...)
    receiver: str = Field(...)
    amount: float = Field(...)

    class Config:
        schema_extra = {
            "example": {
                "sender": "00155d43b31e",
                "receiver": "00155d43b31f",
                "amount": 300.00,
            }
        }

class CancelPaymentSchema(BaseModel):
    tid: str = Field(...)

    class Config:
        schema_extra = {
            "example": {
                "tid": "00155d43b31e"
            }
        }

class SendPaymentSchema(BaseModel):
    tid: str = Field(...)
    sender: str = Field(...)
    receiver: str = Field(...)
    amount: float = Field(...)

    class Config:
        schema_extra = {
            "example": {
                "tid": "94df537c-9758-11ed-af3b-00155d43b31e",
                "sender": "00155d43b31e",
                "receiver": "00155d43b31f",
                "amount": 300.00,
            }
        }

class RejectPaymentSchema(BaseModel):
    tid: str = Field(...)

    class Config:
        schema_extra = {
            "example": {
                "tid": "00155d43b31e"
            }
        }

class AcceptPaymentSchema(BaseModel):
    tid: str = Field(...)

    class Config:
        schema_extra = {
            "example": {
                "tid": "00155d43b31e"
            }
        }

class RegisterResponse(BaseModel):
    access_token: str = Field(...)
    refresh_token: str = Field(...)
    certificate: str = Field(...)
    authority_certificate: str = Field(...)

    class Config:
        schema_extra = {
            "example": {
                "access_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6IjEwOWJhZTg2LTliYzYtMTFlZC05MmZhLTAwMTU1ZGE3NGJmOCIsImV4cGlyZXMiOjE2NzU1MDU2NDcuNzM4NzA3OH0.CawGtSP2RfhQmdDhDFAY2COlXtqsVSAHxTRHvcgfaK8",
                "refresh_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6IjEwOWJhZTg2LTliYzYtMTFlZC05MmZhLTAwMTU1ZGE3NGJmOCIsImV4cGlyZXMiOjE2NzU1MDU2NzQuMDMzMjY5Mn0.lqx9W6P5BsMIl6QnE3mg6Tos9L8QfcerWtL1Jj778Z8",
                "certificate": "-----BEGIN CERTIFICATE----- ... -----END CERTIFICATE-----",
                "authority_certificate": "-----BEGIN CERTIFICATE----- ... -----END CERTIFICATE-----",
            }
        }

class TokenResponse(BaseModel):
    access_token: str = Field(...)

    class Config:
        schema_extra = {
            "example": {
                "access_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6IjEwOWJhZTg2LTliYzYtMTFlZC05MmZhLTAwMTU1ZGE3NGJmOCIsImV4cGlyZXMiOjE2NzU1MDU2NDcuNzM4NzA3OH0.CawGtSP2RfhQmdDhDFAY2COlXtqsVSAHxTRHvcgfaK8"
            }
        }

class GetCertResponse(BaseModel):
    certificate: str = Field(...)
    class Config:
            schema_extra = {
                "example": {
                    "certificate": "-----BEGIN CERTIFICATE----- ... -----END CERTIFICATE-----"
                }
            }
class TransactionResponse(BaseModel):
    tid: str = Field(...)

    class Config:
        schema_extra = {
            "example": {
                "tid": "00155d43b31e"
            }
        }
class SuccessResponse(BaseModel):
    result: str = Field(...)
    class Config:
        schema_extra = {
            "example": {
                "result": "success"
            }
        }
class ErrorResponse(BaseModel):
    error: str = Field(...)

    class Config:
        schema_extra = {
            "example": {
                "error": "Invalid email address"
            }
        }