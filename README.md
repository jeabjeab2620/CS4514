# Final Year Project (P2P Payment System using Light Communication)
This is a final year project for implementing p2p payment application using visible light communication for android.

## Environment
- Python 3.10
- MS SQL database server on Azure
- Android Studio Dolphin | 2021.3.1 Patch 1 Build #AI-213.7172.25.2113.9123335, built on September 30, 2022
- 2 x Samsung S20 FE(SM-G7810) Android Version 13

## Installation
### 1. Set Up Database and Authority Server
1. Set up MS SQL database server.
2. Use [setup_database.sql](setup_database.sql) to inject the database schema
3. Change environment variables to connect to the database in [./authority server/.env](authority&#32;server/.env)
```
db_server = address to the database server
db_database = database name
db_username = username to connect to the database
db_password = password to connect to the database
db_driver = driver used for python to connect to SQL server
```
3. install required packages for authority server using [requirements.txt](requirements.txt)
```bash
pip install -r requirements.txt
```

### 2. Install Application on Android Device
1. Using Android Studio, open [./VLC P2P Payment/MyApplication/](VLC&#32;P2P&#32;Payment/MyApplication/) directory
2. After opening the project, open [app/java/com.example.myapplication/APIRequest.java](VLC&#32;P2P&#32;Payment/MyApplication/app/src/main/java/com/example/myapplication/APIRequest.java)
3. Modify the server address in the code to point to the authority server's address with port 8000
```java
public class APIRequest {
    private String server = "http://{your server IP address}:8000";
...
```
4. Connect Android device to the computer and run the application
