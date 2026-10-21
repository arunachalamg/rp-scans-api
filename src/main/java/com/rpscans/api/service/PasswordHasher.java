package com.rpscans.api.service;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Component;
@Component
public class PasswordHasher {
 private static final int ITERATIONS=210000;
 public String hash(String password) {
  byte[] salt=new byte[16];new SecureRandom().nextBytes(salt);
  return "pbkdf2$"+ITERATIONS+"$"+Base64.getEncoder().encodeToString(salt)+"$"+Base64.getEncoder().encodeToString(derive(password,salt,ITERATIONS));
 }
 public boolean matches(String password,String stored){
  try{
   String[] p=stored.split("\\$");
   if(p.length!=4||!p[0].equals("pbkdf2"))return false;
   return MessageDigest.isEqual(derive(password,Base64.getDecoder().decode(p[2]),Integer.parseInt(p[1])),Base64.getDecoder().decode(p[3]));
  }catch(Exception e){return false;}
 }
 private byte[] derive(String password,byte[] salt,int iterations){
  try{
   PBEKeySpec spec=new PBEKeySpec(password.toCharArray(),salt,iterations,256);
   try{return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();}
   finally{spec.clearPassword();}
  }catch(Exception e){throw new IllegalStateException("Password hashing unavailable",e);}
 }
}
