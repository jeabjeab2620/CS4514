from cryptography.hazmat.primitives.asymmetric import rsa, padding
from cryptography.hazmat.primitives import serialization, hashes
from cryptography.hazmat.backends import default_backend
from OpenSSL import crypto
import json
import random

class Cryptography:
    @staticmethod
    def get_value():
        return

class PublicPrivateKey(Cryptography):
    @staticmethod
    def get_value():
        private_key = rsa.generate_private_key(
            public_exponent=65537,
            key_size=4096,
            backend= default_backend()
        )

        public_key = private_key.public_key()

        return (private_key, public_key)
    
    @staticmethod
    def read_private_key(file_path) -> rsa.RSAPrivateKey:
        file = open(file_path, "rb")
        private_key = serialization.load_pem_private_key(
            file.read(),
            password=None,
            backend=default_backend()
        )

        file.close()

        return private_key
    
    @staticmethod
    def read_public_key(file_path) -> rsa.RSAPublicKey:
        file = open(file_path, "rb")
        public_key = serialization.load_pem_public_key(
            file.read(),
            backend=default_backend()
        )

        file.close()

        return public_key
    
    @staticmethod
    def read_public_key_from_value(value: str) -> rsa.RSAPublicKey:
        public_key = serialization.load_pem_public_key(
            bytes(value, 'utf-8'),
            backend=default_backend()
        )

        return public_key
    @staticmethod
    def save_public_key(uid, public_key):

        public_key = PublicPrivateKey.read_public_key_from_value(public_key)
        try:
                public_pem = public_key.public_bytes(
                    encoding= serialization.Encoding.PEM,
                    format=serialization.PublicFormat.SubjectPublicKeyInfo
                )
                with open(f'./user_keys/{uid}_public_key.pem', 'wb') as f:
                    f.write(public_pem)
        except:
            return False;
        
        return True;

    @staticmethod
    def save_key_pair(uid, public_private_key_pair):
        try:
            private_pem = public_private_key_pair[0].private_bytes(
                encoding= serialization.Encoding.PEM,
                format=serialization.PrivateFormat.PKCS8,
                encryption_algorithm= serialization.NoEncryption()
            )

            public_pem = public_private_key_pair[1].public_bytes(
                encoding= serialization.Encoding.PEM,
                format=serialization.PublicFormat.SubjectPublicKeyInfo
            )
            with open(f'./user_keys/{uid}_private_key.pem', 'wb') as f:
                f.write(private_pem)
            
            with open(f'./user_keys/{uid}_public_key.pem', 'wb') as f:
                f.write(public_pem)
        except:
            return False
        return True
    

