from typing import Union
from fastapi import FastAPI, Body, Depends, Header
from model import UserSchema, UserLoginSchema, ReceivePaymentSchema, SendPaymentSchema, CancelPaymentSchema, AcceptPaymentSchema, RejectPaymentSchema, RegisterResponse, SuccessResponse, ErrorResponse, TokenResponse, GetCertResponse, TransactionResponse
from auth_handler import create_token, decode_token, create_refresh_token, JWTAccessBearer, JWTRefreshBearer
from authority import register_user, authenticate_user, request_payment, approve_payment, cancel_payment, get_payment_history,get_wallet_balance, get_user_certificate, get_user_email, check_uid_exist, reject_payment, get_payment_record, get_user_name_from_uid

app = FastAPI()

@app.get("/", tags=["root"])
async def root() -> dict:
    return {"message":"Welcome to payment system."}

#this will accept public key from user 
@app.post("/user/register", tags=["user"])
async def api_register_user(user: UserSchema = Body(...)):
    print(user.dict())

    result = register_user(**user.dict())

    if result == 1:
        return {"error": "The provided email is registered with the system."}
    elif result == 2:
        return {"error": "Database error."}
    elif result == 3:
        return {"error": "Unable to issue digital certificate for the user."}

    response = create_token(result)
    response.update(create_refresh_token(result))
    print(result)
    with open(f'./user_certificates/{result}.crt', 'r') as f:
        response['certificate'] = f.read()
    with open(f'./authority/authority_cert.crt', 'r') as f:
        response['authority_certificate'] = f.read()
    
    #TODO: encrypt the certificate
    #TODO: include authority's public key in the android app
    print(response)
    return response

@app.post("/user/login", tags=["user"])
async def api_login_user(user: UserLoginSchema = Body(...)):
    result = authenticate_user(**user.dict())

    if result == 1 or result == 2:
        return {"error": "Invalid credentials."}

    if check_uid_exist(result) == 0:
        return create_token(result)
    else:
        return {"error": "Invalid credentials."}

@app.get("/user/get_cert/{uid}", dependencies=[Depends(JWTAccessBearer())], tags=["user"])
async def api_get_user_cert(uid: str):
    result = get_user_certificate(uid)
    if result == 1:
        return {"error": "Server error."}
    elif result == 2:
        return {"error": "User not exist."}

    return {"certificate":result}

@app.get("/user/get_token", dependencies=[Depends(JWTRefreshBearer())], tags=["user"])
async def api_get_token(Authorization: str =  Header(default=None)):
    token = Authorization.replace('Bearer ','')
    token_uid = decode_token(token)['uid']

    if check_uid_exist(token_uid) == 0:
        return create_token(token_uid)

    return {"error":"User does not exist."}

@app.get("/user/get_uid", dependencies=[Depends(JWTAccessBearer())], tags=["user"])
async def api_get_user_uid(Authorization: str =  Header(default=None)):
    token = Authorization.replace('Bearer ','')
    token_uid = decode_token(token)['uid']

    if check_uid_exist(token_uid) == 0:
        return {"uid": token_uid}
    
    return {"error": "User not exist."}

@app.get("/user/get_name", dependencies=[Depends(JWTAccessBearer())], tags=["user"])
async def api_get_user_uid(Authorization: str =  Header(default=None)):
    token = Authorization.replace('Bearer ','')
    token_uid = decode_token(token)['uid']

    result = get_user_name_from_uid(token_uid)
    if result == 1 or result == 2:
        return {"error": "User not exist."}
    
    return {"name": result}

@app.get("/payment/get_payment_info/{tid}", tags=["user"])
async def api_get_user_uid(tid:str):

    result = get_payment_record(tid)

    if result == 1 or result == 2:
        return {"error": "Transaction does not exist."}
    
    return result
    
    

