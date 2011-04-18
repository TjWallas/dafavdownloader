/**
 *    EncryptionHelper - Utility to help encrypt and decrypt a property
 *    Copyright (C) 2009-2011  Philippe Busque
 *    https://sourceforge.net/projects/dafavdownloader/
 *    
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.dragoniade.encrypt;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.xerces.impl.dv.util.Base64;

public class EncryptionHelper {

	
	private static final String[] algorithmes = {"AES","Blowfish","RC2"};
	
	public static void encrypt(Properties p, String seedKey, String key) {
		String value = p.getProperty(key);
		String seed = p.getProperty(seedKey,seedKey);
		
		String encrypted;
		try {
			Cipher cipher = getEncrypter(seed);
			byte[] result = cipher.doFinal(value.getBytes("UTF-16"));
			encrypted = cipher.getAlgorithm() + "{" + Base64.encode(result) + "}";
		} catch (Exception e) {
			try {
				encrypted = "{" + Base64.encode(value.getBytes("UTF-16")) + "}";
			} catch (Exception e2) {
				encrypted = "{" + Base64.encode(value.getBytes()) + "}";
			}
		}

		p.setProperty(key, encrypted);
	}
	
	
	public static void decrypt(Properties p, String seedKey, String key) {
		String value = p.getProperty(key);
		String seed = p.getProperty(seedKey,seedKey);
		
		if (value == null || value.length() == 0) {
			return;
		}
		
		int index = value.indexOf('{');
		switch (index) {
		case -1:
			return;
		case 0: {
			String toDecode = value.substring(1,value.length() -1);
			String decryptStr;
			try {
				decryptStr = new String(Base64.decode(toDecode), "UTF-16");
			} catch (UnsupportedEncodingException e1) {
				decryptStr = new String(Base64.decode(toDecode));
			}
			
			p.setProperty(key, decryptStr);
			return;
		}
		default:
			String toDecode = value.substring(index+1,value.length() -1);
			String algorithm = value.substring(0,index);
			
			byte[] decryptStr = Base64.decode(toDecode);
			
			Cipher cipher = getDecrypter(seed,algorithm);
			
			String decoded;
			try {
				byte[] result = cipher.doFinal(decryptStr);
				try {
					decoded = new String(result, "UTF-16");
				} catch (UnsupportedEncodingException e1) {
					decoded = new String(result);
				}
			} catch (Exception e) {
				decoded = "";
			}
		
			
			p.setProperty(key, decoded);
		}
	}
	
	private static Cipher getEncrypter(String seed) {
		byte[] byteKey = getSeed(seed);
		for (int i=0; i< algorithmes.length; i++) {
			String algorithm = algorithmes[i];
			try {
				SecretKeySpec keySpec = new SecretKeySpec(byteKey, algorithm);
				Cipher encrypt = Cipher.getInstance(algorithm);			
				encrypt.init(Cipher.ENCRYPT_MODE, keySpec);
				return encrypt;
			}
			catch (Exception e) {
				continue;
			}
		}
		return null;
	}
	
	private static Cipher getDecrypter(String seed, String algorithm) {
		try {
			byte[] byteKey = getSeed(seed);
			SecretKeySpec keySpec = new SecretKeySpec(byteKey, algorithm);
			Cipher decrypt = Cipher.getInstance(algorithm);
			decrypt.init(Cipher.DECRYPT_MODE, keySpec);
			return decrypt;
		} catch (Exception e) {
			return null;
		}

	}
	
	private static byte[] getSeed(String seed) {
		byte[] xor = {-35, -54, -58, -91, -69,  23,  88, 103, -107, -95, 50, -100, 123, 57, -22, -87};
		byte[] byteKey = DigestUtils.md5(seed);
		for (int i=0; i< byteKey.length; i++) {
			byteKey[i] = (byte) (byteKey[i] ^ xor[i]);
		}
		return byteKey;
	}
	
	
	public static void main(String[] args) {
		Properties p = new Properties();
		p.setProperty("key", "mypassword");
		
		System.out.println(p.getProperty("key"));
		EncryptionHelper.encrypt(p,"username","key");
		System.out.println(p.getProperty("key"));
		EncryptionHelper.decrypt(p,"username","key");
		System.out.println(p.getProperty("key"));
		
	}
}