class DigitalCertificate(Cryptography):
    @staticmethod
    def generate_ca_cert(private_key_path='./authority/authority_private_key.pem'):
        try:
            with open(private_key_path, 'r') as f:
                ca_key = crypto.load_privatekey(crypto.FILETYPE_PEM,f.read())

            ca_cert = crypto.X509()
            ca_cert.set_version(2)
            ca_cert.set_serial_number(random.randint(50000000, 100000000))

            ca_subj = ca_cert.get_subject()
            ca_subj.countryName = input("Country Name (2 letter code) [XX]: ")
            ca_subj.stateOrProvinceName = input("State or Province Name (full name) []: ")
            ca_subj.localityName = input("Locality Name (eg, city) [Default City]: ")
            ca_subj.organizationName = input("Organization Name (eg, company) [Default Company Ltd]: ")
            ca_subj.emailAddress = input("Email Address []: ")
            
            ca_cert.set_issuer(ca_subj)
            ca_cert.set_pubkey(ca_key)

            ca_cert.add_extensions([
                crypto.X509Extension(b"subjectKeyIdentifier", False, b"hash", subject=ca_cert),
            ])

            ca_cert.add_extensions([
                crypto.X509Extension(b"authorityKeyIdentifier", False, b"keyid:always,issuer", issuer=ca_cert),
            ])

            ca_cert.add_extensions([
                crypto.X509Extension(b"basicConstraints", True, b"CA:TRUE"),
                #crypto.X509Extension(b"keyUsage", True, b"digitalSignature, keyCertSign, cRLSign"),
            ])

            ca_cert.gmtime_adj_notBefore(0)
            ca_cert.gmtime_adj_notAfter(10*365*24*60*60)

            ca_cert.sign(ca_key, 'sha256')

            # Save certificate
            with open('./authority/authority_cert.crt', "wt") as f:
                f.write(crypto.dump_certificate(crypto.FILETYPE_PEM, ca_cert).decode("utf-8"))
            
            return True
        except:
            return False

    @staticmethod
    def get_value(uid, email, uname, public_key):
        try:
            public_key = crypto.load_publickey(crypto.FILETYPE_PEM, public_key)

            with open('./authority/authority_private_key.pem', 'r') as f:
                authority_private_key = crypto.load_privatekey(crypto.FILETYPE_PEM, f.read())

            with open('./authority/authority_cert.crt', 'r') as f:
                authority_certificate = crypto.load_certificate(crypto.FILETYPE_PEM, f.read())
            
            client_cert = crypto.X509()

            client_cert.set_version(2)
            client_cert.set_serial_number(random.randint(50000000, 100000000))

            client_sub = client_cert.get_subject()
            client_sub.O = uname
            client_sub.ST = 'Hong Kong'
            client_sub.C = 'HK'
            client_sub.emailAddress = email

            client_cert.set_issuer(authority_certificate.get_subject())
            client_cert.set_pubkey(public_key)

            client_cert.add_extensions([
                crypto.X509Extension(b"basicConstraints", False, b"CA:FALSE"),
            ])

            client_cert.add_extensions([
                crypto.X509Extension(b"authorityKeyIdentifier", False, b"keyid", issuer=authority_certificate),
                #crypto.X509Extension(b"extendedKeyUsage", False, b"serverAuth"),
                crypto.X509Extension(b"keyUsage", True, b"digitalSignature, keyEncipherment"),
            ])

            client_cert.add_extensions([
                crypto.X509Extension(b"subjectKeyIdentifier", False, b"hash", subject=client_cert),
            ])
    
            client_cert.gmtime_adj_notBefore(0)
            client_cert.gmtime_adj_notAfter(365*24*60*60)

            client_cert.sign(authority_private_key, 'sha256')

            return client_cert

        except Exception as e:
            print("DEBUG: Exception ",e)
            return None
    
    @staticmethod
    def save_certificate(uid, certificate):
        try:
            with open(f'./user_certificates/{uid}.crt', "wb") as f:
                f.write(crypto.dump_certificate(crypto.FILETYPE_PEM, certificate))
                return True
        except:
            return False

    @staticmethod
    def read_certificate(file_path):
        try:
            with open(file_path, 'r') as f:
                certificate = crypto.load_certificate(crypto.FILETYPE_PEM, f.read())
                return certificate
        except:
            return None
    
    @staticmethod
    def convert_to_pem(cert:crypto.X509)->str:
        return crypto.dump_certificate(crypto.FILETYPE_PEM, cert).decode("utf-8")
    
    @staticmethod
    def validate_certificate(uid,authority_cert_path= './authority/authority_cert.crt'):
        authority_cert = crypto.load_certificate(crypto.FILETYPE_PEM, open(authority_cert_path).read())
        user_cert = crypto.load_certificate(crypto.FILETYPE_PEM, open(f'./user_certificates/{uid}.crt','rb').read())

        store = crypto.X509Store()
        store.add_cert(authority_cert)

        ctx = crypto.X509StoreContext(store, user_cert)

        try:
            ctx.verify_certificate()

            return True
        except:
            return False

if __name__ == "__main__":
    data = {
        "key": "abcd"
    }
