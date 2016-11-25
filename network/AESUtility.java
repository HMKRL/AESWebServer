package network;

public class AESUtility {
	private char[] key;
	private char[] iv;
	private int[] AES_Sbox;
	private int[] AES_Sbox_Inv;
	private int[] AES_ShiftRowTab;
	private int[] AES_ShiftRowTab_Inv;
	private int[] AES_xtime;

	public AESUtility() {
		AES_Sbox = new int[] {99,124,119,123,242,107,111,197,48,1,103,43,254,215,171,
				118,202,130,201,125,250,89,71,240,173,212,162,175,156,164,114,192,183,253,
				147,38,54,63,247,204,52,165,229,241,113,216,49,21,4,199,35,195,24,150,5,154,
				7,18,128,226,235,39,178,117,9,131,44,26,27,110,90,160,82,59,214,179,41,227,
				47,132,83,209,0,237,32,252,177,91,106,203,190,57,74,76,88,207,208,239,170,
				251,67,77,51,133,69,249,2,127,80,60,159,168,81,163,64,143,146,157,56,245,
				188,182,218,33,16,255,243,210,205,12,19,236,95,151,68,23,196,167,126,61,
				100,93,25,115,96,129,79,220,34,42,144,136,70,238,184,20,222,94,11,219,224,
				50,58,10,73,6,36,92,194,211,172,98,145,149,228,121,231,200,55,109,141,213,
				78,169,108,86,244,234,101,122,174,8,186,120,37,46,28,166,180,198,232,221,
				116,31,75,189,139,138,112,62,181,102,72,3,246,14,97,53,87,185,134,193,29,
				158,225,248,152,17,105,217,142,148,155,30,135,233,206,85,40,223,140,161,
				137,13,191,230,66,104,65,153,45,15,176,84,187,22};

		AES_ShiftRowTab = new int[] {0,5,10,15,4,9,14,3,8,13,2,7,12,1,6,11};

		AES_Sbox_Inv = new int[256];
		AES_ShiftRowTab_Inv = new int[16];
		AES_xtime = new int[256];
	}
	
	public String Decrypt(String Key, String IV, String Mode, char[] cipherText) {
		String plainText = new String("");
		key = Key.toCharArray();
		iv = IV.toCharArray();
		AES_Init();
		AES_ExpandKey();
		
		while(cipherText.length > 0) {
			char[] block = new char[16];
			int len = cipherText.length;
				for(int i = 0; i < 16; i++) {
				if(i < len) block[i] = cipherText[i];
				else block[i] = ' ';
			}
			char[] t = new char[len-16];
			for(int i = 0;i < len - 16;++i) {
				t[i] = cipherText[i + 16];
			}
			cipherText = t;

			if(Mode.equals("ECB")) {
				AES_Decrypt(block);
			}
			else if(Mode.equals("CBC")) {
				char[] newiv = block.clone();
				AES_Decrypt(block);
				for (int i = 0; i < 16; i++) {
					block[i] ^= iv[i];
				}
				iv = newiv;
			}
			else if(Mode.equals("CFB")) {
				AES_Encrypt(iv);
				char[] newiv = block.clone();
				for (int i = 0; i < 16; i++) {
					block[i] ^= iv[i];
				}
				iv = newiv;
			}
			else if(Mode == "OFB") {
				AES_Encrypt(iv);
				char[] newiv = iv.clone();
				for (int i = 0; i < 16; i++) {
					block[i] ^= iv[i];
				}
				iv = newiv;
			}
			else {
				AES_Encrypt(iv);
				for (int i = 0; i < 16; i++) {
					block[i] ^= iv[i];
				}
				//LFSR
				char tmp = (char)(iv[8] ^ iv[7] ^ iv[6] ^ iv[3]);
				for (int i = 15; i > 0; i--) {
					iv[i] = iv[i - 1];
				}
				iv[0] = tmp;
			}
			plainText += new String(block);
		}
		return plainText;
	}

	public void AES_SubBytes(char[] state, int[] sbox) {
		for(int i = 0; i < 16; i++)
			state[i] = (char)sbox[state[i]];
	}

	public void AES_AddRoundKey(char[] state, char[] rkey) {
		for(int i = 0; i < 16; i++)
			state[i] ^= rkey[i];
	}

	public void AES_ShiftRows(char[] state, int[] shifttab) {
		char[] h = state.clone();
		for(int i = 0; i < 16; i++)
			state[i] = (char)h[shifttab[i]];
	}

	public void AES_MixColumns(char[] state) {
		for(int i = 0; i < 16; i += 4) {
			char s0 = state[i + 0], s1 = state[i + 1];
			char s2 = state[i + 2], s3 = state[i + 3];
			char h = (char)(s0 ^ s1 ^ s2 ^ s3);
			state[i + 0] ^= h ^ AES_xtime[s0 ^ s1];
			state[i + 1] ^= h ^ AES_xtime[s1 ^ s2];
			state[i + 2] ^= h ^ AES_xtime[s2 ^ s3];
			state[i + 3] ^= h ^ AES_xtime[s3 ^ s0];
		}
	}

