from database_handler import DatabaseHandler
from crypto import PublicPrivateKey, DigitalCertificate
from trans_status import TransStatus
import uuid, bcrypt

def generate_unique_uuid_32bits():
    uid = str(uuid.uuid4().int>> (128 - 32))

    while DatabaseHandler().check_uid_exist(uid):
        uid = str(uuid.uuid4().int >> (128 - 32))
    
    return uid

def _issue_certificate(uid: str, email: str, uname: str, public_key: str):

    if not(PublicPrivateKey.save_public_key(uid, public_key)):
        return False
    
    certificate = DigitalCertificate.get_value(uid, email, uname, public_key)

    DigitalCertificate.save_certificate(uid, certificate)

    return True

def register_user(email, password, fullname, hkid, public_key):
    if DatabaseHandler().check_email_exist(email):
        return 1

    salt = bcrypt.gensalt()
    new_password = bcrypt.hashpw(password.encode('utf-8'), salt)
    new_password = new_password.decode('utf-8')

    uid = generate_unique_uuid_32bits()

    if _issue_certificate(uid, email, fullname, public_key):

        result = DatabaseHandler().add_user(uid, email, new_password, fullname, hkid)
        result = result and DatabaseHandler().add_wallet(
            uid, 
            f'./user_certificates/{uid}.crt', 
            f'./user_keys/{uid}_public_key.pem'
        )

        #delete user if any error
        if not result:
            DatabaseHandler().delete_user(uid)
            return 2

    else:
        return 3

    return uid

def get_user_email(uid):
    return DatabaseHandler().get_user_email_from_uid(uid)

def check_uid_exist(uid):
    if not DatabaseHandler().check_uid_exist(uid):
        return 1

    return 0

def authenticate_user(email, password):
    if not (DatabaseHandler().check_email_exist(email)):
        return 1

    pass_hash = DatabaseHandler().get_user_password(email)

    #bcrypt check hash match the password returns True if match
    result = bcrypt.checkpw(bytes(password,"utf-8"), bytes(pass_hash,"utf-8"))

    if result:
        return DatabaseHandler().get_user_uid(email)
    else:
        return 2

def get_user_certificate(uid):
    if DatabaseHandler().check_uid_exist(uid):
        cert_loc = DatabaseHandler().get_user_certificate_location(uid)
        
        if len(cert_loc):
            cert = DigitalCertificate.read_certificate(cert_loc)
            return DigitalCertificate.convert_to_pem(cert)
        else:
            return 1
    
    return 2

def get_user_name_from_uid(uid):
    
    if DatabaseHandler().check_uid_exist(uid):
        name = DatabaseHandler().get_user_name_from_uid(uid)

        if len(name):
            return name
        else:
            return 1
    
    return 2

def request_payment(sender, receiver, amount) -> int | str:
    if not (DatabaseHandler().check_uid_exist(sender) and DatabaseHandler().check_uid_exist(receiver)):
        return 1

    sender_email = DatabaseHandler().get_user_email_from_uid(sender)
    receiver_email = DatabaseHandler().get_user_email_from_uid(receiver)
    
    if amount < 0:
        return 2
    
    tid = generate_unique_uuid_32bits()
    result = DatabaseHandler().add_transaction_record(tid, sender_email, receiver_email, amount)

    if result:
        return tid
    else:
        return 3

def cancel_payment(tid):
    if DatabaseHandler().cancel_transaction(tid):
        record = DatabaseHandler().get_transaction_record(tid)

        if len(record) and record[3] == TransStatus.REJECTED.name:
            return 0
        return 1

    return 2

def approve_payment(tid) -> int:
    #get transaction record from tid
    record = DatabaseHandler().get_transaction_record(tid)
    if len(record) == 0:
        DatabaseHandler().update_transaction_record(tid, TransStatus.REJECTED.name)
        return 1

    sender_uid = record[0]
    amount = float(record[2])
    trans_status = record[3]

    if trans_status != TransStatus.REQUESTED.name:
        DatabaseHandler().update_transaction_record(tid, TransStatus.REJECTED.name)
        return 2

    sender_budget = DatabaseHandler().get_user_budget_amount_from_uid(sender_uid)
    sender_budget = float(sender_budget)

    print(sender_budget)

    if sender_budget == -1:
         
        DatabaseHandler().update_transaction_record(tid, TransStatus.REJECTED.name)
        return 3
        
    if sender_budget < amount:
        DatabaseHandler().update_transaction_record(tid, TransStatus.REJECTED.name)
        return 4
        
    result = DatabaseHandler().process_transaction(tid)
    print("TRANSACTION RESULT", result)
    if result:
        result = DatabaseHandler().update_transaction_record(tid, TransStatus.APPROVED.name)
        if result:
            return 0 
    
    DatabaseHandler().update_transaction_record(tid, TransStatus.REJECTED.name)
    return 6


def reject_payment(tid):
    if not DatabaseHandler().check_transaction_exist(tid):
        return 1

    return 0 if DatabaseHandler().update_transaction_record(tid, TransStatus.REJECTED.name) else 2

def get_payment_record(tid):
    if not DatabaseHandler().check_transaction_exist(tid):
        return 1

    result = DatabaseHandler().get_transaction_record(tid)
    if len(result) == 0:
        return 2
    sender_name = get_user_name_from_uid(result[0])
    receiver_name = get_user_name_from_uid(result[1])
    sender_email = get_user_email(result[0])
    receiver_email = get_user_email(result[1])

    return {
        "sender_uid": result[0],
        "receiver_uid":result[1],
        "amount": result[2],
        "trans_status": result[3],
        "sender_name": sender_name,
        "receiver_name": receiver_name,
        "sender_email": sender_email,
        "receiver_email": receiver_email
    }
    
def get_payment_history(uid):
    result = DatabaseHandler().get_user_transaction_history(uid)

    processed = []
    for record in result:
        processed.append({
            "sender": get_user_name_from_uid(record[1]),
            "sender_uid": record[1],
            "receiver": get_user_name_from_uid(record[2]),
            "receiver_uid": record[2],
            "amount": record[3],
            "time": record[4].strftime("%m/%d/%y, %H:%M:%S"),
            "trans_status": record[5]
        })
    
    processed.sort(key=lambda x: x["time"], reverse=True)

    return processed

def get_wallet_balance(uid):

    result = DatabaseHandler().get_user_budget_amount_from_uid(uid)

    if result == False:
        return -1

    return result


if __name__ == "__main__":
    pass
