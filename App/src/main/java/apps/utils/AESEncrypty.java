package apps.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES算法加密工具。
 * 
 * @author qindf
 * 
 */
public class AESEncrypty {

	/**
	 * 采用的算法名称。
	 */
	private final String ALGORITHM = "AES";

	/**
	 * Key生成器。
	 */
	private KeyGenerator kgen;

	/**
	 * 密钥字节码
	 */
	private byte[] skey;

	/**
	 * 使用密钥十六进制码构造加密工具。
	 * 
	 * @param skeyHex
	 *            密钥十六进制码。
	 */
	public AESEncrypty(String skeyHex) {
		if (skeyHex != null) {
			this.skey = this.hex2byte(skeyHex);

            Logger.d("AES-test", NumberUtils.bytes2HexString(skey));
		}
		initial();
	}

	/**
	 * 默认构造器。
	 */
	public AESEncrypty() {
		initial();
	}

	/**
	 * 初始化。安全密钥种子设置成随机生成,解决windows下密钥相同的问题
	 * 
	 */
	private void initial() {
		try {
			this.kgen = KeyGenerator.getInstance(ALGORITHM);
			kgen.init(128, new SecureRandom(UUID.randomUUID().toString()
					.getBytes()));
			if (skey == null) {
				skey = kgen.generateKey().getEncoded();
			}
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 获取密钥的十六进制码。
	 * 
	 * @return
	 */
	public String getSecuryKeyHex() {
		return this.byte2hex(skey);
	}

	/**
	 * 使用密钥，对传入的明文进行加密。 使用私有密钥。
	 * 
	 * @param plaintext
	 *            待加密明文。
	 * @return
	 */
	public String encrypt(String plaintext) {
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(skey, ALGORITHM);
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			byte[] encrypted = cipher.doFinal(plaintext.getBytes());
			return byte2hex(encrypted);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (NoSuchPaddingException e) {
			throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		} catch (IllegalBlockSizeException e) {
			throw new RuntimeException(e);
		} catch (BadPaddingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 对传入的密文进行解密。
	 * 
	 * @param ciphertext
	 *            待解密密文。
	 * @return
	 */
	public String decrypt(String ciphertext) {
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(skey, ALGORITHM);
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			byte[] encrypted = cipher.doFinal(hex2byte(ciphertext));
			return new String(encrypted);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (NoSuchPaddingException e) {
			throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		} catch (IllegalBlockSizeException e) {
			throw new RuntimeException(e);
		} catch (BadPaddingException e) {
			throw new RuntimeException(e);
		}
	}

	private String byte2hex(byte[] data) {
		StringBuilder sb = new StringBuilder();
		String stmp = "";
		for (int i = 0; i < data.length; i++) {
			stmp = Integer.toHexString(data[i] & 0XFF);
			if (stmp.length() == 1) {
				sb.append(0);
			}
			sb.append(stmp);
		}
		return sb.toString().toUpperCase();
	}

	private byte[] hex2byte(String hex) throws IllegalArgumentException {
		if (hex == null || hex.length() % 2 != 0) {
			throw new IllegalArgumentException();
		}
		char[] arr = hex.toCharArray();
		byte[] b = new byte[hex.length() / 2];
		for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++) {
			String swap = "" + arr[i++] + arr[i];
			int byteint = Integer.parseInt(swap, 16) & 0xFF;
			b[j] = new Integer(byteint).byteValue();
		}
		return b;
	}
}