	public void AES_MixColumns_Inv(char[] state) {
		for(int i = 0; i < 16; i += 4) {
			char s0 = state[i + 0], s1 = state[i + 1];
			char s2 = state[i + 2], s3 = state[i + 3];
			char h = (char)(s0 ^ s1 ^ s2 ^ s3);
			char xh = (char)AES_xtime[h];
			char h1 = (char)(AES_xtime[AES_xtime[xh ^ s0 ^ s2]] ^ h);
			char h2 = (char)(AES_xtime[AES_xtime[xh ^ s1 ^ s3]] ^ h);
			state[i + 0] ^= h1 ^ AES_xtime[s0 ^ s1];
			state[i + 1] ^= h2 ^ AES_xtime[s1 ^ s2];
			state[i + 2] ^= h1 ^ AES_xtime[s2 ^ s3];
			state[i + 3] ^= h2 ^ AES_xtime[s3 ^ s0];
		}
	}

	// AES_Init: initialize the tables needed at runtime. 
	// Call this function before the (first) key expansion.
	public void AES_Init() {
		for(int i = 0; i < 256; i++)
			AES_Sbox_Inv[AES_Sbox[i]] = i;

		for(int i = 0; i < 16; i++)
			AES_ShiftRowTab_Inv[AES_ShiftRowTab[i]] = i;

		for(int i = 0; i < 128; i++) {
			AES_xtime[i] = i << 1;
			AES_xtime[128 + i] = (i << 1) ^ 0x1b;
		}
	}

	/* AES_ExpandKey: expand a cipher key. Depending on the desired encryption 
	   strength of 128, 192 or 256 bits 'key' has to be a char array of length 
	   16, 24 or 32, respectively. The key expansion is done "in place", meaning 
	   that the array 'key' is modified.
	 */  
	public void AES_ExpandKey() {
		int kl = key.length, ks, Rcon = 1, i, j;
		char[] temp = new char[4], temp2 = new char[4];
		switch (kl) {
		case 16: ks = 16 * (10 + 1); break;
		case 24: ks = 16 * (12 + 1); break;
		case 32: ks = 16 * (14 + 1); break;
		default: 
			ks = 0;
			System.out.printf("AES_ExpandKey: Only key lengths of 16, 24 or 32 chars allowed!");
		}

		char[] newKey = new char[ks];
		for(int k = 0;k < kl;k++) newKey[k] = key[k];

		for(i = kl; i < ks; i += 4) {
			for(int k = 0;k < 4;k++) {
				temp[k] = newKey[i - 4 + k]; 
			}
			if (i % kl == 0) {
				temp2[0] = (char)(AES_Sbox[temp[1]] ^ Rcon);
				temp2[1] = (char)AES_Sbox[temp[2]];
				temp2[2] = (char)AES_Sbox[temp[3]];
				temp2[3] = (char)AES_Sbox[temp[0]];
				temp = temp2.clone();
				if ((Rcon <<= 1) >= 256) Rcon ^= 0x11b;
			}
			else if ((kl > 24) && (i % kl == 16)) {
				temp2[0] = (char)AES_Sbox[temp[0]];
				temp2[1] = (char)AES_Sbox[temp[1]];
				temp2[2] = (char)AES_Sbox[temp[2]];
				temp2[3] = (char)AES_Sbox[temp[3]];
				temp = temp2.clone();
			}
			for(j = 0; j < 4; j++)
				newKey[i + j] = (char)(newKey[i + j - kl] ^ temp[j]);
		}
		key = newKey.clone();
	}

	// AES_Encrypt: encrypt the 16 char array 'block' with the previously expanded key 'key'.
	public void AES_Encrypt(char[] block) {
		int l = key.length, i;
		char[] rkey = new char[16];
		AES_AddRoundKey(block, key);
		for(i = 16; i < l - 16; i += 16) {
			for(int k = 0;k < 16;k++) {
				rkey[k] = key[i + k]; 
			}
			AES_SubBytes(block, AES_Sbox);
			AES_ShiftRows(block, AES_ShiftRowTab);
			AES_MixColumns(block);
			AES_AddRoundKey(block, rkey);
		}
		for(int k = 0;k < 16;k++) {
			rkey[k] = key[l - 16 + k]; 
		}
		AES_SubBytes(block, AES_Sbox);
		AES_ShiftRows(block, AES_ShiftRowTab);
		AES_AddRoundKey(block, rkey);
	}

	// AES_Decrypt: decrypt the 16 char array 'block' with the previously expanded key 'key'.
	public void AES_Decrypt(char[] block) {
		int l = key.length, i;
		char[] rkey = new char[16];
		for(int k = 0;k < 16;k++) {
			rkey[k] = key[l - 16 + k]; 
		}
		AES_AddRoundKey(block, rkey);
		AES_ShiftRows(block, AES_ShiftRowTab_Inv);
		AES_SubBytes(block, AES_Sbox_Inv);

		for(i = l - 32; i >= 16; i -= 16) {
			for(int k = 0;k < 16;k++) {
				rkey[k] = key[i + k];
			}
			AES_AddRoundKey(block, rkey);
			AES_MixColumns_Inv(block);
			AES_ShiftRows(block, AES_ShiftRowTab_Inv);
			AES_SubBytes(block, AES_Sbox_Inv);
		}
		for(int k = 0;k < 16;k++) {
			rkey[k] = key[k];
		}
		AES_AddRoundKey(block, rkey);
	}
}
