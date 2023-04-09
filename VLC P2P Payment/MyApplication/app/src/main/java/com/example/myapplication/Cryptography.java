package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

public class Cryptography {

    private static byte[] pemToByte(String pem) throws UnsupportedEncodingException {
        pem = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "")
                .replaceAll(System.lineSeparator(), "");

        return pem.getBytes("utf-8");
    }

    private static Certificate stringToCertificate(String cert) {

        try {
            byte[] cert_byte = cert.getBytes(StandardCharsets.UTF_8);
            Certificate c = CertificateFactory.getInstance("X.509").generateCertificate(
                    new ByteArrayInputStream(cert_byte)
            );

            return c;
        } catch (Exception e){
            Log.e("Cryptography", e.toString());
        }


        return null;
    }
    public static void generateRSAKeyPair(){
        try {
           KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");

           kpg.initialize(
                   new KeyGenParameterSpec.Builder(
                           "key1",
                           KeyProperties.PURPOSE_DECRYPT | KeyProperties.PURPOSE_ENCRYPT)
                           //.setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                           .setKeySize(4096)
                           .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                           //.setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                           //.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                           //.setRandomizedEncryptionRequired(true)
                           .build()

           );
           KeyPair keyPair = kpg.generateKeyPair();




        }
        catch (Exception e){
            Log.e("Cryptography", ""+e);
        }
    }

    public static String publicKeyToPem(PublicKey publicKey)
    {
        return "-----BEGIN PUBLIC KEY-----\n" + Base64.getMimeEncoder().encodeToString(publicKey.getEncoded()) + "\n-----END PUBLIC KEY-----\n";
    }


    public static void storeCertificate(String alias,String cert){
        try{
            Certificate c = Cryptography.stringToCertificate(cert);

            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);

            ks.setEntry(alias, new KeyStore.TrustedCertificateEntry(c), null);

            Log.d("Certificate", alias + " has been stored.");

        } catch (Exception e)
        {
            Log.e("Cryptography", e.toString());
        }
    }

    public static void storeRefreshToken(Context context, String alias, String token){
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString(alias, token);

            editor.commit();
    }

    public static String getRefreshToken(Context context, String alias){
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        String jwtToken = sharedPreferences.getString(alias, null);
        Log.d("REFRESH TOKEN", jwtToken);
        return jwtToken;
    }

    public static Certificate getCertificate(String alias) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);
        return ks.getCertificate(alias);
    }

    public static PrivateKey getPrivateKey(String alias) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException, UnrecoverableEntryException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        KeyStore.Entry entry = keyStore.getEntry(alias, null);

        PrivateKey privateKey = ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
        return privateKey;
    }

    public static String encrypt_rsa(String message){
        try{
            PublicKey pk = Cryptography.getCertificate("certificate").getPublicKey();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, pk);
            byte[] encryptedBytes = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));

            return Base64.getMimeEncoder().encodeToString(encryptedBytes);

        } catch(Exception e){
            Log.e("Encrypt RSA", e.toString());
        }
        return null;
    }
    public static String decrypt_rsa(String cipherText){
        try {
            //cipherText = cipherText.replace("/n","");
            PrivateKey pk = Cryptography.getPrivateKey("key1");
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, pk);
            return new String(cipher.doFinal(Base64.getMimeDecoder().decode(cipherText)));

        } catch (Exception e)
        {
            Log.e("Decrypt RSA", e.toString());
            e.printStackTrace();
        }
        return "";
    }

    public static Boolean verify_certificate_with_authority(Certificate cert){
        try {
            //verify with authority's public key
            cert.verify(Cryptography.getCertificate("authority_certificate").getPublicKey());
            Log.d("Verify Certificate", "Valid Certificate");
            return true;
        } catch(Exception e)
        {
            Log.e("Verify Certificate", "Invalid Certificate");
            return false;
        }
    }

}
