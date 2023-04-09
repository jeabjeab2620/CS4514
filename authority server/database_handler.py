from database import Database
from datetime import datetime
from trans_status import TransStatus

class DatabaseHandler:
    def __init__(self):
        self.db = Database()

    def check_email_exist(self,email):
        condition ={
            "Email": ('=',email)
            }

        result = self.db.query(["Email"],"PUSER",condition)
        
        return True if len(result) else False
    
    
    def check_uid_exist(self, uid):        
        condition ={
            "UserID": ('=',uid)
            }

        result = self.db.query(["UserID"],"PUSER",condition)

        return True if len(result) else False
    
    def check_transaction_exist(self, tid):
        result = self.db.query(["TID"],
            "TRANSACTION_RECORD",
            {
                "TID": ("=", tid)
            })
        return True if len(result) else False  

    def add_user(self, uid, email, password, uname, hkid):
        if self.check_email_exist(email):
            return False
        
        data = {
            "UserID": uid,
            "Email": email,
            "Passwd": password,
            "UName": uname,
            "HKID": hkid,
        }

        return self.db.insert("PUSER",data)
    
    def delete_user(self, uid):
        result = self.db.delete(
            "WALLET",
            {
                "UserID": ("=",uid)
            }
        )
        result = result and self.db.delete(
            "PUSER",
            {
                "UserID": ("=",uid)
            }
        )
        return result

    def add_wallet(self, uid, cert_loc, pub_loc):
        data = {
            "UserID": uid,
            "Deposit": 1000,
            "Cert_Loc": cert_loc,
            "Public_Key_Loc": pub_loc
        }
        return self.db.insert("WALLET",data)

    def get_user_uid(self, email):
        result = self.db.query(
            ["UserID"], 
            "PUSER", 
            {
                "Email": ("=", email)
            },
        )

        if len(result) == 0:
            return ""
        return result[0][0]
    
    def get_user_name_from_uid(self, uid):
        result = self.db.query(
            ["UName"], 
            "PUSER", 
            {
                "UserID": ("=", uid)
            },
        )

        if len(result) == 0:
            return ""
        return result[0][0]

    def get_user_certificate_location(self, uid):
        result = self.db.query(
            ["Cert_Loc"], 
            "WALLET", 
            {
                "UserID": ("=", uid)
            },
        )

        return result[0][0]
    
    def get_user_email_from_uid(self, uid):
        result = self.db.query(
            ["Email"], 
            "PUSER", 
            {
                "UserID": ("=", uid)
            },
        )

        if len(result) == 0:
            return ""
        return result[0][0]

    def get_user_password(self, email):
        result = self.db.query(
            ["Passwd"],
            "PUSER",
            {
                "Email": ("=", email)
            }
        )

        if len(result) == 0:
            return ""

        return result[0][0]
    
    def get_user_budget_amount_from_uid(self, uid):
        result = self.db.query(
            ["Deposit"],
            "WALLET",
            {
                "UserID": ("=", uid)
            }
        )

        if len(result) == 0:
            return False
        
        return result[0][0]
    
    def add_transaction_record(self, tid, sender, receiver, amount):
        result = self.db.insert(
            "TRANSACTION_RECORD",
            {
                "TID": tid,
                "Sender": self.get_user_uid(sender),
                "Receiver": self.get_user_uid(receiver),
                "Amount": amount,
                "TransTime": datetime.now(),
                "TransStatus": TransStatus.REQUESTED.name
            }
        )
        return result
    
    def cancel_transaction(self, tid):
        result = self.db.update(
            "TRANSACTION_RECORD",
            {
                "TransStatus": TransStatus.REJECTED.name
            },
            {
                "TID": ("=", tid)
            }
        )

        return result




    def get_transaction_record(self, tid):
        result = self.db.query(
            ["Sender", "Receiver", "Amount", "TransStatus"], 
            "TRANSACTION_RECORD", 
            {
                "TID": ("=", tid)
            }
        )

        if len(result) == 0:
            return []

        return result[0]
    
    def add_user_budget(self, uid, amount):
        result = self.db.update(
            "WALLET", 
            {
                "Deposit": self.get_user_budget_amount_from_uid(uid) + amount,
            },
            {
                "UserID": ("=", uid)
            }
        )

        return result
    
    def deduct_user_budget(self, uid, amount):
        result = self.db.update(
            "WALLET",
            {
                "Deposit": self.get_user_budget_amount_from_uid(uid) - amount,
            },
            {
                "UserID": ("=", uid)
            }
        )

        return result
    
    def get_user_transaction_history(self, uid):
        result = []
        result += self.db.query(
            ["*"],
            "TRANSACTION_RECORD",
            {
                "Sender": ("=", uid)
            }
        )

        result += self.db.query(
            ["*"],
            "TRANSACTION_RECORD",
            {
                "Receiver": ("=", uid)
            }
        )


        if len(result) == 0:
            return []
        
        return result
    
    def process_transaction(self, tid):
        transaction = self.get_transaction_record(tid)
        sender_uid = transaction[0]
        receiver_uid = transaction[1]
        amount = transaction[2]
        status = transaction[3]

        if not (status == TransStatus.REQUESTED.name):
            return False
        
        deduct_result = self.deduct_user_budget(sender_uid, amount)
        add_result = False
        if deduct_result:
            print ("DEDUCT SUCCESS")
            add_result = self.add_user_budget(receiver_uid, amount)
        
        if add_result:
            print("ADD SUCCESS")
    
        #add back the amount if the amount is not added into receiver
        if deduct_result and not add_result:
            self.add_user_budget(sender_uid, amount)
        
        return add_result and deduct_result
    
    def update_transaction_record(self, tid, status):
        result = self.db.update(
            "TRANSACTION_RECORD", 
            {
                "TransStatus": status,
                "TransTime": datetime.now()
            },
            {
                "TID": ("=", tid)
            }
        )

        return result

if __name__ == "__main__":
    #a = DatabaseHandler().add_user_budget("3118449649", 900)
    print(DatabaseHandler().get_user_transaction_history(3118449649))
    
