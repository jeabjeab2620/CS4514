import pyodbc
from singleton import Singleton
from decouple import config
SERVER = config('db_server') #'fyp-db.database.windows.net'
DATABASE = config('db_database') #'PaymentSystem'
USERNAME = config('db_username')#'jeabjeab'
PASSWORD = config('db_password')#'JEABsocute123!'
DRIVER = config('db_driver') #'{ODBC Driver 18 for SQL Server}'

def list_to_str(i, remove_quotes=False):
    o = ", ".join( repr(e) if not (remove_quotes) else e for e in i)

    return o

class Database(metaclass=Singleton):
    def __init__(self):
        self.conn = pyodbc.connect('DRIVER='+DRIVER+';SERVER=tcp:'+SERVER+';PORT=1433;DATABASE='+DATABASE+';UID='+USERNAME+';PWD='+ PASSWORD)
    
    def _generate_safe_condition(self, condition:dict):    
        con = ""
        for col in condition.keys():
            con += col + condition[col][0] + '(?)' + ' AND'
        con = con[:-4]

        return (con, tuple( [i[1]for i in condition.values()]))

    def _generate_safe_update_set(self, data:dict) -> tuple:
        if len(data) == 0:
            return tuple()
        dat = ""
        
        for col in data.keys():
            dat += str(col) + "=" + "(?), "
        
        
        dat = dat[:-2]

        return (dat, tuple(list(data.values())))


    def query(self, columns:list, table:str, condition={"1":("=","1")}, unique=False):
        q = ""
        condition = self._generate_safe_condition(condition)
        
        if unique:
            q = "SELECT DISTINCT {} FROM {} WHERE {};".format(",".join(columns), table, condition[0])
        else:
            q = "SELECT {} FROM {} WHERE {};".format(",".join(columns), table, condition[0])
        
        results = []
        print("DEBUG:", q.replace("(?)","'%s'") % condition[1])
        
        with self.conn.cursor() as cursor:
            try:
                cursor.execute(q, condition[1])
                rows = cursor.fetchall()

                for row in rows:
                    results.append(list(row))
            except Exception as e:
                print("DEBUG:",e)
                return []
        
        return results
    
    def insert(self, table:str, data:dict):
        values = ""
        for i in range(len(data.keys())):
            values += "(?),"
        values = values[:-1]
        q = "INSERT INTO {} ({}) VALUES ({})".format(table, list_to_str(list(data.keys()), True), values)

        print("DEBUG:", q.replace('(?)',"'%s'") % tuple(list(data.values())))
        
        with self.conn.cursor() as cursor:
            
            try:
                cursor.execute(q, tuple(list(data.values())))
                self.conn.commit()

                return True
            except Exception as e:
                print(e)
                return False
    
    def delete(self, table: str, con:dict = {"1":("=","1")}):
        condition = self._generate_safe_condition(con)
        q = "DELETE FROM {} WHERE {}".format(table, condition[0])
        
        print("DEBUG:", q.replace("(?)","'%s'") % condition[1])

        with self.conn.cursor() as cursor:
            
            try:
                cursor.execute(q, condition[1])
                self.conn.commit()

                return True
            except Exception as e:
                print(e)
                return False
    
    def update(self, table: str, data:dict, con:dict):
        condition = self._generate_safe_condition(con)
        data = self._generate_safe_update_set(data)
        
        q = "UPDATE {} SET {} WHERE {}".format(table, data[0], condition[0])

        print("DEBUG:", q.replace('(?)', "'%s'") % (data[1] + condition[1]))
        with self.conn.cursor() as cursor:
            
            try:
                cursor.execute(q, (data[1] + condition[1]))
                self.conn.commit()

                return True
            except Exception as e:
                print(e)
                return False



if __name__ == "__main__":
    db = Database()
    data = {
        "UserID":"user004",
        "Email": "jeabjeab2620@gmail.com",
        "Passwd": "1234",
        "UName": "Melon",
        "HKID": "F047038(0)"
    }

    print(db.query(["*"],"PUSER",{"Email":('=','jeabjeab2620@gmail.com')}))