@app.post("/payment/receive", dependencies=[Depends(JWTAccessBearer())], tags=["payment"])
async def receive(Authorization: str =  Header(default=None), receive_payment: ReceivePaymentSchema = Body(...)):
    token = Authorization.replace('Bearer ','')
    token_uid = decode_token(token)['uid']

    email = get_user_email(token_uid)
    receiver_email = get_user_email(receive_payment.receiver)
    if len(email) == 0 or receiver_email != email:
        return {"error": "Invalid payment request."}

    result = request_payment(**receive_payment.dict())

    if result == 1 or result == 2:
        return {"error": "Invalid payment request."}
    elif result == 3:
        return {"error": "Database error."}

    return {"tid": result}

@app.post("/payment/cancel", tags=["payment"])
async def cancel(tid: CancelPaymentSchema = Body(...)):

    result = cancel_payment(tid.tid)

    if result == 0:
        return {"result":"success"}

    return {"error": "Cancel Failed."}

@app.post("/payment/accept_payment", dependencies=[Depends(JWTAccessBearer())], tags=["payment"])
async def api_accept_payment(Authorization: str =  Header(default=None), payment: AcceptPaymentSchema = Body(...)):
    token = Authorization.replace('Bearer ','')
    token_uid = decode_token(token)['uid']

    email = get_user_email(token_uid)

    tid = payment.tid

    sender_email = get_payment_record(tid)['sender_email']

    if len(email) == 0 or sender_email != email:
        return {"error": "Invalid payment request."}

    result = approve_payment(tid)
    print("RESULT:", result)
    if result == 0: 
        return {"result": "success"}
    
    if result == 1:
        return {"error": "Payment request does not exist."}
    elif result == 2:
        return {"error": "Payment request has been processed already."}
    elif result == 3 or 4:
        return {"error": "Insufficient budget amount."}
    elif result != 0:
        return {"error": "Invalid payment request."}
    
    return {"error": "Could not process transaction."}


@app.post("/payment/reject_payment", dependencies=[Depends(JWTAccessBearer())], tags=["payment"])
async def api_reject_payment(Authorization: str =  Header(default=None), payment: AcceptPaymentSchema = Body(...)):
    token = Authorization.replace('Bearer ','')
    token_uid = decode_token(token)['uid']

    email = get_user_email(token_uid)

    tid = payment.tid
    sender_email = get_payment_record(tid)['sender_email']
    if len(email) == 0 or sender_email != email:
        return {"error": "Invalid request."}

    result = reject_payment(tid)

    if result == 1:
        return {"error": "Payment request does not exist."}
    elif result == 2:
        return {"error": "Database error."}

    return {"result": "success"}

@app.get("/payment/get_status/{tid}", dependencies=[Depends(JWTAccessBearer())], tags=["payment"])
async def api_get_transaction_status(tid:str, Authorization: str =  Header(default=None)):
    token = Authorization.replace('Bearer ','')
    token_uid = decode_token(token)['uid']

    email = get_user_email(token_uid)

    
    result = get_payment_record(tid)

    if result == 1:
        return {"error": "Payment record does not exist."}
    
    if result == 2:
        return {"error": "Database error."}

    if len(email) == 0 or not (email in [result['sender_email'], result['receiver_email']]):
        return {"error": "Unauthorised to access to the transaction record."}
    
    return {"status":result['trans_status']}

@app.get("/payment/list", dependencies=[Depends(JWTAccessBearer())], tags=["payment"])
def payment_list(Authorization: str =  Header(default=None)):
    token = Authorization.replace('Bearer ','')
    token_uid = decode_token(token)['uid']

    history = get_payment_history(token_uid)

    return {"result": history}

@app.get("/wallet/get_balance", dependencies=[Depends(JWTAccessBearer())], tags=["wallet"])
def get_balance(Authorization: str =  Header(default=None)):
    print(Authorization)
    token = Authorization.replace('Bearer ', '')
    token_uid = decode_token(token)['uid']

    print(token_uid)
    
    balance = get_wallet_balance(token_uid)

    if balance == -1:
        return {"error":"Cannot retrieve wallet balance"}

    return {"balance": balance}


