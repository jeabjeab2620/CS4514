/*
server: fyp-payment-system
user:
password:
 */
CREATE DATABASE PaymentSystem;

USE PaymentSystem;

CREATE TABLE PUSER(
    UserID varchar(255) NOT NULL,
    Email varchar(255) NOT NULL,
    Passwd varchar(255) NOT NULL,
    UName varchar(255) NOT NULL,
    HKID varchar(255) NOT NULL,
    PRIMARY KEY(UserID)
);

CREATE TABLE TRANSACTION_RECORD(
    TID varchar(255) NOT NULL,
    Sender varchar(255) NOT NULL,
    Receiver varchar(255) NOT NULL,
    Amount FLOAT(53) NOT NULL,
    TransTime DATETIME NOT NULL,
    TransStatus varchar(255) NOT NULL,
    PRIMARY KEY(TID),
    FOREIGN KEY (Sender) REFERENCES PUSER(UserID),
    FOREIGN KEY (Receiver) REFERENCES PUSER(UserID)
);

CREATE TABLE WALLET(
    UserID varchar(255) NOT NULL,
    Deposit FLOAT(53) NOT NULL,
    Cert_Loc varchar(255) NOT NULL,
    Public_Key_Loc varchar(255) NOT NULL,
    FOREIGN KEY(UserID) REFERENCES PUSER(UserID),
    PRIMARY KEY(UserID)
);


/*Example Data */
INSERT INTO PUSER (UserID, Email, passwd, UName, HKID) VALUES ('1234', 'test01@test.com', '$2b$12$woRAELFt.ggE8SoJ2gvx/eUsIZP2sBp7Mu9iygWyR7Ch4m.Z6EM.2', 'test01', 'F011111');
INSERT INTO PUSER (UserID, Email, passwd, UName, HKID) VALUES ('1235', 'test02@test.com', '$2b$12$woRAELFt.ggE8SoJ2gvx/eUsIZP2sBp7Mu9iygWyR7Ch4m.Z6EM.2', 'test02', 'F011111');