# Final-Year-Project
This is a final year project for implementing p2p payment application using visible light communication for android.

## Environment
- Python 3.10
- MS SQL database server on Azure
- Android Studio Dolphin | 2021.3.1 Patch 1 Build #AI-213.7172.25.2113.9123335, built on September 30, 2022
- 2 x Samsung S20 FE(SM-G7810) Android Version 13

## Installation
### 1. Set Up Database and Authority Server
1. Set up MS SQL database server.
2. Change environment variables to connect to the database in ./authority server/.env<br>
```
db_server = address to the database server
db_database = database name
db_username = username to connect to the database
db_password = password to connect to the database
db_driver = driver used for python to connect to SQL server
```
3. install required packages for authority server using requirements.txt
```bash
pip install -r requirements.txt
```
